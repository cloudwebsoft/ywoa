package com.redmoon.oa.person;

import java.util.Calendar;
import java.util.Iterator;

import cn.js.fan.db.Conn;
import cn.js.fan.util.*;

import java.sql.*;

import com.cloudwebsoft.framework.util.LogUtil;
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
public class PersonGroupTypeMgr {

  public PersonGroupTypeMgr() {

  }

  public boolean modify(HttpServletRequest request) throws ErrMsgException {
      Privilege privilege = new Privilege();
      if (!privilege.isUserLogin(request))
          throw new ErrMsgException("请先登录！");
      String errmsg = "";

      int id = ParamUtil.getInt(request, "id");
      String name = ParamUtil.get(request, "name");
      if (name.equals(""))
          errmsg += "名称不能为空！\\n";
      if (!errmsg.equals(""))
          throw new ErrMsgException(errmsg);
      
      int orders = ParamUtil.getInt(request, "orders", 0);

      PersonGroupTypeDb pgtd = getPersonGroupTypeDb(id);
      pgtd.setName(name);
      pgtd.setOrders(orders);
      return pgtd.save();
  }

  public PersonGroupTypeDb getPersonGroupTypeDb(int id) {
	  PersonGroupTypeDb addr = new PersonGroupTypeDb();
      return addr.getPersonGroupTypeDb(id);
  }

  public boolean create(HttpServletRequest request) throws ErrMsgException {
      Privilege privilege = new Privilege();
      if (!privilege.isUserLogin(request))
          throw new ErrMsgException("请先登录！");

      String errmsg = "";
      String name = ParamUtil.get(request, "name");
      if (name.equals(""))
          errmsg += "名称不能为空！\\n";
      int orders = ParamUtil.getInt(request, "orders", 0);
      
      if (!errmsg.equals(""))
          throw new ErrMsgException(errmsg);

      PersonGroupTypeDb pgtd = new PersonGroupTypeDb();
      pgtd.setName(name);
      pgtd.setOrders(orders);
      pgtd.setUserName(privilege.getUser(request));
      
      return pgtd.create();
  }

  public boolean del(HttpServletRequest request) throws ErrMsgException {
      int id = ParamUtil.getInt(request, "id");
      PersonGroupTypeDb pgtd = getPersonGroupTypeDb(id);
      if (pgtd==null || !pgtd.isLoaded())
          throw new ErrMsgException("该项已不存在！");

      if (pgtd.del()) {
    	  // 删除组内用户
    	  PersonGroupUserDb pgud = new PersonGroupUserDb();
    	  String sql = "select id from " + pgud.getTable().getName() + " where group_id=?";
    	  
    	  Iterator ir = pgud.list(sql, new Object[]{new Integer(id)}).iterator();
    	  while (ir.hasNext()) {
    		  pgud = (PersonGroupUserDb)ir.next();
    		  try {
				pgud.del();
			} catch (ResKeyException e) {
				LogUtil.getLog(getClass()).error(StrUtil.trace(e));
			}
    	  }
    	  return true;
      }
      else
    	  return false;
  }
}
