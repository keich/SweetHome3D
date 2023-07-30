/*
 * YafarayRenderer.java
 *
 * Copyright (c) 2019 Emmanuel PUYBARET / eTeks <info@eteks.com>. All Rights Reserved.
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
package com.eteks.sweethome3d.j3d;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.EventQueue;
import java.awt.Graphics2D;
import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.awt.image.ImageObserver;
import java.awt.image.RenderedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InterruptedIOException;
import java.io.OutputStream;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import javax.imageio.ImageIO;
import javax.media.j3d.Appearance;
import javax.media.j3d.BoundingSphere;
import javax.media.j3d.BranchGroup;
import javax.media.j3d.ColoringAttributes;
import javax.media.j3d.Geometry;
import javax.media.j3d.GeometryArray;
import javax.media.j3d.GeometryStripArray;
import javax.media.j3d.Group;
import javax.media.j3d.ImageComponent2D;
import javax.media.j3d.IndexedGeometryArray;
import javax.media.j3d.IndexedGeometryStripArray;
import javax.media.j3d.IndexedLineArray;
import javax.media.j3d.IndexedLineStripArray;
import javax.media.j3d.IndexedQuadArray;
import javax.media.j3d.IndexedTriangleArray;
import javax.media.j3d.IndexedTriangleFanArray;
import javax.media.j3d.IndexedTriangleStripArray;
import javax.media.j3d.LineArray;
import javax.media.j3d.LineStripArray;
import javax.media.j3d.Link;
import javax.media.j3d.Material;
import javax.media.j3d.Node;
import javax.media.j3d.PolygonAttributes;
import javax.media.j3d.QuadArray;
import javax.media.j3d.RenderingAttributes;
import javax.media.j3d.Shape3D;
import javax.media.j3d.TexCoordGeneration;
import javax.media.j3d.Texture;
import javax.media.j3d.TextureAttributes;
import javax.media.j3d.Transform3D;
import javax.media.j3d.TransformGroup;
import javax.media.j3d.TransparencyAttributes;
import javax.media.j3d.TriangleArray;
import javax.media.j3d.TriangleFanArray;
import javax.media.j3d.TriangleStripArray;
import javax.vecmath.Color3f;
import javax.vecmath.Point3d;
import javax.vecmath.Point3f;
import javax.vecmath.TexCoord2f;
import javax.vecmath.Vector3f;
import javax.vecmath.Vector4f;

import org.sunflow.SunflowAPI;
import org.sunflow.core.ParameterList;
import org.sunflow.core.ParameterList.InterpolationType;
import org.sunflow.core.light.SunSkyLight;

import com.eteks.sweethome3d.model.Camera;
import com.eteks.sweethome3d.model.Compass;
import com.eteks.sweethome3d.model.Content;
import com.eteks.sweethome3d.model.Home;
import com.eteks.sweethome3d.model.HomeEnvironment;
import com.eteks.sweethome3d.model.HomeFurnitureGroup;
import com.eteks.sweethome3d.model.HomeLight;
import com.eteks.sweethome3d.model.HomePieceOfFurniture;
import com.eteks.sweethome3d.model.HomeTexture;
import com.eteks.sweethome3d.model.Level;
import com.eteks.sweethome3d.model.LightSource;
import com.eteks.sweethome3d.model.ObserverCamera;
import com.eteks.sweethome3d.model.Room;
import com.eteks.sweethome3d.model.Selectable;
import com.eteks.sweethome3d.model.Transformation;
import com.eteks.sweethome3d.model.Wall;
import com.eteks.sweethome3d.tools.OperatingSystem;
import com.eteks.sweethome3d.tools.URLContent;
import com.eteks.sweethome3d.viewcontroller.Object3DFactory;

/**
 * A renderer implemented with YafaRay rendering engine called with JNI.
 */
public class YafarayRenderer extends AbstractPhotoRenderer {
  private static String pluginsFolder;

  static {
    // To generate YafarayRenderer.h, use following command (compiled files are contained in classes folder)
    // SweetHome3D project folder> javah -jni -o src/com/eteks/sweethome3d/j3d/YafarayRenderer.h -cp classes:lib/j3dcore.jar:lib/vecmath.jar com.eteks.sweethome3d.j3d.YafarayRenderer
    //
    // Based on YafaRay 3.5.1 + other changes retrieved with the command:
    // > git clone -b v3.5.1 https://github.com/puybaret/libYafaRay.git
    //
    // To generate JNI DLL for Windows, read "YafaRay Windows 10 MinGW 64bit building - Standalone.txt" in libYafaRay-Old-Structure-3.3.0\building\win available at https://github.com/YafaRay/libYafaRay-Old-Structure/archive/refs/tags/v3.3.0.zip (even if YafaRay 3.5.1 is used)
    // Build YafaRay with current source code after setting "option(WITH_Freetype", "option(WITH_OpenEXR" "option(WITH_JPEG", "option(WITH_PNG", "option(WITH_TIFF", option(WITH_XMLImport, option(WITH_XML_LOADER, "option(WITH_OpenCV" to OFF in libYafaRay\CMakeLists.txt
    // Copy yafa-dev64\build\yafaray_v3\bin\libyafaray_v3_core.dll + yafaray-plugins generated files and required DLLs listed below found in C:\msys64\mingw64\bin
    // Run C:\mingw64\mingw64-shell.exe and use the following command (/C/Program Files/Java/jdk1.8.0_121 contains JDK)
    // SweetHome3D project folder> g++.exe -I"/C/Program Files/Java/jdk1.8.0_121/include" -I"/C/Program Files/Java/jdk1.8.0_121/include/win32" -Iinclude/yafaray -I/C/msys64/mingw64/include src/com/eteks/sweethome3d/j3d/YafarayRenderer.cpp -shared -o lib/yafaray/windows/x64/libyafarayjni.dll -Llib/yafaray/windows/x64 -llibyafaray_v3_core
    // Same instructions for i386 replacing "w64-x86_64" by "w64-i686" in dependent libraries instructions and with the following final command run in  C:\mingw64\mingw32-shell.exe:
    // SweetHome3D project folder> g++.exe -m32 -Wl,--kill-at -I"/C/Program Files/Java/jdk1.8.0_121/include" -I"/C/Program Files/Java/jdk1.8.0_121/include/win32" -Iinclude/yafaray -I/C/msys64/mingw32/include src/com/eteks/sweethome3d/j3d/YafarayRenderer.cpp -shared -o lib/yafaray/windows/i386/libyafarayjni.dll -Llib/yafaray/windows/i386 -llibyafaray_v3_core
    //
    // To generate JNI DLL for macOS, read "YafaRay Debian Testing building - Standalone.txt" in libYafaRay/building/linux, install Xcode 8.2
    // Build YafaRay with current source code after setting "option(WITH_Freetype", "option(WITH_OpenEXR" "option(WITH_JPEG", "option(WITH_PNG", "option(WITH_TIFF", option(WITH_XMLImport, option(WITH_XML_LOADER, "option(WITH_OpenCV" to OFF in libYafaRay/CMakeLists.txt
    // Copy yafa-dev/build/yafaray_v3/libyafaray_v3_core.dylib + yafaray-plugins generated files and required DLLs listed below
    // Open Terminal window and use the following command (/Library/Java/JavaVirtualMachines/jdk1.8.0_202.jdk contains JDK)
    // SweetHome3D project folder> clang++ -std=c++11 -mmacosx-version-min=10.9 -I/Library/Java/JavaVirtualMachines/jdk1.8.0_202.jdk/Contents/Home/include -I/Library/Java/JavaVirtualMachines/jdk1.8.0_202.jdk/Contents/Home/include/darwin -Iinclude/yafaray -arch x86_64 -dynamiclib -F/System/Library/Frameworks/JavaVM.framework/Versions/A/Frameworks/ -framework JavaNativeFoundation src/com/eteks/sweethome3d/j3d/YafarayRenderer.cpp -o lib/yafaray/macosx/libyafarayjni.dylib -Llib/yafaray/macosx -lyafaray_v3_core
    // Same instructions for arm64 with -arch param updated to arm64 and dependencies to OpenJDK
    // SweetHome3D project folder> clang++ -std=c++11 -I/Library/Java/JavaVirtualMachines/zulu-8.jdk/Contents/Home/include -I/Library/Java/JavaVirtualMachines/zulu-8.jdk/Contents/Home/include/darwin -Iinclude/yafaray -arch arm64 -dynamiclib -F/Library/Java/JavaVirtualMachines/zulu-8.jdk/Contents/Home/lib -framework JavaNativeFoundation src/com/eteks/sweethome3d/j3d/YafarayRenderer.cpp -o lib/yafaray/macosx/libyafarayjni.dylib -Llib/yafaray/macosx -lyafaray_v3_core
    // To obtain a libyafarayjni.dylib fat file, store libyafarayjni.dylib for x86_64 in libyafarayjni_x86_64.dylib and its arm64 version in libyafarayjni_arm64.dylib and run
    // SweetHome3D project folder> lipo lib/yafaray/macosx/libyafarayjni_x86_64.dylib lib/yafaray/macosx/libyafarayjni_arm64.dylib -create -output lib/yafaray/macosx/libyafarayjni.dylib
    //
    // To generate JNI DLL for Linux, read "YafaRay Ubuntu 16.04 building - Standalone.txt" in libYafaRay/building/linux (no need to install Python in LIBRARY DEPENDENCIES and stop after installing LIBRARY DEPENDENCIES)
    // Build YafaRay with current source code after setting "option(WITH_Freetype", "option(WITH_OpenEXR" "option(WITH_JPEG", "option(WITH_PNG", "option(WITH_TIFF", option(WITH_XMLImport, option(WITH_XML_LOADER, "option(WITH_OpenCV" to OFF in libYafaRay/CMakeLists.txt
    // Copy yafa-dev/build/yafaray_v3/libyafaray_v3_core.so + yafaray-plugins generated files and required DLLs listed below and found in /usr/lib/i386-linux-gnu
    // Open Terminal window, run the command "sudo apt install openjdk-8-jdk-headless" and the following one (/usr/lib/jvm/java-8-openjdk-amd64/ contains JDK)
    // SweetHome3D project folder> gcc -fPIC -std=c++11 -I/usr/lib/jvm/java-8-openjdk-amd64/include -I/usr/lib/jvm/java-8-openjdk-amd64/include/linux -Iinclude/yafaray src/com/eteks/sweethome3d/j3d/YafarayRenderer.cpp -shared -o lib/yafaray/linux/x64/libyafarayjni.so -Llib/yafaray/linux/x64 -lyafaray_v3_core
    // Same instructions for i386 with the following final command:
    // SweetHome3D project folder> gcc -fPIC -std=c++11 -I/usr/lib/jvm/java-8-openjdk-i386/include -I/usr/lib/jvm/java-8-openjdk-i386/include/linux -Iinclude/yafaray src/com/eteks/sweethome3d/j3d/YafarayRenderer.cpp -shared -o lib/yafaray/linux/i386/libyafarayjni.so -Llib/yafaray/linux/i386 -lyafaray_v3_core

    try {
      pluginsFolder = System.getProperty("com.eteks.sweethome3d.j3d.YafarayPluginsFolder");
      String yafarayLibraryFolder = null;
      if (pluginsFolder == null) {
        String libraryPaths = System.getProperty("java.library.path", "");
        String [] paths = libraryPaths.split(System.getProperty("path.separator"));
        for (int i = 0; i < paths.length && pluginsFolder == null; i++) {
          File [] libraries = new File(paths [i]).listFiles();
          if (libraries != null) {
            for (File library : libraries) {
              if (!library.isDirectory()
                  && library.getName().indexOf("yafaray") >= 0) {
                pluginsFolder = new File(paths [i], "yafaray-plugins").getAbsolutePath();
                yafarayLibraryFolder = new File(pluginsFolder).getParent();
                break;
              }
            }
          }
        }

        // If plugins folder contains some not ASCII characters
        if (yafarayLibraryFolder != null
            && OperatingSystem.isWindows()
            && !Charset.forName("US-ASCII").newEncoder().canEncode(yafarayLibraryFolder)) {
          // Copy plugins DLLs in a folder that will be accepted by YafaRay environment DLL loader
          String jarFile = YafarayRenderer.class.getResource(YafarayRenderer.class.getSimpleName() + ".class").getFile();
          URL applicationJarUrl = new URL(jarFile.substring(0, jarFile.indexOf("!/")));
          if ("file".equals(applicationJarUrl.getProtocol())) {
            File applicationJar = new File(applicationJarUrl.toURI());
            long applicationJarDate = applicationJar.lastModified();
            long applicationJarLength = applicationJar.length();
            File pluginsCacheFolder;
            if (applicationJarDate != 0 && applicationJarLength != 0) {
              File cacheFolder = new File(System.getProperty("java.io.tmpdir"));
              pluginsCacheFolder = new File(cacheFolder, "sweethome3d-cache-yafaray-plugins-"
                  + System.getProperty("sun.arch.data.model") + "-" + applicationJarLength + "-" + (applicationJarDate / 1000L));
              if (!pluginsCacheFolder.exists()
                  && !pluginsCacheFolder.mkdirs()) {
                pluginsCacheFolder = null;
              }
            } else {
              pluginsCacheFolder = File.createTempFile("yafaray-plugins", "tmp");
              pluginsCacheFolder.delete();
              if (!pluginsCacheFolder.mkdirs()) {
                pluginsCacheFolder = null;
              }
            }

            if (pluginsCacheFolder != null) {
              pluginsCacheFolder.deleteOnExit();
              // Copy plug-in DLLs
              for (File pluginDll : new File(pluginsFolder).listFiles()) {
                if (!pluginDll.isDirectory()) {
                  copyFileToFolder(pluginDll, pluginsCacheFolder);
                }
              }
              pluginsFolder = pluginsCacheFolder.getAbsolutePath();
            } else {
              pluginsFolder = null;
              System.err.println("Couldn't extract YafaRay plugins");
            }
          }
        }
      } else {
        yafarayLibraryFolder = new File(pluginsFolder).getParent();
      }

      if (pluginsFolder != null) {
        if (OperatingSystem.isWindows()) {
          // Under Windows, use System.load rather than System.loadLibrary which doesn't work
          // Change library loading order with great care because of dependencies (use Dependency Walker to check them)
          System.load(yafarayLibraryFolder + "\\libwinpthread-1.dll");
          System.load(yafarayLibraryFolder + ("64".equals(System.getProperty("sun.arch.data.model")) ? "\\libgcc_s_seh-1.dll" : "\\libgcc_s_dw2-1.dll"));
          System.load(yafarayLibraryFolder + "\\libstdc++-6.dll");
          System.load(yafarayLibraryFolder + "\\libyafaray_v3_core.dll");
          System.load(yafarayLibraryFolder + "\\libyafarayjni.dll");
        } else {
          System.loadLibrary("yafaray_v3_core");
          System.loadLibrary("yafarayjni");
        }
      } else {
        System.err.println("Couldn't locate YafaRay library");
      }
    } catch (UnsatisfiedLinkError ex) {
      pluginsFolder = null;
      ex.printStackTrace();
    } catch (IOException ex) {
      pluginsFolder = null;
      ex.printStackTrace();
    } catch (URISyntaxException ex) {
      pluginsFolder = null;
      ex.printStackTrace();
    }
  }

