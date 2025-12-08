package com.example.filedb.controller.templates;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.example.filedb.dto.PostDto;
import com.example.filedb.service.PostService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/posts") // 전체 URL prefix /post/... 
@RequiredArgsConstructor
public class PostController {
	
	private final PostService postService;
	
	// 1. 새 게시글 등록
	// POST /posts
	@PostMapping
	@ResponseStatus(HttpStatus.CREATED)
	public PostDto createPost(@Valid @RequestBody PostDto request) {
		return postService.createPost(request);
	}
	
	
	// 2. 기존 게시글 수정
	// PUT /posts/{id}
	@PutMapping("/{id}")
	public PostDto updatePost(@PathVariable Long id, @Valid @RequestBody PostDto request) {
		return postService.updatePost(id, request);
	}
	
	
	// 3. 단일 게시글 조회
	// GET /posts/{id}
	@GetMapping("/{id}")
	public PostDto getPost(@PathVariable Long id) {
		return postService.getPost(id);
	}
	
	
	// 4. 전체 게시글 목록 조회
	// GET /posts
	public List<PostDto> getAllPosts(){
		return postService.getAllPosts();
	}
	
	
	// 5. 키워드 검색(제목+내용)
	// GET /posts/search?keyword=...
	@GetMapping("/search")
	public List<PostDto> searchPosts(@RequestParam(required = false) String keyword){
		return postService.searchPosts(keyword);
	}
	
	
	// 6. 게시글 삭제
	// DELETE /posts/{id}
	public void deletePost(@PathVariable Long id) {
		postService.deletePost(id);
	}
}
