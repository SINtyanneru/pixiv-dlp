package com.rumisystem.pixiv_dlp;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.rumisystem.pixiv_dlp.ENUM.LOG_TYPE;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import static com.rumisystem.pixiv_dlp.Main.LOG;

public class HTTP_REQUEST {
	private URL REQUEST_URI = null;

	public HTTP_REQUEST(String INPUT_REQ_URL){
		try {
			REQUEST_URI = new URL(INPUT_REQ_URL);
		} catch (Exception EX) {
			System.err.println(EX);
			System.exit(1);
		}
	}

	//GET
	public String GET(){
		try{
			LOG(LOG_TYPE.PROCESS, REQUEST_URI.toString() + "にGETリクエストを送信");
			HttpURLConnection HUC = GET_HUC();

			//レスポンスコード
			int RES_CODE = HUC.getResponseCode();

			if (RES_CODE == 200) {
				BufferedReader BR = new BufferedReader(new InputStreamReader(HUC.getInputStream(), StandardCharsets.UTF_8));
				StringBuilder RES_STRING = new StringBuilder();

				String INPUT_LINE;
				while ((INPUT_LINE = BR.readLine()) != null){
					RES_STRING.append(INPUT_LINE);
				}

				BR.close();

				LOG(LOG_TYPE.PROCESS_END_OK, "HTTP通信が完了しました");
				return RES_STRING.toString();
			} else if (RES_CODE == 429) {
				//レートリミット、10秒感待ってから最実行する
				LOG(LOG_TYPE.INFO, "レートリミット！10秒間待機します...");
				Thread.sleep(10000);

				//再実行
				return GET();
			} else if(RES_CODE == 404){
				LOG(LOG_TYPE.PROCESS_END_FAILED, "");
				LOG(LOG_TYPE.FAILED, "404、イラストが存在しません");
				return null;
			} else {
				LOG(LOG_TYPE.PROCESS_END_FAILED, "");
				LOG(LOG_TYPE.FAILED, "エラー" + RES_CODE + "です、処理を終了します");
				System.exit(1);
				return null;
			}
		} catch (Exception EX){
			EX.printStackTrace();
			System.exit(1);
			return null;
		}
	}

	private HttpURLConnection GET_HUC() throws IOException {
		HttpURLConnection HUC = (HttpURLConnection) REQUEST_URI.openConnection();

		//GETリクエストだと主張する
		HUC.setRequestMethod("GET");

		//ヘッダーを入れる
		HUC.setRequestProperty("Host", "i.pximg.net");
		HUC.setRequestProperty("User-Agent", "Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36");
		HUC.setRequestProperty("Referer", "https://www.pixiv.net/");

		File COOKIE_FILE = new File("./cookie.txt");
		if(COOKIE_FILE.exists()){
			StringBuilder COOKIE = new StringBuilder();

			FileReader FR = new FileReader(COOKIE_FILE);
			BufferedReader BR = new BufferedReader(FR);

			String TEMP;
			while ((TEMP = BR.readLine()) != null) {
				COOKIE.append(TEMP);
			}
			BR.close();

			HUC.setRequestProperty("Cookie", COOKIE.toString());
		}
		return HUC;
	}

	//ダウンロード
	public void DOWNLOAD(String PATH){
		try{
			//名前が長すぎるので切り落としたよ
			LOG(LOG_TYPE.PROCESS, REQUEST_URI.toString().split("/")[REQUEST_URI.toString().split("/").length - 1] + "をダウンロード");
			HttpURLConnection HUC = (HttpURLConnection) REQUEST_URI.openConnection();

			//GETリクエストだと主張する
			HUC.setRequestMethod("GET");

			//ヘッダーを入れる
			HUC.setRequestProperty("Accept", "image/avif,image/jxl,image/webp,image/png,image/svg+xml,image/*;q=0.8,*/*;q=0.5");
			HUC.setRequestProperty("Accept-Encoding", "gzip, deflate, br, zstd");
			HUC.setRequestProperty("Host", "i.pximg.net");
			HUC.setRequestProperty("User-Agent", "Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36");
			HUC.setRequestProperty("Referer", "https://pixiv.net");

			//レスポンスコード
			int RES_CODE = HUC.getResponseCode();
			if(RES_CODE == HttpURLConnection.HTTP_OK){
				LOG(LOG_TYPE.PROCESS_END_OK, "");
				LOG(LOG_TYPE.PROCESS, PATH + "へ保存中");

				//ファイルを保存する機構
				InputStream IS = HUC.getInputStream();
				FileOutputStream OS = new FileOutputStream(PATH);
				byte[] BUFFER = new byte[4096];
				int BYTES_READ;
				while((BYTES_READ = IS.read(BUFFER)) != -1){
					OS.write(BUFFER, 0, BYTES_READ);
				}

				LOG(LOG_TYPE.PROCESS_END_OK, PATH);
			} else if (RES_CODE == 429) {
				//レートリミット、10秒感待ってから最実行する
				LOG(LOG_TYPE.INFO, "レートリミット！10秒間待機します...");
				Thread.sleep(10000);

				//再実行
				DOWNLOAD(PATH);
			} else {
				LOG(LOG_TYPE.PROCESS_END_FAILED, "");
				LOG(LOG_TYPE.FAILED, "失敗:" + RES_CODE);
			}
		}catch (Exception EX){
			EX.printStackTrace();
			System.exit(1);
		}
	}

	public List<String> JSON_KEYS_GET(String json, ObjectMapper mapper) throws JsonMappingException, JsonProcessingException {
		List<String> keys = new ArrayList<>();
		JsonNode jsonNode = mapper.readTree(json);
		Iterator<String> iterator = jsonNode.fieldNames();
		iterator.forEachRemaining(keys::add);
		return keys;
	}
}
