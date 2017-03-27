instrument # 一条龙
-c # 只用 apktool 反编译；一般用来测试 app 能不能被反编译
-f # 指定 apk 文件
-v # 指定 dot2smali 版本

adb_run # 用 apktool 编译 output 目录下的 app 代码，并运行
-f # 指定 apk 文件
-d # debug (in adb_monitor)

adb_monitor # 监视 app 的运行

predict # 查看监视结果
