package org.onebusaway.realtime.hamilton.model;

public class WayfarerLogon implements IRecord {
  private String command;
  private String driver;
  private String module;
  private String running;
  private String duty;
  private String service;
  private String journey;
  private String depot;
  private boolean inbound;
  private String departureTimeString;
  private String states;

  public String getCommand() {
    return command;
  }
  public void setCommand(String bcd) {
    this.command = bcd;
  }
  public String getDriver() {
    return driver;
  }
  public void setDriver(String driver) {
    this.driver = driver;
  }
  public String getModule() {
    return module;
  }
  public void setModule(String module) {
    this.module = module;
  }
  public String getRunning() {
    return running;
  }
  public void setRunning(String running) {
    this.running = running;
  }
  public String getDuty() {
    return duty;
  }
  public void setDuty(String duty) {
    this.duty = duty;
  }
  public String getService() {
    return service;
  }
  public void setService(String service) {
    this.service = service;
  }
  public String getJourney() {
    return journey;
  }
  public void setJourney(String journey) {
    this.journey = journey;
  }
  public String getDepot() {
    return depot;
  }
  public void setDepot(String depot) {
    this.depot = depot;
  }
  public boolean isInbound() {
    return inbound;
  }
  public void setInbound(boolean inbound) {
    this.inbound = inbound;
  }
  public String getDepartureTimeString() {
    return departureTimeString;
  }
  public void setDepartureTimeString(String departureTimeString) {
    this.departureTimeString = departureTimeString;
  }
  public String getStates() {
    return states;
  }
  public void setStates(String states) {
    this.states = states;
  }

}
