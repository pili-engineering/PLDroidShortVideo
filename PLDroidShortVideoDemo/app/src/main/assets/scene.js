/**
 * @method serialize
 * @param value {any}
 * @returns {string}
 */
var serialize = function (value) { return JSON.stringify(value); };
/**
 * @method deserialize
 * @param str {string}
 * @returns {any}
 */
var deserialize = function (str) { return JSON.parse(str); };


var cm = easyar.ComponentManager.getInstance();
var s = easyar.Scene.getInstance();

var arcamera = easyar.ARCameraPrefab.createOnObject(s.rootObject());
var cam_matrix = easyar.Matrix44F.lookAt({ x: 0, y: 1, z: 5 }, { x: 0, y: 1, z: 0 }, { x: 0, y: 1, z: 0 });
arcamera.transform.matrix = cam_matrix;
//arcamera.setCenterMode("augmenter");

var com_cameraDevice;
var com_augmenter;
arcamera.components().forEach(function (i) {
    if (i.kind == "cameraDevice") {
        com_cameraDevice = i;
    }
    if (i.kind == "augmenter") {
        com_augmenter = i;
    }
});
//com_cameraDevice.deviceType = "front";
//com_cameraDevice.horizontalFlip = true;

arcamera.open();
//barCodeScaner

var barCodeScaner = easyar.BarCodeScannerPrefab.createOnObject(s.rootObject());
barCodeScaner.bindCamera(arcamera);
var com_barCodeScaner;
barCodeScaner.components().forEach(function(com){
    if(com.kind == "barCodeScanner"){
        com_barCodeScaner = com;
    }
});
com_barCodeScaner.isRunning = false;
com_augmenter.addTextMessageListener(function(aug, text){
   JsNative.sendJSMessage("barCode",text);
});
arcamera.children().forEach(function (o) {
    o.components().forEach(function (i) {
        if (i.kind == "camera") {
            s.setMainCamera(i);
            obj_camera = o;
            com_camera = i;
        }
    });
});

console.log("loadSCeneSuccess");
var imagetracker = easyar.ImageTrackerPrefab.createOnObject(s.rootObject());
imagetracker.bindCamera(arcamera);

var com_imagetracker;
imagetracker.components().forEach(function (i) {
    if (i.kind == "imageTracker") {
        com_imagetracker = i;
    }
});

// choose which tracker to use here after you call preLoadTarget from native interface,
// native callbacks will be triggered afterwards.
s.setPreLoadListener(function (targetDesc) {
    console.log("load target from scene: " + targetDesc);
    imagetracker.preLoadTarget(targetDesc);
});

s.setManifestTargetLoadListener(function (target) {
    target.bindTracker(imagetracker);
});

var targets = [];
var currentTarget = {};
com_imagetracker.addTargetLoadListener(function (tracker, target, status) {
    target.hideOnLost = false;
    var IsContains = false;
    targets.forEach(function (i) {
        if (i != currentTarget) {
            i.object().children().forEach(function (i) {
                i.active = false;
            });
        } else {
            IsContains = true;
        }
    });
    if (IsContains) {
        targets.push(target);
    }
    console.log("global load (" + status + "): " + target.id + " (" + target.targetName + ")");
});
com_imagetracker.addTargetUnloadListener(function (tracker, target, status) {
    console.log("global unload (" + status + "): " + target.id + " (" + target.targetName + ")");
});

com_augmenter.addTargetFoundListener(function (augmenter, target) {
                                     
    currentTarget = target;
    JsNative.targetFound(target.uid);
    num = 0;
    sceneManager.imuTarget["imu"].disableIMU();
    sceneManager.imuTarget["useSingleMove"] = false;
    sceneManager.imuTarget["usePinch"] = false;
    sceneManager.imuTarget["useDoubleMove"] = false;
    sceneManager.imuTarget["useReset"] = false;
    dummyTarget.transform.matrix = easyar.Matrix44F.TRS({ x: 0, y: 0, z: 0 }, { x: 0, y: 0, z: 0 }, { x: 1, y: 1, z: 1 });
    sceneManager.imuTarget["IMULerp"].setResetTransform(dummyTarget.transform);
    console.log("global found111——————: " + target.id + " (" + target.targetName + ")");
});
com_augmenter.addTargetLostListener(function (augmenter, target) {
//    com_imagetracker.isRunning = false;
    JsNative.targetLost(target.uid);
    sceneManager.imuTarget["imu"].enableIMU();
    sceneManager.imuTarget["useSingleMove"] = true;
    sceneManager.imuTarget["usePinch"] = true;
    sceneManager.imuTarget["useDoubleMove"] = true;
    sceneManager.imuTarget["useReset"] = true;
    console.log("global lost111——————: " + target.id + " (" + target.targetName + ")");
});

