package com.rumisystem.pixiv_dlp.GET;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.rumisystem.pixiv_dlp.HTTP_REQUEST;

import java.util.Date;

import static com.rumisystem.pixiv_dlp.Main.API_VERSION;

public class ILLUST_GET {
	public static boolean ILLUST_DOWNLOAD(String ILLUST_ID){
		try{
			String AJAX_RESULT = new HTTP_REQUEST("https://www.pixiv.net/ajax/illust/" + ILLUST_ID + "?lang=ja").GET();

			ObjectMapper OBJ_MAPPER = new ObjectMapper();
			JsonNode AJAX_RESULT_JSON = OBJ_MAPPER.readTree(AJAX_RESULT);

			if(!AJAX_RESULT_JSON.get("error").asBoolean()){
				JsonNode BODY_JSON = AJAX_RESULT_JSON.get("body");
				String ILLUST_TITLE = BODY_JSON.get("title").asText();
				int ILLUST_PAGE_COUNT = BODY_JSON.get("pageCount").asInt();
				String ILLUST_C_DATE = BODY_JSON.get("createDate").asText();
				String ILLUST_U_DATE = BODY_JSON.get("uploadDate").asText();

				//適当に情報を吐く
				System.out.println("┌──────────────────────────────────────────┐");
				System.out.println("│ﾀｲﾄﾙ      :" + ILLUST_TITLE);
				System.out.println("│ﾍﾟｰｼﾞ     :" + ILLUST_PAGE_COUNT);
				System.out.println("│ｻｸｾｲﾋﾞ    :" + ILLUST_C_DATE);
				System.out.println("│ｺｳｼﾝﾋﾞ    :" + ILLUST_U_DATE);
				System.out.println("└──────────────────────────────────────────┘");

				//ページ枚数分回す
				for(int I = 0; I < ILLUST_PAGE_COUNT; I++){
					//画像のURL(_p0をページ番号におきかえている)
					String ORIGIN_ILLUST_URL = BODY_JSON.get("urls").get("original").asText().replace("_p0", "_p" + I);

					//ページをダウンロード(分かりやすくファイルのページ番号を+1する)
					new HTTP_REQUEST(ORIGIN_ILLUST_URL).DOWNLOAD((I + 1) + ".png");
				}
				return true;
			}else {
				System.err.println("APIエラー「" + AJAX_RESULT_JSON.get("message").asText() + "」");
				return false;
			}
		}catch (Exception EX){
			EX.printStackTrace();

			return false;
		}
	}
}
