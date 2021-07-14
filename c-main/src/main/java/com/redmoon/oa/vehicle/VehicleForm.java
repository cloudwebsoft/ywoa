package com.redmoon.oa.vehicle;

import java.io.*;
import java.util.*;
import javax.servlet.*;
import javax.servlet.http.*;
import cn.js.fan.base.*;
import cn.js.fan.security.*;
import cn.js.fan.util.*;
import cn.js.fan.web.*;
import com.redmoon.kit.util.*;


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
public class VehicleForm extends AbstractForm{
    FileUpload fileUpload = null;

    VehicleDb vd = new VehicleDb();

    public VehicleForm() {
    }

    public VehicleDb getVehicleDb(){
        return vd;
    }

    public FileUpload doUpload(ServletContext application, HttpServletRequest request) throws
            ErrMsgException {
        fileUpload = new FileUpload();
        fileUpload.setMaxFileSize(Global.FileSize); // 每个文件最大30000K 即近300M
        String[] extnames = {"jpg","gif"};
        fileUpload.setValidExtname(extnames);//设置可上传的文件类型

        int ret = 0;
        try {
            ret = fileUpload.doUpload(application, request);
            if (ret != fileUpload.RET_SUCCESS) {
                throw new ErrMsgException("ret=" + ret + " " + fileUpload.getErrMessage());
            }
        } catch (IOException e) {
            logger.error("doUpload:" + e.getMessage());
        }
        vd.setFileUpload(fileUpload);
        return fileUpload;
    }

    public String chkLicenseNo(HttpServletRequest request) throws
            ErrMsgException {
        String licenseNo = fileUpload.getFieldValue("licenseNo");
        if (licenseNo == null || licenseNo.equals("")) {
            log(SkinUtil.LoadString(request,
                    "res.module.vehicle", "warn_licenseno_err_vehicle"));
        }
        if (!SecurityUtil.isValidSqlParam(licenseNo))
            log(SkinUtil.LoadString(request,
                    "res.module.vehicle", "warn_licenseno_err_vehicle"));

        vd.setLicenseNo(licenseNo);
        return licenseNo;
    }

    public String chkDelLicenseNo(HttpServletRequest request) throws
            ErrMsgException {
        String licenseNo = ParamUtil.get(request, "licenseNo");

        if (licenseNo == null || licenseNo.equals("")) {
            log(SkinUtil.LoadString(request,
                    "res.module.vehicle", "warn_licenseno_err_vehicle"));
        }
        if (!SecurityUtil.isValidSqlParam(licenseNo))
            log(SkinUtil.LoadString(request,
                    "res.module.vehicle", "warn_licenseno_err_vehicle"));
        vd.setLicenseNo(licenseNo);
        return licenseNo;
    }

    public String chkEngineNo(HttpServletRequest request) {
        String engineNo = fileUpload.getFieldValue("engineNo");
        /*
        if (engineNo.equals("") || engineNo == null) {
            log(SkinUtil.LoadString(request,
                    "res.module.vehicle", "warn_engineno_err_vehicle"));
        }
        */
        if (!SecurityUtil.isValidSqlParam(engineNo))
            log(SkinUtil.LoadString(request,
                    "res.module.vehicle", "warn_engineno_err_vehicle"));
        vd.setEngineNo(engineNo);
        return engineNo;
    }

    public int chkType(HttpServletRequest request) {
        String strType = fileUpload.getFieldValue("type");
        if (!StrUtil.isNumeric(strType)) {
            log("车辆类型必须为数字，请添加类型！");
            return 0;
        }
        int type = Integer.parseInt(strType);
        if (!StrUtil.isNumeric(fileUpload.getFieldValue("type"))) {
            log(SkinUtil.LoadString(request,
                    "res.module.vehicle", "warn_type_err_vehicle"));
        }
        vd.setType(type);
        return type;
    }

