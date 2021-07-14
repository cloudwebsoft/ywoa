package cn.js.fan.module.cms.plugin.wiki;

import java.sql.*;

import cn.js.fan.base.*;
import cn.js.fan.cache.jcs.*;
import cn.js.fan.db.*;
import cn.js.fan.util.*;
import com.cloudwebsoft.framework.db.JdbcTemplate;
import java.util.Calendar;
import com.cloudwebsoft.framework.util.LogUtil;
import com.redmoon.oa.fileark.Document;

public class WikiStatistic extends ObjectCache implements ICacheMgr {
    public static final String ALL_WIKI_COUNT = "ALL_WIKI_COUNT";
    public static final String PREFIX = "ALL_WIKI_";

    public WikiStatistic() {

    }

    public int getAllDocCount() {
        String sql = "select count(*) from cms_wiki_doc_update where page_num=1 and check_status=" + WikiDocUpdateDb.CHECK_STATUS_PASSED;
        Integer v = null;
        try {
            v = (Integer) rmCache.getFromGroup(ALL_WIKI_COUNT, group);
        } catch (Exception e) {
            logger.error("getAllSoftwareCount:" + e.getMessage());
        }

        // If already in cache, return the count.
        if (v != null) {
            return v.intValue();
        }
        // Otherwise, we have to load the count from the db.
        else {
            Conn conn = new Conn(connname);
            ResultSet rs = null;
            try {
                rs = conn.executeQuery(sql);
                if (rs.next()) {
                    v = new Integer(rs.getInt(1));
                }
            } catch (SQLException e) {
                logger.error("getAllWikiCount:" + e.getMessage());
            } finally {
                if (conn != null) {
                    conn.close();
                    conn = null;
                }
            }
            // Add the thread count to cache
            if (v != null) {
                try {
                    rmCache.putInGroup(ALL_WIKI_COUNT, group, v);
                } catch (Exception e) {
                    logger.error(e.getMessage());
                }
            } else
                return 0;
            return v.intValue();
        }
    }

