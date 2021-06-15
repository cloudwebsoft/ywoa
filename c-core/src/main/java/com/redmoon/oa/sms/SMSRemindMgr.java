package com.redmoon.oa.sms;

import javax.servlet.http.HttpServletRequest;
import cn.js.fan.util.ParamUtil;
import cn.js.fan.util.ErrMsgException;
import cn.js.fan.util.ResKeyException;
import java.util.Date;
import cn.js.fan.util.DateUtil;
import cn.js.fan.util.StrUtil;

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
public class SMSRemindMgr {
    public SMSRemindMgr() {
    }

    public void save(HttpServletRequest request) throws  ErrMsgException,
            ResKeyException {

        int boundaryType = ParamUtil.getInt(request,"boundaryType",0);
        if(boundaryType==0){
            throw new ErrMsgException("短信提示设置错误！");
        }
        String title = ParamUtil.get(request,"title");
        String content = "";
        int boundaryCount = 0;
        Config cfg = new Config();
        if(boundaryType==1){
            content = ParamUtil.get(request,"boundaryYearContent");
            boundaryCount = ParamUtil.getInt(request,"boundaryYear",0);
            cfg.saveYearRemind(title,content,boundaryCount);
        }else if(boundaryType==2){
            content = ParamUtil.get(request,"boundaryMonthContent");
            boundaryCount = ParamUtil.getInt(request,"boundaryMonth",0);
            cfg.saveMonthRemind(title,content,boundaryCount);
        }
    }

    public boolean isRemind(int boundaryType){
        Date remindDate = null;
        Date beginDate = null;
        Date endDate = null;
        if(boundaryType == Config.SMS_BOUNDARY_YEAR){
            remindDate = getYearRemaindDate();
            if(remindDate==null){
                return false;
            }
            SMSBoundaryYearMgr sbyMgr = new SMSBoundaryYearMgr();
            beginDate = sbyMgr.getBeginDate();
            endDate = sbyMgr.getEndDate();
        }else if(boundaryType == Config.SMS_BOUNDARY_MONTH){
            remindDate = getMonthRemainDate();
            if(remindDate==null){
                return false;
            }
            int year = DateUtil.getYear(new Date());
            int month = DateUtil.getMonth(new Date());
            beginDate = DateUtil.parse(year+"-"+month+"-01 00:00:00","yyyy-MM-dd hh:mm:ss");
            endDate = DateUtil.parse(year+"-"+month+"-"+DateUtil.getDayCount(year,month)+" 23:59:59","yyyy-MM-dd hh:mm:ss");
        }
        if(remindDate.before(endDate)&&remindDate.after(beginDate)){
            return true;
        }else{
            return false;
        }
    }

    public int getBoundary(int boundaryType){
        Config cfg = new Config();
        String boundary = "";
        if(boundaryType==Config.SMS_BOUNDARY_YEAR){
            return StrUtil.toInt(cfg.getIsUsedProperty("yearBoundary"),1);
        }else{
            return StrUtil.toInt(cfg.getIsUsedProperty("monthBoundary"),1);
        }
    }

    public Date getYearRemaindDate(){
        Config cfg = new Config();
        String yearRemindDateStr = cfg.getIsUsedProperty("yearRemindDate");
        if(yearRemindDateStr==null||yearRemindDateStr.equals("")){
            return null;
        }else{
            return DateUtil.parse(yearRemindDateStr,"yyyy-MM-dd");
        }
    }

    public Date getMonthRemainDate(){
        Config cfg = new Config();
        String monthRemindDateStr = cfg.getIsUsedProperty("monthRemindDate");
        if(monthRemindDateStr==null||monthRemindDateStr.equals("")){
            return null;
        }else{
            return DateUtil.parse(monthRemindDateStr,"yyyy-MM-dd");
        }
    }

    public String getTitle(int boundaryType){
        Config cfg = new Config();
        if(boundaryType==Config.SMS_BOUNDARY_YEAR){
            return cfg.getIsUsedProperty("yearRemindTitle");
        }else if(boundaryType==Config.SMS_BOUNDARY_MONTH){
            return cfg.getIsUsedProperty("monthRemindTitle");
        }
        return "";
    }

    public String getContent(int boundaryType){
        Config cfg = new Config();
        if(boundaryType==Config.SMS_BOUNDARY_YEAR){
            return cfg.getIsUsedProperty("yearRemindContent");
        }else if(boundaryType==Config.SMS_BOUNDARY_MONTH){
            return cfg.getIsUsedProperty("monthRemindContent");
        }
        return "";
    }

    public void saveDate(Date date,int boundaryType){
        Config cfg = new Config();
        if(boundaryType==Config.SMS_BOUNDARY_YEAR){
            cfg.saveYearRemindDate(DateUtil.format(date,"yyyy-MM-dd"));
        }else if(boundaryType==Config.SMS_BOUNDARY_MONTH){
            cfg.saveMonthRemindDate(DateUtil.format(date,"yyyy-MM-dd"));
        }
    }
}
