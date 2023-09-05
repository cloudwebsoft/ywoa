package com.redmoon.oa.flow.macroctl;

import java.util.Iterator;
import java.util.Vector;

import javax.servlet.http.HttpServletRequest;

import com.cloudwebsoft.framework.util.LogUtil;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import cn.js.fan.db.SQLFilter;
import cn.js.fan.util.ErrMsgException;
import cn.js.fan.util.StrUtil;

import com.redmoon.oa.flow.FormDb;
import com.redmoon.oa.flow.FormField;
import com.redmoon.oa.pvg.Privilege;
import com.redmoon.oa.visual.FormDAO;

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
public class ProjectListWinCtl extends AbstractMacroCtl {
    public ProjectListWinCtl() {
    }

    /**
     * 用于列表中显示宏控件的值
     * @param request HttpServletRequest
     * @param ff FormField
     * @param fieldValue String
     * @return String
     */
    public String converToHtml(HttpServletRequest request, FormField ff,
                               String fieldValue) {
        String v = StrUtil.getNullStr(fieldValue);
        if (!v.equals("")) {
            FormDAO fdao = getFormDAOOfProject(StrUtil.toLong(v));
            String str = fdao.getFieldValue("name");
            return str;
        } else
            return "";
    }

    /**
     * 当report时，取得用来替换控件的脚本
     * @param ff FormField
     * @return String
     */
    public String getReplaceCtlWithValueScript(FormField ff) {
        String v = "";
        if (ff.getValue() != null && !ff.getValue().equals("")) {
            // LogUtil.getLog(getClass()).info("StrUtil.toInt(v)=" + StrUtil.toInt(v));
            FormDAO fdao = getFormDAOOfProject(StrUtil.toLong(ff.getValue()));
            v = fdao.getFieldValue("name");
        }
        return "ReplaceCtlWithValue('" + ff.getName() + "', '" + ff.getType() +
                "', '" + v + "');\n";
    }
    
    public String getDisableCtlScript(FormField ff, String formElementId) {
        // String str = "DisableCtl('" + ff.getName() + "_realshow', '" + ff.getType() +
        // "','" + "" + "','" + ff.getValue() + "');\n";
        
    	String str = "o('" + ff.getName() + "_realshow').outerHTML=o('" + ff.getName() + "_realshow').value;\n"; 
    	str += "if (o('" + ff.getName() + "_btn')) o('" + ff.getName() + "_btn').outerHTML='';\n";
        str += "o('" + ff.getName() + "').style.display='none';\n";
        // str += "DisableCtl('" + ff.getName() + "', '" + ff.getType() + "', 'ctlValue', 'ctlValueRaw');\n";

        return str;
    }

    public FormDAO getFormDAOOfProject(long id) {
        FormDb fd = new FormDb();
        fd = fd.getFormDb("project");
        FormDAO fdao = new FormDAO(id, fd);
        return fdao;
    }

    /**
     *
     *
     * @param request HttpServletRequest
     * @param ff FormField
     * @return String
     * @todo Implement this com.redmoon.oa.base.IFormMacroCtl method
     */
    public String convertToHTMLCtl(HttpServletRequest request, FormField ff) {
        String str = "";
        String v = "";
        if (!StrUtil.getNullStr(ff.getValue()).equals("")) {
            // LogUtil.getLog(getClass()).info("StrUtil.toInt(ff.getValue())=" + StrUtil.toInt(ff.getValue()));
            FormDAO fdao = getFormDAOOfProject(StrUtil.toLong(ff.getValue()));
            // LogUtil.getLog(getClass()).info("mobile=" + fdao.getFieldValue("mobile"));
            v = fdao.getFieldValue("name");
        }
        str += "<input id='" + ff.getName() + "_realshow' name='" + ff.getName() + "_realshow' value='" + v +
                "' size=15 readonly>";
        str += "<input id='" + ff.getName() + "' name='" + ff.getName() + "' value='' type='hidden'>";
        str +=
                "&nbsp;<input id='" + ff.getName() + "_btn' type=button class=btn value='选择' onClick='openWinProjectList(" +
                ff.getName() + ")'>";
        return str;
    }
     

    public String getControlType() {
        return "select";
    }

    public String getControlValue(String userName, FormField ff) {
    	if(ff.getValue()!=null && !ff.getValue().trim().equals("")){
    		return ff.getValue();
    	}
        return "";
    }

    public String getControlText(String userName, FormField ff) {
    	 String v = StrUtil.getNullStr(ff.getValue());
         if (!v.equals("")) {
        	  FormDAO fdao = getFormDAOOfProject(StrUtil.toLong(v));
              String str = fdao.getFieldValue("name");
              return str;
         }
         return "";
       
    }

    public String getControlOptions(String userName, FormField ff) {
    	//查询我参与的项目并且未完成
    	String sql = "select id from ft_project order by id desc";
    	//String sql = "SELECT P.id FROM ft_project p,ft_project_members m WHERE p.id=m.cws_id and p.status = 0 and m.prj_user=?";
    	JSONArray selects = new JSONArray();
    	FormDAO fdao = new FormDAO();
    	Vector vector;
		try {
			vector = fdao.list("project", sql);
			if(vector != null && vector.size()>0){
				Iterator ir = null;
				ir = vector.iterator();
				while(ir.hasNext()){
					fdao = (FormDAO)ir.next();
					JSONObject select = new JSONObject();
					select.put("value", String.valueOf(fdao.getId()));
					select.put("name",fdao.getFieldValue("name"));//客户名称
					selects.put(select);
				}
			}
		} catch (ErrMsgException e) {
			LogUtil.getLog(ProjectListWinCtl.class).error(e.getMessage());
		} catch (JSONException e) {
			LogUtil.getLog(ProjectListWinCtl.class).error(e.getMessage());
		}
    	
        return selects.toString();
        
    }
    /**
     * 用于模块查询条件
     */
    @Override
    public String convertToHTMLCtlForQuery(HttpServletRequest request, FormField ff) {
    	Privilege prv = new Privilege();
    	String curUser = prv.getUser(request);
    	//查询我参与的项目并且未完成
    	String sql = "select id from ft_project p where exists (select cws_id from ft_project_members where prj_user="
    		+ SQLFilter.sqlstr(curUser)
    		+ "and p.id=cws_id) or cws_creator=" + StrUtil.sqlstr(curUser)	
    		+ " order by id desc";
    	//String sql = "SELECT P.id FROM ft_project p,ft_project_members m WHERE p.id=m.cws_id and p.status = 0 and m.prj_user=?";
    	StringBuffer str = new StringBuffer();
	    str.append("<select id='" + ff.getName() + "' name='" + ff.getName() + "'>");
	    str.append("<option value='' >不限</option>");
    	FormDAO fdao = new FormDAO();
    	Vector vector;
		try {
			vector = fdao.list("project", sql);
			if(vector != null && vector.size()>0){
				
				Iterator ir = null;
				ir = vector.iterator();
				while(ir.hasNext()){
					fdao = (FormDAO)ir.next();
					str.append("<option value='" + String.valueOf(fdao.getId()) + "' " +
	                        ">" +
	                        fdao.getFieldValue("name") +
	                        "</option>");
				}
			}
		} catch (ErrMsgException e) {
			// TODO Auto-generated catch block
			LogUtil.getLog(ProjectListWinCtl.class).error(e.getMessage());
		} 
		str.append("</select>");
        return str.toString();
    }

}
