/*
 * ObjectProperty.java 26 avr. 2023
 *
 * Copyright (c) 2023 Emmanuel PUYBARET / eTeks <info@eteks.com>. All Rights Reserved.
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

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.Serializable;

/**
 * Information about additional property.
 * @author Emmanuel Puybaret
 * @since 7.2
 */
public class ObjectProperty implements Serializable {
  private static final long serialVersionUID = 1L;

  /**
   * Property type.
   */
  public enum Type {
    ANY,
    STRING,
    BOOLEAN,
    INTEGER,
    NUMBER,
    PRICE,
    LENGTH,
    PERCENTAGE,
    DATE,
    CONTENT
  }

  private final String    name;
  private final String    displayedName;
  private transient Type  type;
  private final String    typeName;
  private final boolean   displayable;
  private final boolean   modifiable;
  private final boolean   exportable;

  /**
   * Creates a displayable / modifiable property with the given name.
   */
  public ObjectProperty(String name) {
    this(name, null);
  }

  public ObjectProperty(String name, Type type) {
    this(name, null, type, true, true, true);
  }

  public ObjectProperty(String name,
                        Type type,
                        boolean displayable,
                        boolean modifiable,
                        boolean exportable) {
    this(name, null, type, true, true, true);
  }

  public ObjectProperty(String name,
                        String displayedName,
                        Type type,
                        boolean displayable,
                        boolean modifiable,
                        boolean exportable) {
    this.name = name;
    this.displayedName = displayedName;
    this.type = type;
    this.typeName = type != null ? type.name() : null;
    this.displayable = displayable;
    this.modifiable = modifiable;
    this.exportable = exportable;
  }

  /**
   * Initializes transient fields.
   */
  private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
    this.type = null;
    in.defaultReadObject();
    // Read type from string
    try {
      if (this.typeName != null) {
        this.type = Type.valueOf(this.typeName);
      }
    } catch (IllegalArgumentException ex) {
      // Ignore malformed enum constant
    }
  }

  /**
   * Returns an <code>ObjectProperty</code> instance built from the given <code>description</code>.
   * @param description a string containing property name possibly followed by a
   *   colon, the property type and displayable modifiable exportable displayedName attributes
   *   name:(ANY|STRING|DATE|BOOLEAN|INTEGER|NUMBER|PRICE|LENGTH|PERCENTAGE|CONTENT) displayable=(true|false) modifiable=(true|false) exportable=(true|false) displayedName=text
   */
  public static ObjectProperty fromDescription(String description) {
    if (description.length() == 0) {
      throw new IllegalArgumentException("Description empty");
    }
    int colonIndex = description.indexOf(':');
    String propertyName = description.substring(0, colonIndex > 0 ? colonIndex : description.length()).trim();
    ObjectProperty.Type type = null;
    boolean displayable = true;
    boolean modifiable = true;
    boolean exportable = true;
    String displayedName = null;
    if (colonIndex > 0) {
      // Colon may be followed by property type then displayable modifiable exportable displayedName attributes
      // :(ANY|STRING|DATE|BOOLEAN|INTEGER|NUMBER|PRICE|LENGTH|PERCENTAGE|CONTENT) displayable=(true|false) modifiable=(true|false) exportable=(true|false) displayedName=text
      String [] attributes = description.substring(colonIndex + 1).trim().split(" ");
      if (attributes.length > 0) {
        type = ObjectProperty.Type.valueOf(attributes [0]);
        for (int i = 1; i < attributes.length; i++) {
          if (attributes [i].startsWith("displayable=")) {
            displayable = "true".equalsIgnoreCase(attributes [i].substring("displayable=".length() + 1));
          } else if (attributes [i].startsWith("modifiable=")) {
            modifiable = "true".equalsIgnoreCase(attributes [i].substring("modifiable=".length() + 1));
          } else if (attributes [i].startsWith("exportable=")) {
            exportable = "true".equalsIgnoreCase(attributes [i].substring("exportable=".length() + 1));
          } else if (attributes [i].startsWith("displayedName=")) {
            displayedName = description.substring(description.indexOf("displayedName=") + "displayedName=".length());
          }
        }
      }
    }
    return new ObjectProperty(propertyName, displayedName, type, displayable, modifiable, exportable);
  }

  /**
   * Returns the name of this property.
   */
  public String getName() {
    return this.name;
  }

  /**
   * Returns the type of this property.
   */
  public Type getType() {
    return this.type;
  }

  /**
   * Returns whether the value of this property is displayable.
   */
  public boolean isDisplayable() {
    return this.displayable;
  }

  /**
   * Returns whether the value of this property is modifiable.
   */
  public boolean isModifiable() {
    return this.modifiable;
  }

  /**
   * Returns whether the value of this property is exportable.
   */
  public boolean isExportable() {
    return this.exportable;
  }

  /**
   * Returns a text used for label and header.
   */
  public String getDisplayedName() {
    if (this.displayedName != null) {
      return this.displayedName;
    } else {
      String displayedName = this.name.replace('_', ' ').toLowerCase();
      return Character.toUpperCase(displayedName.charAt(0))
          + (displayedName.length() > 1 ? displayedName.substring(1) : "");
    }
  }

  @Override
  public int hashCode() {
    return this.name == null ? 0 : this.name.hashCode();
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    } else if (obj instanceof ObjectProperty) {
      return this.name.equals(((ObjectProperty)obj).name);
    }
    return false;
  }
}
