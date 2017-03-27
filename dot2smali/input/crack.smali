.class public Lcrack;
.super Ljava/lang/Object;
.source "crack.java"

.method public static logInt(I)V
    .locals 2
    .prologue

    const-string/jumbo v0, "trackdroid"
    invoke-static {p0}, Ljava/lang/String;->valueOf(I)Ljava/lang/String;
    move-result-object v1
    invoke-static {v0, v1}, Landroid/util/Log;->v(Ljava/lang/String;Ljava/lang/String;)I
    return-void
.end method

.method public static logStr(Ljava/lang/String;)V
    .locals 1
    .prologue

    const-string/jumbo v0, "trackdroid"
    invoke-static {v0, p0}, Landroid/util/Log;->v(Ljava/lang/String;Ljava/lang/String;)I
    return-void
.end method

.method public static getDeviceId()Ljava/lang/String;
    .locals 13

    .prologue
    const-string/jumbo v8, "/sdcard/deviceid/imei.txt"

    const/4 v11, 0x0

    new-instance v2, Ljava/io/File;

    invoke-direct {v2, v8}, Ljava/io/File;-><init>(Ljava/lang/String;)V

    if-eqz v2, :cond_0

    invoke-virtual {v2}, Ljava/io/File;->exists()Z

    move-result v12

    if-eqz v12, :cond_0

    const/4 v3, 0x0

    :try_start_0
    new-instance v4, Ljava/io/FileInputStream;

    invoke-direct {v4, v2}, Ljava/io/FileInputStream;-><init>(Ljava/io/File;)V
    :try_end_0
    .catch Ljava/io/FileNotFoundException; {:try_start_0 .. :try_end_0} :catch_0

    move-object v3, v4

    :goto_0
    const/4 v5, 0x0

    :try_start_1
    new-instance v6, Ljava/io/InputStreamReader;

    const-string/jumbo v12, "utf-8"

    invoke-direct {v6, v3, v12}, Ljava/io/InputStreamReader;-><init>(Ljava/io/InputStream;Ljava/lang/String;)V
    :try_end_1
    .catch Ljava/io/UnsupportedEncodingException; {:try_start_1 .. :try_end_1} :catch_3

    :try_start_2
    new-instance v9, Ljava/io/BufferedReader;

    invoke-direct {v9, v6}, Ljava/io/BufferedReader;-><init>(Ljava/io/Reader;)V

    new-instance v10, Ljava/lang/StringBuffer;

    const-string/jumbo v12, ""

    invoke-direct {v10, v12}, Ljava/lang/StringBuffer;-><init>(Ljava/lang/String;)V

    invoke-virtual {v9}, Ljava/io/BufferedReader;->readLine()Ljava/lang/String;

    move-result-object v7

    invoke-virtual {v10, v7}, Ljava/lang/StringBuffer;->append(Ljava/lang/String;)Ljava/lang/StringBuffer;

    invoke-virtual {v9}, Ljava/io/BufferedReader;->close()V

    invoke-virtual {v3}, Ljava/io/FileInputStream;->close()V

    invoke-virtual {v10}, Ljava/lang/StringBuffer;->toString()Ljava/lang/String;
    :try_end_2
    .catch Ljava/io/IOException; {:try_start_2 .. :try_end_2} :catch_1
    .catch Ljava/io/UnsupportedEncodingException; {:try_start_2 .. :try_end_2} :catch_2

    move-result-object v11

    :cond_0
    :goto_1
    return-object v11

    :catch_0
    move-exception v1

    invoke-virtual {v1}, Ljava/io/FileNotFoundException;->printStackTrace()V

    goto :goto_0

    :catch_1
    move-exception v0

    :try_start_3
    invoke-virtual {v0}, Ljava/io/IOException;->printStackTrace()V
    :try_end_3
    .catch Ljava/io/UnsupportedEncodingException; {:try_start_3 .. :try_end_3} :catch_2

    goto :goto_1

    :catch_2
    move-exception v0

    move-object v5, v6

    :goto_2
    invoke-virtual {v0}, Ljava/io/UnsupportedEncodingException;->printStackTrace()V

    goto :goto_1

    :catch_3
    move-exception v0

    goto :goto_2
.end method

.method public static logMethod0()V
    .locals 2
    .prologue

    const-string/jumbo v0, "trackdroid"
    const-string/jumbo v1, "Call Method: Lsampleclass;->samplemethod"
    invoke-static {v0, v1}, Landroid/util/Log;->v(Ljava/lang/String;Ljava/lang/String;)I
    return-void
.end method

.method public static logApi0()V
    .locals 2
    .prologue

    const-string/jumbo v0, "trackdroid"
    const-string/jumbo v1, "Call API: Ljava/net/URL;-><init>(Ljava/lang/String;)"
    invoke-static {v0, v1}, Landroid/util/Log;->v(Ljava/lang/String;Ljava/lang/String;)I
    return-void
.end method