  private static void copyFileToFolder(File file, File folder) throws IOException {
    File copy = new File(folder, file.getName());
    copy.deleteOnExit();
    if (!copy.exists()) {
      InputStream in = null;
      OutputStream out = null;
      try {
        in = new FileInputStream(file);
        out = new FileOutputStream(copy);
        byte [] buffer = new byte [8192];
        int size;
        while ((size = in.read(buffer)) != -1) {
          out.write(buffer, 0, size);
        }
      } finally {
        if (in != null) {
          in.close();
        }
        if (out != null) {
          out.close();
        }
      }
    }
  }

  private final Object3DFactory object3dFactory;

  private int homeLightColor;
  private boolean useSunSky;
  private boolean useSunskyLight;

  private long environment;
  private long scene;
  private final Map<Selectable, String []> homeItemsNames = new HashMap<Selectable, String []>();
  private final Map<TransparentTextureKey, String> texturesCache = new HashMap<TransparentTextureKey, String>();

  /**
   * Creates an instance ready to render the scene matching the given <code>home</code>.
   */
  public YafarayRenderer(Home home,
                         Object3DFactory object3dFactory,
                         Quality quality) throws IOException {
    super(home, quality);
    if (object3dFactory == null) {
      object3dFactory = new PhotoObject3DFactory();
    }
    this.object3dFactory = object3dFactory;
  }

  @Override
  public boolean isAvailable() {
    return pluginsFolder != null;
  }

  @Override
  public String getName() {
    return "YafaRay";
  }

