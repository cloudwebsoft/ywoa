package com.redmoon.oa.contract;

import cn.js.fan.db.ResultIterator;
import cn.js.fan.db.ResultRecord;

import com.cloudwebsoft.framework.db.JdbcTemplate;
import com.cloudwebsoft.framework.util.LogUtil;

public class ContractMgr {
	public boolean createContract(int flowId){
		String sqlinsert = "insert into ft_sales_contract(flowId,contact_no,contact_name,contact_desc,contact_item,contact_content,demo,customer,cws_creator,saler,contract_type,begindate,enddate,linkman,toname,create_date,seller_name,unit_code,pay,cws_status,amount) values(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
		String sqlget = "select * from ft_sales_contract_new where flowId=?";
		JdbcTemplate jt = new JdbcTemplate();
		JdbcTemplate jt1 = new JdbcTemplate();
		ResultIterator ri = null;
		ResultRecord rd = null;
		try {
			ri = jt.executeQuery(sqlget, new Object[]{flowId});
			if(ri.hasNext()){
				rd = (ResultRecord)ri.next();
				String contractNo = rd.getString("contract_no");
				String contractName = rd.getString("contract_name");
				String contractDesc = rd.getString("contact_desc");
				String contractItem = rd.getString("contact_item");
				String contractContent = rd.getString("contact_content");
				String demo = rd.getString("demo");
				String customer = rd.getString("customer");
				String cwsCreator = rd.getString("seller_name");
				String saler = rd.getString("saler");
				int contractType = rd.getInt("contract_type");
				String beginDate = rd.getString("begindate");
				String endDate = rd.getString("endDate");
				String linkMan = rd.getString("linkman");
				String toName = rd.getString("toname");
				String createDate = rd.getString("createDate");
				String sellerName = rd.getString("seller_name");
				String unitCode = rd.getString("unit_code");
				String pay = rd.getString("pay");
				int cwsStatus = rd.getInt("cws_status");
				String amount = rd.getString("amount");
				return jt1.executeUpdate(sqlinsert, new Object[]{flowId,contractNo,contractName,contractDesc
						,contractItem,contractContent,demo,customer,cwsCreator,saler,contractType,
						beginDate,endDate,linkMan,toName,createDate,sellerName,unitCode,pay,
						cwsStatus,amount})>0?true:false;
			}
		} catch (Exception e) {
			LogUtil.getLog(getClass()).error(e);
		}
		return false;
	}
	
	public int getFlowId(long id){
		String sqlget = "select flowId from ft_sales_contract where id=?";
		JdbcTemplate jt = new JdbcTemplate();
		ResultIterator ri = null;
		ResultRecord rd = null;
		int flowId = 0;
		try {
			ri = jt.executeQuery(sqlget, new Object[]{id});
			if(ri.hasNext()){
				rd = (ResultRecord)ri.next();
				flowId = rd.getInt(1);
			}
		} catch (Exception e) {
			LogUtil.getLog(getClass()).error(e);
		}
		return flowId;
	}
}
