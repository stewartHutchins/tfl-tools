/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.kurtraschke.tfl.tools;

import org.osgeo.proj4j.CRSFactory;
import org.osgeo.proj4j.CoordinateReferenceSystem;
import org.osgeo.proj4j.CoordinateTransform;
import org.osgeo.proj4j.CoordinateTransformFactory;
import org.osgeo.proj4j.ProjCoordinate;

/**
 *
 * @author kurt
 */
public class CoordinateTransformer {

  private static final CoordinateTransform ct;

  static {
    CRSFactory crsf = new CRSFactory();
    CoordinateReferenceSystem from = crsf.createFromParameters("OSGB36", "+proj=tmerc +lat_0=49 +lon_0=-2 +k=0.9996012717 +x_0=400000 +y_0=-100000 +ellps=airy +towgs84=446.448,-125.157,542.060,0.1502,0.2470,0.8421,-20.4894 +units=m +no_defs");
    CoordinateReferenceSystem to = crsf.createFromParameters("WGS84", "+proj=longlat +ellps=WGS84 +towgs84=0,0,0 +no_defs");
    CoordinateTransformFactory ctf = new CoordinateTransformFactory();
    ct = ctf.createTransform(from, to);
  }

  private CoordinateTransformer() {

  }

  public static ProjCoordinate transformCoordinates(int easting, int northing) {
    ProjCoordinate pc = new ProjCoordinate(easting, northing);
    
    ProjCoordinate out = new ProjCoordinate();

    return ct.transform(pc, out);
  }
}
