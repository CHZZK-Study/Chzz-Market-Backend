package org.chzz.market.domain.image.service;

import static org.chzz.market.domain.image.error.ImageErrorCode.IMAGE_DELETE_FAILED;
import static org.chzz.market.domain.image.error.ImageErrorCode.INVALID_IMAGE_EXTENSION;
import static org.chzz.market.domain.image.error.ImageErrorCode.MAX_IMAGE_COUNT_EXCEEDED;
import static org.chzz.market.domain.image.error.ImageErrorCode.NOT_FOUND;
import static org.chzz.market.domain.image.error.ImageErrorCode.NO_IMAGES_PROVIDED;

import com.amazonaws.AmazonServiceException;
import com.amazonaws.services.s3.AmazonS3;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.stream.IntStream;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.chzz.market.domain.image.entity.Image;
import org.chzz.market.domain.image.error.exception.ImageException;
import org.chzz.market.domain.image.repository.ImageRepository;
import org.chzz.market.domain.product.dto.UpdateProductRequest;
import org.chzz.market.domain.product.entity.Product;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ImageService {
    private final ImageUploader imageUploader;
    private final ImageRepository imageRepository;
    private final AmazonS3 amazonS3Client;

    @Value("${cloud.aws.cloudfront.domain}")
    private String cloudfrontDomain;

    @Value("${cloud.aws.s3.bucket}")
    private String bucket;

    /**
     * 여러 이미지 파일 업로드 및 CDN 경로 리스트 반환
     */
    public List<String> uploadImages(List<MultipartFile> images) {//TODO 2024 10 07 23:18:22 : 이미지 벌크 업로드 방법 강구
        List<String> uploadedUrls = images.stream()
                .map(this::uploadImage)
                .toList();
        log.info("업로드 된 이미지 리스트: {}", uploadedUrls);
        return uploadedUrls;
    }

    /**
     * 이미지 갯수의 범위 확인
     */
    public void validateImageSize(Long productId) {
        long count = imageRepository.countByProductId(productId);
        // 삭제 연산 이후 갯수 확인
        if (count < 1) {
            throw new ImageException(NO_IMAGES_PROVIDED);
        } else if (count > 5) {
            throw new ImageException(MAX_IMAGE_COUNT_EXCEEDED);
        }
    }

    /**
     * 단일 이미지 파일 업로드 및 CDN 전체경로 리스트 반환
     */
    //@Transactional(propagation = Propagation.NOT_SUPPORTED)// 써야하려나? 아니면 이벤트기반?
    public String uploadImage(MultipartFile image) {
        String uniqueFileName = createUniqueFileName(Objects.requireNonNull(image.getOriginalFilename()));
        String s3Key = imageUploader.uploadImage(image, uniqueFileName);
        return cloudfrontDomain + "/" + s3Key;
    }

    /**
     * 상품에 대한 이미지 Entity 생성 및 저장
     */
    @Transactional
    public List<Image> saveProductImageEntities(Product product, List<String> cdnPaths) {
        List<Image> images = IntStream.range(0, cdnPaths.size())
                .mapToObj(i -> Image.builder()
                        .cdnPath(cdnPaths.get(i))
                        .product(product)
                        .sequence((i + 1))
                        .build())
                .toList();

        imageRepository.saveAll(images);
        return images;
    }

    @Transactional
    public List<Image> uploadSequentialImages(Map<String, MultipartFile> newImages) {
        List<Image> images = newImages.entrySet().stream()
                .map(entry -> {
                    int sequence = Integer.parseInt(entry.getKey());
                    MultipartFile multipartFile = entry.getValue();
                    String cdnPath = uploadImage(multipartFile);
                    return Image.builder()
                            .sequence(sequence)
                            .cdnPath(cdnPath)
                            .build();
                }).toList();
        imageRepository.saveAll(images);
        return images;
    }

    /**
     * @param productId 상품 ID
     * @param imageIds  남아있을 이미지 ID
     */
    @Transactional
    public void deleteImagesNotContainsIdsOf(Long productId, Set<Long> imageIds) {
        imageRepository.deleteImagesNotContainsIdsOf(productId, imageIds);
    }

    /**
     * 시퀀스 업데이트
     */
    @Transactional
    public void updateSequence(Map<Long, Integer> imageSequence) {
        imageSequence.forEach((imageId, sequence) -> {
            Image image = imageRepository.findById(imageId)
                    .orElseThrow(() -> new ImageException(NOT_FOUND));
            image.setSequence(sequence);
        });
    }

    /**
     * 기존 이미지중 제거할 이미지 삭제 후 시퀀스 수정
     */
    public void updateExistingImages(Product product, UpdateProductRequest request) {
        deleteImagesNotContainsIdsOf(product.getId(), request.getImageSequence().keySet());//제거될 id들을 받아서 삭제
        updateSequence(request.getImageSequence());// 남아있는 시퀀스 매김
    }

    /**
     * 업로드된 이미지 삭제
     */
    public void deleteUploadImages(List<String> fullImageUrls) {
        fullImageUrls.forEach(this::deleteImage);
    }

    /**
     * 단일 이미지 삭제
     */
    private void deleteImage(String cdnPath) {
        try {
            URL url = new URL(cdnPath);
            String path = url.getPath();
            String key = path.substring(1);

            log.info("S3에서 객체 삭제 시도, Key : {}", key);
            amazonS3Client.deleteObject(bucket, key);
        } catch (AmazonServiceException | MalformedURLException e) {
            throw new ImageException(IMAGE_DELETE_FAILED);
        }
    }

    /**
     * 고유한 파일 이름 생성
     */
    private String createUniqueFileName(String originalFileName) {
        String uuid = UUID.randomUUID().toString();
        String extension = StringUtils.getFilenameExtension(originalFileName);

        if (extension == null || !isValidFileExtension(extension)) {
            throw new ImageException(INVALID_IMAGE_EXTENSION);
        }

        return uuid + "." + extension;
    }

    /**
     * 파일 확장자 검증
     */
    private boolean isValidFileExtension(String extension) {
        List<String> allowedExtensions = Arrays.asList("jpg", "jpeg", "png", "webp");
        return allowedExtensions.contains(extension.toLowerCase());
    }
}

