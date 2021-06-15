package com.redmoon.oa.flow;

import java.util.Iterator;

import javax.servlet.http.HttpServletRequest;

import cn.js.fan.util.ErrMsgException;
import cn.js.fan.util.ParamUtil;
import cn.js.fan.util.ResKeyException;

import cn.js.fan.util.StrUtil;
import com.cloudwebsoft.framework.db.JdbcTemplate;
import com.redmoon.oa.kernel.License;
import com.redmoon.oa.pvg.Privilege;
import com.redmoon.oa.visual.ModuleSetupDb;

public class FormViewMgr {

    public synchronized boolean create(HttpServletRequest request) throws
    		ErrMsgException, ResKeyException {
		// 许可性验证
		License.getInstance().validate(request);
		Privilege pvg = new Privilege();
		String userName = pvg.getUser(request);
		
		String name = ParamUtil.get(request, "name");
		String formCode = ParamUtil.get(request, "formCode");
		String content = ParamUtil.get(request, "content");
		boolean hasAttachment = ParamUtil.getInt(request, "hasAttachment")==1;
		
		String ieVersion = ParamUtil.get(request, "ieVersion");
		int kind = ParamUtil.getInt(request, "kind");
		
		FormParser fp = new FormParser();
		String form = fp.generateView(content, ieVersion, formCode);
		
		FormViewDb fvd = new FormViewDb();
		return fvd.create(new JdbcTemplate(), new Object[]{formCode,name,content,new Integer(kind), new Integer(hasAttachment?1:0),userName,new java.util.Date(),ieVersion, form});
	}
    
    public synchronized boolean modify(HttpServletRequest request) throws
		ErrMsgException, ResKeyException {
		// 许可性验证
		License.getInstance().validate(request);
		Privilege pvg = new Privilege();
		String userName = pvg.getUser(request);
		
		String name = ParamUtil.get(request, "name");
		String content = ParamUtil.get(request, "content");
		boolean hasAttachment = ParamUtil.getInt(request, "hasAttachment")==1;
		
		String ieVersion = ParamUtil.get(request, "ieVersion");
		int kind = ParamUtil.getInt(request, "kind");
		
		int id = ParamUtil.getInt(request, "id");
		FormViewDb fvd = new FormViewDb();
		fvd = fvd.getFormViewDb(id);
		
		FormParser fp = new FormParser();
		String form = fp.generateView(content, ieVersion, fvd.getString("form_code"));
		
		fvd.set("name", name);
		fvd.set("content", content);
		fvd.set("kind", new Integer(kind));
		fvd.set("has_attachment", new Integer(hasAttachment?1:0));
		fvd.set("user_name", userName);
		fvd.set("modify_date", new java.util.Date());
		fvd.set("ie_version", ieVersion);
		fvd.set("form", form);
		
		return fvd.save();
	}    
    
    public synchronized boolean del(HttpServletRequest request) throws
            ErrMsgException, ResKeyException {
    	int id = ParamUtil.getInt(request, "id");

        FormViewDb fvd = new FormViewDb();
        fvd = fvd.getFormViewDb(id);

        return fvd.del();
    }

	/**
	 * 为模块生成默认视图内容
	 * 当在嵌套表格中解析视图中的字段时，如果未指定视图时，则通过模块列的设置生成默认视图
	 * @param msd
	 * @return
	 * @throws ErrMsgException
	 */
	public static String makeViewContent(ModuleSetupDb msd) throws ErrMsgException {
		String[] fields = msd.getColAry(false, "list_field");
		if (fields == null || fields.length == 0) {
			throw new ErrMsgException("显示列未配置！");
		}

		String[] fieldsTitle = msd.getColAry(false, "list_field_title");
		String[] fieldsWidth = msd.getColAry(false, "list_field_width");

		String formCode = msd.getString("form_code");
		FormDb fd = new FormDb();
		fd = fd.getFormDb(formCode);
		StringBuffer sb = new StringBuffer();
		sb.append("<table class=\"tabStyle_8\" style=\"border-collapse: collapse;\" data-sort=\"sortDisabled\">");
		sb.append("<tbody>");
		sb.append("<tr>");
		for (int i = 0; i < fields.length; i++) {
			String fieldName = fields[i];
			FormField ff = fd.getFormField(fieldName);
			// 忽略映射字段、系统字段等
			if (ff==null) {
				continue;
			}

			String fieldTitle = fieldsTitle[i];
			if ("#".equals(fieldTitle)) {
				fieldTitle = ff.getTitle();
			}

			String w = fieldsWidth[i];
			int wid = StrUtil.toInt(w, 50);
			if (w.indexOf("%")==w.length()-1) {
				w = w.substring(0, w.length()-1);
				wid = 800*StrUtil.toInt(w, 20)/100;
			}
			sb.append("<td width='" + wid + "'>");
			sb.append(fieldTitle);
			sb.append("</td>");
		}
		sb.append("</tr>");
		sb.append("<tr>");
		for (int i = 0; i < fields.length; i++) {
			String fieldName = fields[i];
			FormField ff = fd.getFormField(fieldName);
			// 忽略映射字段、系统字段等
			if (ff==null) {
				continue;
			}
			sb.append("<td>");
			sb.append("<input name=\"" + fieldName + "\" title=\"" + ff.getTitle() + "\" type=\"text\" value=\"" + ff.getTitle() + "\"/>");
			sb.append("</td>");
		}
		sb.append("</tr>");
		sb.append("</tbody>");
		sb.append("</table>");
		return sb.toString();
	}

	/**
	 * 为makeViewContent中生成的视图内容，生成相应的表单内容
	 * @param formCode
	 * @param content
	 * @return
	 */
	public static String makeViewForm(String formCode, String content) {
		FormParser fp = new FormParser();
		String ieVersion = "11";
		return fp.generateView(content, ieVersion, formCode);
	}
}
