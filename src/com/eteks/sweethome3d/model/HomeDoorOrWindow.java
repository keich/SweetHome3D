/*
 * HomeDoorOrWindow.java 8 mars 2009
 *
 * Sweet Home 3D, Copyright (c) 2009 Emmanuel PUYBARET / eTeks <info@eteks.com>
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

import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.Arrays;

/**
 * A door or a window in {@linkplain Home home}.
 * @author Emmanuel Puybaret
 * @since  1.7
 */
public class HomeDoorOrWindow extends HomePieceOfFurniture implements DoorOrWindow {
  private static final long serialVersionUID = 1L;

  /**
   * The properties of a door or window that may change. <code>PropertyChangeListener</code>s added
   * to a door or window will be notified under a property name equal to the string value of one these properties.
   */
  public enum Property {WALL_THICKNESS, WALL_DISTANCE, WALL_WIDTH, WALL_LEFT, WALL_HEIGHT, WALL_TOP, SASHES,
                        CUT_OUT_SHAPE, WALL_CUT_OUT_ON_BOTH_SIDES, WIDTH_DEPTH_DEFORMABLE, BOUND_TO_WALL};

  private float         wallThickness;
  private float         wallDistance;
  private float         wallWidth;
  private float         wallLeft;
  private float         wallHeight;
  private float         wallTop;
  private boolean       wallCutOutOnBothSides; // false for version < 5.5
  private boolean       widthDepthDeformable;
  private Sash []       sashes;
  private String        cutOutShape;
  private boolean       boundToWall;

  /**
   * Creates a home door or window from an existing one.
   * No additional properties will be copied.
   * @param doorOrWindow the door or window from which data are copied
   */
  public HomeDoorOrWindow(DoorOrWindow doorOrWindow) {
    this(doorOrWindow, EMPTY_PROPERTY_ARRAY);
  }

  /**
   * Creates a home door or window from an existing one.
   * @param doorOrWindow the door or window from which data are copied
   * @param copiedProperties the names of the additional properties which should be copied from the existing piece
   *                         or <code>null</code> if all properties should be copied.
   * @since 7.2
   */
  public HomeDoorOrWindow(DoorOrWindow doorOrWindow, String [] copiedProperties) {
    this(createId("doorOrWindow"), doorOrWindow, copiedProperties);
  }

  /**
   * Creates a home door or window from an existing one.
   * No additional properties will be copied.
   * @param id           the ID of the object
   * @param doorOrWindow the door or window from which data are copied
   * @since 6.4
   */
  public HomeDoorOrWindow(String id, DoorOrWindow doorOrWindow) {
    this(id, doorOrWindow, EMPTY_PROPERTY_ARRAY);
  }

  /**
   * Creates a home door or window from an existing one.
   * @param id           the ID of the object
   * @param doorOrWindow the door or window from which data are copied
   * @param copiedProperties the names of the additional properties which should be copied from the existing piece
   *                         or <code>null</code> if all properties should be copied.
   * @since 7.2
   */
  public HomeDoorOrWindow(String id, DoorOrWindow doorOrWindow, String [] copiedProperties) {
    super(id, doorOrWindow, copiedProperties);
    this.wallThickness = doorOrWindow.getWallThickness();
    this.wallDistance = doorOrWindow.getWallDistance();
    this.wallWidth = 1;
    this.wallLeft = 0;
    this.wallHeight = 1;
    this.wallTop = 0;
    this.wallCutOutOnBothSides = doorOrWindow.isWallCutOutOnBothSides();
    this.widthDepthDeformable = doorOrWindow.isWidthDepthDeformable();
    this.sashes = doorOrWindow.getSashes();
    this.cutOutShape = doorOrWindow.getCutOutShape();
  }

