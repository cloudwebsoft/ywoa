package com.redmoon.weixin.util;

import java.util.Comparator;

import com.redmoon.weixin.bean.SortModel;

/**
 * @Description: 
 * @author: 
 * @Date: 2016-8-24下午05:28:29
 */
public class PinyinComparator implements Comparator<SortModel> {

	/**
	 * @Description: 
	 * @param o1
	 * @param o2
	 * @return
	 */
	@Override
	public int compare(SortModel o1, SortModel o2) {
		// TODO Auto-generated method stub
		if (o1.getSortLetters().equals("@")
				|| o2.getSortLetters().equals("#")) {
			return -1;
		} else if (o1.getSortLetters().equals("#")
				|| o2.getSortLetters().equals("@")) {
			return 1;
		} else {
			int res = o1.getSortLetters().compareTo(o2.getSortLetters());
			
			return res;
		}
	}

}
