/*
 * AbstractPhotoRenderer.java 14 Feb 2022
 *
 * Sweet Home 3D, Copyright (c) 2022 Emmanuel PUYBARET / eTeks <info@eteks.com>
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

import java.awt.image.BufferedImage;
import java.awt.image.ImageObserver;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;
import java.util.Set;

import javax.media.j3d.Bounds;
import javax.media.j3d.GeometryArray;
import javax.media.j3d.Group;
import javax.media.j3d.IndexedGeometryArray;
import javax.media.j3d.Node;
import javax.media.j3d.PolygonAttributes;
import javax.media.j3d.Texture;
import javax.media.j3d.Transform3D;
import javax.media.j3d.TransformGroup;
import javax.vecmath.Matrix4f;
import javax.vecmath.Point3f;
import javax.vecmath.TexCoord2f;
import javax.vecmath.Vector3f;
import javax.vecmath.Vector4f;

import com.eteks.sweethome3d.model.Camera;
import com.eteks.sweethome3d.model.Compass;
import com.eteks.sweethome3d.model.DimensionLine;
import com.eteks.sweethome3d.model.Home;
import com.eteks.sweethome3d.model.ObserverCamera;
import com.eteks.sweethome3d.model.Room;
import com.eteks.sweethome3d.model.Selectable;
import com.eteks.sweethome3d.model.Transformation;
import com.eteks.sweethome3d.viewcontroller.Object3DFactory;

/**
 * A renderer able to create a photo realistic image of a home.
 * @author Emmanuel Puybaret
 * @author Frédéric Mantegazza (Sun location algorithm)
 */
public abstract class AbstractPhotoRenderer {
  public enum Quality {LOW, HIGH}

  private final Quality quality;
  private final Home    home;

  public AbstractPhotoRenderer(Home home, Quality quality) {
    this.home = home;
    this.quality = quality;
  }

  /**
   * Returns an instance of the rendering engine of class <code>renderingEngineClass</code> if available
   * or a default instance of {@link PhotoRenderer} class.
   * @param rendererClassName the name of a renderer class with a constructor taking in parameters
   *            <code>home</code>, <code>object3dFactory</code> and <code>quality</code>.
   */
  public static AbstractPhotoRenderer createInstance(String rendererClassName,
                                                     Home home, Object3DFactory object3dFactory, AbstractPhotoRenderer.Quality quality) {
    if (rendererClassName != null) {
      try {
        Constructor<?> rendererConstructor = Class.forName(rendererClassName).getConstructor(
            Home.class, Object3DFactory.class, AbstractPhotoRenderer.Quality.class);
        AbstractPhotoRenderer renderer = (AbstractPhotoRenderer)rendererConstructor.newInstance(home, object3dFactory, quality);
        if (renderer.isAvailable()) {
          return renderer;
        }
      } catch (Exception ex) {
        // Don't use the required renderer
      }
    }

    try {
      return new PhotoRenderer(home, object3dFactory, quality);
    } catch (IOException ex) {
      throw new RuntimeException(ex); // Shouldn't happen
    }
  }

  public static List<String> getAvailableRenderers() {
    String rendererClassNames = System.getProperty("com.eteks.sweethome3d.j3d.rendererClassNames", PhotoRenderer.class.getName() + "," + YafarayRenderer.class.getName());
    List<String> renderers = new ArrayList<String>();
    for (String rendererClassName : rendererClassNames.split(",")) {
      rendererClassName = rendererClassName.trim();
      if (PhotoRenderer.class.getName().equals(rendererClassName)
          || !PhotoRenderer.class.getName().equals(createInstance(rendererClassName, new Home(), null, Quality.LOW).getClass().getName())) {
        renderers.add(rendererClassName);
      }
    }
    return renderers;
  }

  /**
   * Returns <code>true</code> if this render is able to run in the current environment.
   */
  public boolean isAvailable() {
    return true;
  }

  public abstract String getName();

  /**
   * Returns the rendered home.
   */
  public Home getHome() {
    return this.home;
  }

