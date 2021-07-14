package com.redmoon.oa.sale;

import java.sql.SQLException;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Vector;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

import org.apache.log4j.Logger;

import cn.js.fan.db.ResultIterator;
import cn.js.fan.db.ResultRecord;
import cn.js.fan.util.StrUtil;

import com.cloudwebsoft.framework.db.JdbcTemplate;
import com.redmoon.oa.basic.SelectDb;
import com.redmoon.oa.basic.SelectMgr;
import com.redmoon.oa.basic.SelectOptionDb;

/**
 * @Description: 
 * @author: lzm
 * @Date: 2015-9-11下午01:53:06
 */
public class Chart {
	
	/**
	 * 数据库获得图表的data
	 * @Description: 
	 * @param sql
	 * @return
	 */
	public HashMap<String,Double> chartDatas(String sql){
		HashMap<String,Double> dataMap = new HashMap<String, Double>();
		JdbcTemplate jt = null;
		ResultIterator ri = null;
		jt = new JdbcTemplate();
		try {
			ri = jt.executeQuery(sql);
			while(ri.hasNext()){
				ResultRecord rr = (ResultRecord)ri.next();
				dataMap.put(rr.getString(1),rr.getDouble(2));
			}
		} catch (SQLException e) {
			Logger.getLogger(Chart.class.getName()).error(e.getMessage());
			
		}
		return dataMap;
		
	}
	/**
	 * 饼图
	 * @Description: 
	 * @param sql
	 * @return
	 */
	public JSONArray pieData(String sql){
		JSONArray jsonArr = new JSONArray();
		HashMap<String,Double> dataMap= chartDatas(sql);
		Iterator<Map.Entry<String,Double>> iter = dataMap.entrySet().iterator();
		while (iter.hasNext()) {
			Map.Entry<String,Double> entry = iter.next();
			String key = entry.getKey();
		    Double val = entry.getValue();
		    JSONArray objArr = new JSONArray();
		    objArr.add(key);
		    objArr.add(val);
		    jsonArr.add(objArr);
        }
		return jsonArr;
		
	}
	

	/**
	 * 单条折线图
	 * @Description: 
	 * @param sql
	 * @return
	 */
	public JSONArray lineChartData(String sql){
		HashMap<Integer,Double> defaultValues = new HashMap<Integer, Double>();
		for (int i = 1; i < 13; i++) {
			defaultValues.put(i, 0.0);
		}
		JSONArray jsonArr = new JSONArray();
		HashMap<String,Double> dataMap= chartDatas(sql);
		Iterator<Map.Entry<String,Double>> iter = dataMap.entrySet().iterator();
		while (iter.hasNext()) {
			Map.Entry<String,Double> entry = iter.next();
			int key = Integer.parseInt(entry.getKey());
		    Double val = entry.getValue();
		    if(defaultValues.containsKey(key)){
		    	  defaultValues.put(key, val);
		    }
		  
        }
		
		Iterator<Map.Entry<Integer,Double>> ite = defaultValues.entrySet().iterator();
		while (ite.hasNext()) {
			Map.Entry<Integer,Double> entry = ite.next();
		    Double val = entry.getValue();
		    jsonArr.add(val);
        }
		
		return jsonArr;
	}
	/**
	 * 销售订单 订单额折线图
	 * @Description: 
	 * @param sql
	 * @return
	 */
	public JSONArray salesAnalysisLineDatas(String sql){
		JSONArray arr = lineChartData(sql);
		JSONArray res = new JSONArray();
		JSONObject obj = new JSONObject();
		obj.put("name", "销售额");
		obj.put("data", arr);
		res.add(obj);
		return res;
		
	}
	/**
	 * 跟踪频率
	 * @Description: 
	 * @param unitCode
	 * @return
	 */
	public JSONObject salesActionLineDatas(String unitCode){
		SelectMgr sm = new SelectMgr();
		SelectDb sd = sm.getSelect("sales_customer_type");
		Vector vsd = sd.getOptions();
		Iterator irsd = vsd.iterator();
		ActionSetupDb asd2 = new ActionSetupDb();
		JSONArray datas = new JSONArray();
		JSONArray xCategories = new JSONArray();
		while (irsd.hasNext()) {
			SelectOptionDb sod = (SelectOptionDb)irsd.next();
			ActionSetupDb asd = (ActionSetupDb)asd2.getActionSetupDb(StrUtil.toInt(sod.getValue()), unitCode);
			xCategories.add(sod.getName());
			if (asd==null) {
				datas.add(0);
			}
			else {
				datas.add(asd.getInt("remind_days"));
			}
		}
		JSONArray dataRes = new JSONArray();
		JSONObject obj = new JSONObject();
		obj.put("name", "跟踪频率(天)");
		obj.put("data", datas);
		dataRes.add(obj);
		JSONObject resObj = new JSONObject();
		resObj.put("data", dataRes);
		resObj.put("xCategories", xCategories);
		resObj.put("title", "跟踪频率曲线");
		resObj.put("yTitle", "跟踪频率(天)");
		resObj.put("unit", "天");
		return resObj;
		
		
		
		
	}

}
