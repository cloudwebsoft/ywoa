package com.redmoon.forum;

import javax.servlet.http.HttpServletRequest;

import cn.js.fan.util.StrUtil;

public class TagMgr {
	public static String render(HttpServletRequest request, TagDb tag) {
		String tagName = tag.getString("name");
		String color = StrUtil.getNullStr(tag.getString("color"));
		if (!color.equals("")) {
			tagName = "<font color='" + color + "'>" + tagName +  "</font>";
		}
		String size = StrUtil.getNullStr(tag.getString("font_size"));
		if (!size.equals("")) {
			tagName = "<font size='" + size + "'>" + tagName + "</font>";
		}
		if (tag.getInt("is_bold")==1) {
			tagName = "<strong>" + tagName + "</strong>";
		}

		return tagName;
	}

}
