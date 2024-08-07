package org.chzz.market.domain.image.service;

import org.springframework.web.multipart.MultipartFile;

/*
    테스트 이미지 업로드 인터페이스
 */
public interface ImageUploader {
    String uploadImage(MultipartFile image);
}

