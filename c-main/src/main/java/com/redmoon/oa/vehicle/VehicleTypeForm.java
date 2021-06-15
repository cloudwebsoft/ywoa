package com.redmoon.oa.vehicle;

import javax.servlet.http.*;
import cn.js.fan.base.*;
import cn.js.fan.security.*;
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
public class VehicleTypeForm extends AbstractForm{
    VehicleTypeDb vtd = new VehicleTypeDb();

    public VehicleTypeForm() {
    }

    public VehicleTypeDb getVehicleTypeDb() {
        return vtd;
    }

    public int chkId(HttpServletRequest request) {
        String strId = ParamUtil.get(request, "id");
        if (!StrUtil.isNumeric(strId)){
            log(SkinUtil.LoadString(request,
                    "res.module.vehicletype", "warn_id_err_vehicletype"));
            return 0;
        }
        int id = Integer.parseInt(strId);
        vtd.setId(id);
        return id;
    }


    public String chkTypeCode(HttpServletRequest request) {
        String typeCode = ParamUtil.get(request, "typecode");
        if (typeCode == null || typeCode.equals("")) {
            log(SkinUtil.LoadString(request,
                    "res.module.vehicletype", "warn_typecode_err_vehicletype"));
        }
        if (!SecurityUtil.isValidSqlParam(typeCode))
            log(SkinUtil.LoadString(request,
                    "res.module.vehicletype", "warn_typecode_err_vehicletype"));
        vtd.setTypeCode(typeCode);
        return typeCode;
    }

    public String chkDescription(HttpServletRequest request) {
        String description = ParamUtil.get(request, "description");
        if (description == null || description.equals("")) {
            log(SkinUtil.LoadString(request,
                                    "res.module.vehicletype",
                                    "warn_description_err_vehicletype"));
        }
        if (!SecurityUtil.isValidSqlParam(description))
            log(SkinUtil.LoadString(request,
                                    "res.module.vehicletype",
                                    "warn_description_err_vehicletype"));
        vtd.setDescription(description);
        return description;
    }

    public boolean checkCreate(HttpServletRequest request) throws
            ErrMsgException {
        init();
        chkTypeCode(request);
        chkDescription(request);
        report();
        return true;
    }

    public boolean checkModify(HttpServletRequest request) throws
            ErrMsgException {
        init();
        chkId(request);
        chkTypeCode(request);
        chkDescription(request);
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
