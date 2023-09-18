/*
 * Component3DTransferHandler.java 1 août 2023
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
package com.eteks.sweethome3d.swing;

import java.awt.KeyboardFocusManager;
import java.awt.Point;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.geom.Point2D;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import javax.swing.JComponent;
import javax.swing.SwingUtilities;

import com.eteks.sweethome3d.model.Camera;
import com.eteks.sweethome3d.model.Elevatable;
import com.eteks.sweethome3d.model.Home;
import com.eteks.sweethome3d.model.HomePieceOfFurniture;
import com.eteks.sweethome3d.model.HomeShelfUnit;
import com.eteks.sweethome3d.model.Level;
import com.eteks.sweethome3d.model.PieceOfFurniture;
import com.eteks.sweethome3d.model.Selectable;
import com.eteks.sweethome3d.model.Wall;
import com.eteks.sweethome3d.viewcontroller.HomeController;
import com.eteks.sweethome3d.viewcontroller.View3D;

/**
 * A transfer handler for the 3D view.
 * @author Emmanuel Puybaret
 */
public class Component3DTransferHandler extends LocatedTransferHandler {
  private final Home           home;
  private final HomeController homeController;
  private List<Selectable>     copiedItems;

  /**
   * Creates a handler able to transfer furniture and walls in plan.
   */
  public Component3DTransferHandler(Home home,
                                    HomeController homeController) {
    this.home = home;
    this.homeController = homeController;
  }

  /**
   * Returns <code>COPY_OR_MOVE</code>.
   */
  @Override
  public int getSourceActions(JComponent source) {
    return COPY_OR_MOVE;
  }

  /**
   * Returns a transferable object that contains a copy of the selected items in home
   * and an image of the selected items.
   */
  @Override
  protected Transferable createTransferable(final JComponent source) {
    this.copiedItems = this.home.getSelectedItems();
    return new HomeTransferableList(this.copiedItems);
  }

  /**
   * Removes the copied element once moved.
   */
  @Override
  protected void exportDone(JComponent source, Transferable data, int action) {
    if (action == MOVE) {
      this.homeController.cut(this.copiedItems);
    }
    this.copiedItems = null;
    this.homeController.enablePasteAction();
  }

  /**
   * Returns <code>true</code> if <code>flavors</code> contains
   * {@link HomeTransferableList#HOME_FLAVOR HOME_FLAVOR} flavor
   * or <code>DataFlavor.javaFileListFlavor</code> flavor.
   */
  @Override
  protected boolean canImportFlavor(DataFlavor [] flavors) {
    Level selectedLevel = this.home.getSelectedLevel();
    List<DataFlavor> flavorList = Arrays.asList(flavors);
    return (selectedLevel == null || selectedLevel.isViewable())
        && flavorList.contains(HomeTransferableList.HOME_FLAVOR)
        // Refuse home data transferred from the furniture list of the same home
        && !SwingUtilities.isDescendingFrom(KeyboardFocusManager.getCurrentKeyboardFocusManager().getFocusOwner(),
              (JComponent)this.homeController.getFurnitureController().getView());
  }

  /**
   * Adds items contained in <code>transferable</code> to home.
   */
  @Override
  public boolean importData(JComponent destination, Transferable transferable) {
    if (canImportFlavor(transferable.getTransferDataFlavors())) {
      try {
        List<DataFlavor> flavorList = Arrays.asList(transferable.getTransferDataFlavors());
        return importHomeTransferableList(destination,
            (List<Selectable>)transferable.getTransferData(HomeTransferableList.HOME_FLAVOR));
      } catch (UnsupportedFlavorException ex) {
        throw new RuntimeException("Can't import", ex);
      } catch (IOException ex) {
        throw new RuntimeException("Can't access to data", ex);
      }
    } else {
      return false;
    }
  }

  private boolean importHomeTransferableList(final JComponent destination,
                                             final List<Selectable> transferedItems) {
    if (isDrop()) {
      Level dropLevel = getDropModelLevel(destination);
      float [] dropLocation = getDropModelLocation(destination, transferedItems, dropLevel);
      this.homeController.drop(transferedItems, this.homeController.getHomeController3D().getView(), dropLevel,
            dropLocation [0], dropLocation [1], dropLocation.length == 3 ? dropLocation [2] : null);
    } else {
      this.homeController.paste(transferedItems);
    }
    return true;
  }

