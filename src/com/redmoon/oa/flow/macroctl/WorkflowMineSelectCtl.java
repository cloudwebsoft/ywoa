package com.redmoon.oa.flow.macroctl;

import javax.servlet.http.HttpServletRequest;

import cn.js.fan.util.StrUtil;
import cn.js.fan.web.Global;

import com.redmoon.oa.flow.FormField;
import com.redmoon.oa.flow.WorkflowDb;
import com.redmoon.oa.person.UserDb;

/**
 * 我的流程选择宏控件
 * @author lenovo
 *
 */
public class WorkflowMineSelectCtl  extends AbstractMacroCtl {
	public WorkflowMineSelectCtl() {
	}

	public String convertToHTMLCtl(HttpServletRequest request, FormField ff) {
		String str = "";
		String realName = "";
		if (!StrUtil.getNullStr(ff.getValue()).equals("")) {
			WorkflowDb wf = new WorkflowDb();
			wf = wf.getWorkflowDb(StrUtil.toInt(ff.getValue()));
			
	        com.redmoon.oa.sso.Config config = new com.redmoon.oa.sso.Config();
	        String desKey = config.get("key");			
	        // 以flowId作为值加密
	        String visitKey = cn.js.fan.security.ThreeDesUtil.encrypt2hex(desKey, String.valueOf(wf.getId()));			
	        realName = "<a href='javascript:;' onclick=\"addTab('" + wf.getTitle() + "', '" + request.getContextPath() + "/flow_modify.jsp?flowId=" + wf.getId() + "&visitKey=" + visitKey + "')\">" + wf.getTitle() + "</a>";
			
			// realName = "<a href='" + request.getContextPath() + "/flow_modify.jsp?flowId=" + ff.getValue() + "' target='_blank'>" + wf.getTitle() + "</a>";
		}

		/*
		str += "<input id='" + ff.getName() + "_realshow' name='"
				+ ff.getName() + "_realshow' value='" + realName
				+ "' size=15 readonly>";
		*/

		str += "<span id='" + ff.getName() + "_realshow' name='"
				+ ff.getName() + "_realshow'>" + realName + "</span>";
		
		str += "<input id='" + ff.getName() + "' name='" + ff.getName()
				+ "' value='' type='hidden'>";

		str += "&nbsp;<a id='"
				+ ff.getName()
				+ "_btn' href='javascript:;' onClick=\"openWinWorkflowMineSelect(o('"
				+ ff.getName() + "'))\">选择</a>";
		
		str += "&nbsp;<a id='"
			+ ff.getName()
			+ "_btn_clear' href='javascript:;' onClick=\"o('" + ff.getName() + "_realshow').innerHTML=''; o('" + ff.getName() + "').value='';\">清除</a>";		
		return str;
	}

	/**
	 * 用于列表中显示宏控件的值
	 * 
	 * @param request
	 *            HttpServletRequest
	 * @param ff
	 *            FormField
	 * @param fieldValue
	 *            String
	 * @return String
	 */
	public String converToHtml(HttpServletRequest request, FormField ff,
			String fieldValue) {
		String v = StrUtil.getNullStr(fieldValue);
		if (!v.equals("")) {
			WorkflowDb wf = new WorkflowDb();
			wf = wf.getWorkflowDb(StrUtil.toInt(v, -1));
			
			// WorkflowMgr.getFormAbstractTable中的request参数为null
	        com.redmoon.oa.sso.Config config = new com.redmoon.oa.sso.Config();
	        String desKey = config.get("key");			
	        // 以flowId作为值加密
	        String visitKey = cn.js.fan.security.ThreeDesUtil.encrypt2hex(desKey, String.valueOf(wf.getId()));			
	        String str = "<a href='javascript:;' onclick=\"addTab('" + wf.getTitle() + "', '" + request.getContextPath() + "/flow_modify.jsp?flowId=" + wf.getId() + "&visitKey=" + visitKey + "')\">" + wf.getTitle() + "</a>";
			
			// String str = "<a href=\"javascript:;\" onclick=\"addTab('" + wf.getTitle() + "', '" + Global.getRootPath()
			// 		+ "/flow_modify.jsp?flowId=" + v + "')\">" + wf.getTitle() + "</a>";
			return str;
		} else
			return "";
	}
	
    /**
     * 获取用来保存宏控件toHtml后的值的表单中的HTML元素中保存的值，生成用以禁用控件的脚本
     * @return String
     */
    public String getDisableCtlScript(FormField ff, String formElementId) {
        String str = "$('#" + ff.getName() + "_btn').hide();\r\n";
        str += "$('#" + ff.getName() + "_btn_clear').hide();\r\n";
        // str += super.getDisableCtlScript(ff, formElementId);
        return str;
    }	
	
    /**
     * 当report时，取得用来替换控件的脚本
     * @param ff FormField
     * @return String
     */
    public String getReplaceCtlWithValueScript(FormField ff) {
        String v = "";
        if (ff.getValue() != null && !ff.getValue().equals("")) {
			WorkflowDb wf = new WorkflowDb();
			wf = wf.getWorkflowDb(StrUtil.toInt(ff.getValue(), -1));
			
			// 不能用addTab，因为在ReplaceCtlWithValue方法中会出现JS错误
			// v = "<a href=\"javascript:;\" onclick=\"addTab('" + wf.getTitle() + "', '" + Global.getRootPath()
			// + "/flow_modify.jsp?flowId=" + ff.getValue() + "')\">" + wf.getTitle() + "</a>";
			
			/*
			v = "<a href=\"" + Global.getRootPath()
				+ "/flow_modify.jsp?flowId=" + ff.getValue() + "\" target=_blank>" + wf.getTitle() + "</a>";
			*/
        }
        
        String str = "$('#" + ff.getName() + "_btn').hide();\r\n";
        str += "$('#" + ff.getName() + "_btn_clear').hide();\r\n";
        str += "ReplaceCtlWithValue('" + ff.getName() +"', '" + ff.getType() + "','" + v + "');\n";
        return str;
    }

	public String getControlType() {
		return "";
	}

	public String getControlValue(String userName, FormField ff) {
		return "";
	}

	public String getControlText(String userName, FormField ff) {
		return "";
	}

	public String getControlOptions(String userName, FormField ff) {
		return "";
	}
}