package com.redmoon.oa.book;
import cn.js.fan.db.Conn;
import cn.js.fan.util.*;
import java.sql.*;
import com.redmoon.oa.pvg.Privilege;
import cn.js.fan.util.ErrMsgException;
import javax.servlet.http.*;
import org.apache.log4j.Logger;
import cn.js.fan.db.*;
import cn.js.fan.web.SkinUtil;
/**
 * <p>Title: </p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2003</p>
 * <p>Company: </p>
 * @author not attributable
 * @version 1.0
 */
//日程安排
public class BookTypeMgr {
  Logger logger = Logger.getLogger( BookTypeMgr.class.getName() );

  public BookTypeMgr() {

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

      BookTypeDb wptd =  getBookTypeDb(id);
      wptd.setName(name);
      re = wptd.save();
      return re;
  }

  public BookTypeDb getBookTypeDb(int id) {
      BookTypeDb addr = new BookTypeDb();
      return addr.getBookTypeDb(id);
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
      if (!errmsg.equals(""))
          throw new ErrMsgException(errmsg);
      BookTypeDb wptd = new BookTypeDb();
      if (wptd.isExist(name))
          throw new ErrMsgException("该类别已存在!");
      else{wptd.setName(name);
              re = wptd.create();
          }
      return re;
  }

  public boolean del(HttpServletRequest request) throws ErrMsgException {
      int id = ParamUtil.getInt(request, "id");
      BookTypeDb BookTypeDb = getBookTypeDb(id);
      if (BookTypeDb==null || !BookTypeDb.isLoaded())
          throw new ErrMsgException("该项已不存在！");

      BookDb bd = new BookDb();
      if (bd.hasBookOfType(id)) {
          String info = SkinUtil.LoadString(request, "res.module.book", "warn_type_del_hasbook");
          throw new ErrMsgException(info);
      }
      return BookTypeDb.del();
  }
}
