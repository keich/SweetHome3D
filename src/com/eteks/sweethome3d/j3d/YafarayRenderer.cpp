/*
 * YafarayRenderer.cpp
 *
 * Copyright (c) 2019-2022 Emmanuel PUYBARET / eTeks <info@eteks.com>. All Rights Reserved.
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */

#include "YafarayRenderer.h"
#include <core_api/params.h>
#include <core_api/environment.h>
#include <core_api/scene.h>
#include <core_api/session.h>
#include <core_api/material.h>
#include <core_api/output.h>
#include <core_api/logging.h>
#include <core_api/imagefilm.h>
#include <core_api/light.h>
#include <core_api/background.h>
#include <core_api/camera.h>
#include <core_api/integrator.h>
#include <yafraycore/monitor.h>

using namespace yafaray;

static jclass yafaraySceneClass;
static jfieldID environmentId;
static jfieldID sceneId;
static JavaVM   *javaVM;
static progressBar_t *integratorProgressBar;

/*
 * A silent progress bar
 */
class SilentProgressBar_t : public progressBar_t {
  public:
    virtual void init(int totalSteps) { }
    virtual void update(int steps=1) { }
    virtual void done() { }
    virtual void setTag(const char* text) { }
    virtual void setTag(std::string text) { }
    virtual std::string getTag() const { return ""; }
    virtual float getPercent() const { return 0; }
    virtual float getTotalSteps() const { return 0; }
};

jint JNI_OnLoad(JavaVM *vm, void *reserved) {
  javaVM = vm;
  return JNI_VERSION_1_4;
}

/**
 * Returns the Yafaray environment instance associated to a scene.
 */
renderEnvironment_t *getEnvironment(JNIEnv *jniEnv, jobject javaRenderer) {
  if (environmentId == NULL) {
    if (yafaraySceneClass == NULL) {
      yafaraySceneClass = (jclass)jniEnv->NewGlobalRef(jniEnv->GetObjectClass(javaRenderer));
    }
    environmentId = jniEnv->GetFieldID(yafaraySceneClass, "environment", "J");
  }
  return (renderEnvironment_t *)jniEnv->GetLongField(javaRenderer, environmentId);
}

/**
 * Returns the Yafaray scene instance associated to a scene.
 */
scene_t *getScene(JNIEnv *jniEnv, jobject javaRenderer) {
  if (sceneId == NULL) {
    if (yafaraySceneClass == NULL) {
      yafaraySceneClass = (jclass)jniEnv->NewGlobalRef(jniEnv->GetObjectClass(javaRenderer));
    }
    sceneId = jniEnv->GetFieldID(yafaraySceneClass, "scene", "J");
  }
  return (scene_t *)jniEnv->GetLongField(javaRenderer, sceneId);
}

/**
 * Returns a Yafaray parameters map from the Map<String, Object> object in parameter.
 */
