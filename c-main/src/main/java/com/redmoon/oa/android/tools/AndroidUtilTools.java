package com.redmoon.oa.android.tools;

import java.sql.SQLException;
import java.text.DecimalFormat;

import cn.js.fan.db.ResultIterator;
import cn.js.fan.db.ResultRecord;

import com.cloudwebsoft.framework.db.JdbcTemplate;

public class AndroidUtilTools {
	public static int getNumberOfdecimal(double v) {
		int i = 0;
		String numb = String.valueOf(v);
		int index = numb.lastIndexOf(".");
		if (index == -1) {
			return 0;
		}
		i = numb.substring(index + 1, numb.length()).length();
		return i;

	}

	public static String round(double v, int scale, boolean isRoundTo5) {
		int decimal = AndroidUtilTools.getNumberOfdecimal(v);
		String temp = "";
		if (decimal == 0) {
			for (int i = 0; i < scale; i++) {
				temp = (new StringBuilder(String.valueOf(temp))).append("0")
						.toString();
			}
			StringBuilder sb = new StringBuilder(String.valueOf(v));
			sb = sb.append(".").append(temp);
			return sb.toString();

		} else if (scale - decimal >= 0) {
			for (int i = 0; i < scale - decimal; i++) {
				temp = (new StringBuilder(String.valueOf(temp))).append("0")
						.toString();
			}
			StringBuilder sb = new StringBuilder(String.valueOf(v));
			sb = sb.append(temp);
			return sb.toString();

		} else {
			temp = "##0.";
			double step = 0.0f;
			if (!isRoundTo5) {
				step = 0.5f;
			}
			if (scale == 0)
				temp = "##0";
			for (int i = 0; i < scale; i++) {
				temp = (new StringBuilder(String.valueOf(temp))).append("0")
						.toString();
				step *= 0.1f;
			}
			DecimalFormat d = new DecimalFormat(temp);
			return d.format(v - step);
		}
	}


}
