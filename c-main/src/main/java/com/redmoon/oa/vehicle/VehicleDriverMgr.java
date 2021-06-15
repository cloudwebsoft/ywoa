package com.redmoon.oa.vehicle;

import javax.servlet.http.*;
import cn.js.fan.util.*;


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
public class VehicleDriverMgr {
    public VehicleDriverMgr() {
    }

    public boolean create(HttpServletRequest request) throws
            ErrMsgException {
        VehicleDriverForm vdf = new VehicleDriverForm();
        vdf.checkCreate(request);

        VehicleDriverDb vdd = vdf.getVehicleDriverDb();
        return vdd.create();
    }

    public boolean del(HttpServletRequest request) throws
            ErrMsgException {
        VehicleDriverForm vdf = new VehicleDriverForm();
        vdf.checkDel(request);

        VehicleDriverDb vdd = vdf.getVehicleDriverDb();
        vdd = vdd.getVehicleDriverDb(vdd.getId());
        return vdd.del();
    }

    public boolean modify(HttpServletRequest request) throws
            ErrMsgException {
        VehicleDriverForm vdf = new VehicleDriverForm();
        vdf.checkModify(request);

        VehicleDriverDb vtd = vdf.getVehicleDriverDb();
        return vtd.save();
    }
}
