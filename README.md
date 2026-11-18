# 面包狗表情包

这个项目是基于[酷安表情包](https://github.com/gddhy/coolapk-emotion)进行了大量的代码重构，，功能比原版软件强大很多。

可用作模板打包成其他表情包软件，也鼓励对本软件的二次开发，本项目使用GPL3.0开源软件协议。

## 如何快速制作属于自己的表情包软件

1. 下载项目源码，可通过GitHub的下载压缩包功能或者使用git命令

   ```
   git clone https://github.com/7980963/Bread-Dog
   ```
2. 下载[Android Studio](https://developer.android.google.cn/studio/)，搭建好安卓开发环境
3. 项目中的app/src/main/assets为表情包存放目录，将你像要的表情包以.zip压缩包形式打包存放，表情包支持常用图片格式，长宽比最好为1：1，表情包的图片名会自动显示在图片下方（不会显示扩展名），压缩包命名格式为

   ```
   序号_表情包名称.zip
   ```
   其中序号为一个两位数数字。例如 `01_小二柴.zip`。序号不足两位请用 0 补齐；支持 ZIP 内的子目录，不支持 RAR、tar.gz。支持 PNG、JPEG、GIF、WebP、BMP，其他文件不会进入列表。解压总大小上限为 256 MiB，条目数上限为 10000。
4. 项目中的app/src/main/res/mipmap为表情包的缩略图存放目录，即在表情包列表中显示的缩略图文件，命名格式为

   ```
   image序号.gif
   ```
   序号即为前面的压缩包序号，与前文的压缩包一一对应，支持常见的图片文件。缩略图可选，缺少时使用应用图标。
5. 编译项目，程序会自动根据前文所提到的文件自动生成表情包目录。

## 开发与验证

使用 JDK 17、Android SDK 34 和项目自带的 Gradle Wrapper。仓库包含由现有应用图标打包的 `00_Sample.zip`，可以直接启动；制作自己的版本时可删除示例包。没有 ZIP 时显示空状态，不再依赖特定名称的素材。

```powershell
.\gradlew.bat :app:testDebugUnitTest :app:assembleDebug :app:lintDebug
.\gradlew.bat :app:connectedDebugAndroidTest
```

第二条命令需要连接 Android 设备或启动模拟器。测试覆盖文件过滤、中文文件名、路径穿越、损坏压缩包、导出流失败，以及页面重建与导出状态恢复。

发布构建不再使用 debug 密钥。通过 Android Studio 的 Generate Signed Bundle / APK 配置自己的发布密钥，并妥善保管；不要把密钥或密码提交到仓库。

手工回归：切换多个表情包、加载时旋转屏幕、损坏包失败后重试、打开保存位置选择器后旋转或后台回收应用、取消保存，以及 QQ/微信/TIM 未安装时的分享提示。微信预览仍需在目标应用中验证。

   ## 已知Bug

   分享到微信的预览界面为空白，原因未知，不影响实际使用
