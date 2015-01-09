package org.onebusaway.realtime.hamilton.model;

public class TripMatch {
  private String tripId;
  private boolean isFrequency;
  private String stopId;
  private String routeId;
  private String blockId;
  private Integer scheduleDeviation;
  
  public TripMatch(String tripId, boolean isFrequency, String stopId, String routeId, String blockId, Integer scheduleDeviation) {
    this.tripId = tripId;
    this.isFrequency = isFrequency;
    this.stopId = stopId;
    this.routeId = routeId;
    this.blockId = blockId;
    this.scheduleDeviation = scheduleDeviation;
  }
  public String getTripId() {
    return tripId;
  }
  public boolean isFrequency() {
    return isFrequency;
  }
  public String getStopId() {
    return stopId;
  }
  public void setStopId(String stopId) {
    this.stopId = stopId;
  }
  public String getBlockId() {
    return blockId;
  }
  public Integer getScheduleDeviation() {
    return scheduleDeviation;
  }
  public void setScheduleDeviation(Integer scheduleDeviation) {
    this.scheduleDeviation = scheduleDeviation;
  }
  public String toString() {
    return "{" + tripId + "[" + routeId + "](dev=" + scheduleDeviation + ")}";
  }
}

