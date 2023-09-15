/*
 * DimensionLineController.java 04 mai 2023
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

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.Arrays;
import java.util.List;

import javax.swing.undo.CannotRedoException;
import javax.swing.undo.CannotUndoException;
import javax.swing.undo.UndoableEdit;
import javax.swing.undo.UndoableEditSupport;

import com.eteks.sweethome3d.model.DimensionLine;
import com.eteks.sweethome3d.model.Home;
import com.eteks.sweethome3d.model.Selectable;
import com.eteks.sweethome3d.model.TextStyle;
import com.eteks.sweethome3d.model.UserPreferences;

/**
 * A MVC controller for dimension line view.
 * @author Emmanuel Puybaret
 * @since 7.2
 */
public class DimensionLineController implements Controller {
  /**
   * The properties that may be edited by the view associated to this controller.
   */
  public enum Property {X_START, Y_START, ELEVATION_START, X_END, Y_END, ELEVATION_END, DISTANCE_TO_END_POINT, ORIENTATION,
      EDITABLE_DISTANCE, OFFSET, LENGTH_FONT_SIZE, COLOR, VISIBLE_IN_3D, PITCH}
  /**
   * The possible values for {@linkplain #getOrientation() dimension line type}.
   */
  public enum DimensionLineOrientation {PLAN, ELEVATION, DIAGONAL}

  private final Home                  home;
  private final UserPreferences       preferences;
  private final ViewFactory           viewFactory;
  private final UndoableEditSupport   undoSupport;
  private final PropertyChangeSupport propertyChangeSupport;
  private DialogView                  dimensionLineView;
  private final boolean               dimensionLineModification;

  private boolean      editableDistance;
  private Float        xStart;
  private Float        yStart;
  private Float        elevationStart;
  private Float        xEnd;
  private Float        yEnd;
  private Float        elevationEnd;
  private Float        distanceToEndPoint;
  private DimensionLineOrientation orientation;
  private Float        offset;
  private Float        lengthFontSize;
  private Integer      color;
  private Boolean      visibleIn3D;
  private Float        pitch;

  /**
   * Creates the controller of dimension line view with undo support.
   */
  public DimensionLineController(Home home,
                                 UserPreferences preferences,
                                 ViewFactory viewFactory,
                                 UndoableEditSupport undoSupport) {
    this.home = home;
    this.preferences = preferences;
    this.viewFactory = viewFactory;
    this.undoSupport = undoSupport;
    this.propertyChangeSupport = new PropertyChangeSupport(this);
    this.dimensionLineModification = true;

    updateProperties();
  }

  /**
   * Creates the controller of dimension line view with undo support.
   */
  public DimensionLineController(Home home, float x, float y,
                                 UserPreferences preferences,
                                 ViewFactory viewFactory,
                                 UndoableEditSupport undoSupport) {
    this.home = home;
    this.preferences = preferences;
    this.viewFactory = viewFactory;
    this.undoSupport = undoSupport;
    this.propertyChangeSupport = new PropertyChangeSupport(this);
    this.dimensionLineModification = false;

    this.xStart =
    this.xEnd = x;
    this.yStart =
    this.yEnd = y;
    this.editableDistance = true;
    this.elevationStart = 0f;
    this.elevationEnd =
    this.distanceToEndPoint = home.getWallHeight();
    this.offset = 20f;
    this.orientation = DimensionLineOrientation.ELEVATION;
    this.lengthFontSize = preferences.getDefaultTextStyle(DimensionLine.class).getFontSize();
    this.visibleIn3D = true;
    this.pitch = (float)(-Math.PI / 2);
  }

  /**
   * Returns the view associated with this controller.
   */
  public DialogView getView() {
    // Create view lazily only once it's needed
    if (this.dimensionLineView == null) {
      this.dimensionLineView = this.viewFactory.createDimensionLineView(this.dimensionLineModification, this.preferences, this);
    }
    return this.dimensionLineView;
  }

  /**
   * Displays the view controlled by this controller.
   */
  public void displayView(View parentView) {
    getView().displayView(parentView);
  }

  /**
   * Adds the property change <code>listener</code> in parameter to this controller.
   */
  public void addPropertyChangeListener(Property property, PropertyChangeListener listener) {
    this.propertyChangeSupport.addPropertyChangeListener(property.name(), listener);
  }

  /**
   * Removes the property change <code>listener</code> in parameter from this controller.
   */
  public void removePropertyChangeListener(Property property, PropertyChangeListener listener) {
    this.propertyChangeSupport.removePropertyChangeListener(property.name(), listener);
  }

