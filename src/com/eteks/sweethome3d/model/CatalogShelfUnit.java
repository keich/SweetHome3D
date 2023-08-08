/*
 * CatalogShelfUnit.java 26 mai 2023
 *
 * Sweet Home 3D, Copyright (c) 2023 Emmanuel PUYBARET / eTeks <info@eteks.com>
 *
 * This program is free software; you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation; either version 2 of the License, or (at your option) any later
 * version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 *
 * You should have received a copy of the GNU General Public License along with
 * this program; if not, write to the Free Software Foundation, Inc., 59 Temple
 * Place, Suite 330, Boston, MA 02111-1307 USA
 */
package com.eteks.sweethome3d.model;

import java.math.BigDecimal;
import java.util.Map;

/**
 * A catalog piece of furniture.
 * @author Emmanuel Puybaret
   * @since 7.2
 */
public class CatalogShelfUnit extends CatalogPieceOfFurniture implements ShelfUnit {
  private static final float []     EMPTY_SHELF_ELEVATIONS = {};
  private static final BoxBounds [] EMPTY_SHELF_BOXES = {};

  private final float []     shelfElevations;
  private final BoxBounds [] shelfBoxes;

  /**
   * Creates an unmodifiable catalog shelf unit of the default catalog.
   * @param id    the id of the shelf unit or <code>null</code>
   * @param name  the name of the shelf unit
   * @param description the description of the shelf unit
   * @param information additional information associated to the shelf unit
   * @param license license of the shelf unit
   * @param tags tags associated to the shelf unit
   * @param creationDate creation date of the shelf unit in milliseconds since the epoch
   * @param grade grade of the piece of furniture or <code>null</code>
   * @param icon content of the icon of the shelf unit
   * @param planIcon content of the icon of the shelf unit displayed in plan
   * @param model content of the 3D model of the shelf unit
   * @param width  the width in centimeters of the shelf unit
   * @param depth  the depth in centimeters of the shelf unit
   * @param height  the height in centimeters of the shelf unit
   * @param elevation  the elevation in centimeters of the shelf unit
   * @param dropOnTopElevation  a percentage of the height at which should be placed
   *            an object dropped on the shelf unit
   * @param shelfElevations shelf elevation(s) at which other objects can be placed on the shelf unit
   * @param shelfBoxes coordinates of the shelf box(es) in which other objects can be placed in the shelf unit
   * @param movable if <code>true</code>, the shelf unit is movable
   * @param staircaseCutOutShape the shape used to cut out upper levels when they intersect
   *            with the piece like a staircase
   * @param modelRotation the rotation 3 by 3 matrix applied to the piece model
   * @param modelFlags flags which should be applied to piece model
   * @param modelSize size of the 3D model of the shelf unit
   * @param creator the creator of the model
   * @param resizable if <code>true</code>, the size of the shelf unit may be edited
   * @param deformable if <code>true</code>, the width, depth and height of the shelf unit may
   *            change independently from each other
   * @param texturable if <code>false</code> this piece should always keep the same color or texture
   * @param horizontallyRotatable if <code>false</code> this piece
   *            should not rotate around an horizontal axis
   * @param price the price of the shelf unit or <code>null</code>
   * @param valueAddedTaxPercentage the Value Added Tax percentage applied to the
   *             price of the shelf unit or <code>null</code>
   * @param currency the price currency, noted with ISO 4217 code, or <code>null</code>
   * @param properties additional properties associating a key to a value or <code>null</code>
   * @param contents   additional contents associating a key to a value or <code>null</code>
   */
  public CatalogShelfUnit(String id, String name, String description,
                                 String information, String license,
                                 String [] tags, Long creationDate, Float grade,
                                 Content icon, Content planIcon, Content model,
                                 float width, float depth, float height,
                                 float elevation, float dropOnTopElevation,
                                 float [] shelfElevations, BoxBounds [] shelfBoxes,
                                 boolean movable, String staircaseCutOutShape,
                                 float [][] modelRotation, int modelFlags, Long modelSize, String creator,
                                 boolean resizable, boolean deformable, boolean texturable, boolean horizontallyRotatable,
                                 BigDecimal price, BigDecimal valueAddedTaxPercentage, String currency,
                                 Map<String, String> properties, Map<String, Content> contents) {
    super(id, name, description, information, license, tags, creationDate, grade, icon, planIcon, model, width, depth,
        height, elevation, dropOnTopElevation, movable, staircaseCutOutShape, modelRotation, modelFlags,
        modelSize, creator, resizable, deformable, texturable, horizontallyRotatable,
        price, valueAddedTaxPercentage, currency, properties, contents);
    this.shelfElevations = shelfElevations != null && shelfElevations.length > 0
        ? shelfElevations.clone()
        : EMPTY_SHELF_ELEVATIONS;
    this.shelfBoxes = shelfBoxes != null && shelfBoxes.length > 0
        ? shelfBoxes.clone()
        : EMPTY_SHELF_BOXES;
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
   * Returns the coordinates of the shelf box(es) in which other objects can be placed in this shelf unit.
   */
  public BoxBounds [] getShelfBoxes() {
    return this.shelfBoxes.clone();
  }
}
