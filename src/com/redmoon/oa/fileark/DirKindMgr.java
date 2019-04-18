package com.redmoon.oa.fileark;

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
public class DirKindMgr {
  Logger logger = Logger.getLogger( DirKindMgr.class.getName() );

  public DirKindMgr() {

  }

  public boolean modify(HttpServletRequest request) throws ErrMsgException {
      Privilege privilege = new Privilege();
      if (!privilege.isUserLogin(request))
          throw new ErrMsgException("请先登录！");
      boolean re = true;
      String errmsg = "";

      int id = ParamUtil.getInt(request, "id");
      String kind = ParamUtil.get(request, "kind");
      if (kind.equals(""))
          errmsg += "类别不能为空！\\n";
      
      if (!errmsg.equals(""))
          throw new ErrMsgException(errmsg);
      
      int orders = ParamUtil.getInt(request, "orders", 0);

      DirKindDb wptd =  getDirKindDb(id);
      wptd.setKind(kind);
      wptd.setOrders(orders);
      re = wptd.save();
      return re;
  }

  public DirKindDb getDirKindDb(int id) {
      DirKindDb addr = new DirKindDb();
      return addr.getDirKindDb(id);
  }

  public boolean create(HttpServletRequest request) throws ErrMsgException {
      Privilege privilege = new Privilege();
      if (!privilege.isUserLogin(request))
          throw new ErrMsgException("请先登录！");
      boolean re = true;

      String errmsg = "";
      String kind = ParamUtil.get(request, "kind");
      if (kind.equals(""))
          errmsg += "类别不能为空！\\n";
      
      String dirCode = ParamUtil.get(request, "dirCode");
      if (dirCode.equals(""))
          errmsg += "目录不能为空！\\n";
      
      int orders = ParamUtil.getInt(request, "orders", 0);
      
      if (!errmsg.equals(""))
          throw new ErrMsgException(errmsg);

      DirKindDb wptd = new DirKindDb();
      if (wptd.isKindExist(dirCode, kind)) {
    	  throw new ErrMsgException("类别：" + kind + "已存在！");
      }
      
      wptd.setKind(kind);
      wptd.setOrders(orders);
      wptd.setDirCode(dirCode);
      re = wptd.create();
      return re;
  }

  public boolean del(HttpServletRequest request) throws ErrMsgException {
      int id = ParamUtil.getInt(request, "id");
      DirKindDb DirKindDb = getDirKindDb(id);
      if (DirKindDb==null || !DirKindDb.isLoaded())
          throw new ErrMsgException("该项已不存在！");

      return DirKindDb.del();
  }
}