  /**
   * Initializes this rendering engine.
   * @throws IOException if texture image files required in the scene couldn't be created.
   */
  private void init() throws IOException {
    if (this.environment == 0) {
      synchronized (YafarayRenderer.class) {
        // Synchronize with rendering abortion
        this.environment = createEnvironment(pluginsFolder, "disabled" /* "disabled", "info" or "debug" */);
      }
    }

    this.scene = createScene();

    Home home = getHome();
    HomeEnvironment homeEnvironment = home.getEnvironment();
    this.homeLightColor = home.getEnvironment().getLightColor();
    this.useSunskyLight = !(home.getCamera() instanceof ObserverCamera);
    boolean silk = isSilkShaderUsed(getQuality());

    float subpartSize = homeEnvironment.getSubpartSizeUnderLight();
    // Dividing walls and rooms surface in subparts is useless
    homeEnvironment.setSubpartSizeUnderLight(0);

    // Export to YafaRay the Java 3D shapes and appearance of the ground, the walls, the furniture and the rooms
    List<HomeLight> lights = new ArrayList<HomeLight>();
    for (Selectable item : home.getSelectableViewableItems()) {
      checkCurrentThreadIsntInterrupted();

      if (item instanceof HomeFurnitureGroup) {
        for (HomePieceOfFurniture piece : ((HomeFurnitureGroup)item).getAllFurniture()) {
          if (!(piece instanceof HomeFurnitureGroup)) {
            Node node = (Node)object3dFactory.createObject3D(home, piece, true);
            if (node != null) {
              if (piece instanceof HomeLight) {
                HomeLight light = (HomeLight)piece;
                lights.add(light);
                this.homeItemsNames.put(piece, exportNode(node, false, silk, light.getPower(), light.getLightSourceMaterialNames()));
              } else {
                this.homeItemsNames.put(piece, exportNode(node, false, silk));
              }
            }
          }
        }
      } else {
        Node node = (Node)object3dFactory.createObject3D(home, item, true);
        if (node != null) {
          String [] itemNames;
          if (item instanceof HomeLight) {
            HomeLight light = (HomeLight)item;
            lights.add(light);
            itemNames = exportNode(node, false, silk, light.getPower(), light.getLightSourceMaterialNames());
          } else {
            itemNames = exportNode(node, item instanceof Wall || item instanceof Room, silk);
          }
          this.homeItemsNames.put(item, itemNames);
        }
      }
    }

    checkCurrentThreadIsntInterrupted();

    // Create a 3D ground large enough to join the sky at the horizon (caution: too large values may slow down rendering process)
    Ground3D ground = new Ground3D(home, -1E7f / 2, -1E7f / 2, 1E7f, 1E7f, true);
    Transform3D translation = new Transform3D();
    translation.setTranslation(new Vector3f(0, -0.1f, 0));
    TransformGroup groundTransformGroup = new TransformGroup(translation);
    groundTransformGroup.addChild(ground);
    exportNode(groundTransformGroup, true, silk);
    homeEnvironment.setSubpartSizeUnderLight(subpartSize);

    checkCurrentThreadIsntInterrupted();

    HashMap<String, Object> params = new HashMap<String, Object>();
    HomeTexture skyTexture = homeEnvironment.getSkyTexture();
    this.useSunSky = skyTexture == null || this.useSunskyLight;
    if (!this.useSunSky) {
      // If observer camera is used with a sky texture,
      // create an image base light from sky texture
      Content imageContent = skyTexture.getImage();
      InputStream skyImageStream = imageContent.openStream();
      BufferedImage skyImage = ImageIO.read(skyImageStream);
      skyImageStream.close();
      // Create a temporary image base light twice as high that will contain sky image in the top part
      BufferedImage imageBaseLightImage = new BufferedImage(skyImage.getWidth(),
          skyImage.getHeight() * 2, BufferedImage.TYPE_3BYTE_BGR);
      Graphics2D g2D = (Graphics2D)imageBaseLightImage.getGraphics();
      // Draw mirrored background image at the bottom of imageBaseLightImage to avoid possible line at the horizon
      AffineTransform mirrorTransform = AffineTransform.getScaleInstance(1, -1);
      mirrorTransform.translate(skyImage.getWidth() * skyTexture.getXOffset(), -2 * skyImage.getHeight());
      g2D.drawRenderedImage(skyImage, mirrorTransform);
      // Draw background image at the top of imageBaseLightImage
      g2D.drawRenderedImage(skyImage, AffineTransform.getTranslateInstance(skyImage.getWidth() * skyTexture.getXOffset(), 0));
      g2D.drawRenderedImage(skyImage, AffineTransform.getTranslateInstance(skyImage.getWidth() * (skyTexture.getXOffset() - 1), 0));
      g2D.dispose();

      params.put("type", "image");
      params.put("color_space", "sRGB");
      params.put("width", imageBaseLightImage.getWidth());
      params.put("height", imageBaseLightImage.getHeight());
      byte [] imageData = ((DataBufferByte)imageBaseLightImage.getRaster().getDataBuffer()).getData();
      params.put("channels", imageData.length / (imageBaseLightImage.getWidth() * imageBaseLightImage.getHeight()));
      createTexture("backgroundImage", imageData, params);

      params.put("type", "textureback");
      params.put("mapping", "sphere");
      params.put("texture", "backgroundImage");
      params.put("ibl", false);
      createBackground("background", params);
    }

    checkCurrentThreadIsntInterrupted();

    // Set light settings
    int ceillingLightColor = homeEnvironment.getCeillingLightColor();
    if (ceillingLightColor > 0) {
      // Add lights at the top of each room
      for (Room room : home.getRooms()) {
        Level roomLevel = room.getLevel();
        if (room.isCeilingVisible()
            && (roomLevel == null || roomLevel.isViewableAndVisible())) {
          float xCenter = room.getXCenter();
          float yCenter = room.getYCenter();

          double smallestDistance = Float.POSITIVE_INFINITY;
          float roomElevation = roomLevel != null
              ? roomLevel.getElevation()
              : 0;
          float roomHeight = roomElevation +
              (roomLevel == null ? home.getWallHeight() : roomLevel.getHeight());
          List<Level> levels = home.getLevels();
          if (roomLevel == null || levels.indexOf(roomLevel) == levels.size() - 1) {
            // Search the height of the wall closest to the point xCenter, yCenter
            for (Wall wall : home.getWalls()) {
              if ((wall.getLevel() == null || wall.getLevel().isViewable())
                  && wall.isAtLevel(roomLevel)) {
                float wallElevation = wall.getLevel() == null ? 0 : wall.getLevel().getElevation();
                Float wallHeightAtStart = wall.getHeight();
                float [][] points = wall.getPoints();
                for (int i = 0; i < points.length; i++) {
                  double distanceToWallPoint = Point2D.distanceSq(points [i][0], points [i][1], xCenter, yCenter);
                  if (distanceToWallPoint < smallestDistance) {
                    smallestDistance = distanceToWallPoint;
                    if (i == 0 || i == points.length - 1) { // Wall start
                      roomHeight = wallHeightAtStart != null
                          ? wallHeightAtStart
                          : home.getWallHeight();
                    } else { // Wall end
                      roomHeight = wall.isTrapezoidal()
                          ? wall.getHeightAtEnd()
                          : (wallHeightAtStart != null ? wallHeightAtStart : home.getWallHeight());
                    }
                    roomHeight += wallElevation;
                  }
                }
              }
            }
          }

          params.clear();
          params.put("type", "spherelight");
          params.put("color", new float [] {
              (float)(ceillingLightColor >> 16) / 0xD0 * (this.homeLightColor >> 16) / 255f,
              (float)((ceillingLightColor >> 8) & 0xFF) / 0xD0 * ((this.homeLightColor >> 8) & 0xFF) / 255f,
              (float)(ceillingLightColor & 0xFF) / 0xD0 * (this.homeLightColor & 0xFF) / 255f, 1});
          params.put("power", Math.sqrt(room.getArea()) / 3);
          params.put("from", new float [] {xCenter, -yCenter, roomHeight - 25});
          params.put("radius", 20f);
          params.put("samples", 4);
          createLight(UUID.randomUUID().toString(), params);
        }
      }

      checkCurrentThreadIsntInterrupted();
    }

    final ModelManager modelManager = ModelManager.getInstance();
    // Add visible and turned on light sources
    for (final HomeLight light : lights) {
      Level level = light.getLevel();
      if (light.getPower() > 0f
          && light.getLightSourceMaterialNames().length == 0
          && (level == null
              || level.isViewableAndVisible())) {
        if (light.isHorizontallyRotated()
            || light.getModelTransformations() != null) {
          // Retrieve the 3D model of the light to get the transformation with horizontal rotation
          modelManager.loadModel(light.getModel(), true,
              new ModelManager.ModelObserver() {
                public void modelUpdated(BranchGroup modelRoot) {
                  float [][] modelRotation = light.getModelRotation();

                  Transformation[] transformations = light.getModelTransformations();
                  Transform3D undeformedModelInvertedNormalization = null;
                  Node undeformedModel = null;
                  if (transformations != null) {
                    undeformedModel = modelManager.cloneNode(modelRoot);
                    undeformedModelInvertedNormalization = modelManager.
                        getNormalizedTransform(modelRoot, modelRotation, 1, light.isModelCenteredAtOrigin());
                    undeformedModelInvertedNormalization.invert();
                    updateModelTransformations(modelRoot, transformations);
                  }

                  // Compute normalization of the light
                  Transform3D normalization = modelManager.
                      getNormalizedTransform(modelRoot, modelRotation, 1, light.isModelCenteredAtOrigin());
                  TransformGroup normalizedModel = new TransformGroup(normalization);
                  normalizedModel.addChild(modelRoot);
                  // Get the transformation applied to the light model
                  Transform3D lightTransform = modelManager.getPieceOfFurnitureNormalizedModelTransformation(
                      light, normalizedModel);

                  if (transformations != null) {
                    for (LightSource lightSource : ((HomeLight)light).getLightSources()) {
                      int i;
                      for (i = 0; i < transformations.length; i++) {
                        Transformation transformation = transformations [i];
                        // Compute light source location in undeformed model space
                        Point3f lightSourceLocation = getNormalizedLightSourceLocation(lightSource);
                        undeformedModelInvertedNormalization.transform(lightSourceLocation);
                        BoundingSphere lightSphere = new BoundingSphere(new Point3d(lightSourceLocation), getLightSourceRadius(light, lightSource));
                        String transformUserData = transformation.getName() + ModelManager.DEFORMABLE_TRANSFORM_GROUP_SUFFIX;
                        if (intersectsDeformedNode(undeformedModel, lightSphere, transformUserData)) {
                          Transform3D deformedLightSourceTransform = new Transform3D(lightTransform);
                          // Compute transformation of the light source in deformed model
                          deformedLightSourceTransform.mul(normalization);
                          Transform3D deformation = getDeformation(modelRoot, new Transform3D(), transformUserData);
                          deformedLightSourceTransform.mul(deformation);
                          deformedLightSourceTransform.mul(undeformedModelInvertedNormalization);
                          exportLightSource(light, lightSource, deformedLightSourceTransform);
                          break;
                        }
                      }
                      if (i == transformations.length) {
                        // Export light without deformation
                        exportLightSource(light, lightSource, lightTransform);
                      }
                    }
                  } else {
                    exportLightSources(light, lightTransform);
                  }
                }

                public void modelError(Exception ex) {
                  // In case of problem, ignore light
                }
              });
        } else {
          // Compute the light transformation without horizontal rotation
          Transform3D lightTransform =
              modelManager.getPieceOfFurnitureNormalizedModelTransformation(light, null);
          exportLightSources(light, lightTransform);
        }
      }

      checkCurrentThreadIsntInterrupted();
    }

    params.clear();
    params.put("type", "none");
    createIntegrator("volintegrator", params);
  }

  /**
   * Throws an exception if current thread is interrupted.
   */
  private void checkCurrentThreadIsntInterrupted() throws InterruptedIOException {
    if (Thread.currentThread().isInterrupted()) {
      throw new InterruptedIOException("Current thread interrupted");
    }
  }

