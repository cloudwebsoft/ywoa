package com.redmoon.oa.ui.menu;

import java.util.Vector;

import javax.servlet.http.HttpServletRequest;

import com.cloudwebsoft.framework.util.LogUtil;
import com.redmoon.oa.kernel.License;
import com.redmoon.oa.pvg.Privilege;

public class MenuController {
	public static final String SUPERVIS = "supervis";
	public static final String SUPERVIS_NAME = "部门工作";
	public static final String SUPERVIS_NAME2 = "督办";
	public static final String SUPERVIS_PARENT_CODE = "administration";
	
	public static final String SALES = "sales";
	
	public static boolean canUserSee(HttpServletRequest request, Leaf leaf) {
		Privilege privilege = new Privilege();
		boolean flag = privilege.isUserLogin(request);
		// System.out.println(MenuController.class.getName() + " " + leaf.getCode() + " " + leaf.getName());
		if(leaf != null && leaf.isLoaded()){
			if (leaf.getCode().equals(SUPERVIS) || (leaf.getParentCode().equals(SUPERVIS_PARENT_CODE) && (leaf.getName().equals(SUPERVIS_NAME) ||leaf.getName().equals(SUPERVIS_NAME2)))){
				if (privilege.getUser(request).equals(Privilege.ADMIN)) {
					return true;
				}
				String userName = privilege.getUser(request);
				Vector v = Privilege.getUserAdminDepts(userName);
				if (v.size() == 0){
					flag = false;
				}
			}
		}
		return flag;
	}
}
