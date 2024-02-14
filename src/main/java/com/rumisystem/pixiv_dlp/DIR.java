package com.rumisystem.pixiv_dlp;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;

public class DIR {
	public DIR(String AUTHOR_ID, String ILLUST_ID, JsonNode ILLUST_BODY_JSON){
		try{
			//親ディレクトリ
			if(!new File("pixiv").exists()){
				Path DIR_PATH = Paths.get("pixiv");
				Files.createDirectory(DIR_PATH);
			}

			AUTHOR_INDEX(AUTHOR_ID);

			ILLUST_INDEX(AUTHOR_ID, ILLUST_ID, ILLUST_BODY_JSON);
		}catch (Exception EX){
			EX.printStackTrace();
		}
	}

	public void AUTHOR_INDEX(String AUTHOR_ID) throws IOException {
		String AJAX_RESULT = new HTTP_REQUEST("https://www.pixiv.net/ajax/user/" + AUTHOR_ID + "?lang=ja").GET();

		ObjectMapper OBJ_MAPPER = new ObjectMapper();
		JsonNode AJAX_RESULT_JSON = OBJ_MAPPER.readTree(AJAX_RESULT);
		JsonNode BODY_JSON = AJAX_RESULT_JSON.get("body");

		//投稿者ディレクトリ
		if(!new File("pixiv/" + AUTHOR_ID).exists()){
			Path DIR_PATH = Paths.get("pixiv/" + AUTHOR_ID);
			Files.createDirectory(DIR_PATH);
		}

		//index.jsonのファイルパス
		String PATH = "pixiv/" + AUTHOR_ID + "/index.json";

		//投稿者のindex.json
		File INDEX = new File(PATH);

		//index.jsonが無いか
		if(!INDEX.exists()){
			//作成
			if(INDEX.createNewFile()){
				System.out.println("index.jsonを作成しました");
			} else {//失敗
				System.err.println("index.jsonの作成に失敗しました");
				System.exit(1);
			}
		}

		//書き込む
		BufferedWriter WRITER = new BufferedWriter(new FileWriter(INDEX));

		HashMap<String, Object> MAP = new HashMap<>();
		MAP.put("ID", BODY_JSON.get("userId").asText());
		MAP.put("NAME", BODY_JSON.get("name").asText());

		//MAPをString化
		ObjectMapper OM = new ObjectMapper();
		String RES = OM.writeValueAsString(MAP);

		//書き込み
		WRITER.write(RES);

		//メモリ開放
		WRITER.close();

		//アイコンをDL
		new HTTP_REQUEST(BODY_JSON.get("imageBig").asText()).DOWNLOAD("pixiv/" + AUTHOR_ID + "/" + "icon.png");

		if(!BODY_JSON.get("background").isNull()){
			new HTTP_REQUEST(BODY_JSON.get("background").get("url").asText()).DOWNLOAD("pixiv/" + AUTHOR_ID + "/" + "background.png");
		}
	}

	public void ILLUST_INDEX(String AUTHOR_ID, String ILLUST_ID, JsonNode BODY_JSON) throws IOException {
		//イラストディレクトリ
		if(!new File("pixiv/" + AUTHOR_ID + "/" + ILLUST_ID).exists()){
			Path DIR_PATH = Paths.get("pixiv/" + AUTHOR_ID + "/" + ILLUST_ID);
			Files.createDirectory(DIR_PATH);
		}

		//index.jsonのファイルパス
		String PATH = "pixiv/" + AUTHOR_ID + "/" + ILLUST_ID + "/index.json";

		//投稿者のindex.json
		File INDEX = new File(PATH);

		//index.jsonが無いか
		if(!INDEX.exists()){
			//作成
			if(INDEX.createNewFile()){
				System.out.println("index.jsonを作成しました");
			} else {//失敗
				System.err.println("index.jsonの作成に失敗しました");
				System.exit(1);
			}
		}

		//書き込む
		BufferedWriter WRITER = new BufferedWriter(new FileWriter(INDEX));

		HashMap<String, Object> MAP = new HashMap<>();
		MAP.put("ID", ILLUST_ID);
		MAP.put("TITLE", BODY_JSON.get("title").asText());
		MAP.put("DESC", BODY_JSON.get("description").asText());
		MAP.put("CREATE_DATE", BODY_JSON.get("createDate").asText());
		MAP.put("UPLOAD_DATE", BODY_JSON.get("uploadDate").asText());
		MAP.put("PAGE_COUNT", BODY_JSON.get("pageCount").asInt());

		//MAPをString化
		ObjectMapper OM = new ObjectMapper();
		String RES = OM.writeValueAsString(MAP);

		//書き込み
		WRITER.write(RES);

		//メモリ開放
		WRITER.close();
	}
}
