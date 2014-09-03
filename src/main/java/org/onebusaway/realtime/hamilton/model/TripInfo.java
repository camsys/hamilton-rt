package org.onebusaway.realtime.hamilton.model;

import java.util.ArrayList;

import org.onebusaway.transit_data_federation.services.blocks.BlockInstance;
import org.onebusaway.transit_data_federation.services.realtime.BlockLocation;
import org.onebusaway.transit_data_federation.services.transit_graph.FrequencyEntry;

public class TripInfo {
  private BlockLocation _blockLocation;
  private BlockInstance _blockInstance;
  private FrequencyEntry _frequencyEntry;
  private String _closestStopId;
  private String _scheduleRelationship;
  private ArrayList<StopTimeInfo> _stopTimeInfos = new ArrayList<StopTimeInfo>();
  private boolean _isFrequency = false;
  
  public String getClosestStopId() {
    return _closestStopId;
  }
  public void setClosestStopId(String _closestStopId) {
    this._closestStopId = _closestStopId;
  }
  public BlockLocation getBlockLocation() {
    return _blockLocation;
  }
  public void setBlockLocation(BlockLocation _blockLocation) {
    this._blockLocation = _blockLocation;
  }
  public BlockInstance getBlockInstance() {
    return _blockInstance;
  }
  public void setBlockInstance(BlockInstance _blockInstance) {
    this._blockInstance = _blockInstance;
  }
  public FrequencyEntry getFrequencyEntry() {
    return _frequencyEntry;
  }
  public void setFrequencyEntry(FrequencyEntry _frequencyEntry) {
    this._frequencyEntry = _frequencyEntry;
  }
  public String getScheduleRelationship() {
    return _scheduleRelationship;
  }
  public void setScheduleRelationship(String relationship) {
    _scheduleRelationship = relationship;
  }
  public void addStopTimeInfos(StopTimeInfo stu) {
    _stopTimeInfos.add(stu);
  }
  public ArrayList<StopTimeInfo> getStopTimeInfos() {
    return _stopTimeInfos;
  }
  public void setFrequency(boolean b) {
    _isFrequency = b;
  }
  public boolean isFrequency() {
    return _isFrequency;
  }
}
