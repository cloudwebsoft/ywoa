package com.redmoon.oa.util;

import java.util.ArrayList;
import java.util.Iterator;

import com.redmoon.oa.flow.FormDb;
import com.redmoon.oa.flow.FormField;
import com.redmoon.oa.flow.Leaf;

public class FlowPredefineDirUtil {

	/**
	 * @Description: 流程属性标题替换（字符到中文）
	 * @author: LYL
	 * @Date: 2015-10-13
	 */
	public static String titleReplaceSTC(String title, String code){
		if (code == null || code.equals("")) {
			return "";
		}
		Leaf leaf = new Leaf(code);
		if (leaf == null || !leaf.isLoaded()) {
			return "";
		}
		ArrayList<String> al1 = new ArrayList<String>();
		ArrayList<String> al2 = new ArrayList<String>();
		String formCodes = leaf.getFormCode();
		FormDb fd = new FormDb();
		fd = fd.getFormDb(formCodes);
		Iterator field_vs = fd.getFields().iterator();
		while (field_vs.hasNext()) {
			FormField ffs = (FormField) field_vs.next();
			al1.add("{" + ffs.getName() + "}");
			al2.add(ffs.getTitle());
		}
		if (title.contains("{dept}")) {
			title = title.replace("{dept}", "行政部");
		}
		if (title.contains("{user}")) {
			title = title.replace("{user}", "张三");
		}
		if (title.contains("{date:yyyy-MM-dd}")) {
			title = title.replace("{date:yyyy-MM-dd}", "2001-10-01");
		}
		if (title.contains("{date:MM-dd}")) {
			title = title.replace("{date:MM-dd}", "10-01");
		}
		if (title.contains("{date:MM-dd-yyyy}")) {
			title = title.replace("{date:MM-dd-yyyy}", "10-01-2001");
		}
		for (int i = 0; i < al1.size(); i++) {
			if (title.contains(al1.get(i))) {
				title = title.replace(al1.get(i), al2.get(i));
			}
		}
		return title;
	}
}
