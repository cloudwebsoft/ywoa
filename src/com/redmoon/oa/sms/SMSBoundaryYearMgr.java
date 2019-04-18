package com.redmoon.oa.sms;

import javax.servlet.http.HttpServletRequest;
import cn.js.fan.util.ParamUtil;
import com.cloudwebsoft.framework.db.JdbcTemplate;
import cn.js.fan.util.ResKeyException;
import java.util.Date;
import cn.js.fan.util.DateUtil;
import cn.js.fan.util.ErrMsgException;
import java.sql.SQLException;
import cn.js.fan.db.ResultIterator;
import cn.js.fan.db.ResultRecord;

/**
 * <p>Title: </p>
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
public class SMSBoundaryYearMgr {
    public SMSBoundaryYearMgr() {
    }

    /**
     * 暂时不用
     * @param request HttpServletRequest
     * @return boolean
     * @throws ResKeyException
     */
    /*public boolean create(HttpServletRequest request) throws ResKeyException{
        boolean re = false;
        //String name = ParamUtil.get(request,"name");
        String beginDateStr = ParamUtil.get(request,"beginDate");
        Date beginDate = DateUtil.parse(beginDateStr, "yyyy-MM-dd");
        String endDateStr = ParamUtil.get(request,"endDate");
        Date endDate = DateUtil.parse(endDateStr,"yyyy-MM-dd");
        int total = ParamUtil.getInt(request,"total",0);

        SMSBoundaryYearDb sbyd = new SMSBoundaryYearDb();
        re = sbyd.create(new JdbcTemplate(),new Object[]{
            beginDate,
            endDate,
            new Integer(total)
        });
        return re;
    }*/

    /**
     * 设置短信年配额
     * @param request HttpServletRequest
     * @return boolean
     * @throws ResKeyException
     * @throws ErrMsgException
     */
    public void save(HttpServletRequest request) throws ResKeyException,
            ErrMsgException {
        String beginDateStr = ParamUtil.get(request,"beginDate");
        if(beginDateStr==null||beginDateStr.equals("")){
            throw new ErrMsgException("开始日期不能为空！");
        }
        String endDateStr = ParamUtil.get(request,"endDate");
        if(endDateStr==null||endDateStr.equals("")){
            throw new ErrMsgException("结束日期不能为空！");
        }
        int total = ParamUtil.getInt(request,"total",0);
        Config cfg = new Config();
        cfg.saveYearBoundary(total,beginDateStr,endDateStr);
    }

    public boolean isInUse(Date date){
        Date beginDate = getBeginDate();
        Date endDate = getEndDate();
        if(beginDate==null||endDate == null){
            return false;
        }else if(beginDate.before(date)&&endDate.after(date)){
            return true;
        }else{
            return false;
        }
    }

      public int getRemainingCount() throws SQLException {
          int total = 0;
          int used = 0;

          /*获得当年定额数*/
          /*SMSBoundaryYearDb sbyDb = new SMSBoundaryYearDb();
          sbyDb = sbyDb.getSMSBoundary(1);
          total = sbyDb.getInt("total");*/
          total = getTotal();

          /*获得已经发送的短信数*/
          /*Date beginDate = sbyDb.getDate("begin_date");
          Date endDate = sbyDb.getDate("end_date");
          String sql = "select count(*) from sms_send_record where SENDTIME >=? and SENDTIME <=?";
          JdbcTemplate jt = new JdbcTemplate();
          ResultIterator ri = jt.executeQuery(sql,new Object[]{beginDate,endDate});
          if(ri.hasNext()){
              ResultRecord rd = (ResultRecord)ri.next();
              used = rd.getInt(1);
          }*/
          used = getUsedCount();
          return (total-used);
      }

      /**
       * 获得当年定额数
       * @return int
       */
      public int getTotal(){
          int total = 0;
          Config cfg = new Config();
          String totalStr = cfg.getIsUsedProperty("yearTotal");
          if(isNumber(totalStr)){
              total = Integer.parseInt(totalStr);
          }
          return total;
      }

      public Date getBeginDate(){
          Config cfg = new Config();
          String beginDateStr = cfg.getIsUsedProperty("beginDate");
          if(beginDateStr==null||beginDateStr.equals("")){
              return null;
          }else{
              return DateUtil.parse(beginDateStr,"yyyy-MM-dd");
          }
      }

      public Date getEndDate(){
          Config cfg = new Config();
          String endDateStr = cfg.getIsUsedProperty("endDate");
          if(endDateStr==null||endDateStr.equals("")){
              return null;
          }else{
              return DateUtil.parse(endDateStr,"yyyy-MM-dd");
          }
      }

      /**
       * 获得已经发送的短信数
       * @return int
       */
      public int getUsedCount() throws SQLException {
          int used = 0;
          Date beginDate = getBeginDate();
          Date endDate = getEndDate();
          String sql = "select count(*) from sms_send_record where SENDTIME >=? and SENDTIME <=?";
          JdbcTemplate jt = new JdbcTemplate();
          ResultIterator ri = jt.executeQuery(sql,new Object[]{beginDate,endDate});
          if(ri.hasNext()){
              ResultRecord rd = (ResultRecord)ri.next();
              used = rd.getInt(1);
          }
          return used;
      }

      private boolean isNumber(String str) {
        if(str==null||str.equals("")){
            return false;
        }else{
            final String number = "1234567890";
            for (int i = 0; i < str.length(); i++) {
                if (number.indexOf(str.charAt(i)) == -1) {
                    return false;
                }
            }
        }
        return true;
    }

}
