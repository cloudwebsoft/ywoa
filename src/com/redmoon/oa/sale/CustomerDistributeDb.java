package com.redmoon.oa.sale;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import cn.js.fan.db.Conn;
import cn.js.fan.util.StrUtil;
import cn.js.fan.web.Global;

import com.cloudwebsoft.framework.base.QObjectDb;
import com.cloudwebsoft.framework.util.LogUtil;
import com.redmoon.oa.visual.module.sales.CustomerShareDb;

public class CustomerDistributeDb extends QObjectDb {

	/**
	 * 暂无用处，判断某客户是否被分配给了用户
	 * @param userName
	 * @param customerId
	 * @return
	 */
    public static boolean isExist(String userName, long customerId) {
    	String sql = "select id from oa_sales_customer_distr where user_name=? and customer_id=?";
    	
        ResultSet rs = null;
        Conn conn = new Conn(Global.getDefaultDB());
        try {
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setString(1, userName);
            ps.setLong(2, customerId);
            rs = conn.executePreQuery();
            if (rs!=null && rs.next())
                return true;
        } catch (SQLException e) {
            LogUtil.getLog(CustomerShareDb.class).error("isExist:" + StrUtil.trace(e));
        } finally {
            if (conn != null) {
                conn.close();
                conn = null;
            }
        }
        return false;
    }
}
