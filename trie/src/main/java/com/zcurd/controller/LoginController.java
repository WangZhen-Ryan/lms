package com.zcurd.controller;

import com.jfinal.aop.Before;
import com.jfinal.aop.Duang;
import com.zcurd.model.AttachmentFile;
import com.zcurd.model.Menu;
import com.zcurd.model.SysUser;
import com.zcurd.service.LoginService;
import java.util.List;

public class LoginController extends BaseController {
  public void index() {
    List<AttachmentFile> find = AttachmentFile.me.find("select * from attachment_file order by id desc");
    setAttr("attaches", find);
    render("login.html");
  }
  
  public void getMenu() {
    if ("lmssuperadmin@1".equals(getSessionUser().get("user_name"))) {
      renderJson(Menu.me.findAll());
    } else {
      Object menuList = getSessionAttr("menuList");
      renderJson(menuList);
    } 
  }
  
  @Before({com.zcurd.common.interceptor.MenuAuthInterceptor.class})
  public void login() {
    LoginService loginService = (LoginService)Duang.duang(LoginService.class);
    SysUser sysUser = loginService.login(getPara("user_name"), getPara("password"));
    if (sysUser == null) {
      renderFailed("用户名或密码错误！");
    } else {
      setSessionAttr("sysUser", sysUser);
      addOpLog("登陆系统");
      renderSuccess();
    } 
  }
  
  public void logout() {
    addOpLog("退出系统");
    removeSessionAttr("sysUser");
    removeSessionAttr("authLoad");
    redirect("/login");
  }
}
