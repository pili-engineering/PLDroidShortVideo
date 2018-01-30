# PLDroidShortVideo Release Notes for 1.9.0

### 简介
PLDroidShortVideo 是七牛推出的一款适用于 Android 平台的短视频 SDK，提供了包括美颜、滤镜、水印、断点录制、分段回删、视频编辑、混音特效、本地/云端存储在内的多种功能，支持高度定制以及二次开发。

### 版本
* 发布 pldroid-shortvideo-1.9.0.jar

### 功能
* 新增涂鸦功能
* 新增静态贴图功能
* 新增抖音特效功能
* 新增图片合成 mp4 图片展示模式接口
* 新增编解码器不支持部分音视频格式时的错误码回调

### 缺陷
* 修复设置高倍数拍摄后反复拍摄和回删发生错误的问题
* 修复双声道录制开始后，无法停止的问题
* 修复软编暂停拍摄后，偶现没有发生回调的问题
* 修复视频拼接中，无音轨视频放在第一个会导致无声的问题
* 修复部分机型（华为 P8）对某些视频时光倒流会卡在 100% 的问题
* 修复 setAudioMixAsset(AssetFileDescriptor afd) 接口，传入带 . 符号的路径会导致部分机型崩溃的问题
* 修复软编倍速拍摄，设置慢速或者极慢，录制视频有异常的问题
* 修复 RR-OS 系统上录制画面会倒转的问题

### 注意事项
如果使用抖音特效，需要注意以下事项：

* 相关 appkey ，滤镜文件需要通过联系七牛商务来获取。
* 需要添加特效相关的 so 文件，可在 demo 中 src/main/jniLibs/armeabi-v7a 目录下找到 (分别为：libtusdk-face.so , libtusdk-image.so , libtusdk-library.so )。 如需其它架构，请联系商务获取。
* 需要添加特效相关的 jar 文件，可在 demo 中 libs 目录下找到 （分别为：TuSDKCore-2.9.0.jar , TuSDKFace-1.0.0.jar , TuSDKVideo-1.11.0.jar , universal-image-loader-1.9.4.jar ）。
* 需要添加特效相关资源，可在 demo 中 src/main/assets 中找到 ( TuSDK.bundle )。该资源需要和 appkey 相匹配，请联系商务获取。
* 具体使用方法请参看 demo 。
