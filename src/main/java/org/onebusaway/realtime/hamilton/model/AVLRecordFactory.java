package org.onebusaway.realtime.hamilton.model;

public abstract class AVLRecordFactory<T extends AVLRecord> {

  public abstract AVLFieldDefinition<T>[] getFields();

  public abstract T createEmptyRecord();
  
  public T createRecord(byte[] bytes, int start, int end) {
    T record = createEmptyRecord();
    for (AVLFieldDefinition<T> f : getFields()) {
      if (f.setter != null) {
        f.setter.setData(bytes, start, start + f.length);
        f.setter.setField(record);
      }
      start += f.length;
      if (start >= end) {
        break;
      }
    }
    return record;
  }
  
  
}
