package org.chzz.market.domain.product.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.chzz.market.common.AWSConfig;
import org.chzz.market.domain.product.dto.request.RegisterProductRequest;
import org.chzz.market.domain.product.dto.response.RegisterProductResponse;
import org.chzz.market.domain.product.error.ProductErrorCode;
import org.chzz.market.domain.product.error.ProductException;
import org.chzz.market.domain.product.service.ProductService;
import org.chzz.market.domain.user.error.UserErrorCode;
import org.chzz.market.domain.user.error.exception.UserException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors;
import org.springframework.test.web.servlet.MockMvc;

import static org.chzz.market.domain.product.entity.Product.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Import(AWSConfig.class)
class ProductControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ProductService productService;

    @Autowired
    private ObjectMapper objectMapper;

    private MockMultipartFile image1, image2, image3;

    @BeforeEach
    void setUp() {

        image1 = new MockMultipartFile(
                "images",
                "test.jpg",
                MediaType.IMAGE_JPEG_VALUE,
                "test image content 1".getBytes()
        );

        image2 = new MockMultipartFile(
                "images",
                "test2.jpg",
                MediaType.IMAGE_JPEG_VALUE,
                "test image content 2".getBytes()
        );

        image3 = new MockMultipartFile(
                "images",
                "test3.jpg",
                MediaType.IMAGE_JPEG_VALUE,
                "test image content 3".getBytes()
        );
    }

    @Nested
    @DisplayName("경매 상품 등록 테스트")
    class RegisterAuctionProductTest {

        @Test
        @WithMockUser(username = "tester", roles = {"USER"})
        @DisplayName("경매 상품 등록 - 모든 필드 정상 입력 시 성공 응답")
        void registerAuctionProduct_Success() throws Exception {
            RegisterProductRequest validRequest = RegisterProductRequest.builder()
                    .userId(1L)
                    .name("Test Product")
                    .description("Test Description")
                    .category(Category.ELECTRONICS)
                    .minPrice(1000)
                    .build();

            RegisterProductResponse response = new RegisterProductResponse(1L, ProductStatus.IN_AUCTION, "상품이 등록되었습니다.");
            when(productService.registerAuctionProduct(any(RegisterProductRequest.class))).thenReturn(response);

            mockMvc.perform(multipart("/api/v1/products/register")
                    .file(image1).file(image2).file(image3)
                    .param("userId", validRequest.getUserId().toString())
                    .param("name", validRequest.getName())
                    .param("description", validRequest.getDescription())
                    .param("category", validRequest.getCategory().name())
                    .param("minPrice", validRequest.getMinPrice().toString())
                    .with(SecurityMockMvcRequestPostProcessors.csrf())
                    .contentType(MediaType.MULTIPART_FORM_DATA))
                    .andExpect(jsonPath("$.productId").value(1))
                    .andExpect(jsonPath("$.status").value(ProductStatus.IN_AUCTION.name()))
                    .andExpect(jsonPath("$.message").value("상품이 등록되었습니다."))
                    .andExpect(status().isCreated());

            // 서비스 메소드 호출 확인
            verify(productService, times(1)).registerAuctionProduct(any(RegisterProductRequest.class));
        }

        @Test
        @WithMockUser(username = "tester", roles = {"USER"})
        @DisplayName("존재하지 않는 사용자로 경매 상품 등록 실패")
        void registerAuctionProduct_UserNotFound() throws Exception {
            RegisterProductRequest invalidRequest = RegisterProductRequest.builder()
                    .userId(999L)
                    .name("Test Product")
                    .description("Test Description")
                    .category(Category.ELECTRONICS)
                    .minPrice(1000)
                    .build();

            when(productService.registerAuctionProduct(any(RegisterProductRequest.class))).thenThrow(new UserException(UserErrorCode.USER_NOT_FOUND));

            mockMvc.perform(multipart("/api/v1/products/register")
                            .file(image1)
                            .param("userId", invalidRequest.getUserId().toString())
                            .param("name", invalidRequest.getName())
                            .param("description", invalidRequest.getDescription())
                            .param("category", invalidRequest.getCategory().name())
                            .param("minPrice", invalidRequest.getMinPrice().toString())
                            .with(SecurityMockMvcRequestPostProcessors.csrf())
                            .contentType(MediaType.MULTIPART_FORM_DATA))
                    .andExpect(status().isNotFound());
        }

        @Test
        @DisplayName("필수 필드 누락 시 Bad Request 응답")
        void RegisterProductMissingRequiredField() throws Exception {
            mockMvc.perform(multipart("/api/v1/products/register")
                            .param("userId", "1")
                            // title 누락
                            .param("description", "Test Description")
                            .param("category", Category.ELECTRONICS.name())
                            .param("minPrice", "1000")
                            .contentType(MediaType.MULTIPART_FORM_DATA))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("잘못된 가격 입력 시 Bad Request 응답")
        void RegisterProductInvalidPrice() throws Exception {
            mockMvc.perform(multipart("/api/v1/products/register")
                            .param("userId", "1")
                            .param("title", "Test Product")
                            .param("description", "Test Description")
                            .param("category", Category.ELECTRONICS.name())
                            .param("minPrice", "invalid")
                            .contentType(MediaType.MULTIPART_FORM_DATA))
                    .andExpect(status().isBadRequest());
        }
    }

    @Nested
    @DisplayName("사전 등록 테스트")
    class PreRegisterAuctionProductTest {

        @Test
        @WithMockUser(username = "tester", roles = {"USER"})
        @DisplayName("사전 등록 - 모든 필드 정상 입력 시 성공 응답")
        void prePreRegisterProduct_Success() throws Exception {
            RegisterProductRequest validRequest = RegisterProductRequest.builder()
                    .userId(1L)
                    .name("Test Product")
                    .description("Test Description")
                    .category(Category.ELECTRONICS)
                    .minPrice(1000)
                    .build();

            RegisterProductResponse mockResponse = new RegisterProductResponse(1L, ProductStatus.IN_AUCTION, "상품이 등록되었습니다.");
            when(productService.preRegisterProduct(any(RegisterProductRequest.class))).thenReturn(mockResponse);

            mockMvc.perform(multipart("/api/v1/products/pre-register")
                    .file(image1).file(image2).file(image3)
                    .param("userId", validRequest.getUserId().toString())
                    .param("name", validRequest.getName())
                    .param("description", validRequest.getDescription())
                    .param("category", validRequest.getCategory().name())
                    .param("minPrice", validRequest.getMinPrice().toString())
                    .with(SecurityMockMvcRequestPostProcessors.csrf())
                    .contentType(MediaType.MULTIPART_FORM_DATA))
                    .andExpect(jsonPath("$.productId").value(1))
                    .andExpect(jsonPath("$.status").value(ProductStatus.IN_AUCTION.name()))
                    .andExpect(jsonPath("$.message").value("상품이 등록되었습니다."));

            verify(productService, times(1)).preRegisterProduct(any(RegisterProductRequest.class));
        }

        @Test
        @WithMockUser(username = "tester", roles = {"USER"})
        @DisplayName("존재하지 않는 사용자로 사전 등록 실패")
        void preRegisterProduct_UserNotFound() throws Exception {
            RegisterProductRequest invalidRequest = RegisterProductRequest.builder()
                    .userId(999L)
                    .name("Test Product")
                    .description("Test Description")
                    .category(Category.ELECTRONICS)
                    .minPrice(1000)
                    .build();

            when(productService.preRegisterProduct(any(RegisterProductRequest.class))).thenThrow(new UserException(UserErrorCode.USER_NOT_FOUND));

            mockMvc.perform(multipart("/api/v1/products/pre-register")
                            .file(image1)
                            .param("userId", invalidRequest.getUserId().toString())
                            .param("name", invalidRequest.getName())
                            .param("description", invalidRequest.getDescription())
                            .param("category", invalidRequest.getCategory().name())
                            .param("minPrice", invalidRequest.getMinPrice().toString())
                            .with(SecurityMockMvcRequestPostProcessors.csrf())
                            .contentType(MediaType.MULTIPART_FORM_DATA))
                    .andExpect(status().isNotFound());
        }
    }

    @Nested
    @DisplayName("사전 등록에서 경매 등록 전환 테스트")
    class ConvertToAuctionTest {
        @Test
        @WithMockUser(username = "tester", roles = {"USER"})
        @DisplayName("사전 등록 상품 -> 경매 등록 상품 전환 성공")
        void convertToAuction_Success() throws Exception {
            Long productId = 1L;
            RegisterProductResponse mockResponse = new RegisterProductResponse(productId, ProductStatus.IN_AUCTION, "경매 상품으로 전환되었습니다.");
            when(productService.convertToAuction(productId)).thenReturn(mockResponse);

            mockMvc.perform(post("/api/v1/products/{productId}/convert-to-auction", productId)
                            .with(SecurityMockMvcRequestPostProcessors.csrf())
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.productId").value(productId))
                    .andExpect(jsonPath("$.status").value(ProductStatus.IN_AUCTION.name()))
                    .andExpect(jsonPath("$.message").value("경매 상품으로 전환되었습니다."));

            verify(productService, times(1)).convertToAuction(productId);
        }

        @Test
        @WithMockUser(username = "tester", roles = {"USER"})
        @DisplayName("존재하지 않는 상품 ID로 전환 시도 실패")
        void convertToAuction_ProductNotFound() throws Exception {
            Long nonExistentProductId = 999L;
            when(productService.convertToAuction(nonExistentProductId)).thenThrow(new ProductException(ProductErrorCode.PRODUCT_NOT_FOUND));

            mockMvc.perform(post("/api/v1/products/{productId}/convert-to-auction", nonExistentProductId)
                            .with(SecurityMockMvcRequestPostProcessors.csrf())
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isNotFound());
        }

        @Test
        @WithMockUser(username = "tester", roles = {"USER"})
        @DisplayName("이미 판매 완료된 상품 경매 전환 시도 실패")
        void convertToAuction_AlreadySold() throws Exception {
            Long soldProductId = 2L;
            when(productService.convertToAuction(soldProductId)).thenThrow(new ProductException(ProductErrorCode.INVALID_PRODUCT_STATE));

            mockMvc.perform(post("/api/v1/products/{productId}/convert-to-auction", soldProductId)
                            .with(SecurityMockMvcRequestPostProcessors.csrf())
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isBadRequest());
        }
    }
}