    /**
     * wiki文章创建的统计
     * @param date
     * @return
     */
    public int[] getCreateCounts(java.util.Date date) {
        int[] re = null;
        String strToday = DateUtil.format(date, "yyyy-MM-dd");
        java.util.Date today = DateUtil.parse(strToday, "yyyy-MM-dd");

        String strMonth = DateUtil.format(date, "yyyy-MM");
        java.util.Date month = DateUtil.parse(strMonth, "yyyy-MM");

        String strYear = DateUtil.format(date, "yyyy");
        java.util.Date year = DateUtil.parse(strYear, "yyyy");

        int y = StrUtil.toInt(strYear);
        Calendar cal = Calendar.getInstance();
        cal.setTime(month);
        int m = cal.get(Calendar.MONTH)+1;
        cal.setTime(today);
        int d = cal.get(Calendar.DAY_OF_MONTH);

        try {
            // logger.info("getCounts1: " + rmCache.getFromGroup(PREFIX + strYear + strMonth + strToday, group).getClass());
            // logger.info("getCounts2: " + rmCache.getFromGroup(PREFIX + strYear + strMonth + strToday, group));
            // rmCache.getFromGroup(PREFIX + strYear + strMonth + strToday, group);
            re = (int[]) rmCache.getFromGroup(PREFIX + strYear + strMonth + strToday, group);
        } catch (Exception e) {
            logger.error(StrUtil.trace(e));
        }

        if (re != null) {
            return re;
        }
        else {
            re = new int[5];
            try {
                // 年统计
                int daysOfYear = DateUtil.getDaysOfYear(y);
                java.util.Date tempd = DateUtil.addDate(year, daysOfYear);
                tempd = DateUtil.addMinuteDate(tempd, -1);
                String sql =
                        " select count(s.doc_id) from cms_wiki_doc s, document d where d.id=s.doc_id and d.examine=" + Document.EXAMINE_PASS + " and d.createDate>=? and d.createDate<=?";
                JdbcTemplate jt = new JdbcTemplate();
                ResultIterator ri = jt.executeQuery(sql, new Object[]{DateUtil.toLongString(year), DateUtil.toLongString(tempd)});
                ResultRecord rr = null;
                if (ri.hasNext()) {
                    rr = (ResultRecord) ri.next();
                    re[0] = rr.getInt(1);
                }

                // System.out.print(DateUtil.format(year, "yyyy-MM-dd") + "-" + DateUtil.format(tempd, "yyyy-MM-dd") + "=" + re[0]);
                // 月统计
                int daysOfMonth = DateUtil.getDayCount(y, m-1);
                tempd = DateUtil.addDate(month, daysOfMonth);
                tempd = DateUtil.addMinuteDate(tempd, -1);
                sql =
                        " select count(s.doc_id) from cms_wiki_doc s, document d where d.id=s.doc_id and d.examine=" + Document.EXAMINE_PASS + " and d.createDate>=? and d.createDate<=?";
                ri = jt.executeQuery(sql, new Object[] {DateUtil.toLongString(month), DateUtil.toLongString(tempd)});
                if (ri.hasNext()) {
                    rr = (ResultRecord) ri.next();
                    re[1] = rr.getInt(1);
                }

                // 当天
                sql =
                        " select count(s.doc_id) from cms_wiki_doc s, document d where s.doc_id=d.id and d.examine=" + Document.EXAMINE_PASS + " and d.createDate >= " +
                        StrUtil.sqlstr(Long.toString(today.getTime())) +
                        " and d.createDate < " +
                        StrUtil.sqlstr(Long.toString(today.getTime() +
                        24 * 60 * 60000));
                ri = jt.executeQuery(
                        sql);
                if (ri.hasNext()) {
                    rr = (ResultRecord) ri.next();
                    re[2] = rr.getInt(1);
                }

                // 前一天
                sql =
                        " select count(s.doc_id) from cms_wiki_doc s, document d where s.doc_id=d.id and d.examine=" + Document.EXAMINE_PASS + " and d.createDate >= " +
                        StrUtil.sqlstr(Long.toString(today.getTime() -
                        24 * 60 * 60000)) +
                        " and d.createDate < " +
                        StrUtil.sqlstr(Long.toString(today.getTime()));
                ri = jt.executeQuery(
                        sql);
                if (ri.hasNext()) {
                    rr = (ResultRecord) ri.next();
                    re[3] = rr.getInt(1);
                }

                // 前两天
                tempd = DateUtil.addHourDate(today, -48);
                sql =
                        " select count(s.doc_id) from cms_wiki_doc s, document d where s.doc_id=d.id and d.examine=" + Document.EXAMINE_PASS + " and d.createDate>=? and d.createDate<=?";
                ri = jt.executeQuery(sql, new Object[]{DateUtil.toLongString(tempd), DateUtil.toLongString(DateUtil.addHourDate(today, -24))});
                if (ri.hasNext()) {
                    rr = (ResultRecord) ri.next();
                    re[4] = rr.getInt(1);
                }

                try {
                    rmCache.putInGroup(PREFIX + strYear + strMonth + strToday, group, re);
                } catch (Exception e) {
                    logger.error(StrUtil.trace(e));
                }
            } catch (SQLException e) {
                LogUtil.getLog(getClass()).error(StrUtil.trace(e));
                e.printStackTrace();
            }
            return re;
        }
    }


