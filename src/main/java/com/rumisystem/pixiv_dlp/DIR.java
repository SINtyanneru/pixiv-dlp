package com.rumisystem.pixiv_dlp;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import static com.rumisystem.pixiv_dlp.Main.LOG;

public class DIR {
	public DIR(String AUTHOR_ID, String ILLUST_ID, JsonNode ILLUST_BODY_JSON){
		try{
			// 親ディレクトリ
			TRY_INIT_DIRS();

			AUTHOR_INDEX(AUTHOR_ID);
			ILLUST_INDEX(AUTHOR_ID, ILLUST_ID, ILLUST_BODY_JSON);
		}catch (Exception EX){
			EX.printStackTrace();
		}
	}

	// Absolutely horrendous solution, but it works for now
	private void TRY_INIT_DIRS() throws IOException {
		if (!new File("pixiv").exists()) {
			Path DIR_PATH = Paths.get("pixiv");
			Files.createDirectory(DIR_PATH);
		}

		if (!new File("pixiv/authors/").exists()) {
			Path DIR_PATH = Paths.get("pixiv/authors/");
			Files.createDirectory(DIR_PATH);
		}

		if (!new File("pixiv/works/").exists()) {
			Path DIR_PATH = Paths.get("pixiv/works/");
			Files.createDirectory(DIR_PATH);
		}
	}

	public void AUTHOR_INDEX(String AUTHOR_ID) throws IOException {
		String BASE_PATH = "pixiv/authors/" + AUTHOR_ID;

		// 投稿者ディレクトリ
		if (!new File(BASE_PATH).exists()) {
			Path DIR_PATH = Paths.get(BASE_PATH);
			Files.createDirectory(DIR_PATH);
		} else {
			return;
		}

		String AJAX_RESULT = new HTTP_REQUEST("https://www.pixiv.net/ajax/user/" + AUTHOR_ID + "?lang=ja").GET();

		ObjectMapper OBJ_MAPPER = new ObjectMapper();
		JsonNode AJAX_RESULT_JSON = OBJ_MAPPER.readTree(AJAX_RESULT);
		JsonNode BODY_JSON = AJAX_RESULT_JSON.get("body");

		// 投稿者のindex.json
		File INDEX = new File(BASE_PATH + "/index.json");

		// 書き込む
		BufferedWriter WRITER = INIT_INDEX(INDEX);

		HashMap<String, Object> MAP = new HashMap<>();
		MAP.put("ID", BODY_JSON.get("userId").asText());
		MAP.put("NAME", BODY_JSON.get("name").asText());

		// MAPをString化
		ObjectMapper OM = new ObjectMapper();
		String RES = OM.writeValueAsString(MAP);

		// 書き込み
		WRITER.write(RES);

		// メモリ開放
		WRITER.close();

		// アイコンをDL
		new HTTP_REQUEST(BODY_JSON.get("imageBig").asText()).DOWNLOAD(BASE_PATH + "/icon.png");

		if (!BODY_JSON.get("background").isNull()) {
			new HTTP_REQUEST(BODY_JSON.get("background").get("url").asText()).DOWNLOAD(BASE_PATH + "/background.png");
		}
	}

	public void ILLUST_INDEX(String AUTHOR_ID, String ILLUST_ID, JsonNode BODY_JSON) throws IOException {
		String BASE_PATH = "pixiv/works/" + ILLUST_ID;

		//イラストディレクトリ
		if (!new File(BASE_PATH).exists()) {
			Path DIR_PATH = Paths.get(BASE_PATH);
			Files.createDirectory(DIR_PATH);
		}

		// 投稿者のindex.json
		File INDEX = new File(BASE_PATH + "/index.json");

		// 書き込む
		BufferedWriter WRITER = INIT_INDEX(INDEX);

		HashMap<String, Object> MAP = new HashMap<>();
		MAP.put("ID", ILLUST_ID);
		MAP.put("TITLE", BODY_JSON.get("title").asText());
		MAP.put("DESC", BODY_JSON.get("description").asText());
		MAP.put("AUTHOR", AUTHOR_ID);
		MAP.put("CREATE_DATE", BODY_JSON.get("createDate").asText());
		MAP.put("UPLOAD_DATE", BODY_JSON.get("uploadDate").asText());
		MAP.put("PAGE_COUNT", BODY_JSON.get("pageCount").asInt());

		List<Object> TAG_LIST = new ArrayList<>();

		for(JsonNode ROW:BODY_JSON.get("tags").get("tags")){
			HashMap<String, String> TAG_INFO = new HashMap<>();

			TAG_INFO.put("NAME", ROW.get("tag").asText());

			if (ROW.get("userId") != null) {
				TAG_INFO.put("ADD_USER", ROW.get("userId").asText());
			} else {
				TAG_INFO.put("ADD_USER", null);
			}

			TAG_INFO.put("LOCKED", ROW.get("locked").asText());
			TAG_LIST.add(TAG_INFO);
		}

		MAP.put("TAG", TAG_LIST);

		//MAPをString化
		ObjectMapper OM = new ObjectMapper();
		String RES = OM.writeValueAsString(MAP);

		//書き込み
		WRITER.write(RES);

		//メモリ開放
		WRITER.close();
	}

	public BufferedWriter INIT_INDEX(File INDEX) throws IOException {
		if (!INDEX.exists()) {
			// 作成
			if (INDEX.createNewFile()) {
				LOG(2, "index.jsonを作成しました");
			} else { //失敗
				LOG(1, "index.jsonの作成に失敗しました");
				System.exit(1);
			}
		}

		// 書き込む
		return new BufferedWriter(new FileWriter(INDEX));
	}
}