  /**
   * Updates edited properties from selected dimension lines in the home edited by this controller.
   */
  protected void updateProperties() {
    List<DimensionLine> selectedDimensionLines = Home.getDimensionLinesSubList(this.home.getSelectedItems());
    if (selectedDimensionLines.isEmpty()) {
      setXStart(null); // Nothing to edit
      setYStart(null);
      setElevationStart(null);
      setXEnd(null);
      setYEnd(null);
      setElevationEnd(null);
      setEditableDistance(false);
      setOrientation(null);
      setOffset(null);
      setLengthFontSize(null);
      setColor(null);
      setVisibleIn3D(null);
      setPitch(null);
    } else {
      // Search the common xStart value among dimension lines
      DimensionLine firstDimensionLine = selectedDimensionLines.get(0);
      Float xStart = firstDimensionLine.getXStart();
      for (int i = 1; i < selectedDimensionLines.size(); i++) {
        if (!xStart.equals(selectedDimensionLines.get(i).getXStart())) {
          xStart = null;
          break;
        }
      }
      setXStart(xStart);

      // Search the common yStart value among dimension lines
      Float yStart = firstDimensionLine.getYStart();
      for (int i = 1; i < selectedDimensionLines.size(); i++) {
        if (!yStart.equals(selectedDimensionLines.get(i).getYStart())) {
          yStart = null;
          break;
        }
      }
      setYStart(yStart);

      // Search the common elevationStart value among dimension lines
      Float elevationStart = firstDimensionLine.getElevationStart();
      for (int i = 1; i < selectedDimensionLines.size(); i++) {
        if (!elevationStart.equals(selectedDimensionLines.get(i).getElevationStart())) {
          elevationStart = null;
          break;
        }
      }
      setElevationStart(elevationStart);

      // Search the common xEnd value among dimension lines
      Float xEnd = firstDimensionLine.getXEnd();
      for (int i = 1; i < selectedDimensionLines.size(); i++) {
        if (!xEnd.equals(selectedDimensionLines.get(i).getXEnd())) {
          xEnd = null;
          break;
        }
      }
      setXEnd(xEnd);

      // Search the common yEnd value among dimension lines
      Float yEnd = firstDimensionLine.getYEnd();
      for (int i = 1; i < selectedDimensionLines.size(); i++) {
        if (!yEnd.equals(selectedDimensionLines.get(i).getYEnd())) {
          yEnd = null;
          break;
        }
      }
      setYEnd(yEnd);

      // Search the common elevationEnd value among dimension lines
      Float elevationEnd = firstDimensionLine.getElevationEnd();
      boolean elevationDimensionLine = firstDimensionLine.isElevationDimensionLine();
      for (int i = 1; i < selectedDimensionLines.size(); i++) {
        if (!elevationEnd.equals(selectedDimensionLines.get(i).getElevationEnd())
            || elevationDimensionLine != selectedDimensionLines.get(i).isElevationDimensionLine()) {
          elevationEnd = null;
          break;
        }
      }
      setElevationEnd(elevationEnd);

      setEditableDistance(getXStart() != null
          && getYStart() != null
          && getElevationStart() != null
          && getXEnd() != null
          && getYEnd() != null
          && getElevationEnd() != null);

      // Search the common orientation among dimension lines
      DimensionLineOrientation orientation = null;
      if (firstDimensionLine.isElevationDimensionLine()) {
        orientation = DimensionLineOrientation.ELEVATION;
      } else if (firstDimensionLine.getElevationStart() == firstDimensionLine.getElevationEnd()) {
        orientation = DimensionLineOrientation.PLAN;
      }
      for (int i = 1; i < selectedDimensionLines.size(); i++) {
        DimensionLine dimensionLine = selectedDimensionLines.get(i);
        if (dimensionLine.isElevationDimensionLine() && orientation != DimensionLineOrientation.ELEVATION
            || dimensionLine.getElevationStart() == dimensionLine.getElevationEnd()
                && orientation != DimensionLineOrientation.PLAN) {
          orientation = null;
          break;
        }
      }
      setOrientation(orientation, false);

      // Search the common offset value among dimension lines
      Float offset = firstDimensionLine.getOffset();
      for (int i = 1; i < selectedDimensionLines.size(); i++) {
        if (!offset.equals(selectedDimensionLines.get(i).getOffset())) {
          offset = null;
          break;
        }
      }
      setOffset(offset);

      // Search the common dont size value among dimension lines
      float defaultFontSize = this.preferences.getDefaultTextStyle(DimensionLine.class).getFontSize();
      Float fontSize = firstDimensionLine.getLengthStyle() != null
          ? firstDimensionLine.getLengthStyle().getFontSize()
          : defaultFontSize;
      for (int i = 1; i < selectedDimensionLines.size(); i++) {
        DimensionLine dimensionLine = selectedDimensionLines.get(i);
        if (!fontSize.equals(dimensionLine.getLengthStyle() != null
                ? dimensionLine.getLengthStyle().getFontSize()
                : defaultFontSize)) {
          fontSize = null;
          break;
        }
      }
      setLengthFontSize(fontSize);

      // Search the common color among dimension lines
      Integer color = firstDimensionLine.getColor();
      if (color != null) {
        for (int i = 1; i < selectedDimensionLines.size(); i++) {
          if (!color.equals(selectedDimensionLines.get(i).getColor())) {
            color = null;
            break;
          }
        }
      }
      setColor(color);

      Boolean visibleIn3D = firstDimensionLine.isVisibleIn3D();
      for (int i = 1; i < selectedDimensionLines.size(); i++) {
        if (!visibleIn3D.equals(selectedDimensionLines.get(i).isVisibleIn3D())) {
          visibleIn3D = null;
          break;
        }
      }
      setVisibleIn3D(visibleIn3D);

      // Search the common pitch value among dimension lines
      Float pitch = firstDimensionLine.getPitch();
      for (int i = 1; i < selectedDimensionLines.size(); i++) {
        if (!pitch.equals(selectedDimensionLines.get(i).getPitch())) {
          pitch = null;
          break;
        }
      }
      setPitch(pitch);
    }
  }

