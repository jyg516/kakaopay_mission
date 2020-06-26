package com.mission.kakaopay.service;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.OutputStream;
import java.security.SecureRandom;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Random;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.mission.kakaopay.KakaopayMissionApplication;

@Service
public class MissionService {
	
	private boolean isOpen = true;
	
	// DB를 대신할 파일 저장 경로
//	@Value("${file.path}")
//	public String filePath;
	
	public String sendMoney(String userId, String roomId, int money, int peopleCount) {
		
		JsonObject obj = new JsonObject();
		ArrayList<Integer> list = getPortion(peopleCount, money);
		String token = getToken();
		
		SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd HH:mm:ss");
		
		obj.addProperty("token", token);
		obj.addProperty("sendUserId", userId);
		obj.addProperty("sendRoomId", roomId);
		obj.addProperty("money", money);
		obj.addProperty("peopleCount", peopleCount);
		obj.addProperty("distributeMoney", list.toString());
		obj.addProperty("sendDate", sdf.format(new Date()));
		obj.addProperty("receivedMember", new ArrayList<String>().toString());
		obj.addProperty("completedMoney", 0);
		
		try {
			File file = new File(KakaopayMissionApplication.FILE_PATH + "\\files\\" + token+".txt");
			
			OutputStream outputStream = new FileOutputStream(file);
			byte[] byteArr = obj.toString().getBytes();
			outputStream.write(byteArr);
			
			outputStream.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		return token;
	}
	
	public synchronized String receiveMoney(String userId, String roomId, String token) {
		
		String returnValue = null;
		FileReader fileReader = null;
		OutputStream outputStream = null;
		
		try {
			
			while(!isOpen)
			{
				Thread.sleep(500);
			}
			
			File file = new File(KakaopayMissionApplication.FILE_PATH + "\\files\\" + token + ".txt");
			fileReader = new FileReader(file);
			StringBuffer sendInfo = new StringBuffer();
			
			isOpen = false;
			
			int cnt = -1;
			
			while( (cnt = fileReader.read()) != -1)
			{
				sendInfo.append((char)cnt);
			}
			
			isOpen = true;
			notifyAll();
			
			JsonObject obj = new Gson().fromJson(sendInfo.toString(), JsonObject.class);
			String sendUserId = obj.get("sendUserId").getAsString();
			String sendRoomId = obj.get("sendRoomId").getAsString();
			String receivedMember = obj.get("receivedMember").getAsString();
			String sendDateStr = obj.get("sendDate").getAsString();
			
			SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd HH:mm:ss");
			Date sendDate = sdf.parse(sendDateStr);
			Calendar cal = Calendar.getInstance();
			cal.setTime(sendDate);
			cal.add(Calendar.MINUTE, 10);
			
			if(!roomId.equals(sendRoomId))
			{
				return "뿌리기가 호출된 대화방과 동일한 대화방에 속한 사용자만이 받을 수 있습니다.";
			}
			
			if(userId.equals(sendUserId))
			{
				return "자신이 뿌리기한 건은 자신이 받을 수 없습니다.";
			}
			
			if(sdf.format(cal.getTime()).compareTo(sdf.format(new Date())) < 0)
			{
				return "뿌린 건은 10분간만 유효합니다.";
			}
			
			if(receivedMember.indexOf(userId) > -1)
			{
				return "뿌리기 당 한 사용자는 한번만 받을 수 있습니다.";
			}

			List<String> receivedMemberList = new ArrayList<>();
			String temp = receivedMember.replace("[", "").replace("]", "");
			if(!"".equals(temp))
			{
				receivedMemberList.addAll(Arrays.asList(temp.split(",")));
			}
			
			String receivedMoney = obj.get("distributeMoney").getAsString();
			List<String> updateList = new ArrayList<>(); 
			updateList.addAll(Arrays.asList(receivedMoney.trim().replace("[", "").replace("]", "").split(",")));
			returnValue = updateList.get(0);
			updateList.remove(0);
			
			receivedMemberList.add(userId + "=" + returnValue);
			obj.addProperty("receivedMember", receivedMemberList.toString().trim().replace(" ", ""));
			obj.addProperty("distributeMoney", updateList.toString().trim().replace(" ", ""));
			
			int completedMoney = Integer.parseInt(obj.get("completedMoney").getAsString());
			
			obj.addProperty("completedMoney", completedMoney+Integer.parseInt(returnValue));
			
			while(!isOpen)
			{
				Thread.sleep(500);
			}
			
			isOpen = false;
			
			outputStream = new FileOutputStream(file);
			byte[] byteArr = obj.toString().getBytes();
			outputStream.write(byteArr);
			
			isOpen = true;
			notifyAll();
		}
		catch (FileNotFoundException e)
		{
			return "올바른 Token 값을 입력 하세요.";
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		finally {
			try {
				if(fileReader != null)
				{
					fileReader.close();
				}
				
				if(outputStream != null)
				{
					outputStream.close();
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		
		return returnValue;
	}
	
	public synchronized String selectSendInfo(String userId, String token) {
		FileReader fileReader = null;
		JsonObject returnObj = new JsonObject();
		
		try {
			
			while(!isOpen)
			{
				Thread.sleep(500);
			}
			
			isOpen = false;
			
			File file = new File(KakaopayMissionApplication.FILE_PATH + "\\files\\" + token + ".txt");
			fileReader = new FileReader(file);
			StringBuffer sendInfo = new StringBuffer();
			
			int cnt = -1;
			
			while( (cnt = fileReader.read()) != -1)
			{
				sendInfo.append((char)cnt);
			}
			
			isOpen = true;
			notifyAll();
			
			JsonObject obj = new Gson().fromJson(sendInfo.toString(), JsonObject.class);
			String sendUserId = obj.get("sendUserId").getAsString();
			String sendDateStr = obj.get("sendDate").getAsString();
			
			SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd HH:mm:ss");
			Date sendDate = sdf.parse(sendDateStr);
			Calendar cal = Calendar.getInstance();
			cal.setTime(sendDate);
			cal.add(Calendar.DAY_OF_YEAR, 7);
			
			if(!userId.equals(sendUserId))
			{
				return "나의 뿌리기 건만 조회 가능합니다.";
			}
			
			if(sdf.format(cal.getTime()).compareTo(sdf.format(new Date())) < 0)
			{
				return "뿌린 건에 대한 조회는 7일 동안 할 수 있습니다.";
			}
			
			String sendMoney = obj.get("money").getAsString();
			String receivedInfo = obj.get("receivedMember").getAsString();
			String completedMoney = obj.get("completedMoney").getAsString();
			
			returnObj.addProperty("sendDate", sendDateStr);
			returnObj.addProperty("sendMoney", sendMoney);
			returnObj.addProperty("receivedInfo", receivedInfo);
			returnObj.addProperty("completedMoney", completedMoney);
			
		}catch (FileNotFoundException e) {
			return "올바른 Token 값을 입력 하세요.";
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		finally {
			try {
				if(fileReader != null)
				{
					fileReader.close();
				}
				
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		
		return returnObj.toString();
	}
	
	private ArrayList<Integer> getPortion(int peopleCount, int money)
	{
		ArrayList<Integer> list = new ArrayList<>();
		ArrayList<Integer> portionList = new ArrayList<>();
		
		int listSum = 0;
		
		Random r = new Random();
		
		for(int i=0; i<peopleCount; i++)
		{
			int temp = r.nextInt(10)+1;
			list.add(temp);
			listSum += temp;
		}
		
		for(int i=0; i<peopleCount; i++)
		{
			double temp = (double)list.get(i)/(double)listSum * (double)money;
			portionList.add((int)temp);
		}
		
		return portionList;
	}
	
	private String getToken()
	{
		SecureRandom random = new SecureRandom();
		
		String lower = "abcdefghijklmnopqrstuvwxyz";
        String upper = lower.toUpperCase();
        String number = "0123456789";
        String data = lower + upper + number;
        String returnString = "";

        for(int i=0; i<3; i++)
        {
        	returnString += data.charAt(random.nextInt(data.length()));
        }
		
		return returnString;
	}

	
}
