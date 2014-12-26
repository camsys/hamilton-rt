package org.onebusaway.realtime.hamilton.services;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.Collection;
import java.util.List;

import org.onebusaway.realtime.hamilton.model.AVLRecord;
import org.onebusaway.realtime.hamilton.model.VehicleMessage;
import org.onebusaway.realtime.hamilton.model.VehicleRecord;

public interface VehicleUpdateService {

  void receiveTCIP(byte[] buff);
  List<VehicleMessage> getRecentMessages();
  AVLRecord receiveWayfarerLogOnOff(byte[] byteArray);
  AVLRecord recieveGPSUpdate(byte[] byteArray);
  boolean dispatch(InputStream inputStream) throws Exception;
  List<VehicleRecord> getRecentVehicleRecords();
}