  /**
   * Sets the edited abscissa of the start point.
   */
  public void setXStart(Float xStart) {
    setXStart(xStart, true);
  }

  private void setXStart(Float xStart, boolean updateXEnd) {
    if (xStart != this.xStart) {
      Float oldXStart = this.xStart;
      this.xStart = xStart;
      this.propertyChangeSupport.firePropertyChange(Property.X_START.name(), oldXStart, xStart);
      if (updateXEnd
          && this.orientation == DimensionLineOrientation.ELEVATION) {
        setXEnd(xStart, false);
      } else {
        updateDistanceToEndPoint();
      }
    }
  }

  /**
   * Returns the edited abscissa of the start point.
   */
  public Float getXStart() {
    return this.xStart;
  }

  /**
   * Sets the edited ordinate of the start point.
   */
  public void setYStart(Float yStart) {
    setYStart(yStart, true);
  }

  private void setYStart(Float yStart, boolean updateYEnd) {
    if (yStart != this.yStart) {
      Float oldYStart = this.yStart;
      this.yStart = yStart;
      this.propertyChangeSupport.firePropertyChange(Property.Y_START.name(), oldYStart, yStart);
      if (updateYEnd
          && this.orientation == DimensionLineOrientation.ELEVATION) {
        setYEnd(yStart, false);
      } else {
        updateDistanceToEndPoint();
      }
    }
  }

  /**
   * Returns the edited ordinate of the start point.
   */
  public Float getYStart() {
    return this.yStart;
  }

  /**
   * Sets the edited elevation of the start point.
   */
  public void setElevationStart(Float elevationStart) {
    setElevationStart(elevationStart, true);
  }

  private void setElevationStart(Float elevationStart, boolean updateElevationEnd) {
    if (elevationStart != this.elevationStart) {
      Float oldElevationStart = this.elevationStart;
      this.elevationStart = elevationStart;
      this.propertyChangeSupport.firePropertyChange(Property.ELEVATION_START.name(), oldElevationStart, elevationStart);
      if (updateElevationEnd
          && this.orientation == DimensionLineOrientation.PLAN) {
        setElevationEnd(elevationStart, false);
      } else {
        updateDistanceToEndPoint();
      }
    }
  }

  /**
   * Returns the edited elevation of the start point.
   */
  public Float getElevationStart() {
    return this.elevationStart;
  }

  /**
   * Sets the edited abscissa of the end point.
   */
  public void setXEnd(Float xEnd) {
    setXEnd(xEnd, true);
  }

  private void setXEnd(Float xEnd, boolean updateXStart) {
    if (xEnd != this.xEnd) {
      Float oldXEnd = this.xEnd;
      this.xEnd = xEnd;
      this.propertyChangeSupport.firePropertyChange(Property.X_END.name(), oldXEnd, xEnd);
      if (updateXStart
          && this.orientation == DimensionLineOrientation.ELEVATION) {
        setXStart(xEnd, false);
      } else {
        updateDistanceToEndPoint();
      }
    }
  }

