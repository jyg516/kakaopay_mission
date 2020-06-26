package com.mission.kakaopay;

import java.io.File;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class KakaopayMissionApplication {

	public static String FILE_PATH;
	
	public static void main(String[] args) {
		File file = new File(".");
		FILE_PATH = file.getAbsolutePath();
		SpringApplication.run(KakaopayMissionApplication.class, args);
	}

}
