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
public class VehicleMaintenanceMgr {
    public VehicleMaintenanceMgr() {
    }

    public VehicleMaintenanceDb getVehicleMaintenanceDb(int id) throws
            ErrMsgException {
        VehicleMaintenanceDb vmd = new VehicleMaintenanceDb(id);
        return vmd;
    }


    public boolean create(HttpServletRequest request) throws
            ErrMsgException {
        VehicleMaintenanceForm vmf = new VehicleMaintenanceForm();
        vmf.checkCreate(request);
        VehicleMaintenanceDb vmd = vmf.getVehicleMaintenanceDb();
        return vmd.create();
    }

    public boolean del(HttpServletRequest request) throws
            ErrMsgException {
        VehicleMaintenanceForm vmf = new VehicleMaintenanceForm();
        vmf.checkDel(request);

        VehicleMaintenanceDb vmd = vmf.getVehicleMaintenanceDb();
        vmd = vmd.getVehicleMaintenanceDb(vmd.getId());
        return vmd.del();
    }

    public boolean modify(HttpServletRequest request) throws
            ErrMsgException {
        VehicleMaintenanceForm vmf = new VehicleMaintenanceForm();
        vmf.checkModify(request);
        VehicleMaintenanceDb vmd = vmf.getVehicleMaintenanceDb();
        return vmd.save();
    }
}
