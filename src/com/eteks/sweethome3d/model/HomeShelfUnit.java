/*
 * HomeShelfUnit.java 26 mais 2023
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

import java.util.Arrays;

/**
 * A shelf unit in {@linkplain Home home}.
 * @author Emmanuel Puybaret
 * @since  7.2
 */
public class HomeShelfUnit extends HomePieceOfFurniture implements ShelfUnit {
  private static final long serialVersionUID = 1L;

  /**
   * The properties of a shelf unit that may change. <code>PropertyChangeListener</code>s added
   * to a shelf unit will be notified under a property name equal to the string value of one these properties.
   */
  public enum Property {SHELF_ELEVATIONS, SHELF_BOXES};

  private float []     shelfElevations;
  private BoxBounds [] shelfBoxes;

  /**
   * Creates a shelf unit from an existing one.
   * No additional properties will be copied.
   * @param shelfUnit the shelfUnit from which data are copied
   */
  public HomeShelfUnit(ShelfUnit shelfUnit) {
    this(shelfUnit, EMPTY_PROPERTY_ARRAY);
  }

  /**
   * Creates a shelf unit from an existing one.
   * No additional properties will be copied.
   * @param shelfUnit the shelfUnit from which data are copied
   * @param copiedProperties the names of the additional properties which should be copied from the existing piece
   *                         or <code>null</code> if all properties should be copied.
   */
  public HomeShelfUnit(ShelfUnit shelfUnit, String [] copiedProperties) {
    this(createId("shelfUnit"), shelfUnit, copiedProperties);
  }

  /**
   * Creates a shelf unit from an existing one.
   * No additional properties will be copied.
   * @param id    the ID of the shelfUnit
   * @param shelfUnit the shelfUnit from which data are copied
   */
  public HomeShelfUnit(String id, ShelfUnit shelfUnit) {
    this(id, shelfUnit, EMPTY_PROPERTY_ARRAY);
  }

  /**
   * Creates a shelf unit from an existing one.
   * No additional properties will be copied.
   * @param id    the ID of the shelfUnit
   * @param shelfUnit the shelfUnit from which data are copied
   * @param copiedProperties the names of the additional properties which should be copied from the existing piece
   *                         or <code>null</code> if all properties should be copied.
   */
  public HomeShelfUnit(String id, ShelfUnit shelfUnit, String [] copiedProperties) {
    super(id, shelfUnit, copiedProperties);
    this.shelfElevations = shelfUnit.getShelfElevations();
    this.shelfBoxes = shelfUnit.getShelfBoxes();
  }

  /**
   * Returns the elevation(s) at which other objects can be placed on this shelf unit.
   */
  public float [] getShelfElevations() {
    return this.shelfElevations != null
        ? this.shelfElevations.clone()
        : null;
  }

  /**
   * Sets the elevation(s) at which other objects can be placed on this shelf unit.
   * Once this light is updated, listeners added to this light will receive a change notification.
   * @param shelfElevations elevation of the shelves
   */
  public void setShelfElevations(float [] shelfElevations) {
    if (!Arrays.equals(shelfElevations, this.shelfElevations)) {
      float [] oldShelfElevations = this.shelfElevations.length == 0
          ? this.shelfElevations
          : this.shelfElevations.clone();
      this.shelfElevations = shelfElevations.length == 0
          ? shelfElevations
          : shelfElevations.clone();
      firePropertyChange(Property.SHELF_ELEVATIONS.name(), oldShelfElevations, shelfElevations);
    }
  }

  /**
   * Returns the coordinates of the shelf box(es) in which other objects can be placed in this shelf unit.
   */
  public BoxBounds [] getShelfBoxes() {
    return this.shelfBoxes.clone();
  }

  /**
   * Sets the coordinates of the shelf box(es) in which other objects can be placed in this shelf.
   * Once this light is updated, listeners added to this light will receive a change notification.
   * @param shelfElevations elevation of the shelves
   */
  public void setShelfBoxes(BoxBounds [] shelfBoxes) {
    if (!Arrays.equals(shelfBoxes, this.shelfBoxes)) {
      BoxBounds [] oldShelfBoxes = this.shelfBoxes.length == 0
          ? this.shelfBoxes
          : this.shelfBoxes.clone();
      this.shelfBoxes = shelfBoxes.length == 0
          ? shelfBoxes
          : shelfBoxes.clone();
      firePropertyChange(Property.SHELF_BOXES.name(), oldShelfBoxes, shelfBoxes);
    }
  }

  /**
   * Returns a clone of this shelf unit.
   */
  @Override
  public HomeShelfUnit clone() {
    return (HomeShelfUnit)super.clone();
  }
}