  /**
   * Returns the quality used to render a home.
   */
  public Quality getQuality() {
    return this.quality;
  }

  /**
   * Renders home in <code>image</code> at the given <code>camera</code> location and image size.
   * The rendered objects of the home are the same ones since last call to render or construction.
   */
  public void render(final BufferedImage image,
                     Camera camera,
                     final ImageObserver observer) throws IOException {
    render(image, camera, null, observer);
  }

  /**
   * Renders home in <code>image</code> at the given <code>camera</code> location and image size.
   * The home objects listed in <code>updatedItems</code> will be updated in the renderer,
   * allowing animations or modifications of their appearance.
   */
  public abstract void render(final BufferedImage image,
                              Camera camera,
                              List<? extends Selectable> updatedItems,
                              final ImageObserver observer) throws IOException;
  /**
   * Stops the rendering process.
   */
  public abstract void stop();

  /**
   * Disposes temporary data that may be required to run this renderer.
   * Trying to use this renderer after a call to this method may lead to errors.
   */
  public abstract void dispose();

  /**
   * Returns the value of the given rendering parameter.
   */
  protected String getRenderingParameterValue(String parameterName) {
    // Try to retrieve overridden parameter value from System property
    // (for example: System property com.eteks.sweethome3d.j3d.PhotoRenderer.lowQuality.antiAliasing.min)
    String prefixedParameter = this.quality.name().toLowerCase(Locale.ENGLISH) + "Quality." + parameterName;
    String baseName = getClass().getName();
    String value = System.getProperty(baseName + '.' + prefixedParameter);
    if (value != null) {
      return value;
    } else {
      // Return default value stored in properties resource file
      // (for example: property lowQuality.antiAliasing.min
      //  in com/eteks/sweethome3d/j3d/PhotoRenderer.properties file)
      return ResourceBundle.getBundle(baseName).getString(prefixedParameter);
    }
  }

  /**
   * Sets the transformations applied to <code>node</code> children.
   */
  void updateModelTransformations(Node node, Transformation[] transformations) {
    for (Transformation transformation : transformations) {
      String transformUserData = transformation.getName() + ModelManager.DEFORMABLE_TRANSFORM_GROUP_SUFFIX;
      updateTransformation(node, transformUserData, transformation.getMatrix());
    }
  }

  /**
   * Sets the transformation matrix of the children which user data is equal to <code>transformGroupUserData</code>.
   */
  private void updateTransformation(Node node, String transformGroupUserData, float[][] matrix) {
    if (node instanceof Group) {
      if (node instanceof TransformGroup
          && transformGroupUserData.equals(node.getUserData())) {
        Matrix4f transformMatrix = new Matrix4f();
        transformMatrix.setRow(0, matrix[0]);
        transformMatrix.setRow(1, matrix[1]);
        transformMatrix.setRow(2, matrix[2]);
        transformMatrix.setRow(3, new float [] {0, 0, 0, 1});
        ((TransformGroup)node).setTransform(new Transform3D(transformMatrix));
      } else {
        Enumeration<?> enumeration = ((Group)node).getAllChildren();
        while (enumeration.hasMoreElements()) {
          updateTransformation((Node)enumeration.nextElement(), transformGroupUserData, matrix);
        }
      }
    }
    // No Link parsing
  }

  /**
   * Returns <code>true</code> if the <code>node<code> or its children with a user data equal to
   * <code>transformGroupUserData</code> intersects with <code>lightBounds</code>.
   */
  boolean intersectsDeformedNode(Node node, Bounds lightBounds,
                                 String transformGroupUserData) {
    if (node instanceof Group) {
      if (node instanceof TransformGroup) {
        if (transformGroupUserData.equals(node.getUserData())
            && ModelManager.getInstance().getBounds(node).intersect(lightBounds)) {
          return true;
        }
      }
      Enumeration<?> enumeration = ((Group)node).getAllChildren();
      while (enumeration.hasMoreElements()) {
        if (intersectsDeformedNode((Node)enumeration.nextElement(), lightBounds, transformGroupUserData)) {
          return true;
        }
      }
    }
    // No Link parsing
    return false;
  }

