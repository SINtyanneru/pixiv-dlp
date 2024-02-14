package com.rumisystem.pixiv_dlp.GET;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.rumisystem.pixiv_dlp.HTTP_REQUEST;

public class BOOKMARK_GET {
	public static boolean BOOKMARK_ILLUST_DOWNLOAD(String UID) throws JsonProcessingException {
		String AJAX_RESULT = new HTTP_REQUEST("https://www.pixiv.net/ajax/user/" + UID + "/illusts/bookmarks?tag=&offset=0&limit=48&rest=show&lang=ja").GET();

		ObjectMapper OBJ_MAPPER = new ObjectMapper();
		JsonNode AJAX_RESULT_JSON = OBJ_MAPPER.readTree(AJAX_RESULT);

		if(!AJAX_RESULT_JSON.get("error").asBoolean()){
			//ブックマークの中身を回す
			for(int I = 0; I < AJAX_RESULT_JSON.get("total").asInt(); I++){
				JsonNode ROW = AJAX_RESULT_JSON.get(I);
				System.out.println("ブックマークから：" + ROW.get("title").asText());

				//イラストを取得しダウンロードする
				boolean DOWNLOAD = ILLUST_GET.ILLUST_DOWNLOAD(ROW.get("id").asText());
				//完了
				if(DOWNLOAD){
					System.out.println("[  OK   ]" + I + "個目おｋ");
				}else {
					System.out.println("[  ERR  ]ダウンロードできませんでした");
					System.exit(1);
				}
			}
			return true;
		} else {
			return false;
		}
	}
}
