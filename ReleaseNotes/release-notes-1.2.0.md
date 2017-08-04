# PLDroidShortVideo Release Notes for 1.2.0

### 简介
PLDroidShortVideo 是七牛推出的一款适用于 Android 平台的短视频 SDK，提供了包括美颜、滤镜、水印、断点录制、分段回删、视频编辑、混音特效、本地/云端存储在内的多种功能，支持高度定制以及二次开发。

### 版本
* 发布 pldroid-shortvideo-1.2.0.jar

### 功能
* 新增录屏存 mp4 功能
* 新增拼接、编辑、剪辑、转码的进度回调接口
* 新增拼接、编辑、剪辑、转码的取消接口

### 缺陷
* 修复 demo 层部分视频剪辑时间点计算错误问题

### 注意事项
录制核心类 `PLShortVideoRecorder` 的拼接分段方法签名由 `void concatSections(PLConcatStateListener)` 改为 `void concatSections(PLVideoSaveListener)`。

接口类 `PLConcatStateListener` 被删除。