paraMap_t getParams(JNIEnv *jniEnv, jobject params) {
  jclass mapClass = jniEnv->FindClass("java/util/Map");
  jmethodID entrySetId = jniEnv->GetMethodID(mapClass, "entrySet", "()Ljava/util/Set;");
  jclass setClass = jniEnv->FindClass("java/util/Set");
  jmethodID toArrayId = jniEnv->GetMethodID(setClass, "toArray", "()[Ljava/lang/Object;");
  jclass mapEntryClass = jniEnv->FindClass("java/util/Map$Entry");
  jmethodID getKeyId = jniEnv->GetMethodID(mapEntryClass, "getKey", "()Ljava/lang/Object;");
  jmethodID getValueId = jniEnv->GetMethodID(mapEntryClass, "getValue", "()Ljava/lang/Object;");
  jclass stringClass = jniEnv->FindClass("java/lang/String");
  jclass integerClass = jniEnv->FindClass("java/lang/Integer");
  jmethodID intValueId = jniEnv->GetMethodID(integerClass, "intValue", "()I");
  jclass longClass = jniEnv->FindClass("java/lang/Long");
  jmethodID longClassIntValueId = jniEnv->GetMethodID(longClass, "intValue", "()I");
  jclass booleanClass = jniEnv->FindClass("java/lang/Boolean");
  jmethodID booleanValueId = jniEnv->GetMethodID(booleanClass, "booleanValue", "()Z");
  jclass floatClass = jniEnv->FindClass("java/lang/Float");
  jmethodID floatValueId = jniEnv->GetMethodID(floatClass, "floatValue", "()F");
  jclass doubleClass = jniEnv->FindClass("java/lang/Double");
  jmethodID doubleClassFloatValueId = jniEnv->GetMethodID(doubleClass, "floatValue", "()F");
  jclass floatArrayClass = jniEnv->FindClass("[F");

  jobject entrySet = jniEnv->CallObjectMethod(params, entrySetId);
  jobjectArray entries = (jobjectArray)jniEnv->CallObjectMethod(entrySet, toArrayId);
  jsize entriesCount = jniEnv->GetArrayLength(entries);
  
  paraMap_t parameters;
  // Fill parameters from params Map
  for (int i = 0; i < entriesCount; i++) {
    jobject entry = jniEnv->GetObjectArrayElement(entries, i);
    jobject value = jniEnv->CallObjectMethod(entry, getValueId);
    parameter_t parameterValue;
    if (jniEnv->IsInstanceOf(value, stringClass)) {
      const char *stringValueStr = jniEnv->GetStringUTFChars((jstring)value, 0);
      parameterValue = std::string(stringValueStr);
      jniEnv->ReleaseStringUTFChars((jstring)value, stringValueStr);
    } else if (jniEnv->IsInstanceOf(value, integerClass)) {
      parameterValue = (int)jniEnv->CallIntMethod(value, intValueId);
    } else if (jniEnv->IsInstanceOf(value, longClass)) {
      parameterValue = (int)jniEnv->CallLongMethod(value, longClassIntValueId);
    } else if (jniEnv->IsInstanceOf(value, booleanClass)) {
      parameterValue = (bool)jniEnv->CallBooleanMethod(value, booleanValueId);
    } else if (jniEnv->IsInstanceOf(value, floatClass)) {
      parameterValue = jniEnv->CallFloatMethod(value, floatValueId);
    } else if (jniEnv->IsInstanceOf(value, doubleClass)) {
      parameterValue = jniEnv->CallFloatMethod(value, doubleClassFloatValueId);
    } else if (jniEnv->IsInstanceOf(value, floatArrayClass)) {
      jfloat *floatArray = jniEnv->GetFloatArrayElements((jfloatArray)value, NULL);
      jsize count = jniEnv->GetArrayLength((jfloatArray)value);
      if (count == 3) {
        // Array of 3 floats for points
        parameterValue = point3d_t(floatArray [0], floatArray [1], floatArray [2]);
      } else if (count == 4) {
        // Array of 4 floats for colors
        parameterValue = colorA_t(floatArray [0], floatArray [1], floatArray [2], floatArray [3]);
      }
      jniEnv->ReleaseFloatArrayElements((jfloatArray)value, floatArray, 0);
      if (count != 3 && count != 4) {
        continue;
      }
    } else {
      continue;
    }
    
    jstring key = (jstring)jniEnv->CallObjectMethod(entry, getKeyId);
    const char *parameterNameStr = jniEnv->GetStringUTFChars(key, 0);
    parameters [parameterNameStr] = parameterValue;
    jniEnv->ReleaseStringUTFChars(key, parameterNameStr);
  }

  return parameters;
}

/**
 * Throws an out of memory error.
 */
jint throwOutOfMemoryError(JNIEnv *jniEnv, const char *message) {
  jclass outOfMemoryClass = jniEnv->FindClass("java/lang/OutOfMemoryError");
  return jniEnv->ThrowNew(outOfMemoryClass, message);
}

/*
 * Class:     com_eteks_sweethome3d_j3d_YafarayRenderer
 * Method:    createEnvironment
 * Signature: (Ljava/lang/String;Ljava/lang/String;)J
 */
JNIEXPORT jlong JNICALL Java_com_eteks_sweethome3d_j3d_YafarayRenderer_createEnvironment(JNIEnv *jniEnv, jobject javaRenderer, jstring pluginsPath, jstring logLevel) {
  
  const char *logLevelStr = NULL;
  renderEnvironment_t *env = NULL;
  const char *pluginsPathStr = NULL;
  try {
    logLevelStr = jniEnv->GetStringUTFChars(logLevel, 0);
    yafLog.setConsoleMasterVerbosity(logLevelStr);
    jniEnv->ReleaseStringUTFChars(logLevel, logLevelStr);
    
    if (integratorProgressBar == NULL) {
      integratorProgressBar = new SilentProgressBar_t();
    }
    
    env = new renderEnvironment_t();
    pluginsPathStr = jniEnv->GetStringUTFChars(pluginsPath, 0);
    env->loadPlugins(pluginsPathStr);
    jniEnv->ReleaseStringUTFChars(pluginsPath, pluginsPathStr);
    return (jlong)env;
  } catch (const std::bad_alloc& ex) {
    if (logLevelStr != NULL) {
      jniEnv->ReleaseStringUTFChars(logLevel, logLevelStr);
    }
    if (env != NULL) {
      delete env;
    }
    if (pluginsPathStr != NULL) {
      jniEnv->ReleaseStringUTFChars(pluginsPath, pluginsPathStr);
    }
    throwOutOfMemoryError(jniEnv, "Missing memory for environment");
    return 0;
  }
}

