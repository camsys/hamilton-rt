package org.onebusaway.realtime.hamilton.model;

public class IFieldDefinition<T extends IRecord> {
  int length;
  String name;
  IRecordFieldSetter<T> setter;
  
  public IFieldDefinition(int length, String name, IRecordFieldSetter<T> setter) {
    this.length = length;
    this.name = name;
    this.setter = setter;
  }
}
