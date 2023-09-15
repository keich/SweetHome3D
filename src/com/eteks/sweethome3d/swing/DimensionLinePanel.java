/*
 * DimensionLinePanel.java 04 mai 2023
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

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import javax.swing.ButtonGroup;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JSeparator;
import javax.swing.JSpinner;
import javax.swing.KeyStroke;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import com.eteks.sweethome3d.model.UserPreferences;
import com.eteks.sweethome3d.tools.OperatingSystem;
import com.eteks.sweethome3d.viewcontroller.DialogView;
import com.eteks.sweethome3d.viewcontroller.DimensionLineController;
import com.eteks.sweethome3d.viewcontroller.View;

/**
 * Dimension line editing panel.
 * @author Emmanuel Puybaret
 */
public class DimensionLinePanel extends JPanel implements DialogView {
  private final boolean         dimensionLineModification;
  private final DimensionLineController controller;
  private JLabel               xStartLabel;
  private JSpinner             xStartSpinner;
  private JLabel               yStartLabel;
  private JSpinner             yStartSpinner;
  private JLabel               elevationStartLabel;
  private JSpinner             elevationStartSpinner;
  private JLabel               xEndLabel;
  private JSpinner             xEndSpinner;
  private JLabel               yEndLabel;
  private JSpinner             yEndSpinner;
  private JLabel               distanceToEndPointLabel;
  private JSpinner             distanceToEndPointSpinner;
  private JLabel               offsetLabel;
  private JSpinner             offsetSpinner;
  private JRadioButton         planDimensionLineRadioButton;
  private JRadioButton         elevationDimensionLineRadioButton;
  private JLabel               lengthFontSizeLabel;
  private JSpinner             lengthFontSizeSpinner;
  private JLabel               colorLabel;
  private ColorButton          colorButton;;
  private NullableCheckBox     visibleIn3DViewCheckBox;
  private JLabel               pitchLabel;
  private JRadioButton         pitch0DegreeRadioButton;
  private JRadioButton         pitch90DegreeRadioButton;
  private String               dialogTitle;

  /**
   * Creates a panel that displays wall data according to the units set in
   * <code>preferences</code>.
   * @param modification specifies whether this panel edits existing dimension lines or a new one
   * @param preferences user preferences
   * @param controller the controller of this panel
   */
  public DimensionLinePanel(boolean modification,
                            UserPreferences preferences,
                            DimensionLineController controller) {
    super(new GridBagLayout());
    this.dimensionLineModification = modification;
    this.controller = controller;
    createComponents(modification, preferences, controller);
    setMnemonics(preferences);
    layoutComponents(preferences, controller);
  }

