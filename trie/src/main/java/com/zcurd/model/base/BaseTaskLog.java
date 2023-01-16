package com.zcurd.model.base;

import com.jfinal.plugin.activerecord.IBean;
import com.jfinal.plugin.activerecord.Model;
import java.util.Date;

public abstract class BaseTaskLog<M extends BaseTaskLog<M>> extends Model<M> implements IBean {
  public M setId(Integer id) {
    set("id", id);
    return (M)this;
  }
  
  public Integer getId() { return (Integer)get("id"); }
  
  public M setTaskId(Integer taskId) {
    set("task_id", taskId);
    return (M)this;
  }
  
  public Integer getTaskId() { return (Integer)get("task_id"); }
  
  public M setResult(String result) {
    set("result", result);
    return (M)this;
  }
  
  public String getResult() { return (String)get("result"); }
  
  public M setStartTime(Date startTime) {
    set("start_time", startTime);
    return (M)this;
  }
  
  public Date getStartTime() { return (Date)get("start_time"); }
  
  public M setEndTime(Date endTime) {
    set("end_time", endTime);
    return (M)this;
  }
  
  public Date getEndTime() { return (Date)get("end_time"); }
  
  public M setCostTime(Integer costTime) {
    set("cost_time", costTime);
    return (M)this;
  }
  
  public Integer getCostTime() { return (Integer)get("cost_time"); }
  
  public M setRemark(String remark) {
    set("remark", remark);
    return (M)this;
  }
  
  public String getRemark() { return (String)get("remark"); }
}