  public void render(final BufferedImage image,
                     Camera camera,
                     List<? extends Selectable> updatedItems,
                     final ImageObserver observer) throws IOException {
    if (Thread.currentThread().isInterrupted()
        || !isAvailable()) {
      return;
    }

    if (this.scene == 0) {
      init();
    } else if (updatedItems != null && !updatedItems.isEmpty()) {
      clearAll();
      this.homeItemsNames.clear();
      this.texturesCache.clear();
      init();
    }

    HashMap<String, Object> params = new HashMap<String, Object>();

    if (this.useSunSky) {
      deleteBackground("background");
    }
    deleteLight("sun");
    // Update Sun direction during daytime
    Compass compass = getHome().getCompass();
    float [] sunDirection = getSunDirection(compass, Camera.convertTimeToTimeZone(camera.getTime(), compass.getTimeZone()));
    float [] sunColor = null;
    if (sunDirection [1] > -0.075f) {
      // Retrieve sun color with SunFlow (YafaRay ComputeAttenuatedSunlight is not accessible)
      SunSkyLight sunSkyLight = new SunSkyLight();
      ParameterList parameterList = new ParameterList();
      parameterList.addVectors("up", InterpolationType.NONE, new float [] {0, 1, 0});
      parameterList.addVectors("east", InterpolationType.NONE,
          new float [] {(float)Math.sin(compass.getNorthDirection()), 0, (float)Math.cos(compass.getNorthDirection())});
      parameterList.addVectors("sundir", InterpolationType.NONE,
          new float [] {sunDirection [0], sunDirection [1], sunDirection [2]});
      sunSkyLight.update(parameterList, new SunflowAPI());
      sunColor = sunSkyLight.getSunColor().getRGB();

      if (this.useSunSky) {
        params.put("type", "sunsky");
        params.put("from", new float [] {sunDirection [0], -sunDirection [2], sunDirection [1]});
        params.put("turbidity", 6f);
        params.put("power", 1.5f);
        params.put("light_samples", 12);
        params.put("background_light", this.useSunskyLight);
        createBackground("background", params);
      }

      // Simulate additional Sun with a faraway sphere light of a color depending of the hour of the day
      float sunPower = this.useSunskyLight ? 20 : 40;
      params.clear();
      params.put("type", "spherelight");
      params.put("color", new float [] {
          (float)(this.homeLightColor >> 16) * (float)Math.sqrt(sunColor [0]),
          (float)((this.homeLightColor >> 8) & 0xFF) * (float)Math.sqrt(sunColor [1]),
          (float)(this.homeLightColor & 0xFF) * (float)Math.sqrt(sunColor [2]), 1});
      params.put("power", sunPower);
      params.put("from", new float [] {1000000 * sunDirection [0], -1000000 * sunDirection [2], 1000000 * sunDirection [1]});
      params.put("radius", 10000f);
      params.put("samples", 4);
      createLight("sun", params);
    }

    String lightingMethod = getRenderingParameterValue("lightingMethod");
    deleteIntegrator("integrator");
    params.clear();
    params.put("type", lightingMethod);
    params.put("raydepth", 16);
    params.put("shadowDepth", 4);
    params.put("transpShad", true);
    if ("pathtracing".equals(lightingMethod)) {
      params.put("bounces", Integer.parseInt(getRenderingParameterValue("diffusedBounces")));
    }
    Integer causticsPhotons = new Integer(getRenderingParameterValue("causticsPhotons"));
    if ("pathtracing".equals(lightingMethod)) {
      params.put("caustic_type", causticsPhotons > 0 ? "photon" : "none");
      params.put("photons", causticsPhotons);
    } else {
      params.put("caustics", causticsPhotons > 0);
      params.put("photons", causticsPhotons);
    }
    if (!this.useSunskyLight
        && sunDirection [1] > -0.075f
        && "directlighting".equals(lightingMethod)) {
      // Add ambient occlusion
      params.put("AO_color",
          new float [] {(sunColor [1] + sunColor [2]) / 100,
                        (sunColor [0] + sunColor [2]) / 100,
                        (sunColor [0] + sunColor [1]) / 100, 1f});
      params.put("AO_distance", 1f);
      params.put("AO_samples", 4);
      params.put("do_AO", true);
    }
    createIntegrator("integrator", params);

    float yaw = camera.getYaw();
    float pitch;
    if (camera.getLens() == Camera.Lens.SPHERICAL) {
      pitch = 0;
    } else {
      pitch = camera.getPitch();
    }
    Transform3D transform = new Transform3D();
    transform.rotZ(yaw);
    Transform3D pitchRotation = new Transform3D();
    pitchRotation.rotX(-pitch);
    transform.mul(pitchRotation);
    Point3f to = new Point3f(0, 100, 0);
    transform.transform(to);
    Point3f up = new Point3f(0, 0, 100);
    transform.transform(up);

    deleteCamera("camera");
    params.clear();
    params.put("from", new float [] {camera.getX(), -camera.getY(), camera.getZ()});
    params.put("to", new float [] {camera.getX() + to.x, -camera.getY() - to.y, camera.getZ() + to.z});
    params.put("up", new float [] {camera.getX() + up.x, -camera.getY() - up.y, camera.getZ() + up.z});
    params.put("resx", image.getWidth());
    params.put("resy", image.getHeight());
    switch (camera.getLens()) {
      case SPHERICAL:
        params.put("type", "equirectangular");
        break;
      case FISHEYE:
        params.put("type", "angular");
        params.put("angle", 90f);
        params.put("mirrored", true);
        params.put("projection", "orthographic");
        params.put("circular", true);
        break;
      case NORMAL:
        params.put("dof_distance", new Float(getRenderingParameterValue("normalLens.focusDistance")));
        params.put("aperture", new Float(getRenderingParameterValue("normalLens.radius")));
        // No break
      case PINHOLE:
      default:
        params.put("type", "perspective");
        params.put("focal", (float)(0.5 / Math.tan(camera.getFieldOfView() / 2.)));
        break;
    }
    createCamera("camera", params);

    params.clear();
    params.put("width", image.getWidth());
    params.put("height", image.getHeight());
    params.put("xstart", 0);
    params.put("ystart", 0);
    params.put("filter_type", getRenderingParameterValue("filter")); // mitchell, gauss, lanczos or box (default value)
    if (camera.getLens() == Camera.Lens.NORMAL) {
      params.put("AA_minsamples", Integer.parseInt(getRenderingParameterValue("antiAliasingNormalLens.min")));
    } else {
      params.put("AA_minsamples", Integer.parseInt(getRenderingParameterValue("antiAliasingOtherLens.min")));
    }
    params.put("color_space", "sRGB");
    params.put("tile_size", 32);
    params.put("camera_name", "camera");
    params.put("integrator_name", "integrator");
    params.put("volintegrator_name", "volintegrator");
    params.put("background_name", "background");
    // Set adv_shadow_bias_value to avoid light lines in ceiling corners
    // Caution: using 1E-3 bias lower value can produce some shadow lines on some complex flat shapes
    params.put("adv_auto_shadow_bias_enabled", false);
    params.put("adv_shadow_bias_value", 5E-3f);

    render(new BufferedImageOutput(observer, image), params);
  }

  /**
   * Stops the rendering process.
   */
  public void stop() {
    if (this.scene != 0) {
      synchronized (YafarayRenderer.class) {
        // Synchronize the creation of a new rendering session
        abort();
      }
    }
  }

  /**
   * Disposes temporary data that may be required to run this renderer.
   * Trying to use this renderer after a call to this method may lead to errors.
   */
  public synchronized void dispose() {
    this.texturesCache.clear();
  }

  /**
   * Returns <code>true</code> if silk shader should be used.
   */
  private boolean isSilkShaderUsed(Quality quality) {
    boolean silk = !this.useSunskyLight && quality == Quality.HIGH;
    String shininessShader = getRenderingParameterValue("shininessShader");
    if ("glossy".equals(shininessShader)) {
      silk = false;
    } else if ("silk".equals(shininessShader)) {
      silk = true;
    }
    return silk;
  }

  private String [] exportNode(Node node, boolean ignoreTransparency, boolean silk) throws IOException {
    return exportNode(node, ignoreTransparency, silk, 0, null);
  }

  /**
   * Exports the given Java 3D <code>node</code> and its children with YafaRay API,
   * then returns the YafaRay mesh names that match this node.
   */
  private String [] exportNode(Node node, boolean ignoreTransparency, boolean silk,
                                float lightPower, String [] lightSourceMaterialNames) throws IOException {
    List<String> nodeNames = new ArrayList<String>();
    exportNode(node, ignoreTransparency, silk, lightPower, lightSourceMaterialNames, nodeNames, new Transform3D());
    return nodeNames.toArray(new String [nodeNames.size()]);
  }

  /**
   * Exports all the 3D shapes children of <code>node</code> with YafaRay API.
   */
  private void exportNode(Node node,
                          boolean ignoreTransparency,
                          boolean silk,
                          float lightPower,
                          String [] lightSourceMaterialNames,
                          List<String> nodeNames,
                          Transform3D parentTransformations) throws IOException {
    if (node instanceof Group) {
      if (node instanceof TransformGroup) {
        parentTransformations = new Transform3D(parentTransformations);
        Transform3D transform = new Transform3D();
        ((TransformGroup)node).getTransform(transform);
        parentTransformations.mul(transform);
      }
      // Export all children
      Enumeration<?> enumeration = ((Group)node).getAllChildren();
      while (enumeration.hasMoreElements()) {
        exportNode((Node)enumeration.nextElement(), ignoreTransparency, silk, lightPower, lightSourceMaterialNames,
            nodeNames, parentTransformations);
      }
    } else if (node instanceof Link) {
      exportNode(((Link)node).getSharedGroup(), ignoreTransparency, silk, lightPower, lightSourceMaterialNames,
          nodeNames, parentTransformations);
    } else if (node instanceof Shape3D) {
      Shape3D shape = (Shape3D)node;
      Appearance appearance = shape.getAppearance();
      RenderingAttributes renderingAttributes = appearance != null
          ? appearance.getRenderingAttributes() : null;
      TransparencyAttributes transparencyAttributes = appearance != null
          ? appearance.getTransparencyAttributes() : null;
      boolean transparent = transparencyAttributes != null
          && transparencyAttributes.getTransparency() == 1;
      boolean lightSourceShape = false;
      if (lightSourceMaterialNames != null) {
        for (String lightSourceMaterialName : lightSourceMaterialNames) {
          try {
            if (lightSourceMaterialName.equals(appearance.getName())) {
              lightSourceShape = true;
              break;
            }
          } catch (NoSuchMethodError ex) {
            // getName not supported with Java 3D < 1.4
          }
        }
      }

      // Ignore invisible shapes and fully transparency shapes without a texture
      if ((renderingAttributes == null
              || renderingAttributes.getVisible())
          && (!transparent
              || lightSourceShape)) {
        String shapeName = (String)shape.getUserData();
        // Build a unique object name
        String uuid = UUID.randomUUID().toString();

        String appearanceName = "default";
        TexCoordGeneration texCoordGeneration = null;
        Transform3D textureTransform = new Transform3D();
        int cullFace = PolygonAttributes.CULL_BACK;
        boolean backFaceNormalFlip = false;
        Color3f lightSourceRadiance = null;
        if (appearance != null) {
          PolygonAttributes polygonAttributes = appearance.getPolygonAttributes();
          if (polygonAttributes != null) {
            cullFace = polygonAttributes.getCullFace();
            backFaceNormalFlip = polygonAttributes.getBackFaceNormalFlip();
          }
          appearanceName = "material" + uuid;

          if (lightSourceShape && lightPower > 0) {
            // Get light source color
            Material material = appearance.getMaterial();
            Color3f lightColor = new Color3f();
            if (material != null) {
              material.getDiffuseColor(lightColor);
            } else {
              ColoringAttributes coloringAttributes = appearance.getColoringAttributes();
              if (coloringAttributes != null) {
                coloringAttributes.getColor(lightColor);
              }
            }
            lightSourceRadiance = new Color3f(lightColor.getX() * (this.homeLightColor >> 16),
                lightColor.getY() * ((this.homeLightColor >> 8) & 0xFF),
                lightColor.getZ() * (this.homeLightColor & 0xFF));
            if (transparent) {
              HashMap<String, Object> params = new HashMap<String, Object>();
              params.put("type", "glass");
              params.put("visibility", "invisible");
              createMaterial(appearanceName, params, new ArrayList<Map<String, Object>>());
            } else {
              // Use an appearance with no shading
              Appearance exportedAppearance = new Appearance();
              exportedAppearance.setColoringAttributes(new ColoringAttributes(lightColor, ColoringAttributes.SHADE_GOURAUD));
              exportAppearance(exportedAppearance, appearanceName, false, false, false, lightPower);
            }
            nodeNames.add(appearanceName);
          } else if (!transparent) {
            texCoordGeneration = appearance.getTexCoordGeneration();
            TextureAttributes textureAttributes = appearance.getTextureAttributes();
            if (textureAttributes != null) {
              textureAttributes.getTextureTransform(textureTransform);
            }
            boolean mirror = shapeName != null
                && shapeName.startsWith(ModelManager.MIRROR_SHAPE_PREFIX);
            exportAppearance(appearance, appearanceName, mirror, ignoreTransparency, silk, -1);
            nodeNames.add(appearanceName);
          }
        }

        // Export object geometries
        for (int i = 0, n = shape.numGeometries(); i < n; i++) {
          String objectNameBase = "object" + uuid + "-" + i;
          // Always ignore normals on walls
          String [] objectsName = exportNodeGeometry(shape.getGeometry(i), parentTransformations, texCoordGeneration,
              textureTransform, cullFace, backFaceNormalFlip, objectNameBase, appearanceName, transparent, lightSourceRadiance, lightPower);
          if (objectsName != null) {
            for (String objectName : objectsName) {
              nodeNames.add(objectName);
            }
          }
        }
      }
    }
  }