  /**
   * Creates and initializes components and spinners model.
   */
  private void createComponents(boolean modification,
                                final UserPreferences preferences,
                                final DimensionLineController controller) {
    // Get unit name matching current unit
    String unitName = preferences.getLengthUnit().getName();

    // Create X start label and its spinner bound to X_START controller property
    this.xStartLabel = new JLabel(SwingTools.getLocalizedLabelText(preferences,
        DimensionLinePanel.class, "xLabel.text", unitName));
    final float maximumLength = preferences.getLengthUnit().getMaximumLength();
    final NullableSpinner.NullableSpinnerLengthModel xStartSpinnerModel =
        new NullableSpinner.NullableSpinnerLengthModel(preferences, -maximumLength, maximumLength);
    this.xStartSpinner = new NullableSpinner(xStartSpinnerModel);
    xStartSpinnerModel.setNullable(controller.getXStart() == null);
    xStartSpinnerModel.setLength(controller.getXStart());
    final PropertyChangeListener xStartChangeListener = new PropertyChangeListener() {
        public void propertyChange(PropertyChangeEvent ev) {
          xStartSpinnerModel.setNullable(ev.getNewValue() == null);
          xStartSpinnerModel.setLength((Float)ev.getNewValue());
        }
      };
    controller.addPropertyChangeListener(DimensionLineController.Property.X_START, xStartChangeListener);
    xStartSpinnerModel.addChangeListener(new ChangeListener() {
        public void stateChanged(ChangeEvent ev) {
          controller.removePropertyChangeListener(DimensionLineController.Property.X_START, xStartChangeListener);
          controller.setXStart(xStartSpinnerModel.getLength());
          controller.addPropertyChangeListener(DimensionLineController.Property.X_START, xStartChangeListener);
        }
      });

    // Create Y start label and its spinner bound to Y_START controller property
    this.yStartLabel = new JLabel(SwingTools.getLocalizedLabelText(preferences,
        DimensionLinePanel.class, "yLabel.text", unitName));
    final NullableSpinner.NullableSpinnerLengthModel yStartSpinnerModel =
        new NullableSpinner.NullableSpinnerLengthModel(preferences, -maximumLength, maximumLength);
    this.yStartSpinner = new NullableSpinner(yStartSpinnerModel);
    yStartSpinnerModel.setNullable(controller.getYStart() == null);
    yStartSpinnerModel.setLength(controller.getYStart());
    final PropertyChangeListener yStartChangeListener = new PropertyChangeListener() {
        public void propertyChange(PropertyChangeEvent ev) {
          yStartSpinnerModel.setNullable(ev.getNewValue() == null);
          yStartSpinnerModel.setLength((Float)ev.getNewValue());
        }
      };
    controller.addPropertyChangeListener(DimensionLineController.Property.Y_START, yStartChangeListener);
    yStartSpinnerModel.addChangeListener(new ChangeListener() {
        public void stateChanged(ChangeEvent ev) {
          controller.removePropertyChangeListener(DimensionLineController.Property.Y_START, yStartChangeListener);
          controller.setYStart(yStartSpinnerModel.getLength());
          controller.addPropertyChangeListener(DimensionLineController.Property.Y_START, yStartChangeListener);
        }
      });

    // Create elevation start label and its spinner bound to ELEVATION_START controller property
    this.elevationStartLabel = new JLabel(SwingTools.getLocalizedLabelText(preferences,
        DimensionLinePanel.class, "elevationLabel.text", unitName));
    final NullableSpinner.NullableSpinnerLengthModel elevationStartSpinnerModel =
        new NullableSpinner.NullableSpinnerLengthModel(preferences, 0, maximumLength);
    this.elevationStartSpinner = new NullableSpinner(elevationStartSpinnerModel);
    elevationStartSpinnerModel.setNullable(controller.getElevationStart() == null);
    elevationStartSpinnerModel.setLength(controller.getElevationStart());
    final PropertyChangeListener elevationStartChangeListener = new PropertyChangeListener() {
        public void propertyChange(PropertyChangeEvent ev) {
          elevationStartSpinnerModel.setNullable(ev.getNewValue() == null);
          elevationStartSpinnerModel.setLength((Float)ev.getNewValue());
        }
      };
    controller.addPropertyChangeListener(DimensionLineController.Property.ELEVATION_START, elevationStartChangeListener);
    elevationStartSpinnerModel.addChangeListener(new ChangeListener() {
        public void stateChanged(ChangeEvent ev) {
          controller.removePropertyChangeListener(DimensionLineController.Property.ELEVATION_START, elevationStartChangeListener);
          Float elevationStart = elevationStartSpinnerModel.getLength();
          if (elevationStart != null && controller.getElevationEnd() != null && controller.getElevationStart() != null) {
            controller.setElevationEnd(controller.getElevationEnd() + elevationStart - controller.getElevationStart());
          }
          controller.setElevationStart(elevationStart);
          controller.addPropertyChangeListener(DimensionLineController.Property.ELEVATION_START, elevationStartChangeListener);
        }
      });

    // Create X end label and its spinner bound to X_END controller property
    this.xEndLabel = new JLabel(SwingTools.getLocalizedLabelText(preferences,
        DimensionLinePanel.class, "xLabel.text", unitName));
    final NullableSpinner.NullableSpinnerLengthModel xEndSpinnerModel =
        new NullableSpinner.NullableSpinnerLengthModel(preferences, -maximumLength, maximumLength);
    this.xEndSpinner = new NullableSpinner(xEndSpinnerModel);
    xEndSpinnerModel.setNullable(controller.getXEnd() == null);
    xEndSpinnerModel.setLength(controller.getXEnd());
    final PropertyChangeListener xEndChangeListener = new PropertyChangeListener() {
        public void propertyChange(PropertyChangeEvent ev) {
          xEndSpinnerModel.setNullable(ev.getNewValue() == null);
          xEndSpinnerModel.setLength((Float)ev.getNewValue());
        }
      };
    controller.addPropertyChangeListener(DimensionLineController.Property.X_END, xEndChangeListener);
    xEndSpinnerModel.addChangeListener(new ChangeListener() {
        public void stateChanged(ChangeEvent ev) {
          controller.removePropertyChangeListener(DimensionLineController.Property.X_END, xEndChangeListener);
          controller.setXEnd(xEndSpinnerModel.getLength());
          controller.addPropertyChangeListener(DimensionLineController.Property.X_END, xEndChangeListener);
        }
      });

    // Create Y end label and its spinner bound to Y_END controller property
    this.yEndLabel = new JLabel(SwingTools.getLocalizedLabelText(preferences,
        DimensionLinePanel.class, "yLabel.text", unitName));
    final NullableSpinner.NullableSpinnerLengthModel yEndSpinnerModel =
        new NullableSpinner.NullableSpinnerLengthModel(preferences, -maximumLength, maximumLength);
    this.yEndSpinner = new NullableSpinner(yEndSpinnerModel);
    yEndSpinnerModel.setNullable(controller.getYEnd() == null);
    yEndSpinnerModel.setLength(controller.getYEnd());
    final PropertyChangeListener yEndChangeListener = new PropertyChangeListener() {
        public void propertyChange(PropertyChangeEvent ev) {
          yEndSpinnerModel.setNullable(ev.getNewValue() == null);
          yEndSpinnerModel.setLength((Float)ev.getNewValue());
        }
      };
    controller.addPropertyChangeListener(DimensionLineController.Property.Y_END, yEndChangeListener);
    yEndSpinnerModel.addChangeListener(new ChangeListener() {
        public void stateChanged(ChangeEvent ev) {
          controller.removePropertyChangeListener(DimensionLineController.Property.Y_END, yEndChangeListener);
          controller.setYEnd(yEndSpinnerModel.getLength());
          controller.addPropertyChangeListener(DimensionLineController.Property.Y_END, yEndChangeListener);
        }
      });

    // Create distance to end point label and its spinner bound to DISTANCE_TO_END_POINT controller property
    this.distanceToEndPointLabel = new JLabel(SwingTools.getLocalizedLabelText(preferences,
        DimensionLinePanel.class, "distanceToEndPointLabel.text", unitName));
    final float minimumLength = preferences.getLengthUnit().getMinimumLength();
    final NullableSpinner.NullableSpinnerLengthModel distanceToEndPointSpinnerModel =
        new NullableSpinner.NullableSpinnerLengthModel(preferences, minimumLength, 2 * maximumLength * (float)Math.sqrt(2));
    this.distanceToEndPointSpinner = new NullableSpinner(distanceToEndPointSpinnerModel);
    distanceToEndPointSpinnerModel.setNullable(controller.getDistanceToEndPoint() == null);
    distanceToEndPointSpinnerModel.setLength(controller.getDistanceToEndPoint());
    final PropertyChangeListener distanceToEndPointChangeListener = new PropertyChangeListener() {
        public void propertyChange(PropertyChangeEvent ev) {
          distanceToEndPointSpinnerModel.setNullable(ev.getNewValue() == null);
          distanceToEndPointSpinnerModel.setLength((Float)ev.getNewValue());
        }
      };
    controller.addPropertyChangeListener(DimensionLineController.Property.DISTANCE_TO_END_POINT,
        distanceToEndPointChangeListener);
    distanceToEndPointSpinnerModel.addChangeListener(new ChangeListener() {
        public void stateChanged(ChangeEvent ev) {
          controller.removePropertyChangeListener(DimensionLineController.Property.DISTANCE_TO_END_POINT,
              distanceToEndPointChangeListener);
          controller.setDistanceToEndPoint(distanceToEndPointSpinnerModel.getLength());
          controller.addPropertyChangeListener(DimensionLineController.Property.DISTANCE_TO_END_POINT,
              distanceToEndPointChangeListener);
        }
      });

    // Create offset label and its spinner bound to OFFSET controller property
    this.offsetLabel = new JLabel(SwingTools.getLocalizedLabelText(preferences,
        DimensionLinePanel.class, "offsetLabel.text", unitName));
    final NullableSpinner.NullableSpinnerLengthModel offsetSpinnerModel =
        new NullableSpinner.NullableSpinnerLengthModel(preferences, -10000, 10000);
    this.offsetSpinner = new NullableSpinner(offsetSpinnerModel);
    offsetSpinnerModel.setNullable(controller.getOffset() == null);
    offsetSpinnerModel.setLength(controller.getOffset());
    final PropertyChangeListener offsetChangeListener = new PropertyChangeListener() {
        public void propertyChange(PropertyChangeEvent ev) {
          offsetSpinnerModel.setNullable(ev.getNewValue() == null);
          offsetSpinnerModel.setLength((Float)ev.getNewValue());
        }
      };
    controller.addPropertyChangeListener(DimensionLineController.Property.OFFSET, offsetChangeListener);
    offsetSpinnerModel.addChangeListener(new ChangeListener() {
        public void stateChanged(ChangeEvent ev) {
          controller.removePropertyChangeListener(DimensionLineController.Property.OFFSET, offsetChangeListener);
          controller.setOffset(offsetSpinnerModel.getLength());
          controller.addPropertyChangeListener(DimensionLineController.Property.OFFSET, offsetChangeListener);
        }
      });

    // Orientation radio buttons bound to ORIENTATION controller property
    this.planDimensionLineRadioButton = new JRadioButton(SwingTools.getLocalizedLabelText(preferences,
        DimensionLinePanel.class, "planDimensionLineRadioButton.text"));
    this.planDimensionLineRadioButton.addChangeListener(new ChangeListener() {
        public void stateChanged(ChangeEvent ev) {
          if (planDimensionLineRadioButton.isSelected()) {
            controller.setOrientation(DimensionLineController.DimensionLineOrientation.PLAN);
          }
        }
      });
    this.elevationDimensionLineRadioButton = new JRadioButton(SwingTools.getLocalizedLabelText(preferences,
        DimensionLinePanel.class, "elevationDimensionLineRadioButton.text"));
    this.elevationDimensionLineRadioButton.addChangeListener(new ChangeListener() {
        public void stateChanged(ChangeEvent ev) {
          if (elevationDimensionLineRadioButton.isSelected()) {
            controller.setOrientation(DimensionLineController.DimensionLineOrientation.ELEVATION);
          }
        }
      });
    controller.addPropertyChangeListener(DimensionLineController.Property.ORIENTATION,
        new PropertyChangeListener() {
          public void propertyChange(PropertyChangeEvent ev) {
            updateOrientationRadioButtons(controller);
          }
        });

    ButtonGroup orientationButtonGroup = new ButtonGroup();
    orientationButtonGroup.add(this.planDimensionLineRadioButton);
    orientationButtonGroup.add(this.elevationDimensionLineRadioButton);

    // Create font size label and its spinner bound to FONT_SIZE controller property
    this.lengthFontSizeLabel = new JLabel(SwingTools.getLocalizedLabelText(preferences, DimensionLinePanel.class,
        "lengthFontSizeLabel.text", unitName));
    final NullableSpinner.NullableSpinnerLengthModel lenghtFontSizeSpinnerModel = new NullableSpinner.NullableSpinnerLengthModel(
        preferences, 5, 999);
    this.lengthFontSizeSpinner = new NullableSpinner(lenghtFontSizeSpinnerModel);
    final PropertyChangeListener fontSizeChangeListener = new PropertyChangeListener() {
        public void propertyChange(PropertyChangeEvent ev) {
          Float fontSize = controller.getLengthFontSize();
          lenghtFontSizeSpinnerModel.setNullable(fontSize == null);
          lenghtFontSizeSpinnerModel.setLength(fontSize);
        }
      };
    fontSizeChangeListener.propertyChange(null);
    controller.addPropertyChangeListener(DimensionLineController.Property.LENGTH_FONT_SIZE, fontSizeChangeListener);
    lenghtFontSizeSpinnerModel.addChangeListener(new ChangeListener() {
        public void stateChanged(ChangeEvent ev) {
          controller.removePropertyChangeListener(DimensionLineController.Property.LENGTH_FONT_SIZE, fontSizeChangeListener);
          controller.setLengthFontSize(lenghtFontSizeSpinnerModel.getLength());
          controller.addPropertyChangeListener(DimensionLineController.Property.LENGTH_FONT_SIZE, fontSizeChangeListener);
        }
      });

    // Create color label and button bound to controller COLOR property
    this.colorLabel = new JLabel(SwingTools.getLocalizedLabelText(preferences,
        DimensionLinePanel.class, "colorLabel.text"));

    this.colorButton = new ColorButton(preferences);
    if (OperatingSystem.isMacOSX()) {
      this.colorButton.putClientProperty("JButton.buttonType", "segmented");
      this.colorButton.putClientProperty("JButton.segmentPosition", "only");
    }
    this.colorButton.setColorDialogTitle(preferences
        .getLocalizedString(DimensionLinePanel.class, "colorDialog.title"));
    this.colorButton.setColor(controller.getColor() != null ? controller.getColor() : getForeground().getRGB());
    this.colorButton.addPropertyChangeListener(ColorButton.COLOR_PROPERTY, new PropertyChangeListener() {
        public void propertyChange(PropertyChangeEvent ev) {
          controller.setColor(colorButton.getColor());
        }
      });
    controller.addPropertyChangeListener(DimensionLineController.Property.COLOR, new PropertyChangeListener() {
        public void propertyChange(PropertyChangeEvent ev) {
          colorButton.setColor(controller.getColor());
        }
      });

    this.visibleIn3DViewCheckBox = new NullableCheckBox(SwingTools.getLocalizedLabelText(preferences,
        DimensionLinePanel.class, "visibleIn3DViewCheckBox.text"));
    this.visibleIn3DViewCheckBox.setNullable(controller.isVisibleIn3D() == null);
    this.visibleIn3DViewCheckBox.setValue(controller.isVisibleIn3D());
    final PropertyChangeListener visibilityChangeListener = new PropertyChangeListener() {
        public void propertyChange(PropertyChangeEvent ev) {
          visibleIn3DViewCheckBox.setValue(controller.isVisibleIn3D());
        }
      };
    controller.addPropertyChangeListener(DimensionLineController.Property.VISIBLE_IN_3D, visibilityChangeListener);
    this.visibleIn3DViewCheckBox.addChangeListener(new ChangeListener() {
        public void stateChanged(ChangeEvent ev) {
          controller.removePropertyChangeListener(DimensionLineController.Property.VISIBLE_IN_3D, visibilityChangeListener);
          controller.setVisibleIn3D(visibleIn3DViewCheckBox.getValue());
          if (visibleIn3DViewCheckBox.isNullable()) {
            visibleIn3DViewCheckBox.setNullable(false);
          }
          updateOrientationRadioButtons(controller);
          controller.addPropertyChangeListener(DimensionLineController.Property.VISIBLE_IN_3D, visibilityChangeListener);
        }
      });

    // Create pitch components bound to PITCH controller property
    this.pitchLabel = new JLabel(SwingTools.getLocalizedLabelText(preferences,
        DimensionLinePanel.class, "pitchLabel.text"));
    this.pitch0DegreeRadioButton = new JRadioButton(SwingTools.getLocalizedLabelText(preferences,
        DimensionLinePanel.class, "pitch0DegreeRadioButton.text"));
    ItemListener pitchRadioButtonsItemListener = new ItemListener() {
        public void itemStateChanged(ItemEvent ev) {
          if (pitch0DegreeRadioButton.isSelected()) {
            controller.setPitch(0f);
          } else if (pitch90DegreeRadioButton.isSelected()) {
            controller.setPitch((float)(-Math.PI / 2));
          }
        }
      };
    this.pitch0DegreeRadioButton.addItemListener(pitchRadioButtonsItemListener);
    this.pitch90DegreeRadioButton = new JRadioButton(SwingTools.getLocalizedLabelText(preferences,
        DimensionLinePanel.class, "pitch90DegreeRadioButton.text"));
    this.pitch90DegreeRadioButton.addItemListener(pitchRadioButtonsItemListener);
    final PropertyChangeListener pitchChangeListener = new PropertyChangeListener() {
        public void propertyChange(PropertyChangeEvent ev) {
          updateOrientationRadioButtons(controller);
        }
      };
    controller.addPropertyChangeListener(DimensionLineController.Property.PITCH, pitchChangeListener);
    this.visibleIn3DViewCheckBox.addChangeListener(new ChangeListener() {
        public void stateChanged(ChangeEvent ev) {
          controller.removePropertyChangeListener(DimensionLineController.Property.PITCH, pitchChangeListener);
          updateOrientationRadioButtons(controller);
          controller.addPropertyChangeListener(DimensionLineController.Property.PITCH, pitchChangeListener);
        }
      });

    ButtonGroup pitchGroup = new ButtonGroup();
    pitchGroup.add(this.pitch0DegreeRadioButton);
    pitchGroup.add(this.pitch90DegreeRadioButton);
    updateOrientationRadioButtons(controller);

    this.dialogTitle = preferences.getLocalizedString(DimensionLinePanel.class,
        modification
            ? "dimensionLineModification.title"
            : "dimensionLineCreation.title");
  }

