package com.redmoon.oa.flow.macroctl;

import java.sql.SQLException;

import javax.servlet.http.HttpServletRequest;

import cn.js.fan.db.ResultIterator;
import cn.js.fan.db.ResultRecord;
import cn.js.fan.util.StrUtil;

import com.cloudwebsoft.framework.db.JdbcTemplate;
import com.cloudwebsoft.framework.util.LogUtil;
import com.redmoon.oa.flow.FormField;

/**
 * <p>Title: </p>
 *
 * <p>Description: </p>
 *
 * <p>Copyright: Copyright (c) 2007</p>
 *
 * <p>Company: </p>
 *
 * @author not attributable
 * @version 1.0
 */
public class CityCtl extends AbstractMacroCtl{
    public CityCtl() {
    }

    public String convertToHTMLCtl(HttpServletRequest request, FormField ff) {
        String str = "";

        String rid = ff.getName(); // String)request.getAttribute("province_city_country_id");

        str += "<select id='" + ff.getName() + "' name='" + ff.getName() + "' onchange=\"if (this.value!='') ajaxShowCityCountry" + rid + "('',this.value)\">";
        str += "<option value=''>无</option>";
        if (ff.getValue()!=null && !ff.getValue().equals("")) {
            try {
                JdbcTemplate jt = new JdbcTemplate();
                String sql =
                        "select parent_id from oa_china_region where region_id=" +
                        ff.getValue();
                int parent_id = -1;
                ResultIterator ri = jt.executeQuery(sql);
                if (ri.hasNext()) {
                    ResultRecord rr = (ResultRecord) ri.next();
                    parent_id = rr.getInt(1);
                }

                sql = "select region_id,region_name from oa_china_region where region_type=2 and parent_id=" + parent_id
                      + " order by region_id";
                ri = jt.executeQuery(sql);
                while (ri.hasNext()) {
                    ResultRecord rr = (ResultRecord) ri.next();
                    str += "<option value='" + rr.getInt(1) + "'>" +
                            rr.getString(2) +
                            "</option>";
                }
            } catch (SQLException e) {
                LogUtil.getLog(getClass()).error(StrUtil.trace(e));
            }
        }
        str += "</select>";

        return str;
    }
    
    public String converToHtml(HttpServletRequest request, FormField ff, String fieldValue) {
        if (fieldValue==null || "".equals(fieldValue)) {
        	return "";
        }
        
        try {
            String sql = "select region_name from oa_china_region where region_id=" + fieldValue;
            JdbcTemplate jt = new JdbcTemplate();
            ResultIterator ri = jt.executeQuery(sql);
            if (ri.hasNext()) {
                ResultRecord rr = (ResultRecord) ri.next();
                fieldValue = rr.getString(1);
            }
        }
        catch (SQLException e) {
            LogUtil.getLog(getClass()).error(StrUtil.trace(e));
        }
        
        return fieldValue;
    }    

    /**
     * 当report时，取得用来替换控件的脚本
     * @param ff FormField
     * @return String
     */
    public String getReplaceCtlWithValueScript(FormField ff) {
        String v = "";
        String regionId = StrUtil.getNullStr(ff.getValue());

        if (!regionId.equals("")) {
            try {
                String sql = "select region_name from oa_china_region where region_id=" + regionId;
                JdbcTemplate jt = new JdbcTemplate();
                ResultIterator ri = jt.executeQuery(sql);
                if (ri.hasNext()) {
                    ResultRecord rr = (ResultRecord) ri.next();
                    v = rr.getString(1);
                }
            }
            catch (SQLException e) {
                LogUtil.getLog(getClass()).error(StrUtil.trace(e));
            }
        }
        return "ReplaceCtlWithValue('" + ff.getName() +"', '" + ff.getType() + "','" + v + "');\n";
     }


     /**
      * 获取用来保存宏控件toHtml后的值的表单中的HTML元素中保存的值，生成用以禁用控件的脚本
      * @return String
      */
     public String getDisableCtlScript(FormField ff, String formElementId) {
         String v = "";
         String regionId = StrUtil.getNullStr(ff.getValue());

         if (!regionId.equals("")) {
             try {
                 String sql = "select region_name from oa_china_region where region_id=" + regionId;
                 JdbcTemplate jt = new JdbcTemplate();
                 ResultIterator ri = jt.executeQuery(sql);
                 if (ri.hasNext()) {
                     ResultRecord rr = (ResultRecord) ri.next();
                     v = rr.getString(1);
                 }
             }
             catch (SQLException e) {
                 LogUtil.getLog(getClass()).error(StrUtil.trace(e));
             }
        }
         String str = "DisableCtl('" + ff.getName() + "', '" + ff.getType() +
                 "','" + v + "','" + regionId + "');\n";

         return str;
    }
     /**
      * 根据名称取值，用于导入Excel数据
      * @return
      */    
 	public String getValueByName(FormField ff, String name) {
 		String fieldValue="";
        try {
            String sql = "select region_id from oa_china_region where region_name='" + name + "'"; 
            JdbcTemplate jt = new JdbcTemplate();
            ResultIterator ri = jt.executeQuery(sql);
            if (ri.hasNext()) {
                ResultRecord rr = (ResultRecord) ri.next();
                fieldValue = rr.getString(1);
            }
        }
        catch (SQLException e) {
            LogUtil.getLog(getClass()).error(StrUtil.trace(e));
        }
        
        return fieldValue;
 	}

    public String getControlType() {
        return "text";
    }

    public String getControlValue(String userName, FormField ff) {
        return ff.getValue();
    }

    public String getControlText(String userName, FormField ff) {
    	String fieldValue = ff.getValue();
        if (fieldValue==null || "".equals(fieldValue)) {
        	return "";
        }    	
        try {
            String sql = "select region_name from oa_china_region where region_id=" + fieldValue;
            JdbcTemplate jt = new JdbcTemplate();
            ResultIterator ri = jt.executeQuery(sql);
            if (ri.hasNext()) {
                ResultRecord rr = (ResultRecord) ri.next();
                fieldValue = rr.getString(1);
            }
        }
        catch (SQLException e) {
            LogUtil.getLog(getClass()).error(StrUtil.trace(e));
        }
        
        return fieldValue;
    }

    public String getControlOptions(String userName, FormField ff) {
        return "";
    }
}
