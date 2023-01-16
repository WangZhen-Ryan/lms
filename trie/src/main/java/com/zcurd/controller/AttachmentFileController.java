package com.zcurd.controller;

import com.jfinal.aop.Before;
import com.jfinal.aop.Clear;
import com.jfinal.kit.FileKit;
import com.jfinal.kit.LogKit;
import com.jfinal.kit.PathKit;
import com.jfinal.plugin.activerecord.Record;
import com.jfinal.upload.UploadFile;
import com.zcurd.common.DBTool;
import com.zcurd.common.util.StringUtil;
import com.zcurd.model.AttachmentFile;
import java.io.File;
import java.io.IOException;
import java.sql.Timestamp;
import java.util.Date;
import java.util.List;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.DateFormatUtils;

public class AttachmentFileController extends BaseController {

	private static final String uploadPath;

	static {
		uploadPath = PathKit.getWebRootPath() + File.separator + "res" + File.separator + "upload" + File.separator
				+ "attachment" + File.separator;
	}

	public void listPage() {
		this.render("list.html");
	}

	public void listData() {
		Object[] queryParams = getQueryParams();
		String[] properties = (String[]) queryParams[0];
		String[] symbols = (String[]) queryParams[1];
		Object[] values = (Object[]) queryParams[2];
		String orderBy = getOrderBy();
		if (StringUtil.isEmpty(orderBy))
			orderBy = "id desc";
		List<Record> list = DBTool.findByMultPropertiesDbSource("zcurd_base", "attachment_file", properties, symbols,
				values, orderBy, getPager());
		renderDatagrid(list,
				DBTool.countByMultPropertiesDbSource("zcurd_base", "attachment_file", properties, symbols, values));
	}

	public void addPage() {
		render("add.html");
	}

	@Before({ com.jfinal.plugin.activerecord.tx.Tx.class })
	public void add() {
		String file_name = DateFormatUtils.format(new Date(), "yyyyMMddHHmmssSSS");
		UploadFile file = getFile();
		String fileName = file.getFileName();
		String[] split = StringUtils.split(fileName, ".");
		File destFile = new File(String.valueOf(uploadPath) + file_name + "." + split[split.length - 1]);
		try {
			FileUtils.copyFile(file.getFile(), destFile);
			((AttachmentFile) ((AttachmentFile) ((AttachmentFile) ((AttachmentFile) (new AttachmentFile()).set("title",
					getPara("model.title"))).set("create_time", new Timestamp(System.currentTimeMillis())))
							.set("create_name", getSessionUser().get("display_name"))).set("file_name",
									String.valueOf(file_name) + "." + split[split.length - 1])).save();
			addOpLog("[附件表] 增加");
			renderFileUploadJsonSuccess("上传附件成功!");
		} catch (IOException e) {
			LogKit.error("上传附件失败", e);
			renderFileUploadJsonFailed("上传附件失败![" + e.getMessage() + "]");
		}
	}

	public void updatePage() {
		setAttr("model", AttachmentFile.me.findById(getPara("id")));
		render("update.html");
	}

	@Before({ com.jfinal.plugin.activerecord.tx.Tx.class })
	public void update() {
		AttachmentFile model = (AttachmentFile) AttachmentFile.me.findById(getPara("model.id"));
		model.set("title", getPara("model.title"));
		String type = getPara("model.type");
		try {
			if ("1".equals(type)) {
				String file_name = DateFormatUtils.format(new Date(), "yyyyMMddHHmmssSSS");
				UploadFile file = getFile();
				String fileName = file.getFileName();
				String[] split = StringUtils.split(fileName, ".");
				String f = String.valueOf(file_name) + "." + split[split.length - 1];
				File destFile = new File(String.valueOf(uploadPath) + f);
				FileUtils.copyFile(file.getFile(), destFile);
				file.getFile().delete();
				model.set("file_name", f);
				File srcUpdateFile = new File(String.valueOf(uploadPath) + model.getStr("file_name"));
				srcUpdateFile.delete();
			}
			((AttachmentFile) ((AttachmentFile) model.set("create_time", new Timestamp(System.currentTimeMillis())))
					.set("create_name", getSessionUser().get("display_name"))).update();
			addOpLog("[附件表] 修改");
			if ("1".equals(type)) {
				renderFileUploadJsonSuccess("操作成功!");
			} else {
				renderSuccess("操作成功!");
			}
		} catch (IOException e) {
			LogKit.error("修改失败", e);
			if ("1".equals(type)) {
				renderFileUploadJsonFailed("修改失败![" + e.getMessage() + "]");
			} else {
				renderFailed("修改失败![" + e.getMessage() + "]");
			}
		}
	}

	@Before({ com.jfinal.plugin.activerecord.tx.Tx.class })
	public void delete() {
		Long[] ids = this.getParaValuesToLong("id[]");
		File file = null;
		Long[] var6 = ids;
		int var5 = ids.length;

		for (int var4 = 0; var4 < var5; ++var4) {
			Long id = var6[var4];
			AttachmentFile model = (AttachmentFile) AttachmentFile.me.findById(id);
			model.delete();
			file = new File(uploadPath + model.getStr("file_name"));
			FileKit.delete(file);
		}

		this.addOpLog("[附件表] 删除");
		this.renderSuccess();
	}

	public void detailPage() {
		AttachmentFile model = (AttachmentFile) AttachmentFile.me.findById(getParaToLong("id"));
		setAttr("model", model);
		render("detail.html");
	}

	@Clear
	public void downloadAttachFile() {
		String fileName = getPara("fileName");
		String webRootPath = PathKit.getWebRootPath();
		String filePath = String.valueOf(webRootPath) + File.separator + "res" + File.separator + "upload"
				+ File.separator + "attachment" + File.separator + fileName;
		File file = new File(filePath);
		renderFile(file);
	}
}
