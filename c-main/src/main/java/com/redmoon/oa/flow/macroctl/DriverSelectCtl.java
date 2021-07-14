package com.redmoon.oa.flow.macroctl;

import java.util.Iterator;
import java.util.Vector;

import javax.servlet.http.HttpServletRequest;

import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import cn.js.fan.util.StrUtil;

import com.redmoon.oa.flow.FormField;
import com.redmoon.oa.vehicle.VehicleDriverDb;

/**
 * <p>
 * Title:
 * </p>
 * 
 * <p>
 * Description:
 * </p>
 * 
 * <p>
 * Copyright: Copyright (c) 2006
 * </p>
 * 
 * <p>
 * Company:
 * </p>
 * 
 * @author not attributable
 * @version 1.0
 */
public class DriverSelectCtl extends AbstractMacroCtl {
	public DriverSelectCtl() {
	}

	public String convertToHTMLCtl(HttpServletRequest request, FormField ff) {
		String str = "";
		if (ff.isEditable()) {
			str += "<select name='" + ff.getName() + "'><option value=''>无</option>";
			VehicleDriverDb vd = new VehicleDriverDb();
			Vector v = vd.list();
			if (v != null) {
				Iterator ir = v.iterator();
				while (ir.hasNext()) {
					vd = (VehicleDriverDb) ir.next();
					str += "<option value='" + vd.getId() + "'>"
							+ vd.getUserName() + "</option>";
				}
			}
			str += "</select>";
		} else {
			String value = String.valueOf(ff.getValue());
			if (value.trim().equals("")) {
				return "";
			}
			VehicleDriverDb vd = new VehicleDriverDb(StrUtil.toInt(ff
					.getValue()));
			if (!vd.isLoaded()) {
				return "";
			}
			str += "<span>" + vd.getUserName() + "</span>";
		}
		return str;
	}

	@Override
    public String convertToHTMLCtlForQuery(HttpServletRequest request,
                                           FormField ff) {
		String str = "";
		str += "<select id='" + ff.getName() + "' name='" + ff.getName() + "'>";
		str += "<option value=''>无</option>";
		VehicleDriverDb vd = new VehicleDriverDb();
		Vector v = vd.list();
		if (v != null) {
			Iterator ir = v.iterator();
			while (ir.hasNext()) {
				vd = (VehicleDriverDb) ir.next();
				str += "<option value='" + vd.getUserName() + "'>"
						+ vd.getUserName() + "</option>";
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
		VehicleDriverDb vd = new VehicleDriverDb();
		if (!v.equals("")) {
			vd = vd.getVehicleDriverDb(StrUtil.toInt(v));
			String str = vd.getUserName();
			return str;
		}
		return "";
	}

	public String getControlOptions(String userName, FormField ff) {
		VehicleDriverDb vd = new VehicleDriverDb();
		JSONArray selects = new JSONArray();
		Vector v = vd.list();
		try {
			if (v != null) {
				Iterator ir = v.iterator();
				while (ir.hasNext()) {
					vd = (VehicleDriverDb) ir.next();
					JSONObject option = new JSONObject();
					option.put("name", vd.getUserName());
					option.put("value", vd.getId());
					selects.put(option);
				}
			}
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			Logger.getLogger(DriverSelectCtl.class).error(e.getMessage());
		}
		return selects.toString();

	}
	
	public String converToHtml(HttpServletRequest request, FormField ff, String fieldValue) {
        String v = StrUtil.getNullStr(fieldValue);

        if (!v.equals("")) {
        	VehicleDriverDb vd = new VehicleDriverDb();
        	vd = vd.getVehicleDriverDb(StrUtil.toInt(v));
        	v = vd.getUserName();
        }
        return v;
    }  
	
	public String getReplaceCtlWithValueScript(FormField ff) {
  	  String v = StrUtil.getNullStr(ff.getValue());

        if (!v.equals("")) {
        	VehicleDriverDb vd = new VehicleDriverDb();
        	vd = vd.getVehicleDriverDb(StrUtil.toInt(v));
        	v = vd.getUserName();
        }

        String str = "if (o('" + ff.getName() + "_realname')) o('" + ff.getName() + "_realname').parentNode.removeChild(o('" + ff.getName() + "_realname'));\n";
        str += "ReplaceCtlWithValue('" + ff.getName() +"', '" + ff.getType() + "','" + v + "');\n";
        return str;
    }
}
