package com.redmoon.oa.flow.macroctl;

import javax.servlet.http.HttpServletRequest;

import org.json.JSONException;
import org.json.JSONObject;

import cn.js.fan.util.StrUtil;

import com.cloudwebsoft.framework.util.LogUtil;
import com.redmoon.kit.util.FileUpload;
import com.redmoon.oa.base.IFormDAO;
import com.redmoon.oa.flow.FormDb;
import com.redmoon.oa.flow.FormField;
import com.redmoon.oa.flow.FormQueryDb;
import com.redmoon.oa.visual.FormDAO;
import com.redmoon.oa.visual.FormDAOMgr;

public class FormQueryFieldSelectCtl extends AbstractMacroCtl {
    public FormQueryFieldSelectCtl() {
        super();
    }

    public FormDAO getFormDAO(String formCode, int id) {
        FormDb fd = new FormDb();
        fd = fd.getFormDb(formCode);
        FormDAO fdao = new FormDAO(id, fd);
        return fdao;
    }

    /**
     *
     *
     * @param request HttpServletRequest
     * @param ff FormField
     * @return String
     */
    @Override
    public String convertToHTMLCtl(HttpServletRequest request, FormField ff) {
        String str = "";
        String v = "";

        LogUtil.getLog(getClass()).info("StrUtil.toInt(ff.getValue())=" + ff.getValue());
        
        String openerFormCode = (String)request.getAttribute("formCode");
        
        if (openerFormCode==null || "".equals(openerFormCode)) {
        	openerFormCode = (String)request.getAttribute("formCodeRelated");
        }
        
        LogUtil.getLog(getClass()).info("convertToHTMLCtl: openerFormCode=" + openerFormCode);        

        String formCode = "";
        String strDesc = ff.getDescription();
        /*
        String[] ary = StrUtil.split(strDesc, ":");
        if (ary.length<3)
            return "格式错误";
        */
        
        JSONObject json = null;
		try {
			json = new JSONObject(strDesc);
	        formCode = json.getString("formCode");
        	LogUtil.getLog(getClass()).info(json.toString() + "--" + strDesc);	        
	        int queryId = -1;	        
	        try {
	        	queryId = StrUtil.toInt(json.getString("queryId"));
	        }
	        catch (JSONException e) {
	        	LogUtil.getLog(getClass()).error(StrUtil.trace(e));
	        	queryId = json.getInt("queryId");
	        	LogUtil.getLog(getClass()).info("queryId=" + queryId);	        	
	        }
	        String byFieldName = json.getString("idField");
	        String showFieldName = json.getString("showField");
	        // 解码，替换%sq %dq，即单引号、双引号
	        // String filter = StrUtil.decodeJSON(json.getString("filter"));

	        if (!StrUtil.getNullStr(ff.getValue()).equals("")) {
	            if (byFieldName.equals("id")) {
	                // FormDAO fdao = getFormDAO(formCode, StrUtil.toInt(ff.getValue()));
	                // v = fdao.getFieldValue(byFieldName);
	            }
	            else {
	                // FormDAOMgr fdm = new FormDAOMgr(formCode);
	                // v = fdm.getFieldValueOfOther(ff.getValue(), byFieldName, showFieldName);
	            }
	            // LogUtil.getLog(getClass()).info("mobile=" + fdao.getFieldValue("mobile"));

	        }
	        
	        FormQueryDb fqd = new FormQueryDb();
	        fqd = fqd.getFormQueryDb(queryId);
	        
	        if (!fqd.isScript()) {
	        	// str += "<input name='" + ff.getName() + "_realshow' value='" + StrUtil.getNullStr(v) + "' size=15 readonly>";
	        }
	        str += "<input name='" + ff.getName() + "' value='' type='text'>";
	        str +=
	                "&nbsp;<input id='" + ff.getName() + "_btn' type=button class=btn value='选择' onClick='openWinQueryFieldList(\"" +
	                ff.getName() + "\", \"" + StrUtil.UrlEncode(openerFormCode) + "\", \"" + ff.getName() + "\", " + fqd.isScript() + ", " + fqd.getId() + ")'>";			
		} catch (JSONException e) {
        	LogUtil.getLog(getClass()).error(StrUtil.trace(e));
			str = "json 格式非法";
		}

        return str;
    }
    
	@Override
    public String getDisableCtlScript(FormField ff, String formElementId) {
        if (ff.getValue() != null && !ff.getValue().equals("") && !ff.getValue().equals(ff.getDefaultValueRaw())) {
        	return super.getDisableCtlScript(ff, formElementId);
    	}
        else {
        	String v = "";
    		String str = "DisableCtl('" + ff.getName() + "', '" + ff.getType()
				+ "','" + "" + "','" + v + "');\n";   
    		return str;        	
        }
	}    
    
    @Override
    public String getReplaceCtlWithValueScript(FormField ff) {
        if (ff.getValue() != null && !ff.getValue().equals("") && !ff.getValue().equals(ff.getDefaultValueRaw())) {
        	return super.getReplaceCtlWithValueScript(ff);
        }
        else {
        	String v = "";
            return "ReplaceCtlWithValue('" + ff.getName() +"', '" + ff.getType() + "','" + v + "');\n";
        }
    }    
    
    public String getSetCtlValueScript(HttpServletRequest request, IFormDAO IFormDao, FormField ff, String formElementId) {
        if (ff.getValue() != null && !ff.getValue().equals("") && !ff.getValue().equals(ff.getDefaultValueRaw())) {
        	return super.getSetCtlValueScript(request, IFormDao, ff, formElementId);
        }
        else {
        	return "";   
        }
    }    

     public String getControlType() {
         return "text";
     }

     public String getControlValue(String userName, FormField ff) {
         return ff.getValue();
     }

     public String getControlText(String userName, FormField ff) {
         return ff.getValue();
     }

     public String getControlOptions(String userName, FormField ff) {
         return "";
     }
 

}