  /**
   * Returns the edited abscissa of the end point.
   */
  public Float getXEnd() {
    return this.xEnd;
  }

  /**
   * Sets the edited ordinate of the end point.
   */
  public void setYEnd(Float yEnd) {
    setYEnd(yEnd, true);
  }

  private void setYEnd(Float yEnd, boolean updateYStart) {
    if (yEnd != this.yEnd) {
      Float oldYEnd = this.yEnd;
      this.yEnd = yEnd;
      this.propertyChangeSupport.firePropertyChange(Property.Y_END.name(), oldYEnd, yEnd);
      if (updateYStart
          && this.orientation == DimensionLineOrientation.ELEVATION) {
        setYStart(yEnd, false);
      } else {
        updateDistanceToEndPoint();
      }
    }
  }

  /**
   * Returns the edited ordinate of the end point.
   */
  public Float getYEnd() {
    return this.yEnd;
  }

  /**
   * Sets the edited elevation of the end point.
   */
  public void setElevationEnd(Float elevationEnd) {
    setElevationEnd(elevationEnd, true);
  }

  private void setElevationEnd(Float elevationEnd, boolean updateElevationStart) {
    if (elevationEnd != this.elevationEnd) {
      Float oldElevationEnd = this.elevationEnd;
      this.elevationEnd = elevationEnd;
      this.propertyChangeSupport.firePropertyChange(Property.ELEVATION_END.name(), oldElevationEnd, elevationEnd);
      if (updateElevationStart
          && this.orientation == DimensionLineOrientation.PLAN) {
        setElevationStart(elevationEnd, false);
      } else {
        updateDistanceToEndPoint();
      }
    }
  }

  /**
   * Returns the edited elevation of the end point.
   */
  public Float getElevationEnd() {
    return this.elevationEnd;
  }

  /**
   * Updates the edited distance to end point after its coordinates change.
   */
  private void updateDistanceToEndPoint() {
    Float xStart = getXStart();
    Float yStart = getYStart();
    Float elevationStart = getElevationStart();
    Float xEnd = getXEnd();
    Float yEnd = getYEnd();
    Float elevationEnd = getElevationEnd();
    if (xStart != null && yStart != null && elevationStart != null
        && xEnd != null && yEnd != null && elevationEnd != null) {
      DimensionLine dimensionLine = new DimensionLine(xStart, yStart, elevationStart, xEnd, yEnd, elevationEnd, 0);
      setDistanceToEndPoint(dimensionLine.getLength(), false);
    } else {
      setDistanceToEndPoint(null, false);
    }
  }

  /**
   * Sets the edited distance to end point.
   */
  public void setDistanceToEndPoint(Float distanceToEndPoint) {
    setDistanceToEndPoint(distanceToEndPoint, true);
  }

  /**
   * Sets the edited distance to end point and updates the coordinates of the end point if
   * <code>updateEndPoint</code> is <code>true</code>.
   */
  private void setDistanceToEndPoint(Float distanceToEndPoint, boolean updateEndPoint) {
    if (distanceToEndPoint != this.distanceToEndPoint) {
      Float oldDistance = this.distanceToEndPoint;
      this.distanceToEndPoint = distanceToEndPoint;
      this.propertyChangeSupport.firePropertyChange(Property.DISTANCE_TO_END_POINT.name(), oldDistance, distanceToEndPoint);

      if (updateEndPoint) {
        Float xStart = getXStart();
        Float yStart = getYStart();
        Float elevationStart = getElevationStart();
        Float xEnd = getXEnd();
        Float yEnd = getYEnd();
        Float elevationEnd = getElevationEnd();
        if (xStart != null && yStart != null && elevationStart != null
            && xEnd != null && yEnd != null && elevationEnd != null && distanceToEndPoint != null) {
          double dimensionLinePlanAngle = Math.atan2(yStart - yEnd, xEnd - xStart);
          double dimensionLineVerticalAngle = Math.atan2(elevationEnd - elevationStart, xEnd - xStart);
          setXEnd((float)(xStart + distanceToEndPoint * Math.cos(dimensionLinePlanAngle) * Math.cos(dimensionLineVerticalAngle)));
          setYEnd((float)(yStart - distanceToEndPoint * Math.sin(dimensionLinePlanAngle) * Math.cos(dimensionLineVerticalAngle)));
          setElevationEnd((float)(elevationStart + distanceToEndPoint * Math.sin(dimensionLineVerticalAngle)));
        } else {
          setXEnd(null);
          setYEnd(null);
          setElevationEnd(null);
        }
      }
    }
  }

