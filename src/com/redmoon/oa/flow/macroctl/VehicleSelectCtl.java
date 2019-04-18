package com.redmoon.oa.flow.macroctl;

import java.util.Vector;

import cn.js.fan.util.StrUtil;

import com.redmoon.oa.vehicle.VehicleDb;
import com.redmoon.oa.vehicle.VehicleDriverDb;

import java.util.Iterator;
import javax.servlet.http.HttpServletRequest;

import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.redmoon.oa.flow.FormField;
import com.redmoon.oa.flow.WorkflowDb;

/**
 * <p>Title: </p>
 *
 * <p>Description: </p>
 *
 * <p>Copyright: Copyright (c) 2006</p>
 *
 * <p>Company: </p>
 *
 * @author not attributable
 * @version 1.0
 */
public class VehicleSelectCtl extends AbstractMacroCtl  {
    public VehicleSelectCtl() {
    }

    public String convertToHTMLCtl(HttpServletRequest request, FormField ff) {
        String str = "";
        str += "<select name='" + ff.getName() + "'><option value=''>无</option>";
        VehicleDb vd = new VehicleDb();
        Vector v = vd.list();
        if (v != null) {
            Iterator ir = v.iterator();
            while (ir.hasNext()) {
                vd = (VehicleDb) ir.next();
                str += "<option value='" + vd.getLicenseNo() + "'>" +
                        vd.getLicenseNo() +
                        "</option>";
            }
        }
        str += "</select>";
        return str;
    }

    public String convertToHTMLCtlForQuery(HttpServletRequest request, FormField ff) {
        String str = "";
        str += "<select id='" + ff.getName() + "' name='" + ff.getName() + "'>";
        str += "<option value=''>无</option>";
        VehicleDb vd = new VehicleDb();
        Vector v = vd.list();
        if (v != null) {
            Iterator ir = v.iterator();
            while (ir.hasNext()) {
                vd = (VehicleDb) ir.next();
                str += "<option value='" + vd.getLicenseNo() + "'>" +
                        vd.getLicenseNo() +
                        "</option>";
            }
        }
        str += "</select>";
        return str;
    }

    public String getControlType() {
         return "select";
     }

     public String getControlValue(String userName, FormField ff) {
    	 if (ff.getValue() != null && !ff.getValue().trim().equals("")) {
 			return ff.getValue();
 		}
 		return "";
     }

     public String getControlText(String userName, FormField ff) {
    	 String v = StrUtil.getNullStr(ff.getValue());
 		VehicleDb vd = new VehicleDb();
 		if (!v.equals("")) {
 			vd = vd.getVehicleDb(v);
 			String str = vd.getLicenseNo();
 			return str;
 		}
 		return "";
     }

     public String getControlOptions(String userName, FormField ff) {
    	 VehicleDb vd = new VehicleDb();
		JSONArray selects = new JSONArray();
		Vector v = vd.list();
		try {
			if (v != null) {
				Iterator ir = v.iterator();
				while (ir.hasNext()) {
					vd = (VehicleDb) ir.next();
					JSONObject option = new JSONObject();
					option.put("name", vd.getLicenseNo());
					option.put("value", vd.getLicenseNo());
					selects.put(option);
				}
			}
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			Logger.getLogger(VehicleSelectCtl.class).error(e.getMessage());
		}
		return selects.toString();

     }

}
