package com.redmoon.oa.kaoqin;

import javax.servlet.http.HttpServletRequest;

import com.redmoon.oa.pvg.*;

public class KaoqinPrivilege extends Privilege {
	
	public boolean canAdminUser(HttpServletRequest request, String userName) {
		if (super.canAdminUser(request, userName))
			return true;
		if (this.isUserPrivValid(request, "kaoqin.admin"))
			return true;
		return false;
	}
	public boolean canAdminKaoqin(HttpServletRequest request){
		if (this.isUserPrivValid(request, "admin"))
			return true;
		if (this.isUserPrivValid(request, "kaoqin.admin"))
			return true;
		return false;
	}
}
