//
// Created by Administrator on 2016/3/31.
//

#include "net_wequick_example_small_app_detail_MainActivity.h"
/*
 * Class:     net_wequick_example_small_app_detail_MainActivity
 * Method:    getStringFromNative
 * Signature: ()Ljava/lang/String;
 */
JNIEXPORT jstring JNICALL Java_net_wequick_example_small_app_detail_MainActivity_getStringFromNative
  (JNIEnv * env, jobject obj){
     return (*env)->NewStringUTF(env,"-> I'm comes from to Native Function!");
  }