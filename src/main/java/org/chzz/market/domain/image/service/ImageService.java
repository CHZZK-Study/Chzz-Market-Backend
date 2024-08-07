package org.chzz.market.domain.image.service;

import com.amazonaws.services.s3.AmazonS3;
import lombok.RequiredArgsConstructor;
import org.chzz.market.domain.image.entity.Image;
import org.chzz.market.domain.image.repository.ImageRepository;
import org.chzz.market.domain.product.entity.Product;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ImageService {
    private final ImageUploader imageUploader;
    private final ImageRepository imageRepository;

    @Value("${cloud.aws.cloudfront.domain}")
    private String cloudfrontDomain;

    /**
     * 상품 이미지를 업로드하고 DB에 저장
     */
    public List<String> saveProductImages(Product product, List<MultipartFile> imageUrls) {
        return imageUrls.stream().map(imageUrl -> {
            String cdnPath = imageUploader.uploadImage(imageUrl);
            Image imageEntity = Image.builder()
                    .cdnPath(cdnPath)
                    .product(product)
                    .build();
            imageRepository.save(imageEntity);
            return cdnPath;
        }).toList();
    }

    // 필요할 때 전체 URL 재구성
    // 이미지 -> 서버에 들어왔는지 확인하는 로그에 사용
    public String getFullImageUrl(String cdnPath) {
        return "https://" + cloudfrontDomain + cdnPath;
    }
}

