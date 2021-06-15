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

    @Override
    public String convertToHTMLCtl(HttpServletRequest request, FormField ff) {
        String str = "";

        String rid = ff.getName();

        str += "<select id='" + ff.getName() + "' name='" + ff.getName() + "' title='" + ff.getTitle() + "'";
        if (ff.isReadonly()) {
            str += " readonly='readonly'";
            str += " onfocus='this.defaultIndex=this.selectedIndex;'";
            str += " onchange='this.selectedIndex=this.defaultIndex;'";
        }
        else {
            str += " onchange=\"if (this.value!='') ajaxShowCityCountry" + rid + "('',this.value)\"";
        }
        str += ">";
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
 	@Override
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

    /**
     * 取得根据名称（而不是值）查询时需用到的SQL语句，如果没有特定的SQL语句，则返回空字符串
     * @param request
     * @param ff 当前被查询的字段
     * @param value
     * @param isBlur 是否模糊查询
     * @return
     */
    @Override
    public String getSqlForQuery(HttpServletRequest request, FormField ff, String value, boolean isBlur) {
        if (isBlur) {
            return "select f." + ff.getName() + " from form_table_" + ff.getFormCode() + " f, oa_china_region r where f.city=r.region_id and r.region_type=2 and r.region_name like " +
                    StrUtil.sqlstr("%" + value + "%");
        }
        else {
            return "select f." + ff.getName() + " from form_table_" + ff.getFormCode() + " f, oa_china_region r where f.city=r.region_id and r.region_type=2 and r.region_name=" +
                    StrUtil.sqlstr(value);
        }
    }
}
