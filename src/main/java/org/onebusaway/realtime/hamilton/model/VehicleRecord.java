package org.onebusaway.realtime.hamilton.model;

import java.sql.Timestamp;
import java.util.ArrayList;

public class VehicleRecord {

  private String vehicleId;
  private int delay;
  private double lat;
  private double lon;
  private int speed;
  private int bearing;
  private int seq;
  private Timestamp time;
  private String stopId;
  private String routeId;
  private String tripId;
  private boolean isFrequency;
  private ArrayList<StopTimeInfo> stopTimeInfos;
  
  public String getVehicleId() {
    return vehicleId;
  }
  public void setVehicleId(String vehicleId) {
    this.vehicleId = vehicleId;
  }
  public int getDelay() {
    return delay;
  }
  public void setDelay(int delay) {
    this.delay = delay;
  }
  public double getLat() {
    return lat;
  }
  public void setLat(double lat) {
    this.lat = lat;
  }
  public double getLon() {
    return lon;
  }
  public void setLon(double lon) {
    this.lon = lon;
  }
  public int getSpeed() {
    return speed;
  }
  public void setSpeed(int speed) {
    this.speed = speed;
  }
  public int getBearing() {
    return bearing;
  }
  public void setBearing(int bearing) {
    this.bearing = bearing;
  }
  public int getSeq() {
    return seq;
  }
  public void setSeq(int seq) {
    this.seq = seq;
  }
  public Timestamp getTime() {
    return time;
  }
  public void setTime(Timestamp time) {
    this.time = time;
  }
  public String getStopId() {
    return stopId;
  }
  public void setStopId(String stopId) {
    this.stopId = stopId;
  }
  public String getRouteId() {
    return routeId;
  }
  public void setRouteId(String routeId) {
    this.routeId = routeId;
  }
  public String getTripId() {
    return tripId;
  }
  public void setTripId(String tripId) {
    this.tripId = tripId;
  }
  public void setFrequency(boolean b) {
    isFrequency = b;
  }
  public boolean isFrequency() {
    return isFrequency;
  }
  public ArrayList<StopTimeInfo> getStopTimeInfos() {
    return stopTimeInfos;
  }
  public void setStopTimeInfos(ArrayList<StopTimeInfo> updates) {
    stopTimeInfos = updates;
  }
}
