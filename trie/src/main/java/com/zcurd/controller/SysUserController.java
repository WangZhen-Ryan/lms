package com.zcurd.controller;

import com.jfinal.aop.Before;
import com.jfinal.aop.Clear;
import com.jfinal.kit.PathKit;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Page;
import com.jfinal.plugin.activerecord.Record;
import com.jfinal.render.JsonRender;
import com.jfinal.upload.UploadFile;
import com.zcurd.common.util.StringUtil;
import com.zcurd.common.util.ToolDateTime;
import com.zcurd.excel.ExcelUtils;
import com.zcurd.excel.utils.Utils;
import com.zcurd.ldap.LdapPasswordUtil;
import com.zcurd.model.SysUser;
import com.zcurd.model.SysUserRole;
import java.io.BufferedInputStream;
import java.io.Closeable;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;

public class SysUserController extends BaseController {
	private HSSFWorkbook wb;

	public void listPage() {
		render("list.html");
	}

	public void listData() {
		String select = "select a.*,b.roles";
		String sqlExceptSelect = " from sys_user a left join sys_user_role b on a.id = b.id  where a.id<>'6ae3f56fc43c5420417121954607de52' ";
		if (StringUtils.isNotEmpty(getPara("user_name")))
			sqlExceptSelect = String.valueOf(sqlExceptSelect) + " and a.user_name like '%" + getPara("user_name")
					+ "%'";
		String orderBy = getOrderBy();
		if (StringUtil.isEmpty(orderBy)) {
			sqlExceptSelect = String.valueOf(sqlExceptSelect) + " order by a.id desc";
		} else {
			sqlExceptSelect = String.valueOf(sqlExceptSelect) + " order by " + orderBy;
		}
		Page<Record> paginate = Db.paginate(getPager().getPage(), getPager().getRows(), select, sqlExceptSelect);
		Map<String, Object> dictDataroles = SysUser.me.getDictDataroles();
		List<String> list = null;
		for (Record record : paginate.getList()) {
			if (record.get("roles") != null) {
				String str = record.getStr("roles");
				list = new ArrayList<String>();
				String[] split = StringUtils.split(str, ",");
				byte b;
				int i;
				String[] arrayOfString;
				for (i = (arrayOfString = split).length, b = 0; b < i;) {
					String string = arrayOfString[b];
					if (dictDataroles.containsKey(string))
						list.add(dictDataroles.get(string).toString());
					b++;
				}
				record.set("roles", StringUtils.join(list.iterator(), ","));
			}
		}
		renderDatagrid(paginate);
	}

	public void updatePage() {
		SysUser model = (SysUser) SysUser.me.findById(getPara("id"));
		Map<String, Object> dictDataroles = SysUser.me.getDictDataroles();
		SysUserRole sysUserRole = (SysUserRole) SysUserRole.me.findById(getPara("id"));
		model.put("roles", (sysUserRole != null) ? sysUserRole.get("roles") : "");
		setAttr("dictDataroles", dictDataroles);
		setAttr("model", model);
		render("update.html");
	}

	@Clear
	public void updatePasswordPage() {
		setAttr("dictDataroles", SysUser.me.getDictDataroles());
		setAttr("model", SysUser.me.findFirst("select * from sys_user where id=?",
				new Object[] { getSessionUser().getStr("id") }));
		render("updatePassword.html");
	}

	@Before({ com.jfinal.plugin.activerecord.tx.Tx.class })
	@Clear
	public void updatePassword() {
		SysUser model = (SysUser) SysUser.me.findFirst("select * from sys_user where id=?",
				new Object[] { getSessionUser().getStr("id") });
		String encrypt = LdapPasswordUtil.encrypt((String) getSessionUser().get("user_name"),
				getPara("model.old_password"), LdapPasswordUtil.getStaticSalt());
		if (!model.getStr("password").equals(encrypt)) {
			renderFailed("原始密码输入错误");
			return;
		}
		encrypt = LdapPasswordUtil.encrypt((String) getSessionUser().get("user_name"), getPara("model.password"),
				LdapPasswordUtil.getStaticSalt());
		model.set("password", encrypt);
		model.update();
		addOpLog("[用户行为] 修改密码");
		renderSuccess();
	}

