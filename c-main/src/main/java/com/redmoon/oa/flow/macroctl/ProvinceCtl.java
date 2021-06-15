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
 * <p>Description:
 * 生成一个省份选择的下拉框,如果需要关联城市和区县选择框，需要在宏控件编辑窗口中的描述中填上两者的控件标识，如：city,country
 * 其中city是城市选择框的字段名，country是区县选择框的字段名，两者之间以半角逗号分隔，也可以只有city而没有country）
 * </p>
 * 在Attribute中放province_city_country_id，会因为宏控件解析时顺序问题，可能CityCtl在先，而致获取不到
 * 在visual/get_city_country.jsp中，如果返回<select>...</select>，用outHTML替换，会使得livevaidtion的事件丢失
 * 因cityID在表单中是唯一的，因而可以用作方法名的后缀
 * <p>Copyright: Copyright (c) 2007</p>
 *
 * <p>Company: </p>
 *
 * @author not attributable
 * @version 1.0
 */
public class ProvinceCtl extends AbstractMacroCtl {
    public ProvinceCtl() {
    }

    @Override
    public String convertToHTMLCtl(HttpServletRequest request, FormField ff) {
        String relateStr = ff.getDefaultValueRaw();
        if ("".equals(relateStr)) {
            relateStr = ff.getDescription();
        }
        String[] ary = StrUtil.split(relateStr, ",");
        boolean isCity = false, isCountry = false;

        String cityId = "", countryId = "";
        if (ary!=null) {
            if (ary.length >= 1) {
                isCity = true;
                cityId = ary[0];
            }
            if (ary.length == 2) {
                isCountry = true;
                countryId = ary[1];
            }
        }

        String str = "";

        String rid = cityId; // RandomSecquenceCreator.getId(6);
        // 传递参数
        // request.setAttribute("province_city_country_id", rid);

        // System.out.println(getClass() + " " + (String)request.getAttribute("province_city_country_id"));

        str += "<script>\n";

        str += "var errFuncCityCountry" + rid + " = function(response) {\n";
        str += "    alert(response.responseText);\n";
        str += "}\n";

        str += "function doGetCityCountry" + rid + "(response) {\n";
        str += "if (response.responseText.trim()=='') return;\n";
        str += "var ary = response.responseText.trim().split('|');\n";
        if (isCity) {
            str += "if (o('" + cityId + "')==null) alert('缺少城市输入框');\n";
            // str += "else if (ary[0]!='') o('" + cityId + "').outerHTML=ary[0];\n";
            str += "else if (ary[0]!='') $('#" + cityId + "').html(ary[0]);\n";
        }
        if (isCountry) {
            // str += "alert(ary[1]);";

            str += "if (o('" + countryId + "')==null) alert('缺少区县输入框');\n";
            // str += "else o('" + countryId + "').outerHTML=ary[1].trim();\n";
            str += "else $('#" + countryId + "').html(ary[1].trim());\n";
        }
        str += "}\n";

        str += "function ajaxShowCityCountry" + rid + "(province,city) {\n";
        str += "var str = 'rid=" + rid + "&cityId=" + cityId + "&countryId=" + countryId + "&isCity=" + isCity + "&isCountry=" + isCountry + "&province=' + province + '&city=' + city;\n";
        str += "var myAjax = new cwAjax.Request(\n ";
        str += "        '" + request.getContextPath() + "/visual/get_city_country.jsp',\n";
        str += "        {\n";
        str += "                method:'post',\n";
        str += "                parameters:str,\n";
        str += "                onComplete:doGetCityCountry" + rid + ",\n";
        str += "                onError:errFuncCityCountry" + rid + "\n";
        str += "        }\n";
	    str += ");";
        str += "}\n";
        /*
        str += "function window_onload() {\n";
        str += "ajaxShowCityCountry('" + ff.getValue() + "','')\n";
        str += "}\n";

        str += "if (document.all) {\n";
        str += "window.attachEvent('onload', window_onload);\n";
        str += "}\n";
        str += "else\n";
        str += "window.addEventListener('load', window_onload, false);\n";
        */

        str += "</script>\n";

        str += "<select id='" + ff.getName() + "' name='" + ff.getName() + "' title='" + ff.getTitle() + "'";
        if (ff.isReadonly()) {
            str += " readonly='readonly'";
            str += " onfocus='this.defaultIndex=this.selectedIndex;'";
            str += " onchange='this.selectedIndex=this.defaultIndex;'";
        }
        else {
            str += " onchange=\"ajaxShowCityCountry" + rid + "(this.value, '')\"";
        }
        str += ">";
        str += "<option value=''>无</option>";
        String sql = "select region_id,region_name from oa_china_region where region_type=1 order by region_id";
        try {
            JdbcTemplate jt = new JdbcTemplate();
            ResultIterator ri = jt.executeQuery(sql);
            while (ri.hasNext()) {
                ResultRecord rr = (ResultRecord) ri.next();
                str += "<option value='" + rr.getInt(1) + "'>" + rr.getString(2) + "</option>";
            }
        }
        catch (SQLException e) {
            LogUtil.getLog(getClass()).error(StrUtil.trace(e));
        }
        str += "</select>";

        return str;
    }

    @Override
    public String convertToHTMLCtlForQuery(HttpServletRequest request, FormField ff) {
        String str = "";
        str += "<select id='" + ff.getName() + "' name='" + ff.getName() + "'>";
        str += "<option value=''>无</option>";
        String sql = "select region_id,region_name from oa_china_region where region_type=1 order by region_id";
        try {
            JdbcTemplate jt = new JdbcTemplate();
            ResultIterator ri = jt.executeQuery(sql);
            while (ri.hasNext()) {
                ResultRecord rr = (ResultRecord) ri.next();
                str += "<option value='" + rr.getInt(1) + "'>" + rr.getString(2) +
                        "</option>";
            }
        }
        catch (SQLException e) {
            LogUtil.getLog(getClass()).error(StrUtil.trace(e));
        }
        str += "</select>";
        return str;
    }

    /**
     * 用于列表中显示宏控件的值
     * @param request HttpServletRequest
     * @param ff FormField
     * @param fieldValue String
     * @return String
     */
    public String converToHtml(HttpServletRequest request, FormField ff, String fieldValue) {
        String regionId = StrUtil.getNullStr(fieldValue);
        String name = "";

        if (!regionId.equals("")) {
            try {
                String sql = "select region_name from oa_china_region where region_id=" + regionId;
                JdbcTemplate jt = new JdbcTemplate();
                ResultIterator ri = jt.executeQuery(sql);
                if (ri.hasNext()) {
                    ResultRecord rr = (ResultRecord) ri.next();
                    name = rr.getString(1);
                }
            }
            catch (SQLException e) {
                LogUtil.getLog(getClass()).error(StrUtil.trace(e));
            }
        }

        return name;
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
 		String fieldValue = "";
        if (name==null || "".equals(name)) {
        	return "";
        }    	
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
            return "select f." + ff.getName() + " from form_table_" + ff.getFormCode() + " f, oa_china_region r where f.city=r.region_id and r.region_type=1 and r.region_name like " +
                    StrUtil.sqlstr("%" + value + "%");
        }
        else {
            return "select f." + ff.getName() + " from form_table_" + ff.getFormCode() + " f, oa_china_region r where f.city=r.region_id and r.region_type=1 and r.region_name=" +
                    StrUtil.sqlstr(value);
        }
    }

}
