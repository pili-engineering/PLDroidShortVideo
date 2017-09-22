# PLDroidShortVideo Release Notes for 1.5.1

### 简介
PLDroidShortVideo 是七牛推出的一款适用于 Android 平台的短视频 SDK，提供了包括美颜、滤镜、水印、断点录制、分段回删、视频编辑、混音特效、本地/云端存储在内的多种功能，支持高度定制以及二次开发。

### 版本
- 发布 pldroid-shortvideo-1.5.1.jar

### 功能
- 新增摄像头变焦功能
- 新增 https 上传和配置上传 key 的接口
- 录制的 mp4 文件 moov 信息从尾部改到头部

### 缺陷
- 修复视频拼接 0 度视频出现画面拉伸的问题
- 修复部分机型移动混音位置导致画面抖动的问题 

### 更新注意事项

- 请更新七牛存储 SDK 到 `7.3.10`
```
compile 'com.qiniu:qiniu-android-sdk:7.3.10'
```
