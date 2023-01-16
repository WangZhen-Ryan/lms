package com.zcurd.service;

import com.jfinal.plugin.activerecord.Model;
import com.zcurd.model.SysAddressDictMain;
import java.util.List;

public class SysAddressDictMainService {
  private static final SysAddressDictMain dao = (SysAddressDictMain)(new SysAddressDictMain()).dao();
  
  public List<SysAddressDictMain> findByCondition(Model<SysAddressDictMain> model) {
    return dao.find("select * from sys_address_dict_main where build_code = ? and class_code=?", new Object[] { model.getStr("build_code"), model.getStr("class_code") });
  }
}
