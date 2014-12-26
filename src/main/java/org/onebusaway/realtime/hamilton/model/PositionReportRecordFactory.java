package org.onebusaway.realtime.hamilton.model;

abstract class PositionReportFieldSetter extends AVLRecordFieldSetter<PositionReport> {
}

/*
 * >RTCP1111BQQQQRRRRRRCCCCDDDEEEFFFGGGJKKLMMMMMMNNNNNOPHISSSTTTTTTTUUUUVVVVVVVW
WWWWWXXXXYYYYYYZaaabbbcccdddeffffffffffffg;ID=YYYYYYYY;*ZZ<
1111 Protocol Sequence Number: 16-bit hex value, increments by 1 each report message

B Trigger type:   '0' = Periodic Report
                        '1' = Input Report
                        '2' = Ignition OFF Report
                        '3' = Server Query for Report
                        '4' = Ignition ON Report
                        '5' = Start Report
                        '6' = Stop Report
                        '7' = Speed Threshold Report
                        '8' = Reserved
                        '9' = Reserved
                        'A' = Reserved

QQQQ Trigger Time - GPS Week Number - retrieved from on-board RTC

RRRRRR Trigger Time - GPS Seconds into Week - retrieved from on-board RTC

CCCC States for Digital Inputs 0, 1,2, 3: '0' = Low, '1' = High
If an input is not configured, it is reported as "0" by default.

DDD Power source voltage level. Units of hundreds of mV.
This field indicates the voltage level of the current power source which can be either
the main vehicle power or the backup battery. For the power source indicator, see
field P in this message.

EEE Analog Input 1 voltage level. Units of hundreds of mV.
If the input is not configured, it is reported as "0 mV" by default.

FFF Analog Input 2 voltage level. Units of hundreds of mV.
If the input is not configured, it is reported as "0 mV" by default.

GGG Analog Input 3 voltage level. Units of hundreds of mV.
If the input is not configured, it is reported as "0 mV" by default.

J GSM status: '0' = Network Available.
                        '1' = Message Logged. Configured to not send.
                        '2' = Network Unavailable.
                        '3' = SIM Error / No SIM.
                        '4' = PIN Error.
                        '5' = Error During SW Init.
                        '6' = Error During ME Starting.
                        '7' = Error During SMS or GPRS Send

KK GSM Signal Strength

L GSM Registration info '0' = Registered and Not Roaming, 
                                      '1' = Registered and Roaming,
                                      '2' = Not Registered

MMMMMM Operator ID

NNNN Cell ID

N Reserved for future use

O Ignition: '0' = Off, '1' = On, '2' = Ignition line checking disabled

P Current source of power: '0' = Main power, '1' = Battery power. For the voltage level 
of the current source, see field DDD in this message.

H GPS State: '0' = Fix Obtained, '1' = Zero satellites in view, '2' = One satellite in view, 
and so on.

I GPS Antenna: '0' = Connected, '1' = Open, '2' = Short

SSS.TTTTTTT Latitude in WGS-84 coordinates (positive = north). Units of degrees.

UUUU.VVVVVVV Longitude in WGS-84 coordinates (positive = east). Units of degrees.

WWWWWW Altitude above MSL. Units of feet.

XXXX Fix Time - GPS Week Number

YYYYYY Fix Time - GPS Seconds into Week

Z Type of Fix         '0' = 3D Fix, '1' = 2D Fix

aaa Horizontal Speed. Units of MPH.

bbb Heading based on True North, increasing easterly. Units of degrees.

ccc Maximum horizontal speed since last report. Units of MPH.

ddd.e Distance traveled between START and STOP reports. Units of Miles.

ffffffffffff Driver ID. This is a 12-character iButton serial number which excludes the checksum 
and family code characters. 
Note - Applies only to devices equipped with the Driver ID peripheral. Otherwise, this
field is reserved.

g Reserved for future use.
 */
public class PositionReportRecordFactory extends AVLRecordFactory<PositionReport> {

  static class FieldDef extends AVLFieldDefinition<PositionReport> {
    public FieldDef(int length, String name, AVLRecordFieldSetter<PositionReport> setter) {
      super(length, name, setter);
    }
  }
  