  /**
   * Returns the names of the exported 3D geometries with YafaRay API.
   */
  private String [] exportNodeGeometry(Geometry geometry,
                                       Transform3D parentTransformations,
                                       TexCoordGeneration texCoordGeneration,
                                       Transform3D textureTransform,
                                       int cullFace,
                                       boolean backFaceNormalFlip,
                                       String objectNameBase,
                                       String appearanceName,
                                       boolean transparent,
                                       Color3f lightSourceRadiance,
                                       float lightPower) {
    if (geometry instanceof GeometryArray) {
      GeometryArray geometryArray = (GeometryArray)geometry;

      // Create vertices indices array according to geometry class
      int [] verticesIndices = null;
      int [] stripVertexCount = null;
      if (geometryArray instanceof IndexedGeometryArray) {
        if (geometryArray instanceof IndexedLineArray) {
          verticesIndices = new int [((IndexedGeometryArray)geometryArray).getIndexCount()];
        } else if (geometryArray instanceof IndexedTriangleArray) {
          verticesIndices = new int [((IndexedGeometryArray)geometryArray).getIndexCount()];
        } else if (geometryArray instanceof IndexedQuadArray) {
          verticesIndices = new int [((IndexedQuadArray)geometryArray).getIndexCount() * 3 / 2];
        } else if (geometryArray instanceof IndexedGeometryStripArray) {
          IndexedTriangleStripArray geometryStripArray = (IndexedTriangleStripArray)geometryArray;
          stripVertexCount = new int [geometryStripArray.getNumStrips()];
          geometryStripArray.getStripIndexCounts(stripVertexCount);
          if (geometryArray instanceof IndexedLineStripArray) {
            verticesIndices = new int [getLineCount(stripVertexCount) * 2];
          } else {
            verticesIndices = new int [getTriangleCount(stripVertexCount) * 3];
          }
        }
      } else {
        if (geometryArray instanceof LineArray) {
          verticesIndices = new int [((GeometryArray)geometryArray).getVertexCount()];
        } else if (geometryArray instanceof TriangleArray) {
          verticesIndices = new int [((GeometryArray)geometryArray).getVertexCount()];
        } else if (geometryArray instanceof QuadArray) {
          verticesIndices = new int [((QuadArray)geometryArray).getVertexCount() * 3 / 2];
        } else if (geometryArray instanceof GeometryStripArray) {
          GeometryStripArray geometryStripArray = (GeometryStripArray)geometryArray;
          stripVertexCount = new int [geometryStripArray.getNumStrips()];
          geometryStripArray.getStripVertexCounts(stripVertexCount);
          if (geometryArray instanceof LineStripArray) {
            verticesIndices = new int [getLineCount(stripVertexCount) * 2];
          } else {
            verticesIndices = new int [getTriangleCount(stripVertexCount) * 3];
          }
        }
      }

      if (verticesIndices != null) {
        boolean line = geometryArray instanceof IndexedLineArray
            || geometryArray instanceof IndexedLineStripArray
            || geometryArray instanceof LineArray
            || geometryArray instanceof LineStripArray;
        float [] vertices = new float [geometryArray.getVertexCount() * 3];
        float [] normals = !line && (geometryArray.getVertexFormat() & GeometryArray.NORMALS) != 0
            ? new float [geometryArray.getVertexCount() * 3]
            : null;
        // Store temporarily exported triangles to avoid to add their opposite triangles
        // (YafaRay doesn't render correctly a face and its opposite)
        Set<Triangle> exportedTriangles = line
            ? null
            : new HashSet<Triangle>(geometryArray.getVertexCount());

        boolean uvsGenerated = false;
        Vector4f planeS = null;
        Vector4f planeT = null;
        if (!line && texCoordGeneration != null) {
          uvsGenerated = texCoordGeneration.getGenMode() == TexCoordGeneration.OBJECT_LINEAR
              && texCoordGeneration.getEnable();
          if (uvsGenerated) {
            planeS = new Vector4f();
            planeT = new Vector4f();
            texCoordGeneration.getPlaneS(planeS);
            texCoordGeneration.getPlaneT(planeT);
          }
        }

        float [] uvs;
        if (uvsGenerated
            || (geometryArray.getVertexFormat() & GeometryArray.TEXTURE_COORDINATE_2) != 0) {
          uvs = new float [geometryArray.getVertexCount() * 2];
        } else {
          uvs = null;
        }

        if ((geometryArray.getVertexFormat() & GeometryArray.BY_REFERENCE) != 0) {
          if ((geometryArray.getVertexFormat() & GeometryArray.INTERLEAVED) != 0) {
            float [] vertexData = geometryArray.getInterleavedVertices();
            int vertexSize = vertexData.length / geometryArray.getVertexCount();
            // Export vertices coordinates
            for (int index = 0, i = vertexSize - 3, n = geometryArray.getVertexCount();
                 index < n; index++, i += vertexSize) {
              Point3f vertex = new Point3f(vertexData [i], vertexData [i + 1], vertexData [i + 2]);
              exportVertex(parentTransformations, vertex, index, vertices);
            }
            // Export normals
            if (normals != null) {
              for (int index = 0, i = vertexSize - 6, n = geometryArray.getVertexCount();
                   index < n; index++, i += vertexSize) {
                Vector3f normal = new Vector3f(vertexData [i], vertexData [i + 1], vertexData [i + 2]);
                exportNormal(parentTransformations, normal, index, normals, backFaceNormalFlip);
              }
            }
            // Export texture coordinates
            if (texCoordGeneration != null) {
              if (uvsGenerated) {
                for (int index = 0, i = vertexSize - 3, n = geometryArray.getVertexCount();
                      index < n; index++, i += vertexSize) {
                  TexCoord2f textureCoordinates = generateTextureCoordinates(
                      vertexData [i], vertexData [i + 1], vertexData [i + 2], planeS, planeT);
                  exportTextureCoordinates(textureCoordinates, textureTransform, index, uvs);
                }
              }
            } else if (uvs != null) {
              for (int index = 0, i = 0, n = geometryArray.getVertexCount();
                    index < n; index++, i += vertexSize) {
                TexCoord2f textureCoordinates = new TexCoord2f(vertexData [i], vertexData [i + 1]);
                exportTextureCoordinates(textureCoordinates, textureTransform, index, uvs);
              }
            }
          } else {
            // Export vertices coordinates
            float [] vertexCoordinates = geometryArray.getCoordRefFloat();
            for (int index = 0, i = 0, n = geometryArray.getVertexCount(); index < n; index++, i += 3) {
              Point3f vertex = new Point3f(vertexCoordinates [i], vertexCoordinates [i + 1], vertexCoordinates [i + 2]);
              exportVertex(parentTransformations, vertex, index, vertices);
            }
            // Export normals
            if (normals != null) {
              float [] normalCoordinates = geometryArray.getNormalRefFloat();
              for (int index = 0, i = 0, n = geometryArray.getVertexCount(); index < n; index++, i += 3) {
                Vector3f normal = new Vector3f(normalCoordinates [i], normalCoordinates [i + 1], normalCoordinates [i + 2]);
                exportNormal(parentTransformations, normal, index, normals, backFaceNormalFlip);
              }
            }
            // Export texture coordinates
            if (texCoordGeneration != null) {
              if (uvsGenerated) {
                for (int index = 0, i = 0, n = geometryArray.getVertexCount(); index < n; index++, i += 3) {
                  TexCoord2f textureCoordinates = generateTextureCoordinates(
                      vertexCoordinates [i], vertexCoordinates [i + 1], vertexCoordinates [i + 2], planeS, planeT);
                  exportTextureCoordinates(textureCoordinates, textureTransform, index, uvs);
                }
              }
            } else if (uvs != null) {
              float [] textureCoordinatesArray = geometryArray.getTexCoordRefFloat(0);
              for (int index = 0, i = 0, n = geometryArray.getVertexCount(); index < n; index++, i += 2) {
                TexCoord2f textureCoordinates = new TexCoord2f(textureCoordinatesArray [i], textureCoordinatesArray [i + 1]);
                exportTextureCoordinates(textureCoordinates, textureTransform, index, uvs);
              }
            }
          }
        } else {
          // Export vertices coordinates
          for (int index = 0, n = geometryArray.getVertexCount(); index < n; index++) {
            Point3f vertex = new Point3f();
            geometryArray.getCoordinate(index, vertex);
            exportVertex(parentTransformations, vertex, index, vertices);
          }
          // Export normals
          if (normals != null) {
            for (int index = 0, n = geometryArray.getVertexCount(); index < n; index++) {
              Vector3f normal = new Vector3f();
              geometryArray.getNormal(index, normal);
              exportNormal(parentTransformations, normal, index, normals, backFaceNormalFlip);
            }
          }
          // Export texture coordinates
          if (texCoordGeneration != null) {
            if (uvsGenerated) {
              for (int index = 0, n = geometryArray.getVertexCount(); index < n; index++) {
                Point3f vertex = new Point3f();
                geometryArray.getCoordinate(index, vertex);
                TexCoord2f textureCoordinates = generateTextureCoordinates(
                    vertex.x, vertex.y, vertex.z, planeS, planeT);
                exportTextureCoordinates(textureCoordinates, textureTransform, index, uvs);
              }
            }
          } else if (uvs != null) {
            for (int index = 0, n = geometryArray.getVertexCount(); index < n; index++) {
              TexCoord2f textureCoordinates = new TexCoord2f();
              geometryArray.getTextureCoordinate(0, index, textureCoordinates);
              exportTextureCoordinates(textureCoordinates, textureTransform, index, uvs);
            }
          }
        }

        // Export lines, triangles or quadrilaterals according to the geometry
        int [] uvsIndices = uvs != null
            ? new int [verticesIndices.length]
            : null;
        if (geometryArray instanceof IndexedGeometryArray) {
          int [] normalsIndices = normals != null
              ? new int [verticesIndices.length]
              : null;

          if (geometryArray instanceof IndexedLineArray) {
            IndexedLineArray lineArray = (IndexedLineArray)geometryArray;
            for (int i = 0, n = lineArray.getIndexCount(); i < n; i += 2) {
              exportIndexedLine(lineArray, i, i + 1, verticesIndices, i);
            }
          } else {
            if (geometryArray instanceof IndexedTriangleArray) {
              IndexedTriangleArray triangleArray = (IndexedTriangleArray)geometryArray;
              for (int i = 0, n = triangleArray.getIndexCount(), triangleIndex = 0; i < n; i += 3) {
                triangleIndex = exportIndexedTriangle(triangleArray, i, i + 1, i + 2,
                    verticesIndices, normalsIndices, uvsIndices, triangleIndex, vertices, exportedTriangles, cullFace);
              }
            } else if (geometryArray instanceof IndexedQuadArray) {
              IndexedQuadArray quadArray = (IndexedQuadArray)geometryArray;
              for (int i = 0, n = quadArray.getIndexCount(), triangleIndex = 0; i < n; i += 4) {
                triangleIndex = exportIndexedTriangle(quadArray, i, i + 1, i + 2,
                    verticesIndices, normalsIndices, uvsIndices, triangleIndex, vertices, exportedTriangles, cullFace);
                triangleIndex = exportIndexedTriangle(quadArray, i, i + 2, i + 3,
                    verticesIndices, normalsIndices, uvsIndices, triangleIndex, vertices, exportedTriangles, cullFace);
              }
            } else if (geometryArray instanceof IndexedLineStripArray) {
              IndexedLineStripArray lineStripArray = (IndexedLineStripArray)geometryArray;
              for (int initialIndex = 0, lineIndex = 0, strip = 0; strip < stripVertexCount.length; strip++) {
                for (int i = initialIndex, n = initialIndex + stripVertexCount [strip] - 1;
                     i < n; i++, lineIndex += 2) {
                   exportIndexedLine(lineStripArray, i, i + 1, verticesIndices, lineIndex);
                }
                initialIndex += stripVertexCount [strip];
              }
            } else if (geometryArray instanceof IndexedTriangleStripArray) {
              IndexedTriangleStripArray triangleStripArray = (IndexedTriangleStripArray)geometryArray;
              for (int initialIndex = 0, triangleIndex = 0, strip = 0; strip < stripVertexCount.length; strip++) {
                for (int i = initialIndex, n = initialIndex + stripVertexCount [strip] - 2, j = 0;
                     i < n; i++, j++) {
                  if (j % 2 == 0) {
                    triangleIndex = exportIndexedTriangle(triangleStripArray, i, i + 1, i + 2,
                        verticesIndices, normalsIndices, uvsIndices, triangleIndex, vertices, exportedTriangles, cullFace);
                  } else { // Vertices of odd triangles are in reverse order
                    triangleIndex = exportIndexedTriangle(triangleStripArray, i, i + 2, i + 1,
                        verticesIndices, normalsIndices, uvsIndices, triangleIndex, vertices, exportedTriangles, cullFace);
                  }
                }
                initialIndex += stripVertexCount [strip];
              }
            } else if (geometryArray instanceof IndexedTriangleFanArray) {
              IndexedTriangleFanArray triangleFanArray = (IndexedTriangleFanArray)geometryArray;
              for (int initialIndex = 0, triangleIndex = 0, strip = 0; strip < stripVertexCount.length; strip++) {
                for (int i = initialIndex, n = initialIndex + stripVertexCount [strip] - 2;
                     i < n; i++) {
                  triangleIndex = exportIndexedTriangle(triangleFanArray, initialIndex, i + 1, i + 2,
                      verticesIndices, normalsIndices, uvsIndices, triangleIndex, vertices, exportedTriangles, cullFace);
                }
                initialIndex += stripVertexCount [strip];
              }
            }
          }

          if (normalsIndices != null && !Arrays.equals(verticesIndices, normalsIndices)) {
            // Remove indirection in verticesIndices, normals and uvsIndices
            // because YafaRay expects the same indices for vertices and normals
            float [] directVertices = new float [verticesIndices.length * 3];
            float [] directNormals =  normalsIndices != null
                ? new float [verticesIndices.length * 3]
                : null;
            float [] directUvs =  uvsIndices != null
                ? new float [verticesIndices.length * 2]
                : null;
            int verticeIndex = 0;
            int normalIndex = 0;
            int uvIndex = 0;
            for (int i = 0; i < verticesIndices.length; i++) {
              int indirectIndex = verticesIndices [i] * 3;
              directVertices [verticeIndex++] = vertices [indirectIndex++];
              directVertices [verticeIndex++] = vertices [indirectIndex++];
              directVertices [verticeIndex++] = vertices [indirectIndex++];
              if (normalsIndices != null) {
                indirectIndex = normalsIndices [i] * 3;
                directNormals [normalIndex++] = normals [indirectIndex++];
                directNormals [normalIndex++] = normals [indirectIndex++];
                directNormals [normalIndex++] = normals [indirectIndex++];
              }
              if (uvsIndices != null) {
                indirectIndex = uvsIndices [i] * 2;
                directUvs [uvIndex++] = uvs [indirectIndex++];
                directUvs [uvIndex++] = uvs [indirectIndex++];
                uvsIndices [i] = i;
              }
              verticesIndices [i] = i;
            }
            vertices = directVertices;
            normals = directNormals;
            uvs = directUvs;
          }
        } else {
          if (geometryArray instanceof LineArray) {
            LineArray lineArray = (LineArray)geometryArray;
            for (int i = 0, n = lineArray.getVertexCount(); i < n; i += 2) {
              exportLine(lineArray, i, i + 1, verticesIndices, i);
            }
          } else {
            if (geometryArray instanceof TriangleArray) {
              TriangleArray triangleArray = (TriangleArray)geometryArray;
              for (int i = 0, n = triangleArray.getVertexCount(), triangleIndex = 0; i < n; i += 3) {
                triangleIndex = exportTriangle(triangleArray, i, i + 1, i + 2,
                    verticesIndices, triangleIndex, vertices, exportedTriangles, cullFace);
              }
            } else if (geometryArray instanceof QuadArray) {
              QuadArray quadArray = (QuadArray)geometryArray;
              for (int i = 0, n = quadArray.getVertexCount(), triangleIndex = 0; i < n; i += 4) {
                triangleIndex = exportTriangle(quadArray, i, i + 1, i + 2,
                    verticesIndices, triangleIndex, vertices, exportedTriangles, cullFace);
                triangleIndex = exportTriangle(quadArray, i + 2, i + 3, i,
                    verticesIndices, triangleIndex, vertices, exportedTriangles, cullFace);
              }
            } else if (geometryArray instanceof LineStripArray) {
              LineStripArray lineStripArray = (LineStripArray)geometryArray;
              for (int initialIndex = 0, lineIndex = 0, strip = 0; strip < stripVertexCount.length; strip++) {
                for (int i = initialIndex, n = initialIndex + stripVertexCount [strip] - 1;
                     i < n; i++, lineIndex += 2) {
                  exportLine(lineStripArray, i, i + 1, verticesIndices, lineIndex);
                }
                initialIndex += stripVertexCount [strip];
              }
            } else if (geometryArray instanceof TriangleStripArray) {
              TriangleStripArray triangleStripArray = (TriangleStripArray)geometryArray;
              for (int initialIndex = 0, triangleIndex = 0, strip = 0; strip < stripVertexCount.length; strip++) {
                for (int i = initialIndex, n = initialIndex + stripVertexCount [strip] - 2, j = 0;
                     i < n; i++, j++) {
                  if (j % 2 == 0) {
                    triangleIndex = exportTriangle(triangleStripArray, i, i + 1, i + 2,
                        verticesIndices, triangleIndex, vertices, exportedTriangles, cullFace);
                  } else { // Vertices of odd triangles are in reverse order
                    triangleIndex = exportTriangle(triangleStripArray, i, i + 2, i + 1,
                        verticesIndices, triangleIndex, vertices, exportedTriangles, cullFace);
                  }
                }
                initialIndex += stripVertexCount [strip];
              }
            } else if (geometryArray instanceof TriangleFanArray) {
              TriangleFanArray triangleFanArray = (TriangleFanArray)geometryArray;
              for (int initialIndex = 0, triangleIndex = 0, strip = 0; strip < stripVertexCount.length; strip++) {
                for (int i = initialIndex, n = initialIndex + stripVertexCount [strip] - 2;
                     i < n; i++) {
                  triangleIndex = exportTriangle(triangleFanArray, initialIndex, i + 1, i + 2, verticesIndices,
                      triangleIndex, vertices, exportedTriangles, cullFace);
                }
                initialIndex += stripVertexCount [strip];
              }
            }
          }

          if (uvsIndices != null) {
            for (int i = 0; i < uvsIndices.length; i++) {
              uvsIndices [i] = verticesIndices [i];
            }
          }
        }

        if (line) {
          if (!transparent) {
            // Replace line segments by 0.15 wide triangles
            float [] closeVertices = new float [verticesIndices.length * 3];
            for (int i = 0, j = 0; i < verticesIndices.length; i += 2, j += 6) {
              int index = 3 * verticesIndices [i];
              int nextIndex = 3 * verticesIndices [i + 1];
              Vector3f direction = new Vector3f(vertices [nextIndex] - vertices [index], vertices [nextIndex + 1] - vertices [index + 1], vertices [nextIndex + 2] - vertices [index + 2]);
              Vector3f normal = direction.z == 0
                  ? new Vector3f(-direction.y, direction.x, 0)
                      : (direction.y == 0
                      ? new Vector3f(-direction.z, 0, direction.x)
                          : new Vector3f(-direction.y, direction.z, 0));
                  normal.normalize();
                  normal.scale(0.15f);
                  closeVertices [j] = vertices [index] + normal.x;
                  closeVertices [j + 1] = vertices [index + 1] + normal.y;
                  closeVertices [j + 2] = vertices [index + 2] + normal.z;
                  closeVertices [j + 3] = vertices [nextIndex] + normal.x;
                  closeVertices [j + 4] = vertices [nextIndex + 1] + normal.y;
                  closeVertices [j + 5] = vertices [nextIndex + 2] + normal.z;
            }

            // Based on code sequences programmed in xmlparser.cc
            startGeometry();
            int vertexCount = vertices.length / 3;
            startTriMesh(-1, vertexCount + verticesIndices.length, verticesIndices.length, false, false, 0, 0);
            for (int i = 0; i < vertices.length; i += 3) {
              addVertex(vertices [i], vertices [i + 1], vertices [i + 2]);
            }
            for (int i = 0; i < closeVertices.length; i += 3) {
              addVertex(closeVertices [i], closeVertices [i + 1], closeVertices [i + 2]);
            }
            for (int i = 0; i < verticesIndices.length; i += 2) {
              // Create 2 triangles for each segment
              addTriangle(vertexCount + i, vertexCount + i + 1, verticesIndices [i], appearanceName);
              addTriangle(verticesIndices [i], verticesIndices [i + 1], vertexCount + i + 1, appearanceName);
            }
            endTriMesh();
            endGeometry();

            startGeometry();
            smoothMesh(0, 180f);
            endGeometry();

            return new String [] {objectNameBase};
          }
        } else {
          if (lightSourceRadiance != null) {
            startGeometry();
            long id = startTriMesh(-1, vertices.length / 3, verticesIndices.length / 3, false, false, 0, 0);
            addTriangles(vertices, null, null, verticesIndices, null, appearanceName);
            endTriMesh();
            endGeometry();

            HashMap<String, Object> params = new HashMap<String, Object>();
            params.put("type", "meshlight");
            params.put("object", id);
            params.put("color", new float [] {
                lightSourceRadiance.getX(), lightSourceRadiance.getY(), lightSourceRadiance.getZ(), 1});
            params.put("power", 10 * lightPower * lightPower);
            params.put("samples", 64);
            createLight(objectNameBase, params);
            return new String [] {objectNameBase};
          } else if (!transparent) {
            boolean normalsWithNoNaN = normals != null;
            if (normals != null) {
              // Check there's no NaN values in normals to avoid issues
              for (float val : normals) {
                if (Float.isNaN(val)) {
                  normalsWithNoNaN = false;
                  break;
                }
              }
            }

            startGeometry();
            startTriMesh(-1, vertices.length / 3, verticesIndices.length / 3, false, uvs != null, 0, 0);
            addTriangles(vertices, normals, uvs, verticesIndices, verticesIndices, appearanceName);
            endTriMesh();
            endGeometry();

            if (!normalsWithNoNaN) {
              // Generate missing normals
              startGeometry();
              smoothMesh(0, 90f);
              endGeometry();
            }
            return new String [] {objectNameBase};
          }
        }
      }
    }
    return null;
  }

