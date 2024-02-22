package com.rumisystem.pixiv_dlp.GET;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.rumisystem.pixiv_dlp.HTTP_REQUEST;

import static com.rumisystem.pixiv_dlp.Main.LOG;

public class BOOKMARK_GET {
	public static boolean BOOKMARK_ILLUST_DOWNLOAD(String UID, boolean HIDE_BOOKMARK) throws JsonProcessingException {
		try{
			int OFFSET = 0;
			int LIMIT = 48;
			boolean LOOP = true;

			//ブックマークの種類
			String REST = "show";

			//非公開のをダウンロードするか
			if(HIDE_BOOKMARK){
				REST = "hide";
			}

			while (LOOP){
				String AJAX_RESULT = new HTTP_REQUEST("https://www.pixiv.net/ajax/user/" + UID + "/illusts/bookmarks?tag=&offset=" + OFFSET + "&limit=" + LIMIT + "&rest=" + REST + "&lang=ja").GET();

				ObjectMapper OBJ_MAPPER = new ObjectMapper();
				JsonNode AJAX_RESULT_JSON = OBJ_MAPPER.readTree(AJAX_RESULT);

				if(!AJAX_RESULT_JSON.get("error").asBoolean()){
					//ブックマークの内容があるか？
					if(AJAX_RESULT_JSON.get("body").get("works").size() != 0){
						//ブックマークの中身を回す
						for(int I = 0; I < LIMIT; I++){
							JsonNode ROW = AJAX_RESULT_JSON.get("body").get("works").get(I);
							if(ROW != null){
								LOG(3, "ブックマークからダウンロードします");

								//イラストを取得しダウンロードする
								boolean DOWNLOAD = ILLUST_GET.ILLUST_DOWNLOAD(ROW.get("id").asText());
								//完了
								if(DOWNLOAD){
									LOG(0,  ((OFFSET / LIMIT) + 1) +"/" + (I + 1) + "個目おｋ");

									//レートリミット対策、というか倫理的理由で5秒待つ
									LOG(2, "5秒間待機します...");
									Thread.sleep(5000);
								}else {
									LOG(1, I + "ダウンロードできませんでした");
									return false;
								}
							} else {
								LOG(2, "此のページでやることはもうありません。");
								break;
							}
						}
					} else {
						LOG(2, "ブックーマークはもうありません");
						return true;
					}
				} else {//APIのエラー
					LOG(1, AJAX_RESULT_JSON.get("message").asText());

					return false;
				}

				//インクリメント
				OFFSET = OFFSET + LIMIT;
			}

			//終了
			return true;
		} catch (Exception EX) {
			EX.printStackTrace();
			return false;
		}
	}
}
