# PLDroidShortVideo Release Notes for 1.16.1

### 简介

PLDroidShortVideo 是七牛推出的一款适用于 Android 平台的短视频 SDK，提供了包括美颜、滤镜、水印、断点录制、分段回删、视频编辑、混音特效、本地/云端存储在内的多种功能，支持高度定制以及二次开发。

### 版本

* 发布 pldroid-shortvideo-1.16.1.jar
* 更新 libpldroid_amix.so

### 功能

* 新增多重音频混合的功能

### 优化

* 编辑时支持精准seek（限8.0+系统）

### 缺陷

* 修复视频拼接时设置帧率不生效的问题

### 注意事项

上传 SDK 的依赖需要更新到如下版本：

```java
compile 'com.qiniu:qiniu-android-sdk:7.3.11'
```