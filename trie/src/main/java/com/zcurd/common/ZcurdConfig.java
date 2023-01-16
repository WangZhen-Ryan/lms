package com.zcurd.common;

import com.jfinal.aop.Duang;
import com.jfinal.config.Constants;
import com.jfinal.config.Handlers;
import com.jfinal.config.Interceptors;
import com.jfinal.config.JFinalConfig;
import com.jfinal.config.Plugins;
import com.jfinal.config.Routes;
import com.jfinal.core.ActionReporter;
import com.jfinal.core.JFinal;
import com.jfinal.ext.interceptor.SessionInViewInterceptor;
import com.jfinal.kit.PropKit;
import com.jfinal.plugin.activerecord.ActiveRecordPlugin;
import com.jfinal.plugin.activerecord.SqlReporter;
import com.jfinal.plugin.druid.DruidPlugin;
import com.jfinal.render.FreeMarkerRender;
import com.jfinal.render.ViewType;
import com.jfinal.template.Engine;
import com.zcurd.common.handler.ZcurdHandler;
import com.zcurd.common.interceptor.AuthInterceptor;
import com.zcurd.common.interceptor.ErrorInterceptor;
import com.zcurd.common.writer.LogFileWriter;
import com.zcurd.service.TaskService;
import freemarker.template.TemplateModelException;

public class ZcurdConfig extends JFinalConfig {
  public void configConstant(Constants me) {
    PropKit.use("config.properties");
    me.setDevMode(PropKit.getBoolean("devMode", Boolean.valueOf(false)).booleanValue());
    me.setViewType(ViewType.FREE_MARKER);
    SqlReporter.setLog(true);
    ActionReporter.setReportAfterInvocation(false);
    ActionReporter.setWriter(new LogFileWriter());
  }
  
  public void configRoute(Routes me) {
    me.add("/login", com.zcurd.controller.LoginController.class, "/zcurd/login");
    me.add("/zcurd", com.zcurd.controller.ZcurdController.class, "/zcurd/zcurd");
    me.add("/zcurdHead", com.zcurd.controller.ZcurdHeadController.class, "/zcurd");
    me.add("/menu", com.zcurd.controller.MenuController.class, "/zcurd/menu");
    me.add("/main", com.zcurd.controller.MainController.class, "/zcurd");
    me.add("/role", com.zcurd.controller.RoleController.class, "/zcurd/role");
    me.add("/common", com.zcurd.controller.CommonController.class, "/zcurd");
    me.add("/oplog", com.zcurd.controller.SysOplogController.class, "/zcurd/sysOplog");
    me.add("/user", com.zcurd.controller.SysUserController.class, "/zcurd/sysUser");
    me.add("/task", com.zcurd.controller.TaskBaseController.class, "/zcurd/taskBase");
    me.add("/menubtn", com.zcurd.controller.SysMenuBtnController.class, "/zcurd/menubtn");
    me.add("/menudatarule", com.zcurd.controller.SysMenuDataruleController.class, "/zcurd/menudatarule");
    me.add("/sysdict", com.zcurd.controller.SysDictController.class, "/zcurd/sysDict");
    me.add("/rack", com.zcurd.controller.RackController.class, "/zcurd/rack");
    me.add("/statistic", com.zcurd.controller.StatisitcController.class, "/zcurd/Statistic");
    me.add("/platform", com.zcurd.controller.PlatformController.class, "/zcurd/platform");
    me.add("/jumpline", com.zcurd.controller.JumpLineController.class, "/zcurd/jumpLine");
    me.add("/material", com.zcurd.controller.MaterialController.class, "/zcurd/material");
    me.add("/material2", com.zcurd.controller.MaterialController2.class, "/zcurd/material2");
    me.add("/instrument", com.zcurd.controller.InstrumentController.class, "/zcurd/instrument");
    me.add("/pr", com.zcurd.controller.PrController.class, "/zcurd/pr");
    me.add("/emd", com.zcurd.controller.EmdController.class, "/zcurd/emd");
    me.add("/addressdict", com.zcurd.controller.SysAddressDictController.class, "/zcurd/addressDict");
    me.add("/pc", com.zcurd.controller.PcController.class, "/zcurd/pc");
    me.add("/ldap", com.zcurd.controller.SysLadpSetController.class, "/zcurd/ldap");
    me.add("/attach", com.zcurd.controller.AttachmentFileController.class, "/zcurd/attachmentFile");
  }
  