  /**
   * Returns the edited distance to end point.
   */
  public Float getDistanceToEndPoint() {
    return this.distanceToEndPoint;
  }

  /**
   * Sets the edited orientation.
   */
  public void setOrientation(DimensionLineOrientation orientation) {
    setOrientation(orientation, true);
  }

  /**
   * Sets the edited orientation.
   */
  private void setOrientation(DimensionLineOrientation orientation, boolean updateEndPointAndPitch) {
    if (orientation != this.orientation) {
      DimensionLineOrientation oldOrientation = this.orientation;
      this.orientation = orientation;
      this.propertyChangeSupport.firePropertyChange(Property.ORIENTATION.name(), oldOrientation, orientation);

      if (updateEndPointAndPitch) {
        if (orientation == DimensionLineOrientation.PLAN
            && this.pitch != 0
            && this.pitch != (float)(Math.PI / 2)) {
          setPitch(0f);
        }
        if (this.distanceToEndPoint != null) {
          float distanceToEndPoint = this.distanceToEndPoint;
          Float xStart = getXStart();
          Float yStart = getYStart();
          Float elevationStart = getElevationStart();
          if (orientation == DimensionLineOrientation.PLAN) {
              setElevationEnd(elevationStart, false);
              setYEnd(yStart, false);
              setXEnd(xStart + distanceToEndPoint, false);
          } else if (orientation == DimensionLineOrientation.ELEVATION) {
              setXEnd(xStart, false);
              setYEnd(yStart, false);
              setElevationEnd(elevationStart + distanceToEndPoint, false);
          }
        }
      }
    }
  }

  /**
   * Returns the edited orientation.
   */
  public DimensionLineOrientation getOrientation() {
    return this.orientation;
  }

  /**
   * Sets whether the distance can be be edited or not.
   */
  public void setEditableDistance(boolean editableDistance) {
    if (editableDistance != this.editableDistance) {
      this.editableDistance = editableDistance;
      this.propertyChangeSupport.firePropertyChange(Property.EDITABLE_DISTANCE.name(), !editableDistance, editableDistance);
    }
  }

  /**
   * Returns whether the distance can be be edited or not.
   */
  public boolean isEditableDistance() {
    return this.editableDistance;
  }

  /**
   * Sets the edited offset.
   */
  public void setOffset(Float offset) {
    if (offset != this.offset) {
      Float oldOffset = this.offset;
      this.offset = offset;
      this.propertyChangeSupport.firePropertyChange(Property.OFFSET.name(), oldOffset, offset);
    }
  }

  /**
   * Returns the edited offset.
   */
  public Float getOffset() {
    return this.offset;
  }

  /**
   * Sets the edited font size.
   */
  public void setLengthFontSize(Float lengthFontSize) {
    if (lengthFontSize != this.lengthFontSize) {
      Float oldLengthFontSize = this.lengthFontSize;
      this.lengthFontSize = lengthFontSize;
      this.propertyChangeSupport.firePropertyChange(Property.LENGTH_FONT_SIZE.name(), oldLengthFontSize, lengthFontSize);
    }
  }

  /**
   * Returns the edited font size.
   */
  public Float getLengthFontSize() {
    return this.lengthFontSize;
  }

  /**
   * Sets the edited color.
   */
  public void setColor(Integer color) {
    if (color != this.color) {
      Integer oldColor = this.color;
      this.color = color;
      this.propertyChangeSupport.firePropertyChange(Property.COLOR.name(), oldColor, color);
    }
  }

  /**
   * Returns the edited color.
   */
  public Integer getColor() {
    return this.color;
  }

  /**
   * Sets whether all edited dimension lines are viewed in 3D.
   */
  public void setVisibleIn3D(Boolean visibleIn3D) {
    if (visibleIn3D != this.visibleIn3D) {
      Boolean oldVisibleIn3D = this.visibleIn3D;
      this.visibleIn3D = visibleIn3D;
      this.propertyChangeSupport.firePropertyChange(Property.VISIBLE_IN_3D.name(), oldVisibleIn3D, visibleIn3D);
    }
  }

  /**
   * Returns <code>Boolean.TRUE</code> if all edited dimension lines are viewed in 3D,
   * or <code>Boolean.FALSE</code> if no dimension line is viewed in 3D.
   */
  public Boolean isVisibleIn3D() {
    return this.visibleIn3D;
  }

