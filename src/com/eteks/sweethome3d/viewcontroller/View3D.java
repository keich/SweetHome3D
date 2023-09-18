/*
 * View3D.java 2 aug 2023
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
package com.eteks.sweethome3d.viewcontroller;

import com.eteks.sweethome3d.model.Selectable;

/**
 * The view that displays the 3D view of a home.
 * @author Emmanuel Puybaret
 * @since 7.2
 */
public interface View3D extends View {
  /**
   * Returns the closest {@link Selectable} object at component coordinates (x, y),
   * or <code>null</code> if not found.
   */
  public Selectable getClosestSelectableItemAt(int x, int y);

  /**
   * Returns the coordinates of the 3D point matching the point (x, y) in component coordinates space.
   */
  public float [] convertPixelLocationToVirtualWorld(int x, int y);

  /**
   * Returns the coordinates of the 3D point intersecting the plane at the given <code>elevation</code>
   * in the direction joining camera location and component coordinates (x, y).
   */
  public float [] getVirtualWorldPointAt(int x, int y, float elevation);
}