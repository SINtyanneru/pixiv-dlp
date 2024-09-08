import os

BUILD_DIR = "./target/"
BIN_DIR = "/usr/local/bin/"
JAR_FILE_NAME = "pixiv_dlp-2.0-jar-with-dependencies.jar"

#インスコする関数
def INSTALL():
	if(os.path.exists(BUILD_DIR + JAR_FILE_NAME)):
		print("jarファイルが在るよ！")

		#フォルダがなければ作る
		if(os.path.exists(BIN_DIR + "pixiv-dlp-bin") == False):
			os.makedirs(BIN_DIR + "pixiv-dlp-bin")
			print("フォルダがなかったので作っといたで")

		#gccでCをビルドする
		if(os.system("gcc ./java_runner.c -o " + BIN_DIR + "pixiv-dlp") == 0):
			print("gccが成功しました！")

			#jarファイルをコピーする
			if os.system("cp " + BUILD_DIR + JAR_FILE_NAME + " " + BIN_DIR + "pixiv-dlp-bin/java.jar") == 0:
				print("全て成功！")
			else:
				print("無理だった！！")
		else:
			print("gcc失敗！！！")
	else:
		print("jarファイルがないよ！！！ビルドしろ！！")

#ルートで実行されたか？
if(os.geteuid() == 0):
	INSTALL()
else:
	print("rootで実行しろ！！")