/*
 * Class:     com_eteks_sweethome3d_j3d_YafarayRenderer
 * Method:    createMaterial
 * Signature: (Ljava/lang/String;Ljava/util/Map;Ljava/util/List;)V
 */
JNIEXPORT void JNICALL Java_com_eteks_sweethome3d_j3d_YafarayRenderer_createMaterial(JNIEnv *jniEnv, jobject javaRenderer, jstring name, jobject params, jobject eparams) {
  const char *nameStr = NULL;
  try {
    paraMap_t parameters = getParams(jniEnv, params);
    
    jclass listClass = jniEnv->FindClass("java/util/List");
    jmethodID sizeId = jniEnv->GetMethodID(listClass, "size", "()I");
    jint size = jniEnv->CallIntMethod(eparams, sizeId);
    jmethodID getId = jniEnv->GetMethodID(listClass, "get", "(I)Ljava/lang/Object;");
    
    std::list<paraMap_t> eparameters = std::list<paraMap_t>();
    for (int i = 0; i < size; i++) {
      jobject eparam = jniEnv->CallObjectMethod(eparams, getId, i);
      eparameters.push_back(getParams(jniEnv, eparam));
    }

    const char *nameStr = jniEnv->GetStringUTFChars(name, 0);
    getEnvironment(jniEnv, javaRenderer)->createMaterial(nameStr, parameters, eparameters);
    jniEnv->ReleaseStringUTFChars(name, nameStr);
  } catch (const std::bad_alloc& ex) {
    if (nameStr != NULL) {
      jniEnv->ReleaseStringUTFChars(name, nameStr);
    }
    throwOutOfMemoryError(jniEnv, "Missing memory for material");
  }
}

/*
 * Class:     com_eteks_sweethome3d_j3d_YafarayRenderer
 * Method:    createTexture
 * Signature: (Ljava/lang/String;[BLjava/util/Map;)V
 */
JNIEXPORT void JNICALL Java_com_eteks_sweethome3d_j3d_YafarayRenderer_createTexture(JNIEnv *jniEnv, jobject javaRenderer, jstring name, jbyteArray imageData, jobject params) {
  jbyte *byteArray = NULL;
  const char *nameStr = NULL;
  try {
    paraMap_t parameters = getParams(jniEnv, params);
    byteArray = jniEnv->GetByteArrayElements(imageData, NULL);
    parameters ["data"] = (unsigned char *)byteArray;
  
    nameStr = jniEnv->GetStringUTFChars(name, 0);
    texture_t* t = getEnvironment(jniEnv, javaRenderer)->createTexture(nameStr, parameters);
    jniEnv->ReleaseStringUTFChars(name, nameStr);
    jniEnv->ReleaseByteArrayElements(imageData, byteArray, 0);
  } catch (const std::bad_alloc& ex) {
    if (byteArray != NULL) {
      jniEnv->ReleaseByteArrayElements(imageData, byteArray, 0);
    }
    if (nameStr != NULL) {
      jniEnv->ReleaseStringUTFChars(name, nameStr);
    }
    throwOutOfMemoryError(jniEnv, "Missing memory for textures");
  }
}

/*
 * Class:     com_eteks_sweethome3d_j3d_YafarayRenderer
 * Method:    createLight
 * Signature: (Ljava/lang/String;Ljava/util/Map;)V
 */
JNIEXPORT void JNICALL Java_com_eteks_sweethome3d_j3d_YafarayRenderer_createLight(JNIEnv *jniEnv, jobject javaRenderer, jstring name, jobject params) {
  const char *nameStr = NULL;
  light_t *light = NULL;
  try {
    paraMap_t parameters = getParams(jniEnv, params);
    nameStr = jniEnv->GetStringUTFChars(name, 0);
    light = getEnvironment(jniEnv, javaRenderer)->createLight(nameStr, parameters);
    jniEnv->ReleaseStringUTFChars(name, nameStr);
    getScene(jniEnv, javaRenderer)->addLight(light);
  } catch (const std::bad_alloc& ex) {
    if (nameStr != NULL) {
      jniEnv->ReleaseStringUTFChars(name, nameStr);
    }
    if (light != NULL) {
      delete light;
    }
    throwOutOfMemoryError(jniEnv, "Missing memory for light");
  }
}

/*
 * Class:     com_eteks_sweethome3d_j3d_YafarayRenderer
 * Method:    deleteLight
 * Signature: (Ljava/lang/String;)Z
 */
JNIEXPORT jboolean JNICALL Java_com_eteks_sweethome3d_j3d_YafarayRenderer_deleteLight(JNIEnv *jniEnv, jobject javaRenderer, jstring name) {
  const char *nameStr = jniEnv->GetStringUTFChars(name, 0);
  light_t *light = getEnvironment(jniEnv, javaRenderer)->removeLight(nameStr);
  jniEnv->ReleaseStringUTFChars(name, nameStr);
  if (light != NULL) {
    getScene(jniEnv, javaRenderer)->removeLight(light);
    delete light;
  }
  return light != NULL;
}