  /**
   * Updates orientation radio buttons.
   */
  private void updateOrientationRadioButtons(DimensionLineController controller) {
    if (controller.getOrientation() == DimensionLineController.DimensionLineOrientation.PLAN) {
      this.planDimensionLineRadioButton.setSelected(true);
    } else if (controller.getOrientation() == DimensionLineController.DimensionLineOrientation.ELEVATION) {
      this.elevationDimensionLineRadioButton.setSelected(true);
    } else { // null
      SwingTools.deselectAllRadioButtons(this.planDimensionLineRadioButton, this.elevationDimensionLineRadioButton);
    }
    boolean orientable = controller.isEditableDistance();
    this.planDimensionLineRadioButton.setEnabled(orientable);
    this.elevationDimensionLineRadioButton.setEnabled(orientable);

    if (controller.getPitch() == null
        || controller.getOrientation() == DimensionLineController.DimensionLineOrientation.ELEVATION) {
      SwingTools.deselectAllRadioButtons(this.pitch0DegreeRadioButton, this.pitch90DegreeRadioButton);
    } else if (controller.getPitch() == 0) {
      this.pitch0DegreeRadioButton.setSelected(true);
    } else if (Math.abs(controller.getPitch()) == (float)(Math.PI / 2)) {
      this.pitch90DegreeRadioButton.setSelected(true);
    } else {
      SwingTools.deselectAllRadioButtons(this.pitch0DegreeRadioButton, this.pitch90DegreeRadioButton);
    }
    boolean planOrientation = controller.getOrientation() == DimensionLineController.DimensionLineOrientation.PLAN;
    boolean visibleIn3D = Boolean.TRUE.equals(controller.isVisibleIn3D());
    this.pitch0DegreeRadioButton.setEnabled(visibleIn3D && planOrientation);
    this.pitch90DegreeRadioButton.setEnabled(visibleIn3D && planOrientation);

    this.elevationStartSpinner.setEnabled(visibleIn3D
        || controller.getOrientation() == DimensionLineController.DimensionLineOrientation.ELEVATION);
    this.xEndSpinner.setEnabled(planOrientation);
    this.yEndSpinner.setEnabled(planOrientation);
  }

