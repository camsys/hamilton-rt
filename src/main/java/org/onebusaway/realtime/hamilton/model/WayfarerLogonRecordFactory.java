package org.onebusaway.realtime.hamilton.model;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

abstract class WayfarerFieldSetter extends AVLRecordFieldSetter<WayfarerLogon> {
}

public class WayfarerLogonRecordFactory extends AVLRecordFactory<WayfarerLogon> {
  private static final Logger _log = LoggerFactory.getLogger(WayfarerLogonRecordFactory.class);
  static class FieldDef extends AVLFieldDefinition<WayfarerLogon> {
    public FieldDef(int lenght, String name, AVLRecordFieldSetter<WayfarerLogon> setter) {
      super (lenght, name, setter);
    }
  }

  private static AVLFieldDefinition[] fields = {
    new FieldDef(1, "stx", new WayfarerFieldSetter() {
      public void setField(WayfarerLogon record) {
        _log.info("stx=" + getBcd());
      }
    }),
    new FieldDef(1, "device address", new WayfarerFieldSetter() {
      public void setField(WayfarerLogon record) {
        _log.info("device=" + getBcd());
      }
    }),
    new FieldDef(1, "length", new WayfarerFieldSetter() {
      public void setField(WayfarerLogon record) {
        _log.info("length=" + new Integer(bytes[start]));
      }
    }),
    new FieldDef(1, "cmd", new WayfarerFieldSetter() {
      public void setField(WayfarerLogon record) {
        record.setCommand(getBcd());
      }
    }),
    new FieldDef(4, "driver", new WayfarerFieldSetter() {
      public void setField(WayfarerLogon record) {
        record.setDriver(getBcd());
      }
    }),
    new FieldDef(3, "module", new WayfarerFieldSetter() {
      public void setField(WayfarerLogon record) {
        record.setModule(getBcd());
      }
    }),
    new FieldDef(3, "running", new WayfarerFieldSetter() {
      public void setField(WayfarerLogon record) {
        record.setRunning(getBcd());
      }
    }),
    new FieldDef(3, "duty", new WayfarerFieldSetter() {
      public void setField(WayfarerLogon record) {
        record.setDuty(getBcd());
      }
    }),
    new FieldDef(4, "service", new WayfarerFieldSetter() {
      public void setField(WayfarerLogon record) {
        record.setService(getStringData());
      }
    }),
    new FieldDef(2, "journey", new WayfarerFieldSetter() {
      public void setField(WayfarerLogon record) {
        record.setJourney(getBcd());
      }
    }),
    new FieldDef(1, "inbound", new WayfarerFieldSetter() {
      public void setField(WayfarerLogon record) {
        record.setInbound((byte)1 == bytes[start]); 
      }
    }),
    new FieldDef(1, "depot", new WayfarerFieldSetter() {
      public void setField(WayfarerLogon record) {
        record.setDepot(getBcd());
      }
    }),
    new FieldDef(2, "time", new WayfarerFieldSetter() {
      public void setField(WayfarerLogon record) {
        String hour = bcdToString(bytes, start, 1);
        String minute = bcdToString(bytes, start+1, 1);
        record.setDepartureTimeString(hour + ":" + minute);
      }
    }),
    new FieldDef(1, "states", new WayfarerFieldSetter() {
      public void setField(WayfarerLogon record) {
        record.setStates("" + new Integer(bytes[start]));
      }
    })
  };
  
  @Override
  public AVLFieldDefinition<WayfarerLogon>[] getFields() {
    return fields;
  }
  
  @Override
  public WayfarerLogon createEmptyRecord() {
    return new WayfarerLogon();
  }
}
