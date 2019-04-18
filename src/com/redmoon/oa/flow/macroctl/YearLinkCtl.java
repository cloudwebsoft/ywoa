package com.redmoon.oa.flow.macroctl;

import java.util.Calendar;


import javax.servlet.http.HttpServletRequest;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import cn.js.fan.util.StrUtil;

import com.redmoon.oa.flow.FormField;
import com.redmoon.oa.pvg.Privilege;

public class YearLinkCtl extends AbstractMacroCtl {

	@Override
	public String convertToHTMLCtl(HttpServletRequest request, FormField ff) {
	
		StringBuilder sb = new StringBuilder();
		Calendar calendar = Calendar.getInstance();
		int CurrentYear = calendar.get(Calendar.YEAR);//当前年份
		String content = ff.getValue();
		if(ff.isEditable()){//如果是可编辑状态
			sb.append("<select id= '").append(ff.getName()).append("' name='").append(ff.getName()).append("' >");
			//前30年
			 for (int i = 30; i >= 1; i--) {
				 sb.append("<option value='").append(CurrentYear-i).append("' >").append(CurrentYear-i).append("</option>");	
			 }
			 sb.append("<option value='").append(CurrentYear).append("' selected >").append(CurrentYear).append("</option>");
			 //后30年
			 for (int i = 1; i <=30; i++) {
				sb.append("<option value='").append(CurrentYear+i).append("' >").append(CurrentYear+i).append("</option>");	
			 }
			 sb.append("</select>");
		}else{
			if ((content != null) && (!content.equals(""))) {
				sb.append("<span id='" + ff.getName() + "'>").append(content).append("</span>");
			}
		}
		
		return sb.toString();
	}

	@Override
	public String getControlOptions(String arg0, FormField arg1) {
		// TODO Auto-generated method stub
		JSONArray arr = new JSONArray();
		Calendar calendar = Calendar.getInstance();
		int currentYear = calendar.get(Calendar.YEAR);//当前年份
		 try {
			 for (int i = 10; i >= 1; i--) {
				 JSONObject obj = new JSONObject();
				 obj.put("name", currentYear-i);
				 obj.put("value", currentYear-i);
				 arr.put(obj);
			 }
			 for (int i = 1; i <=10; i++) {
				 JSONObject obj = new JSONObject();
				 obj.put("name", currentYear+i);
				 obj.put("value", currentYear+i);
				 arr.put(obj);
			 }
			
		} catch (JSONException e) {
		
		}
			
		
		
		return arr.toString();
	}

	@Override
	public String getControlText(String arg0, FormField ff) {
		// TODO Auto-generated method stub
		String value = StrUtil.getNullStr(ff.getValue());
		return value;
	}

	@Override
	public String getControlType() {
		// TODO Auto-generated method stub
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
    		int currentYear = calendar.get(Calendar.YEAR);//当前年份
            ffNew.setValue(String.valueOf(currentYear));
        }
        // System.out.println(getClass() + " getOuterHTMLOfElementsWithRAWValueAndHTMLValue ffNew=" + ffNew.getValue());
        return super.getOuterHTMLOfElementsWithRAWValueAndHTMLValue(request,
                ffNew);
    }

}
