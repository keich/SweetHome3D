/*
 * DimensionLine3D.java 4 mai 2023
 *
 * Sweet Home 3D, Copyright (c) 2023 Emmanuel PUYBARET / eTeks <info@eteks.com>
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

import java.awt.Color;
import java.awt.Font;
import java.awt.font.FontRenderContext;
import java.awt.font.LineMetrics;
import java.awt.geom.AffineTransform;

import javax.media.j3d.Appearance;
import javax.media.j3d.BranchGroup;
import javax.media.j3d.ColoringAttributes;
import javax.media.j3d.Group;
import javax.media.j3d.IndexedGeometryArray;
import javax.media.j3d.LineArray;
import javax.media.j3d.LineAttributes;
import javax.media.j3d.Shape3D;
import javax.media.j3d.Transform3D;
import javax.media.j3d.TransformGroup;
import javax.swing.UIManager;
import javax.vecmath.Color3f;
import javax.vecmath.Point3f;
import javax.vecmath.Vector3f;

import com.eteks.sweethome3d.model.DimensionLine;
import com.eteks.sweethome3d.model.Home;
import com.eteks.sweethome3d.model.Label;
import com.eteks.sweethome3d.model.TextStyle;
import com.eteks.sweethome3d.model.UserPreferences;

/**
 * Root of a dimension line branch.
 * @author Emmanuel Puybaret
 */
public class DimensionLine3D extends Object3DBranch {
  private static final LineAttributes DIMENSION_LINE_ATTRIBUTES =
      new LineAttributes(2f, LineAttributes.PATTERN_SOLID, true);

  private UserPreferences preferences;

  public DimensionLine3D(DimensionLine dimensionLine, Home home, UserPreferences preferences) {
    setUserData(dimensionLine);
    this.preferences = preferences;

    setCapability(ALLOW_CHILDREN_EXTEND);
    setCapability(ALLOW_CHILDREN_READ);
    setCapability(ALLOW_CHILDREN_WRITE);
    setCapability(ALLOW_DETACH);
    update();
  }