  /**
   * Returns the level where drop location should occur.
   */
  private Level getDropModelLevel(JComponent destination) {
    if (destination instanceof View3D) {
      View3D view3D = (View3D)destination;
      Point dropLocation = getDropLocation();
      SwingUtilities.convertPointFromScreen(dropLocation, destination);
      Selectable closestItem = view3D.getClosestSelectableItemAt(dropLocation.x, dropLocation.y);
      Level selectedLevel = this.home.getSelectedLevel();
      if (closestItem != null
          && closestItem instanceof Elevatable
          && !((Elevatable)closestItem).isAtLevel(selectedLevel)) {
        return ((Elevatable)closestItem).getLevel();
      }
    }
    return this.home.getSelectedLevel();
  }

  /**
   * Returns the drop location converted in model coordinates space.
   */
  private float [] getDropModelLocation(JComponent destination, List<Selectable> transferedItems, Level dropLevel) {
    float [] floorLocation = {0, 0, 0};
    if (destination instanceof View3D) {
      View3D view3D = (View3D)destination;
      Point dropLocation = getDropLocation();
      SwingUtilities.convertPointFromScreen(dropLocation, destination);
      Selectable closestItem = view3D.getClosestSelectableItemAt(dropLocation.x, dropLocation.y);
      float floorElevation = 0;
      if (dropLevel != null) {
        floorElevation = dropLevel.getElevation();
      }
      if (closestItem instanceof HomePieceOfFurniture) {
        HomePieceOfFurniture closestPiece = (HomePieceOfFurniture)closestItem;
        floorLocation = new float [] {closestPiece.getX(), closestPiece.getY()};
        if (transferedItems.size() == 1
            && transferedItems.get(0) instanceof PieceOfFurniture) {
          float [] pointOnFloor = view3D.getVirtualWorldPointAt(dropLocation.x, dropLocation.y, floorElevation);
          float [] intersectionWithPieceMiddle = computeIntersection(pointOnFloor [0], pointOnFloor [1], this.home.getCamera().getX(), this.home.getCamera().getY(),
              floorLocation [0], floorLocation [1], floorLocation [0] + (float)Math.cos(closestPiece.getAngle()), floorLocation [1] + (float)Math.sin(closestPiece.getAngle()));
          if (Point2D.distance(intersectionWithPieceMiddle [0], intersectionWithPieceMiddle [1], closestPiece.getX(), closestPiece.getY()) < closestPiece.getWidth() / 2) {
            floorLocation = intersectionWithPieceMiddle;
          }
          PieceOfFurniture transferedPiece = (PieceOfFurniture)transferedItems.get(0);
          floorLocation [0] -= transferedPiece.getWidth() / 2;
          floorLocation [1] -= transferedPiece.getDepth() / 2;
          float elevation;
          if (closestItem instanceof HomeShelfUnit) {
            Camera camera = this.home.getCamera();
            float distancePointOnFloorToCamera = (float)Point2D.distance(pointOnFloor [0], pointOnFloor [1], camera.getX(), camera.getY());
            float distancePointOnFloorToLocation = (float)Point2D.distance(pointOnFloor [0], pointOnFloor [1], floorLocation [0], floorLocation [1]);
            elevation = (camera.getZ() - (this.home.getSelectedLevel() != null ? this.home.getSelectedLevel().getElevation() : 0))
                / distancePointOnFloorToCamera * distancePointOnFloorToLocation;
          } else if (closestPiece.isHorizontallyRotated()) {
            elevation = closestPiece.getElevation() + closestPiece.getHeightInPlan();
          } else if (closestPiece.getDropOnTopElevation() >= 0) {
            elevation = closestPiece.getElevation() + closestPiece.getHeight() * closestPiece.getDropOnTopElevation();
          } else {
            elevation = 0;
          }
          floorLocation = new float [] {floorLocation [0], floorLocation [1], elevation};
        }
      } else if (closestItem instanceof Wall
                  && ((Wall)closestItem).getArcExtent() == null
                  && transferedItems.size() == 1) {
        float[] pointOnFloor = view3D.getVirtualWorldPointAt(dropLocation.x, dropLocation.y, floorElevation);
        // Compute intersection between camera - pointOnFloor line and left/right sides of the wall
        Wall wall = (Wall)closestItem;
        float [][] wallPoints = wall.getPoints();
        float [] leftSideIntersection = computeIntersection(pointOnFloor [0], pointOnFloor [1], this.home.getCamera().getX(), this.home.getCamera().getY(),
            wallPoints [0][0], wallPoints [0][1], wallPoints [1][0], wallPoints [1][1]);
        float [] rightSideIntersection = computeIntersection(pointOnFloor [0], pointOnFloor [1], this.home.getCamera().getX(), this.home.getCamera().getY(),
            wallPoints [3][0], wallPoints [3][1], wallPoints [2][0], wallPoints [2][1]);
        if (Point2D.distanceSq(this.home.getCamera().getX(), this.home.getCamera().getY(), leftSideIntersection [0], leftSideIntersection [1])
             < Point2D.distanceSq(this.home.getCamera().getX(), this.home.getCamera().getY(), rightSideIntersection [0], rightSideIntersection [1])) {
          floorLocation = leftSideIntersection;
        } else {
          floorLocation = rightSideIntersection;
        }
        if (transferedItems.get(0) instanceof PieceOfFurniture) {
          PieceOfFurniture transferedPiece = (PieceOfFurniture)transferedItems.get(0);
          double wallYawAngle = Math.atan((wall.getYEnd() - wall.getYStart()) / (wall.getXEnd() - wall.getXStart()));
          floorLocation [0] -= transferedPiece.getWidth() / 2 * Math.cos(wallYawAngle);
          floorLocation [1] -= transferedPiece.getWidth() / 2 * Math.sin(wallYawAngle);
        }
      } else if (!this.home.isEmpty()) {
        floorLocation = view3D.getVirtualWorldPointAt(dropLocation.x, dropLocation.y, floorElevation);
        floorLocation = new float [] {floorLocation [0], floorLocation [1]};
        if (transferedItems.size() == 1
            && transferedItems.get(0) instanceof PieceOfFurniture) {
          PieceOfFurniture transferedPiece = (PieceOfFurniture)transferedItems.get(0);
          floorLocation [0] -= transferedPiece.getWidth() / 2;
          floorLocation [1] -= transferedPiece.getDepth() / 2;
        }
      }
    }
    return floorLocation;
  }