  /**
   * Returns the transformation applied from <code>node<code> to its child which user data
   * is equal to <code>transformGroupUserData</code>.
   */
  Transform3D getDeformation(Node node, Transform3D parentTransformation,
                             String transformGroupUserData) {
    if (node instanceof Group) {
      if (node instanceof TransformGroup) {
        parentTransformation = new Transform3D(parentTransformation);
        Transform3D transform = new Transform3D();
        ((TransformGroup)node).getTransform(transform);
        parentTransformation.mul(transform);
        if (transformGroupUserData.equals(node.getUserData())) {
          return parentTransformation;
        }
      }
      Enumeration<?> enumeration = ((Group)node).getAllChildren();
      while (enumeration.hasMoreElements()) {
        Transform3D transform = getDeformation((Node)enumeration.nextElement(), parentTransformation,
            transformGroupUserData);
        if (transform != null) {
          return transform;
        }
      }
    }
    // No Link parsing
    return null;
  }

  /**
   * Returns sun direction at a given <code>time</code>.
   * @author Frédéric Mantegazza
   */
  float [] getSunDirection(Compass compass, long time) {
    float elevation = compass.getSunElevation(time);
    float azimuth = compass.getSunAzimuth(time);
    azimuth += compass.getNorthDirection() - Math.PI / 2f;
    return new float [] {(float)(Math.cos(azimuth) * Math.cos(elevation)),
                         (float)Math.sin(elevation),
                         (float)(Math.sin(azimuth) * Math.cos(elevation))};
  }

  /**
   * Returns texture coordinates generated with <code>texCoordGeneration</code> computed
   * as described in <code>TexCoordGeneration</code> javadoc.
   */
  TexCoord2f generateTextureCoordinates(float x, float y, float z,
                                        Vector4f planeS,
                                        Vector4f planeT) {
    return new TexCoord2f(x * planeS.x + y * planeS.y + z * planeS.z + planeS.w,
        x * planeT.x + y * planeT.y + z * planeT.z + planeT.w);
  }

  /**
   * Returns the sum of line integers in <code>stripVertexCount</code> array.
   */
  int getLineCount(int [] stripVertexCount) {
    int lineCount = 0;
    for (int strip = 0; strip < stripVertexCount.length; strip++) {
      lineCount += stripVertexCount [strip] - 1;
    }
    return lineCount;
  }

  /**
   * Returns the sum of triangle integers in <code>stripVertexCount</code> array.
   */
  int getTriangleCount(int [] stripVertexCount) {
    int triangleCount = 0;
    for (int strip = 0; strip < stripVertexCount.length; strip++) {
      triangleCount += stripVertexCount [strip] - 2;
    }
    return triangleCount;
  }

  /**
   * Applies to <code>vertex</code> the given transformation, and stores it in <code>vertices</code>.
   */
  void exportVertex(Transform3D transformationToParent,
                    Point3f vertex, int index,
                    float [] vertices) {
    transformationToParent.transform(vertex);
    index *= 3;
    vertices [index++] = vertex.x;
    vertices [index++] = vertex.y;
    vertices [index] = vertex.z;
  }

  /**
   * Applies to <code>normal</code> the given transformation, and stores it in <code>normals</code>.
   */
  void exportNormal(Transform3D transformationToParent,
                    Vector3f normal, int index,
                    float [] normals,
                    boolean backFaceNormalFlip) {
    if (backFaceNormalFlip) {
      normal.negate();
    }
    transformationToParent.transform(normal);
    int i = index * 3;
    normals [i++] = normal.x;
    normals [i++] = normal.y;
    normals [i] = normal.z;
  }

