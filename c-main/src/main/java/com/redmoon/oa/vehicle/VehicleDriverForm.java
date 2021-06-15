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
public class VehicleDriverForm extends AbstractForm{
    VehicleDriverDb vdd = new VehicleDriverDb();

    public VehicleDriverForm() {
    }

    public VehicleDriverDb getVehicleDriverDb() {
        return vdd;
    }

    public int chkId(HttpServletRequest request) {
        String strId = ParamUtil.get(request, "id");
        if (!StrUtil.isNumeric(strId)){
            log(SkinUtil.LoadString(request,
                    "res.module.vehicledriver", "warn_id_err_vehicledriver"));
            return 0;
        }
        int id = Integer.parseInt(strId);
        vdd.setId(id);
        return id;
    }


    public String chkUserName(HttpServletRequest request) {
        String userName = ParamUtil.get(request, "userName");
        if (userName == null || userName.equals("")) {
            log(SkinUtil.LoadString(request,
                    "res.module.vehicledriver", "warn_username_err_vehicledriver"));
        }
        if (!SecurityUtil.isValidSqlParam(userName))
            log(SkinUtil.LoadString(request,
                    "res.module.vehicledriver", "warn_username_err_vehicletype"));
        vdd.setUserName(userName);
        return userName;
    }

    public boolean checkCreate(HttpServletRequest request) throws
            ErrMsgException {
        init();
        chkUserName(request);
        report();
        return true;
    }

    public boolean checkModify(HttpServletRequest request) throws
            ErrMsgException {
        init();
        chkId(request);
        chkUserName(request);
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
