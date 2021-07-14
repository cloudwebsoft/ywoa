package com.redmoon.oa.visual.module.sales;
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
public class CustomerShareMgr {
  Logger logger = Logger.getLogger( CustomerShareMgr.class.getName() );

  public CustomerShareMgr() {

  }

  public boolean modify(HttpServletRequest request) throws ErrMsgException {
      //if (!privilege.isUserLogin(request))
        //  throw new ErrMsgException("请先登录！");
      boolean re = true;
      String errmsg = "";

      int id = ParamUtil.getInt(request, "id");
      int customerId = ParamUtil.getInt(request, "customerId");
      String sharePerson = ParamUtil.get(request, "sharePerson");
      if (!errmsg.equals(""))
          throw new ErrMsgException(errmsg);
      CustomerShareDb cd =  getCustomerShareDb(id);
      cd.setCustomerId(customerId);
      cd.setSharePerson(sharePerson);
      re = cd.save();
      return re;
  }

  public CustomerShareDb getCustomerShareDb(int id) {
      CustomerShareDb addr = new CustomerShareDb();
      return addr.getCustomerShareDb(id);
  }

  public void addBatch(HttpServletRequest request) throws ErrMsgException {
      String strIds = ParamUtil.get(request, "ids");
      // Privilege privilege = new Privilege();
      //if (!privilege.isUserLogin(request))
      //  throw new ErrMsgException("请先登录！");
      String sharePerson = ParamUtil.get(request, "sharePerson");
      String[] ids = strIds.split(",");
      int len = ids.length;
      for (int i=0; i<len; i++) {
          CustomerShareDb cd = new CustomerShareDb();
          int customerId = StrUtil.toInt(ids[i]);
          cd.setCustomerId(customerId);
          if (cd.isExist(sharePerson, customerId))
              continue; // throw new ErrMsgException("该共享人员已存在!");
          else {
              cd.setSharePerson(sharePerson);
              cd.create();
          }
      }
  }

  public boolean create(HttpServletRequest request) throws ErrMsgException {
      //if (!privilege.isUserLogin(request))
      //  throw new ErrMsgException("请先登录！");
      boolean re = true;
      String errmsg = "";
      int customerId = ParamUtil.getInt(request, "customerId");
      String sharePerson = ParamUtil.get(request, "sharePerson");
      if (!errmsg.equals(""))
          throw new ErrMsgException(errmsg);
      CustomerShareDb cd = new CustomerShareDb();
      cd.setCustomerId(customerId);
      if (cd.isExist(sharePerson, customerId))
          throw new ErrMsgException("该共享人员已存在!");
      else {
          cd.setSharePerson(sharePerson);
          re = cd.create();
      }
      return re;
  }

  public boolean del(HttpServletRequest request) throws ErrMsgException {
      int id = ParamUtil.getInt(request, "delId");
      CustomerShareDb cd = getCustomerShareDb(id);
      if (cd==null || !cd.isLoaded())
          throw new ErrMsgException("该项已不存在！");

      // Privilege privilege = new Privilege();
      // if (!privilege.getUser(request).equals(kd.getName()))
      //     throw new ErrMsgException("非法操作！");
      return cd.del();
  }
}
