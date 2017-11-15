# PLDroidShortVideo Release Notes for 1.7.0

### 简介
PLDroidShortVideo 是七牛推出的一款适用于 Android 平台的短视频 SDK，提供了包括美颜、滤镜、水印、断点录制、分段回删、视频编辑、混音特效、本地/云端存储在内的多种功能，支持高度定制以及二次开发。

### 版本
* 发布 pldroid-shortvideo-1.7.0.jar
* 更新 libpldroid\_shortvideo_core.so
* 更新 libpldroid_amix.so

### 功能
* 新增 AR 拍摄功能
* 新增纯音频拍摄功能
* 新增时光倒流功能
* 新增视频剪辑中的快速剪辑模式
* 新增视频编辑时暂停播放/恢复播放的接口
* 新增从 SD 卡获取滤镜资源的接口
* 新增上传自定义变量的配置接口
* 新增录制时添加背景音乐的接口
* 新增视频转码中的视频旋转的接口

### 缺陷
- 修复部分机型（荣耀6）转码后画面拉伸问题
- 修复视频录制时在 onReady 回调中开始录制产生的崩溃问题
- 修复部分机型（三星GALAXY A7）编辑视频时崩溃问题