/*
 * Class:     com_eteks_sweethome3d_j3d_YafarayRenderer
 * Method:    createBackground
 * Signature: (Ljava/lang/String;Ljava/util/Map;)V
 */
JNIEXPORT void JNICALL Java_com_eteks_sweethome3d_j3d_YafarayRenderer_createBackground(JNIEnv *jniEnv, jobject javaRenderer, jstring name, jobject params) {
  const char *nameStr = NULL;
  try {
    paraMap_t parameters = getParams(jniEnv, params);
    nameStr = jniEnv->GetStringUTFChars(name, 0);
    getEnvironment(jniEnv, javaRenderer)->createBackground(nameStr, parameters);
    jniEnv->ReleaseStringUTFChars(name, nameStr);
  } catch (const std::bad_alloc& ex) {
    if (nameStr != NULL) {
      jniEnv->ReleaseStringUTFChars(name, nameStr);
    }
    throwOutOfMemoryError(jniEnv, "Missing memory for background");
  }
}

/*
 * Class:     com_eteks_sweethome3d_j3d_YafarayRenderer
 * Method:    deleteBackground
 * Signature: (Ljava/lang/String;)Z
 */
JNIEXPORT jboolean JNICALL Java_com_eteks_sweethome3d_j3d_YafarayRenderer_deleteBackground(JNIEnv *jniEnv, jobject javaRenderer, jstring name) {
  const char *nameStr = jniEnv->GetStringUTFChars(name, 0);
  background_t *background = getEnvironment(jniEnv, javaRenderer)->removeBackground(nameStr);
  jniEnv->ReleaseStringUTFChars(name, nameStr);
  if (background != NULL) {
    getScene(jniEnv, javaRenderer)->setBackground(NULL);
    delete background;
  }
  return background != NULL;
}

/*
 * Class:     com_eteks_sweethome3d_j3d_YafarayRenderer
 * Method:    createCamera
 * Signature: (Ljava/lang/String;Ljava/util/Map;)V
 */
JNIEXPORT void JNICALL Java_com_eteks_sweethome3d_j3d_YafarayRenderer_createCamera(JNIEnv *jniEnv, jobject javaRenderer, jstring name, jobject params) {
  const char *nameStr = NULL;
  try {
    paraMap_t parameters = getParams(jniEnv, params);
    nameStr = jniEnv->GetStringUTFChars(name, 0);
    getEnvironment(jniEnv, javaRenderer)->createCamera(nameStr, parameters);
    jniEnv->ReleaseStringUTFChars(name, nameStr);
  } catch (const std::bad_alloc& ex) {
    if (nameStr != NULL) {
      jniEnv->ReleaseStringUTFChars(name, nameStr);
    }
    throwOutOfMemoryError(jniEnv, "Missing memory for camera");
  }
}

/*
 * Class:     com_eteks_sweethome3d_j3d_YafarayRenderer
 * Method:    deleteCamera
 * Signature: (Ljava/lang/String;)V
 */
JNIEXPORT jboolean JNICALL Java_com_eteks_sweethome3d_j3d_YafarayRenderer_deleteCamera(JNIEnv *jniEnv, jobject javaRenderer, jstring name) {
  const char *nameStr = jniEnv->GetStringUTFChars(name, 0);
  camera_t *camera = getEnvironment(jniEnv, javaRenderer)->removeCamera(nameStr);
  jniEnv->ReleaseStringUTFChars(name, nameStr);
  if (camera != NULL) {
    delete camera;
  }
  return camera != NULL;
}

/*
 * Class:     com_eteks_sweethome3d_j3d_YafarayRenderer
 * Method:    createIntegrator
 * Signature: (Ljava/lang/String;Ljava/util/Map;)V
 */
JNIEXPORT void JNICALL Java_com_eteks_sweethome3d_j3d_YafarayRenderer_createIntegrator(JNIEnv *jniEnv, jobject javaRenderer, jstring name, jobject params) {
  const char *nameStr = NULL;
  try {
    paraMap_t parameters = getParams(jniEnv, params);
    nameStr = jniEnv->GetStringUTFChars(name, 0);
    integrator_t *integrator = getEnvironment(jniEnv, javaRenderer)->createIntegrator(nameStr, parameters);
    integrator->setProgressBar(integratorProgressBar);
    jniEnv->ReleaseStringUTFChars(name, nameStr);
  } catch (const std::bad_alloc& ex) {
    if (nameStr != NULL) {
      jniEnv->ReleaseStringUTFChars(name, nameStr);
    }
    throwOutOfMemoryError(jniEnv, "Missing memory for integrator");
  }
}

