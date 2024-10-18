package org.chzz.market.domain.address;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;
import org.chzz.market.domain.address.dto.request.AddressDto;
import org.chzz.market.domain.address.dto.request.DeliveryDto;
import org.chzz.market.domain.address.entity.Address;
import org.chzz.market.domain.address.error.AddressException;
import org.chzz.market.domain.address.repository.AddressRepository;
import org.chzz.market.domain.address.service.AddressService;
import org.chzz.market.domain.user.entity.User;
import org.chzz.market.domain.user.error.exception.UserException;
import org.chzz.market.domain.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class AddressServiceTest {

    @Mock
    private AddressRepository addressRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private AddressService addressService;

    private User testUser;
    private AddressDto testAddressDto;
    private DeliveryDto testDeliveryDto;
    private Address testAddress;

    @BeforeEach
    void setUp() {
        testUser = User.builder()
                .id(1L)
                .build();

        testAddressDto = AddressDto.builder()
                .roadAddress("서울시 강남구")
                .jibun("강남대로 123")
                .zipcode("12345")
                .detailAddress("상세주소")
                .isDefault(true)
                .build();

        testDeliveryDto = DeliveryDto.builder()
                .addressDto(testAddressDto)
                .recipientName("홍길동")
                .phoneNumber("01012345678")
                .deliveryMemo("문 앞에 놔주세요")
                .build();

        testAddress = Address.builder()
                .id(1L)
                .user(testUser)
                .roadAddress("서울시 강남구")
                .jibun("강남대로 123")
                .zipcode("12345")
                .detailAddress("상세주소")
                .recipientName("홍길동")
                .phoneNumber("01012345678")
                .deliveryMemo("문 앞에 놔주세요")
                .isDefault(true)
                .build();
    }

    @Test
    @DisplayName("배송지 추가 성공")
    void addDelivery_Success() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(addressRepository.save(any(Address.class))).thenReturn(testAddress);

        assertDoesNotThrow(() -> addressService.addDelivery(1L, testDeliveryDto));

        verify(addressRepository).updateAllDefaultToFalse(1L);
        verify(addressRepository).save(any(Address.class));
    }

    @Test
    @DisplayName("배송지 추가 실패 - 사용자를 찾을 수 없음")
    void addDelivery_UserNotFound() {
        when(userRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(UserException.class, () -> addressService.addDelivery(1L, testDeliveryDto));
    }

    @Test
    @DisplayName("배송지 수정 성공")
    void updateDelivery_Success() {
        when(addressRepository.findByIdAndUserId(1L, 1L)).thenReturn(Optional.of(testAddress));

        assertDoesNotThrow(() -> addressService.updateDelivery(1L, 1L, testDeliveryDto));

        verify(addressRepository).updateAllDefaultToFalse(1L);
        verify(addressRepository).findByIdAndUserId(1L, 1L);
    }

    @Test
    @DisplayName("배송지 수정 실패 - 배송지를 찾을 수 없음")
    void updateDelivery_AddressNotFound() {
        when(addressRepository.findByIdAndUserId(1L, 1L)).thenReturn(Optional.empty());

        assertThrows(AddressException.class, () -> addressService.updateDelivery(1L, 1L, testDeliveryDto));
    }

    @Test
    @DisplayName("배송지 삭제 성공")
    void deleteDelivery_Success() {
        Address nonDefaultAddress = Address.builder()
                .id(2L)
                .user(testUser)
                .roadAddress("서울시 강남구")
                .jibun("강남대로 123")
                .zipcode("12345")
                .detailAddress("상세주소")
                .recipientName("홍길동")
                .phoneNumber("01012345678")
                .deliveryMemo("문 앞에 놔주세요")
                .isDefault(false)
                .build();

        when(addressRepository.findByIdAndUserId(2L, 1L)).thenReturn(Optional.of(nonDefaultAddress));

        assertDoesNotThrow(() -> addressService.deleteDelivery(1L, 2L));

        verify(addressRepository).delete(nonDefaultAddress);
    }

    @Test
    @DisplayName("배송지 삭제 실패 - 배송지를 찾을 수 없음")
    void deleteDelivery_AddressNotFound() {
        when(addressRepository.findByIdAndUserId(1L, 1L)).thenReturn(Optional.empty());

        assertThrows(AddressException.class, () -> addressService.deleteDelivery(1L, 1L));
    }

    @Test
    @DisplayName("배송지 삭제 실패 - 기본 배송지 삭제 시도")
    void deleteDelivery_CannotDeleteDefaultAddress() {
        when(addressRepository.findByIdAndUserId(1L, 1L)).thenReturn(Optional.of(testAddress));

        assertThrows(AddressException.class, () -> addressService.deleteDelivery(1L, 1L));
    }

}
