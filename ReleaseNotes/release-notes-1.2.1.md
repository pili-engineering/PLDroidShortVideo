# PLDroidShortVideo Release Notes for 1.2.1

### 简介
PLDroidShortVideo 是七牛推出的一款适用于 Android 平台的短视频 SDK，提供了包括美颜、滤镜、水印、断点录制、分段回删、视频编辑、混音特效、本地/云端存储在内的多种功能，支持高度定制以及二次开发。

### 版本
* 发布 pldroid-shortvideo-1.2.1.jar
* 移除 libpldroid_decoder.so
* 更新 filters 滤镜缩略图

### 功能
* 新增读取所有视频帧功能（不仅仅是关键帧）
* 新增通过时间戳读取视频帧的接口

### 缺陷
* 修复剪辑、转码带 B 帧的视频会花屏问题
* 修复华为部分机型录制有绿边问题
* 修复导入纯视频文件无法剪辑的问题

### 注意事项
* 不再需要 libpldroid_decoder.so

* `PLShortVideoTrimmer` 中关于获取帧的方法有以下重构：

```
// 原方法 getKeyFrameCount
int getVideoFrameCount(boolean keyFrame)

// 原方法 getKeyFrame(int index)
PLVideoFrame getVideoFrameByIndex(int index, boolean keyFrame)

// 原方法 getKeyFrame(int index, int outputWidth, int outputHeight)`
PLVideoFrame getVideoFrameByIndex(int index, boolean keyFrame, int width, int height)
```