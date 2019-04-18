package com.redmoon.oa.basic;

import java.util.Calendar;
import cn.js.fan.db.Conn;
import cn.js.fan.util.*;
import java.sql.*;
import com.redmoon.oa.pvg.Privilege;
import cn.js.fan.util.ErrMsgException;
import javax.servlet.http.*;
import org.apache.log4j.Logger;
import cn.js.fan.web.Global;
/**
 * <p>Title: </p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2003</p>
 * <p>Company: </p>
 * @author not attributable
 * @version 1.0
 */
//日程安排
public class SelectKindMgr {

  public SelectKindMgr() {

  }

  public boolean modify(HttpServletRequest request) throws ErrMsgException {
      Privilege privilege = new Privilege();
      if (!privilege.isUserLogin(request))
          throw new ErrMsgException("请先登录！");
      boolean re = true;
      String errmsg = "";

      int id = ParamUtil.getInt(request, "id");
      String name = ParamUtil.get(request, "name");
      if (name.equals(""))
          errmsg += "名称不能为空！\\n";
      if (!errmsg.equals(""))
          throw new ErrMsgException(errmsg);
      
      int orders = ParamUtil.getInt(request, "orders", 0);

      SelectKindDb wptd =  getSelectKindDb(id);
      wptd.setName(name);
      wptd.setOrders(orders);
      re = wptd.save();
      return re;
  }

  public SelectKindDb getSelectKindDb(int id) {
      SelectKindDb addr = new SelectKindDb();
      return addr.getSelectKindDb(id);
  }

  public boolean create(HttpServletRequest request) throws ErrMsgException {
      Privilege privilege = new Privilege();
      if (!privilege.isUserLogin(request))
          throw new ErrMsgException("请先登录！");
      boolean re = true;

      String errmsg = "";
      String name = ParamUtil.get(request, "name");
      if (name.equals(""))
          errmsg += "名称不能为空！\\n";
      int orders = ParamUtil.getInt(request, "orders", 0);
      
      if (!errmsg.equals(""))
          throw new ErrMsgException(errmsg);

      SelectKindDb wptd = new SelectKindDb();
      wptd.setName(name);
      wptd.setOrders(orders);
      re = wptd.create();
      return re;
  }

  public boolean del(HttpServletRequest request) throws ErrMsgException {
      int id = ParamUtil.getInt(request, "id");
      SelectKindDb SelectKindDb = getSelectKindDb(id);
      if (SelectKindDb==null || !SelectKindDb.isLoaded())
          throw new ErrMsgException("该项已不存在！");

      return SelectKindDb.del();
  }
}
