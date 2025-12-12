package com.example.filedb.service;

import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.example.filedb.dto.ImageGcResult;
import com.example.filedb.dto.PostDto;
import com.example.filedb.repository.FilePostRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ImageGcService {

    // 게시글 목록 조회 파일 DB 저장소 주입
    private final FilePostRepository postRepository;

    // content 파싱용 (JSON 문자열)
    private final ObjectMapper objectMapper;

    /*
     * 업로드 베이스 경로
     * C:/playground/projects/FileDB/uploads/editor
     */
    @Value("${filedb.upload-path}")
    private String uploadDir;

    
    /*
     * 이미지 가비지 컬렉션(GC)을 수행하는 메인 메서드.
     *
     * 1. 모든 게시글의 content에서 "현재 참조 중인 이미지 파일명"을 수집하고
     * 2. uploads/editor 디렉토리에 실제로 존재하는 파일 목록과 비교해서
     * 3. 어떤 게시글에서도 사용하지 않는 고아 이미지를 삭제
     */
    public ImageGcResult cleanupOrphanImages() {

        // 1. 게시글에서 실제로 사용 중인 이미지 파일명 집합
        Set<String> referencedFileNames = collectReferencedImageNames();

        // 2. uploads/editor 안에 실제로 존재하는 모든 이미지 파일
        List<File> allImageFiles = findAllEditorImageFiles();

        // 3. "폴더에는 있는데, 어떤 글에서도 참조하지 않는 파일"만 추리기
        List<File> orphanFiles = filterOrphanFiles(allImageFiles, referencedFileNames);

        // 4. 고아 이미지 파일들 실제 삭제
        List<String> deletedFileNames = deleteFiles(orphanFiles);

        // 5. 결과 DTO로 묶어서 반환
        return ImageGcResult.of(
                referencedFileNames.size(),   // 현재 글에서 참조 중인 파일 수
                allImageFiles.size(),         // editor 폴더 내 전체 파일 수
                orphanFiles.size(),           // 고아 후보 수
                deletedFileNames              // 실제 삭제된 파일 이름들
        );
    }

    
    /**
     * 모든 게시글의 content(JSON 문자열)에서
     * "현재 글에서 사용 중인 이미지 파일명"들을 수집한다.
     *
     * content 구조 예시:
     * {
     *   "type": "toast",
     *   "markdown": "...",
     *   "html": "...",
     *   "images": ["/editor/1765xxxx.png", "/editor/aaaa.jpg"]
     * }
     */
    private Set<String> collectReferencedImageNames() {
        List<PostDto> posts = postRepository.findAllPosts();
        Set<String> fileNames = new HashSet<>();

        for (PostDto post : posts) {
            addImagesFromPostContent(post.getContent(), fileNames);
        }

        return fileNames;
    }

    
    
    // 게시글 하나의 content 문자열에서 images 배열을 꺼내 "파일명"만 추출해서 bucket(Set)에 넣는다.
    private void addImagesFromPostContent(String rawContent, Set<String> bucket) {
        if (rawContent == null || rawContent.isBlank()) {
            return; // 내용 없으면 스킵
        }

        try {
            JsonNode root = objectMapper.readTree(rawContent);

            JsonNode imagesNode = root.get("images");
            if (imagesNode == null || !imagesNode.isArray()) {
                // images 필드가 없거나 배열이 아니면 이미지 없는 글로 간주
                return;
            }

            for (JsonNode imageNode : imagesNode) {
                String imageUrl = imageNode.asText();        // "/editor/1765xxx.png" 같은 문자열
                String fileName = extractFileName(imageUrl); // "1765xxx.png" 로 변환

                if (!fileName.isBlank()) {
                    bucket.add(fileName);
                }
            }
        } catch (Exception e) {
            // content가 JSON이 아니거나 파싱 실패 → 그냥 이미지 없는 글로 처리
        }
    }

    
    
    //"/editor/1765xxx.png" 또는 "http://.../editor/1765xxx.png" 같은 문자열에서 파일명만 뽑아냄 (슬래시 뒤 마지막 토큰) 
    private String extractFileName(String urlOrPath) {
        if (urlOrPath == null || urlOrPath.isBlank()) {
            return "";
        }

        String trimmed = urlOrPath.trim();
        int index = trimmed.lastIndexOf('/');

        if (index == -1) {
            // 슬래시가 없으면 애초에 파일명이라고 보고 그대로 사용
            return trimmed;
        }

        return trimmed.substring(index + 1);
    }

    
    /**
     * uploads/editor 디렉토리 아래의 모든 파일을 가져옴
     * (하위 디렉토리는 고려하지 않고, depth 1만 스캔)
     * C:/playground/projects/FileDB/uploads
     * 실제 에디터 이미지는 {uploadDir}/editor 에 저장
     */
    private List<File> findAllEditorImageFiles() {
        // uploads/editor
        File editorDir = new File(uploadDir, "editor");

        if (!editorDir.exists() || !editorDir.isDirectory()) {
            // 디렉토리가 없거나 디렉토리가 아니면 스캔할 게 없다고 판단
            return List.of();
        }

        File[] files = editorDir.listFiles();
        if (files == null || files.length == 0) {
            return List.of();
        }

        List<File> result = new ArrayList<>();
        for (File file : files) {
            if (file.isFile()) {
                result.add(file);
            }
        }
        return result;
    }

    
    /**
     * 전체 파일 목록(allFiles)에서
     * "게시글에서 참조 중인 파일명(referencedNames)에 포함되지 않는 파일"만 모아서 반환
     */
    private List<File> filterOrphanFiles(List<File> allFiles, Set<String> referencedNames) {
        if (allFiles.isEmpty()) {
            return List.of();
        }

        List<File> result = new ArrayList<>();

        for (File file : allFiles) {
            String fileName = file.getName();
            if (!referencedNames.contains(fileName)) {
                result.add(file); // 어떤 글에서도 쓰지 않는 고아 이미지
            }
        }

        return result;
    }

    
    /**
     * 실제 파일 삭제를 수행하고,
     * 삭제에 성공한 파일 이름만 리스트로 모아 반환
     */
    private List<String> deleteFiles(List<File> files) {
        if (files.isEmpty()) {
            return List.of();
        }

        List<String> deletedFileNames = new ArrayList<>();

        for (File file : files) {
            boolean deleted = file.delete();
            if (deleted) {
                deletedFileNames.add(file.getName());
            } else {
                // 실패한 경우는 일단 콘솔에만 로그
                System.err.println("[ImageGcService] 삭제 실패: " + file.getAbsolutePath());
            }
        }

        return deletedFileNames;
    }
}
