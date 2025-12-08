package com.example.filedb.dto;

import lombok.Data;

@Data
public class PostDto {
	private Long id; // PK 
	private String title;
	private String content;
	private String writer;
	private String createdAt;
	private String updatedAt;
	private String imageFilename; // 이미지 업로드 파일명
	
}
