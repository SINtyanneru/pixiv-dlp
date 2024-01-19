package com.rumisystem.pixiv_dlp;

import com.rumisystem.pixiv_dlp.GET.ILLUST_GET;

public class Main {
	public static String  API_VERSION = "6c38cc7c723c6ae8b0dc7022d497a1ee751824c0";

	public static void main(String[] args) {
		System.out.println("pixiv-dlp V1");

		String USER_INPUT_URL = args[0];

		//URLはPixivか
		if(USER_INPUT_URL.startsWith("https://www.pixiv.net/")){
			//イラストのURL
			if(USER_INPUT_URL.startsWith("https://www.pixiv.net/artworks/")){
				//イラストのIDがあるか
				if(USER_INPUT_URL.split("/").length == 5){
					String ILLUST_ID = USER_INPUT_URL.split("/")[4];//イラストのID

					//ダウンロード中
					System.out.println("[  ***  ]イラストをダウンロードします");

					//イラストを取得しダウンロードする
					boolean DOWNLOAD = ILLUST_GET.ILLUST_DOWNLOAD(ILLUST_ID);

					//完了
					if(DOWNLOAD){
						System.out.println("[  OK   ]完了");
					}else {
						System.out.println("[  ERR  ]ダウンロードできませんｄねｈすぃた");
					}
				}else {//ない
					System.err.println("URLにIDがない");
					System.exit(1);
				}
			}
		}else {//ちがう
			System.err.println("PixivのURLを貼れ");
			System.exit(1);
		}
	}
}
