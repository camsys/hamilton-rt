package org.onebusaway.realtime.hamilton.model;

import java.sql.Date;
import java.util.Calendar;
import java.util.TimeZone;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AVLRecord {

  private static final Logger _log = LoggerFactory.getLogger(AVLRecord.class);
  
  private int id;
  private int busId;
  private long reportTime;
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
  public long getReportTime() {
    return reportTime;
  }
  public void setReportTime(long reportTime) {
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
  public boolean isValid() {
    return
        !"--".equals(getLogonTrip())
        && isRecent(""+getId(), getReportTime()); 
  }
  private boolean isRecent(String id, long time) {
    long now = (System.currentTimeMillis() - startOfDay())/1000; 
//    _log.error("id= " + id + ", now=" + now + " ?= " + time);
    return Math.abs(now - time) < 1000; 
  }

  private long startOfDay() {
    Calendar cal = Calendar.getInstance();
    cal.set(Calendar.HOUR, 0);
    cal.set(Calendar.MINUTE, 0);
    cal.set(Calendar.SECOND, 0);
    cal.set(Calendar.MILLISECOND, 0);
    cal.set(Calendar.AM_PM, Calendar.AM);
    cal.setTimeZone(TimeZone.getTimeZone("Pacific/Auckland"));
    long startOfDay = cal.getTimeInMillis();
    return startOfDay;
  }
}
