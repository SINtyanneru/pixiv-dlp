package com.rumisystem.pixiv_dlp.GET;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.rumisystem.pixiv_dlp.DIR;
import com.rumisystem.pixiv_dlp.HTTP_REQUEST;
import com.rumisystem.pixiv_dlp.LOG_TYPE;
import com.rumisystem.rumi_java_lib.FILER;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import static com.rumisystem.pixiv_dlp.Main.*;

public class ILLUST_GET {
	public static boolean ILLUST_DOWNLOAD(String ILLUST_ID) throws Exception {
		String AJAX_RESULT = new HTTP_REQUEST("https://www.pixiv.net/ajax/illust/" + ILLUST_ID + "?lang=ja").GET();
		ObjectMapper OBJ_MAPPER;
		JsonNode AJAX_RESULT_JSON;

		//nullちぇっく
		if (AJAX_RESULT == null) {
			LOG(1, "処理をスキップします");
			FAILED_JOB++;

			return false;
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
							new HTTP_REQUEST(ORIGIN_ILLUST_URL).DOWNLOAD("pixiv/" + AUTHOR_ID + "/" + ILLUST_ID + "/" + (i + 1) + ".png");
						}

						OK_JOB++;
						return true;
					}

					LOG(1, "pagesがNullです、スキップします");
					FAILED_JOB++;

					return false;
				} else if (BODY_JSON.get("illustType").asInt() == 2) {
					LOG(0, "うごイラです、ffmpegを使用して結合します。。。");

					// FFMPEGがあるか？
					if(!Files.exists(Path.of("/bin/ffmpeg"))){
						LOG(1, "FFMPEGがありません、スキップします");
						FAILED_JOB++;

						return false;
					}

					// うごイラの情報を落とす
					String AJAX_UGOIRA_META = new HTTP_REQUEST("https://www.pixiv.net/ajax/illust/" + ILLUST_ID + "/ugoira_meta").GET();
					JsonNode UGOIRA_META = new ObjectMapper().readTree(AJAX_UGOIRA_META);

					// エラーがないことを確認(trueでエラーです、私はるみ鯖式が好み)
					if (UGOIRA_META.get("error").asBoolean()) {
						LOG(1, "エラー");
						FAILED_JOB++;

						return false;
					}

					// ディレクトリを作成する
					new DIR(AUTHOR_ID, ILLUST_ID, BODY_JSON);

					// ZIPを落とす
					new HTTP_REQUEST(UGOIRA_META.get("body").get("originalSrc").asText()).DOWNLOAD("pixiv/" + AUTHOR_ID + "/" + ILLUST_ID + "/origin.zip");

					// ZIPを解凍
					ZipInputStream ZIS = new ZipInputStream(Files.newInputStream(Path.of("pixiv/" + AUTHOR_ID + "/" + ILLUST_ID + "/origin.zip")));
					ZipEntry E;

					while ((E = ZIS.getNextEntry()) != null) {
						Files.write(Path.of("pixiv/" + AUTHOR_ID + "/" + ILLUST_ID + "/" + E.getName()), ZIS.readAllBytes());
					}
					
					return PROCESS_UGOIRA(UGOIRA_META, AUTHOR_ID, ILLUST_ID);
				} else {
					LOG(1, "エラー");
					FAILED_JOB++;

					return false;
				}
			} else {
				LOG(LOG_TYPE.OK, "すでに存在するのでスキップしました");
				return true;
			}
		} else {
			LOG(1, "APIエラー「" + AJAX_RESULT_JSON.get("message").asText() + "」");
			FAILED_JOB++;

			return false;
		}
	}

	private static boolean PROCESS_UGOIRA(JsonNode UGOIRA_META, String AUTHOR_ID, String ILLUST_ID) throws IOException, InterruptedException {
		// フレームテキストを作る
		StringBuilder FRAME_TEXT = new StringBuilder();
		JsonNode UGOIRA_FRAMES = UGOIRA_META.get("body").get("frames");
		
		for (int i = 0; i < UGOIRA_FRAMES.size(); i++) {
			String NAME = UGOIRA_FRAMES.get(i).get("file").asText();
			double TIME = UGOIRA_FRAMES.get(i).get("delay").asInt() / 1000.0;

			FRAME_TEXT.append("file '").append(NAME).append("'\n");
			FRAME_TEXT.append("duration ").append(TIME).append("\n");
		}

		new FILER(new File("pixiv/" + AUTHOR_ID + "/" + ILLUST_ID + "/frame.txt")).WRITE_STRING(FRAME_TEXT.toString());

		// ffmpeg
		LOG(3, "結合中");

		// コマンド容易
		String[] APNG_CMD = new String[] {
				"/bin/ffmpeg",
				"-f", "concat",
				"-safe", "0",
				"-i", "./pixiv/" + AUTHOR_ID + "/" + ILLUST_ID + "/frame.txt",
				"-plays", "0", "./pixiv/" + AUTHOR_ID + "/" + ILLUST_ID + "/output.apng"
		};

		String[] GIF_CMD = new String[] {
				"/bin/ffmpeg",
				"-i", "./pixiv/" + AUTHOR_ID + "/" + ILLUST_ID + "/output.apng",
				"-filter_complex", "[0:v] split [a][b];[a] palettegen [p];[b][p] paletteuse",
				"./pixiv/" + AUTHOR_ID + "/" + ILLUST_ID + "/output.gif"
		};

		Process APNG_PROCESS = new ProcessBuilder(APNG_CMD).start();
		int APNG_RESULT = APNG_PROCESS.waitFor();
		Process GIF_PROCESS = new ProcessBuilder(GIF_CMD).start();
		int GIF_RESULT = GIF_PROCESS.waitFor();

		if (APNG_RESULT == 0 && GIF_RESULT == 0) {
			LOG(4, "");

			// 要らないファイルを消していく
			LOG(3, "お掃除しています...");

			// 連番ファイル
			for (int I = 0; I < UGOIRA_FRAMES.size(); I++) {
				String NAME = "pixiv/" + AUTHOR_ID + "/" + ILLUST_ID + "/" + UGOIRA_FRAMES.get(I).get("file").asText();
				new File(NAME).delete();

				LOG(0, "削除：" + NAME);
			}

			// frame.txt
			new File("pixiv/" + AUTHOR_ID + "/" + ILLUST_ID + "/frame.txt").delete();
			LOG(0, "削除：" + "pixiv/" + AUTHOR_ID + "/" + ILLUST_ID + "/frame.txt");

			// origin.zip
			new File("pixiv/" + AUTHOR_ID + "/" + ILLUST_ID + "/origin.zip").delete();
			LOG(0, "削除：" + "pixiv/" + AUTHOR_ID + "/" + ILLUST_ID + "/origin.zip");

			// 終わり
			LOG(0, "完了");
			OK_JOB++;

			return true;
		} else {
			LOG(5, "");
			FAILED_JOB++;

			return false;
		}
	}
}
