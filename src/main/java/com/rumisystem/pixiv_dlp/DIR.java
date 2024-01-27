package com.rumisystem.pixiv_dlp;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class DIR {
	public DIR(String AUTHOR_ID, String ILLUST_ID){
		try{
			//親ディレクトリ
			if(!new File("pixiv").exists()){
				Path DIR_PATH = Paths.get("pixiv");
				Files.createDirectory(DIR_PATH);
			}
			//投稿者ディレクトリ
			if(!new File("pixiv/" + AUTHOR_ID).exists()){
				Path DIR_PATH = Paths.get("pixiv/" + AUTHOR_ID);
				Files.createDirectory(DIR_PATH);
			}
			//イラストディレクトリ
			if(!new File("pixiv/" + AUTHOR_ID + "/" + ILLUST_ID).exists()){
				Path DIR_PATH = Paths.get("pixiv/" + AUTHOR_ID + "/" + ILLUST_ID);
				Files.createDirectory(DIR_PATH);
			}
		}catch (Exception EX){
			EX.printStackTrace();
		}
	}
}
