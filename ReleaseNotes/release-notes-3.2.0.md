# PLDroidShortVideo Release Notes for 3.2.0

### 简介

PLDroidShortVideo 是七牛推出的一款适用于 Android 平台的短视频 SDK，提供了包括美颜、滤镜、水印、断点录制、分段回删、视频编辑、混音特效、本地/云端存储在内的多种功能，支持高度定制以及二次开发。

### 版本

* 发布 pldroid-shortvideo-3.2.0.jar
* 更新 libpldroid\_shortvideo_core.so

### 功能

* 图片合成新增预览及多个转场功能
* 新增屏幕录制帧率设置
* 视频拼接功能支持缩放模式设置

### 缺陷
* 修复快速剪辑后的视频文件信息异常的问题
* 修复在某些机型上会偶现混音卡住的问题
* 修复转码偶现的空指针异常的问题
* 修复某些机型极慢速录制时偶现的花屏问题

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


