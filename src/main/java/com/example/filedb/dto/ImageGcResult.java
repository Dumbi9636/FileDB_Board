package com.example.filedb.dto;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class ImageGcResult {

    // 게시글에서 실제로 참조 중인 이미지 파일명 개수
    private final int referencedImageCount;

    // uploads/images 폴더 안에 존재하는 전체 파일 개수
    private final int totalImageFileCount;

    // 이 중에서 "고아 이미지"로 판별된 파일 개수
    private final int orphanImageCount;

    // 실제로 삭제에 성공한 파일 이름 목록
    private final List<String> deletedFileNames;

    public static ImageGcResult of(
            int referencedImageCount,
            int totalImageFileCount,
            int orphanImageCount,
            List<String> deletedFileNames
    ) {
        return new ImageGcResult(
                referencedImageCount,
                totalImageFileCount,
                orphanImageCount,
                deletedFileNames
        );
    }
}
