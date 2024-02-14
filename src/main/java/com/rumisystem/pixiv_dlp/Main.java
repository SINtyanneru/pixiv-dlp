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
				}else if(Pattern.matches("https://www.pixiv.net/users/\\d+/bookmarks/artworks", USER_INPUT_URL)){
					String UID = USER_INPUT_URL.split("/")[4];//イラストのID

					//イラストを取得しダウンロードする
					boolean DOWNLOAD = BOOKMARK_GET.BOOKMARK_ILLUST_DOWNLOAD(UID);

					//完了
					if(DOWNLOAD){
						System.out.println("[  OK   ]完了");
					}else {
						System.out.println("[  ERR  ]ダウンロードできませんｄねｈすぃた");
					}
				}
			}else {//ちがう
				System.err.println("PixivのURLを貼れ");
				System.exit(1);
			}
		}else {
			System.out.println("引数のURLを");
		}
	}
}
