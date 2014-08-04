package org.onebusaway.realtime.hamilton.model;

import org.onebusaway.transit_data_federation.services.blocks.BlockInstance;
import org.onebusaway.transit_data_federation.services.realtime.BlockLocation;
import org.onebusaway.transit_data_federation.services.transit_graph.FrequencyEntry;

public class TripInfo {
  private BlockLocation _blockLocation;
  private BlockInstance _blockInstance;
  private FrequencyEntry _frequencyEntry;
  private String _closestStopId;
  
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
}