/*
 * Class:     com_eteks_sweethome3d_j3d_YafarayRenderer
 * Method:    deleteIntegrator
 * Signature: (Ljava/lang/String;)Z
 */
JNIEXPORT jboolean JNICALL Java_com_eteks_sweethome3d_j3d_YafarayRenderer_deleteIntegrator(JNIEnv *jniEnv, jobject javaRenderer, jstring name) {
  const char *nameStr = jniEnv->GetStringUTFChars(name, 0);
  integrator_t *integrator = getEnvironment(jniEnv, javaRenderer)->removeIntegrator(nameStr);
  jniEnv->ReleaseStringUTFChars(name, nameStr);
  if (integrator != NULL) {
    delete integrator;
  }
  return integrator != NULL;
}

/*
 * Class:     com_eteks_sweethome3d_j3d_YafarayRenderer
 * Method:    createScene
 * Signature: ()J
 */
JNIEXPORT jlong JNICALL Java_com_eteks_sweethome3d_j3d_YafarayRenderer_createScene(JNIEnv *jniEnv, jobject javaRenderer) {
  scene_t *scene = NULL;
  try {
    renderEnvironment_t *env = getEnvironment(jniEnv, javaRenderer);
    
    scene = new scene_t(env);
    env->setScene(scene);
    scene->setMode(0); // Triangles
    
    return (jlong)scene;
  } catch (const std::bad_alloc& ex) {
    if (scene != NULL) {
      delete scene;
    }
    throwOutOfMemoryError(jniEnv, "Missing memory for scene");
    return 0;
  }
}

/*
 * Class:     com_eteks_sweethome3d_j3d_YafarayRenderer
 * Method:    abort
 * Signature: ()V
 */
JNIEXPORT void JNICALL Java_com_eteks_sweethome3d_j3d_YafarayRenderer_abort(JNIEnv *jniEnv, jobject javaRenderer) {
  getScene(jniEnv, javaRenderer)->abort();
}

/*
 * Class:     com_eteks_sweethome3d_j3d_YafarayRenderer
 * Method:    clearAll
 * Signature: ()V
 */
JNIEXPORT void JNICALL Java_com_eteks_sweethome3d_j3d_YafarayRenderer_clearAll(JNIEnv *jniEnv, jobject javaRenderer) {
  getEnvironment(jniEnv, javaRenderer)->clearAll();
}

/*
 * Class:     com_eteks_sweethome3d_j3d_YafarayRenderer
 * Method:    startGeometry
 * Signature: ()J
 */
JNIEXPORT void JNICALL Java_com_eteks_sweethome3d_j3d_YafarayRenderer_startGeometry(JNIEnv *jniEnv, jobject javaRenderer) {
  getScene(jniEnv, javaRenderer)->startGeometry();
}

/*
 * Class:     com_eteks_sweethome3d_j3d_YafarayRenderer
 * Method:    endGeometry
 * Signature: ()V
 */
JNIEXPORT void JNICALL Java_com_eteks_sweethome3d_j3d_YafarayRenderer_endGeometry(JNIEnv *jniEnv, jobject javaRenderer) {
  getScene(jniEnv, javaRenderer)->endGeometry();
}

/*
 * Class:     com_eteks_sweethome3d_j3d_YafarayRenderer
 * Method:    startTriMesh
 * Signature: (JIIZZII)J
 */
JNIEXPORT jlong JNICALL Java_com_eteks_sweethome3d_j3d_YafarayRenderer_startTriMesh(JNIEnv *jniEnv, jobject javaRenderer, jlong id, jint vertices, jint triangles, jboolean hasOrco, jboolean hasUv, jint type, jint objectPassIndex) {
  try {
    scene_t *scene = getScene(jniEnv, javaRenderer);
    if (id == -1) {
      id = scene->getNextFreeID();
    }
    scene->startTriMesh(id, vertices, triangles, hasOrco, hasUv, type, objectPassIndex);
    return id;
  } catch (const std::bad_alloc& ex) {
    throwOutOfMemoryError(jniEnv, "Missing memory for mesh");
    return 0;
  }
}

/*
 * Class:     com_eteks_sweethome3d_j3d_YafarayRenderer
 * Method:    endTriMesh
 * Signature: ()V
 */
JNIEXPORT void JNICALL Java_com_eteks_sweethome3d_j3d_YafarayRenderer_endTriMesh(JNIEnv *jniEnv, jobject javaRenderer) {
  getScene(jniEnv, javaRenderer)->endTriMesh();
}

/*
 * Class:     com_eteks_sweethome3d_j3d_YafarayRenderer
 * Method:    addVertex
 * Signature: (FFF)V
 */