    /**
     * wiki文章编辑的统计
     * @param date
     * @return
     */
    public int[] getEditCounts(java.util.Date date) {
        int[] re = null;
        String strToday = DateUtil.format(date, "yyyy-MM-dd");
        java.util.Date today = DateUtil.parse(strToday, "yyyy-MM-dd");

        String strMonth = DateUtil.format(date, "yyyy-MM");
        java.util.Date month = DateUtil.parse(strMonth, "yyyy-MM");

        String strYear = DateUtil.format(date, "yyyy");
        java.util.Date year = DateUtil.parse(strYear, "yyyy");

        int y = StrUtil.toInt(strYear);
        Calendar cal = Calendar.getInstance();
        cal.setTime(month);
        int m = cal.get(Calendar.MONTH)+1;
        cal.setTime(today);
        int d = cal.get(Calendar.DAY_OF_MONTH);

        try {
            // logger.info("getCounts1: " + rmCache.getFromGroup(PREFIX + strYear + strMonth + strToday, group).getClass());
            // logger.info("getCounts2: " + rmCache.getFromGroup(PREFIX + strYear + strMonth + strToday, group));
            // rmCache.getFromGroup(PREFIX + strYear + strMonth + strToday, group);
            re = (int[]) rmCache.getFromGroup(PREFIX + strYear + strMonth + strToday, group);
        } catch (Exception e) {
            logger.error(StrUtil.trace(e));
        }

        if (re != null) {
            return re;
        }
        else {
            re = new int[5];
            try {
                // 年统计
                int daysOfYear = DateUtil.getDaysOfYear(y);
                java.util.Date tempd = DateUtil.addDate(year, daysOfYear);
                tempd = DateUtil.addMinuteDate(tempd, -1);
                String sql =
                        " select count(*) from cms_wiki_doc_update where edit_date>=? and edit_date<=?";
                JdbcTemplate jt = new JdbcTemplate();
                ResultIterator ri = jt.executeQuery(sql, new Object[]{year, tempd});
                ResultRecord rr = null;
                if (ri.hasNext()) {
                    rr = (ResultRecord) ri.next();
                    re[0] = rr.getInt(1);
                }

                // System.out.print(DateUtil.format(year, "yyyy-MM-dd") + "-" + DateUtil.format(tempd, "yyyy-MM-dd") + "=" + re[0]);
                // 月统计
                int daysOfMonth = DateUtil.getDayCount(y, m-1);
                tempd = DateUtil.addDate(month, daysOfMonth);
                tempd = DateUtil.addMinuteDate(tempd, -1);
                ri = jt.executeQuery(sql, new Object[] {month, tempd});
                if (ri.hasNext()) {
                    rr = (ResultRecord) ri.next();
                    re[1] = rr.getInt(1);
                }

                // 当天
                ri = jt.executeQuery(sql, new Object[]{today, DateUtil.addDate(today, 1)});
                if (ri.hasNext()) {
                    rr = (ResultRecord) ri.next();
                    re[2] = rr.getInt(1);
                }

                // 前一天
                ri = jt.executeQuery(sql, new Object[]{DateUtil.addDate(today, -1), today});
                if (ri.hasNext()) {
                    rr = (ResultRecord) ri.next();
                    re[3] = rr.getInt(1);
                }

                // 前两天
                ri = jt.executeQuery(sql, new Object[]{DateUtil.addDate(today, -2), today});
                ri = jt.executeQuery(sql, new Object[]{DateUtil.toLongString(tempd), DateUtil.toLongString(DateUtil.addHourDate(today, -24))});
                if (ri.hasNext()) {
                    rr = (ResultRecord) ri.next();
                    re[4] = rr.getInt(1);
                }

                try {
                    rmCache.putInGroup(PREFIX + strYear + strMonth + strToday, group, re);
                } catch (Exception e) {
                    logger.error(StrUtil.trace(e));
                }
            } catch (SQLException e) {
                LogUtil.getLog(getClass()).error(StrUtil.trace(e));
                e.printStackTrace();
            }
            return re;
        }
    }
    
    
    /**
     * 得到一个月内被编辑最多的文章的排行
     * @param date
     * @param n 条数
     * @return
     */
    public int[][] getRankDocEditCountMonth(java.util.Date date, int n) {
        int[][] v = null;

        String strMonth = DateUtil.format(date, "yyyy-MM");
        java.util.Date month = DateUtil.parse(strMonth, "yyyy-MM");

        String strYear = DateUtil.format(date, "yyyy");
        java.util.Date year = DateUtil.parse(strYear, "yyyy");

        int y = StrUtil.toInt(strYear);
        Calendar cal = Calendar.getInstance();
        cal.setTime(month);
        int m = cal.get(Calendar.MONTH) + 1;

        // int d = cal.get(Calendar.DAY_OF_MONTH);

        try {
            v = (int[][]) rmCache.getFromGroup(PREFIX + "editrank" + strYear + strMonth, group);
        } catch (Exception e) {
            logger.error("getCounts:" + e.getMessage());
        }

        if (v != null) {
            return v;
        }
        else {
            try {
                String sql =
                    "select doc_id, count(*) as c from cms_wiki_doc_update where edit_date>=? and edit_date<? group by doc_id order by 2 desc";

                // 月排行
                int daysOfMonth = DateUtil.getDayCount(y, m-1);
                java.util.Date tempd = DateUtil.addDate(month, daysOfMonth);
                tempd = DateUtil.addMinuteDate(tempd, -1);
                JdbcTemplate jt = new JdbcTemplate();
                ResultIterator ri = jt.executeQuery(sql, new Object[]{month, tempd}, 1, n);
                
                if (ri.size()<n)
                	v = new int[ri.size()][2];
                else
                	v = new int[n][2];
                
                int k = 0;
                while (ri.hasNext()) {
                    ResultRecord rr = (ResultRecord) ri.next();
                    v[k][0] = rr.getInt(1);
                    v[k][1] = rr.getInt(2);
                    k++;
                    if (k >= n)
                        break;
                }
                try {
                    rmCache.putInGroup(PREFIX + "editrank" + strYear + strMonth, group, v);
                } catch (Exception e) {
                    logger.error(e.getMessage());
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
            return v;
        }
    }
    
    /**
     * 得到一个月内得分最多的用户的排行
     * @param date
     * @param n
     * @return
     */
    public String[][] getRankUserScoreMonth(java.util.Date date, int n) {
        String[][] v = null;

        String strMonth = DateUtil.format(date, "yyyy-MM");
        java.util.Date month = DateUtil.parse(strMonth, "yyyy-MM");

        String strYear = DateUtil.format(date, "yyyy");
        java.util.Date year = DateUtil.parse(strYear, "yyyy");

        int y = StrUtil.toInt(strYear);
        Calendar cal = Calendar.getInstance();
        cal.setTime(month);
        int m = cal.get(Calendar.MONTH)+1;

        // int d = cal.get(Calendar.DAY_OF_MONTH);

        try {
            v = (String[][]) rmCache.getFromGroup(PREFIX + "scorerank" + strYear + strMonth, group);
        } catch (Exception e) {
            logger.error("getCounts:" + e.getMessage());
        }

        if (v != null) {
            return v;
        }
        else {
            try {
                String sql =
                    "select user_name, sum(score) as c from cms_wiki_doc_update where check_status=" + WikiDocUpdateDb.CHECK_STATUS_PASSED + " and edit_date>=? and edit_date<? group by user_name order by 2 desc";
 
                // 月排行
                int daysOfMonth = DateUtil.getDayCount(y, m-1);
                java.util.Date tempd = DateUtil.addDate(month, daysOfMonth);
                tempd = DateUtil.addMinuteDate(tempd, -1);
                JdbcTemplate jt = new JdbcTemplate();
                ResultIterator ri = jt.executeQuery(sql, new Object[]{month, tempd}, 1, n);
                if (ri.size()<n)
                	v = new String[ri.size()][2];
                else
                	v = new String[n][2];
                int k = 0;
                while (ri.hasNext()) {
                    ResultRecord rr = (ResultRecord) ri.next();
                    v[k][0] = rr.getString(1);
                    v[k][1] = rr.getInt(2) + "";
                    k++;
                    if (k >= n)
                        break;
                }
                try {
                    rmCache.putInGroup(PREFIX + "scorerank" + strYear + strMonth, group, v);
                } catch (Exception e) {
                    logger.error(e.getMessage());
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
            return v;
        }
    }    

    public void refresh() {
        try {
            rmCache.invalidateGroup(group);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void regist() {
        if (!isRegisted) {
            rmCache.regist(this);
            isRegisted = true;
            // System.out.println(getClass() + " is registed.");
        }
    }

    /**
     * 定时刷新缓存
     */
    public void timer() {
        // 刷新全文检索
        curRefreshLife--;
        if (curRefreshLife <= 0) {
            refresh();
            curRefreshLife = statInterval;
            // System.out.println(getClass() + " has done on timer");
        }
    }

    static int statInterval = Config.getInstance().getIntProperty("statInterval"); // 10分钟

    static boolean isRegisted = false;
    static int curRefreshLife = statInterval; // 10分钟

}
