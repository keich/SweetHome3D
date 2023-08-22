/*
 * DimensionLine.java 17 sept 2007
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
package com.eteks.sweethome3d.model;

import java.awt.Shape;
import java.awt.geom.Ellipse2D;
import java.awt.geom.GeneralPath;
import java.awt.geom.Line2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.io.IOException;
import java.io.ObjectInputStream;

/**
 * A dimension line in plan.
 * @author Emmanuel Puybaret
 */
public class DimensionLine extends HomeObject implements Selectable, Elevatable {
  /**
   * The properties of a dimension line that may change. <code>PropertyChangeListener</code>s added
   * to a dimension line will be notified under a property name equal to the string value of one these properties.
   */
  public enum Property {X_START, Y_START, ELEVATION_START, X_END, Y_END, ELEVATION_END, OFFSET, END_MARK_SIZE, PITCH, LENGTH_STYLE, COLOR, VISIBLE_IN_3D, LEVEL}

  private static final long serialVersionUID = 1L;

  private float           xStart;
  private float           yStart;
  private float           elevationStart;
  private float           xEnd;
  private float           yEnd;
  private float           elevationEnd;
  private float           offset;
  private float           endMarkSize;
  private float           pitch;
  private TextStyle       lengthStyle;
  private Integer         color;
  private boolean         visibleIn3D;
  private Level           level;

  private transient Shape shapeCache;


  /**
   * Creates a dimension line from (<code>xStart</code>, <code>yStart</code>)
   * to (<code>xEnd</code>, <code>yEnd</code>), with a given offset.
   */
  public DimensionLine(float xStart, float yStart, float xEnd, float yEnd, float offset) {
    this(xStart, yStart, 0, xEnd, yEnd, 0, offset);
  }

  /**
   * Creates a dimension line from (<code>xStart</code>, <code>yStart</code>, <code>elevationStart</code>)
   * to (<code>xEnd</code>, <code>yEnd</code>, <code>elevationEnd</code>), with a given offset.
   * @since 7.2
   */
  public DimensionLine(float xStart, float yStart, float elevationStart,
                       float xEnd, float yEnd, float elevationEnd, float offset) {
    this(createId("dimensionLine"), xStart, yStart, elevationStart, xEnd, yEnd, elevationEnd, offset);
  }

  /**
   * Creates a dimension line from (<code>xStart</code>,<code>yStart</code>)
   * to (<code>xEnd</code>, <code>yEnd</code>), with a given offset.
   * @since 6.4
   */
  public DimensionLine(String id, float xStart, float yStart, float xEnd, float yEnd, float offset) {
    this(id, xStart, yStart, 0, xEnd, yEnd, 0, offset);
  }

  /**
   * Creates a dimension line from (<code>xStart</code>, <code>yStart</code>, <code>elevationStart</code>)
   * to (<code>xEnd</code>, <code>yEnd</code>, <code>elevationEnd</code>), with a given offset.
   * @since 7.2
   */
  public DimensionLine(String id, float xStart, float yStart, float elevationStart,
                       float xEnd, float yEnd, float elevationEnd, float offset) {
    super(id);
    this.xStart = xStart;
    this.yStart = yStart;
    this.elevationStart = elevationStart;
    this.xEnd = xEnd;
    this.yEnd = yEnd;
    this.elevationEnd = elevationEnd;
    this.offset = offset;
    this.endMarkSize = 10;
  }

