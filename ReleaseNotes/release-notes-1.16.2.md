# PLDroidShortVideo Release Notes for 1.16.2

### 简介

PLDroidShortVideo 是七牛推出的一款适用于 Android 平台的短视频 SDK，提供了包括美颜、滤镜、水印、断点录制、分段回删、视频编辑、混音特效、本地/云端存储在内的多种功能，支持高度定制以及二次开发。

### 版本

* 发布 pldroid-shortvideo-1.16.2.jar

### 功能

* 新增视频合拍功能

### 优化

* 编辑时支持预览变速（Android 6.0 以上）

### 缺陷

* 修复剪辑某些视频时可能会失败的问题
* 修复删除多重混音时预览的音效仍然存在的问题
* 修复在一些机型上视频剪辑可能出现的崩溃问题

### 注意事项

上传 SDK 的依赖需要更新到如下版本：

```java
compile 'com.qiniu:qiniu-android-sdk:7.3.11'
```