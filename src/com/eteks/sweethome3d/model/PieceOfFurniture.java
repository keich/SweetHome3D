/*
 * PieceOfFurniture.java 15 mai 2006
 *
 * Sweet Home 3D, Copyright (c) 2006 Emmanuel PUYBARET / eTeks <info@eteks.com>
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

import java.math.BigDecimal;
import java.util.Collection;

/**
 * A piece of furniture.
 * @author Emmanuel Puybaret
 */
public interface PieceOfFurniture {
  /**
   * The default cut out shape that covers a 1 unit wide square.
   */
  public static final String      DEFAULT_CUT_OUT_SHAPE = "M0,0 v1 h1 v-1 z";
  /**
   * Identity model rotation matrix.
   */
  public static final float [][]  IDENTITY_ROTATION = new float [][] {{1, 0, 0}, {0, 1, 0}, {0, 0, 1}};
  /**
   * The flag used to specify that the back faces of a 3D model should be shown.
   */
  public static final int SHOW_BACK_FACE = 0x01;
  /**
   * The flag used to specify that the shapes of a 3D model which uses a material prefixed by "edge_color" should be hidden.
   */
  public static final int HIDE_EDGE_COLOR_MATERIAL = 0x02;

  /**
   * Returns the name of this piece of furniture.
   */
  public abstract String getName();

  /**
   * Returns the description of this piece of furniture.
   */
  public abstract String getDescription();

  /**
   * Returns the additional information associated to this piece, or <code>null</code>.
   * @since 4.2
   */
  public String getInformation();

  /**
   * Returns the license of this piece of furniture.
   * @since 7.2
   */
  public abstract String getLicense();

  /**
   * Returns the depth of this piece of furniture.
   */
  public abstract float getDepth();

  /**
   * Returns the height of this piece of furniture.
   */
  public abstract float getHeight();

  /**
   * Returns the width of this piece of furniture.
   */
  public abstract float getWidth();

  /**
   * Returns the elevation of this piece of furniture.
   */
  public abstract float getElevation();

  /**
   * Returns the elevation at which should be placed an object dropped on this piece.
   * @return a percentage of the height of this piece. A negative value means that the piece
   *         should be ignored when an object is dropped on it.
   * @since 4.4
   */
  public abstract float getDropOnTopElevation();

  /**
   * Returns <code>true</code> if this piece of furniture is movable.
   */
  public abstract boolean isMovable();

  /**
   * Returns <code>true</code> if this piece of furniture is a door or a window.
   * As this method existed before {@linkplain DoorOrWindow DoorOrWindow} interface,
   * you shouldn't rely on the value returned by this method to guess if a piece
   * is an instance of <code>DoorOrWindow</code> class.
   */
  public abstract boolean isDoorOrWindow();

  /**
   * Returns the icon of this piece of furniture.
   */
  public abstract Content getIcon();

  /**
   * Returns the icon of this piece of furniture displayed in plan or <code>null</code>.
   * @since 2.2
   */
  public abstract Content getPlanIcon();

    /**
   * Returns the 3D model of this piece of furniture.
   */
  public abstract Content getModel();

  /**
   * Returns flags that should apply on the model of this piece of furniture.
   * @since 7.0
   */
  public abstract int getModelFlags();

  /**
   * Returns the size of the 3D model of this piece of furniture or <code>null</code> if not known.
   * @since 5.5
   */
  public abstract Long getModelSize();

  /**
   * Returns the rotation 3 by 3 matrix of this piece of furniture that ensures
   * its model is correctly oriented.
   */
  public float [][] getModelRotation();

  /**
   * Returns the shape used to cut out upper levels when they intersect with the piece
   * like a staircase.
   * @since 3.4
   */
  public String getStaircaseCutOutShape();

  /**
   * Returns the creator of this piece or <code>null</code>.
   * @since 4.2
   */
  public String getCreator();

  /**
   * Returns <code>true</code> if the back face of the piece of furniture
   * model should be displayed.
   */
  public abstract boolean isBackFaceShown();

  /**
   * Returns the color of this piece of furniture.
   */
  public abstract Integer getColor();

  /**
   * Returns <code>true</code> if this piece is resizable.
   */
  public abstract boolean isResizable();

  /**
   * Returns <code>true</code> if this piece is deformable. The width, depth and height
   * of a deformable piece may change independently from each other.
   * @since 3.0
   */
  public abstract boolean isDeformable();

  /**
   * Returns <code>true</code> if the width and depth of this piece may
   * be changed independently from each other.
   * @since 5.5
   */
  boolean isWidthDepthDeformable();

  /**
   * Returns <code>false</code> if this piece should always keep the same color or texture.
   * @since 3.0
   */
  public abstract boolean isTexturable();

  /**
   * Returns <code>false</code> if this piece should not rotate around an horizontal axis.
   * @since 5.5
   */
 boolean isHorizontallyRotatable();

  /**
   * Returns the price of this piece of furniture or <code>null</code>.
   */
  public abstract BigDecimal getPrice();

  /**
   * Returns the Value Added Tax percentage applied to the price of this piece of furniture.
   */
  public abstract BigDecimal getValueAddedTaxPercentage();

  /**
   * Returns the price currency, noted with ISO 4217 code, or <code>null</code>
   * if it has no price or default currency should be used.
   * @since 3.4
   */
  public abstract String getCurrency();

  /**
   * Returns the value of an additional property <code>name</code> of this piece.
   * @return the value of the property or <code>null</code> if it doesn't exist or if it's not a string.
   * @since 7.2
   */
  public String getProperty(String name);

  /**
   * Returns the names of the additional properties of this piece.
   * @return a collection of all the names of the properties
   * @since 7.2
   */
  public Collection<String> getPropertyNames();

  /**
   * Returns the value of an additional content <code>name</code> associated to this piece.
   * @return the value of the content or <code>null</code> if it doesn't exist or if it's not a content.
   * @since 7.2
   */
  public Content getContentProperty(String name);

  /**
   * Returns <code>true</code> if the type of given additional property is a content.
   * @since 7.2
   */
  public boolean isContentProperty(String name);
}