  /**
   * Sets components mnemonics and label / component associations.
   */
  private void setMnemonics(UserPreferences preferences) {
    if (!OperatingSystem.isMacOSX()) {
      this.xStartLabel.setDisplayedMnemonic(KeyStroke.getKeyStroke(
          preferences.getLocalizedString(DimensionLinePanel.class, "xLabel.mnemonic")).getKeyCode());
      this.xStartLabel.setLabelFor(this.xStartSpinner);
      this.yStartLabel.setDisplayedMnemonic(KeyStroke.getKeyStroke(
          preferences.getLocalizedString(DimensionLinePanel.class, "yLabel.mnemonic")).getKeyCode());
      this.yStartLabel.setLabelFor(this.yStartSpinner);
      this.elevationStartLabel.setDisplayedMnemonic(KeyStroke.getKeyStroke(
          preferences.getLocalizedString(DimensionLinePanel.class, "elevationLabel.mnemonic")).getKeyCode());
      this.elevationStartLabel.setLabelFor(this.elevationStartSpinner);
      this.xEndLabel.setDisplayedMnemonic(KeyStroke.getKeyStroke(
          preferences.getLocalizedString(DimensionLinePanel.class, "xLabel.mnemonic")).getKeyCode());
      this.xEndLabel.setLabelFor(this.xEndSpinner);
      this.yEndLabel.setDisplayedMnemonic(KeyStroke.getKeyStroke(
          preferences.getLocalizedString(DimensionLinePanel.class, "yLabel.mnemonic")).getKeyCode());
      this.yEndLabel.setLabelFor(this.yEndSpinner);
      this.planDimensionLineRadioButton.setMnemonic(KeyStroke.getKeyStroke(
          preferences.getLocalizedString(DimensionLinePanel.class, "planDimensionLineRadioButton.mnemonic")).getKeyCode());
      this.elevationDimensionLineRadioButton.setMnemonic(KeyStroke.getKeyStroke(
          preferences.getLocalizedString(DimensionLinePanel.class, "elevationDimensionLineRadioButton.mnemonic")).getKeyCode());
      this.distanceToEndPointLabel.setDisplayedMnemonic(KeyStroke.getKeyStroke(
          preferences.getLocalizedString(DimensionLinePanel.class, "distanceToEndPointLabel.mnemonic")).getKeyCode());
      this.distanceToEndPointLabel.setLabelFor(this.distanceToEndPointSpinner);
      this.offsetLabel.setDisplayedMnemonic(KeyStroke.getKeyStroke(
          preferences.getLocalizedString(DimensionLinePanel.class, "offsetLabel.mnemonic")).getKeyCode());
      this.offsetLabel.setLabelFor(this.offsetSpinner);
      this.lengthFontSizeLabel.setDisplayedMnemonic(KeyStroke.getKeyStroke(
          preferences.getLocalizedString(DimensionLinePanel.class, "lengthFontSizeLabel.mnemonic")).getKeyCode());
      this.lengthFontSizeLabel.setLabelFor(this.lengthFontSizeSpinner);
      this.visibleIn3DViewCheckBox.setMnemonic(KeyStroke.getKeyStroke(preferences.getLocalizedString(
          DimensionLinePanel.class, "visibleIn3DViewCheckBox.mnemonic")).getKeyCode());
      this.pitch0DegreeRadioButton.setMnemonic(KeyStroke.getKeyStroke(preferences.getLocalizedString(
          DimensionLinePanel.class, "pitch0DegreeRadioButton.mnemonic")).getKeyCode());
      this.pitch90DegreeRadioButton.setMnemonic(KeyStroke.getKeyStroke(preferences.getLocalizedString(
          DimensionLinePanel.class, "pitch90DegreeRadioButton.mnemonic")).getKeyCode());
    }
  }

