/*
 * CatalogItemToolTip.java 17 avr. 2013
 *
 * Sweet Home 3D, Copyright (c) 2013 Emmanuel PUYBARET / eTeks <info@eteks.com>
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

import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Image;
import java.awt.Insets;
import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.Format;
import java.text.NumberFormat;
import java.util.Iterator;

import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.stream.ImageInputStream;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JToolTip;

import com.eteks.sweethome3d.model.CatalogItem;
import com.eteks.sweethome3d.model.CatalogPieceOfFurniture;
import com.eteks.sweethome3d.model.CatalogTexture;
import com.eteks.sweethome3d.model.UserPreferences;
import com.eteks.sweethome3d.tools.OperatingSystem;
import com.eteks.sweethome3d.tools.URLContent;

/**
 * A tool tip displaying the information and the icon of a catalog item.
 * @author Emmanuel Puybaret
 */
public class CatalogItemToolTip extends JToolTip {
  /**
   * Type of information displayed by a tool tip.
   */
  public enum DisplayedInformation {
    /**
     * A tool tip displaying only the icon of a piece of furniture.
     */
    ICON,
    /**
     * A tool tip displaying only the icon, the name and the author of a piece of furniture.
     */
    ICON_NAME_AUTHOR,
    /**
     * A tool tip displaying only the icon, the name, the author and the category
     * of a piece of furniture.
     */
    ICON_NAME_AUTHOR_CATEGORY}

  private static final int ICON_SIZE = Math.round(128 * SwingTools.getResolutionScale());

  private final DisplayedInformation displayedInformation;
  private final UserPreferences      preferences;
  private final JLabel               itemIconLabel;
  private CatalogItem                catalogItem;

  /**
   * Creates a tool tip that displays the icon of a piece of furniture, its name
   * and its category if <code>ignoreCategory</code> is <code>true</code>.
   */
  public CatalogItemToolTip(boolean ignoreCategory,
                            UserPreferences preferences) {
    this(ignoreCategory
           ? DisplayedInformation.ICON_NAME_AUTHOR
           : DisplayedInformation.ICON_NAME_AUTHOR_CATEGORY,
         preferences);
  }

  /**
   * Creates a tool tip that displays furniture information.
   */
  public CatalogItemToolTip(DisplayedInformation displayedInformation,
                            UserPreferences preferences) {
    this.displayedInformation = displayedInformation;
    this.preferences = preferences;
    this.itemIconLabel = new JLabel();
    this.itemIconLabel.setPreferredSize(new Dimension(ICON_SIZE, ICON_SIZE));
    // Force minimum size to ensure icon label won't be displayed at bottom of the tool tip
    this.itemIconLabel.setMinimumSize(this.itemIconLabel.getPreferredSize());
    this.itemIconLabel.setHorizontalAlignment(JLabel.CENTER);
    this.itemIconLabel.setVerticalAlignment(JLabel.CENTER);
    setLayout(new GridBagLayout());
    add(this.itemIconLabel, new GridBagConstraints(0, 0, 1, 1, 1, 1,
        GridBagConstraints.SOUTH, GridBagConstraints.NONE, new Insets(3, 3, 3, 3), 0, 0));
  }

