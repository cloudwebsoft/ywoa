package com.redmoon.oa.vehicle;

import javax.servlet.http.HttpServletRequest;
import cn.js.fan.security.SecurityUtil;
import cn.js.fan.web.SkinUtil;
import cn.js.fan.base.*;
import java.util.Date;
import cn.js.fan.util.*;
import cn.js.fan.web.*;

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
public class VehicleSQLBuilder extends AbstractForm{
    public static final String RESULT_APPLY = "-";
    public static final String RESULT_AGREE = "是";
    public static final String RESULT_DISAGREE = "否";
    public static final String RESULT_USED = "使用"; // 当result为agree且当时正处于开始和结束期间内

    public VehicleSQLBuilder() {
    }

    public static String getVehicleMaintenanceSearchSql (HttpServletRequest request)throws ErrMsgException{
        String licenseNo = ParamUtil.get(request, "licenseNo");
        if (!SecurityUtil.isValidSqlParam(licenseNo))
            throw new ErrMsgException(SkinUtil.LoadString(request,
                    "res.module.vehiclesqlbuilder",
                    "warn_licenseno_err_vehiclemaintenance"));

        String strBeginDate = ParamUtil.get(request, "beginDate");
        String strEndDate = ParamUtil.get(request, "endDate");

        int type = -1;
        if(!ParamUtil.get(request, "type").equals("")){
            type = Integer.parseInt(ParamUtil.get(request, "type"));
            if (!StrUtil.isNumeric(ParamUtil.get(request, "type")))
                throw new ErrMsgException(SkinUtil.LoadString(request,
                        "res.module.vehiclesqlbuilder",
                        "warn_type_err_vehiclemaintenance"));
        }

        String cause = ParamUtil.get(request, "cause");
        if (!SecurityUtil.isValidSqlParam(cause))
            throw new ErrMsgException(SkinUtil.LoadString(request,
                    "res.module.vehiclesqlbuilder",
                    "warn_cause_err_vehiclemaintenance"));

        String expense = ParamUtil.get(request, "expense");
        if (!SecurityUtil.isValidSqlParam(expense))
            throw new ErrMsgException(SkinUtil.LoadString(request,
                    "res.module.vehiclesqlbuilder",
                    "warn_expense_err_vehiclemaintenance"));

        String transactor = ParamUtil.get(request, "transactor");
        if (!SecurityUtil.isValidSqlParam(transactor))
            throw new ErrMsgException(SkinUtil.LoadString(request,
                    "res.module.vehiclesqlbuilder",
                    "warn_transactor_err_vehiclemaintenance"));

        String remark = ParamUtil.get(request, "remark");
        if (!SecurityUtil.isValidSqlParam(remark))
            throw new ErrMsgException(SkinUtil.LoadString(request,
                    "res.module.vehiclesqlbuilder",
                    "warn_remark_err_vehiclemaintenance"));

        String sql = "select id from vehicle_maintenance where licenseNo = " + StrUtil.sqlstr(licenseNo);
        if(type != -1){
            sql +=  " and type = " + type;
        }
        if (!cause.equals("")) {
            sql += " and cause like " +
                    StrUtil.sqlstr("%" + cause + "%");
        }
        if (!transactor.equals("")) {
            sql += " and transactor like " +
                    StrUtil.sqlstr("%" + transactor + "%");
        }
        if (!remark.equals("")) {
            sql += " and remark like " +
                    StrUtil.sqlstr("%" + remark + "%");
        }
        if (!expense.equals("")) {
            sql += " and expense like " +
                    StrUtil.sqlstr("%" + expense + "%");
        }

        if(!strBeginDate.equals("")){
            sql += " and beginDate >= " + strBeginDate;
        }
        if(!strEndDate.equals("")) {
            sql += " and endDate <= " + strEndDate;
        }
        return sql;
    }

