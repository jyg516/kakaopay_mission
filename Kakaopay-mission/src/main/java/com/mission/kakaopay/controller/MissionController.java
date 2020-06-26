package com.mission.kakaopay.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.mission.kakaopay.service.MissionService;

@RestController
public class MissionController {
	
	@Autowired
	MissionService service;
	
	@PostMapping("/sendMoney")
	public String sendMoney(@RequestHeader(value="X-USER-ID", required = false) String userId, @RequestHeader(value="X-ROOM-ID", required = false) String roomId,
			@RequestParam(value="money", required = false) Integer money, @RequestParam(value = "peopleCount", required = false) Integer peopleCount) {
		
		if(userId == null || "".equals(userId))
		{
			return "HTTP HEADER에 X-USER-ID를 세팅해주세요.";
		}
		
		if(roomId == null || "".equals(roomId))
		{
			return "HTTP HEADER에 X-ROOM-ID를 세팅해주세요.";
		}
		
		if(money == null)
		{
			return "뿌릴 금액을 입력 하세요.";
		}
		else if(money.intValue() == 0)
		{
			return "뿌릴 금액은 0이 될 수 없습니다.";
		}
		
		if(peopleCount == null)
		{
			return "뿌릴 인원을 입력 하세요.";
		}
		else if(peopleCount.intValue() == 0)
		{
			return "뿌릴 인원은 0이 될 수 없습니다.";
		}
		
		String token = service.sendMoney(userId, roomId, money, peopleCount);
		
		return token;
	}
	
	@PutMapping("/receiveMoney")
	public String receiveMoney(@RequestHeader(value="X-USER-ID", required = false) String userId, @RequestHeader(value="X-ROOM-ID", required = false) String roomId,
			@RequestParam(value="token", required = false) String token) {
		
		if(userId == null || "".equals(userId))
		{
			return "HTTP HEADER에 X-USER-ID를 세팅해주세요.";
		}
		
		if(roomId == null || "".equals(roomId))
		{
			return "HTTP HEADER에 X-ROOM-ID를 세팅해주세요.";
		}
		
		String returnMoney = service.receiveMoney(userId, roomId, token);
		
		return returnMoney;
	}
	
	@GetMapping("/selectSendInfo")
	public String selectSendInfo(@RequestHeader(value="X-USER-ID", required = false) String userId, @RequestParam(value="token", required = false) String token) {
		
		if(userId == null || "".equals(userId))
		{
			return "HTTP HEADER에 X-USER-ID를 세팅해주세요.";
		}
		
		String returnValue = service.selectSendInfo(userId, token);
		
		return returnValue;
	
	}
	
}
