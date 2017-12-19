# PLDroidShortVideo Release Notes for 1.8.0

### 简介

PLDroidShortVideo 是七牛推出的一款适用于 Android 平台的短视频 SDK，提供了包括美颜、滤镜、水印、断点录制、分段回删、视频编辑、混音特效、本地/云端存储在内的多种功能，支持高度定制以及二次开发。

### 版本

* 发布 pldroid-shortvideo-1.8.0.jar
* 发布 libpldroid_encoder.so

### 功能

* 新增视频拍摄软编的支持
* 新增编辑时变速功能
* 新增编辑时配音功能
* 新增图片拼接 mp4 添加背景音乐接口
* 新增对不同音频采样率视频拼接的支持
* 新增录制时添加水印的功能，其中，**水印的位置是相对于预览分辨率的**
* 新增草稿功能

### 缺陷
* 修复编辑时纯视频（无音频轨）无法混音问题
* 修复部分机型编辑、转码、时光倒流保存时崩溃问题

### 注意事项

若要使用软编功能，请先把需要的架构 so 文件拷贝到你的项目，然后通过 PLVideoEncodeSetting.setHWCodecEnabled(false) 和 PLAudioEncodeSetting.setHWCodecEnabled(false) 方法分别开启 H.264 软编与 AAC 软编。