  /**
   * Layouts panel components in panel with their labels.
   */
  private void layoutComponents(UserPreferences preferences,
                                final DimensionLineController controller) {
    int labelAlignment = OperatingSystem.isMacOSX()
        ? GridBagConstraints.LINE_END
        : GridBagConstraints.LINE_START;
    int standardGap = Math.round(5 * SwingTools.getResolutionScale());
    // First row
    final JPanel startPointPanel = SwingTools.createTitledPanel(
        preferences.getLocalizedString(DimensionLinePanel.class, "startPointPanel.title"));
    startPointPanel.add(this.xStartLabel, new GridBagConstraints(
        0, 0, 1, 1, 0, 0, labelAlignment,
        GridBagConstraints.NONE, new Insets(0, 0, 0, standardGap), 0, 0));
    startPointPanel.add(this.xStartSpinner, new GridBagConstraints(
        1, 0, 1, 1, 1, 0, GridBagConstraints.LINE_START,
        GridBagConstraints.HORIZONTAL, new Insets(0, 0, 0, 0), 5, 0));
    startPointPanel.add(this.yStartLabel, new GridBagConstraints(
        2, 0, 1, 1, 0, 0, labelAlignment,
        GridBagConstraints.NONE, new Insets(0, 2 * standardGap, 0, standardGap), 0, 0));
    startPointPanel.add(this.yStartSpinner, new GridBagConstraints(
        3, 0, 1, 1, 1, 0, GridBagConstraints.LINE_START,
        GridBagConstraints.HORIZONTAL, new Insets(0, 0, 0, 0), 5, 0));
    startPointPanel.add(this.elevationStartLabel, new GridBagConstraints(
        0, 1, 1, 1, 0, 0, labelAlignment,
        GridBagConstraints.NONE, new Insets(standardGap, 0, 0, standardGap), 0, 0));
    startPointPanel.add(this.elevationStartSpinner, new GridBagConstraints(
        1, 1, 1, 1, 1, 0, GridBagConstraints.LINE_START,
        GridBagConstraints.HORIZONTAL, new Insets(standardGap, 0, 0, 0), 5, 0));
    Insets rowInsets;
    if (OperatingSystem.isMacOSXLeopardOrSuperior()) {
      // User smaller insets for Mac OS X 10.5
      rowInsets = new Insets(0, 0, 0, 0);
    } else {
      rowInsets = new Insets(0, 0, standardGap, 0);
    }
    add(startPointPanel, new GridBagConstraints(
        0, 0, 1, 1, 0, 0, GridBagConstraints.LINE_START,
        GridBagConstraints.HORIZONTAL, rowInsets, 0, 0));

    // Second row
    final JPanel endPointPanel = SwingTools.createTitledPanel(
        preferences.getLocalizedString(DimensionLinePanel.class, "endPointPanel.title"));
    endPointPanel.add(this.xEndLabel, new GridBagConstraints(
        0, 0, 1, 1, 0, 0, labelAlignment,
        GridBagConstraints.NONE, new Insets(0, 0, 0, standardGap), 0, 0));
    endPointPanel.add(this.xEndSpinner, new GridBagConstraints(
        1, 0, 1, 1, 1, 0, GridBagConstraints.LINE_START,
        GridBagConstraints.HORIZONTAL, new Insets(0, 0, 0, 0), 5, 0));
    endPointPanel.add(this.yEndLabel, new GridBagConstraints(
        2, 0, 1, 1, 0, 0, GridBagConstraints.LINE_END,
        GridBagConstraints.NONE, new Insets(0, 2 * standardGap, 0, standardGap), 0, 0));
    endPointPanel.add(this.yEndSpinner, new GridBagConstraints(
        3, 0, 1, 1, 1, 0, GridBagConstraints.LINE_START,
        GridBagConstraints.HORIZONTAL, new Insets(0, 0, 0, 0), 5, 0));
    // Add distance label and spinner at second row of endPointPanel
    endPointPanel.add(this.distanceToEndPointLabel, new GridBagConstraints(
        0, 1, 3, 1, 0, 0, GridBagConstraints.LINE_END,
        GridBagConstraints.NONE, new Insets(standardGap, 0, 0, standardGap), 0, 0));
    endPointPanel.add(this.distanceToEndPointSpinner, new GridBagConstraints(
        3, 1, 1, 1, 1, 0, GridBagConstraints.LINE_START,
        GridBagConstraints.HORIZONTAL, new Insets(standardGap, 0, 0, 0), 0, 0));
    // Add offset label and spinner at third row of endPointPanel
    endPointPanel.add(this.offsetLabel, new GridBagConstraints(
        2, 2, 1, 1, 0, 0, labelAlignment,
        GridBagConstraints.NONE, new Insets(standardGap, 0, 0, standardGap), 0, 0));
    endPointPanel.add(this.offsetSpinner, new GridBagConstraints(
        3, 2, 1, 1, 1, 0, GridBagConstraints.LINE_START,
        GridBagConstraints.HORIZONTAL, new Insets(standardGap, 0, 0, 0), 0, 0));
    // Add orientation radio buttons at fourth row of endPointPanel
    endPointPanel.add(new JSeparator(), new GridBagConstraints(
        0, 3, 4, 1, 0, 0, GridBagConstraints.CENTER,
        GridBagConstraints.HORIZONTAL, new Insets(standardGap, 0, 0, 0), 0, 0));
    endPointPanel.add(this.planDimensionLineRadioButton, new GridBagConstraints(
        0, 4, 2, 1, 0, 0, GridBagConstraints.CENTER,
        GridBagConstraints.NONE, new Insets(3, 0, 0, 0), 0, 0));
    endPointPanel.add(this.elevationDimensionLineRadioButton, new GridBagConstraints(
        2, 4, 2, 1, 0, 0, GridBagConstraints.CENTER,
        GridBagConstraints.NONE, new Insets(3, 2 * standardGap, 0, 0), 0, 0));
    add(endPointPanel, new GridBagConstraints(
        0, 1, 1, 1, 0, 0, GridBagConstraints.LINE_START,
        GridBagConstraints.HORIZONTAL, rowInsets, 0, 0));

    // Third row
    JPanel stylePanel = SwingTools.createTitledPanel(
        preferences.getLocalizedString(DimensionLinePanel.class, "stylePanel.title"));
    stylePanel.add(this.lengthFontSizeLabel, new GridBagConstraints(
        0, 0, 1, 1, 0, 0, labelAlignment,
        GridBagConstraints.NONE, new Insets(0, 0, 0, standardGap), 0, 0));
    stylePanel.add(this.lengthFontSizeSpinner, new GridBagConstraints(
        1, 0, 1, 1, 0, 0, GridBagConstraints.LINE_START,
        GridBagConstraints.HORIZONTAL, new Insets(0, 0, 0, 0), 5, 0));
    stylePanel.add(this.colorLabel, new GridBagConstraints(
        2, 0, 1, 1, 0, 0, labelAlignment,
        GridBagConstraints.NONE, new Insets(0, 2 * standardGap, 0, standardGap), 0, 0));
    stylePanel.add(this.colorButton, new GridBagConstraints(
        3, 0, 1, 1, 0, 0, GridBagConstraints.LINE_START,
        GridBagConstraints.NONE, new Insets(0, 0, 0, 0), 0, 0));
    add(stylePanel, new GridBagConstraints(
        0, 2, 1, 1, 0, 0, GridBagConstraints.LINE_START,
        GridBagConstraints.BOTH, new Insets(0, 0, 0, 0), 0, 0));

    // Fourth row
    JPanel rendering3DPanel = SwingTools.createTitledPanel(
        preferences.getLocalizedString(DimensionLinePanel.class, "rendering3DPanel.title"));
    rendering3DPanel.add(this.visibleIn3DViewCheckBox, new GridBagConstraints(
        0, 0, 3, 1, 0, 0, GridBagConstraints.LINE_START,
        GridBagConstraints.NONE, new Insets(0, OperatingSystem.isMacOSX() ? -8 : 0, standardGap, 0), 0, 0));
    rendering3DPanel.add(this.pitchLabel, new GridBagConstraints(
        0, 1, 1, 1, 0, 0, labelAlignment,
        GridBagConstraints.NONE, new Insets(0, 0, standardGap, standardGap), 0, 0));
    rendering3DPanel.add(this.pitch0DegreeRadioButton, new GridBagConstraints(
        1, 1, 1, 1, 0, 0, GridBagConstraints.LINE_START,
        GridBagConstraints.NONE, new Insets(0, 0, standardGap, standardGap), 0, 0));
    rendering3DPanel.add(this.pitch90DegreeRadioButton, new GridBagConstraints(
        2, 1, 1, 1, 0, 0, GridBagConstraints.LINE_START,
        GridBagConstraints.NONE, new Insets(0, 0, standardGap, 0), 0, 0));
    add(rendering3DPanel, new GridBagConstraints(
        0, 3, 1, 1, 0, 0, GridBagConstraints.LINE_START,
        GridBagConstraints.HORIZONTAL, new Insets(0, 0, 0, 0), 0, 0));

    PropertyChangeListener distanceListener = new PropertyChangeListener() {
        public void propertyChange(PropertyChangeEvent ev) {
          distanceToEndPointLabel.setVisible(controller.isEditableDistance());
          distanceToEndPointSpinner.setVisible(controller.isEditableDistance());
        }
      };
    controller.addPropertyChangeListener(DimensionLineController.Property.EDITABLE_DISTANCE, distanceListener);
    distanceListener.propertyChange(null);
  }

  /**
   * Displays this panel in a modal dialog box.
   */
  public void displayView(View parentView) {
    JSpinner editedSpinner = this.distanceToEndPointSpinner.getValue() != null
        ? this.distanceToEndPointSpinner
        : this.offsetSpinner;
    if (SwingTools.showConfirmDialog((JComponent)parentView,
            this, this.dialogTitle, ((JSpinner.DefaultEditor)editedSpinner.getEditor()).getTextField()) == JOptionPane.OK_OPTION
        && this.controller != null) {
      if (this.dimensionLineModification) {
        this.controller.modifyDimensionLines();
      } else {
        this.controller.createDimensionLine();
      }
    }
  }
}