  public void configPlugin(Plugins me) {
    DruidPlugin druidPlugin = new DruidPlugin(PropKit.get("base_jdbcUrl"), PropKit.get("base_user"), 
        PropKit.get("base_password").trim());
    me.add(druidPlugin);
    ActiveRecordPlugin arp = new ActiveRecordPlugin("zcurd_base", druidPlugin);
    arp.setShowSql(true);
    me.add(arp);
    arp.addMapping("zcurd_head", com.zcurd.model.ZcurdHead.class);
    arp.addMapping("zcurd_field", com.zcurd.model.ZcurdField.class);
    arp.addMapping("zcurd_head_btn", com.zcurd.model.ZcurdHeadBtn.class);
    arp.addMapping("zcurd_head_js", com.zcurd.model.ZcurdHeadJs.class);
    arp.addMapping("sys_menu", com.zcurd.model.Menu.class);
    arp.addMapping("sys_menu_btn", com.zcurd.model.MenuBtn.class);
    arp.addMapping("sys_menu_datarule", com.zcurd.model.MenuDatarule.class);
    arp.addMapping("sys_user", com.zcurd.model.SysUser.class);
    arp.addMapping("sys_oplog", com.zcurd.model.SysOplog.class);
    arp.addMapping("common_file", com.zcurd.model.CommonFile.class);
    arp.addMapping("sys_role", com.zcurd.model.SysRole.class);
    arp.addMapping("task_base", com.zcurd.model.TaskBase.class);
    arp.addMapping("task_log", "id", com.zcurd.model.TaskLog.class);
    arp.addMapping("sys_dict", "id", com.zcurd.model.SysDict.class);
    arp.addMapping("rack", "id", com.zcurd.model.Rack.class);
    arp.addMapping("platform", "id", com.zcurd.model.Platform.class);
    arp.addMapping("jump_line", com.zcurd.model.JumpLine.class);
    arp.addMapping("sys_user_role", com.zcurd.model.SysUserRole.class);
    arp.addMapping("material", com.zcurd.model.Material.class);
    arp.addMapping("material2", com.zcurd.model.Material2.class);
    arp.addMapping("instrument", com.zcurd.model.Instrument.class);
    arp.addMapping("pr", com.zcurd.model.Pr.class);
    arp.addMapping("emd", com.zcurd.model.Emd.class);
    arp.addMapping("sys_address_dict", "id", com.zcurd.model.SysAddressDict.class);
    arp.addMapping("pc", com.zcurd.model.Pc.class);
    arp.addMapping("attachment_file", com.zcurd.model.AttachmentFile.class);
    arp.addMapping("pr_order_detail", com.zcurd.model.PrOrderDetail.class);
    arp.addMapping("emd_order_detail", com.zcurd.model.EmdOrderDetail.class);
    DruidPlugin druidPluginAir = new DruidPlugin(PropKit.get("busi_jdbcUrl"), PropKit.get("busi_user"), 
        PropKit.get("busi_password").trim());
    me.add(druidPluginAir);
    ActiveRecordPlugin arpAir = new ActiveRecordPlugin("zcurd_busi", druidPluginAir);
    arpAir.setShowSql(true);
    me.add(arpAir);
  }
  
  public void configInterceptor(Interceptors me) {
    me.add(new SessionInViewInterceptor());
    me.add(new AuthInterceptor());
    me.add(new ErrorInterceptor());
  }
  
  public void configHandler(Handlers me) { me.add(new ZcurdHandler()); }
  
  public void afterJFinalStart() {
    try {
      FreeMarkerRender.getConfiguration().setSharedVariable("basePath", JFinal.me().getContextPath());
    } catch (TemplateModelException e) {
      e.printStackTrace();
    } 
    TaskService taskService = (TaskService)Duang.duang(TaskService.class);
    taskService.startAll();
  }
  
  public static void main(String[] args) { JFinal.start("src/main/webapp", 9008, "/zcurd", 5); }
  
  public void configEngine(Engine me) {}
}
