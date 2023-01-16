package com.zcurd.common.task;

import java.util.HashMap;
import java.util.Map;

public class TaskConstant {
  public static final int TASK_STATU1 = 1;
  
  public static final int TASK_STATU2 = 2;
  
  public static final int TASK_TARGET_TYPE1 = 1;
  
  public static final int TASK_TARGET_TYPE2 = 2;
  
  public static final int TASK_TARGET_TYPE3 = 3;
  
  private static Map<Integer, Object> taskMap = new HashMap();
  
  public static Map<Integer, Object> getTaskMap() { return taskMap; }
}