com_camera.removeRenderer();
var customPipeline = cm.createComponentOnObject("customPipeline", obj_camera, {});
customPipeline.load("local://Recorder.json");
/**
  * @param sendMessage {(name:string, params:string[]) => void}
  * @param pushResponseHandler {(name:string, resultReceiver:string => void) => void}
  */
var JsNativeBinding = function (sendMessage, pushResponseHandler) {
    return {
        openWebView: function (url) {
            if (url == undefined) {
                url = "";
            }
            sendMessage("request:JsNativeBinding.openWebView", [url]);
        },
        getMetaData: function (meta) {
            sendMessage("request:JsNativeBinding.getMetaData", [meta]);
        },
        getCameraDeviceType: function (type) {
            sendMessage("response:JsNativeBinding.getCameraDeviceType", [type.toString()]);
        },
        targetLost: function (type) {
            sendMessage("request:JsNativeBinding.targetLost", [type]);
        },
        targetFound: function (type) {
            sendMessage("request:JsNativeBinding.targetFound", [type]);
        },
        openView: function (type) {
            sendMessage("request:JsNativeBinding.openView", [type]);
        },
        takePhoto: function (type) {
            sendMessage("request:JsNativeBinding.takePhoto", [type]);
        },
        showNativeButton: function (type) {
            sendMessage("request:JsNativeBinding.showNativeButton", [type]);
        },
        closeNativeButton: function (type) {
            sendMessage("request:JsNativeBinding.closeNativeButton", [type]);
        },
        SendCardName: function (type) {
            sendMessage("request:JsNativeBinding.SendCardName", [type]);
        },
        FinishLoading:function(type){
            sendMessage("request:JsNativeBinding.FinishLoading", [type]);
        },
        sendJSMessage: function (key, value) {
            sendMessage("request:JsNativeBinding.sendJSMessage", [key, value]);
        },
        sendMarkMessage:function(type){
            sendMessage("request:JsNativeBinding.sendMarkMessage", [type]);
        },
        openGesture:function(type){
            sendMessage("request:JsNativeBinding.openGesture", [type]);
        },
    };
};

var NativeJsBinding = function () {
    var functions = {};
    functions["request:NativeJsBinding.resetContent"] = function (receiver, params, sendMessage) {
        receiver.resetContent();
    };
    functions["request:NativeJsBinding.barCodeOpen"] = function (receiver, params, sendMessage) {
        receiver.barCodeOpen(params[0]);
    };
    functions["request:NativeJsBinding.getCameraDeviceType"] = function (receiver, params, sendMessage) {
        receiver.getCameraDeviceType();
    };
    functions["request:NativeJsBinding.changeCameraDeviceType"] = function (receiver, params, sendMessage) {
        receiver.changeCameraDeviceType();
    };
    functions["request:NativeJsBinding.hideAR"] = function (receiver, params, sendMessage) {
        receiver.hideAR();
    };
    functions["request:NativeJsBinding.showARWithTrack"] = function (receiver, params, sendMessage) {
        receiver.showARWithTrack();
    };
    functions["request:NativeJsBinding.showARWithIMU"] = function (receiver, params, sendMessage) {
        receiver.showARWithIMU();
    };
    functions["request:NativeJsBinding.StartScan"] = function (receiver, params, sendMessage)
    {
        receiver.StartScan(params[0]);
    };
	functions["request:NativeJsBinding.hengshu"] = function (receiver, params, sendMessage)
    {
        receiver.hengshu(params[0]);
    };
	functions["request:NativeJsBinding.nextParticle"] = function (receiver, params, sendMessage)
    {
        receiver.nextParticle(params[0]);
    };
    return {
        /**
         * @param name {string}
         * @returns {(receiver:INativeJsBindingReceiver, params:string[]) => string}
         */
        tryGetFunction: function (name) {
            if (name in functions) {
                return functions[name];
            } else {
                return null;
            }
        }
    };
};


