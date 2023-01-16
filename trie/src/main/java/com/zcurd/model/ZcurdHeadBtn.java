package com.zcurd.model;

import com.jfinal.plugin.activerecord.Model;
import java.util.List;

public class ZcurdHeadBtn extends Model<ZcurdHeadBtn> {
  private static final long serialVersionUID = 1L;
  
  public static final ZcurdHeadBtn me = new ZcurdHeadBtn();
  
  public List<ZcurdHeadBtn> findByHeadId(int headId) { return find("select * from zcurd_head_btn where head_id=?", new Object[] { Integer.valueOf(headId) }); }
}
