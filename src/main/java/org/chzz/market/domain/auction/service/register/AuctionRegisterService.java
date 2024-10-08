package org.chzz.market.domain.auction.service.register;

import static org.chzz.market.domain.auction.type.AuctionStatus.PROCEEDING;

import java.time.LocalDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.chzz.market.domain.auction.dto.request.BaseRegisterRequest;
import org.chzz.market.domain.auction.dto.response.RegisterAuctionResponse;
import org.chzz.market.domain.auction.dto.response.RegisterResponse;
import org.chzz.market.domain.auction.entity.Auction;
import org.chzz.market.domain.auction.repository.AuctionRepository;
import org.chzz.market.domain.image.service.ImageService;
import org.chzz.market.domain.product.entity.Product;
import org.chzz.market.domain.product.repository.ProductRepository;
import org.chzz.market.domain.user.entity.User;
import org.chzz.market.domain.user.error.UserErrorCode;
import org.chzz.market.domain.user.error.exception.UserException;
import org.chzz.market.domain.user.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Service
@RequiredArgsConstructor
public class AuctionRegisterService implements AuctionRegistrationService {
    private final UserRepository userRepository;
    private final ProductRepository productRepository;
    private final AuctionRepository auctionRepository;
    private final ImageService imageService;

    @Override
    @Transactional
    public RegisterResponse register(Long userId, BaseRegisterRequest request, List<MultipartFile> images) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserException(UserErrorCode.USER_NOT_FOUND));

        Product product = createProduct(request, user);
        Product savedProduct = productRepository.save(product);

        List<String> imageUrls = imageService.uploadImages(images);
        imageService.saveProductImageEntities(savedProduct, imageUrls);

        imageService.validateImageSize(product.getId());

        Auction auction = createAuction(savedProduct);
        auctionRepository.save(auction);

        return RegisterAuctionResponse.of(savedProduct.getId(), auction.getId(), auction.getStatus());
    }

    private Product createProduct(BaseRegisterRequest request, User user) {
        return Product.builder()
                .user(user)
                .name(request.getProductName())
                .minPrice(request.getMinPrice())
                .description(request.getDescription())
                .category(request.getCategory())
                .build();
    }

    private Auction createAuction(Product product) {
        return Auction.builder()
                .product(product)
                .status(PROCEEDING)
                .endDateTime(LocalDateTime.now().plusHours(24))
                .build();
    }
}