var responseRegistry = {}
var JsNative = new JsNativeBinding(easyar.Rpc.sendMessage, function (name, resultReceiver) {
    var queue = responseRegistry[name];
    if (queue === undefined) {
        queue = new Array();
        responseRegistry[name] = queue;
    }
    queue.push(resultReceiver);
});

var nativeReceiver = {
    resetContent: function () {
        console.log("[Js] request:NativeJsBinding.resetContent : default");
    },
    getCameraDeviceType: function () {
        var cameraType = com_cameraDevice.deviceType;
        JsNative.getCameraDeviceType(cameraType.toString());
        console.log("[Js] request:NativeJsBinding.getCameraDeviceType : default");
    },
    changeCameraDeviceType: function () {
        if (com_cameraDevice.deviceType == "normal" || com_cameraDevice.deviceType == "back") {
            arcamera.close();
            com_cameraDevice.deviceType = "front";
            arcamera.open();
        } else if (com_cameraDevice.deviceType == "front") {
            arcamera.close();
            com_cameraDevice.deviceType = "back";
            arcamera.open();

        }
        sceneManager.imuTarget["usefornt"] = !sceneManager.imuTarget["usefornt"];
        sceneManager.imuTarget["imu"].enableIMU();
        console.log("[Js] request:NativeJsBinding.changeCameraDeviceType : default");
    },
    hideAR: function () {
        sceneState.hideAR();
        console.log("[Js] request:NativeJsBinding.changeToLost : default");
    },
    showARWithIMU: function () {
        sceneState.showARWithIMU();
        console.log("[Js] request:NativeJsBinding.showARWithIMU : default");
    },
    showARWithTrack: function () {
        sceneState.showARWithTrack();
        console.log("[Js] request:NativeJsBinding.showARWithTrack : default");
    },
    getJSMessage: function (key, value) {
        console.log("[Js] response:NativeJsBinding.getJSMessage : default " + key + ": " + value);
    },
    GestureEnd: function (type) {
        console.log("[Js] request:NativeJsBinding.GestureEnd : default " + type);
    },
    hengshu: function (type) {
        console.log("[Js] request:NativeJsBinding.GestureEnd : hengshu " + type);
    },
	nextParticle: function (type) {
        console.log("[Js] request:NativeJsBinding.GestureEnd : nextParticle " + type);
    },
    barCodeOpen:function(value){
    		if(value == "true"){
    			com_barCodeScaner.isRunning = true;
    		}else{
    			com_barCodeScaner.isRunning = false;
    		}
    },
	StartScan: function (type) {
		if(type == "false") {
			type = false;
		}
		else
		{
			type = true;
		}
		com_imagetracker.isRunning = type;
		console.log("[Js] request:NativeJsBinding.StartScan : default " + type);
	}
};
com_imagetracker.isRunning = true;
var cloud = easyar.CloudRecognizerPrefab.createOnObject(s.rootObject());
//cloud.isRunning = true;
var com_cloud;
var dnum = 0;
    cloud.components().forEach(function(i) {
                               if(i.kind == "cloudRecognizer"){
                               com_cloud = i;
                               }
                               });

//if(dnum == 0)
//{
    com_cloud.addCloudUpdateListener(function(cloud, status, target_str) {
                                     //                                console.log("cloud: " + status);
                                     if (target_str != "") {
                                     console.log("cloud target: " + target_str);
                                     //dnum = 1;
                                     JsNative.getMetaData(target_str);
//                                     cloud.isRunning = false;
                                     }
                                     });
//}


    cloud.bindCamera(arcamera);