  /**
   * Applies to <code>vertex</code> the given transformation, and stores it in <code>vertices</code>.
   */
  @Override
  void exportVertex(Transform3D transformationToParent,
                    Point3f vertex, int index,
                    float [] vertices) {
    transformationToParent.transform(vertex);
    index *= 3;
    // Caution : Z-up Y-back in YafaRay
    vertices [index++] = vertex.x;
    vertices [index++] = -vertex.z;
    vertices [index] = vertex.y;
  }

  /**
   * Applies to <code>normal</code> the given transformation, and stores it in <code>normals</code>.
   */
  @Override
  void exportNormal(Transform3D transformationToParent,
                    Vector3f normal, int index,
                    float [] normals,
                    boolean backFaceNormalFlip) {
    if (backFaceNormalFlip) {
      normal.negate();
    }
    transformationToParent.transform(normal);

    int i = index * 3;
    // Caution : Z-up Y-back in YafaRay
    normals [i++] = normal.x;
    normals [i++] = -normal.z;
    normals [i] = normal.y;
  }

  /**
   * Exports a Java3D appearance as a YafaRay shader.
   */
  private void exportAppearance(Appearance appearance,
                                String appearanceName,
                                boolean mirror,
                                boolean ignoreTransparency,
                                boolean silk,
                                float lightPower) throws IOException {
    HashMap<String, Object> params = new HashMap<String, Object>();
    Texture texture = appearance.getTexture();
    if (mirror) {
      params.put("type", "shinydiffusemat");
      Material material = appearance.getMaterial();
      Color3f color = new Color3f(0.9f, 0.9f, 0.9f);
      if (material != null) {
        material.getDiffuseColor(color);
      }
      params.put("mirror_color", new float [] {color.x, color.y, color.z, 1});
      params.put("specular_reflect", 1f);
      createMaterial(appearanceName, params, new ArrayList<Map<String, Object>>());
    } else if (texture != null) {
      // Check shape transparency
      TransparencyAttributes transparencyAttributes = appearance.getTransparencyAttributes();
      float transparency;
      if (transparencyAttributes != null
          && transparencyAttributes.getTransparency() > 0
          && !ignoreTransparency) {
        transparency = 1 - transparencyAttributes.getTransparency();
      } else {
        transparency = 1;
      }

      TransparentTextureKey key = new TransparentTextureKey(texture, transparency);
      String textureName = this.texturesCache.get(key);
      if (textureName == null) {
        textureName = "texture-" + appearanceName;
        BufferedImage transferredImage;
        if (texture.getUserData() instanceof URL && transparency == 1) {
          URL userData = (URL)texture.getUserData();
          URLContent textureContent = new URLContent(userData);
          InputStream in = null;
          try {
            in = textureContent.openStream();
            transferredImage = ImageIO.read(in);
            if (transferredImage.getTransparency() != BufferedImage.OPAQUE) {
              textureName = "transparent-" + textureName;
            }
          } finally {
            if (in != null) {
              in.close();
            }
          }
          if (transferredImage.getType() != BufferedImage.TYPE_4BYTE_ABGR
              && transferredImage.getType() != BufferedImage.TYPE_3BYTE_BGR
              && transferredImage.getType() != BufferedImage.TYPE_BYTE_GRAY) {
            // Convert to raster supported by createTextureFromImageData
            BufferedImage convertedImage = new BufferedImage(transferredImage.getWidth(), transferredImage.getHeight(),
                transferredImage.getTransparency() != BufferedImage.OPAQUE  ? BufferedImage.TYPE_4BYTE_ABGR  : BufferedImage.TYPE_3BYTE_BGR);
            Graphics2D g2D = (Graphics2D)convertedImage.getGraphics();
            g2D.drawRenderedImage(transferredImage, null);
            g2D.dispose();
            transferredImage = convertedImage;
          }
        } else {
          ImageComponent2D imageComponent = (ImageComponent2D)texture.getImage(0);
          RenderedImage image = imageComponent.getRenderedImage();
          // Compute a partially transparent image
          transferredImage = new BufferedImage(image.getWidth(), image.getHeight(),
              transparency < 1 || image.getColorModel().hasAlpha() ? BufferedImage.TYPE_4BYTE_ABGR : BufferedImage.TYPE_3BYTE_BGR);
          Graphics2D g2D = (Graphics2D)transferredImage.getGraphics();
          if (transparency < 1) {
            g2D.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, transparency));
          }
          g2D.drawRenderedImage(image, null);
          g2D.dispose();
          if (transferredImage.getColorModel().hasAlpha()) {
            textureName = "transparent-" + textureName;
          }
        }

        params.put("type", "image");
        params.put("color_space", "sRGB");
        params.put("width", transferredImage.getWidth());
        params.put("height", transferredImage.getHeight());
        byte [] imageData = ((DataBufferByte)transferredImage.getRaster().getDataBuffer()).getData();
        params.put("channels", imageData.length / (transferredImage.getWidth() * transferredImage.getHeight()));

        checkCurrentThreadIsntInterrupted();

        createTexture(textureName, imageData, params);

        this.texturesCache.put(key, textureName);
      }

