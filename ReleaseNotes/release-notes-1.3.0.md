# PLDroidShortVideo Release Notes for 1.3.0

### 简介
PLDroidShortVideo 是七牛推出的一款适用于 Android 平台的短视频 SDK，提供了包括美颜、滤镜、水印、断点录制、分段回删、视频编辑、混音特效、本地/云端存储在内的多种功能，支持高度定制以及二次开发。

### 版本
* 发布 pldroid-shortvideo-1.3.0.jar

### 功能
* 新增横屏拍摄功能
* 新增制作 GIF 动图功能
* 新增指定回调 OES 类型纹理接口
* 新增录制时旋转纹理接口

### 缺陷
* 修复切换摄像头，摄像头 ID 未更新问题
* 修复 onSurfaceCreated 回调带有 glError 问题
* 修复 onPreviewFrame 回调中的角度参数与实际数据不一致问题
* 修复少数机型录制时快速点击崩溃问题
* 修复未停止录制情况下，直接拼接失败问题

### 注意事项
以下接口的详细用法请参考文档

* `PLShortVideoRecorder` 新增以下方法

	```
	public void setTextureRotation(int rotation);

	public final void setVideoFilterListener(PLVideoFilterListener listener, boolean callbackOES)
	```

* `PLShortVideoEditor` 新增以下方法

	```
	public void startPlayback(PLVideoFilterListener listener, boolean callbackOES);

	public void save(PLVideoFilterListener listener, boolean callbackOES);
	```

* `PLVideoFilterListener` 接口有以下修改

	```
	// int onDrawFrame(int texId, int texWidth, int texHeight, long timeStampNs);
	int onDrawFrame(int texId, int texWidth, int texHeight, long timeStampNs, float[] transformMatrix);
	```

* `PLCameraSetting` 方法有以下修改

	```
	// public int getCameraId()
	public CAMERA_FACING_ID getCameraId();
	```