  /**
   * Sets the catalog item displayed by this tool tip.
   */
  public void setCatalogItem(CatalogItem item) {
    if (item != this.catalogItem) {
      String tipTextCreator = null;
      String tipTextModelSize = null;
      String tipTextDimensions = null;
      String tipTextDescription = null;
      if (this.preferences != null) {
        String creator = item.getCreator();
        if (creator != null && creator.length() > 0) {
          tipTextCreator = this.preferences.getLocalizedString(CatalogItemToolTip.class, "tooltipCreator", creator);
        }
        Format format = this.preferences.getLengthUnit().getFormatWithUnit();
        if (item instanceof CatalogPieceOfFurniture) {
          CatalogPieceOfFurniture piece = (CatalogPieceOfFurniture)item;
          tipTextDimensions = this.preferences.getLocalizedString(CatalogItemToolTip.class, "tooltipPieceOfFurnitureDimensions",
              format.format(piece.getWidth()), format.format(piece.getDepth()), format.format(piece.getHeight()));
          if (piece.getModelSize() != null && piece.getModelSize() > 0) {
            tipTextModelSize = this.preferences.getLocalizedString(CatalogItemToolTip.class, "tooltipModelSize",
                NumberFormat.getIntegerInstance().format(Math.max(1, (int)Math.round(piece.getModelSize() / 1000.))));
          }
          tipTextDescription = piece.getDescription();
          if (tipTextDescription != null) {
            if (Boolean.getBoolean("com.eteks.sweethome3d.descriptionIgnoredInCatalogToolTip")) {
              tipTextDescription = null;
            } else {
              int descriptionLength = tipTextDescription.length();
              int maxLineLength = Math.max(tipTextDimensions.length(), piece.getName() != null ? piece.getName().length() : 0);
              if (descriptionLength > 200
                  || tipTextDescription.indexOf(' ') < 0 && descriptionLength > maxLineLength
                  || tipTextDescription.indexOf(' ') >= maxLineLength) {
                tipTextDescription = tipTextDescription.substring(0, descriptionLength > 200 ? 200 : maxLineLength) + "...";
              }
              if (tipTextDescription.indexOf("<br>") < 0
                  && descriptionLength > maxLineLength) {
                // Split long description to avoid too large tool tips
                String [] texts = tipTextDescription.split(" ");
                tipTextDescription = "";
                for (int i = 0; i < texts.length; ) {
                  String line = texts [i++];
                  while (i < texts.length && line.length() < maxLineLength) {
                    line += " " + texts [i++];
                  }
                  if (tipTextDescription.length() > 0) {
                    tipTextDescription += "<br>";
                  }
                  tipTextDescription += line;
                }
              }
            }
          }
        } else if (item instanceof CatalogTexture) {
          CatalogTexture piece = (CatalogTexture)item;
          tipTextDimensions = this.preferences.getLocalizedString(CatalogItemToolTip.class, "tooltipTextureDimensions",
              format.format(piece.getWidth()), format.format(piece.getHeight()));
        }
      }

      // Use HTML presentation in the tip text only from Java 6 because with Java 5,
      // HTML tags are not reinterpreted as soon as the tip text is changed
      String tipText;
      boolean iconInHtmlImgTag = false;
      if (OperatingSystem.isJavaVersionGreaterOrEqual("1.6")) {
        if (this.displayedInformation != DisplayedInformation.ICON) {
          tipText = "<html><center>";
          if (this.displayedInformation == DisplayedInformation.ICON_NAME_AUTHOR_CATEGORY
              && (item instanceof CatalogPieceOfFurniture)) {
            tipText += "- <b>" + ((CatalogPieceOfFurniture)item).getCategory().getName() + "</b> -<br>";
          }

          tipText += "<b>" + item.getName() + "</b>";
          if (tipTextDescription != null) {
            tipText += "<br>" + tipTextDescription + "</table>";
          }
          if (tipTextDimensions != null) {
            tipText += "<br>" + tipTextDimensions;
          }
          if (tipTextModelSize != null) {
            tipText += "<br>" + tipTextModelSize;
          }
          if (tipTextCreator != null) {
            tipText += "<br>" + tipTextCreator;
          }
          tipText += "</center>";
        } else {
          tipText = "";
        }
      } else if (isTipTextComplete()) {
        // Use an alternate HTML presentation that includes icon in an <img> tag
        // for the Mac OS X users who still run Sweet Home 3D under Java 5
        // because jar protocol bug mentioned further doesn't cause issues under this system
        iconInHtmlImgTag = true;

        tipText = "<html><table>";
        if (this.displayedInformation != DisplayedInformation.ICON) {
          tipText += "<tr><td align='center'>";
          if (this.displayedInformation == DisplayedInformation.ICON_NAME_AUTHOR_CATEGORY
              && (item instanceof CatalogPieceOfFurniture)) {
            tipText += "- <b>" + ((CatalogPieceOfFurniture)item).getCategory().getName() + "</b> -<br>";
          }
          tipText += "<b>" + item.getName() + "</b>";
          if (tipTextDescription != null) {
            tipText += "<br>" + tipTextDescription;
          }
          if (tipTextDimensions != null) {
            tipText += "<br>" + tipTextDimensions;
          }
          if (tipTextModelSize != null) {
            tipText += "<br>" + tipTextModelSize;
          }
          if (tipTextCreator != null) {
            tipText += "<br>" + tipTextCreator;
          }
          tipText += "</td></tr>";
        }
      } else if (this.displayedInformation != DisplayedInformation.ICON) {
          // Use plain text presentation
          tipText = item.getName();
          if (tipTextCreator != null) {
            tipText += " " + tipTextCreator;
          }
      } else {
        tipText = null;
      }

      this.itemIconLabel.setIcon(null);
      if (item.getIcon() instanceof URLContent) {
        InputStream iconStream = null;
        try {
          // Ensure image will always be viewed in a 128x128 pixels cell
          iconStream = new BufferedInputStream(item.getIcon().openStream());
          // Prefer to use a JLabel for the piece icon instead of a HTML <img> tag
          // to avoid using cache to access files with jar protocol as suggested
          // in http://bugs.sun.com/bugdatabase/view_bug.do?bug_id=6962459
          ByteArrayOutputStream out = new ByteArrayOutputStream();
          byte [] bytes = new byte [1024];
          for (int i; (i = iconStream.read(bytes)) != -1; ) {
            out.write(bytes, 0, i);
          }
          byte [] imageData = out.toByteArray();

          ImageInputStream imageInputStream = ImageIO.createImageInputStream(new ByteArrayInputStream(imageData));
          Iterator<ImageReader> it = ImageIO.getImageReaders(imageInputStream);
          if (it.hasNext()) {
            ImageReader reader = (ImageReader)it.next();
            reader.setInput(imageInputStream);
            int imageWidth = reader.getWidth(reader.getMinIndex());
            int imageHeight = reader.getHeight(reader.getMinIndex());
            int width = Math.round(ICON_SIZE * Math.min(1f, (float)imageWidth / imageHeight));
            int height = Math.round((float)width * imageHeight / imageWidth);

            if (iconInHtmlImgTag) {
              tipText += "<tr><td width='" + ICON_SIZE + "' height='" + ICON_SIZE + "' align='center' valign='middle'><img width='" + width
                  + "' height='" + height + "' src='"
                  + ((URLContent)item.getIcon()).getURL() + "'></td></tr>";
            } else {
              // Create ImageIcon from imageData to keep GIF animation if any
              ImageIcon imageIcon = new ImageIcon(imageData);
              if (imageHeight != height) {
                imageIcon.setImage(imageIcon.getImage().getScaledInstance(width, height,
                    reader.getNumImages(true) > 1 ? Image.SCALE_DEFAULT : Image.SCALE_SMOOTH));
              }
              this.itemIconLabel.setIcon(imageIcon);
            }
            reader.dispose();
          }
        } catch (IOException ex) {
        } finally {
          if (iconStream != null) {
            try {
              iconStream.close();
            } catch (IOException ex) {
            }
          }
        }
      }

      if (iconInHtmlImgTag) {
        tipText += "</table>";
      }
      setTipText(tipText);
      this.catalogItem = item;
    }
  }

  /**
   * Returns <code>true</code> if the text of this tool tip contains
   * all the information that the tool tip should display.
   */
  public boolean isTipTextComplete() {
    return !OperatingSystem.isJavaVersionGreaterOrEqual("1.6")
        && OperatingSystem.isMacOSX();
  }

  @Override
  public Dimension getPreferredSize() {
    Dimension preferredSize = super.getPreferredSize();
    if (this.itemIconLabel.getIcon() != null) {
      if (OperatingSystem.isWindows()
          && OperatingSystem.isJavaVersionBetween("10", "16")) {
        // Enlarge tool tip to ensure its text isn't split on more lines
        // See https://bugs.openjdk.java.net/browse/JDK-8213535
        preferredSize.width += 8;
      }
      preferredSize.width = Math.max(preferredSize.width, ICON_SIZE + Math.round(8 * SwingTools.getResolutionScale()));
      preferredSize.height += ICON_SIZE + Math.round(8 * SwingTools.getResolutionScale());
    }
    return preferredSize;
  }
}

