package org.onebusaway.realtime.hamilton.model;

public class PositionReport implements AVLRecord {
  
  int sequenceNumber;
  int triggerType;
  int gpsTriggerWeekNumber;
  long gpsTriggerSecondsIntoWeek;
  String operatorId;
  String cellId;
  int ignitionFlag;
  int powerSourceFlag;
  int gpsSatellites;
  double lat;
  double lon;
  int gpsFixWeekNumber;
  long gpsFixSecondsIntoWeek;
  int speed;
  int heading;
  int maxSpeed;
  double distanceTravelled;
  String driverId;
  
  public int getSequenceNumber() {
    return sequenceNumber;
  }
  public void setSequenceNumber(int number) {
    this.sequenceNumber = number;
  }

  public int getTriggerType() {
    return triggerType;
  }
  
  public void setTriggerType(int integer) {
    this.triggerType = integer;
  }

  public int getGpsTriggerWeekNumber() {
    return gpsTriggerWeekNumber;
  }
  
  public void setGpsTriggerWeekNumber(int integer) {
    this.gpsTriggerWeekNumber = integer;
  }

  public long getGpsTriggerSecondsIntoWeek() {
    return gpsTriggerSecondsIntoWeek;
  }
    
  public void setGpsTriggerSecondsIntoWeek(long long1) {
    this.gpsTriggerSecondsIntoWeek = long1;
  }
  
  public String getOperatorId() {
    return operatorId;
  }

  public void setOperatorId(String stringData) {
    this.operatorId = stringData;
  }
  
  public String getCellId() {
    return cellId;
  }

  public void setCellId(String stringData) {
    this.cellId = stringData;
  }

  public int getIgnitionFlag() {
    return ignitionFlag;
  }
  
  public void setIgnitionFlag(int integer) {
    this.ignitionFlag = integer;
  }

  public int getPowerSourceFlag() {
    return powerSourceFlag;
  }
  
  public void setPowerSourceFlag(int integer) {
    this.powerSourceFlag = integer;    
  }

  public int getGpsSatellites() {
    return gpsSatellites;
  }
  
  public void setGpsSatellites(int integer) {
    this.gpsSatellites = integer;
  }

  public double getLat() {
    return lat;
  }
  
  public void setLat(float decimalFixedPoint) {
    this.lat = decimalFixedPoint;
  }
  
  public double getLon() {
    return lon;
  }
  
  public void setLon(float decimalFixedPoint) {
    this.lon = decimalFixedPoint;
  }

  public int getGpsFixWeekNumber() {
    return gpsFixWeekNumber;
  }
  
  public void setGpsFixWeekNumber(int integer) {
    this.gpsFixWeekNumber = integer;
  }

  public long getGpsFixSecondsIntoWeek() {
    return gpsFixSecondsIntoWeek;
  }
  
  public void setGpsFixSecondsIntoWeek(long long1) {
    this.gpsFixSecondsIntoWeek = long1;
  }
  
  public int getSpeed() {
    return speed;
  }

  public void setSpeed(int integer) {
    this.speed = integer;
  }
  
  public int getHeading() {
    return heading;
  }
  
  public void setHeading(int integer) {
    this.heading = integer;
  }
  
  public int getMaxSpeed() {
    return maxSpeed;
  }
  
  public void setMaxSpeed(int integer) {
    this.maxSpeed = integer;
  }

  public double getDistanceTravelled() {
    return distanceTravelled;
  }
  
  public void setDistanceTravelled(float decimalFixedPoint) {
    this.distanceTravelled = decimalFixedPoint;
  }
  
}
