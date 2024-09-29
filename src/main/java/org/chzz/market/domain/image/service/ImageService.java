package org.chzz.market.domain.image.service;

import com.amazonaws.AmazonServiceException;
import com.amazonaws.services.s3.AmazonS3;
import lombok.RequiredArgsConstructor;
import org.chzz.market.domain.image.entity.Image;
import org.chzz.market.domain.image.error.exception.ImageException;
import org.chzz.market.domain.image.repository.ImageRepository;
import org.chzz.market.domain.product.entity.Product;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;

import static org.chzz.market.domain.image.error.ImageErrorCode.*;

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
                .toList();

        uploadedUrls.forEach(url -> logger.info("업로드 된 이미지 : {}", getFullImageUrl(url)));

        return uploadedUrls;
    }

    /**
     * 단일 이미지 파일 업로드 및 CDN 경로 리스트 반환
     */
    private String uploadImage(MultipartFile image) {
        String cdnPath = imageUploader.uploadImage(image);
        return getFullImageUrl(cdnPath);
    }

    /**
     * 상품에 대한 이미지 Entity 생성 및 저장
     */
    @Transactional
    public List<Image> saveProductImageEntities(Product product, List<String> cdnPaths) {
        List<Image> images = cdnPaths.stream()
                .map(cdnPath -> Image.builder()
                        .cdnPath(cdnPath)
                        .product(product)
                        .build())
                .toList();
        imageRepository.saveAll(images);

        return images;
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
            String encodeKey = cdnPath.substring(cdnPath.lastIndexOf("/") + 1);
            String decodedKey = URLDecoder.decode(encodeKey, StandardCharsets.UTF_8.toString());
            amazonS3Client.deleteObject(bucket, decodedKey);
        } catch (AmazonServiceException | UnsupportedEncodingException e) {
            throw new ImageException(IMAGE_DELETE_FAILED);
        }
    }

    /**
     * CDN 경로로부터 전체 이미지 URL 재구성
     * 이미지 -> 서버에 들어왔는지 확인하는 로그에 사용
     */
    public String getFullImageUrl(String cdnPath) {
        String domain = cloudfrontDomain.endsWith("/") ? cloudfrontDomain.substring(0, cloudfrontDomain.length() - 1) : cloudfrontDomain;
        String path = cdnPath.startsWith("/") ? cdnPath.substring(1) : cdnPath;

        try {
            // URL 인코딩
            path = URLEncoder.encode(path, StandardCharsets.UTF_8.toString()).replace("+", "%20");
        } catch (UnsupportedEncodingException e) {
            throw new ImageException(IMAGE_URL_ENCODING_FAILED);
        }

        return domain + "/" + path;
    }
}

