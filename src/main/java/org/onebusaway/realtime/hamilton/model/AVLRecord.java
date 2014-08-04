package org.onebusaway.realtime.hamilton.model;

import java.sql.Date;

public class AVLRecord {

  private int id;
  private int busId;
  private Date reportTime;
  private double lat;
  private double lon;
  private String logonRoute;
  private String logonTrip;
  private String busNumber;
  private Date reportDate;
  
  public int getId() {
    return id;
  }
  public void setId(int id) {
    this.id = id;
  }
  public int getBusId() {
    return busId;
  }
  public void setBusId(int busId) {
    this.busId = busId;
  }
  public Date getReportTime() {
    return reportTime;
  }
  public void setReportTime(Date reportTime) {
    this.reportTime = reportTime;
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
  public String getLogonRoute() {
    return logonRoute;
  }
  public void setLogonRoute(String logonRoute) {
    this.logonRoute = logonRoute;
  }
  public String getLogonTrip() {
    return logonTrip;
  }
  public void setLogonTrip(String logonTrip) {
    this.logonTrip = logonTrip;
  }
  public String getBusNumber() {
    return busNumber;
  }
  public void setBusNumber(String busNumber) {
    this.busNumber = busNumber;
  }
  public Date getReportDate() {
    return reportDate;
  }
  public void setReportDate(Date reportDate) {
    this.reportDate = reportDate;
  }
  
  public String toString() {
    return "AVL(" + getId() + ", "
        + getBusId() + ", "
        + getReportTime() + ", "
        + getLat() + ", "
        + getLon() + ", "
        + getLogonRoute() + ", "
        + getLogonTrip() + ", "
        + getBusNumber() + ", "
        + getReportDate() 
        + ")";
        
  }

}