  @Override
  public void update() {
    DimensionLine dimensionLine = (DimensionLine)getUserData();
    if (dimensionLine.isVisibleIn3D()
        && (dimensionLine.getLevel() == null
            || dimensionLine.getLevel().isViewableAndVisible())) {
      float dimensionLineLength = dimensionLine.getLength();
      String lengthText = this.preferences.getLengthUnit().getFormat().format(dimensionLineLength);
      TextStyle lengthStyle = dimensionLine.getLengthStyle();
      if (lengthStyle == null) {
        lengthStyle = this.preferences.getDefaultTextStyle(dimensionLine.getClass());
      }

      Font defaultFont;
      if (lengthStyle.getFontName() != null) {
        defaultFont = new Font(lengthStyle.getFontName(), Font.PLAIN, 1);
      } else {
        defaultFont = UIManager.getFont("TextField.font");
      }
      Font font = defaultFont.deriveFont(Font.PLAIN, lengthStyle.getFontSize());
      LineMetrics lengthFontMetrics = font.getLineMetrics(lengthText,
          new FontRenderContext(new AffineTransform(), true, true));
      float fontAscent = lengthFontMetrics.getAscent();
      float offset = dimensionLine.getOffset();
      float zTranslation = offset <= 0
          ? -lengthFontMetrics.getDescent() - 1
          : fontAscent + 1;

      TransformGroup transformGroup;
      LineArray lines;
      ColoringAttributes linesColoringAttributes;
      if (numChildren() == 0) {
        BranchGroup group = new BranchGroup();
        group.setCapability(BranchGroup.ALLOW_CHILDREN_READ);
        group.setCapability(BranchGroup.ALLOW_DETACH);

        transformGroup = new TransformGroup();
        transformGroup.setCapability(TransformGroup.ALLOW_TRANSFORM_WRITE);
        transformGroup.setCapability(TransformGroup.ALLOW_CHILDREN_READ);

        Label lengthLabel = new Label(lengthText, dimensionLineLength / 2, zTranslation);
        lengthLabel.setColor(dimensionLine.getColor());
        lengthLabel.setStyle(lengthStyle);
        lengthLabel.setPitch(0f);
        lengthLabel.setLevel(dimensionLine.getLevel());
        Label3D label3D = new Label3D(lengthLabel, null, false);
        transformGroup.addChild(label3D);

        lines = new LineArray(7 * 2, IndexedGeometryArray.COORDINATES);
        lines.setCapability(LineArray.ALLOW_COORDINATE_WRITE);
        linesColoringAttributes = new ColoringAttributes();
        linesColoringAttributes.setCapability(ColoringAttributes.ALLOW_COLOR_WRITE);
        Appearance mainLinesAppearance = new Appearance();
        mainLinesAppearance.setColoringAttributes(linesColoringAttributes);
        mainLinesAppearance.setLineAttributes(DIMENSION_LINE_ATTRIBUTES);
        mainLinesAppearance.setCapability(Appearance.ALLOW_COLORING_ATTRIBUTES_READ);
        Shape3D mainLinesShape = new Shape3D(lines, mainLinesAppearance);
        mainLinesShape.setCapability(Shape3D.ALLOW_GEOMETRY_READ);
        transformGroup.addChild(mainLinesShape);

        group.addChild(transformGroup);
        addChild(group);
      } else {
        transformGroup = (TransformGroup)(((Group)getChild(0)).getChild(0));
        Label3D label3D = (Label3D)transformGroup.getChild(0);
        Label lengthLabel = (Label)label3D.getUserData();
        lengthLabel.setText(lengthText);
        lengthLabel.setX(dimensionLineLength / 2);
        lengthLabel.setY(zTranslation);
        lengthLabel.setColor(dimensionLine.getColor());
        lengthLabel.setStyle(lengthStyle);
        label3D.update();

        Shape3D linesShape = (Shape3D)transformGroup.getChild(1);
        lines = (LineArray)linesShape.getGeometry();
        linesColoringAttributes = linesShape.getAppearance().getColoringAttributes();
      }

      float elevationStart = dimensionLine.getElevationStart();
      Transform3D startPointTransform = new Transform3D();
      startPointTransform.setTranslation(new Vector3f(
          dimensionLine.getXStart(), dimensionLine.getLevel() != null ? dimensionLine.getLevel().getElevation() + elevationStart : elevationStart, dimensionLine.getYStart()));
      Transform3D rotation = new Transform3D();
      double elevationAngle = Math.atan2(dimensionLine.getElevationEnd() - elevationStart,
          dimensionLine.getXEnd() - dimensionLine.getXStart());
      rotation.rotZ(elevationAngle);
      startPointTransform.mul(rotation);
      rotation = new Transform3D();
      double endsAngle = Math.atan2(dimensionLine.getYStart() - dimensionLine.getYEnd(),
          dimensionLine.getXEnd() - dimensionLine.getXStart());
      rotation.rotY(endsAngle);
      startPointTransform.mul(rotation);
      rotation = new Transform3D();
      rotation.rotX(-dimensionLine.getPitch());
      startPointTransform.mul(rotation);
      Transform3D offsetTransform = new Transform3D();
      offsetTransform.setTranslation(new Vector3f(0, 0, offset));
      startPointTransform.mul(offsetTransform);
      transformGroup.setTransform(startPointTransform);

      // Handle dimension lines
      float endMarkSize = dimensionLine.getEndMarkSize() / 2;
      Point3f [] linesCoordinates = new Point3f [7 * 2];
      linesCoordinates [0] = new Point3f(0, 0, 0);
      linesCoordinates [1] = new Point3f(dimensionLineLength, 0, 0);
      linesCoordinates [2] = new Point3f(-endMarkSize, 0, endMarkSize);
      linesCoordinates [3] = new Point3f(endMarkSize, 0, -endMarkSize);
      linesCoordinates [4] = new Point3f(0, 0, endMarkSize);
      linesCoordinates [5] = new Point3f(0, 0, -endMarkSize);
      linesCoordinates [6] = new Point3f(dimensionLineLength - endMarkSize, 0, endMarkSize);
      linesCoordinates [7] = new Point3f(dimensionLineLength + endMarkSize, 0, -endMarkSize);
      linesCoordinates [8] = new Point3f(dimensionLineLength, 0, endMarkSize);
      linesCoordinates [9] = new Point3f(dimensionLineLength, 0, -endMarkSize);
      linesCoordinates [10] = new Point3f(0, 0, -offset);
      linesCoordinates [11] = new Point3f(0, 0, -endMarkSize);
      linesCoordinates [12] = new Point3f(dimensionLineLength, 0, -offset);
      linesCoordinates [13] = new Point3f(dimensionLineLength, 0, -endMarkSize);
      lines.setCoordinates(0, linesCoordinates);
      linesColoringAttributes.setColor(new Color3f(dimensionLine.getColor() != null ? new Color(dimensionLine.getColor()) : Color.BLACK));
    } else {
      removeAllChildren();
    }
  }
}
