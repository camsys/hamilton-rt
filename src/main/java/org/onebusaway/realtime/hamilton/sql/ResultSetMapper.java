package org.onebusaway.realtime.hamilton.sql;

import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

import org.onebusaway.realtime.hamilton.model.AVLRecord;

public class ResultSetMapper {

  
  public List<AVLRecord> map(ResultSet rs) throws Exception {
    ArrayList<AVLRecord> data = new ArrayList<AVLRecord>();
    while (rs.next()) {
      AVLRecord avl = readRow(rs);
      if (avl != null) {
        data.add(avl);
      }
    }
    return data;
  }
  
  private AVLRecord readRow(ResultSet rs) throws Exception {
    AVLRecord avl = new AVLRecord();
    avl.setId(rs.getInt(1));
    avl.setBusId(rs.getInt(2));
    avl.setReportTime(rs.getDate(3));
    avl.setLat(rs.getDouble(4));
    avl.setLon(rs.getDouble(5));
    avl.setLogonRoute(rs.getString(6));
    avl.setLogonTrip(rs.getString(7));
    avl.setBusNumber(rs.getString(8));
    avl.setReportDate(rs.getDate(9));
    return avl;
  }
  
}
