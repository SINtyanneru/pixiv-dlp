package com.rumisystem.pixiv_dlp.DL;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.rumisystem.pixiv_dlp.DIR;
import com.rumisystem.pixiv_dlp.HTTP_REQUEST;

import static com.rumisystem.pixiv_dlp.Main.*;

public class ILLUST_DL {
    public static int DOWNLOAD(JsonNode BODY_JSON, String ILLUST_ID, String AUTHOR_ID, int ILLUST_PAGE_COUNT) throws JsonProcessingException {
        String AJAX_ILLUST_ALL_PAGE = new HTTP_REQUEST("https://www.pixiv.net/ajax/illust/" + ILLUST_ID + "/pages?lang=ja").GET();

        // nullちぇっく
        if (AJAX_ILLUST_ALL_PAGE != null) {
            ObjectMapper ALL_PAGE_OBJ_MAPPER = new ObjectMapper();
            JsonNode ILLUST_ALL_PAGE_JSON = ALL_PAGE_OBJ_MAPPER.readTree(AJAX_ILLUST_ALL_PAGE);

            // ページ枚数分回す
            for (int i = 0; i < ILLUST_PAGE_COUNT; i++) {
                // 画像のURL(_p0をページ番号におきかえている)
                String ORIGIN_ILLUST_URL = ILLUST_ALL_PAGE_JSON.get("body").get(i).get("urls").get("original").asText();

                // ディレクトリを作成する
                new DIR(AUTHOR_ID, ILLUST_ID, BODY_JSON);

                // ページをダウンロード(分かりやすくファイルのページ番号を+1する)
                new HTTP_REQUEST(ORIGIN_ILLUST_URL).DOWNLOAD("pixiv/works/" + ILLUST_ID + "/" + (i + 1) + ".png");
            }

            OK_JOB++;
            return 1;
        }

        LOG(1, "pagesがNullです、スキップします");
        FAILED_JOB++;

        return 0;
    }
}
