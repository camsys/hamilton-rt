package org.onebusaway.realtime.hamilton.model;

import java.sql.Date;

public class DBAVLRecord {

  private int id;
  private int busId;
  private String reportTimeString;
  private double lat;
  private double lon;
  private String logonRoute;
  private String logonTrip;
  private Date logonTripDate;
  private String busNumber;
  private String reportDateString;
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
  public String getReportTimeString() {
    return reportTimeString;
  }
  public void setReportTimeString(String reportTime) {
    this.reportTimeString = reportTime;
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
  
  public String getReportDateString() {
    return reportDateString;
  }
  
  public void setReportDateString(String reportDateString) {
    this.reportDateString = reportDateString;
  }
  
  public Date getReportDate() {
    return reportDate;
  }
  public void setReportDate(Date reportDate) {
    this.reportDate = reportDate;
  }

  public Date getLogonTripDate() {
    return logonTripDate;
  }
  public void setLogonTripDate(Date logonTripDate) {
    this.logonTripDate = logonTripDate;
  }
  
  public String toString() {
    return "AVL(" + getId() + ", "
        + getBusId() + ", "
        + getReportTimeString() + ", "
        + getLat() + ", "
        + getLon() + ", "
        + getLogonRoute() + ", "
        + getLogonTrip() + ", "
        + getBusNumber() + ", "
        + getReportDate() 
        + ")";
        
  }

}
