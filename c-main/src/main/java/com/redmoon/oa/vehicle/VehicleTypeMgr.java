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
public class VehicleTypeMgr {
    public VehicleTypeMgr() {
    }

    public boolean create(HttpServletRequest request) throws
            ErrMsgException {
        VehicleTypeForm vtf = new VehicleTypeForm();
        vtf.checkCreate(request);

        VehicleTypeDb vtd = vtf.getVehicleTypeDb();
        return vtd.create();
    }

    public boolean del(HttpServletRequest request) throws
            ErrMsgException {
        VehicleTypeForm vtf = new VehicleTypeForm();
        vtf.checkDel(request);

        VehicleTypeDb vtd = vtf.getVehicleTypeDb();
        vtd = vtd.getVehicleTypeDb(vtd.getId());
        return vtd.del();
    }

    public boolean modify(HttpServletRequest request) throws
            ErrMsgException {
        VehicleTypeForm vtf = new VehicleTypeForm();
        vtf.checkModify(request);

        VehicleTypeDb vtd = vtf.getVehicleTypeDb();
        return vtd.save();
    }
}