  /**
   * Sets the edited pitch angle.
   */
  public void setPitch(Float pitch) {
    if (pitch != this.pitch) {
      Float oldPitch = this.pitch;
      this.pitch = pitch;
      this.propertyChangeSupport.firePropertyChange(Property.PITCH.name(), oldPitch, pitch);
    }
  }

  /**
   * Returns the edited pitch.
   */
  public Float getPitch() {
    return this.pitch;
  }

  /**
   * Returns a new dimension line instance added to home.
   */
  protected DimensionLine createDimensionLine(float xStart, float yStart, float elevationStart,
                                              float xEnd, float yEnd, float elevationEnd, float offset) {
    DimensionLine dimensionLine = new DimensionLine(getXStart(), getYStart(), getElevationStart(),
        getXEnd(), getYEnd(), getElevationEnd(), getOffset());
    this.home.addDimensionLine(dimensionLine);
    return dimensionLine;
  }

  /**
   * Controls the creation of a dimension line.
   */
  public void createDimensionLine() {
    // Apply modification
    List<Selectable> oldSelection = this.home.getSelectedItems();
    boolean basePlanLocked = this.home.isBasePlanLocked();
    boolean allLevelsSelection = this.home.isAllLevelsSelection();
    DimensionLine dimensionLine = createDimensionLine(getXStart(), getYStart(), getElevationStart(),
        getXEnd(), getYEnd(), getElevationEnd(), getOffset());
    Float fontSize = getLengthFontSize();
    if (fontSize != null) {
      TextStyle style = this.preferences.getDefaultTextStyle(DimensionLine.class).deriveStyle(fontSize);
      dimensionLine.setLengthStyle(style);
    }
    dimensionLine.setColor(getColor());
    dimensionLine.setVisibleIn3D(isVisibleIn3D());
    dimensionLine.setPitch(getPitch());
    doAddAndSelectDimensionLine(this.home, dimensionLine, false);
    if (this.undoSupport != null) {
      UndoableEdit undoableEdit = new DimensionLineCreationUndoableEdit(
          this.home, this.preferences, oldSelection.toArray(new Selectable [oldSelection.size()]),
          basePlanLocked, allLevelsSelection, dimensionLine);
      this.undoSupport.postEdit(undoableEdit);
    }
  }

  /**
   * Undoable edit for dimension line creation. This class isn't anonymous to avoid
   * being bound to controller and its view.
   */
  private static class DimensionLineCreationUndoableEdit extends LocalizedUndoableEdit {
    private final Home          home;
    private final Selectable [] oldSelection;
    private final boolean       oldBasePlanLocked;
    private final boolean       oldAllLevelsSelection;
    private final DimensionLine dimensionLine;

    private DimensionLineCreationUndoableEdit(Home home,
                                              UserPreferences preferences,
                                              Selectable [] oldSelection,
                                              boolean oldBasePlanLocked,
                                              boolean oldAllLevelsSelection,
                                              DimensionLine dimensionLine) {
      super(preferences, DimensionLineController.class, "undoCreateDimensionLineName");
      this.home = home;
      this.oldSelection = oldSelection;
      this.oldBasePlanLocked = oldBasePlanLocked;
      this.oldAllLevelsSelection = oldAllLevelsSelection;
      this.dimensionLine = dimensionLine;
    }

    @Override
    public void undo() throws CannotUndoException {
      super.undo();
      doDeleteDimensionLine(this.home, this.dimensionLine, this.oldBasePlanLocked);
      this.home.setSelectedItems(Arrays.asList(this.oldSelection));
      this.home.setAllLevelsSelection(this.oldAllLevelsSelection);
    }

    @Override
    public void redo() throws CannotRedoException {
      super.redo();
      doAddAndSelectDimensionLine(this.home, this.dimensionLine, true);
    }
  }

  /**
   * Adds dimension line to home and selects it.
   */
  private static void doAddAndSelectDimensionLine(Home home,
                                                  DimensionLine dimensionLine,
                                                  boolean addToHome) {
    if (addToHome) {
      home.addDimensionLine(dimensionLine);
    }
    home.setBasePlanLocked(false);
    home.setSelectedItems(Arrays.asList(new Selectable [] {dimensionLine}));
    home.setAllLevelsSelection(false);
  }

  /**
   * Deletes dimensionLine from home.
   */
  private static void doDeleteDimensionLine(Home home, DimensionLine dimensionLine, boolean basePlanLocked) {
    home.deleteDimensionLine(dimensionLine);
    home.setBasePlanLocked(basePlanLocked);
  }

