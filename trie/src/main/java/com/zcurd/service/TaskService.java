package com.zcurd.service;

import com.jfinal.log.Log;
import com.jfinal.plugin.cron4j.Cron4jPlugin;
import com.jfinal.plugin.cron4j.ITask;
import com.zcurd.common.ErrorMsgException;
import com.zcurd.common.task.TaskConstant;
import com.zcurd.common.task.ZcurdTask;
import com.zcurd.model.TaskBase;
import java.util.Date;
import java.util.List;

public class TaskService {
  private static final Log logger = Log.getLog(TaskService.class);
  
  public void startAll() {
    logger.info(new Date() + "开始启动定时任务");
    List<TaskBase> taskList = TaskBase.me.find("select * from task_base");
    int count = 0;
    for (TaskBase taskBase : taskList) {
      Cron4jPlugin cp = new Cron4jPlugin();
      cp.addTask(taskBase.getCron(), createTask(taskBase));
      TaskConstant.getTaskMap().put(taskBase.getId(), cp);
      if (taskBase.getStatus().intValue() == 1) {
        cp.start();
        count++;
      } 
    } 
    logger.info(new Date() + "完成启动定时任务\t已启动" + count + "个任务");
  }
  
  public void startOrStop(int taskId, int status) {
    ((TaskBase)((TaskBase)TaskBase.me.findById(Integer.valueOf(taskId))).setStatus(Integer.valueOf(status))).update();
    Cron4jPlugin cp = (Cron4jPlugin)TaskConstant.getTaskMap().get(Integer.valueOf(taskId));
    if (status == 1) {
      cp.start();
    } else if (status == 2) {
      cp.stop();
    } 
  }
  
  public void add(TaskBase task) {
    task.setStatus(Integer.valueOf(2));
    task.save();
    Cron4jPlugin cp = new Cron4jPlugin();
    cp.addTask(task.getCron(), createTask(task));
    TaskConstant.getTaskMap().put(task.getId(), cp);
  }
  
  public void update(TaskBase taskBase) {
    Cron4jPlugin cp = (Cron4jPlugin)TaskConstant.getTaskMap().get(taskBase.getId());
    try {
      if (taskBase.getStatus().intValue() == 1)
        cp.stop(); 
    } catch (Exception e) {
      logger.error("任务停止失败", e);
      throw new ErrorMsgException("任务停止失败，" + e.getMessage());
    } 
    cp = new Cron4jPlugin();
    cp.addTask(taskBase.getCron(), createTask(taskBase));
    try {
      if (taskBase.getStatus().intValue() == 1)
        cp.start(); 
    } catch (Exception e) {
      logger.error("任务启动失", e);
      throw new ErrorMsgException("任务启动失败，" + e.getMessage());
    } 
    taskBase.update();
    TaskConstant.getTaskMap().put(taskBase.getId(), cp);
  }
  
  public void delete(Integer id) {
    Cron4jPlugin cp = (Cron4jPlugin)TaskConstant.getTaskMap().get(id);
    TaskBase task = (TaskBase)TaskBase.me.findById(id);
    try {
      if (task.getStatus().intValue() == 1)
        cp.stop(); 
      cp = null;
      TaskConstant.getTaskMap().remove(id);
      task.delete();
    } catch (Exception e) {
      logger.error("任务停止失败", e);
      throw new ErrorMsgException("任务停止失败，" + e.getMessage());
    } finally {
      cp = null;
      TaskConstant.getTaskMap().remove(id);
    } 
  }
  
  public void runAtSoon(TaskBase taskBase) { createTask(taskBase).run(); }
  
  private ITask createTask(TaskBase taskBase) { return new ZcurdTask(taskBase.getId().intValue(), taskBase.getTargetType().intValue(), taskBase.getTargetValue()); }
}
