package com.redmoon.oa.video;

import java.util.Comparator;
import java.util.Date;

import org.jdom.Element;

import cn.js.fan.util.DateUtil;

public class TimeComparator implements Comparator<Element> {

	@Override
	public int compare(Element e1, Element e2) {
		String o1_startTime = e1.getChildText("startTime");
		String o2_startTime = e2.getChildText("startTime");
		Date o1_date = DateUtil.parse(o1_startTime.replace("T", " "), "yyyy-MM-dd HH:mm:ss");
		Date o2_date = DateUtil.parse(o2_startTime.replace("T", " "), "yyyy-MM-dd HH:mm:ss");
		int result = 0;
		if (o1_date.before(o2_date)){
			result = -1;
		} else if (o1_date.after(o2_date)){
			result = 1;
		} else {
			result = 0;
		}
		return result;
	}
}
