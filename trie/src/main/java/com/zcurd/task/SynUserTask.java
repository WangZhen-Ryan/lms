package com.zcurd.task;

import com.jfinal.aop.Duang;
import com.jfinal.kit.LogKit;
import com.jfinal.kit.PropKit;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.IAtom;
import com.jfinal.plugin.activerecord.Record;
import com.jfinal.plugin.cron4j.ITask;
import com.zcurd.ldap.LdapPasswordUtil;
import com.zcurd.ldap.LdapServerinfoEntity;
import com.zcurd.ldap.LdapService;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import javax.naming.NamingEnumeration;
import javax.naming.directory.Attribute;
import javax.naming.directory.SearchResult;

public class SynUserTask implements ITask {
	private void sysLadpUserToDb() {
		List<Record> baseUserList = Db.find("select * from sys_user where id <> '6ae3f56fc43c5420417121954607de52'");
		List<String> userNameListDB = new ArrayList<String>();
		List<String> userNameListLDAP = new ArrayList<String>();
		List<Record> baseUserListForSync = new ArrayList<Record>();
		LdapService ldapService = (LdapService) Duang.duang(LdapService.class);
		LdapServerinfoEntity ldapEntity = ldapService.getLdapServerinfoEntity();
		NamingEnumeration<SearchResult> infos = null;
		try {
			infos = ldapService.getAllUsersInfo(ldapEntity);
			boolean flag = false;
			if (baseUserList == null || baseUserList.isEmpty()) {
				flag = false;
			} else {
				flag = true;
				for (Record u : baseUserList)
					userNameListDB.add((String) u.get("user_name"));
			}
			Record tsbu = null;
			while (infos.hasMoreElements()) {
				NamingEnumeration<? extends Attribute> attrs = ((SearchResult) infos.next()).getAttributes().getAll();
				tsbu = new Record();
				while (attrs.hasMore()) {
					Attribute attr = (Attribute) attrs.next();
					if (attr.getID().equals("name")) {
						boolean cFlag = flag ? (!userNameListDB.contains(attr.get().toString())) : true;
						if (cFlag) {
							tsbu.set("id", UUID.randomUUID().toString().replace("-", ""));
							tsbu.set("user_name", attr.get().toString());
							tsbu.set("password", LdapPasswordUtil.encrypt(attr.get().toString(), "123456",
									LdapPasswordUtil.getSalt()));
							baseUserListForSync.add(tsbu);
							userNameListLDAP.add(attr.get().toString());
						}
					}
					if (attr.getID().equals(PropKit.get("ldap.userpros.displayName")))
						tsbu.set("displayName", attr.get().toString());
					if (attr.getID().equals(PropKit.get("ldap.userpros.mail")))
						tsbu.set("mail", attr.get().toString());
					System.out.println(String.valueOf(attr.getID()) + ":" + attr.get());
				}
			}
			if (baseUserListForSync.size() > 0) {
		        boolean tx = Db.tx(new IAtom()
	            {
	              public boolean run() throws SQLException {
	                Db.batchSave("sys_user", baseUserListForSync, 1000);
	                return true;
	              }
	            });
		        String message = flag ? "DB中包含用户数据" : "DB不包含用户数据";
		        message = message + (tx ? "同步用户数据成功，共同步" + baseUserListForSync.size() + "条" : "同步用户发生异常，数据库回滚！");
	            LogKit.info(message);
			} else {
				LogKit.warn("没有要同步的用户信息!");
			}
		} catch (Exception e) {
			LogKit.error(e.getMessage(), e);
		}
	}

	public void run() {
		LogKit.info("运行同步用户任务...");
		sysLadpUserToDb();
		LogKit.info("运行同步用户任务结束");
	}

	public void stop() {
	}
}
