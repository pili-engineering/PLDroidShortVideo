# PLDroidShortVideo Release Notes for 3.2.1

### 简介

PLDroidShortVideo 是七牛推出的一款适用于 Android 平台的短视频 SDK，提供了包括美颜、滤镜、水印、断点录制、分段回删、视频编辑、混音特效、本地/云端存储在内的多种功能，支持高度定制以及二次开发。

### 版本

* 发布 pldroid-shortvideo-3.2.1.jar

### 功能

* 视频编码导出新增内容填充模式设置
* 转码场景新增码率模式的配置

### 缺陷

* 修复慢速录制场景下预览卡住的问题
* 修复个别机型转码某些资源无画面的问题
* 修复个别机型合成个别视频异常的问题
* 修复某些机型上软编出现的 OOM 问题
* 修复了个别场景下的崩溃问题
* 修复合拍 deleteAllSection 后素材进度异常的问题

### 注意事项

* 从 v3.1.0 版本开始，需要在 Application 中初始化 sdk：

```java
PLShortVideoEnv.init(getApplicationContext());
```

* 七牛短视频 SDK 自 v3.0.0 版本起, 划分为精简版、基础版、进阶版、专业版。不同版本 SDK 可以使用的功能点数量有差别，请按照购买的 License 版本使用对应的短视频 SDK 版本。
* 上传 SDK 的依赖需要更新到如下版本：

```java
compile 'com.qiniu:qiniu-android-sdk:7.6.4'
```


