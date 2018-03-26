# PLDroidShortVideo Release Notes for 1.10.0

### 简介
PLDroidShortVideo 是七牛推出的一款适用于 Android 平台的短视频 SDK，提供了包括美颜、滤镜、水印、断点录制、分段回删、视频编辑、混音特效、本地/云端存储在内的多种功能，支持高度定制以及二次开发。

### 版本
* 发布 pldroid-shortvideo-1.10.0.jar
* 更新 libpldroid_beauty.so

### 功能
* 新增制作过场字幕功能
* 新增视频分段功能
* PLShortAudioRecorder 和 PLShortVideoRecorder 中新增 deleteAllSections() 接口，用于删除所有录制的片段
* 支持导入 H.265 的 mp4 进行编辑处理

### 缺陷
* 修复录制不调用 PLShortVideoRecorder.setRecordSpeed 导致回调的时间错误
* 修复上传类不设置回调进度监听会发生崩溃的问题