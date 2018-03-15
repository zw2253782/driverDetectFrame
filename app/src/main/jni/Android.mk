LOCAL_PATH := $(call my-dir)

include $(CLEAR_VARS)

#opencv
OPENCVROOT:= /home/wei/Desktop/OpenCV-android-sdk
OPENCV_CAMERA_MODULES:=on
OPENCV_INSTALL_MODULES:=on
OPENCV_LIB_TYPE:=SHARED
include ${OPENCVROOT}/sdk/native/jni/OpenCV.mk

LOCAL_SRC_FILES := lane_marker_detector.cpp api_NativeClassAPI.cpp fec.cpp
LOCAL_LDLIBS += -llog
LOCAL_MODULE := MyOpencvLibs

include $(BUILD_SHARED_LIBRARY)