      params.clear();
      params.put("type", "shinydiffusemat");

      boolean transparentTexture = textureName.startsWith("transparent-");
      Material material = appearance.getMaterial();
      float shininess;
      String textureMaterialName = appearanceName;
      if (material != null
          && (shininess = material.getShininess()) > 1
          && !transparentTexture) {
        if (silk) {
          HashMap<String, Object> glossyParams = new HashMap<String, Object>();
          shininess /= 128f;
          glossyParams.put("type", "glossy");
          glossyParams.put("glossy_reflect", shininess);
          createMaterial(appearanceName + "-glossy", glossyParams, new ArrayList<Map<String,Object>>());
          textureMaterialName += "-diffuse";
        } else {
          shininess /= 512f;
          params.put("specular_reflect", shininess);
          params.put("mirror_color", new float [] {shininess, shininess, shininess, 1});
        }
      }

      params.put("diffuse_shader", "diffuse_layer");
      Map<String, Object> textureMapLayerParams = new HashMap<String, Object>();
      textureMapLayerParams.put("element", "shader_node");
      textureMapLayerParams.put("name", "diffuse_layer");
      textureMapLayerParams.put("type", "layer");
      textureMapLayerParams.put("input", "textureMap");
      Map<String, Object> textureMapTextureMapperParams = new HashMap<String, Object>();
      textureMapTextureMapperParams.put("element", "shader_node");
      textureMapTextureMapperParams.put("name", "textureMap");
      textureMapTextureMapperParams.put("type", "texture_mapper");
      textureMapTextureMapperParams.put("texco", "uv");
      textureMapTextureMapperParams.put("texture", textureName);
      if (transparentTexture) {
        params.put("transparency_shader", "transparency_layer");
        textureMapLayerParams.put("upper_color", new float [] {1, 1, 1, 1});
        Map<String, Object> transparencyMapLayerParams = new HashMap<String, Object>();
        transparencyMapLayerParams.put("element", "shader_node");
        transparencyMapLayerParams.put("name", "transparency_layer");
        transparencyMapLayerParams.put("type", "layer");
        transparencyMapLayerParams.put("input", "transparencyMap");
        transparencyMapLayerParams.put("negative", true);
        transparencyMapLayerParams.put("use_alpha", true);
        transparencyMapLayerParams.put("do_scalar", true);
        Map<String, Object> transparencyMapTextureMapperParams = new HashMap<String, Object>();
        transparencyMapTextureMapperParams.put("element", "shader_node");
        transparencyMapTextureMapperParams.put("name", "transparencyMap");
        transparencyMapTextureMapperParams.put("type", "texture_mapper");
        transparencyMapTextureMapperParams.put("texco", "uv");
        transparencyMapTextureMapperParams.put("texture", textureName);
        createMaterial(textureMaterialName, params, Arrays.asList(textureMapLayerParams, textureMapTextureMapperParams, transparencyMapLayerParams, transparencyMapTextureMapperParams));
      } else {
        createMaterial(textureMaterialName, params, Arrays.asList(textureMapLayerParams, textureMapTextureMapperParams));
      }

