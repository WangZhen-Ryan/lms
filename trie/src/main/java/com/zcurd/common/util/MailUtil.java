package com.zcurd.common.util;

import com.jfinal.kit.LogKit;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Record;
import com.zcurd.ext.mail.SendMail;
import com.zcurd.model.SysDict;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.commons.mail.EmailException;
import org.apache.commons.mail.HtmlEmail;

public class MailUtil {
	
	public static final Map<String, String> keyMap = new HashMap<String, String>();

	private static final String sql = "SELECT c.mail,c.display_name from sys_role a,sys_user_role b,sys_user c  where a.notify_type = '1' and c.id = b.id and FIND_IN_SET(a.id,b.roles)";

	
	public static void sendMail(Map<String, String> map, String toAddress, String subject, String content)
			throws EmailException {
		HtmlEmail email = new HtmlEmail();
		email.setHostName((String) map.get("host"));
		email.setAuthentication((String) map.get("username"), (String) map.get("password"));
		email.setCharset("utf-8");
		try {
			email.setFrom((String) map.get("from"));
			email.addTo(toAddress, toAddress);
			email.setSubject(subject);
			email.setHtmlMsg(content);
			email.send();
		} catch (Exception e) {
			LogKit.error("发送邮件失败", e);
			throw e;
		}
	}


	public static void notifyMail(String subject, String text) {
		List<SysDict> find = SysDict.me.find("select * from sys_dict where dict_type='邮箱设置'");
		Map<String, String> map = new HashMap<String, String>();
		for (SysDict sysDict : find) {
			String key = sysDict.getStr("dict_key");
			String value = sysDict.getStr("dict_value");
			map.put((String) keyMap.get(key), value);
		}
		List<Record> list = Db.find(
				"SELECT c.mail,c.display_name from sys_role a,sys_user_role b,sys_user c  where a.notify_type = '1' and c.id = b.id and FIND_IN_SET(a.id,b.roles)");
		List<Map<String, String>> l = new ArrayList<Map<String, String>>();
		for (Record record : list) {
			Map<String, String> tomap = new HashMap<String, String>();
			tomap.put("address", record.getStr("mail"));
			tomap.put("personal", record.getStr("display_name"));
			l.add(tomap);
		}
		SendMail sendMail = new SendMail(map);
		sendMail.sendMail(subject, text, l);
	}
}
