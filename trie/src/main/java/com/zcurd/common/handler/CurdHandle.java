package com.zcurd.common.handler;

import com.zcurd.vo.ZcurdMeta;
import java.util.Map;
import javax.servlet.http.HttpServletRequest;

public interface CurdHandle {
  void add(ZcurdMeta paramZcurdMeta, HttpServletRequest paramHttpServletRequest, Map<String, String[]> paramMap);
  
  void update(ZcurdMeta paramZcurdMeta, HttpServletRequest paramHttpServletRequest, Map<String, String[]> paramMap);
  
  void delete(ZcurdMeta paramZcurdMeta, HttpServletRequest paramHttpServletRequest, Map<String, String[]> paramMap);
}
