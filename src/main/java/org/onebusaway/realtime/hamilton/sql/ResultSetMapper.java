package org.onebusaway.realtime.hamilton.sql;

import java.sql.Date;
import java.sql.ResultSet;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

import org.onebusaway.realtime.hamilton.model.DBAVLRecord;

public class ResultSetMapper {
  private static SimpleDateFormat _sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm");
  
  public List<DBAVLRecord> map(ResultSet rs) throws Exception {
    ArrayList<DBAVLRecord> data = new ArrayList<DBAVLRecord>();
    while (rs.next()) {
      DBAVLRecord avl = readRow(rs);
      if (avl != null && avl.isValid()) {
        data.add(avl);
      }
    }
    return data;
  }
  
  private DBAVLRecord readRow(ResultSet rs) throws Exception {
    DBAVLRecord avl = new DBAVLRecord();
    avl.setId(rs.getInt(1));
    avl.setBusId(rs.getInt(2));
    avl.setReportTimeString(rs.getString(3));
    avl.setLat(rs.getDouble(4));
    avl.setLon(rs.getDouble(5));
    avl.setLogonRoute(rs.getString(6));
    String logonTrip = rs.getString(7);
    avl.setLogonTrip(logonTrip);
    avl.setBusNumber(rs.getString(8));
    Date reportDate = new Date(_sdf.parse(rs.getString(9) + " " + rs.getString(3)).getTime());
    avl.setReportDate(reportDate);
    if (logonTrip != null && logonTrip.length() > 3) {
      reportDate = new Date(_sdf.parse(rs.getString(9) + " " + logonTrip.substring(0, 2) + ":" + logonTrip.substring(2,4)).getTime());
      avl.setLogonTripDate(reportDate);
    }
    return avl;
  }

  private long parseTime(String time) {
    if (time != null) {
      int hour = Integer.parseInt(time.substring(0, 2));
      int minute = Integer.parseInt(time.substring(3,5));
      int second = Integer.parseInt(time.substring(6,8));
      return (hour * 60 * 60) + (minute * 60) + second;
    }
    return 0;
  }
  
}