var NativeJs = new NativeJsBinding();
easyar.Rpc.setMessageReceiver(function (name, params) {
    var f = NativeJs.tryGetFunction(name);
    if (f != null) {
        f(nativeReceiver, params, easyar.Rpc.sendMessage);
    } else if (name in responseRegistry) {
        var queue = responseRegistry[name];
        var f = queue.shift();
        if (queue.length == 0) {
            delete responseRegistry[name];
        }
        f(params);
    } else {
        throw new Error("InvalidMethodName: " + name);
    }
});

var sceneState = {
    status: "lost", recentTarget: {},//tracking lost IMU
    hideAR: function () {
        this.status = "lost";
        console.log("ssssssssssssssssssssssssssssssssssssss");
        //com_imagetracker.isRunning = true;
        if (this.recentTarget.object == undefined) {
            return;
        }
console.log("ssssssssssssssssssssssssssssssssssssss");
        this.recentTarget.object().children().forEach(function (i) {
            i.active = false;
            
        });
        console.log("ssssssssssssssssssssssssssssssssssssss");
    },
    showARWithTrack: function () {
        this.status = "tracking";
        if (currentTarget.object == undefined) {
            return;
        }

        currentTarget.object().children().forEach(function (i) {
            i.active = true;
        });
        console.log("found_________________________________");

    },
    showARWithIMU: function () {
        this.status = "IMU";
        if (currentTarget.object == undefined) {
        
            return;
        }
        this.recentTarget = currentTarget;
        console.log("lost_________________________________");

    }
};

//imu
function v_m(v, m) {
    var _v = { x: v.x, y: v.y, z: v.z };
    var _m = m.raw();
    _v.x = v.x * _m[0] + v.y * _m[4] + v.z * _m[8];
    _v.y = v.x * _m[1] + v.y * _m[5] + v.z * _m[9];
    _v.z = v.x * _m[2] + v.y * _m[6] + v.z * _m[10];
    return _v;
}



var vec_d_distance = function (v1, v2) {
    var _v = { x: 0, y: 0, z: 0 };
    _v.x = v1.x - v2.x;
    _v.y = v1.y - v2.y;
    _v.z = v1.z - v2.z;

    var distance = Math.sqrt(_v.x * _v.x + _v.y * _v.y + _v.z * _v.z);
    return distance;
}

function dot(v1, v2) {
    return v1.x * v2.x + v1.y * v2.y + v1.z * v2.z;
}

function normalize(v) {
    var len = Math.sqrt(dot(v, v));
    return { x: v.x / len, y: v.y / len, z: v.z / len };
}

function cross(v1, v2) {
    return { x: v1.y * v2.z - v2.y * v1.z, y: v1.z * v2.x - v1.x * v2.z, z: v1.x * v2.y - v1.y * v2.x };
}

function multiply(v, num) {
    return { x: v.x * num, y: v.y * num, z: v.z * num };
}

function add(v1, v2) {
    return { x: v1.x + v2.x, y: v1.y + v2.y, z: v1.z + v2.z };
}

var dummyTarget = s.rootObject().createChild();
/*
var getShowInCameraPos = function (obj, d) {
    var v = { x: 0, y: 0, z: -d };
    v = v_m(v, arcamera.transform.matrix);
    var cam_pos = arcamera.transform.position;
    var pos = add(cam_pos, v);
    var cam_project = { x: cam_pos.x, y: 0, z: cam_pos.z };
    var obj_project = { x: pos.x, y: 0, z: pos.z };
    if (cam_project.x != obj_project.x && cam_project.z != obj_project.z) {
        var matrix = easyar.Matrix44F.lookAt(cam_project, obj_project, { x: 0, y: 1, z: 0 });
        dummyTarget.transform.matrix = matrix;
    }
    dummyTarget.transform.position = pos;
    dummyTarget.transform.scale = { x: 1, y: 1, z: 1 };
	if(com_cameraDevice.deviceType == "front"){
		// var r = dummyTarget.transform.rotationXYZ;
		// r = { x:r.x+180*3.14/180 , y:r.y , z:r.z+90*3.14/180 };
		// dummyTarget.transform.rotationXYZ = r;
		// console.log("zzzzzzzzDebug->rotationXYZ:"+r.x+","+r.y+","+r.z);

		var r = { x:180*3.14/180 , y:0 , z:0 };
		dummyTarget.transform.worldRotateXYZ(r);
	}
    return dummyTarget.transform;
};*/

