package com.zcurd.model;

import com.jfinal.plugin.activerecord.Model;
import java.util.List;

public class ZcurdHeadJs extends Model<ZcurdHeadJs> {
  private static final long serialVersionUID = 1L;
  
  public static final ZcurdHeadJs me = new ZcurdHeadJs();
  
  public List<ZcurdHeadJs> findByHeadId(int headId) { return find("select * from zcurd_head_js where head_id=?", new Object[] { Integer.valueOf(headId) }); }
}
