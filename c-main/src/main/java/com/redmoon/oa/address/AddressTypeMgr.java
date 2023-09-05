package com.redmoon.oa.address;
import cn.js.fan.util.*;

import com.redmoon.oa.dept.DeptDb;
import com.redmoon.oa.pvg.Privilege;
import cn.js.fan.util.ErrMsgException;
import javax.servlet.http.*;

/**
 * <p>Title: </p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2003</p>
 * <p>Company: </p>
 * @author not attributable
 * @version 1.0
 */
//日程安排
public class AddressTypeMgr {

  public AddressTypeMgr() {

  }

  public boolean modify(HttpServletRequest request) throws ErrMsgException {
      boolean re = true;
      String errmsg = "";

      int id = ParamUtil.getInt(request, "id");
      String name = ParamUtil.get(request, "name");
      if (name.equals(""))
          errmsg += "名称不能为空！\\n";
      if (!errmsg.equals(""))
          throw new ErrMsgException(errmsg);

      AddressTypeDb wptd =  getAddressTypeDb(id);
      wptd.setName(name);
      re = wptd.save();
    
      return re;
  }

  public AddressTypeDb getAddressTypeDb(int id) {
      AddressTypeDb addr = new AddressTypeDb();
      return addr.getAddressTypeDb(id);
  }

  public boolean create(HttpServletRequest request) throws ErrMsgException {
      Privilege privilege = new Privilege();
      boolean re = true;
      String errmsg = "";
      String name = ParamUtil.get(request, "name");
      if (name.equals(""))
          errmsg += "名称不能为空！\\n";
      if (!errmsg.equals(""))
          throw new ErrMsgException(errmsg);
      int type = ParamUtil.getInt(request, "type");
      AddressTypeDb wptd = new AddressTypeDb();
      String user = privilege.getUser(request);
      if (type == AddressDb.TYPE_PUBLIC)
          user = AddressTypeDb.PUBLIC;
      String unitCode = privilege.getUserUnitCode(request);
      wptd.setUnitCode(unitCode);
      boolean isExist = false;
      if (type==AddressDb.TYPE_PUBLIC) {
    	  isExist = wptd.isExist(name, user, unitCode);
      }
      else {
    	  isExist = wptd.isExist(name, user);
      }
      if (isExist)
          throw new ErrMsgException("该类别已存在!");
      else {
          wptd.setName(name);
          wptd.setUserName(user);
          re = wptd.create();
          
         
      }
      return re;
  }

  public boolean del(HttpServletRequest request) throws ErrMsgException {
      int id = ParamUtil.getInt(request, "id");
      AddressTypeDb AddressTypeDb = getAddressTypeDb(id);
      if (AddressTypeDb==null || !AddressTypeDb.isLoaded())
          throw new ErrMsgException("该项已不存在！");
      boolean re = AddressTypeDb.del();
      
      return re;
  }
  
	
	/*
	 *  发现struts 调用ParamUtil.get 方法获取含有中文的会出现乱码，估计是重复设定字符集
	 *  所以重新建立个方法以request.getParameter方法来获取
	 */
	public boolean createByMoblie(HttpServletRequest request) throws ErrMsgException {
		Privilege privilege = new Privilege();
	      boolean re = true;
	      String errmsg = "";
	      String name = request.getParameter("name");
	      if (name.equals(""))
	          errmsg += "名称不能为空！\\n";
	      if (!errmsg.equals(""))
	          throw new ErrMsgException(errmsg);
	      int type = ParamUtil.getInt(request, "type");
	      AddressTypeDb wptd = new AddressTypeDb();
	      String user = privilege.getUser(request);
	      if (type == AddressDb.TYPE_PUBLIC)
	          user = AddressTypeDb.PUBLIC;
	      String unitCode = privilege.getUserUnitCode(request);
	      wptd.setUnitCode(unitCode);
	      boolean isExist = false;
	      if (type==AddressDb.TYPE_PUBLIC) {
	    	  isExist = wptd.isExist(name, user, unitCode);
	      }
	      else {
	    	  isExist = wptd.isExist(name, user);
	      }
	      if (isExist)
	          throw new ErrMsgException("该类别已存在!");
	      else {
	          wptd.setName(name);
	          wptd.setUserName(user);
	          re = wptd.create();
	      }

	      return re;
	  }
	
	
	public boolean modifyByMobile(HttpServletRequest request) throws ErrMsgException {
		  Privilege privilege = new Privilege();
	      boolean re = true;
	      String errmsg = "";

	      int id = ParamUtil.getInt(request, "id");
	      String name = request.getParameter("name");
	      if (name.equals(""))
	          errmsg += "名称不能为空！\\n";
	      if (!errmsg.equals(""))
	          throw new ErrMsgException(errmsg);

	      AddressTypeDb wptd =  getAddressTypeDb(id);
	      wptd.setName(name);
	      re = wptd.save();
	    
	      return re;
	}
	
}
