package org.onebusaway.realtime.hamilton.services;

import org.onebusaway.realtime.hamilton.model.TripMatch;
import org.onebusaway.transit_data_federation.services.transit_graph.BlockTripEntry;

public interface VehicleLocationService {

  Double computeDistanceAlongBlock(String vehicleId, BlockTripEntry trip, Double lat, Double lon);
  
}