//var getShowInCameraPos = function (obj, d) {
//    var v = { x: 0, y: 0, z: -d };
//    v = v_m(v, arcamera.transform.matrix);
//    var cam_pos = arcamera.transform.position;
//    var pos = add(cam_pos, v);
//    var dir = easyar.Vector3F.substract(arcamera.transform.position,pos);
//    var rotation = easyar.Transform.createRotationByAxis({x:0, y:1, z:0}, {x:0, y:0, z:1}, dir);
//    var angle = rotation.rotationXYZ.y;
//    if(rotation.rotationXYZ.x != 0 && rotation.rotationXYZ.z != 0){
//        angle = 3.1415926-angle;
//    }
//    return {position:pos,rotationXYZ:{x:0,y:angle,z:0},scale:{ x: 1, y: 1, z: 1}};
//};
var getShowInCameraPos = function (obj, d) {
    var v = { x: 0, y: 0, z: -d };
    v = v_m(v, arcamera.transform.matrix);
    var cam_pos = arcamera.transform.position;
    var pos = add(cam_pos, v);
    var dir = easyar.Vector3F.substract(arcamera.transform.position,pos);
    //new version line
    var rotation = easyar.Transform.createRotationByAxis({x:0, y:1, z:0}, {x:0, y:0, z:1}, dir);
    //end new version line
    return { position: pos, rotation: rotation.rotation, scale: { x: 1, y: 1, z: 1 } };
};

var vec_d_distance = function (v1, v2) {
    var _v = { x: 0, y: 0, z: 0 };
    _v.x = v1.x - v2.x;
    _v.y = v1.y - v2.y;
    _v.z = v1.z - v2.z;

    var distance = Math.sqrt(_v.x * _v.x + _v.y * _v.y + _v.z * _v.z);
    return distance;
};

var v_lerp = function(v1,v2,f){
    var _v = { x: 0, y: 0, z: 0 };
    if(Math.abs(v2.x - v1.x) >= 0.005){
        _v.x = (v2.x - v1.x) * f;
    }
    if(Math.abs(v2.y - v1.y) >= 0.005){
        _v.y = (v2.y - v1.y) * f;
    }
    if(Math.abs(v2.z - v1.z)>=0.005){
        _v.z = (v2.z - v1.z) * f;
    }
    return _v;
};

function setTouchXYWithGroy(_x, _y) {
    var target_cam = arcamera;
    var t = target_cam.transform;

    var cam_r = { x: 1, y: 0, z: 0 };
    var cam_up = { x: 0, y: 1, z: 0 };
    cam_r = v_m(cam_r, t.matrix);
    cam_up = v_m(cam_up, t.matrix);

    var world_up = { x: 0, y: 1, z: 0 };

    var dot_value_r = dot(cam_r, world_up);
    var dot_value_up = dot(cam_up, world_up);
    var right;
    var right_value;
    var forward_value;
    var orientation;

    if (Math.abs(dot_value_r) > Math.abs(dot_value_up)) {

        if (dot_value_r < 0) {
            orientation = "LandspaceRight";
        } else {
            orientation = "LandspaceLeft";
        }
        right_value = -_y;
        forward_value = -_x;
        right = normalize({ x: cam_up.x, y: 0, z: cam_up.z });
    }
    else {

        if (dot_value_up < 0) {
            orientation = "InPortrait";
        } else {
            orientation = "Portrait";
        }
        right = normalize({ x: cam_r.x, y: 0, z: cam_r.z });
        right_value = _x;
        forward_value = -_y;
    }
    return { x: right_value, y: forward_value, _right: right, _orientation: orientation };
}



