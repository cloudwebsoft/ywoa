package com.redmoon.forum.util;

import cn.js.fan.db.*;
import com.cloudwebsoft.framework.base.*;
import com.cloudwebsoft.framework.db.*;
import com.cloudwebsoft.framework.util.*;
import cn.js.fan.util.StrUtil;

public class IPStoreDb extends QObjectDb {
    public IPStoreDb() {
    }

    public IPStoreDb getIPStoreDb(long ip1, long ip2) {
        PrimaryKey pk = (PrimaryKey)primaryKey.clone();
        pk.setKeyValue("ip1", new Long(ip1));
        pk.setKeyValue("ip2", new Long(ip2));
        return (IPStoreDb)getQObjectDb(pk.getKeys());
    }

    public String getPosition(String ip) {
        try {
            /*
            String ipArr[] = ip.split("\\.");

            long ipNum = Long.parseLong(ipArr[0]) * 256 * 256 * 256 +
                         Long.parseLong(ipArr[1]) * 256 * 256 +
                         Long.parseLong(ipArr[2]) * 256 +
                         (Long.parseLong(ipArr[3]));
            */
            long ipNum = IPUtil.ip2long(ip);

            String sql = "select ip1, ip2 from " + table.getName() +
                         " where ip1<=? and ip2>=?";
            // LogUtil.getLog(getClass()).info("getPosition:ipNum=" + ipNum);
            JdbcTemplate jt = new JdbcTemplate();
            ResultIterator ri = jt.executeQuery(sql, new Object[] {new Long(ipNum), new Long(ipNum)});
            if (ri.hasNext()) {
                ResultRecord rr = (ResultRecord) ri.next();
                IPStoreDb ipsd = getIPStoreDb(rr.getLong(1), rr.getLong(2));
                String country = ipsd.getString("country");
                String city = StrUtil.getNullStr(ipsd.getString("city"));
                if (!city.equals(""))
                    country += "-" + city;
                return country;
            }
        } catch (Exception e) {
            LogUtil.getLog(getClass()).error("getPosition:" + e.getMessage() + " " + StrUtil.trace(e));
        }
        return "";
    }

}
