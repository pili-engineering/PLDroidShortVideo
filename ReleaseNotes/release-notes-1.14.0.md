# PLDroidShortVideo Release Notes for 1.14.0

### 简介

PLDroidShortVideo 是七牛推出的一款适用于 Android 平台的短视频 SDK，提供了包括美颜、滤镜、水印、断点录制、分段回删、视频编辑、混音特效、本地/云端存储在内的多种功能，支持高度定制以及二次开发。

### 版本

* 发布 pldroid-shortvideo-1.14.0.jar

### 功能

* 新增短视频拍摄时实时返回输出视频时长接口
* 变速录制时添加背景音乐，背景音乐不变速

### 优化

* Demo UI 界面优化，增加实时录制进度显示窗和横屏录制时添加背景音乐功能

### 注意事项

上传 SDK 的依赖需要更新到如下版本：

```java
compile 'com.qiniu:qiniu-android-sdk:7.3.11'
```