var sceneManager = {
    imuTarget : {},Touch:{
        tapListeners:[],addTapListener:function(_function){
            this.tapListeners.push(_function);
        },singleMoveListeners:[],addSingleMoveListener:function(_function){
            this.singleMoveListeners.push(_function);
        },pinchListeners:[],addPinchListener:function(_function){
            this.pinchListeners.push(_function);
        },doubleMoveListeners:[],addDoubleMove:function(_function){
            this.doubleMoveListeners.push(_function);
        }
    },
    setImuTarget:function(obj){
        if(obj.transform == undefined){
            return;
        }
        obj.reparent(this.imuTarget);
        obj.transform.position = {x:0,y:0,z:0};
        sceneManager.imuTarget["imu"].enableIMU();
    },
    removeImuTarget:function(obj){
        obj.reparent(s.rootObject());
         obj.transform.position = {x:0,y:0,z:0};
        this.imuTarget["imu"].disableIMU();
    }
};
sceneManager.imuTarget = s.rootObject().createChild();

sceneManager.imuTarget["imu"] = cm.createComponentOnObject("imu", sceneManager.imuTarget, {});
sceneManager.imuTarget["useSingleMove"] = true;
sceneManager.imuTarget["usePinch"] = true;
sceneManager.imuTarget["useDoubleMove"] = true;
sceneManager.imuTarget["usefornt"] = false;
sceneManager.imuTarget["useReset"] = false;
var d = vec_d_distance(arcamera.transform.position, sceneManager.imuTarget.transform.position);
sceneManager.imuTarget["singleMoveListener"] = function(_x,_y){
//	if(com_cameraDevice.deviceType == "front"){
//		 _x *= -1;
//		_y *= -1;
//	}
    if(!sceneManager.imuTarget["useSingleMove"]){
        return;
    }

     d = vec_d_distance(arcamera.transform.position, sceneManager.imuTarget.transform.position);

    var translation = setTouchXYWithGroy(_x, _y);


    if (sceneManager.imuTarget["usefornt"] === true) {
        if (translation._orientation == "InPortrait" || translation._orientation == "Portrait") {
            translation.x = -translation.x;
        } else {
            translation.y = -translation.y;
        }
    }

    var cam_right = translation._right;
    var cam_forward = cross({ x: 0, y: 1, z: 0 }, translation._right);
    if (sceneManager.imuTarget["usefornt"] === true) {
        if (translation._orientation == "InPortrait" || translation._orientation == "LandspaceLeft") {
            translation.y = translation.y;
        }
        translation = add(multiply(cam_right, translation.x / 1000 * d), multiply({ x: 0, y: 1, z: 0 }, translation.y / 1000 * d));
    } else {
        translation = add(multiply(cam_right, translation.x / 1000 * d), multiply(cam_forward, translation.y / 1000 * d));
    }
    sceneManager.imuTarget.transform.position = add(sceneManager.imuTarget.transform.position, translation);
    sceneManager.imuTarget["IMULerp"].setResetTransform(sceneManager.imuTarget.transform);
};

sceneManager.imuTarget["doubleMove"] = function(_x,_y){
//	if(com_cameraDevice.deviceType == "front"){
////		_x *= -1;
//		// _y *= -1;
//	}
    if(!sceneManager.imuTarget["useDoubleMove"]){
        return;
    }
    d = vec_d_distance(arcamera.transform.position, sceneManager.imuTarget.transform.position);
    var translation = setTouchXYWithGroy(_x, _y);
    if (sceneManager.imuTarget["usefornt"] === true) {
        switch (translation._orientation) {
            case "Portrait":
                translation.x = -translation.x;
                break;
            case "InPortrait":
                translation.y = -translation.y;
                break;
            case "LandspaceRight":
                translation.y = -translation.y;
                break;
            case "LandspaceLeft":
                translation.x = -translation.x;
                break;
        }
    }
        if(Math.abs(sceneManager.imuTarget.transform.rotationXYZ.y) >= 3.14*2){
            var i = Math.abs(sceneManager.imuTarget.transform.rotationXYZ.y)/sceneManager.imuTarget.transform.rotationXYZ.y;
            sceneManager.imuTarget.transform.rotationXYZ.y = i*(Math.abs(sceneManager.imuTarget.transform.rotationXYZ.y)- 3.14*2);
        }
        var angle = translation.x * 3.14 / 360;
         if(com_cameraDevice.deviceType == "front"){
//            if(translation._orientation =="LandspaceLeft" ||translation._orientation =="InPortrait" ){
//                angle = -angle;
//            }
          }
          else
         {
                if(translation._orientation =="LandspaceLeft" ||translation._orientation =="InPortrait" ){
                       angle = -angle;
                 }
          }
        sceneManager.imuTarget.transform.rotateXYZ({x:0,y:angle,z:0});
        //new version line
        sceneManager.imuTarget["IMULerp"].resetRotation = sceneManager.imuTarget.transform.rotation;


};

