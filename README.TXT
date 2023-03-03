SWEET HOME 3D v 7.1
-------------------

This archive contains source code of Sweet Home 3D, an interior design Java application.
Please read http://www.sweethome3d.com/history.html for more information
about new features available in this version.

You may also download this source code with the following SVN command:

svn checkout https://svn.code.sf.net/p/sweethome3d/code/tags/V_7_1/SweetHome3D SweetHome3D


HOW TO USE THIS SOURCE CODE
---------------------------

Source code classes are compatible with Java SE 5, and the directory where
you find this file contains all the information required to compile source code
as an Eclipse project or with Ant.

If you want to compile and test the Java files of this release 
with Eclipse, please follow the next steps:
1. install JDK or OpenJDK >= 5 available at http://java.sun.com and elsewhere 
2. install Eclipse available at http://www.eclipse.org
3. in Eclipse, select File > Import... menu
4. In "Select an import source" list of Import dialog box, 
   choose "Existing project into Workspace" option
5. click on Next button
6. in "Select root directory" text field, 
   type or select the path to the root of this archive
7. click on Finish 

You may execute applications and tests of this archive, with the Eclipse Run menu.
To execute applications and tests that require Java 3D, open Run Dialog in Eclipse, 
select the Arguments tab of the target application, and add to the VM arguments 
text field the option:
-Djava.library.path=lib/java3d-1.6/windows/i586;lib/yafaray/windows/i386
-Djava.library.path=lib/java3d-1.6/windows/amd64;lib/yafaray/windows/x64
-Djava.library.path=lib/java3d-1.6/macosx:lib/yafaray/macosx
-Djava.library.path=lib/java3d-1.6/linux/i586:lib/yafaray/linux/i386
or -Djava.library.path=lib/java3d-1.6/linux/amd64:lib/yafaray/linux/x64
according to your system and its architecture.
If you use Java 16 or above, you should add also the following VM arguments 
to run the program:
--add-opens=java.desktop/java.awt=ALL-UNNAMED
--add-opens=java.desktop/sun.awt=ALL-UNNAMED
--add-opens=java.desktop/com.apple.eio=ALL-UNNAMED
--add-opens=java.desktop/com.apple.eawt=ALL-UNNAMED

The last release of Sweet Home 3D can be tested directly as a Java Web Start 
application at http://www.sweethome3d.com/ or as an applet at
http://www.sweethome3d.com/SweetHome3DOnLine.html

If you want to build Sweet Home 3D with Ant, please install last JDK 5 or greater and 
Ant available at http://ant.apache.org/, and run ant command in the directory where
you uncompressed this archive.
The default target provided by build.xml Ant file builds the Jar executable version
in the install subdirectory. Read build.xml Ant file for more information about
other available targets. 
Note that the last part of the Ant targets javaWebStart, applet, viewer and signed installers
requires a passphrase for keystore to sign the generated files. For security reasons, 
the required file keys.p12 isn't provided with this source code. You'll have 
first to generate this file and choose a passphrase, with the following command run 
in the directory where you uncompressed this archive:
  keytool -genkey -keystore keys.p12 -alias SweetHome3D -storetype pkcs12



Read LICENSE.TXT file for more details about licenses applicable to this software
and included materials developed by third parties.
    
    
Sweet Home 3D, Copyright (c) 2005-2023 Emmanuel PUYBARET / eTeks. 
Distributed under GNU General Public License    