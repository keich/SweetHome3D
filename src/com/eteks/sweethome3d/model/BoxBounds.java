/*
 * BoxBounds.java 1 juin 2023
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
package com.eteks.sweethome3d.model;

import java.io.Serializable;

/**
 * Bounds of a box.
 * @author Emmanuel Puybaret
 * @since 7.2
 */
public class BoxBounds implements Serializable {
  private static final long serialVersionUID = 1L;

  private final float xLower;
  private final float yLower;
  private final float zLower;
  private final float xUpper;
  private final float yUpper;
  private final float zUpper;

  /**
   * Creates the bounds of a box from the coordinates of its lower and upper corners.
   */
  public BoxBounds(float xLower, float yLower, float zLower,
                   float xUpper, float yUpper, float zUpper) {
    this.xLower = xLower;
    this.yLower = yLower;
    this.zLower = zLower;
    this.xUpper = xUpper;
    this.yUpper = yUpper;
    this.zUpper = zUpper;
  }

  /**
   * Returns the abscissa of the lower corner of these bounds.
   */
  public float getXLower() {
    return this.xLower;
  }

  /**
   * Returns the ordinate of the lower corner of these bounds.
   */
  public float getYLower() {
    return this.yLower;
  }

  /**
   * Returns the elevation of the lower corner of these bounds.
   */
  public float getZLower() {
    return this.zLower;
  }

  /**
   * Returns the abscissa of the upper corner of these bounds.
   */
  public float getXUpper() {
    return this.xUpper;
  }

  /**
   * Returns the ordinate of the upper corner of these bounds.
   */
  public float getYUpper() {
    return this.yUpper;
  }

  /**
   * Returns the elevation of the upper corner of these bounds.
   */
  public float getZUpper() {
    return this.zUpper;
  }

  /**
   * Returns <code>true</code> if these bounds are equal to <code>object</code>.
   */
  @Override
  public boolean equals(Object object) {
    if (object instanceof BoxBounds) {
      BoxBounds bounds = (BoxBounds)object;
      return bounds.xLower == this.xLower
          && bounds.yLower == this.yLower
          && bounds.zLower == this.zLower
          && bounds.xUpper == this.xUpper
          && bounds.yUpper == this.yUpper
          && bounds.zUpper == this.zUpper;
    }
    return false;
  }

  /**
   * Returns a hash code for these bounds.
   */
  @Override
  public int hashCode() {
    int hashCode = Float.floatToIntBits(this.xLower)
        + Float.floatToIntBits(this.yLower)
        + Float.floatToIntBits(this.zLower);
    hashCode += 31 * (Float.floatToIntBits(this.xUpper)
        + Float.floatToIntBits(this.yUpper)
        + Float.floatToIntBits(this.zUpper));
    return hashCode;
  }
}
