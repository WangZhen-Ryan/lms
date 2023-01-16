package com.zcurd.common.task;

import com.jfinal.plugin.cron4j.ITask;

public class DemoTask implements ITask {
  public void run() { System.out.println("demo run!"); }
  
  public void stop() { System.out.println("demo stop!"); }
}