  /**
   * Stores <code>textureCoordinates</code> in <code>uvs</code>.
   */
  void exportTextureCoordinates(TexCoord2f textureCoordinates,
                                Transform3D textureTransform,
                                int index, float [] uvs) {
    index *= 2;
    if (textureTransform.getBestType() != Transform3D.IDENTITY) {
      Point3f transformedCoordinates = new Point3f(textureCoordinates.x, textureCoordinates.y, 0);
      textureTransform.transform(transformedCoordinates);
      uvs [index++] = transformedCoordinates.x;
      uvs [index] = transformedCoordinates.y;
    } else {
      uvs [index++] = textureCoordinates.x;
      uvs [index] = textureCoordinates.y;
    }
  }

  /**
   * Stores in <code>verticesIndices</code> the indices given at vertexIndex1, vertexIndex2.
   */
  void exportIndexedLine(IndexedGeometryArray geometryArray,
                         int vertexIndex1, int vertexIndex2,
                         int [] verticesIndices,
                         int index) {
    verticesIndices [index++] = geometryArray.getCoordinateIndex(vertexIndex1);
    verticesIndices [index] = geometryArray.getCoordinateIndex(vertexIndex2);
  }

  /**
   * Stores in <code>verticesIndices</code> the indices given at vertexIndex1, vertexIndex2, vertexIndex3.
   */
  int exportIndexedTriangle(IndexedGeometryArray geometryArray,
                            int vertexIndex1, int vertexIndex2, int vertexIndex3,
                            int [] verticesIndices, int [] normalsIndices, int [] textureCoordinatesIndices,
                            int index,
                            float [] vertices,
                            Set<Triangle> exportedTriangles,
                            int cullFace) {
    if (cullFace == PolygonAttributes.CULL_FRONT) {
      // Reverse vertex order
      int tmp = vertexIndex1;
      vertexIndex1 = vertexIndex3;
      vertexIndex3 = tmp;
    }

    int coordinateIndex1 = geometryArray.getCoordinateIndex(vertexIndex1);
    int coordinateIndex2 = geometryArray.getCoordinateIndex(vertexIndex2);
    int coordinateIndex3 = geometryArray.getCoordinateIndex(vertexIndex3);
    Triangle exportedTriangle = new Triangle(vertices, coordinateIndex1, coordinateIndex2, coordinateIndex3);
    if (!exportedTriangles.contains(exportedTriangle)) {
      exportedTriangles.add(exportedTriangle);
      verticesIndices [index] = coordinateIndex1;
      verticesIndices [index + 1] = coordinateIndex2;
      verticesIndices [index + 2] = coordinateIndex3;
      if (normalsIndices != null) {
        normalsIndices [index] = geometryArray.getNormalIndex(vertexIndex1);
        normalsIndices [index + 1] = geometryArray.getNormalIndex(vertexIndex2);
        normalsIndices [index + 2] = geometryArray.getNormalIndex(vertexIndex3);
      }
      if (textureCoordinatesIndices != null) {
        textureCoordinatesIndices [index] = geometryArray.getTextureCoordinateIndex(0, vertexIndex1);
        textureCoordinatesIndices [index + 1] = geometryArray.getTextureCoordinateIndex(0, vertexIndex2);
        textureCoordinatesIndices [index + 2] = geometryArray.getTextureCoordinateIndex(0, vertexIndex3);
      }
      return index + 3;
    }
    return index;
  }

  /**
   * Stores in <code>verticesIndices</code> the indices vertexIndex1 and vertexIndex2.
   */
  void exportLine(GeometryArray geometryArray,
                  int vertexIndex1, int vertexIndex2,
                  int [] verticesIndices, int index) {
    verticesIndices [index++] = vertexIndex1;
    verticesIndices [index] = vertexIndex2;
  }