JNIEXPORT void JNICALL Java_com_eteks_sweethome3d_j3d_YafarayRenderer_addVertex(JNIEnv *jniEnv, jobject javaRenderer, jfloat x, jfloat y, jfloat z) {
  try {
    getScene(jniEnv, javaRenderer)->addVertex(point3d_t(x, y, z));
  } catch (const std::bad_alloc& ex) {
    throwOutOfMemoryError(jniEnv, "Missing memory for vertex");
  }
}

/*
 * Class:     com_eteks_sweethome3d_j3d_YafarayRenderer
 * Method:    addNormal
 * Signature: (FFF)V
 */
JNIEXPORT void JNICALL Java_com_eteks_sweethome3d_j3d_YafarayRenderer_addNormal(JNIEnv *jniEnv, jobject javaRenderer, jfloat x, jfloat y, jfloat z) {
  try {
    getScene(jniEnv, javaRenderer)->addNormal(normal_t(x, y, z));
  } catch (const std::bad_alloc& ex) {
    throwOutOfMemoryError(jniEnv, "Missing memory for normal");
  }
}

/*
 * Class:     com_eteks_sweethome3d_j3d_YafarayRenderer
 * Method:    addUV
 * Signature: (FF)V
 */
JNIEXPORT void JNICALL Java_com_eteks_sweethome3d_j3d_YafarayRenderer_addUV(JNIEnv *jniEnv, jobject javaRenderer, jfloat u, jfloat v) {
  try {
    getScene(jniEnv, javaRenderer)->addUV(u, v);
  } catch (const std::bad_alloc& ex) {
    throwOutOfMemoryError(jniEnv, "Missing memory for UV");
  }
}

/*
 * Class:     com_eteks_sweethome3d_j3d_YafarayRenderer
 * Method:    addTriangle
 * Signature: (IIIJ)V
 */
JNIEXPORT void JNICALL JNICALL Java_com_eteks_sweethome3d_j3d_YafarayRenderer_addTriangle__IIILjava_lang_String_2(JNIEnv *jniEnv, jobject javaRenderer, jint a, jint b, jint c, jstring materialName) {
  const char *materialNameStr = NULL;
  try {
    materialNameStr = jniEnv->GetStringUTFChars(materialName, 0);
    getScene(jniEnv, javaRenderer)->addTriangle(a, b, c, getEnvironment(jniEnv, javaRenderer)->getMaterial(materialNameStr));
    jniEnv->ReleaseStringUTFChars(materialName, materialNameStr);
  } catch (const std::bad_alloc& ex) {
    if (materialNameStr != NULL) {
      jniEnv->ReleaseStringUTFChars(materialName, materialNameStr);
    }
    throwOutOfMemoryError(jniEnv, "Missing memory for triangle");
  }
}

/*
 * Class:     com_eteks_sweethome3d_j3d_YafarayRenderer
 * Method:    addTriangle
 * Signature: (IIIIIIJ)V
 */
JNIEXPORT void JNICALL Java_com_eteks_sweethome3d_j3d_YafarayRenderer_addTriangle__IIIIIILjava_lang_String_2(JNIEnv *jniEnv, jobject javaRenderer, jint a, jint b, jint c, jint uva, jint uvb, jint uvc, jstring materialName) {
  const char *materialNameStr = NULL;
  try {
    materialNameStr = jniEnv->GetStringUTFChars(materialName, 0);
    getScene(jniEnv, javaRenderer)->addTriangle(a, b, c, uva, uvb, uvc, getEnvironment(jniEnv, javaRenderer)->getMaterial(materialNameStr));
    jniEnv->ReleaseStringUTFChars(materialName, materialNameStr);
  } catch (const std::bad_alloc& ex) {
    if (materialNameStr != NULL) {
      jniEnv->ReleaseStringUTFChars(materialName, materialNameStr);
    }
    throwOutOfMemoryError(jniEnv, "Missing memory for triangle");
  }
}

/*
 * Class:     com_eteks_sweethome3d_j3d_YafarayRenderer
 * Method:    addTriangles
 * Signature: ([F[F[F[I[ILjava/lang/String;)V
 */
