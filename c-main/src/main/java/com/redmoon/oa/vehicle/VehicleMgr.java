package com.redmoon.oa.vehicle;

import javax.servlet.*;
import javax.servlet.http.*;

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
public class VehicleMgr {
    String connname = Global.getDefaultDB();

    public VehicleMgr() {
    }



    public VehicleDb getVehicleDb(String licenseNo) throws
            ErrMsgException{
        VehicleDb vd = new VehicleDb(licenseNo);
        return vd;
    }

    public boolean create(ServletContext application,HttpServletRequest request) throws
            ErrMsgException{
        VehicleForm vf = new VehicleForm();
        vf.checkCreate(application,request);
        VehicleDb vd = vf.getVehicleDb();
        return vd.create();
    }

    public boolean del(ServletContext application,HttpServletRequest request) throws
            ErrMsgException {
        VehicleForm vf = new VehicleForm();
        vf.checkDel(request);
        VehicleDb vd = vf.getVehicleDb();
        return vd.del(application.getRealPath("/"));
    }

    public boolean modify(ServletContext application,HttpServletRequest request) throws
            ErrMsgException {
        VehicleForm vf = new VehicleForm();
        vf.checkModify(application,request);
        VehicleDb vd = vf.getVehicleDb();
        return vd.save(vd.getFileUpload());
    }
}
