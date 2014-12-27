package org.onebusaway.realtime.hamilton.model;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class IRecordFieldSetter<T extends IRecord> {
  
  private static final Logger _log = LoggerFactory.getLogger(IRecordFieldSetter.class);
  private boolean TRIM_LEFT_PADDING = true;
  
  protected byte[] bytes;
  protected int start;
  protected int end;
  public void setData(byte[] bytes, int start, int end) {
    this.bytes = bytes;
    this.start = start;
    this.end = end;
  }
  public abstract void setField(T record);
  
  public String getStringDataUppercased() {
    return getStringData().toUpperCase();
  }
  
  public String getStringData() {
    if (start > bytes.length) {
      return "";
    }
    if (end > bytes.length) {
      return new String(bytes, start, bytes.length - start).trim();
    }
    return new String(bytes, start, end - start).trim();
  }

  public int getInteger() {
    return Integer.parseInt(getStringData());
  }
  
  public int getIntegerFromHex() {
    return Integer.parseInt(getStringData(), 16);
  }

  public long getLong() {
    return Long.parseLong(getStringData());
  }

  public int getIntegerSafe() {
    String data = getStringData();
    try {
      return Integer.parseInt(data);
    } catch (NumberFormatException e) {
      return -1;
    }
  }
  
  public float getDecimalFixedPoint(int digits) {
    while(bytes[start] == ' ') {
      start += 1;
    }
    if (bytes[start] == '-') {
      digits += 1;
    }
    String beforePoint = new String(bytes, start, digits).trim();
    String afterPoint = new String(bytes, start + digits, (end - start) - digits).trim();
    return Float.parseFloat(beforePoint + "." + afterPoint);
  }

  public boolean getBoolean() {
    while(bytes[start] == ' ') {
      start++;
    }
    return bytes[start] == 'Y';
  }

  /**
   * parse bytes as Binary-coded decimal.
   * @return
   */
  public String getBcd() {
    if (start > bytes.length) {
      return "";
    }
    if (end > bytes.length) {
      return bcdToString(bytes, start, bytes.length - start);
    }
    return bcdToString(bytes, start, end - start);
  }
  
  public String bcdToString(byte[] buff, int start, int length) {
    StringBuffer sb = new StringBuffer();
    
    for (int i = start; i < start+length; i++) {
      String s = bcdToString(buff[i]);
//      _log.info(s);
      sb.append(s);
    }
//    _log.info(start + "->" + length + "="+sb.toString());
    return trimLeftPadding(sb.toString());
  }

  private String trimLeftPadding(String string) {
    if (this.TRIM_LEFT_PADDING) {
      StringBuffer trimmed = new StringBuffer();
      boolean foundNonZero = false;
      for (int i = 0; i < string.length(); i++) {
        if (! foundNonZero && string.charAt(i) == '0') {
          continue;
        }
        foundNonZero = true;
        trimmed.append(string.charAt(i));
      }
      return trimmed.toString();
    }
    return string;
  }

  public String bcdToString(byte bcd) {
    StringBuffer sb = new StringBuffer();
    byte high = (byte) (bcd & 0xf0);
    high >>>= (byte) 4; 
    high = (byte) (high & 0x0f);
    byte low = (byte) (bcd & 0x0f);
    sb.append(high);
    sb.append(low);
    return sb.toString();
  }


}
