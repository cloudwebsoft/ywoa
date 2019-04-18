package com.redmoon.oa.android.sales;

import java.sql.SQLException;
import java.util.Calendar;
import org.apache.log4j.Logger;
import org.json.JSONException;
import org.json.JSONObject;

import com.cloudwebsoft.framework.db.JdbcTemplate;
import cn.js.fan.db.ResultIterator;
import cn.js.fan.db.ResultRecord;
import cn.js.fan.db.SQLFilter;
import com.redmoon.oa.android.Privilege;
import com.redmoon.oa.android.tools.AndroidUtilTools;

/**
 * 订单回款记录
 * 
 * @author Administrator
 * 
 */
public class SalesMonthStatisAction {
	private String skey = "";
	private String result = "";

	public String getSkey() {
		return skey;
	}

	public void setSkey(String skey) {
		this.skey = skey;
	}

	public String getResult() {
		return result;
	}

	public void setResult(String result) {
		this.result = result;
	}

	public String execute() {
		JSONObject json = new JSONObject();
		Privilege privilege = new Privilege();
		boolean re = privilege.Auth(getSkey());
		try {
			if (re) {
				json.put("res", "-2");
				json.put("msg", "时间过期");
				setResult(json.toString());
				return "SUCCESS";
			}
			json.put("res", "0");
			json.put("msg", "操作成功");
			JSONObject result = new JSONObject();
			StringBuilder sqlSb = new StringBuilder();
			Calendar calender = Calendar.getInstance();
			int now_month =calender.get(Calendar.MONTH)+1;
			sqlSb
					.append(
							"select sum(expectPrice) from form_table_sales_chance")
					.append(
							" where customer in (SELECT id FROM  form_table_sales_customer where sales_person = ")
					.append("'").append(privilege.getUserName(skey)).append(
							"')").append(" and state in(8,9) ").append(" and ")
					.append(SQLFilter.month("pre_date")).append(" = ").append(
							now_month);
			double monthDealPrice = SalesModuleDao.getCountInfoById(sqlSb.toString());
			StringBuilder backPriSb = new StringBuilder();
			backPriSb
					.append(
							"select sum(hkje) from form_table_sales_ord_huikuan where cws_id in (")
					.append("select id from form_table_sales_order")
					.append(
							" where customer in (SELECT id FROM  form_table_sales_customer where sales_person = ")
					.append("'").append(privilege.getUserName(skey)).append(
							"')) and ").append(SQLFilter.month("hkrq")).append(
							" = ").append(now_month);
			double monthBackPrice = SalesModuleDao.getCountInfoById(backPriSb.toString());
			double monthCompletePri =  0.00;
			if((int)monthDealPrice !=0){
				monthCompletePri = 	Double.parseDouble(AndroidUtilTools.round(monthBackPrice / monthDealPrice, 2, true));
			}
			
			
			result.put("monthCompletePercent",monthCompletePri*100 );
			result.put("monthDealPrice", monthDealPrice);
			result.put("monthBackPrice", monthBackPrice);
			json.put("result", result);
		} catch (JSONException e) {
			Logger.getLogger(SalesMonthStatisAction.class).error(
					"json error:" + e.getMessage());
		}
		setResult(json.toString());
		return "SUCCESS";
	}


}
