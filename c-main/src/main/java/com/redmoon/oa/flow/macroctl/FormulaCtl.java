package com.redmoon.oa.flow.macroctl;

import cn.js.fan.db.ResultIterator;
import cn.js.fan.db.ResultRecord;
import cn.js.fan.util.ErrMsgException;
import cn.js.fan.util.StrUtil;
import com.cloudwebsoft.framework.db.JdbcTemplate;
import com.redmoon.kit.util.FileUpload;
import com.redmoon.oa.base.IFormDAO;
import com.redmoon.oa.flow.FormDb;
import com.redmoon.oa.flow.FormField;
import com.redmoon.oa.sys.DebugUtil;
import com.redmoon.oa.util.RequestUtil;
import com.redmoon.oa.visual.FormDAO;
import com.redmoon.oa.visual.Formula;
import com.redmoon.oa.visual.FormulaResult;
import com.redmoon.oa.visual.FormulaUtil;
import org.apache.commons.lang3.StringUtils;
import org.json.JSONException;
import org.json.JSONObject;

import javax.servlet.http.HttpServletRequest;
import java.sql.SQLException;
import java.util.Iterator;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class FormulaCtl extends AbstractMacroCtl {
    public FormulaCtl() {
    }

    @Override
	public String convertToHTMLCtl(HttpServletRequest request, FormField ff) {
    	// 如果不需要计算value，因为在getSetCtlValueScript、getDisableCtlScript及getReplaceCtlWithValueScript中进行了计算
        // String str = "<input id='" + ff.getName() + "' name='" + ff.getName() + "' value='" + getFormulaValue(ifdao, ff) + "' readonly />";
		String str = "<input id='" + ff.getName() + "' name='" + ff.getName() + "' value='' style='width:" + ff.getCssWidth() + "' readonly />";

		int flowId = -1;
		String pageType = StrUtil.getNullStr((String)request.getAttribute("pageType"));
		if ("flow".equals(pageType) || "flowShow".equals(pageType)) {
			flowId = StrUtil.toInt((String) request.getAttribute("cwsId"), -1);
		}

		long id = -1;
		// 模块添加的时候，ifdao为null
		if (iFormDAO!=null) {
			id = iFormDAO.getId();
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
	 * 取得函数宏控件的值
	 * @param ifdao
	 * @param ff
	 * @return
	 */
	public String getFormulaValue(IFormDAO ifdao, FormField ff) {
		// 当在nest_table_view.jsp中生成辅助表格行时，ifdao为null
		if (ifdao == null) {
			return "";
		}
		FormDb fd = null;
		if (ifdao!=null) {
			fd = ifdao.getFormDb();
		}
		else {
			fd = new FormDb(ff.getFormCode());
		}
		return getFormulaValue(ifdao, fd, null, ff);
    }

	/**
	 * 取得函数宏控件的值
	 * @param fu
	 * @param ff
	 * @return
	 */
	public String getFormulaValue(IFormDAO ifdao, FormDb fd, FileUpload fu, FormField ff) {
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

		// 解析形参params，并取得其值，例：user_name, "12,15,16"
		// 先将其中的,号改为%co，以免在split的时候出现问题
		Pattern p = Pattern.compile("\"(.*?)\"",
				Pattern.DOTALL | Pattern.CASE_INSENSITIVE);
		Matcher m = p.matcher(params);
		StringBuffer sb = new StringBuffer();
		while (m.find()) {
			String str = m.group(1);

			// 将其中的逗号改为%co
			String patternStr = ","; //
			String replacementStr = "%co";
			Pattern pattern = Pattern.compile(patternStr);
			Matcher matcher = pattern.matcher(str);
			str = matcher.replaceAll(replacementStr);

			m.appendReplacement(sb, "\"" + str + "\"");
		}
		m.appendTail(sb);

		String[] paramAry = StrUtil.split(sb.toString(), ",");
		// split后将字符串中的逗号还原
		if (paramAry!=null) {
			for (int i = 0; i < paramAry.length; i++) {
				paramAry[i] = paramAry[i].replaceAll("%co", ",").trim();
			}
		}

		for (int i = 0; i < paramAry.length; i++) {
			paramAry[i] = paramAry[i].trim();
			// 如果不是数字，也不是字符串，则检查是否为表单域
			if (!StrUtil.isDouble(paramAry[i]) && !paramAry[i].startsWith("\"")) {
				// 判断是否为字段
				boolean isField = false;
				String param = paramAry[i];
				if ("id".equalsIgnoreCase(param)) {
					isField = true;
					if (ifdao!=null) {
						paramAry[i] = String.valueOf(ifdao.getId());
					}
				}
				else if ("cws_id".equalsIgnoreCase(param)) {
					isField = true;
					if (ifdao!=null) {
						paramAry[i] = ifdao.getCwsId();
					}
				}
				else if ("cws_status".equalsIgnoreCase(param)) {
					isField = true;
					if (ifdao!=null) {
						paramAry[i] = String.valueOf(ifdao.getCwsStatus());
					}
				}
				else if ("formCode".equalsIgnoreCase(param)) {
					isField = true;
					paramAry[i] = fd.getCode();
				}
				else if ("cws_quote_id".equalsIgnoreCase(param)) {
					isField = true;
					if (ifdao!=null) {
						paramAry[i] = String.valueOf(ifdao.getCwsQuoteId());
					}
				}
				else if ("cws_quote_form".equalsIgnoreCase(param)) {
					isField = true;
					if (ifdao != null) {
						paramAry[i] = ifdao.getCwsQuoteForm();
					}
				}
				else {
					for (FormField formField : fd.getFields()) {
						if (formField.getName().equals(param)) {
							isField = true;
							if (fu != null) {
								paramAry[i] = StrUtil.getNullStr(fu.getFieldValue(paramAry[i]));
							} else if (ifdao != null) {
								paramAry[i] = StrUtil.getNullStr(ifdao.getFieldValue(paramAry[i]));
							}
							break;
						}
					}
				}
				// 如果是字段，则从ifdao中取值，否则仍保留为原字符串
				if (!isField) {
					DebugUtil.i(getClass(), "getFormulaValue", "函数：" + code + " 参数：" + params + "中的" + paramAry[i] + " 在表单中未找到，将被视为字符串");
					// return "函数：" + code + " 参数：" + params + "中的" + paramAry[i] + " 在表单中未找到";
				}
			}
		}

		// 生成实参
		String args = StringUtils.join(paramAry, ",");

		String formula = "#" + code + "(" + args + ")";
		String val = "";
		try {
			// ThreadContext.setFileUpload(fu);
			FormulaResult fr = FormulaUtil.render(formula);
			val = fr.getValue();
		} catch (ErrMsgException e) {
			e.printStackTrace();
		}

		DebugUtil.log(FormulaCtl.class, "getFormulaValue", "字段：" + ff.getName() + " " + ff.getTitle() + " val=" + val + " formula=" + formula);

		return val;
	}

    /**
     * 用于模块列表中显示宏控件的值
     * @param request HttpServletRequest
     * @param ff FormField 表单域的描述，其中的value值为空
     * @param fieldValue String 表单域的值
     * @return String
     */
    @Override
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
		// 如果是嵌入在流程中的脚本查询，则ifdao为null
		if (ifdao == null) {
			return fieldValue;
		}
		return getFormulaValue(ifdao, ff);
    }

    @Override
	public String getControlType() {
        return "text";
    }

    @Override
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
    	return getFormulaValue(iFormDAO, ff);
	}

	@Override
	public String getControlValue(String userName, FormField ff) {
    	return getFormulaValue(iFormDAO, ff);
	}
	
	@Override
	public String getDisableCtlScript(FormField ff, String formElementId) {
    	String v;
    	if (iFormDAO != null) {
			v = getFormulaValue(iFormDAO, ff);
		}
    	else {
    		// 当在nest_table_view.jsp中生成辅助表格行时，会用到getDisableCtlScript，此时ifdao为null
    		v = "";
		}
		String str = "DisableCtl('" + ff.getName() + "', '" + ff.getType() + "','" + v + "','" + ff.getValue() + "');\n";
		return str;
	}	
	
    @Override
	public String getReplaceCtlWithValueScript(IFormDAO ifdao, FormField ff) {
    	String v = getFormulaValue(ifdao, ff);    	
        return "ReplaceCtlWithValue('" + ff.getName() + "', '" + ff.getType() + "','" + v + "');\n";
    }
    
	@Override
	public String getSetCtlValueScript(HttpServletRequest request,
									   IFormDAO IFormDao, FormField ff, String formElementId) {
    	String v = getFormulaValue(IFormDao, ff);
		return "setCtlValue('" + ff.getName() + "', '" + ff.getType() + "', '" + v + "', '" + ff.getFormCode() + "');\n";
	}

	@Override
	public Object getValueForCreate(int flowId, FormField ff) {
    	// 为流程中自动生成值，如果为数值型则为0.0，否则如果是空字符串会报错：Incorrect integer value: '' for column 'ts' at row 1
		String desc = ff.getDescription();
		try {
			JSONObject json = new JSONObject(desc);
			String formulaCode = json.getString("code");
			FormulaUtil formulaUtil = new FormulaUtil();
			Formula formula = formulaUtil.getFormula(new JdbcTemplate(), formulaCode);
			// 使字段类型为函数中所设的数据类型
			ff.setFieldType(formula.getFieldType());
			if (FormField.isNumeric(ff.getFieldType())) {
				ff.setValue("0.0");
			}
			else {
				ff.setValue("");
			}
		} catch (JSONException | ErrMsgException ex) {
			ex.printStackTrace();
		}
		return ff.getValue();
	}

	@Override
	public Object getValueForCreate(FormField ff, FileUpload fu, FormDb fd) {
		return getFormulaValue(null, fd, fu, ff);
	}

	@Override
	public Object getValueForSave(FormField ff, int flowId, FormDb fd, FileUpload fu) {
		com.redmoon.oa.flow.FormDAO fdao = new com.redmoon.oa.flow.FormDAO();
		fdao = fdao.getFormDAO(flowId, fd);
		return getFormulaValue(fdao, fdao.getFormDb(), fu, ff);
	}

	@Override
	public Object getValueForSave(FormField ff, FormDb fd, long formDAOId, FileUpload fu) {
    	// 在保存值的时候，再计算一次，以免前台因为某些字段没有联动致保存时修改的值不对，如：项目推进遇阻原因，因其参数中的相fields为字符串，中间以逗号分隔，前台不会有事件产生
		FormDAO fdao = new FormDAO();
		fdao = fdao.getFormDAO(formDAOId, fd);
		return getFormulaValue(fdao, fdao.getFormDb(), fu, ff);
	}

	@Override
	public int getFieldType(FormField ff) {
		if (ff.getFieldType() != FormField.FIELD_TYPE_VARCHAR) {
			return ff.getFieldType();
		}
		else if (!StringUtils.isEmpty(ff.getRule())) {
			// 如果规则不为空，则说明是6.0以后的新版
			return ff.getFieldType();
		}

    	String desc = ff.getDescription();
		JSONObject json;
		try {
			json = new JSONObject(desc);
			return FormulaCtl.getFormulaType(json.getString("code"));
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return FormField.FIELD_TYPE_VARCHAR;
	}
	
	/**
	 * 取得函数的字段类型
	 * @param code
	 * @return
	 */
	public static int getFormulaType(String code) {
		String sql = "select field_type from form_table_formula where code=?";
		JdbcTemplate jt = new JdbcTemplate();
		try {
			ResultIterator ri = jt.executeQuery(sql, new Object[]{code});
			if (ri.hasNext()) {
				ResultRecord rr = (ResultRecord)ri.next();
				return StrUtil.toInt(rr.getString(1), -1);
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return -1;
	}	
}