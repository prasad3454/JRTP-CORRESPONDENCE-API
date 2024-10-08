package com.co.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import com.co.binding.CoResponse;
import com.co.service.CoServiceImpl;

@RestController
public class CoRestController {
	
	@Autowired
	private CoServiceImpl serviceImpl;
	
	@GetMapping("/process")
	public ResponseEntity<CoResponse> processPendingTrigger() {
		
		CoResponse response = serviceImpl.processPendingTriggers();
		
		return new ResponseEntity<>(response, HttpStatus.OK);
	}
}
