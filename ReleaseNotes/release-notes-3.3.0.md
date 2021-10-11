# PLDroidShortVideo Release Notes for 3.2.2

### 简介

PLDroidShortVideo 是七牛推出的一款适用于 Android 平台的短视频 SDK，提供了包括美颜、滤镜、水印、断点录制、分段回删、视频编辑、混音特效、本地/云端存储在内的多种功能，支持高度定制以及二次开发。

### 版本

* 发布 pldroid-shortvideo-3.3.0.jar
* 更新上传库依赖版本到 v8.3.2

### 功能

* 转码支持时间段裁剪
* 重构了草稿箱功能

### 缺陷

* 修复软编时可能出现的空指针异常
* 修复个别机型合拍录制中出现的回删异常问题
* 修复预览与编辑之间产生的色差问题
* 修复个别机型二次录屏时会失败的问题
* 修复编辑个别视频出现闪退的问题
* 修复编辑界面预览混音时长与实际不符的问题
* 修复转码时的视频裁剪不生效的问题
* 修复 GIF 图位置偏移的问题

### 注意事项

* 从 v3.1.0 版本开始，需要在 Application 中初始化 sdk：

```java
PLShortVideoEnv.init(getApplicationContext());
```

* 七牛短视频 SDK 自 v3.0.0 版本起, 划分为精简版、基础版、进阶版、专业版。不同版本 SDK 可以使用的功能点数量有差别，请按照购买的 License 版本使用对应的短视频 SDK 版本。
* 上传 SDK 的依赖需要更新到如下版本：

```java
compile 'com.qiniu:qiniu-android-sdk:8.3.2'
```
