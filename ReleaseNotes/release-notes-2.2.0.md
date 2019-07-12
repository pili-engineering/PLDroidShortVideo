# PLDroidShortVideo Release Notes for 2.2.0

### 简介

PLDroidShortVideo 是七牛推出的一款适用于 Android 平台的短视频 SDK，提供了包括美颜、滤镜、水印、断点录制、分段回删、视频编辑、混音特效、本地/云端存储在内的多种功能，支持高度定制以及二次开发。

### 版本

* 发布 pldroid-shortvideo-2.2.0.jar

### 功能

* 新增双视频拼图功能
* 新增合拍录制时，设置录制视频和样本视频的层级
* 新增合拍录制时，设置样本视频的显示模式
* 新增视频编辑时，GIF 动图的预览开关

### 缺陷

* 修复切换摄像头后, PLCameraSetting.getCameraId() 接口返回错误的问题
* 修复视频编辑时, PLShortVideoEditor.setAudioMixAsset(AssetFileDescriptor afd) 接口无效的问题
* 修复视频编辑时, 在未添加水印、滤镜的场景下旋转视频会失效的问题
* 修复无音频的视频在转码时没有进度回调的问题

### 优化

* 降低了视频剪辑，转码，编辑保存等操作的时耗

### 注意事项
* 七牛短视频 SDK 自 v2.0.0 版本起, 划分为基础版、进阶版、专业版。不同版本 SDK 可以使用的功能点数量有差别，请按照购买的 License 版本使用对应的短视频 SDK 版本。
* 上传 SDK 的依赖需要更新到如下版本：

```java
compile 'com.qiniu:qiniu-android-sdk:7.3.11'
```