  /**
   * Initializes new fields to their default values
   * and reads object from <code>in</code> stream with default reading method.
   */
  private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
    this.cutOutShape = PieceOfFurniture.DEFAULT_CUT_OUT_SHAPE;
    this.widthDepthDeformable = true;
    this.wallWidth = 1;
    this.wallLeft = 0;
    this.wallHeight = 1;
    this.wallTop = 0;
    in.defaultReadObject();
  }

  /**
   * Returns the thickness of the wall in which this door or window should be placed.
   * @return a value in percentage of the depth of the door or the window.
   */
  public float getWallThickness() {
    return this.wallThickness;
  }

  /**
   * Sets the thickness of the wall in which this door or window should be placed.
   * Once this piece is updated, listeners added to this piece will receive a change notification.
   * @param wallThickness a value in percentage of the depth of the door or the window.
   * @since 6.0
   */
  public void setWallThickness(float wallThickness) {
    if (wallThickness != this.wallThickness) {
      float oldWallThickness = this.wallThickness;
      this.wallThickness = wallThickness;
      firePropertyChange(Property.WALL_THICKNESS.name(), oldWallThickness, wallThickness);
    }
  }

  /**
   * Returns the distance between the back side of this door or window and the wall where it's located.
   * @return a distance in percentage of the depth of the door or the window.
   */
  public float getWallDistance() {
    return this.wallDistance;
  }

  /**
   * Sets the distance between the back side of this door or window and the wall where it's located.
   * Once this piece is updated, listeners added to this piece will receive a change notification.
   * @param wallDistance a distance in percentage of the depth of the door or the window.
   * @since 6.0
   */
  public void setWallDistance(float wallDistance) {
    if (wallDistance != this.wallDistance) {
      float oldWallDistance = this.wallDistance;
      this.wallDistance = wallDistance;
      firePropertyChange(Property.WALL_DISTANCE.name(), oldWallDistance, wallDistance);
    }
  }

  /**
   * Returns the width of the wall part in which this door or window should be placed.
   * @return a value in percentage of the width of the door or the window.
   * @since 6.0
   */
  public float getWallWidth() {
    return this.wallWidth;
  }

  /**
   * Sets the width of the wall part in which this door or window should be placed.
   * Once this piece is updated, listeners added to this piece will receive a change notification.
   * @param wallWidth a value in percentage of the width of the door or the window.
   * @since 6.0
   */
  public void setWallWidth(float wallWidth) {
    if (wallWidth != this.wallWidth) {
      float oldWallWidth = this.wallWidth;
      this.wallWidth = wallWidth;
      firePropertyChange(Property.WALL_WIDTH.name(), oldWallWidth, wallWidth);
    }
  }

  /**
   * Returns the distance between the left side of this door or window and the wall part where it should be placed.
   * @return a distance in percentage of the width of the door or the window.
   * @since 6.0
   */
  public float getWallLeft() {
    return this.wallLeft;
  }

  /**
   * Sets the distance between the left side of this door or window and the wall part where it should be placed.
   * Once this piece is updated, listeners added to this piece will receive a change notification.
   * @param wallLeft a distance in percentage of the width of the door or the window.
   * @since 6.0
   */
  public void setWallLeft(float wallLeft) {
    if (wallLeft != this.wallLeft) {
      float oldWallLeft = this.wallLeft;
      this.wallLeft = wallLeft;
      firePropertyChange(Property.WALL_LEFT.name(), oldWallLeft, wallLeft);
    }
  }

  /**
   * Returns the height of the wall part in which this door or window should be placed.
   * Once this piece is updated, listeners added to this piece will receive a change notification.
   * @return a value in percentage of the height of the door or the window.
   * @since 6.0
   */
  public float getWallHeight() {
    return this.wallHeight;
  }

  /**
   * Sets the height of the wall part in which this door or window should be placed.
   * Once this piece is updated, listeners added to this piece will receive a change notification.
   * @param wallHeight a value in percentage of the height of the door or the window.
   * @since 6.0
   */
  public void setWallHeight(float wallHeight) {
    if (wallHeight != this.wallHeight) {
      float oldWallHeight = this.wallHeight;
      this.wallHeight = wallHeight;
      firePropertyChange(Property.WALL_HEIGHT.name(), oldWallHeight, wallHeight);
    }
  }

  /**
   * Returns the distance between the left side of this door or window and the wall part where it should be placed.
   * @return a distance in percentage of the height of the door or the window.
   * @since 6.0
   */
  public float getWallTop() {
    return this.wallTop;
  }

  /**
   * Sets the distance between the top side of this door or window and the wall part where it should be placed.
   * Once this piece is updated, listeners added to this piece will receive a change notification.
   * @param wallTop a distance in percentage of the height of the door or the window.
   * @since 6.0
   */
  public void setWallTop(float wallTop) {
    if (wallTop != this.wallTop) {
      float oldWallTop = this.wallTop;
      this.wallTop = wallTop;
      firePropertyChange(Property.WALL_TOP.name(), oldWallTop, wallTop);
    }
  }

  /**
   * Returns a copy of the sashes attached to this door or window.
   * If no sash is defined an empty array is returned.
   */
  public Sash [] getSashes() {
    if (this.sashes.length == 0) {
      return this.sashes;
    } else {
      return this.sashes.clone();
    }
  }

  /**
   * Sets the sashes attached to this door or window. Once this piece is updated,
   * listeners added to this piece will receive a change notification.
   * @param sashes sashes of this window.
   * @since 6.0
   */
  public void setSashes(Sash [] sashes) {
    if (!Arrays.equals(sashes, this.sashes)) {
      Sash [] oldSashes = this.sashes.length == 0
          ? this.sashes
          : this.sashes.clone();
      this.sashes = sashes.length == 0
          ? sashes
          : sashes.clone();
      firePropertyChange(Property.SASHES.name(), oldSashes, sashes);
    }
  }

  /**
   * Returns the shape used to cut out walls that intersect this door or window.
   * @since 4.2
   */
  public String getCutOutShape() {
    return this.cutOutShape;
  }

  /**
   * Sets the shape used to cut out walls that intersect this door or window.
   * Once this piece is updated, listeners added to this piece will receive a change notification.
   * @param cutOutShape a SVG path element.
   * @since 6.5
   */
  public void setCutOutShape(String cutOutShape) {
    if (cutOutShape != this.cutOutShape
        && (cutOutShape == null || !cutOutShape.equals(this.cutOutShape))) {
      String oldCutOutShape = this.cutOutShape;
      this.cutOutShape = cutOutShape;
      firePropertyChange(Property.CUT_OUT_SHAPE.name(), oldCutOutShape, cutOutShape);
    }
  }

  /**
   * Returns <code>true</code> if this door or window should cut out the both sides
   * of the walls it intersects, even if its front or back side are within the wall thickness.
   * @since 5.5
   */
  public boolean isWallCutOutOnBothSides() {
    return this.wallCutOutOnBothSides;
  }

  /**
   * Sets whether the width and depth of the new door or window may
   * be changed independently from each other.
   * Once this piece is updated, listeners added to this piece will receive a change notification.
   * @since 6.5
   */
  public void setWallCutOutOnBothSides(boolean wallCutOutOnBothSides) {
    if (wallCutOutOnBothSides != this.wallCutOutOnBothSides) {
      this.wallCutOutOnBothSides = wallCutOutOnBothSides;
      firePropertyChange(Property.WALL_CUT_OUT_ON_BOTH_SIDES.name(),
          !wallCutOutOnBothSides, wallCutOutOnBothSides);
    }
  }

  /**
   * Returns <code>false</code> if the width and depth of this door or window may
   * not be changed independently from each other. When <code>false</code>, this door or window
   * will also make a hole in the wall when it's placed whatever its depth if its
   * {@link #isBoundToWall() bouldToWall} flag is <code>true</code>.
   * @since 5.5
   */
  @Override
  public boolean isWidthDepthDeformable() {
    return this.widthDepthDeformable;
  }

  /**
   * Sets whether the width and depth of the new door or window may
   * be changed independently from each other.
   * Once this piece is updated, listeners added to this piece will receive a change notification.
   * @since 6.5
   */
  public void setWidthDepthDeformable(boolean widthDepthDeformable) {
    if (widthDepthDeformable != this.widthDepthDeformable) {
      this.widthDepthDeformable = widthDepthDeformable;
      firePropertyChange(Property.WIDTH_DEPTH_DEFORMABLE.name(),
          !widthDepthDeformable, widthDepthDeformable);
    }
  }

  /**
   * Returns <code>true</code> if the location and the size of this door or window
   * were bound to a wall, last time they were updated.
   */
  public boolean isBoundToWall() {
    return this.boundToWall;
  }

  /**
   * Sets whether the location and the size of this door or window
   * were bound to a wall, last time they were updated.
   * Once this piece is updated, listeners added to this piece will receive a change notification.
   */
  public void setBoundToWall(boolean boundToWall) {
    if (boundToWall != this.boundToWall) {
      this.boundToWall = boundToWall;
      firePropertyChange(Property.BOUND_TO_WALL.name(), !boundToWall, boundToWall);
    }
  }

  /**
   * Sets the abscissa of this door or window and
   * resets its {@link #isBoundToWall() boundToWall} flag if the abscissa changed.
   */
  @Override
  public void setX(float x) {
    if (getX() != x) {
      this.boundToWall = false;
    }
    super.setX(x);
  }

  /**
   * Sets the ordinate of this door or window and
   * resets its {@link #isBoundToWall() boundToWall} flag if the ordinate changed.
   */
  @Override
  public void setY(float y) {
    if (getY() != y) {
      this.boundToWall = false;
    }
    super.setY(y);
  }

  /**
   * Sets the angle of this door or window and
   * resets its {@link #isBoundToWall() boundToWall} flag if the angle changed.
   */
  @Override
  public void setAngle(float angle) {
    if (getAngle() != angle) {
      this.boundToWall = false;
    }
    super.setAngle(angle);
  }

  /**
   * Sets the depth of this door or window and
   * resets its {@link #isBoundToWall() boundToWall} flag if the depth changed.
   */
  @Override
  public void setDepth(float depth) {
    if (getDepth() != depth) {
      this.boundToWall = false;
    }
    super.setDepth(depth);
  }

  /**
   * Returns always <code>true</code>.
   */
  @Override
  public boolean isDoorOrWindow() {
    return true;
  }

  /**
   * Returns a copy of this door or window.
   * @since 6.4
   */
  @Override
  public HomeObject duplicate() {
    HomeDoorOrWindow copy = (HomeDoorOrWindow)super.duplicate();
    copy.boundToWall = false;
    return copy;
  }
}