  private static AVLFieldDefinition[] fields = {
    new FieldDef(4, "record type", null),
    new FieldDef(4, "sequence number", new PositionReportFieldSetter() {
      public void setField(PositionReport record) {
        record.setSequenceNumber(getIntegerFromHex());
      }

    }),
    new FieldDef(1, "periodic report", new PositionReportFieldSetter() {
      public void setField(PositionReport record) {
        record.setTriggerType(getInteger());
      }
    }),
    new FieldDef(4, "trigger time week number", new PositionReportFieldSetter() {
      public void setField(PositionReport record) {
        record.setGpsTriggerWeekNumber(getInteger());
      }
    }),
    new FieldDef(6, "trigger time seconds into week", new PositionReportFieldSetter() {
      public void setField(PositionReport record) {
        record.setGpsTriggerSecondsIntoWeek(getLong());
      }
    }),
    new FieldDef(4, "digital input states", null),
    new FieldDef(3, "source voltage level", null),
    new FieldDef(3, "input 1 voltage level", null),
    new FieldDef(3, "input 2 voltage level", null),
    new FieldDef(3, "input 3 voltage level", null),
    new FieldDef(1, "GSM status", null),
    new FieldDef(2, "GSM signal strength", null),
    new FieldDef(1, "GSM registration info", null),
    new FieldDef(6, "operator id", new PositionReportFieldSetter() {
      public void setField(PositionReport record) {
        record.setOperatorId(getStringData());
      }
    }),
    new FieldDef(4, "cell id", new PositionReportFieldSetter() {
      public void setField(PositionReport record) {
        record.setCellId(getStringData());
      }
    }),
    new FieldDef(1, "reserved", null),
    new FieldDef(1, "ignition", new PositionReportFieldSetter() {
      public void setField(PositionReport record) {
        record.setIgnitionFlag(getInteger());
      }
    }),
    new FieldDef(1, "power source", new PositionReportFieldSetter() {
      public void setField(PositionReport record) {
        record.setPowerSourceFlag(getInteger());
      }
    }),
    new FieldDef(1, "GPS state", new PositionReportFieldSetter() {
      public void setField(PositionReport record) {
        record.setGpsSatellites(getInteger());
      }
    }),
    new FieldDef(1, "GPS antenna", null),
    new FieldDef(10, "lat", new PositionReportFieldSetter() {
      public void setField(PositionReport record) {
        record.setLat(getDecimalFixedPoint(3));
      }
    }),
    new FieldDef(11, "lon", new PositionReportFieldSetter() {
      public void setField(PositionReport record) {
        record.setLon(getDecimalFixedPoint(4));
      }
    }),
    new FieldDef(6, "altitude", null),
    new FieldDef(4, "Fix GPS week number", new PositionReportFieldSetter() {
      public void setField(PositionReport record) {
        record.setGpsFixWeekNumber(getInteger());
      }
    }),
    new FieldDef(6, "Fix GPS seconds into week", new PositionReportFieldSetter() {
      public void setField(PositionReport record) {
        record.setGpsFixSecondsIntoWeek(getLong());
      }
    }),
    new FieldDef(1, "Fix type", null),
    new FieldDef(3, "speed", new PositionReportFieldSetter() {
      public void setField(PositionReport record) {
        record.setSpeed(getInteger());
      }
    }),
    new FieldDef(3, "heading", new PositionReportFieldSetter() {
      public void setField(PositionReport record) {
        record.setHeading(getInteger());
      }
    }),
    new FieldDef(3, "max speed", new PositionReportFieldSetter() {
      public void setField(PositionReport record) {
        record.setMaxSpeed(getInteger());
      }
    }),
    new FieldDef(3, "distance travelled", new PositionReportFieldSetter() {
      public void setField(PositionReport record) {
        record.setDistanceTravelled(getDecimalFixedPoint(3));
      }
    }),
    new FieldDef(1, "reserved", null)
  };
  
  @Override
  public AVLFieldDefinition<PositionReport>[] getFields() {
    return fields;
  }

  @Override
  public PositionReport createEmptyRecord() {
    return new PositionReport();
  }

  
}
