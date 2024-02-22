package com.rumisystem.pixiv_dlp;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.rumisystem.pixiv_dlp.GET.BOOKMARK_GET;
import com.rumisystem.pixiv_dlp.GET.ILLUST_GET;

import java.util.regex.Pattern;

public class Main {
	public static String  API_VERSION = "6c38cc7c723c6ae8b0dc7022d497a1ee751824c0";

	public static void main(String[] args) throws JsonProcessingException {
		//引数があるか
		if(args.length != 0){
			String DOWNLOAD_URL = "";				//ダウンロード先URL
			int DOWNLOAD_TYPE = 0;					//なにをDLするか(1:イラスト/2:ブックマーク)
			boolean HIDE = false;

			//引数を全て読む
			for(String ARG:args){
				//URL
				if(ARG.startsWith("https://www.pixiv.net/")){
					//URLをセット
					DOWNLOAD_URL = ARG;

					//なにをダウンロードするか
					if(Pattern.matches("https://www.pixiv.net/artworks/\\d+", ARG)){
						//イラスト
						DOWNLOAD_TYPE = 1;
					} else if(Pattern.matches("https://www.pixiv.net/users/\\d+/bookmarks/artworks", ARG)){
						//ブックマーク
						DOWNLOAD_TYPE = 2;
					}
				} else if(ARG.startsWith("--")){//設定
					//非公開を取得
					if(ARG.equals("--hide")){
						HIDE = true;
					}
				}
			}

			//実行する
			switch (DOWNLOAD_TYPE){
				case 1:{
					if(DOWNLOAD_URL.split("/").length == 5){
						String ILLUST_ID = DOWNLOAD_URL.split("/")[4];//イラストのID

						//イラストを取得しダウンロードする
						boolean DOWNLOAD = ILLUST_GET.ILLUST_DOWNLOAD(ILLUST_ID);

						//完了
						if(DOWNLOAD){
							LOG(0, "すべての仕事が完了しました");
							System.exit(0);
						}else {
							LOG(1, "ダウンロードに失敗しました");
							System.exit(1);
						}
					} else {
						LOG(1, "IDをセットしてください");
						System.exit(1);
					}
					break;
				}

				case 2:{
					String UID = DOWNLOAD_URL.split("/")[4];//イラストのID

					//イラストを取得しダウンロードする
					boolean DOWNLOAD = BOOKMARK_GET.BOOKMARK_ILLUST_DOWNLOAD(UID, HIDE);

					//完了
					if(DOWNLOAD){
						LOG(0, "すべての仕事が完了しました");
						System.exit(0);
					}else {
						LOG(1, "ダウンロードに失敗しました");
						System.exit(1);
					}
					break;
				}

				default:{
					LOG(2, "??????????????");
					System.exit(255);
				}
			}
		} else {
			HELP();
		}
	}

	public static void HELP(){
		System.out.println("Pixiv-dlp V1.0");
		System.out.println("制作：るみ/八木 瑠海 伸梧");
	}

	public static void LOG(int LEVEL, String TEXT){
		switch (LEVEL){
			case 0:{
				System.out.println("[  \u001B[32mOK\u001B[0m    ]" + TEXT);
				break;
			}

			case 1:{
				System.out.println("[  \u001B[31mERR\u001B[0m   ]" + TEXT);
				break;
			}

			case 2:{
				System.out.println("[  INFO  ]" + TEXT);
				break;
			}

			case 3:{
				System.out.println("[  ***   ]" + TEXT);
				break;
			}
		}
	}
}
