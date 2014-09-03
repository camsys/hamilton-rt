package org.onebusaway.realtime.hamilton.model;

public class StopTimeInfo {
  private int _stopSequence;
  private long _arrivalTime;
  private String _stopId;
  
  public StopTimeInfo(int stopSequence, long arrivalTime, String stopId) {
    _stopSequence = stopSequence;
    _arrivalTime = arrivalTime;
    _stopId = stopId;
  }

  public int getStopSequence() {
    return _stopSequence;
  }
  
  public long getArrivalTimeInSeconds() {
    return _arrivalTime;
  }
  
  public String getStopId() {
    return _stopId;
  }
}