	@Before({ com.jfinal.plugin.activerecord.tx.Tx.class })
	public void resetPassword() {
		String[] ids = getParaValues("id[]");
		byte b;
		int i;
		String[] arrayOfString;
		for (i = (arrayOfString = ids).length, b = 0; b < i;) {
			String id = arrayOfString[b];
			SysUser sysUser = (SysUser) SysUser.me.findById(id);
			((SysUser) sysUser.set("password",
					LdapPasswordUtil.encrypt(sysUser.getStr("user_name"), "123456", LdapPasswordUtil.getStaticSalt())))
							.update();
			b++;
		}
		addOpLog("[系统用户] 重置密码");
		renderSuccess();
	}

	@Before({ com.jfinal.plugin.activerecord.tx.Tx.class })
	public void update() {
		String roles = getPara("model.roles");
		SysUserRole model = (SysUserRole) SysUserRole.me.findById(getPara("id"));
		if (model == null) {
			((SysUserRole) ((SysUserRole) (new SysUserRole()).set("id", getPara("id"))).set("roles", roles)).save();
		} else {
			model.set("roles", roles);
			model.update();
		}
		addOpLog("[用户管理] 修改");
		renderSuccess();
	}

	public void detailPage() {
		SysUser model = (SysUser) SysUser.me.findById(getPara("id"));
		Map<String, Object> dictDataroles = SysUser.me.getDictDataroles();
		SysUserRole sysUserRole = (SysUserRole) SysUserRole.me.findById(getPara("id"));
		List<String> list = new ArrayList<String>();
		if (sysUserRole != null) {
			String str = sysUserRole.getStr("roles");
			String[] split = StringUtils.split(str, ",");
			if (split != null) {
				byte b;
				int i;
				String[] arrayOfString;
				for (i = (arrayOfString = split).length, b = 0; b < i;) {
					String string = arrayOfString[b];
					if (dictDataroles.containsKey(string))
						list.add(dictDataroles.get(string).toString());
					b++;
				}
			}
		}
		model.put("roles", StringUtils.join(list.iterator(), ","));
		model.put("create_time", (sysUserRole != null) ? sysUserRole.get("create_time") : "");
		setAttr("model", model);
		render("detail.html");
	}

