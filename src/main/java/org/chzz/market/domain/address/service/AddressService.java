package org.chzz.market.domain.address.service;

import static org.chzz.market.domain.address.error.AddressErrorCode.ADDRESS_NOT_FOUND;
import static org.chzz.market.domain.address.error.AddressErrorCode.CANNOT_DELETE_DEFAULT_ADDRESS;
import static org.chzz.market.domain.user.error.UserErrorCode.USER_NOT_FOUND;

import lombok.RequiredArgsConstructor;
import org.chzz.market.domain.address.dto.request.AddressDto;
import org.chzz.market.domain.address.dto.request.DeliveryDto;
import org.chzz.market.domain.address.entity.Address;
import org.chzz.market.domain.address.error.AddressException;
import org.chzz.market.domain.address.repository.AddressRepository;
import org.chzz.market.domain.user.error.exception.UserException;
import org.chzz.market.domain.user.repository.UserRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class AddressService {
    private final AddressRepository addressRepository;
    private final UserRepository userRepository;


    public Page<?> getAddresses(Long userId, Pageable pageable) {
        return addressRepository.findAddressesByUserId(pageable, userId);
    }

    @Transactional
    public void addAddress(Long userId, AddressDto addressDto) {
        userRepository.findById(userId)
                .ifPresentOrElse(user -> addressRepository.save(Address.initialAddress(user, addressDto)), () -> {
                    throw new UserException(USER_NOT_FOUND);
                });
    }

    @Transactional
    public void addDelivery(Long userId, DeliveryDto deliveryDto) {
        userRepository.findById(userId)
                .ifPresentOrElse(user -> addressRepository.save(Address.deliveryAddress(user, deliveryDto)), () -> {
                    throw new UserException(USER_NOT_FOUND);
                });

        // 기본 배송지로 설정한 경우, 기존의 기본 배송지를 해제
        if (deliveryDto.addressDto().isDefault()) {
            addressRepository.updateAllDefaultToFalse(userId);
        }
    }

    @Transactional
    public void updateDelivery(Long userId, Long addressId, DeliveryDto deliveryDto) {
        Address address = addressRepository.findByIdAndUserId(addressId, userId)
                .orElseThrow(() -> new AddressException(ADDRESS_NOT_FOUND));

        // 기본 배송지로 설정한 경우, 기존의 기본 배송지를 해제
        if (deliveryDto.addressDto().isDefault()) {
            addressRepository.updateAllDefaultToFalse(userId);
        }

        address.updateAsDeliveryAddress(deliveryDto);
    }

    @Transactional
    public void deleteDelivery(Long userId, Long addressId) {
        Address address = addressRepository.findByIdAndUserId(addressId, userId)
                .orElseThrow(() -> new AddressException(ADDRESS_NOT_FOUND));

        // 기본 배송지는 삭제할 수 없음
        if (address.isDefault()) {
            throw new AddressException(CANNOT_DELETE_DEFAULT_ADDRESS);
        }

        addressRepository.delete(address);
    }

}