    public static String getVehicleResultSearchSql(HttpServletRequest
            request) throws ErrMsgException {
        String licenseNo = ParamUtil.get(request, "licenseNo");
        if (!SecurityUtil.isValidSqlParam(licenseNo))
            throw new ErrMsgException(SkinUtil.LoadString(request,
                    "res.module.vehiclesqlbuilder",
                    "warn_licenseno_err_vehicleuse"));

        String strBeginDate = ParamUtil.get(request, "beginDate");
        String strEndDate = ParamUtil.get(request, "endDate");

        String result = ParamUtil.get(request, "result");

        String person = ParamUtil.get(request, "person");
        if (!SecurityUtil.isValidSqlParam(person))
            throw new ErrMsgException(SkinUtil.LoadString(request,
                    "res.module.vehiclesqlbuilder",
                    "warn_licenseno_err_vehicleuse"));

        String depts = ParamUtil.get(request, "depts");
        if (!SecurityUtil.isValidSqlParam(depts))
            throw new ErrMsgException(SkinUtil.LoadString(request,
                    "res.module.vehiclesqlbuilder",
                    "warn_licenseno_err_vehicleuse"));

        String applier = ParamUtil.get(request, "applier");
        if (!SecurityUtil.isValidSqlParam(applier))
            throw new ErrMsgException(SkinUtil.LoadString(request,
                    "res.module.vehiclesqlbuilder",
                    "warn_licenseno_err_vehicleuse"));


        String sql = "select flowId from form_table_vehicle_apply where licenseNo = " +
                     StrUtil.sqlstr(licenseNo);
        sql += " and myresult = " + StrUtil.sqlstr(result);

        if (!person.equals("")) {
            sql += " and person like " +
                    StrUtil.sqlstr("%" + person + "%");
        }
        if (!depts.equals("")) {
            sql += " and dept like " +
                    StrUtil.sqlstr("%" + depts + "%");
        }
        if (!applier.equals("")) {
            sql += " and remark like " +
                    StrUtil.sqlstr("%" + applier + "%");
        }

        if (!strBeginDate.equals("")) {
            sql += " and beginDate >= " + strBeginDate;
        }
        if (!strEndDate.equals("")) {
            sql += " and endDate <= " + strEndDate;
        }
        return sql;
    }

    public static String getVehicleApplySearchSql(){
        String sql = "select flowId from form_table_vehicle_apply where myresult = " +
                     StrUtil.sqlstr(RESULT_APPLY) + " and cws_status <> -1 order by flowId desc";
        return sql;
    }

    public static String getVehicleAgreeSearchSql() {
        String sql =
                "select flowId from form_table_vehicle_apply where myresult = " +
                StrUtil.sqlstr(RESULT_AGREE) + " order by flowId desc";
        return sql;
    }

    public static String getVehicleDisagreeSearchSql(){
        String sql = "select flowId from form_table_vehicle_apply where myresult = " +
                     StrUtil.sqlstr(RESULT_DISAGREE) + " order by flowId desc";
        return sql;
    }

    public static String getVehicleUsedSearchSql() {
        String sql = "";
        if (Global.db.equals(Global.DB_SQLSERVER)) {
            sql =
                    "select flowId from form_table_vehicle_apply where myresult = " +
                    StrUtil.sqlstr(RESULT_AGREE) +
                    " and beginDate <= getDate() and endDate > getDate() order by flowId desc";
        }
        else if (Global.db.equals(Global.DB_ORACLE)) {
            sql =
                    "select flowId from form_table_vehicle_apply where myresult = " +
                    StrUtil.sqlstr(RESULT_AGREE) +
                    " and beginDate <= sysdate and endDate > sysdate order by flowId desc";
        }
        else {
            sql =
                    "select flowId from form_table_vehicle_apply where myresult = " +
                    StrUtil.sqlstr(RESULT_AGREE) +
                    " and beginDate <= now() and endDate > now() order by flowId desc";
        }
        return sql;
    }


}
