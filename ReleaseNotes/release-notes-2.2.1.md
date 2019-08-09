# PLDroidShortVideo Release Notes for 2.2.1

### 简介

PLDroidShortVideo 是七牛推出的一款适用于 Android 平台的短视频 SDK，提供了包括美颜、滤镜、水印、断点录制、分段回删、视频编辑、混音特效、本地/云端存储在内的多种功能，支持高度定制以及二次开发。

### 版本

* 发布 pldroid-shortvideo-2.2.1.jar

### 功能

* 新增包名鉴权信息获取接口

### 缺陷

* 修复视频编辑模块增加循环背景音乐时崩溃的问题
* 修复图片、GIF 拼接过程中内存泄漏的问题
* 修复动态贴纸改变参数后无法删除的问题

### 注意事项
* 七牛短视频 SDK 自 v2.0.0 版本起, 划分为基础版、进阶版、专业版。不同版本 SDK 可以使用的功能点数量有差别，请按照购买的 License 版本使用对应的短视频 SDK 版本。
* 上传 SDK 的依赖需要更新到如下版本：

```java
compile 'com.qiniu:qiniu-android-sdk:7.3.11'
```