package com.zcurd.controller;

import com.jfinal.upload.UploadFile;
import com.zcurd.common.DBTool;
import com.zcurd.common.util.StringUtil;
import com.zcurd.model.CommonFile;

public class CommonController extends BaseController {
  public void getDictData() {
    Object[] queryParams = getQueryParams();
    String[] properties = (String[])queryParams[0];
    String[] symbols = (String[])queryParams[1];
    Object[] values = (Object[])queryParams[2];
    String orderBy = getOrderBy();
    if (StringUtil.isEmpty(orderBy))
      orderBy = "id desc"; 
    renderJson(DBTool.findByMultProperties("sys_dict", properties, symbols, values));
  }
  
  public void iconsPage() { render("common/icons.html"); }
  
  public void uploadFile() {
    UploadFile file = getFile("upload", "images", 5242880);
    if (file != null) {
      ((CommonFile)((CommonFile)((CommonFile)(new CommonFile()).set("type", Integer.valueOf(1)))
        .set("path", file.getFileName())).set("sys_user_id", getSessionUser().get("id"))).save();
      renderJson("/upload/images/" + file.getFileName());
    } else {
      renderFailed();
    } 
  }
}