JNIEXPORT void JNICALL Java_com_eteks_sweethome3d_j3d_YafarayRenderer_addTriangles(JNIEnv *jniEnv, jobject javaRenderer, jfloatArray vertices, jfloatArray normals, jfloatArray uvs, jintArray triangleVertices, jintArray triangleUvs, jstring materialName) {
  const char *materialNameStr = NULL;
  jfloat *verticesArray = NULL;
  jfloat *normalsArray = NULL;
  jfloat *uvsArray = NULL;
  jint *triangleVerticesArray = NULL;
  jint *triangleUvsArray = NULL;
  try {
    scene_t *scene = getScene(jniEnv, javaRenderer);
    materialNameStr = jniEnv->GetStringUTFChars(materialName, 0);
    material_t *material = getEnvironment(jniEnv, javaRenderer)->getMaterial(materialNameStr);
    jniEnv->ReleaseStringUTFChars(materialName, materialNameStr);
    
    // Utility method which calls addVertex, addNormal, addUV and addTriangle YafaRay methods in the same JNI call for better performances
    verticesArray = jniEnv->GetFloatArrayElements((jfloatArray)vertices, NULL);
    jsize   verticesCount = jniEnv->GetArrayLength((jfloatArray)vertices);
    normalsArray = normals != NULL ? jniEnv->GetFloatArrayElements((jfloatArray)normals, NULL) : NULL;
    for (int i = 0; i < verticesCount; i += 3) {
      scene->addVertex(point3d_t(verticesArray [i], verticesArray [i + 1], verticesArray [i + 2]));
      if (normalsArray != NULL)  {
        scene->addNormal(normal_t(normalsArray [i], normalsArray [i + 1], normalsArray [i + 2]));
      }
    }
    jniEnv->ReleaseFloatArrayElements(vertices, verticesArray, 0);
    verticesArray = NULL;
    if (normalsArray != NULL) {
      jniEnv->ReleaseFloatArrayElements(normals, normalsArray, 0);
      normalsArray = NULL;
    }

    if (uvs != NULL) {
      uvsArray = jniEnv->GetFloatArrayElements((jfloatArray)uvs, NULL);
      jsize   uvsCount = jniEnv->GetArrayLength((jfloatArray)uvs);
      for (int i = 0; i < uvsCount; i += 2) {
        scene->addUV(uvsArray [i], uvsArray [i + 1]);
      }
      jniEnv->ReleaseFloatArrayElements(uvs, uvsArray, 0);
      uvsArray = NULL;
    }

    triangleVerticesArray = jniEnv->GetIntArrayElements((jintArray)triangleVertices, NULL);
    jsize triangleVerticesCount = jniEnv->GetArrayLength((jintArray)triangleVertices);
    triangleUvsArray = triangleUvs != NULL ? jniEnv->GetIntArrayElements((jintArray)triangleUvs, NULL) : NULL;
    for (int i = 0; i < triangleVerticesCount; i += 3) {
      if (triangleUvsArray != NULL) {
        scene->addTriangle(triangleVerticesArray [i], triangleVerticesArray [i + 1], triangleVerticesArray [i + 2],
            triangleUvsArray [i], triangleUvsArray [i + 1], triangleUvsArray [i + 2], material);
      } else {
        scene->addTriangle(triangleVerticesArray [i], triangleVerticesArray [i + 1], triangleVerticesArray [i + 2], material);
      }
    }
    jniEnv->ReleaseIntArrayElements(triangleVertices, triangleVerticesArray, 0);
    if (triangleUvs != NULL) {
      jniEnv->ReleaseIntArrayElements(triangleUvs, triangleUvsArray, 0);
    }
  } catch (const std::bad_alloc& ex) {
    if (materialNameStr != NULL) {
      jniEnv->ReleaseStringUTFChars(materialName, materialNameStr);
    }
    if (verticesArray != NULL) {
      jniEnv->ReleaseFloatArrayElements(vertices, verticesArray, 0);
    }
    if (normalsArray != NULL) {
      jniEnv->ReleaseFloatArrayElements(normals, normalsArray, 0);
    }
    if (uvsArray != NULL) {
      jniEnv->ReleaseFloatArrayElements(uvs, uvsArray, 0);
    }
    if (triangleVerticesArray != NULL) {
      jniEnv->ReleaseIntArrayElements(triangleVertices, triangleVerticesArray, 0);
    }
    if (triangleUvs != NULL) {
      jniEnv->ReleaseIntArrayElements(triangleUvs, triangleUvsArray, 0);
    }
    throwOutOfMemoryError(jniEnv, "Missing memory for objects");
  }
}

/*
 * Class:     com_eteks_sweethome3d_j3d_YafarayRenderer
 * Method:    smoothMesh
 * Signature: (JF)V
 */
JNIEXPORT void JNICALL Java_com_eteks_sweethome3d_j3d_YafarayRenderer_smoothMesh(JNIEnv *jniEnv, jobject javaRenderer, jlong id, jfloat angle) {
  getScene(jniEnv, javaRenderer)->smoothMesh(objID_t(id), angle);
}

class imageColor_t : public colorOutput_t {
  private:
    JNIEnv *jniEnvironment;
    jobject jniImageOutput;
    jmethodID jniSetPixelId;
    jmethodID jniFlushAreaId;
    jmethodID jniFlushId;
    jmethodID jniHighlightAreaId;
  public:
    imageColor_t(JNIEnv *jniEnv, jobject imageOutput, jmethodID setPixelId, jmethodID flushAreaId, jmethodID flushId, jmethodID highlightAreaId) : jniEnvironment(jniEnv), jniSetPixelId(setPixelId), jniFlushAreaId(flushAreaId), jniFlushId(flushId), jniHighlightAreaId(highlightAreaId) {
      jniImageOutput = jniEnvironment->NewGlobalRef(imageOutput);
    }
  