  /**
   * Controls the modification of selected dimension lines in edited home.
   */
  public void modifyDimensionLines() {
    List<Selectable> oldSelection = this.home.getSelectedItems();
    List<DimensionLine> selectedDimensionLines = Home.getDimensionLinesSubList(oldSelection);
    if (!selectedDimensionLines.isEmpty()) {
      Float xStart = getXStart();
      Float yStart = getYStart();
      Float elevationStart = getElevationStart();
      Float xEnd = getXEnd();
      Float yEnd = getYEnd();
      Float elevationEnd = getElevationEnd();
      Float offset = getOffset();
      Float lengthFontSize = getLengthFontSize();
      Integer color = getColor();
      Boolean visibleIn3D = isVisibleIn3D();
      Float pitch = getPitch();

      // Create an array of modified dimension lines with their current properties values
      ModifiedDimensionLine [] modifiedDimensionLines = new ModifiedDimensionLine [selectedDimensionLines.size()];
      for (int i = 0; i < modifiedDimensionLines.length; i++) {
        modifiedDimensionLines [i] = new ModifiedDimensionLine(selectedDimensionLines.get(i));
      }
      // Apply modification
      TextStyle defaultStyle = this.preferences.getDefaultTextStyle(DimensionLine.class);
      doModifyDimensionLines(modifiedDimensionLines,
          xStart, yStart, elevationStart, xEnd, yEnd, elevationEnd, offset,
          lengthFontSize, defaultStyle, color, visibleIn3D, pitch);
      if (this.undoSupport != null) {
        UndoableEdit undoableEdit = new DimensionLinesModificationUndoableEdit(this.home,
            this.preferences, oldSelection.toArray(new Selectable [oldSelection.size()]) , modifiedDimensionLines,
            xStart, yStart, elevationStart, xEnd, yEnd, elevationEnd, offset,
            lengthFontSize, defaultStyle, color, visibleIn3D, pitch);
        this.undoSupport.postEdit(undoableEdit);
      }
    }
  }

  /**
   * Undoable edit for dimension lines modification. This class isn't anonymous to avoid
   * being bound to controller and its view.
   */
  private static class DimensionLinesModificationUndoableEdit extends LocalizedUndoableEdit {
    private final Home             home;
    private final Selectable []    oldSelection;
    private final ModifiedDimensionLine [] modifiedDimensionLines;
    private final Float            xStart;
    private final Float            yStart;
    private final Float            elevationStart;
    private final Float            xEnd;
    private final Float            yEnd;
    private final Float            elevationEnd;
    private final Float            offset;
    private final Float            lengthFontSize;
    private final TextStyle        defaultStyle;
    private final Integer          color;
    private final Boolean          visibleIn3D;
    private final Float            pitch;

    private DimensionLinesModificationUndoableEdit(Home home,
                                          UserPreferences preferences,
                                          Selectable [] oldSelection, ModifiedDimensionLine [] modifiedDimensionLines,
                                          Float xStart, Float yStart, Float elevationStart,
                                          Float xEnd, Float yEnd, Float elevationEnd, Float offset,
                                          Float lengthFontSize, TextStyle defaultStyle,
                                          Integer color, Boolean visibleIn3D, Float pitch) {
      super(preferences, DimensionLineController.class, "undoModifyDimensionLinesName");
      this.home = home;
      this.oldSelection = oldSelection;
      this.modifiedDimensionLines = modifiedDimensionLines;
      this.xStart = xStart;
      this.yStart = yStart;
      this.elevationStart = elevationStart;
      this.xEnd = xEnd;
      this.yEnd = yEnd;
      this.elevationEnd = elevationEnd;
      this.offset = offset;
      this.lengthFontSize = lengthFontSize;
      this.defaultStyle = defaultStyle;
      this.color = color;
      this.visibleIn3D = visibleIn3D;
      this.pitch = pitch;
    }

    @Override
    public void undo() throws CannotUndoException {
      super.undo();
      undoModifyDimensionLines(this.modifiedDimensionLines);
      this.home.setSelectedItems(Arrays.asList(this.oldSelection));
    }

    @Override
    public void redo() throws CannotRedoException {
      super.redo();
      doModifyDimensionLines(this.modifiedDimensionLines, this.xStart, this.yStart, this.elevationStart,
          this.xEnd, this.yEnd, this.elevationEnd, this.offset,
          this.lengthFontSize, this.defaultStyle, this.color, this.visibleIn3D, this.pitch);
      this.home.setSelectedItems(Arrays.asList(this.oldSelection));
    }
  }

