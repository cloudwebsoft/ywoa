package com.redmoon.oa.flow.macroctl;

import cn.js.fan.util.ErrMsgException;
import cn.js.fan.util.StrUtil;
import com.cloudwebsoft.framework.util.LogUtil;
import com.redmoon.oa.base.IFormDAO;
import com.redmoon.oa.flow.FormDb;
import com.redmoon.oa.flow.FormField;
import com.redmoon.oa.sys.DebugUtil;
import com.redmoon.oa.util.RequestUtil;
import com.redmoon.oa.visual.FormulaResult;
import com.redmoon.oa.visual.FormulaUtil;
import org.apache.commons.lang3.StringUtils;
import org.json.JSONException;
import org.json.JSONObject;

import javax.servlet.http.HttpServletRequest;
import java.util.Iterator;

public class FormulaCtl extends AbstractMacroCtl {
    public FormulaCtl() {
    }

    public String convertToHTMLCtl(HttpServletRequest request, FormField ff) {
    	IFormDAO ifdao;
    	if (formDaoFlow!=null) {
    		ifdao = formDaoFlow;
    	}
    	else {
    		ifdao = formDaoVisual;
    	}
    	// 如果不需要计算value，因为在getSetCtlValueScript、getDisableCtlScript及getReplaceCtlWithValueScript中进行了计算
        // String str = "<input id='" + ff.getName() + "' name='" + ff.getName() + "' value='" + getFormulaValue(ifdao, ff) + "' readonly />";
		String str = "<input id='" + ff.getName() + "' name='" + ff.getName() + "' value='' readonly />";

		int flowId = -1;
		String pageType = (String)request.getAttribute("pageType");
		if ("flow".equals(pageType) || "flowShow".equals(pageType)) {
			flowId = StrUtil.toInt((String) request.getAttribute("cwsId"), -1);
		}

		long id = -1;
		// 模块添加的时候，ifdao为null
		if (ifdao!=null) {
			id = ifdao.getId();
		}
		if (request.getAttribute("isFormulaCtlJS_" + ff.getName()) == null) {
			str += "<script src='" + request.getContextPath()
					+ "/flow/macro/macro_formula_ctl_js.jsp?pageType=" + pageType
					+ "&formCode=" + StrUtil.UrlEncode(ff.getFormCode())
					+ "&fieldName=" + ff.getName() + "&flowId=" + flowId + "&id=" + id + "&isHidden=" + ff.isHidden() + "&editable=" + ff.isEditable()
					+ "'></script>\n";
			request.setAttribute("isFormulaCtlJS_" + ff.getName(), "y");
		}
		return str;
    }

	/**
	 * 取得公式宏控件的值
	 * @param ifdao
	 * @param ff
	 * @return
	 */
	public String getFormulaValue(IFormDAO ifdao, FormField ff) {
    	String desc = ff.getDescription();
		String code = null;
		String params = null;
		try {
			JSONObject json = new JSONObject(desc);
			code = json.getString("code");
			params = json.getString("params");
		} catch (JSONException e) {
			DebugUtil.e(getClass(), "getFormulaValue " + ff.getName() + " " + ff.getTitle(), desc);
			e.printStackTrace();
		}

		// 解析形参params，取值，例：user_name, 12
    	String[] paramAry = StrUtil.split(params, ",");
    	// 当模块添加的时候，ifdao为null
    	if (ifdao!=null) {
	    	for (int i=0; i<paramAry.length; i++) {
	    		// 如果不是数字，也不是字符串
				paramAry[i] = paramAry[i].trim();
	    		if (!StrUtil.isDouble(paramAry[i]) && !paramAry[i].startsWith("\"")) {
	    			// 判断是否为字段
	    			boolean isField = false;
	    			String param = paramAry[i];
	    			if (param.equalsIgnoreCase("id")) {
						isField = true;
						paramAry[i] = String.valueOf(ifdao.getId());
					}
	    			else if (param.equalsIgnoreCase("cws_id")) {
						isField = true;
						paramAry[i] = ifdao.getCwsId();
					}
					else if (param.equalsIgnoreCase("cws_status")) {
						isField = true;
						paramAry[i] = String.valueOf(ifdao.getCwsStatus());
					}
	    			else {
						Iterator ir = ifdao.getFields().iterator();
						while (ir.hasNext()) {
							FormField formField = (FormField) ir.next();
							if (formField.getName().equals(param)) {
								isField = true;
								paramAry[i] = StrUtil.getNullStr(ifdao.getFieldValue(paramAry[i]));
								break;
							}
						}
					}
	    			// 如果是字段，则从ifdao中取值，否则仍保留为原字符串
	    			if (!isField) {
						// DebugUtil.e(getClass(), "getFormulaValue", "公式：" + code + " 参数：" + params + "中的" + paramAry[i] + " 在表单中未找到");
	    				return "公式：" + code + " 参数：" + params + "中的" + paramAry[i] + " 在表单中未找到";
					}
	    		}
	    	}
    	}
    	else {
    		FormDb fd = new FormDb();
    		fd = fd.getFormDb(ff.getFormCode());
    		// 如果fdao为null，则说明是添加模块，如果参数中带有表单域，则不需运算公式，直接返回空值
			for (int i=0; i<paramAry.length; i++) {
				String param = paramAry[i].trim();
				// 如果不是数字
				if (!StrUtil.isDouble(param) && !param.startsWith("\"")) {
					Iterator ir = fd.getFields().iterator();
					while (ir.hasNext()) {
						FormField formField = (FormField)ir.next();
						if (formField.getName().equals(param)) {
							return "";
						}
					}
				}
			}
		}
    	
    	// 生成实参
    	String args = StringUtils.join(paramAry, ",");
    	
    	String formula = "#" + code + "(" + args + ")";
    	// DebugUtil.log(FormulaCtl.class, "getFormulaValue", "formula=" + formula);
    	String val = "";
    	try {
			FormulaResult fr = FormulaUtil.render(formula);
			val = fr.getValue();
		} catch (ErrMsgException e) {
			e.printStackTrace();
		}
    	
    	return val;
    }