	@Before({ com.jfinal.plugin.activerecord.tx.Tx.class })
	public void importExcel() {
		logger.info(String.valueOf(getSessionUser().getStr("display_name")) + "开始上传系统用户文件...");
		Map<String, Object> result = new HashMap<String, Object>();
		result.put("result", "fail");
		result.put("msg", "操作失败！");
		UploadFile file = getFile();
		File uploadFile = file.getFile();
		FileInputStream fis = null;
		FileInputStream fis1 = null;
		BufferedInputStream bis = null;
		BufferedInputStream bis1 = null;
		try {
			fis = new FileInputStream(uploadFile);
			bis = new BufferedInputStream(fis);
			List<List<String>> ruleExcelListList = ExcelUtils.getInstance().readExcel2List(bis, 0, "valiRule");
			Map<Integer, String[]> ruleMap = new HashMap<Integer, String[]>();
			if (ruleExcelListList == null || ruleExcelListList.isEmpty()) {
				result.put("result", "fail");
				result.put("msg", "对不起，文件不符合模板。");
			} else {
				for (int i = 0; i < ruleExcelListList.size(); i++)
					ruleMap.put(Integer.valueOf(i),
							(String[]) ((List) ruleExcelListList.get(i)).toArray(new String[0]));
				fis1 = new FileInputStream(uploadFile);
				bis1 = new BufferedInputStream(fis1);
				List<List<String>> readExcel2List = ExcelUtils.getInstance().readExcel2List(bis1, 1);
				if (readExcel2List == null || readExcel2List.isEmpty()) {
					result.put("result", "fail");
					result.put("msg", "对不起，请向文件中写入内容！");
				} else {
					List<Record> findUser = Db.find("select id as k ,user_name as v from sys_user");
					List<Record> findRole = Db.find("select id as k ,role_name as v from sys_role");
					Map<String, String> rackUserMap = new HashMap<String, String>();
					for (int i = 0; i < findUser.size(); i++) {
						Record record = (Record) findUser.get(i);
						rackUserMap.put(record.getStr("v"), record.getStr("k"));
					}
					Map<String, Long> rackRoleMap = new HashMap<String, Long>();
					for (int i = 0; i < findRole.size(); i++) {
						Record record = (Record) findRole.get(i);
						rackRoleMap.put(record.getStr("v"), record.getLong("k"));
					}
					List<String> listResult = new ArrayList<String>();
					List<Record> batchRecord = new ArrayList<Record>();
					Record record = null;
					boolean isUseRecord = true;
					for (int i = 0; i < readExcel2List.size(); i++) {
						List<String> list = (List) readExcel2List.get(i);
						record = new Record();
						for (int j = 0; j < list.size(); j++) {
							boolean valiflag = true;
							String val = (String) list.get(j);
							String[] strings = (String[]) ruleMap.get(Integer.valueOf(j));
							if ("required:true".equals(strings[3]) && StringUtils.isEmpty(val)) {
								valiflag = false;
								listResult.add("Excel" + (i + 2) + "行" + strings[0] + "列数据为必录项");
							}
							if (valiflag && StringUtils.isNotEmpty(val)) {
								Object v = null;
								if (strings[2].startsWith("Long")) {
									try {
										v = Utils.str2TargetClass(val, Long.class);
									} catch (Exception e) {
										valiflag = false;
										listResult.add("Excel" + (i + 2) + "行" + strings[0] + "列数据应为数字");
									}
								} else if (strings[2].startsWith("Date")) {
									try {
										v = Utils.str2TargetClass(val, Date.class);
									} catch (Exception e) {
										valiflag = false;
										listResult.add("Excel" + (i + 2) + "行" + strings[0] + "列数据应为日期");
									}
								} else if (strings[2].startsWith("Integer")) {
									try {
										v = Utils.str2TargetClass(val, Integer.class);
										String[] split = strings[2].split(":");
										if (split.length > 1) {
											valiflag = (((Integer) v).intValue() >= Integer.parseInt(split[1]));
											if (!valiflag) {
												listResult.add("Excel" + (i + 2) + "行" + strings[0] + "列最小值为"
														+ Integer.parseInt(split[1]));
											} else if (split.length == 3) {
												valiflag = (((Integer) v).intValue() <= Integer.parseInt(split[2]));
												if (!valiflag)
													listResult.add("Excel" + (i + 2) + "行" + strings[0] + "列最大值为"
															+ Integer.parseInt(split[2]));
											}
										}
									} catch (Exception e) {
										valiflag = false;
										listResult.add("Excel" + (i + 2) + "行" + strings[0] + "列数据应为数字");
									}
								} else {
									v = val;
								}
								if (valiflag) {
									switch (j) {
									case 0:
										if (rackUserMap.containsKey(v)) {
											valiflag = false;
											listResult.add("Excel" + (i + 2) + "行" + strings[0] + "列数据用户名已存在");
										}
										break;
									case 1:
										if (rackRoleMap.containsKey(v)) {
											v = rackRoleMap.get(v);
											break;
										}
										valiflag = false;
										listResult.add("Excel" + (i + 2) + "行" + strings[0] + "列数据不存在于角色列表中");
										break;
									}
									record.set(strings[1], v);
								}
							}
							isUseRecord = isUseRecord ? valiflag : isUseRecord;
						}
						if (isUseRecord)
							batchRecord.add(record);
					}
					if (listResult.isEmpty()) {
						for (int i = 0; i < batchRecord.size(); i++) {
							Record record_ = (Record) batchRecord.get(i);
							String id = UUID.randomUUID().toString().replace("-", "");
							Long role = record_.getLong("roles");
							record_.remove("roles");
							record_.set("id", id);
							Db.save("sys_user", record_);
							SysUserRole sysUserRole = (SysUserRole) ((SysUserRole) (new SysUserRole()).set("id", id))
									.set("roles", role);
							sysUserRole.save();
						}
						result.put("result", "success");
						result.put("msg", "操作成功,共入库" + batchRecord.size() + "条数据");
						logger.info(String.valueOf(getSessionUser().getStr("display_name")) + "上传系统用户文件结束，共入库"
								+ batchRecord.size() + "条数据");
					} else {
						result.put("result", "fail");
						result.put("msg", "操作失败!<br/>" + StringUtils.join(listResult.iterator(), "<br/>"));
						logger.info(String.valueOf(getSessionUser().getStr("display_name")) + "上传系统用户文件异常结束");
					}
				}
			}
		} catch (FileNotFoundException e) {
			logger.error("上传文件未找到", e);
			result.put("result", "fail");
			result.put("msg", e.getMessage());
		} catch (Exception e) {
			logger.error("读取Excel文件或入库异常,请先下载模板后再录入", e);
			result.put("result", "fail");
			result.put("msg", "读取Excel文件或入库异常，请先下载模板后再录入！[" + e.getMessage() + "]");
		} finally {
			IOUtils.closeQuietly(new Closeable[] { fis, bis, fis1, bis1 });
			uploadFile.delete();
		}
		render((new JsonRender(result)).forIE());
	}