  /**
   * Initializes new dimension line fields to their default values
   * and reads room from <code>in</code> stream with default reading method.
   */
  private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
    this.endMarkSize = 10;
    in.defaultReadObject();
  }

  /**
   * Returns the start point abscissa of this dimension line.
   */
  public float getXStart() {
    return this.xStart;
  }

  /**
   * Sets the start point abscissa of this dimension line. Once this dimension line
   * is updated, listeners added to this dimension line will receive a change notification.
   */
  public void setXStart(float xStart) {
    if (xStart != this.xStart) {
      float oldXStart = this.xStart;
      this.xStart = xStart;
      this.shapeCache = null;
      firePropertyChange(Property.X_START.name(), oldXStart, xStart);
    }
  }

  /**
   * Returns the start point ordinate of this dimension line.
   */
  public float getYStart() {
    return this.yStart;
  }

  /**
   * Sets the start point ordinate of this dimension line. Once this dimension line
   * is updated, listeners added to this dimension line will receive a change notification.
   */
  public void setYStart(float yStart) {
    if (yStart != this.yStart) {
      float oldYStart = this.yStart;
      this.yStart = yStart;
      this.shapeCache = null;
      firePropertyChange(Property.Y_START.name(), oldYStart, yStart);
    }
  }

  /**
   * Returns the start point elevation of this dimension line.
   * @since 7.2
   */
  public float getElevationStart() {
    return this.elevationStart;
  }

  /**
   * Sets the start point elevation of this dimension line. Once this dimension line
   * is updated, listeners added to this dimension line will receive a change notification.
   * @since 7.2
   */
  public void setElevationStart(float elevationStart) {
    if (elevationStart != this.elevationStart) {
      float oldElevationStart = this.elevationStart;
      this.elevationStart = elevationStart;
      this.shapeCache = null;
      firePropertyChange(Property.ELEVATION_START.name(), oldElevationStart, elevationStart);
    }
  }

  /**
   * Returns the end point abscissa of this dimension line.
   */
  public float getXEnd() {
    return this.xEnd;
  }

  /**
   * Sets the end point abscissa of this dimension line. Once this dimension line
   * is updated, listeners added to this dimension line will receive a change notification.
   */
  public void setXEnd(float xEnd) {
    if (xEnd != this.xEnd) {
      float oldXEnd = this.xEnd;
      this.xEnd = xEnd;
      this.shapeCache = null;
      firePropertyChange(Property.X_END.name(), oldXEnd, xEnd);
    }
  }

  /**
   * Returns the end point ordinate of this dimension line.
   */
  public float getYEnd() {
    return this.yEnd;
  }

  /**
   * Sets the end point ordinate of this dimension line. Once this dimension line
   * is updated, listeners added to this dimension line will receive a change notification.
   */
  public void setYEnd(float yEnd) {
    if (yEnd != this.yEnd) {
      float oldYEnd = this.yEnd;
      this.yEnd = yEnd;
      this.shapeCache = null;
      firePropertyChange(Property.Y_END.name(), oldYEnd, yEnd);
    }
  }

  /**
   * Returns the end point elevation of this dimension line.
   * @since 7.2
   */
  public float getElevationEnd() {
    return this.elevationEnd;
  }

  /**
   * Sets the end point elevation of this dimension line. Once this dimension line
   * is updated, listeners added to this dimension line will receive a change notification.
   * @since 7.2
   */
  public void setElevationEnd(float elevationEnd) {
    if (elevationEnd != this.elevationEnd) {
      float oldElevationEnd = this.elevationEnd;
      this.elevationEnd = elevationEnd;
      this.shapeCache = null;
      firePropertyChange(Property.ELEVATION_END.name(), oldElevationEnd, elevationEnd);
    }
  }

  /**
   * Returns <code>true</code> if this dimension line is an elevation (vertical) dimension line.
   * @since 7.2
   */
  public boolean isElevationDimensionLine() {
    return this.xStart == this.xEnd
        && this.yStart == this.yEnd
        && this.elevationStart != this.elevationEnd;
  }

  /**
   * Returns the offset of this dimension line.
   */
  public float getOffset() {
    return this.offset;
  }

  /**
   * Sets the offset of this dimension line. Once this dimension line
   * is updated, listeners added to this dimension line will receive a change notification.
   */
  public void setOffset(float offset) {
    if (offset != this.offset) {
      float oldOffset = this.offset;
      this.offset = offset;
      this.shapeCache = null;
      firePropertyChange(Property.OFFSET.name(), oldOffset, offset);
    }
  }

  /**
   * Returns the pitch angle in radians of this dimension line around its axis.
   * @since 7.2
   */
  public float getPitch() {
    return this.pitch;
  }

  /**
   * Sets the pitch angle of this dimension line. Once this dimension line
   * is updated, listeners added to this dimension line will receive a change notification.
   * @since 7.2
   */
  public void setPitch(float pitch) {
    if (pitch != this.pitch) {
      float oldPitch = this.pitch;
      this.pitch = pitch;
      this.shapeCache = null;
      firePropertyChange(Property.PITCH.name(), oldPitch, pitch);
    }
  }

  /**
   * Returns the length of this dimension line.
   */
  public float getLength() {
    float deltaX = this.xEnd - this.xStart;
    float deltaY = this.yEnd - this.yStart;
    float deltaElevation = this.elevationEnd - this.elevationStart;
    return (float)Math.sqrt(deltaX * deltaX + deltaY * deltaY + deltaElevation * deltaElevation);
  }

  /**
   * Returns the text style used to display dimension line length.
   */
  public TextStyle getLengthStyle() {
    return this.lengthStyle;
  }

  /**
   * Sets the text style used to display dimension line length.
   * Once this dimension line is updated, listeners added to it will receive a change notification.
   */
  public void setLengthStyle(TextStyle lengthStyle) {
    if (lengthStyle != this.lengthStyle) {
      TextStyle oldLengthStyle = this.lengthStyle;
      this.lengthStyle = lengthStyle;
      firePropertyChange(Property.LENGTH_STYLE.name(), oldLengthStyle, lengthStyle);
    }
  }

  /**
   * Returns the color used to display the text of this dimension line.
   * @since 7.2
   */
  public Integer getColor() {
    return this.color;
  }

  /**
   * Sets the color used to display the text of this dimension line.
   * Once this dimension line is updated, listeners added to this dimension line
   * will receive a change notification.
   * @since 7.2
   */
  public void setColor(Integer color) {
    if (color != this.color) {
      Integer oldColor = this.color;
      this.color = color;
      firePropertyChange(Property.COLOR.name(), oldColor, color);
    }
  }

  /**
   * Returns the size of marks drawn at the end of the dimension line.
   * @since 7.2
   */
  public float getEndMarkSize() {
    return this.endMarkSize;
  }

  /**
   * Sets the size of marks drawn at the end of the dimension line.
   * @since 7.2
   */
  public void setEndMarkSize(float endMarkSize) {
    if (endMarkSize != this.endMarkSize) {
      float oldEndMarkSize = this.endMarkSize;
      this.endMarkSize = endMarkSize;
      this.shapeCache = null;
      firePropertyChange(Property.END_MARK_SIZE.name(), oldEndMarkSize, endMarkSize);
    }
  }

  /**
   * Returns <code>true</code> if this dimension line should be displayed in 3D.
   * @since 7.2
   */
  public boolean isVisibleIn3D() {
    return this.visibleIn3D;
  }

  /**
   * Sets whether this dimension line should be displayed in 3D.
   * Once this dimension line is updated, listeners added to this dimension line
   * will receive a change notification.
   * @since 7.2
   */
  public void setVisibleIn3D(boolean visibleIn3D) {
    if (visibleIn3D != this.visibleIn3D) {
      this.visibleIn3D = visibleIn3D;
      firePropertyChange(Property.VISIBLE_IN_3D.name(), !visibleIn3D, visibleIn3D);
    }
  }

  /**
   * Returns the level which this dimension line belongs to.
   * @since 3.4
   */
  public Level getLevel() {
    return this.level;
  }

  /**
   * Sets the level of this dimension line. Once this dimension line is updated,
   * listeners added to this dimension line will receive a change notification.
   * @since 3.4
   */
  public void setLevel(Level level) {
    if (level != this.level) {
      Level oldLevel = this.level;
      this.level = level;
      firePropertyChange(Property.LEVEL.name(), oldLevel, level);
    }
  }

  /**
   * Returns <code>true</code> if this dimension line is at the given <code>level</code>
   * or at a level with the same elevation and a smaller elevation index
   * or if the elevation of its highest end is higher than <code>level</code> elevation.
   * @since 3.4
   */
  public boolean isAtLevel(Level level) {
    if (this.level == level) {
      return true;
    } else if (this.level != null && level != null) {
      float dimensionLineLevelElevation = this.level.getElevation();
      float levelElevation = level.getElevation();
      return dimensionLineLevelElevation == levelElevation
             && this.level.getElevationIndex() < level.getElevationIndex()
          || dimensionLineLevelElevation < levelElevation
             && dimensionLineLevelElevation + Math.max(this.elevationStart, this.elevationEnd) > levelElevation;
    } else {
      return false;
    }
  }

  /**
   * Returns the points of the rectangle surrounding
   * this dimension line and its extension lines.
   * @return an array of the 4 (x,y) coordinates of the rectangle.
   */
  public float [][] getPoints() {
    double angle = isElevationDimensionLine()
        ? this.pitch
        : Math.atan2(this.yEnd - this.yStart, this.xEnd - this.xStart);
    float dx = (float)-Math.sin(angle) * this.offset;
    float dy = (float)Math.cos(angle) * this.offset;

    return new float [] [] {{this.xStart, this.yStart},
                            {this.xStart + dx, this.yStart + dy},
                            {this.xEnd + dx, this.yEnd + dy},
                            {this.xEnd, this.yEnd}};
  }

  /**
   * Returns <code>true</code> if this dimension line intersects
   * with the horizontal rectangle which opposite corners are at points
   * (<code>x0</code>, <code>y0</code>) and (<code>x1</code>, <code>y1</code>).
   */
  public boolean intersectsRectangle(float x0, float y0, float x1, float y1) {
    Rectangle2D rectangle = new Rectangle2D.Float(x0, y0, 0, 0);
    rectangle.add(x1, y1);
    return getShape().intersects(rectangle);
  }

  /**
   * Returns <code>true</code> if this dimension line contains
   * the point at (<code>x</code>, <code>y</code>)
   * with a given <code>margin</code>.
   */
  public boolean containsPoint(float x, float y, float margin) {
    return containsShapeAtWithMargin(getShape(), x, y, margin);
  }

  /**
   * Returns <code>true</code> if the middle point of this dimension line
   * is the point at (<code>x</code>, <code>y</code>)
   * with a given <code>margin</code>.
   */
  public boolean isMiddlePointAt(float x, float y, float margin) {
    if (this.elevationStart == this.elevationEnd) {
      double angle = Math.atan2(this.yEnd - this.yStart, this.xEnd - this.xStart);
      float dx = (float)-Math.sin(angle) * this.offset;
      float dy = (float)Math.cos(angle) * this.offset;
      float xMiddle = (this.xStart + this.xEnd) / 2 + dx;
      float yMiddle = (this.yStart + this.yEnd) / 2 + dy;
      return Math.abs(x - xMiddle) <= margin && Math.abs(y - yMiddle) <= margin;
    } else {
      return false;
    }
  }

  /**
   * Returns <code>true</code> if the extension line at the start of this dimension line
   * contains the point at (<code>x</code>, <code>y</code>)
   * with a given <code>margin</code> around the extension line.
   */
  public boolean containsStartExtensionLinetAt(float x, float y, float margin) {
    if (this.elevationStart == this.elevationEnd) {
      double angle = Math.atan2(this.yEnd - this.yStart, this.xEnd - this.xStart);
      Line2D startExtensionLine = new Line2D.Float(this.xStart, this.yStart,
          this.xStart + (float)-Math.sin(angle) * this.offset,
          this.yStart + (float)Math.cos(angle) * this.offset);
      return containsShapeAtWithMargin(startExtensionLine, x, y, margin);
    } else {
      return false;
    }
  }

  /**
   * Returns <code>true</code> if the extension line at the end of this dimension line
   * contains the point at (<code>x</code>, <code>y</code>)
   * with a given <code>margin</code> around the extension line.
   */
  public boolean containsEndExtensionLineAt(float x, float y, float margin) {
    if (this.elevationStart == this.elevationEnd) {
      double angle = Math.atan2(this.yEnd - this.yStart, this.xEnd - this.xStart);
      Line2D endExtensionLine = new Line2D.Float(this.xEnd, this.yEnd,
          this.xEnd + (float)-Math.sin(angle) * this.offset,
          this.yEnd + (float)Math.cos(angle) * this.offset);
      return containsShapeAtWithMargin(endExtensionLine, x, y, margin);
    } else {
      return false;
    }
  }

  /**
   * Returns <code>true</code> if the top point of this dimension line is
   * the point at (<code>x</code>, <code>y</code>) with a given <code>margin</code>.
   * @since 7.2
   */
  public boolean isTopPointAt(float x, float y, float margin) {
    if (this.elevationStart != this.elevationEnd) {
      double angle = this.yEnd != this.yStart || this.xEnd != this.xStart
          ? Math.atan2(this.yEnd - this.yStart, this.xEnd - this.xStart)
         : this.pitch;
      float dx = (float)-Math.sin(angle) * (this.offset - this.endMarkSize / 2 * (Math.signum(this.offset) == 0 ? -1 : Math.signum(this.offset)));
      float dy = (float)Math.cos(angle) * (this.offset - this.endMarkSize / 2 * (Math.signum(this.offset) == 0 ? -1 : Math.signum(this.offset)));
      double distanceSquareToTopPoint = Point2D.distanceSq(x, y, this.xStart + dx, this.yStart + dy);
      return distanceSquareToTopPoint <= margin * margin;
    } else {
      return false;
    }
  }

  /**
   * Returns <code>true</code> if the right point of this dimension line is
   * the point at (<code>x</code>, <code>y</code>) with a given <code>margin</code>.
   * @since 7.2
   */
  public boolean isRightPointAt(float x, float y, float margin) {
    if (this.elevationStart != this.elevationEnd) {
      double angle = this.yEnd != this.yStart || this.xEnd != this.xStart
          ? Math.atan2(this.yEnd - this.yStart, this.xEnd - this.xStart)
         : this.pitch;
      float sin = (float)Math.sin(angle);
      float cos = (float)Math.cos(angle);
      float dx = -sin * this.offset + cos * this.endMarkSize / 2;
      float dy = cos * this.offset + sin * this.endMarkSize / 2;
      double distanceSquareToTopPoint = Point2D.distanceSq(x, y, this.xStart + dx, this.yStart + dy);
      return distanceSquareToTopPoint <= margin * margin;
    } else {
      return false;
    }
  }

  /**
   * Returns <code>true</code> if the bottom left point of this dimension line is
   * the point at (<code>x</code>, <code>y</code>) with a given <code>margin</code>.
   * @since 7.2
   */
  public boolean isBottomPointAt(float x, float y, float margin) {
    if (this.elevationStart != this.elevationEnd) {
      double angle = this.yEnd != this.yStart || this.xEnd != this.xStart
          ? Math.atan2(this.yEnd - this.yStart, this.xEnd - this.xStart)
         : this.pitch;
      float dx = (float)-Math.sin(angle) * (this.offset + this.endMarkSize / 2 * (Math.signum(this.offset) == 0 ? -1 : Math.signum(this.offset)));
      float dy = (float)Math.cos(angle) * (this.offset + this.endMarkSize / 2 * (Math.signum(this.offset) == 0 ? -1 : Math.signum(this.offset)));
      double distanceSquareToTopPoint = Point2D.distanceSq(x, y, this.xStart + dx, this.yStart + dy);
      return distanceSquareToTopPoint <= margin * margin;
    } else {
      return false;
    }
  }

  /**
   * Returns <code>true</code> if the left point of this dimension line is
   * the point at (<code>x</code>, <code>y</code>) with a given <code>margin</code>.
   * @since 7.2
   */
  public boolean isLeftPointAt(float x, float y, float margin) {
    if (this.elevationStart != this.elevationEnd) {
      double angle = this.yEnd != this.yStart || this.xEnd != this.xStart
          ? Math.atan2(this.yEnd - this.yStart, this.xEnd - this.xStart)
         : this.pitch;
      float sin = (float)Math.sin(angle);
      float cos = (float)Math.cos(angle);
      float dx = -sin * this.offset - cos * this.endMarkSize / 2;
      float dy = cos * this.offset - sin * this.endMarkSize / 2;
      double distanceSquareToTopPoint = Point2D.distanceSq(x, y, this.xStart + dx, this.yStart + dy);
      return distanceSquareToTopPoint <= margin * margin;
    } else {
      return false;
    }
  }

  /**
   * Returns <code>true</code> if <code>shape</code> contains
   * the point at (<code>x</code>, <code>y</code>)
   * with a given <code>margin</code>.
   */
  private boolean containsShapeAtWithMargin(Shape shape, float x, float y, float margin) {
    if (margin == 0) {
      return shape.contains(x, y);
    } else {
      return shape.intersects(x - margin, y - margin, 2 * margin, 2 * margin);
    }
  }

  /**
   * Returns the shape matching this dimension line.
   */
  private Shape getShape() {
    if (this.shapeCache == null) {
      // Create the shape which matches bounds
      double angle = this.yEnd != this.yStart || this.xEnd != this.xStart
          ? Math.atan2(this.yEnd - this.yStart, this.xEnd - this.xStart)
          : this.pitch;
      boolean horizontalDimensionLine = this.elevationStart == this.elevationEnd;
      float dx = (float)-Math.sin(angle) * this.offset;
      float dy = (float)Math.cos(angle) * this.offset;

      GeneralPath dimensionLineShape = new GeneralPath();
      if (horizontalDimensionLine) {
        // Append dimension line
        dimensionLineShape.append(new Line2D.Float(this.xStart + dx, this.yStart + dy, this.xEnd + dx, this.yEnd + dy), false);
        // Append extension lines
        dimensionLineShape.append(new Line2D.Float(this.xEnd, this.yEnd, this.xEnd + dx, this.yEnd + dy), false);
      } else {
        dimensionLineShape.append(new Ellipse2D.Float(this.xStart + dx - this.endMarkSize / 2, this.yStart + dy - this.endMarkSize / 2,
            this.endMarkSize, this.endMarkSize), false);
      }
      dimensionLineShape.append(new Line2D.Float(this.xStart, this.yStart, this.xStart + dx, this.yStart + dy), false);
      // Cache shape
      this.shapeCache = dimensionLineShape;
    }
    return this.shapeCache;
  }

  /**
   * Moves this dimension line of (<code>dx</code>, <code>dy</code>) units.
   */
  public void move(float dx, float dy) {
    setXStart(getXStart() + dx);
    setYStart(getYStart() + dy);
    setXEnd(getXEnd() + dx);
    setYEnd(getYEnd() + dy);
  }

  /**
   * Returns a clone of this dimension line.
   */
  @Override
  public DimensionLine clone() {
    DimensionLine clone = (DimensionLine)super.clone();
    clone.level = null;
    return clone;
  }
}
