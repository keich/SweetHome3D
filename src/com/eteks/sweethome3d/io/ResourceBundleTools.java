/*
 * ResourceBundleTools.java 21 avr. 2021
 *
 * Sweet Home 3D, Copyright (c) 2021 Emmanuel PUYBARET / eTeks <info@eteks.com>
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
package com.eteks.sweethome3d.io;

import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Locale;
import java.util.MissingResourceException;
import java.util.PropertyResourceBundle;
import java.util.ResourceBundle;

import com.eteks.sweethome3d.tools.URLContent;

/**
 * Tools for resource bundle handling
 * @author Emmanuel Puybaret
 */
class ResourceBundleTools {
  private ResourceBundleTools() {
    // This class contains only tools
  }

  /**
   * Returns the resource bundle of <code>familyName</code> name read from the given URL.
   */
  static ResourceBundle getBundle(URL pluginFurnitureCatalogUrl, String familyName) {
    // Build resource bundle hierarchy without using
    // ResourceBundle.getBundle(PLUGIN_FURNITURE_CATALOG_FAMILY, Locale.getDefault(), urlLoader)
    // because from Java 9, ResourceBundle#getBundle uses Java modules which implementation
    // doesn't allow to control how .properties entries in a .zip file are read, whereas until Java 8,
    // URLContent#openStream could be called in an overridden getResourceAsStream method of ClassLoader
    Locale defaultLocale = Locale.getDefault();
    String language = defaultLocale.getLanguage();
    String country = defaultLocale.getCountry();
    String [] suffixes = {".properties",
                          "_" + language + ".properties",
                          "_" + language + "_" + country + ".properties"};
    ResourceBundle resourceBundle = null;
    for (String suffix : suffixes) {
      try {
        // Return a stream managed by URLContent to be able to delete the writable files accessed with jar protocol
        URLContent content = new URLContent(new URL("jar:" + pluginFurnitureCatalogUrl.toURI() + "!/" + familyName + suffix));
        InputStream in = content.openStream();
        if (in != null) {
          final ResourceBundle parentResourceBundle = resourceBundle;
          try {
            resourceBundle = new PropertyResourceBundle(in) {
              {
                setParent(parentResourceBundle);
              }
            };
          } catch (IllegalArgumentException ex) {
            // May happen if the file contains some wrongly encoded characters
            ex.printStackTrace();
          } finally {
            in.close();
          }
        }
      } catch (IOException ex) {
      } catch (URISyntaxException ex1) {
      }
    }
    if (resourceBundle != null) {
      return resourceBundle;
    } else {
      throw new MissingResourceException("Can't find bundle for base name " + familyName, familyName, "");
    }
  }

  /**
   * Returns the value of <code>propertyKey</code> in <code>resource</code>,
   * or <code>defaultValue</code> if the property doesn't exist.
   */
  static String getOptionalString(ResourceBundle resource,
                                  String propertyKey,
                                  String defaultValue) {
    try {
      return resource.getString(propertyKey);
    } catch (MissingResourceException ex) {
      return defaultValue;
    }
  }

  /**
   * Returns the value of <code>propertyKey</code> in <code>resource</code>,
   * or <code>defaultValue</code> if the property doesn't exist.
   */
  static float getOptionalFloat(ResourceBundle resource,
                                String propertyKey,
                                float defaultValue) {
    try {
      return Float.parseFloat(resource.getString(propertyKey));
    } catch (MissingResourceException ex) {
      return defaultValue;
    }
  }

  /**
   * Returns the boolean value of <code>propertyKey</code> in <code>resource</code>,
   * or <code>defaultValue</code> if the property doesn't exist.
   */
  static boolean getOptionalBoolean(ResourceBundle resource,
                                    String propertyKey,
                                    boolean defaultValue) {
    try {
      return Boolean.parseBoolean(resource.getString(propertyKey));
    } catch (MissingResourceException ex) {
      return defaultValue;
    }
  }
}
