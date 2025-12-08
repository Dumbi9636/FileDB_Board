package com.example.filedb.service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

import org.springframework.stereotype.Service;

import com.example.filedb.dto.PostDto;
import com.example.filedb.exception.PostNotFoundException;
import com.example.filedb.repository.FilePostRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class PostService {
	
	// 의존성 주입
	private final FilePostRepository postRepository;
	
	// 날짜는 String 으로 저장
	private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ISO_LOCAL_DATE_TIME;
	
	// 1. 새 게시글 생성
	public PostDto createPost(PostDto request) {
		String now = LocalDateTime.now().format(DATE_TIME_FORMATTER);
		
		// createdAt / updatedAt 세팅 
		request.setCreatedAt(now);
		request.setUpdatedAt(now);
		
		// ID 는 Repositroy 에서 시퀀스로 채우기
		return postRepository.save(request);
	}
	
	
	// 2. 기존 게시글 수정
	public PostDto updatePost(Long id, PostDto request) {
		
		// 기존 게시글 조회 (없으면 PostNotFoundException 예외 던지기)
		PostDto existing = postRepository.findPostById(id)
				.orElseThrow(()-> new PostNotFoundException(id));
		
		// 변경 가능한 필드만 교체
		existing.setTitle(request.getTitle());
		existing.setContent(request.getContent());
		existing.setWriter(request.getWriter());
		
		// 수정 시간 갱신
		existing.setUpdatedAt(LocalDateTime.now().format(DATE_TIME_FORMATTER));
		
		// 다시 저장
		return postRepository.save(existing);
	}
	
	
	// 3. 단건 조회(없으면 PostNotFoundException 예외 던지기)
	public PostDto getPost(Long id) {
		return postRepository.findPostById(id)
				.orElseThrow(()-> new PostNotFoundException(id));
	}
	
	
	// 4. 전체 게시글 목록 조회
	public List<PostDto> getAllPosts(){
		return postRepository.findAllPosts();
	}
	
	
	// 5. 키워드 검색(제목+내용)
	public List<PostDto> searchPosts(String keyword) {
		// 키워드를 입력하지 않으면 전체 목록 반환
	    if (keyword == null || keyword.isBlank()) {
	        return getAllPosts();
	    }
	    // searchPosts 메소드를 이용해서 키워드 검색 후 반환
	    return postRepository.searchPosts(keyword);
	}
	
	
	// 6. 게시글 삭제
    public void deletePost(Long postId) {
        // ID 가 없다면 Post 예외 던지기
        if (postRepository.findPostById(postId).isEmpty()) {
            throw new PostNotFoundException(postId);
        }
        // 있다면 ID 를 이용해서 삭제
        postRepository.deletePostById(postId);
    }
}
