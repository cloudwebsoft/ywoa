package com.redmoon.oa.flow.macroctl;
import cn.js.fan.util.StrUtil;
import com.cloudweb.oa.entity.Post;
import com.cloudweb.oa.entity.PostUser;
import com.cloudweb.oa.service.IPostService;
import com.cloudweb.oa.service.IPostUserService;
import com.cloudweb.oa.utils.ConstUtil;
import com.cloudweb.oa.utils.SpringUtil;
import com.redmoon.oa.pvg.Privilege;
import javax.servlet.http.HttpServletRequest;
import com.redmoon.oa.base.IFormDAO;
import com.redmoon.oa.flow.FormField;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.List;

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
public class CurUserPostSelectCtl extends AbstractMacroCtl {
	public CurUserPostSelectCtl() {
	}

	@Override
	public String convertToHTMLCtl(HttpServletRequest request, FormField ff) {
		String str = "";
		String style = "";
		if (!"".equals(ff.getCssWidth())) {
			style = "style='width:" + ff.getCssWidth() + "'";
		}
		else {
			style = "style='width:150px'";
		}

		if (ff.isReadonly()) {
			str = "<select id='" + ff.getName() + "' name='" + ff.getName() + "' title='" + ff.getTitle() + "' " + style + " onfocus='this.defaultIndex=this.selectedIndex;' onchange='this.selectedIndex=this.defaultIndex;'>";
		}
		else {
			str += "<select id='" + ff.getName() + "' name='" + ff.getName() + "' title='" + ff.getTitle() + "' " + style + ">";
		}
		str += "<option value=''>" + ConstUtil.NONE + "</option>";

		String userName = SpringUtil.getUserName();
		IPostUserService postUserService = SpringUtil.getBean(IPostUserService.class);
		List<PostUser> list = postUserService.listByUserName(userName);
		for (PostUser postUser : list) {
			str += "<option value='" + postUser.getPostId() + "'>" + getPostNameById(postUser.getPostId()) + "</option>";
		}

		str += "</select>";

		/*StringBuilder sb = new StringBuilder();
		int postId = 0;
		String value = StrUtil.getNullStr(ff.getValue());
		if("".equals(value)){
			Privilege privilege = new Privilege();
			String userName = privilege.getUser(request);
			IPostUserService postUserService = SpringUtil.getBean(IPostUserService.class);
			PostUser postUser = postUserService.getPostUserByUserName(userName);
			if(postUser!=null){
				postId = postUser.getPostId();
			}
		}else{
			postId = Integer.parseInt(value);
		}
		
		if(postId != 0){
			IPostService postService = SpringUtil.getBean(IPostService.class);
			Post post = postService.getById(postId);
			if(post != null){
				String pName = post.getName();
				sb.append("<span id='" + ff.getName() + "_realshow'>").append(pName).append("</span>");
				sb.append("<input id='").append(ff.getName())
						.append("'").append(" name = '").append(ff.getName()).append("'").append(" type='hidden' value='").append(postId).append(
								"' />");
			}
		}else{
			//当没有岗位的时候  默认显示o
			if(ff.isEditable()){
				sb.append("<input id='").append(ff.getName())
				.append("'").append(" name = '").append(ff.getName()).append("'").append(" type='hidden' value='").append(postId).append(
						"' />");
			}
		}*/
		return str;
	}

	@Override
	public String getControlOptions(String userName, FormField ff) {
		IPostUserService postUserService = SpringUtil.getBean(IPostUserService.class);
		List<PostUser> list = postUserService.listByUserName(userName);
		com.alibaba.fastjson.JSONArray ary = new com.alibaba.fastjson.JSONArray();
		com.alibaba.fastjson.JSONObject children = new com.alibaba.fastjson.JSONObject();
		children.put("name", ConstUtil.NONE);
		children.put("value", "");
		ary.add(children);
		for (PostUser postUser : list) {
			children = new com.alibaba.fastjson.JSONObject();
			children.put("name", getPostNameById(postUser.getPostId()));
			children.put("value", postUser.getPostId());
			ary.add(children);
		}
		return ary.toString();
	}

	@Override
	public String getControlText(String userName, FormField ff) {
		int value = StrUtil.toInt(ff.getValue(), -1);
		if(value != -1) {
			return getPostNameById(value);
		} else {
			if (ff.isEditable()) {
				IPostUserService postUserService = SpringUtil.getBean(IPostUserService.class);
				PostUser postUser = postUserService.getPostUserByUserName(userName);
				if (postUser != null) {
					value = postUser.getPostId();
					return getPostNameById(value);
				}
			}
		}
		return "";
	}

	@Override
	public String getControlType() {
		return "select";
	}

	@Override
	public String getControlValue(String userName, FormField ff) {
		String value = StrUtil.getNullStr(ff.getValue());
		if(StrUtil.isEmpty(value)) {
			if (ff.isEditable()) {
				IPostUserService postUserService = SpringUtil.getBean(IPostUserService.class);
				List<PostUser> list = postUserService.listByUserName(userName);
				if (list.size() == 1) {
					PostUser postUser = list.get(0);
					return String.valueOf(postUser.getPostId());
				}
			}
		}
		return value;
	}

	@Override
	public String getSetCtlValueScript(HttpServletRequest request,
									   IFormDAO IFormDao, FormField ff, String formElementId) {
			String value = StrUtil.getNullStr(ff.getValue());
			String str = "";
		if ("".equals(value) && ff.isEditable()) {
			Privilege privilege = new Privilege();
			String userName = privilege.getUser(request);
			IPostUserService postUserService = SpringUtil.getBean(IPostUserService.class);
			List<PostUser> list = postUserService.listByUserName(userName);
			if (list.size() == 1) {
				PostUser postUser = list.get(0);
				if (postUser != null) {
					int postId = postUser.getPostId();
					str += "setCtlValue('" + ff.getName() + "', '" + ff.getType() + "', '" + postId + "');\n";
				}
			}
		} else {
			str += "setCtlValue('" + ff.getName() + "', '"+ff.getType()+"', '" + value + "');\n";
		}
		return str;
	}

	@Override
	public String converToHtml(HttpServletRequest request, FormField ff,
							   String fieldValue) {
		return getPostNameById(StrUtil.toInt(fieldValue, -1));
	}

	@Override
	public String getReplaceCtlWithValueScript(FormField ff) {
		return "ReplaceCtlWithValue('" + ff.getName() + "', '" + ff.getType()
				+ "','" + getPostNameById(StrUtil.toInt(ff.getValue(), -1)) + "');\n";
	}

	public String getPostNameById(int id) {
		String res = "";
		IPostService postService = SpringUtil.getBean(IPostService.class);
		Post post = postService.getById(id);
		if (post != null) {
			res = post.getName();
		}
		return res;
	}
}
