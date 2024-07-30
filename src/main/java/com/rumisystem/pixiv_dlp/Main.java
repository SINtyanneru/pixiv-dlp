package com.rumisystem.pixiv_dlp;

import com.rumisystem.pixiv_dlp.GET.BOOKMARK_GET;
import com.rumisystem.pixiv_dlp.GET.ILLUST_GET;
import org.apache.commons.cli.*;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Main {
	public static String  API_VERSION = "6c38cc7c723c6ae8b0dc7022d497a1ee751824c0";

	public static int OK_JOB = 0;
	public static int FAILED_JOB = 0;

	public static boolean HIDE = false;
	public static String DOWNLOAD_URL = "";				//ダウンロード先URL
	public static int DOWNLOAD_TYPE = 0;					//なにをDLするか (1:イラスト/2:ブックマーク)

	private static final Pattern ARTWORK_PATTERN = Pattern.compile("https://www\\.pixiv\\.net(/[a-z]+)?/artworks/(\\d+)");
	private static final Pattern BOOKMARKS_PATTERN = Pattern.compile("https://www\\.pixiv\\.net(/[a-z]+)?/users/(\\d+)/bookmarks/artworks");

	public static void main(String[] args) throws ParseException {
		// Options setup
		Options options = new Options();
		options.addOption("h", "hide", false, "非公開を取得");

		CommandLineParser parser = new DefaultParser();
		CommandLine commandLine = parser.parse(options, args);

		// 引数があるか
		if (args.length == 0) {
			HELP();
			System.exit(0);
		}

		// 引数を全て読む
		for (String ARG: args) {
			// URL
			if (ARG.startsWith("https://www.pixiv.net/")) {
				// URLをセット
				DOWNLOAD_URL = ARG;

				// なにをダウンロードするか
				if (Pattern.matches(ARTWORK_PATTERN.pattern(), ARG)) {
					DOWNLOAD_TYPE = 1; // イラスト
				} else if (Pattern.matches(BOOKMARKS_PATTERN.pattern(), ARG)) {
					DOWNLOAD_TYPE = 2; // ブックマーク
				}
			}
		}

		if (commandLine.hasOption("h")) {
			HIDE = true;
		}

		//実行する
		try {
			EXECUTE();
		} catch (Exception exception) {
            throw new RuntimeException(exception);
        }
    }

	public static void HELP() {
		System.out.println("pixiv-dlp V1.0");
		System.out.println("制作：るみ/八木 瑠海 伸梧");
	}

	public static void EXECUTE() throws Exception {
		boolean DOWNLOAD;
		String ILLUST_ID;
		Matcher matcher;

		switch (DOWNLOAD_TYPE) {
			case 1: {
				matcher = ARTWORK_PATTERN.matcher(DOWNLOAD_URL);

				if (matcher.find()) {
					if (matcher.group(2) != null) {
						ILLUST_ID = matcher.group(2); // イラストのID
						DOWNLOAD = ILLUST_GET.ILLUST_DOWNLOAD(ILLUST_ID); // イラストを取得しダウンロードする
						DOWNLOAD_REPORT(DOWNLOAD); // 完了
					}
				}

				break;
			}

			case 2: {
				matcher = BOOKMARKS_PATTERN.matcher(DOWNLOAD_URL);

				if (matcher.find()) {
					if (matcher.group(2) != null) {
						ILLUST_ID = matcher.group(2); // イラストのID
						DOWNLOAD = BOOKMARK_GET.BOOKMARK_ILLUST_DOWNLOAD(ILLUST_ID, HIDE); // イラストを取得しダウンロードする
						DOWNLOAD_REPORT(DOWNLOAD); // 完了
					}
				}

				break;
			}
		}

		LOG(2, "不明な URL");
		System.exit(255);
	}

	private static void DOWNLOAD_REPORT(boolean DOWNLOAD) {
		if (DOWNLOAD) {
			LOG(0, "すべての仕事が完了しました");
			LOG(0, "完了：" + OK_JOB);
			LOG(0, "失敗：" + FAILED_JOB);
			System.exit(0);
		} else {
			LOG(1, "ダウンロードに失敗しました");
			LOG(0, "完了：" + OK_JOB);
			LOG(0, "失敗：" + FAILED_JOB);
			System.exit(1);
		}
	}

	public static void LOG(int LEVEL, String TEXT){
		switch (LEVEL) {
			case 0: {
				System.out.println("[  \u001B[32mOK\u001B[0m  ]" + TEXT);
				break;
			}

			case 1: {
				System.out.println("[\u001B[31mFAILED\u001B[0m]" + TEXT);
				break;
			}

			case 2: {
				System.out.println("[ INFO ]" + TEXT);
				break;
			}

			case 3: {
				System.out.println("[ **** ]" + TEXT);
				break;
			}

			case 4: {
				System.out.println("\u001B[1F[  \u001B[32mOK\u001B[0m  ]");
				break;
			}

			case 5: {
				System.out.println("\u001B[1F[\u001B[31mFAILED\u001B[0m]");
				break;
			}
		}
	}
}