	public void expExcel() {
		String select = "select a.*,b.roles";
		String sqlExceptSelect = " from sys_user a left join sys_user_role b on a.id = b.id  where a.id<>'6ae3f56fc43c5420417121954607de52' ";
		if (StringUtils.isNotEmpty(getPara("user_name")))
			sqlExceptSelect = String.valueOf(sqlExceptSelect) + " and a.user_name like '%" + getPara("user_name")
					+ "%'";
		String orderBy = getOrderBy();
		if (StringUtil.isEmpty(orderBy)) {
			sqlExceptSelect = String.valueOf(sqlExceptSelect) + " order by a.id desc";
		} else {
			sqlExceptSelect = String.valueOf(sqlExceptSelect) + " order by " + orderBy;
		}
		List<Record> paginate2 = Db.find(String.valueOf(select) + sqlExceptSelect);
		Map<String, Object> dictDataroles = SysUser.me.getDictDataroles();
		List<String> list = null;
		for (Record record : paginate2) {
			if (record.get("roles") != null) {
				String str = record.getStr("roles");
				list = new ArrayList<String>();
				String[] split = StringUtils.split(str, ",");
				byte b;
				int j;
				String[] arrayOfString;
				for (j = (arrayOfString = split).length, b = 0; b < j;) {
					String string = arrayOfString[b];
					if (dictDataroles.containsKey(string))
						list.add(dictDataroles.get(string).toString());
					b++;
				}
				record.set("roles", StringUtils.join(list.iterator(), ","));
			}
		}
		String fileName = "系统用户表" + ToolDateTime.format(new Date(), "yyyyMMddHHmmssSSS") + ".xls";
		String path = String.valueOf(PathKit.getWebRootPath()) + "\\upload\\" + fileName;
		System.out.println(path);
		File file = new File(path);
		this.wb = new HSSFWorkbook();
		HSSFSheet sheet = this.wb.createSheet("系统用户表");
		HSSFRow row0 = sheet.createRow(0);
		row0.createCell(0).setCellValue("用户名");
		row0.createCell(1).setCellValue("角色");
		row0.createCell(2).setCellValue("邮箱");
		row0.createCell(3).setCellValue("真实名称");
		row0.createCell(4).setCellValue("密码");
		for (int i = 0; i < paginate2.size(); i++) {
			Record record2 = (Record) paginate2.get(i);
			HSSFRow rowi = sheet.createRow(i + 1);
			rowi.createCell(0).setCellValue(isNull(record2.get("user_name")));
			rowi.createCell(1).setCellValue(isNull(record2.get("roles")));
			rowi.createCell(2).setCellValue(isNull(record2.get("mail")));
			rowi.createCell(3).setCellValue(isNull(record2.get("display_name")));
			rowi.createCell(4).setCellValue(isNull(record2.get("password")));
		}
		FileOutputStream fileOut = null;
		try {
			fileOut = new FileOutputStream(file);
			this.wb.write(fileOut);
			renderFile(file);
		} catch (Exception e) {
			e.printStackTrace();
			renderSuccess("导出失败!");
		} finally {
			if (fileOut != null)
				try {
					fileOut.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
		}
	}
}
