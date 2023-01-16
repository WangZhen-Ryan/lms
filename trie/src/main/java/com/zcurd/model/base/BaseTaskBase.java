package com.zcurd.model.base;

import com.jfinal.plugin.activerecord.IBean;
import com.jfinal.plugin.activerecord.Model;
import java.util.Date;

public abstract class BaseTaskBase<M extends BaseTaskBase<M>> extends Model<M> implements IBean {
  public M setId(Integer id) {
    set("id", id);
    return (M)this;
  }
  
  public Integer getId() { return (Integer)get("id"); }
  
  public M setName(String name) {
    set("name", name);
    return (M)this;
  }
  
  public String getName() { return (String)get("name"); }
  
  public M setTargetType(Integer targetType) {
    set("target_type", targetType);
    return (M)this;
  }
  
  public Integer getTargetType() { return (Integer)get("target_type"); }
  
  public M setTargetValue(String targetValue) {
    set("target_value", targetValue);
    return (M)this;
  }
  
  public String getTargetValue() { return (String)get("target_value"); }
  
  public M setCron(String cron) {
    set("cron", cron);
    return (M)this;
  }
  
  public String getCron() { return (String)get("cron"); }
  
  public M setCreateUserId(Integer createUserId) {
    set("create_user_id", createUserId);
    return (M)this;
  }
  
  public Integer getCreateUserId() { return (Integer)get("create_user_id"); }
  
  public M setLastRunResult(String lastRunResult) {
    set("last_run_result", lastRunResult);
    return (M)this;
  }
  
  public String getLastRunResult() { return (String)get("last_run_result"); }
  
  public M setLastRunTime(Date lastRunTime) {
    set("last_run_time", lastRunTime);
    return (M)this;
  }
  
  public Date getLastRunTime() { return (Date)get("last_run_time"); }
  
  public M setLastRunTimeCost(Integer lastRunTimeCost) {
    set("last_run_time_cost", lastRunTimeCost);
    return (M)this;
  }
  
  public Integer getLastRunTimeCost() { return (Integer)get("last_run_time_cost"); }
  
  public M setStatus(Integer status) {
    set("status", status);
    return (M)this;
  }
  
  public Integer getStatus() { return (Integer)get("status"); }
  
  public M setCreateTime(Date createTime) {
    set("create_time", createTime);
    return (M)this;
  }
  
  public Date getCreateTime() { return (Date)get("create_time"); }
}