  /**
   * Modifies dimension lines properties with the values in parameter.
   */
  private static void doModifyDimensionLines(ModifiedDimensionLine [] modifiedDimensionLines,
                                             Float xStart, Float yStart, Float elevationStart,
                                             Float xEnd, Float yEnd, Float elevationEnd, Float offset,
                                             Float lengthFontSize, TextStyle defaultStyle,
                                             Integer color, Boolean visibleIn3D, Float pitch) {
    for (ModifiedDimensionLine modifiedDimensionLine : modifiedDimensionLines) {
      DimensionLine dimensionLine = modifiedDimensionLine.getDimensionLine();
      if (xStart != null) {
        dimensionLine.setXStart(xStart);
      }
      if (yStart != null) {
        dimensionLine.setYStart(yStart);
      }
      if (elevationStart != null) {
        if (elevationEnd == null) {
          if (dimensionLine.isElevationDimensionLine()) {
            dimensionLine.setElevationEnd(elevationStart + dimensionLine.getElevationEnd() - dimensionLine.getElevationStart());
          } else {
            dimensionLine.setElevationEnd(elevationStart);
          }
        }
        dimensionLine.setElevationStart(elevationStart);
      }
      if (xEnd != null) {
        dimensionLine.setXEnd(xEnd);
      }
      if (yEnd != null) {
        dimensionLine.setYEnd(yEnd);
      }
      if (elevationEnd != null) {
        dimensionLine.setElevationEnd(elevationEnd);
      }
      if (offset != null) {
        dimensionLine.setOffset(offset);
      }
      if (lengthFontSize != null) {
        dimensionLine.setLengthStyle(dimensionLine.getLengthStyle() != null
            ? dimensionLine.getLengthStyle().deriveStyle(lengthFontSize)
            : defaultStyle.deriveStyle(lengthFontSize));
      }
      if (color != null) {
        dimensionLine.setColor(color);
      }
      if (visibleIn3D != null) {
        dimensionLine.setVisibleIn3D(visibleIn3D);
      }
      if (pitch != null) {
        dimensionLine.setPitch(pitch);
      }
    }
  }

  /**
   * Restores dimension line properties from the values stored in <code>modifiedDimensionLines</code>.
   */
  private static void undoModifyDimensionLines(ModifiedDimensionLine [] modifiedDimensionLines) {
    for (ModifiedDimensionLine modifiedDimensionLine : modifiedDimensionLines) {
      modifiedDimensionLine.reset();
    }
  }

  /**
   * Stores the current properties values of a modified dimension line.
   */
  private static final class ModifiedDimensionLine {
    private final DimensionLine dimensionLine;
    private final float         xStart;
    private final float         yStart;
    private final float         elevationStart;
    private final float         xEnd;
    private final float         yEnd;
    private final float         elevationEnd;
    private final float         offset;
    private final TextStyle     lengthStyle;
    private final Integer       color;
    private final boolean       visibleIn3D;
    private final float         pitch;

    public ModifiedDimensionLine(DimensionLine dimensionLine) {
      this.dimensionLine = dimensionLine;
      this.xStart = dimensionLine.getXStart();
      this.yStart = dimensionLine.getYStart();
      this.elevationStart = dimensionLine.getElevationStart();
      this.xEnd = dimensionLine.getXEnd();
      this.yEnd = dimensionLine.getYEnd();
      this.elevationEnd = dimensionLine.getElevationEnd();
      this.offset = dimensionLine.getOffset();
      this.lengthStyle = dimensionLine.getLengthStyle();
      this.color = dimensionLine.getColor();
      this.visibleIn3D = dimensionLine.isVisibleIn3D();
      this.pitch = dimensionLine.getPitch();
    }

    public DimensionLine getDimensionLine() {
      return this.dimensionLine;
    }

    public void reset() {
      this.dimensionLine.setXStart(this.xStart);
      this.dimensionLine.setYStart(this.yStart);
      this.dimensionLine.setElevationStart(this.elevationStart);
      this.dimensionLine.setXEnd(this.xEnd);
      this.dimensionLine.setYEnd(this.yEnd);
      this.dimensionLine.setElevationEnd(this.elevationEnd);
      this.dimensionLine.setOffset(this.offset);
      this.dimensionLine.setLengthStyle(this.lengthStyle);
      this.dimensionLine.setColor(this.color);
      this.dimensionLine.setVisibleIn3D(this.visibleIn3D);
      this.dimensionLine.setPitch(this.pitch);
    }
  }
}
