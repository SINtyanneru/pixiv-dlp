package com.rumisystem.pixiv_dlp.GET;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.rumisystem.pixiv_dlp.HTTP_REQUEST;

public class BOOKMARK_GET {
	public static boolean BOOKMARK_ILLUST_DOWNLOAD(String UID) throws JsonProcessingException {
		try{
			int OFFSET = 0;
			int LIMIT = 0;
			boolean LOOP = true;
			while (LOOP){
				String AJAX_RESULT = new HTTP_REQUEST("https://www.pixiv.net/ajax/user/" + UID + "/illusts/bookmarks?tag=&offset=" + OFFSET + "&limit=" + LIMIT + "&rest=show&lang=ja").GET();

				ObjectMapper OBJ_MAPPER = new ObjectMapper();
				JsonNode AJAX_RESULT_JSON = OBJ_MAPPER.readTree(AJAX_RESULT);

				if(!AJAX_RESULT_JSON.get("error").asBoolean()){
					//ブックマークの内容があるか？
					if(AJAX_RESULT_JSON.get("body").get("works").size() != 0){
						//ブックマークの中身を回す
						for(int I = 0; I < LIMIT; I++){
							JsonNode ROW = AJAX_RESULT_JSON.get("body").get("works").get(I);
							System.out.println("ブックマークから：" + ROW.get("title").asText());

							//イラストを取得しダウンロードする
							boolean DOWNLOAD = ILLUST_GET.ILLUST_DOWNLOAD(ROW.get("id").asText());
							//完了
							if(DOWNLOAD){
								System.out.println("[  OK   ]" + I + "個目おｋ");

								//レートリミット対策、というか倫理的理由で1秒待つ
								Thread.sleep(1000);
							}else {
								System.out.println("[  ERR  ]ダウンロードできませんでした");
								return false;
							}
						}
					} else {
						return true;
					}
				} else {//APIのエラー
					System.err.println("[  ERR  ]" + AJAX_RESULT_JSON.get("message").asText());

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
