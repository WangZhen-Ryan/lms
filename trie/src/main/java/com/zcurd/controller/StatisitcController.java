package com.zcurd.controller;

import com.jfinal.core.Controller;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Record;
import com.jfinal.render.JsonRender;
import com.zcurd.common.util.Check;
import com.zcurd.model.Material;
import org.apache.commons.lang3.StringUtils;

import java.util.*;

public class StatisitcController extends Controller {
    public void outStat(){
        setAttr("dictDataCategory", Material.me.getDictDataCategory());

        render("outStat.html");
    }
    public void l(){
//		List<Record> tar = Db
//				.find("select OUT_from,type from zcurd_base.material"); // where 'status' = 'out'");
//		Map<Object ,Object> dict = new HashMap<>();
//		for(Record r : tar){
//			dict.put(r.get("type"),r.get("OUT_from"));
//		}
        //Map<String, Object> dictDataStatus = Material.me.getDictDataStatus();
//		for(String key:dictDataStatus.keySet()){
//			if(!key.equals("out")){
//				dictDataStatus.remove(key);
//			}}
//		return dict;

        String item_name = getPara("item_name");

        Map<Object ,Object> dict = new HashMap<>();
        Map<String ,Object> category = new HashMap<>();

        Map<String ,Object> COMMON_status = new HashMap<>();
        Map<String ,Object> PEM_status = new HashMap<>();

        List<Record> ALL_type = (Db.find("SELECT Category,Type,User,Status from zcurd_base.material where Type = " + "'" + item_name +"'" ));

        int size = ALL_type.size();
        dict.put("all",size);


        render((new JsonRender(dict)).forIE());
//		render((new JsonRender(category)).forIE());
//		render((new JsonRender(COMMON_status)).forIE());
//		render((new JsonRender(PEM_status)).forIE());
    }

    public 	void c(){
        String item_name = getPara("item_name");

        Map<String ,Integer> category = new HashMap<>();
        List<Record> ALL_type = (Db.find("SELECT Category,Type,User,Status from zcurd_base.material where Type = " + "'" + item_name +"'"));

        for(Record r : ALL_type){

            if(r != null){
                String name = "";
                try{
                    name = r.get("User").toString();}
                catch (NullPointerException e){
                    break;
                }
                System.out.println("name is "+name);
                if(!name.equals("")){
                    category.put(name,(Db.find("SELECT Type,User,Status from zcurd_base.material where Type = "  + "'" + item_name +"'" + " and User = " +"'"+ name+"'" )).size());}
                int a=-1;
                a= (category.isEmpty()) ? 1 :  0;
                System.out.println("empty "+ a);
            }}

        System.out.println("cat1 is " + category);
        // filter the map to get rid of null
        for (String a : category.keySet()){
            try{
                if(a.equals("")){
                    category.remove(a);
                }}
            catch (ConcurrentModificationException e){
                category.remove(a);
            }
        }
        System.out.println("cat2 is " + category);
        render((new JsonRender(category)).forIE());

    }

    public void S_c(){
        String item_name = getPara("item_name");

        Map<String ,Integer> S_category = new HashMap<>();
        List<Record> ALL_type = (Db.find("SELECT Category,Type,User,Status from zcurd_base.material where Type = " + "'" + item_name +"'"));
        String status_out_free = "out";
        S_category.put("out",(Db.find("SELECT Type,User from zcurd_base.material where Type = " + "'"+item_name +"'" + " and Status = " + "'"+ status_out_free+"'")).size());
        status_out_free = "free";
        S_category.put("free",(Db.find("SELECT Type,User from zcurd_base.material where Type = "+"'"+item_name+"'"+ " and Status = " + "'"+ status_out_free+"'")).size());

        System.out.println("final ratio on free is "+S_category);

        render((new JsonRender(S_category)).forIE());

    }
    public void p() {
        String item_name = getPara("item_name");
        Map<Object, Object> dict = new HashMap<>();
        Map<String, Object> category = new HashMap<>();

        Map<String, Object> COMMON_status = new HashMap<>();
        Map<String, Object> PEM_status = new HashMap<>();

        List<Record> ALL_type = (Db.find("SELECT Category,Type,User,Status from zcurd_base.material where Type = " + "'" + item_name +"'"));

        int size = ALL_type.size();

        dict.put("all", size);
        // PR_EMD_No status count
        if (size != 0) {
            // PR_EMD_No is yellow label
            PEM_status.put("yellow_free_number", (Db.find("SELECT Type,User,Status from zcurd_base.material where  Type = " +"'"+item_name +"'"+ " and status = 'free'and Category = 'PEM' and PR_EMD_No = '黄色标签'")).size());
            PEM_status.put("yellow_out_number", (Db.find("SELECT Type,User,Status from zcurd_base.material where  Type = " +"'" + item_name +"'" +" and status = 'out'and Category = 'PEM' and PR_EMD_No = '黄色标签'")).size());

            // PR_EMD_No is green label
            PEM_status.put("green_free_number",  (Db.find("SELECT Type,User,Status from zcurd_base.material where  Type = " +"'"+item_name +"'"+ " and status = 'free'and Category = 'PEM' and PR_EMD_No = '绿色标签'")).size());
            PEM_status.put("green_out_number", (Db.find("SELECT Type,User,Status from zcurd_base.material where  Type = " +"'"+item_name +"'"+ " and status = 'out'and Category = 'PEM' and PR_EMD_No = '绿色标签'")).size());

            render((new JsonRender(PEM_status)).forIE());
        }
    }
    public void np(){
        String item_name = getPara("item_name");
        Map<Object, Object> dict = new HashMap<>();
        Map<String, Object> category = new HashMap<>();

        Map<String, Object> COMMON_status = new HashMap<>();
        Map<String, Object> PEM_status = new HashMap<>();

        List<Record> ALL_type = (Db.find("SELECT Category,Type,User,Status from zcurd_base.material where Type = " + "'" + item_name +"'"));

        int size = ALL_type.size();

        dict.put("all", size);
        // free-out status count
        if (size != 0) {
            // PR_EMD_No is yellow label
            COMMON_status.put("free_number", (Db.find("SELECT Type,User,Status from zcurd_base.material where Type = " +"'"+item_name +"'" + " and status = 'free'and Category != 'PEM' ")).size());
            COMMON_status.put("out_number", (Db.find("SELECT Type,User,Status from zcurd_base.material where Type = " +"'"+item_name +"'" + " and status = 'out'and Category != 'PEM' ")).size());

            render((new JsonRender(COMMON_status)).forIE());
        }
    }

}
