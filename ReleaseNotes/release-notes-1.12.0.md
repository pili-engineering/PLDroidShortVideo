# PLDroidShortVideo Release Notes for 1.12.0

### 简介

PLDroidShortVideo 是七牛推出的一款适用于 Android 平台的短视频 SDK，提供了包括美颜、滤镜、水印、断点录制、分段回删、视频编辑、混音特效、本地/云端存储在内的多种功能，支持高度定制以及二次开发。

### 版本

* 发布 pldroid-shortvideo-1.12.0.jar

### 功能

* 新增短视频的分段变速录制功能(硬件编码)

### 缺陷

* 修复从草稿箱导入后回删视频片段导致崩溃的问题

### 注意事项

上传 SDK 的依赖需要更新到如下版本：

```java
compile 'com.qiniu:qiniu-android-sdk:7.3.11'
```