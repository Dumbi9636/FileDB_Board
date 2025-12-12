package com.example.filedb.controller;

import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.filedb.dto.ImageGcResult;
import com.example.filedb.service.ImageGcService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/admin/images")
@RequiredArgsConstructor
public class ImageCleanupController {

    private final ImageGcService imageGcService;

    /**
     * 고아 이미지(어떤 게시글에서도 참조되지 않는 파일)를 일괄 삭제하는 API.
     *
     * - 서비스(ImageGcService)가 GC 로직을 모두 담당하고,
     * - 컨트롤러는 요청을 위임하고 결과만 반환
     *
     * @return ImageGcResult : 삭제된 파일 수, 총 파일 수, 참조 중인 파일 수, 삭제된 파일 리스트
     */
    @DeleteMapping("/cleanup")
    public ImageGcResult cleanupOrphanImages() {
        return imageGcService.cleanupOrphanImages();
    }
}
