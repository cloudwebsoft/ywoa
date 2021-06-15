package com.redmoon.oa.flow.macroctl;

import java.util.Calendar;
import java.util.Date;

import javax.servlet.http.HttpServletRequest;

import cn.js.fan.util.DateUtil;
import com.redmoon.oa.visual.SQLBuilder;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import cn.js.fan.util.StrUtil;

import com.redmoon.oa.flow.FormField;

public class MonthLinkCtl extends AbstractMacroCtl{
	@Override
	public String convertToHTMLCtl(HttpServletRequest request, FormField ff) {
		Calendar calendar = Calendar.getInstance();
		int m = calendar.get(Calendar.MONTH) + 1;//当前月份

		StringBuilder sb = new StringBuilder();
		if (ff.isReadonly()) {
			sb.append("<select id='" + ff.getName() + "' name='" + ff.getName() + "' title='" + ff.getTitle() + "' style='background-color:#eeeeee' onfocus='this.defaultIndex=this.selectedIndex;' onchange='this.selectedIndex=this.defaultIndex;'>");
		}
		else {
			sb.append("<select id= '").append(ff.getName()).append("' name='").append(ff.getName()).append("' >");
		}
		sb.append("<option value=''></option>");
		for (int i = 1; i <= 12; i++) {
			if (i == m) {
				sb.append("<option value='").append(i).append("' selected>").append(i).append("</option>");
			} else {
				sb.append("<option value='").append(i).append("' >").append(i).append("</option>");
			}
		}
		sb.append("</select>");
		return sb.toString();
	}

	/**
	 * 将宏控件展开为用于查询的HTML字符串
	 * @param request HttpServletRequest
	 * @param ff FormField
	 * @return String
	 */
	@Override
	public String convertToHTMLCtlForQuery(HttpServletRequest request, FormField ff) {
		if (ff.getCondType().equals(SQLBuilder.COND_TYPE_FUZZY)) {
			return super.convertToHTMLCtlForQuery(request, ff);
		}
		Calendar calendar = Calendar.getInstance();
		int m = calendar.get(Calendar.MONTH) + 1;//当前月份
		StringBuilder sb = new StringBuilder();
		sb.append("<select id= '").append(ff.getName()).append("' name='").append(ff.getName()).append("' >");
		sb.append("<option value=''>无</option>");
		for (int i = 1; i <= 12; i++) {
			if (i == m) {
				sb.append("<option value='").append(i).append("' selected>").append(i).append("</option>");
			} else {
				sb.append("<option value='").append(i).append("' >").append(i).append("</option>");
			}
		}
		sb.append("</select>");
		return sb.toString();
	}

	@Override
	public String getControlOptions(String arg0, FormField arg1) {
		JSONArray arr = new JSONArray();
		 try {
			 for (int i = 1; i <= 12; i++) {
				 JSONObject obj = new JSONObject();
				 obj.put("name", i);
				 obj.put("value",i);
				 arr.put(obj);
			 }
		
		} catch (JSONException e) {
		
		}
		return arr.toString();
	}

	@Override
	public String getControlText(String arg0, FormField ff) {
		String value = StrUtil.getNullStr(ff.getValue());
		return value;
	}

	@Override
	public String getControlType() {
		return "select";
	}

	@Override
	public String getControlValue(String arg0, FormField ff) {
		String value = StrUtil.getNullStr(ff.getValue());
		return value;
	}

    /**
     * 取得用来保存宏控件原始值及toHtml后的值的表单中的HTML元素，通常前者为textarea，后者为span
     * 如果不继承此方法，则该控件的默认值会为空，而不是当前时间，设了selected也没用，会被setCtlValue重置为空
     * @return String
     */
    @Override
	public String getOuterHTMLOfElementsWithRAWValueAndHTMLValue(
            HttpServletRequest request, FormField ff) {
        // 则检查如果没有赋值就赋予其当前年份
        FormField ffNew = new FormField();
        ffNew.setName(ff.getName());
        ffNew.setValue(ff.getValue());
        ffNew.setType(ff.getType());
        ffNew.setFieldType(ff.getFieldType());
 
        // 如果是当前用户宏控件，则检查如果没有赋值就赋予其当前用户名称
        if (StrUtil.getNullStr(ff.getValue()).equals("")) {
    		Calendar calendar = Calendar.getInstance();
    		int currentMonth = calendar.get(Calendar.MONTH)+1;//当前年份
            ffNew.setValue(String.valueOf(currentMonth));
        }
        // System.out.println(getClass() + " getOuterHTMLOfElementsWithRAWValueAndHTMLValue ffNew=" + ffNew.getValue());
        return super.getOuterHTMLOfElementsWithRAWValueAndHTMLValue(request,
                ffNew);
    }

	/**
	 * 取得表单域的类型
	 * @return int
	 */
	@Override
	public int getFieldType(FormField ff) {
		return FormField.FIELD_TYPE_INT;
	}

	/**
	 * 用于流程处理时，生成表单默认值，如基础数据宏控件，取其默认值
	 * @param ff FormField
	 * @return Object
	 */
	@Override
	public Object getValueForCreate(int flowId, FormField ff) {
		int m = DateUtil.getMonth(new Date());
		return new Integer(m + 1);
	}
}