//sceneManager.imuTarget["pinchListener"] = function(_z){
//    if(!sceneManager.imuTarget["usePinch"]){
//        return;
//    }
//     d = vec_d_distance(arcamera.transform.position, sceneManager.imuTarget.transform.position);
//    _z = -_z * d / 30;
//    var v = { x: 0, y: 0, z: -_z };
//    v = v_m(v, arcamera.transform.matrix);
//    var pos = sceneManager.imuTarget.transform.position;
//    pos.x += v.x;
//    pos.y += v.y;
//    pos.z += v.z;
//    sceneManager.imuTarget.transform.position = pos;
//    sceneManager.imuTarget["IMULerp"].setResetTransform(sceneManager.imuTarget.transform);
//}

sceneManager.imuTarget["pinchListener"] = function (_z) {
    if (!sceneManager.imuTarget["usePinch"]) {
        return;
    }
	_z = -_z * d / 30;
    var v = { x: 0, y: 0, z: -_z };
    v = v_m(v, arcamera.transform.matrix);
    var pos = sceneManager.imuTarget.transform.position;
    pos.x += v.x;
    pos.y += v.y;
    pos.z += v.z;
	d = vec_d_distance(arcamera.transform.position, pos);
	if(d<0.1){
	    return;
	}
	if(d>4.5){
	    return;
	}
	sceneManager.imuTarget.transform.position = pos;
    if(d <= 4.5){
		sceneManager.imuTarget["IMULerp"].resetPos  = pos;
	}
}
sceneManager.Touch.addSingleMoveListener(sceneManager.imuTarget["singleMoveListener"]);
sceneManager.Touch.addDoubleMove(sceneManager.imuTarget["doubleMove"]);
sceneManager.Touch.addPinchListener(sceneManager.imuTarget["pinchListener"]);

s.touch.addTapListener(function (_x, _y) {
    sceneManager.Touch.tapListeners.forEach(function(i){
        i(_x,_y);
    });
});


s.touch.addSingleMoveListener(function (_x, _y) {
    sceneManager.Touch.singleMoveListeners.forEach(function(i){
        i(_x,_y);
    });
});



s.touch.addPinchListener(function (_z) {
    sceneManager.Touch.pinchListeners.forEach(function(i){
        i(_z);
    });
});

s.touch.addDoubleMoveListener(function (_x, _y) {
    sceneManager.Touch.doubleMoveListeners.forEach(function(i){
        i(_x,_y);
    });
});


