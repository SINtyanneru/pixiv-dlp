package com.rumisystem.pixiv_dlp.GET;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.rumisystem.pixiv_dlp.HTTP_REQUEST;
import com.rumisystem.pixiv_dlp.LOG_TYPE;

import com.rumisystem.pixiv_dlp.DL.*;

import java.io.File;

import static com.rumisystem.pixiv_dlp.Main.*;

public class ILLUST_GET {
	public static int ILLUST_DOWNLOAD(String ILLUST_ID) throws Exception {
		String AJAX_RESULT = new HTTP_REQUEST("https://www.pixiv.net/ajax/illust/" + ILLUST_ID + "?lang=ja").GET();
		ObjectMapper OBJ_MAPPER;
		JsonNode AJAX_RESULT_JSON;

		// nullちぇっく
		if (AJAX_RESULT == null) {
			LOG(1, "処理をスキップします");
			FAILED_JOB++;

			return 0;
		}

		OBJ_MAPPER = new ObjectMapper();
		AJAX_RESULT_JSON = OBJ_MAPPER.readTree(AJAX_RESULT);

		if (!AJAX_RESULT_JSON.get("error").asBoolean()) {
			JsonNode BODY_JSON = AJAX_RESULT_JSON.get("body");
			String ILLUST_TITLE = BODY_JSON.get("title").asText();
			int ILLUST_PAGE_COUNT = BODY_JSON.get("pageCount").asInt();
			String ILLUST_C_DATE = BODY_JSON.get("createDate").asText();
			String ILLUST_U_DATE = BODY_JSON.get("uploadDate").asText();
			String AUTHOR_ID = BODY_JSON.get("userId").asText();

			File ILLUST_FILE = new File("pixiv/" + AUTHOR_ID + "/" + ILLUST_ID);

			if (!ILLUST_FILE.exists()) {
				// 適当に情報を吐く
				LOG(2, "┌──────────────────────────────────────────┐");
				LOG(2, "│ﾀｲﾄﾙ      :" + ILLUST_TITLE);
				LOG(2, "│ﾍﾟｰｼﾞ     :" + ILLUST_PAGE_COUNT);
				LOG(2, "│ｻｸｾｲﾋﾞ    :" + ILLUST_C_DATE);
				LOG(2, "│ｺｳｼﾝﾋﾞ    :" + ILLUST_U_DATE);
				LOG(2, "└──────────────────────────────────────────┘");

				if (BODY_JSON.get("illustType").asInt() == 0) {
					return ILLUST_DL.DOWNLOAD(BODY_JSON, ILLUST_ID, AUTHOR_ID, ILLUST_PAGE_COUNT);
				} else if (BODY_JSON.get("illustType").asInt() == 2) {
					return UGOIRA_DL.DOWNLOAD(BODY_JSON, ILLUST_ID, AUTHOR_ID);
				} else {
					LOG(1, "エラー");
					FAILED_JOB++;

					return 0;
				}
			} else {
				LOG(LOG_TYPE.OK, "すでに存在するのでスキップしました");
				return 2;
			}
		} else {
			LOG(1, "APIエラー「" + AJAX_RESULT_JSON.get("message").asText() + "」");
			FAILED_JOB++;

			return 0;
		}
	}
}
