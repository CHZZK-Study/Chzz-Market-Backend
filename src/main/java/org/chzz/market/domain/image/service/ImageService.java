package org.chzz.market.domain.image.service;

import com.amazonaws.AmazonServiceException;
import com.amazonaws.services.s3.AmazonS3;
import lombok.RequiredArgsConstructor;
import org.chzz.market.domain.image.entity.Image;
import org.chzz.market.domain.image.error.ImageErrorCode;
import org.chzz.market.domain.image.error.exception.ImageException;
import org.chzz.market.domain.image.repository.ImageRepository;
import org.chzz.market.domain.product.entity.Product;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ImageService {

    private static final Logger logger = LoggerFactory.getLogger(ImageService.class);

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
    public List<String> uploadImages(List<MultipartFile> images) {
        List<String> uploadedUrls = images.stream()
                .map(this::uploadImage)
                .collect(Collectors.toList());

        uploadedUrls.forEach(url -> logger.info("업로드 된 이미지 : {}", getFullImageUrl(url)));

        return uploadedUrls;
    }

    /**
     * 단일 이미지 파일 업로드 및 CDN 경로 리스트 반환
     */
    private String uploadImage(MultipartFile image) {
        return imageUploader.uploadImage(image);
    }

    /**
     * 상품에 대한 이미지 Entity 생성 및 저장
     */
    @Transactional
    public void saveProductImageEntities(Product product, List<String> cdnPaths) {
        cdnPaths.forEach(cdnPath -> {
            Image imageEntity = Image.builder()
                    .cdnPath(cdnPath)
                    .product(product)
                    .build();
            imageRepository.save(imageEntity);
            logger.info("상품에 저장된 이미지 Entity {}: {}", product.getId(), getFullImageUrl(cdnPath));
        });
    }

    /**
     * 업로드된 이미지 삭제
     */
    public void deleteUploadImages(List<String> cdnPaths) {
        cdnPaths.forEach(this::deleteImage);
    }

    /**
     * 단일 이미지 삭제
     */
    private void deleteImage(String cdnPath) {
        try {
            String key = cdnPath.substring(1);
            amazonS3Client.deleteObject(bucket, key);
        } catch (AmazonServiceException e) {
            throw new ImageException(ImageErrorCode.IMAGE_DELETE_FAILED);
        }
    }

    /**
     * CDN 경로로부터 전체 이미지 URL 재구성
     * 이미지 -> 서버에 들어왔는지 확인하는 로그에 사용
     */
    public String getFullImageUrl(String cdnPath) {
        return "https://" + cloudfrontDomain + cdnPath;
    }
}

