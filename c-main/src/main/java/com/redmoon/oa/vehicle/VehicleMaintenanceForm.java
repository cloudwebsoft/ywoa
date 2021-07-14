package com.redmoon.oa.vehicle;

import javax.servlet.http.*;
import cn.js.fan.base.*;
import cn.js.fan.security.*;
import cn.js.fan.util.*;
import cn.js.fan.web.*;
import java.util.*;

/**
 * <p>Title: </p>
 *
 * <p>Description: </p>
 *
 * <p>Copyright: Copyright (c) 2005</p>
 *
 * <p>Company: </p>
 *
 * @author not attributable
 * @version 1.0
 */
public class VehicleMaintenanceForm extends AbstractForm{
    VehicleMaintenanceDb vmd = new VehicleMaintenanceDb();

    public VehicleMaintenanceForm() {
    }

    public VehicleMaintenanceDb getVehicleMaintenanceDb() {
        return vmd;
    }

    public int chkId(HttpServletRequest request) {
        String strId = ParamUtil.get(request, "id");
        if (!StrUtil.isNumeric(strId)){
            log(SkinUtil.LoadString(request,
                    "res.module.vehiclemaintenance", "warn_id_err_vehiclemaintenance"));
            return 0;
        }
        int id = Integer.parseInt(strId);
        vmd.setId(id);
        return id;
    }


    public String chkLicenseNo(HttpServletRequest request){
        String licenseNo =  ParamUtil.get(request,"licenseNo");
        if (licenseNo == null || licenseNo.equals("")) {
            log(SkinUtil.LoadString(request,
                    "res.module.vehiclemaintenance", "warn_licenseno_err_vehiclemaintenance"));
        }
        if (!SecurityUtil.isValidSqlParam(licenseNo))
            log(SkinUtil.LoadString(request,
                    "res.module.vehiclemaintenance", "warn_licenseno_err_vehiclemaintenance"));
        vmd.setLicenseNo(licenseNo);
        return licenseNo;
    }

    public Date chkBeginDate(HttpServletRequest request) {
        String strBeginDate = ParamUtil.get(request,"beginDate");
        java.util.Date d = null;
        try {
            d = DateUtil.parse(strBeginDate, "yyyy-MM-dd");
        } catch (Exception e) {
            logger.error("chkBeginDate:" + e.getMessage());
        }
        vmd.setBeginDate(d);
        return d;
    }

    public Date chkEndDate(HttpServletRequest request) {
        String strEndDate = ParamUtil.get(request,"endDate");
        java.util.Date d = null;
        try {
            d = DateUtil.parse(strEndDate, "yyyy-MM-dd");
        } catch (Exception e) {
            logger.error("chkBeginDate:" + e.getMessage());
        }
        vmd.setEndDate(d);
        return d;
    }

    public int chkType(HttpServletRequest request) {
        int type = Integer.parseInt(ParamUtil.get(request,"type"));
        if (!StrUtil.isNumeric(ParamUtil.get(request,"type"))) {
            log(SkinUtil.LoadString(request,
                                    "res.module.vehiclemaintenance",
                                    "warn_type_err_vehiclemaintenance"));
        }
        vmd.setType(type);
        return type;
    }

    public String chkCause(HttpServletRequest request) {
        String cause = ParamUtil.get(request,"cause");
        if (cause == null || cause.equals("")) {
            log(SkinUtil.LoadString(request,
                    "res.module.vehiclemaintenance", "warn_cause_err_vehiclemaintenance"));
        }
        if (!SecurityUtil.isValidSqlParam(cause))
            log(SkinUtil.LoadString(request,
                    "res.module.vehiclemaintenance", "warn_cause_err_vehiclemaintenance"));
        vmd.setCause(cause);
        return cause;
    }

    public String chkExpense(HttpServletRequest request) {
        String expense = ParamUtil.get(request,"expense");
        if (!SecurityUtil.isValidSqlParam(expense))
            log(SkinUtil.LoadString(request,
                    "res.module.vehiclemaintenance", "warn_expense_err_vehiclemaintenance"));
        vmd.setExpense(expense);
        return expense;
    }

    public String chkTransactor(HttpServletRequest request) {
        String transactor = ParamUtil.get(request,"transactor");
        if (transactor == null || transactor.equals("")) {
            log(SkinUtil.LoadString(request,
                    "res.module.vehiclemaintenance", "warn_transactor_err_vehiclemaintenance"));
        }
        if (!SecurityUtil.isValidSqlParam(transactor))
            log(SkinUtil.LoadString(request,
                    "res.module.vehiclemaintenance", "warn_transactor_err_vehiclemaintenance"));
        vmd.setTransactor(transactor);
        return transactor;
    }

    public String chkRemark(HttpServletRequest request) {
        String remark = ParamUtil.get(request,"remark");
        if (!SecurityUtil.isValidSqlParam(remark))
            log(SkinUtil.LoadString(request,
                                    "res.module.vehiclemaintenance",
                                    "warn_remark_err_vehiclemaintenance"));
        vmd.setRemark(remark);
        return remark;
    }


    public boolean checkCreate(HttpServletRequest request) throws
            ErrMsgException {
        init();
        chkLicenseNo(request);
        chkBeginDate(request);
        chkEndDate(request);
        chkType(request);
        chkCause(request);
        chkExpense(request);
        chkTransactor(request);
        chkRemark(request);
        report();
        return true;
    }

    public boolean checkModify(HttpServletRequest request) throws
            ErrMsgException {
        init();
        chkId(request);
        chkLicenseNo(request);
        chkBeginDate(request);
        chkEndDate(request);
        chkType(request);
        chkCause(request);
        chkExpense(request);
        chkTransactor(request);
        chkRemark(request);
        report();
        return true;
    }

    public boolean checkDel(HttpServletRequest request) throws ErrMsgException {
        init();
        chkId(request);
        report();
        return true;
    }
}
