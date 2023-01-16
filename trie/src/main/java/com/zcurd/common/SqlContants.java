package com.zcurd.common;

public interface SqlContants {
  public static final String dictComboboxSql = "select dict_key as k,dict_value as v from sys_dict where dict_type='#1'";
  
  public static final String dictAddressComboboxSql = "select id as k ,auto_val as v from sys_address_dict  where `status`='1'";
  
  public static final String dictUserComboboxSql = "select id as k ,display_name as v from sys_user  where `id` !='6ae3f56fc43c5420417121954607de52'";
  
  public static final String dictUser = "select id as k ,user_name as v from sys_user";
  
  public static final String dictRole = "select id as k ,role_name as v from sys_role";
}
