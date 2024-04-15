#include <stdio.h>
#include <stdlib.h>
#include <string.h>

int main(int argc, char *argv[]) {
	int i;

	//実行するコマンド
	char command[1000] = "java -jar /usr/local/bin/pixiv-dlp-bin/java.jar";

	//コマンドライン引数をスペース区切りで結合
	for (i = 1; i < argc; i++) {
		strcat(command, " ");
		strcat(command, argv[i]);
	}

	//コマンド実行
	int status = system(command);

	return status;
}