  /**
   * Returns the intersection point between the line joining the first two points and
   * the line joining the two last points.
   */
  private float [] computeIntersection(float xPoint1, float yPoint1, float xPoint2, float yPoint2,
                                      float xPoint3, float yPoint3, float xPoint4, float yPoint4) {
    float x = xPoint2;
    float y = yPoint2;
    float alpha1 = (yPoint2 - yPoint1) / (xPoint2 - xPoint1);
    float alpha2 = (yPoint4 - yPoint3) / (xPoint4 - xPoint3);
    // If the two lines are not parallel
    if (alpha1 != alpha2) {
      // If first line is vertical
      if (Math.abs(alpha1) > 4000)  {
        if (Math.abs(alpha2) < 4000) {
          x = xPoint1;
          float beta2  = yPoint4 - alpha2 * xPoint4;
          y = alpha2 * x + beta2;
        }
      // If second line is vertical
      } else if (Math.abs(alpha2) > 4000) {
        if (Math.abs(alpha1) < 4000) {
          x = xPoint3;
          float beta1  = yPoint2 - alpha1 * xPoint2;
          y = alpha1 * x + beta1;
        }
      } else {
        boolean sameSignum = Math.signum(alpha1) == Math.signum(alpha2);
        if (Math.abs(alpha1 - alpha2) > 1E-5
            && (!sameSignum || (Math.abs(alpha1) > Math.abs(alpha2)   ? alpha1 / alpha2   : alpha2 / alpha1) > 1.004)) {
          float beta1  = yPoint2 - alpha1 * xPoint2;
          float beta2  = yPoint4 - alpha2 * xPoint4;
          x = (beta2 - beta1) / (alpha1 - alpha2);
          y = alpha1 * x + beta1;
        }
      }
    }
    return new float [] {x, y};
  }
}