    public int chkState(HttpServletRequest request) {
        int state = Integer.parseInt(fileUpload.getFieldValue("state"));
        if (!StrUtil.isNumeric(fileUpload.getFieldValue("state"))) {
            log(SkinUtil.LoadString(request,
                                    "res.module.vehicle",
                                    "warn_state_err_vehicle"));
        }
        vd.setState(state);
        return state;
    }


    public String chkDriver(HttpServletRequest request) {
        String driver = fileUpload.getFieldValue("driver");
        if (!SecurityUtil.isValidSqlParam(driver))
            log(SkinUtil.LoadString(request,
                    "res.module.vehicle", "warn_driver_err_vehicle"));
        vd.setDriver(driver);
        return driver;
    }

    public String chkPrice(HttpServletRequest request) {
        String price = fileUpload.getFieldValue("price");
        if (!SecurityUtil.isValidSqlParam(price))
            log(SkinUtil.LoadString(request,
                    "res.module.vehicle", "warn_price_err_vehicle"));
        vd.setPrice(price);
        return price;
    }

    public String chkRemark(HttpServletRequest request) {
        String remark = fileUpload.getFieldValue("remark");
        if (!SecurityUtil.isValidSqlParam(remark))
            log(SkinUtil.LoadString(request,
                                    "res.module.vehicle",
                                    "warn_remark_err_vehicle"));
        vd.setRemark(remark);
        return remark;
    }

    public String chkBrand(HttpServletRequest request) {
        String brand = fileUpload.getFieldValue("brand");
        if (!SecurityUtil.isValidSqlParam(brand))
            log(SkinUtil.LoadString(request,
                                    "res.module.vehicle",
                                    "warn_brand_err_vehicle"));
        vd.setBrand(brand);
        return brand;
    }

    public Date chkBuyDate(HttpServletRequest request) {
        String strbuyDate = fileUpload.getFieldValue("buyDate");
        java.util.Date buyDate = null;
        try {
            buyDate = DateUtil.parse(strbuyDate, "yyyy-MM-dd");
        } catch (Exception e) {
            logger.error("chkBuyDate:" + e.getMessage());
        }
        vd.setBuyDate(buyDate);
        return buyDate;
    }

    public String chkOldLicenseNo(HttpServletRequest request) throws
            ErrMsgException {
        String oldLicenseNo = fileUpload.getFieldValue("oldLicenseNo");
        if (oldLicenseNo == null || oldLicenseNo.equals("")) {
            log(SkinUtil.LoadString(request,
                    "res.module.vehicle", "warn_licenseno_err_vehicle"));
        }
        if (!SecurityUtil.isValidSqlParam(oldLicenseNo))
            log(SkinUtil.LoadString(request,
                    "res.module.vehicle", "warn_licenseno_err_vehicle"));
        vd.setOldLicenseNo(oldLicenseNo);
        return oldLicenseNo;
    }


    public boolean checkCreate(ServletContext application,HttpServletRequest request) throws
            ErrMsgException {
        init();
        doUpload(application, request);
        chkLicenseNo(request);
        chkEngineNo(request);
        chkType(request);
        chkDriver(request);
        chkPrice(request);
        chkBuyDate(request);
        chkState(request);
        chkRemark(request);
        chkBrand(request);
        report();
        return true;
    }

    public boolean checkModify(ServletContext application,HttpServletRequest request) throws
            ErrMsgException {
        init();
        FileUpload fu = doUpload(application, request);
        String oldLicenseNo = chkOldLicenseNo(request);
        String licenseNo = chkLicenseNo(request);
        vd = vd.getVehicleDb(licenseNo);
        vd.setFileUpload(fu);
        vd.setOldLicenseNo(oldLicenseNo);
        chkEngineNo(request);
        chkType(request);
        chkDriver(request);
        chkPrice(request);
        chkBuyDate(request);
        chkState(request);
        chkRemark(request);
        chkBrand(request);
        report();
        return true;
    }

    public boolean checkDel(HttpServletRequest request) throws ErrMsgException {
        init();
        chkDelLicenseNo(request);
        report();
        return true;
    }

}