    virtual bool putPixel(int numView, int x, int y, const renderPasses_t *renderPasses, int idx, const colorA_t &color, bool alpha) {
      jniEnvironment->CallVoidMethod(jniImageOutput, jniSetPixelId, x, y, color.R, color.G, color.B, color.A, alpha);
      return true;
    }
  
    virtual bool putPixel(int numView, int x, int y, const renderPasses_t *renderPasses, const std::vector<colorA_t> &colExtPasses, bool alpha) {
      jniEnvironment->CallVoidMethod(jniImageOutput, jniSetPixelId, x, y, colExtPasses.at(0).R, colExtPasses.at(0).G, colExtPasses.at(0).B, colExtPasses.at(0).A, alpha);
      return true;
    }
  
    virtual void flush(int numView, const renderPasses_t *renderPasses) {
      jniEnvironment->CallVoidMethod(jniImageOutput, jniFlushId);
    }
  
    virtual void flushArea(int numView, int x0, int y0, int x1, int y1, const renderPasses_t *renderPasses) {
      jniEnvironment->CallVoidMethod(jniImageOutput, jniFlushAreaId, x0, y0, x1, y1);
    }
  
    virtual void highliteArea(int numView, int x0, int y0, int x1, int y1) {
      // Need to attach current thread to avoid crashes
      JNIEnv *jniEnvironment = NULL;
      jint result = javaVM->AttachCurrentThread((void **)&jniEnvironment, NULL);
      jniEnvironment->CallVoidMethod(jniImageOutput, jniHighlightAreaId, x0, y0, x1, y1);
      javaVM->DetachCurrentThread();
    }
  
    virtual bool isPreview() {
      return false;
    }
  
    virtual ~imageColor_t() {
      jniEnvironment->DeleteGlobalRef(jniImageOutput);
    }
};


/*
 * Class:     com_eteks_sweethome3d_j3d_YafarayRenderer
 * Method:    render
 * Signature: (Lcom/eteks/sweethome3d/plugin/yafarayrendering/YafarayRenderer/ImageOutput;Ljava/util/Map;)V
 */
JNIEXPORT void JNICALL Java_com_eteks_sweethome3d_j3d_YafarayRenderer_render(JNIEnv *jniEnv, jobject javaRenderer, jobject imageOutput, jobject params) {
  imageColor_t *imageColorCallback = NULL;
  try {
    paraMap_t renderParamaters = getParams(jniEnv, params);
    
    scene_t *scene = getScene(jniEnv, javaRenderer);
    renderEnvironment_t *env = getEnvironment(jniEnv, javaRenderer);

    jclass imageOutputClass = jniEnv->GetObjectClass(imageOutput);
    jmethodID setPixelId = jniEnv->GetMethodID(imageOutputClass, "setPixel", "(IIFFFFZ)V");
    jmethodID flushAreaId = jniEnv->GetMethodID(imageOutputClass, "flushArea", "(IIII)V");
    jmethodID flushId = jniEnv->GetMethodID(imageOutputClass, "flush", "()V");
    jmethodID highlightAreaId = jniEnv->GetMethodID(imageOutputClass, "highlightArea", "(IIII)V");
    
    imageColorCallback = new imageColor_t(jniEnv, imageOutput, setPixelId, flushAreaId, flushId, highlightAreaId);
    env->setupScene(*scene, renderParamaters, *imageColorCallback);
    session.setInteractive(true);
    // Don't log render progress
    SilentProgressBar_t *progressBar = new SilentProgressBar_t();
    scene->getImageFilm()->setProgressBar(progressBar);
    scene->render();
    
    delete imageColorCallback;    
    delete scene->getImageFilm();
    scene->setImageFilm(NULL);
  } catch (const std::bad_alloc& ex) {
    if (imageColorCallback != NULL) {
      delete imageColorCallback;
    }
    throwOutOfMemoryError(jniEnv, "Missing memory while renderering");
  }
}

/*
 * Class:     com_eteks_sweethome3d_j3d_YafarayRenderer
 * Method:    finalize
 * Signature: ()V
 */
JNIEXPORT void JNICALL Java_com_eteks_sweethome3d_j3d_YafarayRenderer_finalize(JNIEnv *jniEnv, jobject javaRenderer) {
  scene_t *scene = getScene(jniEnv, javaRenderer);
  renderEnvironment_t *env = getEnvironment(jniEnv, javaRenderer);
  
  if (scene) {
    // Image film created by setupScene must be deleted before scene
	if (scene->getImageFilm()) {
	  delete scene->getImageFilm();
	}
    delete scene;
  }
  if (env) {
    delete env;
  }
}
