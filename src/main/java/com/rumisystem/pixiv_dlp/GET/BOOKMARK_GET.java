package com.rumisystem.pixiv_dlp.GET;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.rumisystem.pixiv_dlp.HTTP_REQUEST;

import static com.rumisystem.pixiv_dlp.Main.LOG;

public class BOOKMARK_GET {
	public static int BOOKMARK_ILLUST_DOWNLOAD(String UID, boolean HIDE_BOOKMARK) throws Exception {
		int OFFSET = 0;
		int LIMIT = 48;
		boolean LOOP = true;

		// ブックマークの種類
		String REST = "show";

		// 非公開のをダウンロードするか
		if (HIDE_BOOKMARK) {
			REST = "hide";
		}

		while (LOOP) {
			String AJAX_RESULT = new HTTP_REQUEST("https://www.pixiv.net/ajax/user/" + UID + "/illusts/bookmarks?tag=&offset=" + OFFSET + "&limit=" + LIMIT + "&rest=" + REST + "&lang=ja").GET();

			if (AJAX_RESULT != null) {
				ObjectMapper OBJ_MAPPER = new ObjectMapper();
				JsonNode AJAX_RESULT_JSON = OBJ_MAPPER.readTree(AJAX_RESULT);

				if (!AJAX_RESULT_JSON.get("error").asBoolean()) {
					// ブックマークの内容があるか？
					if (!AJAX_RESULT_JSON.get("body").get("works").isEmpty()) {
						// ブックマークの中身を回す
						for (int I = 0; I < LIMIT; I++) {
							JsonNode ROW = AJAX_RESULT_JSON.get("body").get("works").get(I);

							if (ROW != null) {
								LOG(3, "ブックマークからダウンロードします");

								// イラストを取得しダウンロードする
								int DOWNLOAD = ILLUST_GET.ILLUST_DOWNLOAD(ROW.get("id").asText());

								// 完了
								if (DOWNLOAD == 1) {
									LOG(0,  ((OFFSET / LIMIT) + 1) + "/" + (I + 1) + "個目おｋ");

									// レートリミット対策、というか倫理的理由で5秒待つ
									LOG(2, "5秒間待機します...");
									Thread.sleep(5000);
								} else if (DOWNLOAD == 0) {
									LOG(1, "#" + I + ": ダウンロードできませんでした");
								}
							} else {
								LOG(2, "此のページでやることはもうありません。");
								break;
							}
						}
					} else {
						LOG(2, "ブックーマークはもうありません");
						return 1;
					}
				} else { // APIのエラー
					LOG(1, AJAX_RESULT_JSON.get("message").asText());
					return 0;
				}
			}

			// インクリメント
			OFFSET = OFFSET + LIMIT;
		}

		// 終了
		return 1;
	}
}