  /**
   * Stores in <code>verticesIndices</code> the indices vertexIndex1, vertexIndex2, vertexIndex3.
   */
  int exportTriangle(GeometryArray geometryArray,
                     int vertexIndex1, int vertexIndex2, int vertexIndex3,
                     int [] verticesIndices, int index,
                     float [] vertices,
                     Set<Triangle> exportedTriangles,
                     int cullFace) {
    if (cullFace == PolygonAttributes.CULL_FRONT) {
      // Reverse vertex order
      int tmp = vertexIndex1;
      vertexIndex1 = vertexIndex3;
      vertexIndex3 = tmp;
    }

    Triangle exportedTriangle = new Triangle(vertices, vertexIndex1, vertexIndex2, vertexIndex3);
    if (!exportedTriangles.contains(exportedTriangle)) {
      exportedTriangles.add(exportedTriangle);
      verticesIndices [index++] = vertexIndex1;
      verticesIndices [index++] = vertexIndex2;
      verticesIndices [index++] = vertexIndex3;
    }
    return index;
  }

  /**
   * A triangle used to remove faces cited more that once (opposite faces included).
   */
  static class Triangle {
    private float [] point1;
    private float [] point2;
    private float [] point3;
    private int      hashCode;
    private boolean  hashCodeSet;

    public Triangle(float [] vertices, int index1, int index2, int index3) {
      this.point1 = new float [] {vertices [index1 * 3], vertices [index1 * 3 + 1], vertices [index1 * 3 + 2]};
      this.point2 = new float [] {vertices [index2 * 3], vertices [index2 * 3 + 1], vertices [index2 * 3 + 2]};
      this.point3 = new float [] {vertices [index3 * 3], vertices [index3 * 3 + 1], vertices [index3 * 3 + 2]};
    }

    @Override
    public int hashCode() {
      if (!this.hashCodeSet) {
        this.hashCode = 31 * Arrays.hashCode(this.point1)
            + 31 * Arrays.hashCode(this.point2)
            + 31 * Arrays.hashCode(this.point3);
        this.hashCodeSet = true;
      }
      return this.hashCode;
    }

    @Override
    public boolean equals(Object obj) {
      if (this == obj) {
        return true;
      } else if (obj instanceof Triangle) {
        Triangle triangle = (Triangle)obj;
        // Compare first with point with opposite face
        return Arrays.equals(this.point1, triangle.point3)
               && Arrays.equals(this.point2, triangle.point2)
               && Arrays.equals(this.point3, triangle.point1)
            || Arrays.equals(this.point1, triangle.point2)
               && Arrays.equals(this.point2, triangle.point1)
               && Arrays.equals(this.point3, triangle.point3)
            || Arrays.equals(this.point1, triangle.point1)
               && Arrays.equals(this.point2, triangle.point3)
               && Arrays.equals(this.point3, triangle.point2)
            || Arrays.equals(this.point1, triangle.point1)
               && Arrays.equals(this.point2, triangle.point2)
               && Arrays.equals(this.point3, triangle.point3);
      }
      return false;
    }
  }

  /**
   * A key used to manage textures at different levels of transparency.
   */
  static class TransparentTextureKey {
    private Texture texture;
    private float   transparency;

    public TransparentTextureKey(Texture texture, float transparency) {
      this.texture = texture;
      this.transparency = transparency;
    }

    @Override
    public boolean equals(Object obj) {
      return ((TransparentTextureKey)obj).texture.equals(this.texture)
          && ((TransparentTextureKey)obj).transparency == this.transparency;
    }

    @Override
    public int hashCode() {
      return this.texture.hashCode() + Float.floatToIntBits(this.transparency);
    }
  }

  /**
   * Default factory for photo creation with no ceiling for rooms when top camera is used.
   */
  static class PhotoObject3DFactory extends Object3DBranchFactory {
    @Override
    public boolean isDrawingModeEnabled() {
      return false;
    }

    public Object createObject3D(Home home, Selectable item, boolean waitForLoading) {
      if (item instanceof Room) {
        // Never display ceiling with top camera
        return new Room3D((Room)item, home, !(home.getCamera() instanceof ObserverCamera), waitForLoading);
      } else if (item instanceof DimensionLine) {
        return null;
      } else {
        return super.createObject3D(home, item, waitForLoading);
      }
    }
  }
}
