package com.zcurd.common.util;

import freemarker.cache.StringTemplateLoader;
import freemarker.template.Configuration;
import freemarker.template.Template;
import java.io.StringWriter;
import java.io.Writer;

public class FreemarkUtil {
  public static String parse(String tempStr, Object data) {
    if (StringUtil.isNotEmpty(tempStr) && (tempStr.indexOf("${") != -1 || tempStr.indexOf("</#") != -1)) {
      Configuration cfg = new Configuration(Configuration.VERSION_2_3_0);
      StringTemplateLoader stringLoader = new StringTemplateLoader();
      stringLoader.putTemplate("myTemplate", tempStr);
      cfg.setTemplateLoader(stringLoader);
      try {
        Template temp = cfg.getTemplate("myTemplate", "utf-8");
        Writer out = new StringWriter(2048);
        temp.process(data, out);
        return out.toString();
      } catch (Exception e) {
        e.printStackTrace();
      } 
    } 
    return tempStr;
  }
}
