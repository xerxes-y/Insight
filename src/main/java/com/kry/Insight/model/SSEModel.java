package com.kry.Insight.model;

import java.util.ArrayList;

public class SSEModel {
  private String event;
  private String data;
  private String id;
  private Long retry;

  public SSEModel(String event, String data, String id, Long retry) {
    this.event = event;
    this.data = data;
    this.id = id;
    this.retry = retry;
  }

  @Override
  public String toString() {
    var builder  = new ArrayList<String>();
    if(event!=null) builder.add("event : ".concat(event));
    if(data!=null) builder.add("data :".concat(data));
    if (id!=null) builder.add("id : ".concat(id));
    if (retry!=null)builder.add("retry : ".concat(retry.toString()));
    return String.join("\n", builder);
  }
}
