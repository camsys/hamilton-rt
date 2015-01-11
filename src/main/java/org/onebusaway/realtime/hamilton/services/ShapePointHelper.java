package org.onebusaway.realtime.hamilton.services;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.onebusaway.gtfs.model.AgencyAndId;
import org.onebusaway.gtfs.model.ShapePoint;
import org.onebusaway.transit_data_federation.model.ShapePoints;
import org.onebusaway.transit_data_federation.services.shapes.ShapePointService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ShapePointHelper {
  private static final Logger _log = LoggerFactory.getLogger(ShapePointHelper.class);
  
  private ShapePointService _shapePointService;

  public void setShapePointService(ShapePointService sps) {
    _shapePointService = sps;
  }


  private Map<AgencyAndId, ShapePoints> _cache = new HashMap<AgencyAndId, ShapePoints>();

  public ShapePoints getShapePointsForShapeId(AgencyAndId shapeId) {

    ShapePoints shapePoints = _cache.get(shapeId);
    if (shapePoints == null) {
      shapePoints = getShapePointsForShapeIdNonCached(shapeId);
      _cache.put(shapeId, shapePoints);
    }
    return shapePoints;
  }

  private ShapePoints getShapePointsForShapeIdNonCached(AgencyAndId shapeId) {
    
    ShapePoints shapePointsList = _shapePointService.getShapePointsForShapeId(shapeId);
    if (shapePointsList == null) {
      _log.error("no shape points found for shapeId=" + shapeId);
      return null;
    }
    List<ShapePoint> shapePoints = new ArrayList<ShapePoint>();
    for (int i = 0; i <shapePointsList.getSize(); i++) {
      ShapePoint sp = new ShapePoint();
      sp.setSequence(i);
      sp.setLat(shapePointsList.getLatForIndex(i));
      sp.setLon(shapePointsList.getLonForIndex(i));
      sp.setDistTraveled(shapePointsList.getDistTraveledForIndex(i));
      shapePoints.add(sp);
    }

    shapePoints = deduplicateShapePoints(shapePoints);

    if (shapePoints.isEmpty())
      return null;

    int n = shapePoints.size();

    double[] lat = new double[n];
    double[] lon = new double[n];
    double[] distTraveled = new double[n];

    int i = 0;
    for (ShapePoint shapePoint : shapePoints) {
      lat[i] = shapePoint.getLat();
      lon[i] = shapePoint.getLon();
      i++;
    }

    ShapePoints result = new ShapePoints();
    result.setShapeId(shapeId);
    result.setLats(lat);
    result.setLons(lon);
    result.setDistTraveled(distTraveled);

    result.ensureDistTraveled();

    return result;
  }

  private List<ShapePoint> deduplicateShapePoints(List<ShapePoint> shapePoints) {

    List<ShapePoint> deduplicated = new ArrayList<ShapePoint>();
    ShapePoint prev = null;

    for (ShapePoint shapePoint : shapePoints) {
      if (prev == null
          || !(prev.getLat() == shapePoint.getLat() && prev.getLon() == shapePoint.getLon())) {
        deduplicated.add(shapePoint);
      }
      prev = shapePoint;
    }

    return deduplicated;
  }
}
