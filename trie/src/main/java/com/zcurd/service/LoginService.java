package com.zcurd.service;

import com.jfinal.aop.Duang;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Record;
import com.zcurd.common.ErrorMsgException;
import com.zcurd.common.util.FreemarkUtil;
import com.zcurd.common.util.StringUtil;
import com.zcurd.common.util.UrlUtil;
import com.zcurd.ldap.LdapPasswordUtil;
import com.zcurd.ldap.LdapServerinfoEntity;
import com.zcurd.ldap.LdapService;
import com.zcurd.model.Menu;
import com.zcurd.model.MenuBtn;
import com.zcurd.model.MenuDatarule;
import com.zcurd.model.SysUser;
import com.zcurd.model.SysUserRole;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public class LoginService {
	private List<Menu> allMenuList;

	private List<Menu> userMenuList;

	public List<Menu> getUserMenu(SysUser user) {
		this.allMenuList = Menu.me.findAll();
		this.userMenuList = Menu.me.findByUser(user);
		Set<Menu> chainSet = new HashSet<Menu>();
		for (Menu menu : this.userMenuList) {
			chainSet.add(menu);
			getPChain(this.allMenuList, menu, chainSet);
		}
		List<Menu> chainList = new ArrayList<Menu>(chainSet);
		Collections.sort(chainList, new Comparator<Menu>() {
			public int compare(Menu o1, Menu o2) {
				if (o1.get("order_num") == null || o2.get("order_num") == null
						|| o1.getInt("order_num").intValue() < o2.getInt("order_num").intValue()) {
					return -1;
				}
				return 0;
			}
		});
		formatSubMenu(chainList);
		List<Menu> result = new ArrayList<Menu>();
		for (Menu menu : chainList)
			result.add(menu);
		return result;
	}

	public List<String> getNoAuthUrl() {
		List<String> urlList = new ArrayList<String>();
		for (Menu menu1 : this.allMenuList) {
			boolean flag = true;
			for (Menu menu2 : this.userMenuList) {
				if (menu1.getInt("id") == menu2.getInt("id")) {
					flag = false;
					break;
				}
			}
			if (flag && StringUtil.isNotEmpty(menu1.getStr("menu_url")))
				urlList.add(menu1.getStr("menu_url"));
		}
		return urlList;
	}

	public Map<String, Object> getNoAuthBtnUrl(SysUser user) {
		Map<Integer, Menu> userMenuMap = new HashMap<Integer, Menu>();
		for (Menu menu : this.userMenuList)
			userMenuMap.put(menu.getInt("id"), menu);
		Map<String, Object> result = new HashMap<String, Object>();
		List<String> btnUrlList = new ArrayList<String>();
		List<MenuBtn> userBtnList = getDifference(MenuBtn.me.findAll(), MenuBtn.me.findByUser(user));
		Map<String, String> pageBtnMap = new HashMap<String, String>();
		for (MenuBtn menuBtn : userBtnList) {
			String methodName = menuBtn.getStr("method_name");
			Menu menu = (Menu) userMenuMap.get(menuBtn.getInt("menu_id"));
			if (menu == null)
				continue;
			String menuUrl = menu.getStr("menu_url");
			menuUrl = UrlUtil.removeUrlParam(menuUrl);
			if (StringUtil.isNotEmpty(methodName) && StringUtil.isNotEmpty(menuUrl)) {
				byte b; int i; String[] arrayOfString; 
				for (i = (arrayOfString = methodName.split(",")).length, b = 0; b < i; ) {
					String method = arrayOfString[b]; 

					method = ("/" + method).replaceAll("//+", "/");
					if (method.lastIndexOf("/") > 0) {
						btnUrlList.add(method);
					} else {
						btnUrlList.add(String.valueOf(UrlUtil.formatBaseUrl(menuUrl)) + method);
					}
					b++;
				}
				String btnName = (String) pageBtnMap.get(menuUrl);
				if (StringUtil.isEmpty(btnName)) {
					btnName = menuBtn.getStr("class_name");
				} else {
					btnName = String.valueOf(btnName) + "," + menuBtn.getStr("class_name");
				}
				pageBtnMap.put(menuUrl, btnName);
			}
		}
		result.put("btnUrlList", btnUrlList);
		result.put("pageBtnMap", pageBtnMap);
		return result;
	}

	public Map<String, List<MenuDatarule>> getNoAuthDatarule(SysUser user) {
		Map<Integer, Menu> userMenuMap = new HashMap<Integer, Menu>();
		for (Menu menu : this.userMenuList)
			userMenuMap.put(menu.getInt("id"), menu);
		Map<String, List<MenuDatarule>> pageDataruleMap = new HashMap<String, List<MenuDatarule>>();
		List<MenuDatarule> userDataruleList = MenuDatarule.me.findByUser(user);
		for (MenuDatarule menuDatarule : userDataruleList) {
			String value = (String) menuDatarule.get("value");
			Map<String, Object> data = new HashMap<String, Object>();
			data.put("user", user);
			menuDatarule.set("value", FreemarkUtil.parse(value, data));
		}
		for (MenuDatarule menuDatarule : userDataruleList) {
			String menuUrl = ((Menu) userMenuMap.get(menuDatarule.getInt("menu_id"))).getStr("menu_url");
			if (StringUtil.isNotEmpty(menuUrl)) {
				menuUrl = UrlUtil.formatBaseUrl(menuUrl);
				List<MenuDatarule> dataruleList = (List) pageDataruleMap.get(menuUrl);
				if (dataruleList == null)
					dataruleList = new ArrayList<MenuDatarule>();
				dataruleList.add(menuDatarule);
				pageDataruleMap.put(menuUrl, dataruleList);
			}
		}
		return pageDataruleMap;
	}

	public void getPChain(Collection<Menu> list, Menu menu, Set<Menu> chainlist) {
		for (Menu m : list) {
			if (menu.getInt("parent_id") == m.getInt("id")) {
				chainlist.add(m);
				getPChain(list, m, chainlist);
			}
		}
	}

	private void formatSubMenu(Collection<Menu> list) {
		for (Menu menu1 : list) {
			for (Menu menu2 : list) {
				if (menu1.getInt("id") == menu2.getInt("parent_id"))
					menu1.getSubMenuList().add(menu2);
			}
		}
	}

	private List<MenuBtn> getDifference(List<MenuBtn> list1, List<MenuBtn> list2) {
		List<MenuBtn> result = new ArrayList<MenuBtn>();
		for (MenuBtn menuBtn1 : list1) {
			boolean flag = true;
			for (MenuBtn menuBtn2 : list2) {
				int i = menuBtn1.getInt("id").intValue();
				int j = menuBtn2.getInt("id").intValue();
				if (i == j) {
					flag = false;
					break;
				}
			}
			if (flag)
				result.add(menuBtn1);
		}
		return result;
	}

	public SysUser login(String userName, String password) {
		LdapService ldapService1 = (LdapService) Duang.duang(LdapService.class);
		LdapService ldapService2 = (LdapService) Duang.duang(LdapService.class);
		System.out.println((ldapService1 == ldapService2));
		SysUser sysUser = null;
		String encrypt = LdapPasswordUtil.encrypt(userName, password, LdapPasswordUtil.getStaticSalt());
		if ("lmssuperadmin@1".equals(userName)) {
			List<SysUser> list = SysUser.me.findByMultiProperties(new String[] { "user_name", "password" },
					new Object[] { userName, encrypt });
			if (list != null && !list.isEmpty())
				return (SysUser) list.get(0);
		}
		try {
			LdapService ldapService = (LdapService) Duang.duang(LdapService.class);
			LdapServerinfoEntity ldapEntity = ldapService.getLdapServerinfoEntity();
			String sql = "select * from sys_user where id <> '6ae3f56fc43c5420417121954607de52'";
			List<Record> baseUserList = Db.find(sql);
			Map<String, String> map = new HashMap<String, String>();
			map.put("user_name", userName);
			map.put("password", password);
			if (baseUserList == null || baseUserList.isEmpty()) {
				map = ldapService.loginSearch(map, ldapEntity);
				if (map != null) {
					sysUser = (SysUser) ((SysUser) ((SysUser) ((SysUser) ((SysUser) (new SysUser()).set("id",
							UUID.randomUUID().toString().replace("-", ""))).set("user_name", userName)).set("mail",
									map.get("mail"))).set("display_name", map.get("display_name"))).set("password",
											encrypt);
					sysUser.save();
				}
			} else {
				List<SysUser> list = SysUser.me.findByMultiProperties(new String[] { "user_name", "password" },
						new Object[] { userName, encrypt });
				if (list != null && !list.isEmpty()) {
					sysUser = (SysUser) list.get(0);
				} else {
					String userPrinciPal = ldapService.getPrinciPal(userName, ldapEntity);
					ldapEntity.setPrincipal(userPrinciPal);
					map = ldapService.loginSearch(map, ldapEntity);
					if (map != null)
						sysUser = findByNameAndSave(new String[] { "user_name", "password" },
								new Object[] { userName, encrypt }, sysUser, map);
				}
			}
			if (sysUser != null) {
				SysUserRole sysUserRole = (SysUserRole) SysUserRole.me
						.findFirst("select * from sys_user_role where id=?", new Object[] { sysUser.getStr("id") });
				String roles = (sysUserRole != null) ? sysUserRole.getStr("roles") : "";
				sysUser.put("roles", roles);
			}
		} catch (Exception e) {
			throw new ErrorMsgException("对不起，系统发生异常", e);
		}
		return sysUser;
	}

	public static final Object lock = new Object();

	private SysUser findByNameAndSave(String[] strings, Object[] objects, SysUser sysUser, Map<String, String> map) {
		synchronized (lock) {
			List<SysUser> list1 = SysUser.me.findByMultiProperties(new String[] { strings[0] },
					new Object[] { objects[0] });
			if (list1 != null && !list1.isEmpty()) {
				sysUser = (SysUser) list1.get(0);
				sysUser.set("password", objects[1]);
				sysUser.update();
			} else {
				sysUser = (SysUser) ((SysUser) ((SysUser) ((SysUser) ((SysUser) (new SysUser()).set("id",
						UUID.randomUUID().toString().replace("-", ""))).set("user_name", objects[0])).set("mail",
								map.get("mail"))).set("display_name", map.get("display_name"))).set("password",
										objects[1]);
				sysUser.save();
			}
			return sysUser;
		}
	}
}
