/*
 * HomeLight.java 12 mars 2009
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

import java.util.Arrays;

/**
 * A light in {@linkplain Home home}.
 * @author Emmanuel Puybaret
 * @since  1.7
 */
public class HomeLight extends HomePieceOfFurniture implements Light {
  private static final long serialVersionUID = 1L;

  /**
   * The properties of a light that may change. <code>PropertyChangeListener</code>s added
   * to a light will be notified under a property name equal to the string value of one these properties.
   */
  public enum Property {POWER, LIGHT_SOURCES, LIGHT_SOURCE_MATERIAL_NAMES};

  private LightSource [] lightSources;
  private String []      lightSourceMaterialNames;
  private float power;

  /**
   * Creates a home light from an existing one.
   * @param light the light from which data are copied
   */
  public HomeLight(Light light) {
    this(createId("light"), light);
  }

  /**
   * Creates a home light from an existing one.
   * @param id    the ID of the light
   * @param light the light from which data are copied
   * @since 6.4
   */
  public HomeLight(String id, Light light) {
    super(id, light);
    this.lightSources = light.getLightSources();
    this.lightSourceMaterialNames = light.getLightSourceMaterialNames();
    this.power = 0.5f;
  }

  /**
   * Returns the sources managed by this light. Each light source point
   * is a percentage of the width, the depth and the height of this light.
   * with the abscissa origin at the left side of the piece,
   * the ordinate origin at the front side of the piece
   * and the elevation origin at the bottom side of the piece.
   * @return a copy of light sources array.
   */
  public LightSource [] getLightSources() {
    if (this.lightSources.length == 0) {
      return this.lightSources;
    } else {
      return this.lightSources.clone();
    }
  }

  /**
   * Sets the sources managed by this light. Once this light is updated,
   * listeners added to this light will receive a change notification.
   * @param lightSources sources of the light
   * @since 6.5
   */
  public void setLightSources(LightSource [] lightSources) {
    if (!Arrays.equals(lightSources, this.lightSources)) {
      LightSource [] oldLightSources = this.lightSources.length == 0
          ? this.lightSources
          : this.lightSources.clone();
      this.lightSources = lightSources.length == 0
          ? lightSources
          : lightSources.clone();
      firePropertyChange(Property.LIGHT_SOURCES.name(), oldLightSources, lightSources);
    }
  }

  /**
   * Returns the material names of the light sources in the 3D model managed by this light.
   * @return a copy of light source material names array.
   * @since 7.0
   */
  public String [] getLightSourceMaterialNames() {
    if (this.lightSourceMaterialNames.length == 0) {
      return this.lightSourceMaterialNames;
    } else {
      return this.lightSourceMaterialNames.clone();
    }
  }

  /**
   * Sets the material names of the light sources in the 3D model managed by this light.
   * Once this light is updated, listeners added to this light will receive a change notification.
   * @param lightSourceMaterialNames material names of the light sources
   * @since 7.0
   */
  public void setLightSourceMaterialNames(String [] lightSourceMaterialNames) {
    if (!Arrays.equals(lightSourceMaterialNames, this.lightSourceMaterialNames)) {
      String [] oldLightSourceMaterialNames = this.lightSourceMaterialNames.length == 0
          ? this.lightSourceMaterialNames
          : this.lightSourceMaterialNames.clone();
      this.lightSourceMaterialNames = lightSourceMaterialNames.length == 0
          ? lightSourceMaterialNames
          : lightSourceMaterialNames.clone();
      firePropertyChange(Property.LIGHT_SOURCE_MATERIAL_NAMES.name(), oldLightSourceMaterialNames, lightSourceMaterialNames);
    }
  }

  /**
   * Returns the power of this light.
   * @since 3.0
   */
  public float getPower() {
    return this.power;
  }

  /**
   * Sets the power of this light. Once this light is updated,
   * listeners added to this light will receive a change notification.
   * @param power power of the light
   * @since 3.0
   */
  public void setPower(float power) {
    if (power != this.power) {
      float oldPower = this.power;
      this.power = power;
      firePropertyChange(Property.POWER.name(), oldPower, power);
    }
  }

  /**
   * Returns a clone of this light.
   */
  @Override
  public HomeLight clone() {
    return (HomeLight)super.clone();
  }
}
