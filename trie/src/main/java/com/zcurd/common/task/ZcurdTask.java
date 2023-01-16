package com.zcurd.common.task;

import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.cron4j.ITask;
import com.zcurd.common.util.StringUtil;
import com.zcurd.model.TaskBase;
import com.zcurd.model.TaskLog;
import java.util.Date;

public class ZcurdTask implements ITask {
	private int id;

	private int taskTargetType;

	private String taskTargetValue;

	public ZcurdTask(int taskTargetType, String taskTargetValue) {
		this.taskTargetType = taskTargetType;
		this.taskTargetValue = taskTargetValue;
	}

	public ZcurdTask(int id, int taskTargetType, String taskTargetValue) {
		this.id = id;
		this.taskTargetType = taskTargetType;
		this.taskTargetValue = taskTargetValue;
	}

	public void run() {
		TaskLog log = new TaskLog();
		Date startDate = new Date();
		((TaskLog) ((TaskLog) log.setTaskId(this.id)).setStartTime(startDate)).save();
		String result = "成功";

		int costTime;
		try {
			String[] var7;
			int var6 = (var7 = this.taskTargetValue.trim().split(";")).length;

			for (costTime = 0; costTime < var6; ++costTime) {
				String value = var7[costTime];
				if (!StringUtil.isEmpty(value) && this.taskTargetType != 1) {
					if (this.taskTargetType == 2) {
						Db.find(value);
					} else if (this.taskTargetType == 3) {
						ITask task = (ITask) Class.forName(value).newInstance();
						task.run();
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
			result = "失败";
			log.setRemark(e.getMessage());
		}
		Date endDate = new Date();
		costTime = (int) (endDate.getTime() - startDate.getTime());
		log.setResult(result);
		log.setEndTime(endDate);
		log.setCostTime(Integer.valueOf(costTime));
		log.update();
		if (this.id > 0)
			((TaskBase) ((TaskBase) ((TaskBase) ((TaskBase) TaskBase.me.findById(Integer.valueOf(this.id)))
					.setLastRunResult(result)).setLastRunTime(startDate)).setLastRunTimeCost(Integer.valueOf(costTime)))
							.update();
		System.out.println(new Date() + "定时任务执行完成");
	}

	public void stop() {
		System.out.println("top");
	}
}