      if (!appearanceName.equals(textureMaterialName)) {
        // Blend texture with glossy material
        HashMap<String, Object> blendParams = new HashMap<String, Object>();
        blendParams.put("type", "blend_mat");
        blendParams.put("material1", appearanceName + "-diffuse");
        blendParams.put("material2", appearanceName + "-glossy");
        createMaterial(appearanceName, blendParams, new ArrayList<Map<String,Object>>());
      }
    } else {
      Material material = appearance.getMaterial();
      if (material != null) {
        Color3f color = new Color3f();
        material.getDiffuseColor(color);
        TransparencyAttributes transparencyAttributes = appearance.getTransparencyAttributes();
        if (transparencyAttributes != null
            && transparencyAttributes.getTransparency() > 0
            && !ignoreTransparency) {
          if (material instanceof OBJMaterial
              && ((OBJMaterial)material).isOpticalDensitySet()) {
            float opticalDensity = ((OBJMaterial)material).getOpticalDensity();
            // To avoid rendering issues, use glass ETA for optical density equal to 1
            // (i.e. the index of refraction of vacuum that has no meaning for furniture parts)
            params.put("IOR", opticalDensity <= 1f ?  1.55f  : opticalDensity);
          } else {
            // Use glass ETA as default
            params.put("IOR", 1.55f);
          }
          float transparency = 1 - transparencyAttributes.getTransparency();
          params.put("type", "glass");
          params.put("fake_shadows", true);
          params.put("filter_color", new float [] {color.x, color.y, color.z, 1});
          params.put("transmit_filter", transparency);
        } else if (material.getLightingEnable()) {
          float shininess = material.getShininess();
          if (shininess > 1) {
            if (silk) {
              params.put("diffuse_color", new float [] {color.x, color.y, color.z, 1});
              params.put("diffuse_reflect", 1.5f);
              material.getSpecularColor(color);
              params.put("color", new float [] {color.x, color.y, color.z, 1});
              params.put("glossy_reflect", shininess / 256f);
              params.put("exponent", 100f);
              params.put("type", "glossy");
            } else {
              params.put("color", new float [] {color.x, color.y, color.z, 1});
              params.put("specular_reflect", shininess / 512f);
              params.put("mirror_color", new float [] {color.x, color.y, color.z, 1});
              params.put("type", "shinydiffusemat");
            }
          } else {
            params.put("color", new float [] {color.x, color.y, color.z, 1});
            params.put("type", "shinydiffusemat");
          }
        } else {
          params.put("type", "light_mat");
          params.put("color", new float [] {color.x, color.y, color.z, 1});
          if (lightPower >= 0) {
            params.put("power", lightPower * 100);
          }
        }
        createMaterial(appearanceName, params, new ArrayList<Map<String, Object>>());
      } else {
        params.put("type", "light_mat");
        ColoringAttributes coloringAttributes = appearance.getColoringAttributes();
        if (coloringAttributes != null) {
          Color3f color = new Color3f();
          coloringAttributes.getColor(color);
          params.put("color", new float [] {color.x, color.y, color.z, 1});
        } else {
          params.put("color", new float [] {0, 0, 0, 1});
        }
        if (lightPower >= 0) {
          params.put("power", lightPower * 100);
        }
        createMaterial(appearanceName, params, new ArrayList<Map<String, Object>>());
      }
    }
  }

  /**
   * Exports the given light sources as YafaRay lights placed at the right location
   * with <code>lightTransform</code>.
   */
  private void exportLightSources(HomeLight light, Transform3D lightTransform) {
    for (LightSource lightSource : light.getLightSources()) {
      exportLightSource(light, lightSource, lightTransform);
    }
  }

  /**
   * Exports the given light source as YafaRay light placed at the right location
   * with <code>lightTransform</code>.
   */
  private void exportLightSource(HomeLight light, LightSource lightSource, Transform3D lightTransform) {
    float lightPower = light.getPower();
    float lightRadius = getLightSourceRadius(light, lightSource);
    float power = 5 * lightPower * lightPower / (lightRadius * lightRadius);
    int lightColor = lightSource.getColor();
    HashMap<String, Object> params = new HashMap<String, Object>();
    params.put("type", "spherelight");
    params.put("color", new float [] {
        (lightColor >> 16) * (this.homeLightColor >> 16),
        ((lightColor >> 8) & 0xFF) * ((this.homeLightColor >> 8) & 0xFF),
        (lightColor & 0xFF) * (this.homeLightColor & 0xFF), 1});
    params.put("power", power);
    Point3f lightSourceLocation = getNormalizedLightSourceLocation(lightSource);
    lightTransform.transform(lightSourceLocation);
    params.put("from", new float [] {lightSourceLocation.getX(),
        -lightSourceLocation.getZ(),
        lightSourceLocation.getY()});
    params.put("radius", lightRadius);
    params.put("samples", 4);
    createLight(UUID.randomUUID().toString(), params);
  }

  private float getLightSourceRadius(HomeLight light, LightSource lightSource) {
    return lightSource.getDiameter() != null
        ? lightSource.getDiameter() * light.getWidth() / 2
        : 3.25f; // Default radius compatible with most lights available before version 3.0
  }

  private Point3f getNormalizedLightSourceLocation(LightSource lightSource) {
    return new Point3f(lightSource.getX() - 0.5f, lightSource.getZ() - 0.5f, 0.5f - lightSource.getY());
  }

  /**
   * An image output that updates an existing image.
   */
  private static final class BufferedImageOutput implements ImageOutput {
    private static final int BASE_INFO_FLAGS = ImageObserver.WIDTH | ImageObserver.HEIGHT | ImageObserver.PROPERTIES;
    private static final int [] BORDERS = {Color.RED.getRGB(), Color.GREEN.getRGB(), Color.BLUE.getRGB(),
                                           Color.YELLOW.getRGB(), Color.CYAN.getRGB(), Color.MAGENTA.getRGB(),
                                           new Color(1, 0.5f, 0).getRGB(), new Color(0.5f, 1, 0).getRGB()};
    private final ImageObserver observer;
    private final BufferedImage image;
    private int areaNumber;

    private BufferedImageOutput(ImageObserver observer, BufferedImage image) {
      this.observer = observer;
      this.image = image;
    }

    public void setPixel(int x, int y, float r, float g, float b, float a, boolean hasAlpha) {
      this.image.setRGB(x, y, new Color(Math.min(1, r), Math.min(1, g), Math.min(1, b), a).getRGB());
    }

    public void flushArea(int x0, int y0, int x1, int y1) {
      notifyObserver(ImageObserver.SOMEBITS | BASE_INFO_FLAGS, x0, y0, x1 - x0, y1 - y0);
    }

    public void flush() {
      notifyObserver(ImageObserver.FRAMEBITS | BASE_INFO_FLAGS, 0, 0, this.image.getWidth(), this.image.getHeight());
    }

    public void highlightArea(int x0, int y0, int x1, int y1) {
      int width = x1 - x0;
      int height = y1 - y0;
      int border = BORDERS [areaNumber++ % BORDERS.length] | 0xFF000000;
      for (int by = 0; by < height; by++) {
        for (int bx = 0; bx < width; bx++) {
          if (bx < 2 || bx > width - 3) {
            if (5 * by < height || 5 * (height - by - 1) < height) {
              this.image.setRGB(x0 + bx, y0 + by, border);
            }
          } else if (by < 2 || by > height - 3) {
            if (5 * bx < width || 5 * (width - bx - 1) < width) {
              this.image.setRGB(x0 + bx, y0 + by, border);
            }
          }
        }
      }
      notifyObserver(ImageObserver.SOMEBITS | BASE_INFO_FLAGS, x0, y0, width, height);
    }

    private void notifyObserver(final int flags, final int x, final int y, final int width, final int height) {
      if (this.observer != null) {
        EventQueue.invokeLater(new Runnable() {
            public void run() {
              observer.imageUpdate(image, flags, x, y, width, height);
            }
          });
      }
    }
  }

  // Native methods declarations which call YafaRay DLLs
  private native long createEnvironment(String pluginsPath, String logLevel); // See logging.cc for logLevel

  private native void createMaterial(String name, Map<String, Object> params, List<Map<String, Object>> eparams);

  private native void createTexture(String name, byte [] imageData, Map<String, Object> params);

  private native void createLight(String name, Map<String, Object> params);

  private native boolean deleteLight(String name);

  private native void createBackground(String name, Map<String, Object> params);

  private native boolean deleteBackground(String name);

  private native void createCamera(String name, Map<String, Object> params);

  private native boolean deleteCamera(String name);

  private native void createIntegrator(String name, Map<String, Object> params);

  private native boolean deleteIntegrator(String name);

  private native long createScene();

  private native void abort();

  private native void clearAll();

  private native void startGeometry();

  private native void endGeometry();

  private native long startTriMesh(long id, int vertices, int triangles, boolean hasOrco, boolean hasUv, int type, int objectPassIndex);

  private native void endTriMesh();

  private native void addVertex(float x, float y, float z);

  private native void addNormal(float x, float y, float z);

  private native void addUV(float u, float v);

  private native void addTriangle(int a, int b, int c, String materialName);

  private native void addTriangle(int a, int b, int c, int uva, int uvb, int uvc, String materialName);

  private native void addTriangles(float [] vertices, float [] normals, float uvs [], int [] triangleVertices, int [] triangleUvs, String materialName);

  private native void smoothMesh(long id, float angleInDegree);

  private native void render(ImageOutput imageOutput, Map<String, Object> params);

  protected native void finalize();

  private interface ImageOutput {
    void setPixel(int x, int y, float r, float g, float b, float a, boolean hasAlpha);

    void flushArea(int x0, int y0, int x1, int y1);

    void flush();

    void highlightArea(int x0, int y0, int x1, int y1);
  }
}
