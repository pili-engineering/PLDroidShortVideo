# PLDroidShortVideo Release Notes for 3.5.0

### 简介

PLDroidShortVideo 是七牛推出的一款适用于 Android 平台的短视频 SDK，提供了包括美颜、滤镜、水印、断点录制、分段回删、视频编辑、混音特效、本地/云端存储在内的多种功能，支持高度定制以及二次开发。

### 版本

* 发布 pldroid-shortvideo-3.5.0.jar
* 发布 libpldroid_decoder.so
* 更新 libpldroid_encoder.so
* 更新 libpldroid_shortvideo_core.so
* 更新 libpldroid_amix.so

### 功能

* 支持硬编失败自动切换软编
* 支持编辑模块的镜像功能
* 支持生成带有透明度的 GIF 动图
* 支持视频帧读取功能
* 支持对视频的软解功能
* 支持拍摄时添加静态水印
* 支持自定义录制时长
* 支持静音录制功能

### 缺陷

* 修复水印位置异常的问题
* 修复多重混音场景下声音偶现异常的问题
* 修复多重混音保存卡住的问题
* 修复特定场景下偶现的空指针异常
* 修复采集曝光过暗的问题
* 修复转码模块个别视频指定帧率结果不预期的问题

### 注意事项

* 从 v3.1.0 版本开始，需要在 Application 中初始化 sdk：

```java
PLShortVideoEnv.init(getApplicationContext());
```

* 七牛短视频 SDK 自 v3.0.0 版本起, 划分为精简版、基础版、进阶版、专业版。不同版本 SDK 可以使用的功能点数量有差别，请按照购买的 License 版本使用对应的短视频 SDK 版本。
* 上传 SDK 的依赖需要更新到如下版本：

```java
compile 'com.qiniu:qiniu-android-sdk:8.4.3'
```
