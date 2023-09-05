package com.redmoon.oa.util;

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;

import com.cloudwebsoft.framework.db.JdbcTemplate;

import cn.js.fan.util.DateUtil;
import cn.js.fan.util.ErrMsgException;
import cn.js.fan.util.ParamUtil;
import cn.js.fan.util.ResKeyException;
import cn.js.fan.util.StrUtil;
import com.cloudwebsoft.framework.util.LogUtil;

public class ExchangeRateMgr {
	public ExchangeRateMgr() {

	}

	public boolean create(HttpServletRequest request) throws ErrMsgException,
			ResKeyException, IOException {
		ExchangeRateDb er = new ExchangeRateDb();
		String moneytype = "";
		float rate = 0.0f;
		moneytype = ParamUtil.get(request, "bz");
		rate = Float.parseFloat(ParamUtil.get(request, "rate"));
		java.util.Date modifydate = new java.util.Date();
		return er.create(new JdbcTemplate(), new Object[] { moneytype,
				new Float(rate), modifydate });
	}

	public boolean save(HttpServletRequest request) throws ErrMsgException,
			ResKeyException, IOException {
		ExchangeRateDb er = new ExchangeRateDb();
		String moneytype = ParamUtil.get(request, "bz");
		float rate = StrUtil.toFloat(ParamUtil.get(request, "rate"));
		int id = ParamUtil.getInt(request, "id");
		er = er.getExchangeRateDb(id);
		er.set("money_type", moneytype);
		er.set("rate", new Float(rate));
		er.set("modify_date", new java.util.Date());
		return er.save();
	}

	public boolean del(int id) throws Exception {
		ExchangeRateDb er = new ExchangeRateDb();
		boolean re = false;
		er = er.getExchangeRateDb(id);
		re = er.del();
		return re;
	}

	public void delBatch(HttpServletRequest request) throws ErrMsgException {
		String strids = ParamUtil.get(request, "ids");
		String[] ids = StrUtil.split(strids, ",");
		if (ids == null)
			return;
		int len = ids.length;
		for (int i = 0; i < len; i++) {
			try {
				del(Integer.parseInt(ids[i]));
			} catch (Exception e) {
				LogUtil.getLog(getClass()).error(e);
			}
		}
	}
}
