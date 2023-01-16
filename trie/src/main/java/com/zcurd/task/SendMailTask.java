package com.zcurd.task;

import com.jfinal.kit.LogKit;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Record;
import com.jfinal.plugin.cron4j.ITask;
import com.zcurd.common.util.Check;
import com.zcurd.common.util.MailUtil;
import com.zcurd.model.SysDict;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.commons.mail.EmailException;

public class SendMailTask implements ITask {

	Map<String, String> keyMap = new HashMap<String, String>() {
		private static final long serialVersionUID = 7517466294239768249L;
	};

	public void run() {
		List<SysDict> find = SysDict.me.find("select * from sys_dict where dict_type='邮箱设置'");
		Map<String, String> map = new HashMap<String, String>();
		for (SysDict sysDict : find) {
			String key = sysDict.getStr("dict_key");
			String value = sysDict.getStr("dict_value");
			map.put((String) this.keyMap.get(key), value);
		}
		String materialSql = "select a.*,b.mail,b.id as uid,c.dict_value from material a,sys_user b,sys_dict c where a.out_till<=date(now()) and a.`User_id`= b.id and c.dict_type='material_name' and c.dict_key = a.Type";
		String instrumentSql = "select a.*,b.mail,b.id as uid from instrument a,sys_user b where a.outtill<=date(now()) and a.`user_id`= b.id ";
		String pcSql = "select a.*,b.mail,b.id as uid from pc a,sys_user b where a.out_till<=date(now()) and a.`user_id` = b.id ";
		List<Record> materialList = Db.find(materialSql);
		List<Record> instrumentList = Db.find(instrumentSql);
		List<Record> pcList = Db.find(pcSql);
		Record mailLogRecord = null;
		if (!materialList.isEmpty())
			for (Record record : materialList) {
				String content = "您当前有物料" + record.getStr("dict_value") + "需要归还！";
				String subject = "归还物料提醒邮件";
				String mailAddr = record.getStr("mail");
				mailLogRecord = (new Record()).set("uid", record.get("uid")).set("mail_content", content)
						.set("mail_subject", subject).set("type", "0");
				if (!Check.IsStringNULL(mailAddr))
					senMailUtil(map, mailLogRecord, content, subject, mailAddr);
			}
		if (instrumentList != null)
			for (Record record : instrumentList) {
				String content = "您当前有仪表需要归还！";
				String subject = "归还仪表提醒邮件";
				String mailAddr = record.getStr("mail");
				mailLogRecord = (new Record()).set("uid", record.get("uid")).set("mail_content", content)
						.set("mail_subject", subject).set("type", "0");
				if (!Check.IsStringNULL(mailAddr))
					senMailUtil(map, mailLogRecord, content, subject, mailAddr);
			}
		if (pcList != null)
			for (Record record : pcList) {
				String content = "您当前有PC需要归还！";
				String subject = "归还PC提醒邮件";
				String mailAddr = record.getStr("mail");
				mailLogRecord = (new Record()).set("uid", record.get("uid")).set("mail_content", content)
						.set("mail_subject", subject).set("type", "0");
				if (!Check.IsStringNULL(mailAddr))
					senMailUtil(map, mailLogRecord, content, subject, mailAddr);
			}
	}

	private void senMailUtil(Map<String, String> map, Record mailLogRecord, String content, String subject,
			String mailAddr) {
		boolean flag = false;
		try {
			Db.save("mail_log", mailLogRecord);
			MailUtil.sendMail(map, mailAddr, content, subject);
			flag = true;
			if (mailLogRecord != null && mailLogRecord.getLong("id") != null)
				Db.update("update mail_log set type='1' where id = " + mailLogRecord.getLong("id"));
		} catch (EmailException e) {
			LogKit.error("发送邮件失败", e);
			if (mailLogRecord != null && mailLogRecord.getLong("id") != null)
				Db.update("update mail_log set type='2' where id = " + mailLogRecord.getLong("id"));
		} catch (Exception e) {
			LogKit.error("发送邮件前后，更新数据库失败", e);
		} finally {
			if (!flag)
				try {
					MailUtil.sendMail(map, mailAddr, content, subject);
				} catch (EmailException e) {
					LogKit.error("发送邮件失败", e);
				}
		}
	}

	public void stop() {
	}
}
