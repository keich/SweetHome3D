/*
 * SweetHome3DBootstrap.java 2 sept. 07
 *
 * Sweet Home 3D, Copyright (c) 2007 Emmanuel PUYBARET / eTeks <info@eteks.com>
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
package com.eteks.sweethome3d;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Array;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.List;
import java.util.Locale;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import javax.swing.JOptionPane;

import com.eteks.sweethome3d.tools.ExtensionsClassLoader;

/**
 * This bootstrap class loads Sweet Home 3D application classes from jars in classpath
 * or from extension jars stored as resources.
 * @author Emmanuel Puybaret
 */
public class SweetHome3DBootstrap {
  public static void main(String [] args) throws MalformedURLException, IllegalAccessException,
        InvocationTargetException, NoSuchMethodException, ClassNotFoundException {
    Class<?> sweetHome3DBootstrapClass = SweetHome3DBootstrap.class;
    List<String> extensionJarsAndDlls = new ArrayList<String>(Arrays.asList(new String [] {
        "batik-svgpathparser-1.7.jar", // Jars included in Sweet Home 3D executable jar file
        "jeksparser-calculator.jar",
        "iText-2.1.7.jar",
        "freehep-vectorgraphics-svg-2.1.1c.jar",
        "sunflow-0.07.3i.jar",
        "jmf.jar",
        "jnlp.jar"}));
    String yafarayPluginsFolder = null;
    List<String> yafarayWindowsDlls = new ArrayList<String>();

    String operatingSystemName = System.getProperty("os.name");
    String operatingSystemVersion = System.getProperty("os.version");
    String javaVersion = System.getProperty("java.version");
    if (operatingSystemName.startsWith("Mac OS X")) {
      boolean macOSXLionOrInferior = operatingSystemVersion.startsWith("10.4")
          || operatingSystemVersion.startsWith("10.5")
          || operatingSystemVersion.startsWith("10.6")
          || operatingSystemVersion.startsWith("10.7");
      if (javaVersion.startsWith("1.6")
          && System.getProperty("com.eteks.sweethome3d.deploymentInformation", "").startsWith("Java Web Start")) {
        // Refuse to let Sweet Home 3D run under Mac OS X with Java Web Start 6
        String message = Locale.getDefault().getLanguage().equals(Locale.FRENCH.getLanguage())
            ? "Sweet Home 3D ne peut pas fonctionner avec Java\n"
              + "Web Start 6 sous Mac OS X de façon fiable.\n"
              + "Merci de télécharger le programme d'installation depuis\n"
              + "http://www.sweethome3d.com/fr/download.jsp"
            : "Sweet Home 3D can't reliably run with Java Web Start 6\n"
              + "under Mac OS X.\n"
              + "Please download the installer version from\n"
              + "http://www.sweethome3d.com/download.jsp";
        JOptionPane.showMessageDialog(null, message);
        System.exit(1);
      } else if ((javaVersion.startsWith("1.5")
                  || javaVersion.startsWith("1.6"))
              && (macOSXLionOrInferior
                  || operatingSystemVersion.startsWith("10.8"))) {
        extensionJarsAndDlls.addAll(Arrays.asList(new String [] {
            "j3dcore.jar", // Main Java 3D jars
            "vecmath.jar",
            "j3dutils.jar",
            "macosx/gluegen-rt.jar", // Mac OS X jars and DLLs for Java 5 or 6
            "macosx/jogl.jar",
            "macosx/libgluegen-rt.jnilib",
            "macosx/libjogl.jnilib",
            "macosx/libjogl_awt.jnilib",
            "macosx/libjogl_cg.jnilib"}));
      } else if (javaVersion.startsWith("1.6")
                 || javaVersion.startsWith("1.7")
                 || macOSXLionOrInferior) {
        // Refuse to let Sweet Home 3D run under Mac OS X with Java 7
        String message = Locale.getDefault().getLanguage().equals(Locale.FRENCH.getLanguage())
            ? "Sweet Home 3D ne peut fonctionner avec Java 6/7 sur votre\n"
              + "système et requiert Java 8 ou plus. Merci de mettre à jour votre\n"
              + "version de Java ou de télécharger le programme d'installation\n"
              + "depuis http://www.sweethome3d.com/fr/download.jsp"
            : "Sweet Home 3D can't run with Java 6/7 under your system\n"
              + "and requires Java 8 or above. Please, update you Java version\n"
              + "or download the installer version from\n"
              + "http://www.sweethome3d.com/download.jsp";
        JOptionPane.showMessageDialog(null, message);
        System.exit(1);
      } else { // Java > 1.7
        extensionJarsAndDlls.addAll(Arrays.asList(new String [] {
            "java3d-1.6/j3dcore.jar", // Mac OS X Java 3D 1.6 jars and DLLs
            "java3d-1.6/vecmath.jar",
            "java3d-1.6/j3dutils.jar",
            "java3d-1.6/gluegen-rt.jar",
            "java3d-1.6/jogl-java3d.jar",
            "java3d-1.6/macosx/libgluegen_rt.dylib",
            "java3d-1.6/macosx/libjogl_desktop.dylib",
            "java3d-1.6/macosx/libnativewindow_awt.dylib",
            "java3d-1.6/macosx/libnativewindow_macosx.dylib"}));
        // Disable JOGL library loader
        System.setProperty("jogamp.gluegen.UseTempJarCache", "false");
      }

      // Yafaray DLLs for Mac OS X
      if ("64".equals(System.getProperty("sun.arch.data.model"))) {
        extensionJarsAndDlls.addAll(Arrays.asList(new String [] {
            "yafaray/macosx/libyafaray_v3_core.dylib",
            "yafaray/macosx/libyafarayjni.dylib"}));
        yafarayPluginsFolder = "yafaray/macosx/yafaray-plugins";
      }
    } else { // Other OS
      if ("1.5.2".equals(System.getProperty("com.eteks.sweethome3d.j3d.version", "1.6"))
          || "d3d".equals(System.getProperty("j3d.rend", "jogl"))
          || javaVersion.startsWith("1.5")
          || javaVersion.startsWith("1.6")
          || javaVersion.startsWith("1.7")) {
        extensionJarsAndDlls.addAll(Arrays.asList(new String [] {
            "j3dcore.jar", // Main Java 3D jars
            "vecmath.jar",
            "j3dutils.jar"}));
        if ("64".equals(System.getProperty("sun.arch.data.model"))) {
          extensionJarsAndDlls.addAll(Arrays.asList(new String [] {
              "linux/x64/libj3dcore-ogl.so",    // Linux 64 bits DLL for Java 3D 1.5.2
              "windows/x64/j3dcore-ogl.dll"})); // Windows 64 bits DLL for Java 3D 1.5.2
        } else {
          extensionJarsAndDlls.addAll(Arrays.asList(new String [] {
              "linux/i386/libj3dcore-ogl.so", // Linux 32 bits DLLs
              "linux/i386/libj3dcore-ogl-cg.so",
              "windows/i386/j3dcore-d3d.dll", // Windows 32 bits DLLs
              "windows/i386/j3dcore-ogl.dll",
              "windows/i386/j3dcore-ogl-cg.dll",
              "windows/i386/j3dcore-ogl-chk.dll"}));
        }
      } else {
        extensionJarsAndDlls.addAll(Arrays.asList(new String [] {
            "java3d-1.6/j3dcore.jar", // Java 3D 1.6 jars
            "java3d-1.6/vecmath.jar",
            "java3d-1.6/j3dutils.jar"}));
        if ("64".equals(System.getProperty("sun.arch.data.model"))) {
          extensionJarsAndDlls.addAll(Arrays.asList(new String [] {
            "java3d-1.6/gluegen-rt.jar",
            "java3d-1.6/jogl-java3d.jar"}));
        } else {
          extensionJarsAndDlls.addAll(Arrays.asList(new String [] {
            "java3d-1.6/i586/gluegen-rt.jar",
            "java3d-1.6/i586/jogl-java3d.jar"}));
        }
        // Disable JOGL library loader
        System.setProperty("jogamp.gluegen.UseTempJarCache", "false");
        if ("64".equals(System.getProperty("sun.arch.data.model"))) {
          extensionJarsAndDlls.addAll(Arrays.asList(new String [] {
              "java3d-1.6/linux/amd64/libgluegen_rt.so", // Linux 64 bits DLLs for Java 3D 1.6
              "java3d-1.6/linux/amd64/libjogl_desktop.so",
              "java3d-1.6/linux/amd64/libnativewindow_awt.so",
              "java3d-1.6/linux/amd64/libnativewindow_x11.so",
              "java3d-1.6/windows/amd64/gluegen_rt.dll", // Windows 64 bits DLLs for Java 3D 1.6
              "java3d-1.6/windows/amd64/jogl_desktop.dll",
              "java3d-1.6/windows/amd64/nativewindow_awt.dll",
              "java3d-1.6/windows/amd64/nativewindow_win32.dll"}));
        } else {
          extensionJarsAndDlls.addAll(Arrays.asList(new String [] {
              "java3d-1.6/linux/i586/libgluegen_rt.so", // Linux 32 bits DLLs for Java 3D 1.6
              "java3d-1.6/linux/i586/libjogl_desktop.so",
              "java3d-1.6/linux/i586/libnativewindow_awt.so",
              "java3d-1.6/linux/i586/libnativewindow_x11.so",
              "java3d-1.6/windows/i586/gluegen_rt.dll", // Windows 32 bits DLLs for Java 3D 1.6
              "java3d-1.6/windows/i586/jogl_desktop.dll",
              "java3d-1.6/windows/i586/nativewindow_awt.dll",
              "java3d-1.6/windows/i586/nativewindow_win32.dll"}));
        }
      }

      if (operatingSystemName.startsWith("Windows")) {
        // Yafaray DLLs for Windows
        if ("64".equals(System.getProperty("sun.arch.data.model"))) {
          // YafaRay Windows DLLs are managed differently to be loaded by System#load method
          yafarayWindowsDlls.addAll(Arrays.asList(new String [] {
              "yafaray/windows/x64/libgcc_s_seh-1.dll",
              "yafaray/windows/x64/libstdc++-6.dll",
              "yafaray/windows/x64/libwinpthread-1.dll",
              "yafaray/windows/x64/libyafaray_v3_core.dll",
              "yafaray/windows/x64/libyafarayjni.dll"}));
          yafarayPluginsFolder = "yafaray/windows/x64/yafaray-plugins";
        } else {
          yafarayWindowsDlls.addAll(Arrays.asList(new String [] {
              "yafaray/windows/i386/libgcc_s_dw2-1.dll",
              "yafaray/windows/i386/libstdc++-6.dll",
              "yafaray/windows/i386/libwinpthread-1.dll",
              "yafaray/windows/i386/libyafaray_v3_core.dll",
              "yafaray/windows/i386/libyafarayjni.dll"}));
          yafarayPluginsFolder = "yafaray/windows/i386/yafaray-plugins";
        }
      } else if (operatingSystemName.startsWith("Linux")) {
        // Yafaray DLLs for Linux
        if ("64".equals(System.getProperty("sun.arch.data.model"))) {
          extensionJarsAndDlls.addAll(Arrays.asList(new String [] {
              "yafaray/linux/x64/libyafaray_v3_core.so",
              "yafaray/linux/x64/libyafarayjni.so"}));
          yafarayPluginsFolder = "yafaray/linux/x64/yafaray-plugins";
        } else {
          extensionJarsAndDlls.addAll(Arrays.asList(new String [] {
              "yafaray/linux/i386/libyafaray_v3_core.so",
              "yafaray/linux/i386/libyafarayjni.so"}));
          yafarayPluginsFolder = "yafaray/linux/i386/yafaray-plugins";
        }
      }
    }

    String [] applicationPackages = {
        "com.eteks.sweethome3d",
        "javax.media",
        "javax.vecmath",
        "com.sun.j3d",
        "com.sun.opengl",
        "com.sun.gluegen.runtime",
        "com.jogamp",
        "jogamp",
        "javax.media.opengl",
        "javax.media.nativewindow",
        "com.sun.media",
        "com.ibm.media",
        "jmpapps.util",
        "org.sunflow",
        "org.apache.batik",
        "com.eteks.parser"};
    String applicationClassName = "com.eteks.sweethome3d.SweetHome3D";
    File cacheFolder = new File(System.getProperty("java.io.tmpdir"));
    String cachedFilesPrefix = applicationClassName + "-cache-";
    ClassLoader java3DClassLoader;
    if (operatingSystemName.startsWith("Windows")) {
      java3DClassLoader = new ExtensionsClassLoader(
          sweetHome3DBootstrapClass.getClassLoader(),
          sweetHome3DBootstrapClass.getProtectionDomain(),
          extensionJarsAndDlls.toArray(new String [extensionJarsAndDlls.size()]), null, applicationPackages,
          // Use cache under Windows because temporary files tagged as deleteOnExit can't
          // be deleted if they are still opened when program exits
          cacheFolder, cachedFilesPrefix);
    } else {
      java3DClassLoader = new ExtensionsClassLoader(
          sweetHome3DBootstrapClass.getClassLoader(),
          sweetHome3DBootstrapClass.getProtectionDomain(),
          extensionJarsAndDlls.toArray(new String [extensionJarsAndDlls.size()]), applicationPackages);
    }

    if (yafarayPluginsFolder != null) {
      try {
        String jarFile = sweetHome3DBootstrapClass.getResource(sweetHome3DBootstrapClass.getSimpleName() + ".class").getFile();
        URL applicationJarUrl = new URL(jarFile.substring(0, jarFile.indexOf("!/")));
        if ("file".equals(applicationJarUrl.getProtocol())) {
          File applicationJar = new File(applicationJarUrl.toURI());
          long applicationJarDate = applicationJar.lastModified();
          long applicationJarLength = applicationJar.length();
          File yafarayCacheFolder = null;
          String pluginDllsFolder = "yafaray-plugins";
          // Create a temporary folder for YafaRay plugins
          if (operatingSystemName.startsWith("Windows") && applicationJarDate != 0 && applicationJarLength != 0) {
            yafarayCacheFolder = new File(cacheFolder, cachedFilesPrefix + "yafaray-" + System.getProperty("sun.arch.data.model") + "-" + applicationJarLength + "-" + (applicationJarDate / 1000L));
            if (!yafarayCacheFolder.exists()
                && !yafarayCacheFolder.mkdirs()) {
              yafarayCacheFolder = null;
            }
          } else {
            yafarayCacheFolder = File.createTempFile("yafaray", "tmp");
            yafarayCacheFolder.delete();
            if (!yafarayCacheFolder.mkdirs()) {
              yafarayCacheFolder = null;
            }
          }
          if (yafarayCacheFolder != null) {
            yafarayCacheFolder.deleteOnExit();
            File yafarayPluginDllsFolder = new File(yafarayCacheFolder, pluginDllsFolder);
            System.setProperty("com.eteks.sweethome3d.j3d.YafarayPluginsFolder", yafarayPluginDllsFolder.getAbsolutePath());
            if (yafarayPluginDllsFolder.exists()
                || yafarayPluginDllsFolder.mkdirs()) {
              yafarayPluginDllsFolder.deleteOnExit();
              // Copy plug-in DLLs
              for (Enumeration<? extends ZipEntry> entryEnum = new ZipFile(applicationJar).entries(); entryEnum.hasMoreElements(); ) {
                ZipEntry entry = entryEnum.nextElement();
                if (!entry.isDirectory() && entry.getName().contains(yafarayPluginsFolder)) {
                  copyFileToFolder(sweetHome3DBootstrapClass.getResource("/" + entry.getName()), yafarayPluginDllsFolder);
                }
              }
              for (String yafarayDll : yafarayWindowsDlls) {
                copyFileToFolder(sweetHome3DBootstrapClass.getResource("/" + yafarayDll), yafarayCacheFolder);
              }
            }
          } else {
            System.err.println("Couldn't extract YafaRay library");
          }
        }
      } catch (URISyntaxException ex) {
        ex.printStackTrace();
      } catch (IOException ex) {
        ex.printStackTrace();
      }
    }
    Class<?> applicationClass = java3DClassLoader.loadClass(applicationClassName);
    Method applicationClassMain =
        applicationClass.getMethod("main", Array.newInstance(String.class, 0).getClass());
    // Call application class main method with reflection
    applicationClassMain.invoke(null, new Object [] {args});
  }

  private static String copyFileToFolder(URL url, File folder) throws IOException, URISyntaxException {
    String file = url.getFile();
    File copy = new File(folder, file.substring(file.lastIndexOf('/'), file.length()));
    copy.deleteOnExit();
    if (!copy.exists()) {
      InputStream tempIn = null;
      OutputStream tempOut = null;
      try {
        tempIn = url.openStream();
        tempOut = new FileOutputStream(copy);
        byte [] buffer = new byte [8192];
        int size;
        while ((size = tempIn.read(buffer)) != -1) {
          tempOut.write(buffer, 0, size);
        }
      } finally {
        if (tempIn != null) {
          tempIn.close();
        }
        if (tempOut != null) {
          tempOut.close();
        }
      }
    }
    return copy.getAbsolutePath();
  }
}