package com.rumisystem.pixiv_dlp.DL;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.rumisystem.pixiv_dlp.DIR;
import com.rumisystem.pixiv_dlp.HTTP_REQUEST;
import com.rumisystem.rumi_java_lib.FILER;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import static com.rumisystem.pixiv_dlp.Main.*;

public class UGOIRA_DL {
	public static int DOWNLOAD(JsonNode BODY_JSON, String ILLUST_ID, String AUTHOR_ID)
			throws IOException, InterruptedException {
		LOG(0, "うごイラです、ffmpegを使用して結合します。。。");

		// FFMPEGがあるか？
		if (!CHECK_FFMPEG()) {
			return 0;
		}

		String BASE_PATH = "pixiv/works/" + ILLUST_ID;

		// うごイラの情報を落とす
		String AJAX_UGOIRA_META = new HTTP_REQUEST("https://www.pixiv.net/ajax/illust/" + ILLUST_ID + "/ugoira_meta")
				.GET();
		JsonNode UGOIRA_META = new ObjectMapper().readTree(AJAX_UGOIRA_META);

		// エラーがないことを確認(trueでエラーです、私はるみ鯖式が好み)
		if (UGOIRA_META.get("error").asBoolean()) {
			LOG(1, "エラー");
			FAILED_JOB++;

			return 0;
		}

		// ディレクトリを作成する
		new DIR(AUTHOR_ID, ILLUST_ID, BODY_JSON);

		// ZIPを落とす
		new HTTP_REQUEST(UGOIRA_META.get("body").get("originalSrc").asText()).DOWNLOAD(BASE_PATH + "/origin.zip");

		// ZIPを解凍
		ZipInputStream ZIS = new ZipInputStream(Files.newInputStream(Path.of(BASE_PATH + "/origin.zip")));
		ZipEntry E;

		while ((E = ZIS.getNextEntry()) != null) {
			Files.write(Path.of(BASE_PATH + "/" + E.getName()), ZIS.readAllBytes());
		}

		return PROCESS_UGOIRA(UGOIRA_META, AUTHOR_ID, ILLUST_ID);
	}

	private static int PROCESS_UGOIRA(JsonNode UGOIRA_META, String AUTHOR_ID, String ILLUST_ID)
			throws IOException, InterruptedException {
		// フレームテキストを作る
		StringBuilder FRAME_TEXT = new StringBuilder();
		JsonNode UGOIRA_FRAMES = UGOIRA_META.get("body").get("frames");

		for (int i = 0; i < UGOIRA_FRAMES.size(); i++) {
			String NAME = UGOIRA_FRAMES.get(i).get("file").asText();
			double TIME = UGOIRA_FRAMES.get(i).get("delay").asInt() / 1000.0;

			FRAME_TEXT.append("file '").append(NAME).append("'\n");
			FRAME_TEXT.append("duration ").append(TIME).append("\n");
		}

		String BASE_PATH = "pixiv/works/" + ILLUST_ID;

		new FILER(new File(BASE_PATH + "/frame.txt")).WRITE_STRING(FRAME_TEXT.toString());

		// ffmpeg
		LOG(3, "結合中");

		// コマンド容易
		String[] APNG_CMD = new String[] { "ffmpeg", "-f", "concat", "-safe", "0", "-i",
				"./" + BASE_PATH + "/frame.txt", "-plays", "0", "./" + BASE_PATH + "/output.apng" };

		String[] GIF_CMD = new String[] { "ffmpeg", "-i", "./" + BASE_PATH + "/output.apng", "-filter_complex",
				"[0:v] split [a][b];[a] palettegen [p];[b][p] paletteuse", "./" + BASE_PATH + "/output.gif" };

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
				String NAME = BASE_PATH + "/" + UGOIRA_FRAMES.get(I).get("file").asText();
				new File(NAME).delete();

				LOG(0, "削除：" + NAME);
			}

			// frame.txt
			new File(BASE_PATH + "/frame.txt").delete();
			LOG(0, "削除：" + BASE_PATH + "/frame.txt");

			// origin.zip
			new File(BASE_PATH + "/origin.zip").delete();
			LOG(0, "削除：" + BASE_PATH + "/origin.zip");

			// 終わり
			LOG(0, "完了");
			OK_JOB++;

			return 1;
		} else {
			LOG(5, "");
			FAILED_JOB++;

			return 0;
		}
	}
}
