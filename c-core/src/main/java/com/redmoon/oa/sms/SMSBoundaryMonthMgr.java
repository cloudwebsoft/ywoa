package com.redmoon.oa.sms;

import javax.servlet.http.HttpServletRequest;
import cn.js.fan.util.ParamUtil;
import com.cloudwebsoft.framework.db.JdbcTemplate;
import cn.js.fan.util.ResKeyException;
import cn.js.fan.db.ResultRecord;
import cn.js.fan.db.ResultIterator;
import java.sql.SQLException;
import java.util.Date;
import cn.js.fan.util.DateUtil;
import cn.js.fan.util.StrUtil;

/**
 * <p>Title: 短信配额</p>
 *
 * <p>Description: </p>
 *
 * <p>Copyright: Copyright (c) 2007</p>
 *
 * <p>Company: </p>
 *
 * @author not attributable
 * @version 1.0
 */
public class SMSBoundaryMonthMgr {
    public SMSBoundaryMonthMgr() {
    }

    /**
     * 暂时没有用到
     * @param request HttpServletRequest
     * @return boolean
     * @throws ResKeyException
     */
    /*public boolean creat(HttpServletRequest request) throws ResKeyException {
        boolean re = false;
        int total = ParamUtil.getInt(request,"total",0);

        SMSBoundaryMonthDb sbmDb = new SMSBoundaryMonthDb();
        re = sbmDb.create(new JdbcTemplate(),new Object[]{
            new Integer(total)
        });

        return re;
         }*/

    /**
     * 设置短信月配额
     * @param request HttpServletRequest
     * @return boolean
     */
    public void save(HttpServletRequest request) throws ResKeyException {

        int total = ParamUtil.getInt(request, "total", 0);

        Config cfg = new Config();
        cfg.setMonthTotal(total);

        //SMSBoundaryMonthDb sbmDb = new SMSBoundaryMonthDb();
        //sbmDb = sbmDb.getSMSBoundaryDetialDb(1);
        //sbmDb.set("total",new Integer(total));
    }

    public int getRemainingCount(int month) throws SQLException {
        int total = 0;
        int used = 0;

        /*获得当月定额数*/
        /*SMSBoundaryMonthDb sbmDb = new SMSBoundaryMonthDb();
                 sbmDb = sbmDb.getSMSBoundaryDetialDb(1);
                 //total = sbmDb.getInt(getMonth(month));
                 total = sbmDb.getInt("total");*/
        total = getTotal();


        /*获得已经使用数*/
        used = getUsedCount(month);
        /*
        int year = DateUtil.getYear(new Date());
        month++; //月份原来是重0开始的
        int day = getDays(year, month);
        Date beginDate = DateUtil.parse(year + "-" + month + "-01 00:00:00",
                                        "yyyy-MM-dd hh:mm:ss");
        Date endDate = DateUtil.parse(year + "-" + month + "-" + day +
                                      " 23:59:59", "yyyy-MM-dd hh:mm:ss");
        String sql =
                "select count(*) from sms_send_record where SENDTIME > ? and SENDTIME < ?";

        JdbcTemplate jt = new JdbcTemplate();
        ResultRecord rd = null;
        ResultIterator ri = jt.executeQuery(sql, new Object[] {beginDate,
                                            endDate});
        if (ri.hasNext()) {
            rd = (ResultRecord) ri.next();
            used = rd.getInt(1);
        }*/

        return (total - used);
    }

    /**
     * 获得当月定额数
     * @return int
     */
    public int getTotal() {
        Config cfg = new Config();
        String totalStr = cfg.getIsUsedProperty("monthTotal");
        return StrUtil.toInt(totalStr, 200);
    }

    /**
     * 获得当月已经发送的短信数
     * @return int 月份（0开始）
     */
    public int getUsedCount(int month) throws SQLException {
        int used = 0;
        int year = DateUtil.getYear(new Date());
        int day = DateUtil.getDayCount(year, month);
        Date beginDate = DateUtil.parse(year + "-" + month + "-01 00:00:00",
                                        "yyyy-MM-dd hh:mm:ss");
        Date endDate = DateUtil.parse(year + "-" + month + "-" + day +
                                      " 23:59:59",
                                      "yyyy-MM-dd hh:mm:ss");
        String sql =
                "select count(*) from sms_send_record where SENDTIME > ? and SENDTIME < ?";

        JdbcTemplate jt = new JdbcTemplate();
        ResultRecord rd = null;
        ResultIterator ri = jt.executeQuery(sql, new Object[] {beginDate,
                                            endDate});
        if (ri.hasNext()) {
            rd = (ResultRecord) ri.next();
            used = rd.getInt(1);
        }

        return used;
    }

}