    /**
     * 用于模块列表中显示宏控件的值
     * @param request HttpServletRequest
     * @param ff FormField 表单域的描述，其中的value值为空
     * @param fieldValue String 表单域的值
     * @return String
     */
    public String converToHtml(HttpServletRequest request, FormField ff, String fieldValue) {
		if (request == null) {
			return fieldValue;
		}

		String desc = ff.getDescription();
		boolean isAutoWhenList = false;
		try {
			JSONObject json = new JSONObject(desc);
			if (json.has("isAutoWhenList")) {
				isAutoWhenList = json.getBoolean("isAutoWhenList");
				if (!isAutoWhenList) {
					return fieldValue;
				}
			}
		} catch (JSONException e) {
			e.printStackTrace();
		}

		IFormDAO ifdao = RequestUtil.getFormDAO(request);
		return getFormulaValue(ifdao, ff);
    }

    public String getControlType() {
        return "text";
    }

    public String getControlOptions(String userName, FormField ff) {
/*    	IFormDAO ifdao;
    	if (formDaoFlow!=null) {
    		ifdao = formDaoFlow;
    	}
    	else {
    		ifdao = formDaoVisual;
    	}
    	return getFormulaValue(ifdao, ff);*/
		return "";
    }

	@Override
	public String getControlText(String userName, FormField ff) {
    	IFormDAO ifdao;
    	if (formDaoFlow!=null) {
    		ifdao = formDaoFlow;
    	}
    	else {
    		ifdao = formDaoVisual;
    	}
    	return getFormulaValue(ifdao, ff);
	}

	@Override
	public String getControlValue(String userName, FormField ff) {
    	IFormDAO ifdao;
    	if (formDaoFlow!=null) {
    		ifdao = formDaoFlow;
    	}
    	else {
    		ifdao = formDaoVisual;
    	}
    	return getFormulaValue(ifdao, ff);
	}
	
	public String getDisableCtlScript(FormField ff, String formElementId) {
    	IFormDAO ifdao;
    	if (formDaoFlow!=null) {
    		ifdao = formDaoFlow;
    	}
    	else {
    		ifdao = formDaoVisual;
    	}
    	String v = getFormulaValue(ifdao, ff);
		String str = "DisableCtl('" + ff.getName() + "', '" + ff.getType()
				+ "','" + v + "','" + ff.getValue() + "');\n";
		return str;
	}	
	
    public String getReplaceCtlWithValueScript(IFormDAO ifdao, FormField ff) {
    	String v = getFormulaValue(ifdao, ff);    	
        return "ReplaceCtlWithValue('" + ff.getName() + "', '" + ff.getType() +
                "','" + v + "');\n";
    }	
    
	public String getSetCtlValueScript(HttpServletRequest request,
			IFormDAO IFormDao, FormField ff, String formElementId) {
    	String v = getFormulaValue(IFormDao, ff);    			
        String str = "setCtlValue('" + ff.getName() + "', '" +
    			ff.getType() + "', '" + v + "');\n";        	
        return str;
	}    
}