var wholeflare;
var IMULerp = function (obj) {
    var lerp_whole_rotation={ x: 0, y: 0, z: 0 };
    var wholeflare_rotation={ x: 0, y: 0, z: 0 };
    var restRotationXYZ={ x: 0, y: 0, z: 0 };
    var isBool=true;
    var objTrans;
    var imuTargetTrans;
    return {
        //new version line
        isPlay:true,resetPos: { x: 0, z: 0, y: 0 }, resetRotation: { x: 0, y: 0, z: 0, w: 1 }, resetScale: { x: 1, y: 1, z: 1 }, LerpSpeed: 0.08, frame: 0, state: "start",targetEuler:{x:0,y:0,z:0},
        //end new version line
        setResetTransform: function (t) {
            this.resetPos = t.position;
            this.resetRotation = t.rotation;
            this.resetScale = t.scale;
            var eulerResetRotaion=easyar.Mathf.quat2euler(this.resetRotation);
            if(wholeflare!=null){
                if(isBool){
                    restRotationXYZ=wholeflare.transform.rotationXYZ;
                    isBool=false;
                }
                wholeflare.transform.rotationXYZ=restRotationXYZ;
                if(0<objTrans.rotationXYZ.y-eulerResetRotaion.y<=2.7){
                    //if(objTrans.rotationXYZ.y-eulerResetRotaion.y<=1.5){
                        lerp_whole_rotation={ x: 0, y: 32*Math.PI/180, z: 0 };
                }
                if(0>=objTrans.rotationXYZ.y-eulerResetRotaion.y>=-2.7){
                   // if(objTrans.rotationXYZ.y-eulerResetRotaion.y>=-1.5){
                        lerp_whole_rotation={ x: 0, y: -32*Math.PI/180, z: 0 };
                   // }
                }
            }
        },
        setResetTransform_Only: function (t) {
            this.resetPos = t.position;
            this.resetRotation = t.rotation;
            this.resetScale = t.scale;
        },
        awake: function () {
            objTrans=obj.transform;
            imuTargetTrans=sceneManager.imuTarget.transform;
        },
        update: function () {
            if(this.isPlay){
                if(this.state == "reset"){
                    this.frame++;
                    if (this.frame >= 5) {
                        this.frame = 0;
                        nativeReceiver.resetContent();
                        this.state = "playing";
                    }
                }
                var v_lerp_pos = v_lerp(objTrans.position, this.resetPos, this.LerpSpeed);
                var v_lerp_scale = v_lerp(objTrans.scale, this.resetScale, this.LerpSpeed);
                objTrans.position = add(objTrans.position, v_lerp_pos);

                //new version line
                var lerp_rotation = easyar.Quaternion.slerp(objTrans.rotation, this.resetRotation,this.LerpSpeed);
                if(lerp_rotation.x != null && lerp_rotation.y != null && lerp_rotation.z != null && lerp_rotation.w != null){
                    objTrans.rotation = lerp_rotation;
                }
                //end new version line
                if(wholeflare!=null){
                    wholeflare_rotation=v_lerp(lerp_whole_rotation, restRotationXYZ, this.LerpSpeed);
                    lerp_whole_rotation=add(lerp_whole_rotation,{ x: restRotationXYZ.x, y: wholeflare_rotation.y, z: restRotationXYZ.z });
                    if(Math.abs(wholeflare_rotation.y)>=0.0001){
                        wholeflare.transform.rotateXYZ({ x: 0, y: lerp_whole_rotation.y, z: 0 });
                    }
                }
                objTrans.scale = add(objTrans.scale, v_lerp_scale);
                d = vec_d_distance(arcamera.transform.position, imuTargetTrans.position);
            }
        },
        onEnable: function () { },
        onDisable: function () { },
        onDestroy: function () { }
    };
};
cm.registerComponent("IMULerpsadasdasdasdasdasdasdasd", function (obj) { return new IMULerp(obj) });
sceneManager.imuTarget["IMULerp"] = cm.createComponentOnObject("IMULerpsadasdasdasdasdasdasdasd", sceneManager.imuTarget, {});




nativeReceiver.resetContent = function () {

    if (sceneManager.imuTarget["useReset"]) {

        var _d;
        if (currentTarget.size != undefined) {
        _d = currentTarget.size.x*1.5;
        } else {
        _d = sceneManager.imuTarget.transform.scale.x*1.5;
        }
        var t = getShowInCameraPos(sceneManager.imuTarget, _d);
//        sceneManager.imuTarget.transform.position = t.position;
//
//        sceneManager.imuTarget.transform.rotationXYZ = t.rotationXYZ;
        sceneManager.imuTarget["IMULerp"].setResetTransform(t);
        d = vec_d_distance(arcamera.transform.position, sceneManager.imuTarget.transform.position);
    }
};

sceneManager.imuTarget["imu"].imuStartWorking(function(){
    nativeReceiver.resetContent();
});





