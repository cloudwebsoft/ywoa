package com.redmoon.oa.flow.macroctl;

import java.util.Iterator;
import java.util.Vector;

import cn.js.fan.util.StrUtil;

import com.redmoon.oa.post.PostDb;
import javax.servlet.http.HttpServletRequest;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import com.redmoon.oa.base.IFormDAO;
import com.redmoon.oa.flow.FormField;

/**
 * <p>
 * Title:
 * </p>
 * 
 * <p>
 * Description:
 * </p>
 * 
 * <p>
 * Copyright: Copyright (c) 2006
 * </p>
 * 
 * <p>
 * Company:
 * </p>
 * 
 * @author not attributable
 * @version 1.0
 */
public class PostListSelectCtl extends AbstractMacroCtl {
	public PostListSelectCtl() {
	}

	public String convertToHTMLCtl(HttpServletRequest request, FormField ff) {
		StringBuilder sb = new StringBuilder();
		if (ff.isEditable()) {
			sb.append("<select name=").append(StrUtil.sqlstr(ff.getName()))
					.append(" id=").append(StrUtil.sqlstr(ff.getName()))
					.append(" >");
			sb.append("<option value = ''>请选择</option>");
			PostDb postDb = new PostDb();
			Vector<PostDb> vec = postDb.list();
			Iterator<PostDb> it = vec.iterator();
			while (it.hasNext()) {
				PostDb pd = it.next();
				if (pd == null || !pd.isLoaded()) {
					continue;
				}
				int postId = pd.getInt("id");
				String pName = pd.getString("name");
				sb
						.append("<option value = '")
						.append(postId)
						.append("'")
						.append(" ")
						.append(
								StrUtil.getNullStr(ff.getValue()).equals(postId + "") ? "selected"
										: "").append(">");
				sb.append(pName);
				sb.append("</option>");
			}
			sb.append("</select>");

		} else {
			String pName = "";
			try {
				String value = StrUtil.getNullStr(ff.getValue());
				int post_id = 0;
				if (!value.equals("")) {
					post_id = Integer.parseInt(value);
					PostDb pd = new PostDb();
					pd = pd.getPostDb(post_id);
					pName = pd.getString("name");

				}
				sb.append("<span>").append(pName).append("</span>");
				sb.append("<input type='hidden' id='").append(ff.getName())
						.append("'").append(" value='").append(ff.getValue())
						.append("' />");

			} catch (java.lang.NumberFormatException e) {

			}

		}

		return sb.toString();
	}

	@Override
	public String getControlOptions(String userName, FormField arg1) {
		// TODO Auto-generated method stub
		PostDb postDb = new PostDb();
		Vector<PostDb> vec = postDb.list();
		Iterator<PostDb> it = vec.iterator();
		JSONArray selects = new JSONArray();
		while (it.hasNext()) {
			JSONObject select = new JSONObject();
			PostDb pd = it.next();
			int postId = pd.getInt("id");
			String pName = pd.getString("name");
			try {
				select.put("name", pName);
				select.put("value", String.valueOf(postId));
				selects.put(select);
			} catch (JSONException ex) {

			}
		}
		return selects.toString();
	}

	@Override
	public String getControlText(String arg0, FormField ff) {
		// TODO Auto-generated method stub
		String v = StrUtil.getNullStr(ff.getValue());
		return postNameById(v);
	}

	@Override
	public String getControlType() {
		// TODO Auto-generated method stub
		return "select";
	}

	@Override
	public String getControlValue(String arg0, FormField ff) {
		// TODO Auto-generated method stub
		return ff.getValue();
	}

	public String getSetCtlValueScript(HttpServletRequest request,
			IFormDAO IFormDao, FormField ff, String formElementId) {
		return "";

	}

	/**
	 * @Description:
	 * @param request
	 * @param ff
	 * @param fieldValue
	 * @return
	 */
	public String converToHtml(HttpServletRequest request, FormField ff,
			String fieldValue) {
		return postNameById(fieldValue);

	}

	public String getReplaceCtlWithValueScript(FormField ff) {

		return "ReplaceCtlWithValue('" + ff.getName() + "', '" + ff.getType()
				+ "','" + postNameById(ff.getValue()) + "');\n";
	}

	public String postNameById(String id) {
		String res = "";
		try {
			if (id != null && !id.equals("")) {
				PostDb postDb = new PostDb();
				postDb = postDb.getPostDb(Integer.parseInt(id));
				if (postDb != null && postDb.isLoaded()) {
					res = postDb.getString("name");
				}
			}

		} catch (java.lang.NumberFormatException e) {

		}

		return res;

	}

}
