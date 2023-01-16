package com.zcurd.model;

import com.zcurd.model.base.BaseTaskLog;

public class TaskLog extends BaseTaskLog<TaskLog> {
  public static final TaskLog dao = (TaskLog)(new TaskLog()).dao();
}
