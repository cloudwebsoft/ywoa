package com.redmoon.oa.sms;

import com.cloudwebsoft.framework.db.JdbcTemplate;
import java.io.IOException;
import javax.servlet.http.HttpServletRequest;
import cn.js.fan.util.ResKeyException;
import cn.js.fan.util.ErrMsgException;
import cn.js.fan.util.ParamUtil;
import cn.js.fan.util.StrUtil;
import com.cloudwebsoft.framework.util.LogUtil;

/**
 * <p>Title: </p>
 *
 * <p>Description: </p>
 *
 * <p>Copyright: Copyright (c) 2007</p>
 *
 * <p>Company: </p>
 *
 * @author not attributable
 * @version 1.0
 */
public class SMSTemplateMgr {
    public SMSTemplateMgr() {

    }

    public boolean create(HttpServletRequest request) throws ErrMsgException,ResKeyException, IOException {
        SMSTemplateDb stDb = new SMSTemplateDb();
        boolean re = false;
        String type = "",content = "";

        type = ParamUtil.get(request, "sms_type");
        content = ParamUtil.get(request, "content");

        return re = stDb.create(new JdbcTemplate(), new Object[] { type, content });
      }

      public boolean save(HttpServletRequest request) throws ErrMsgException,ResKeyException, IOException {
          SMSTemplateDb stDb = new SMSTemplateDb();
          boolean re = false;
          String type = "",content = "";

          type = ParamUtil.get(request, "sms_type");
          content = ParamUtil.get(request, "content");

          int id =  ParamUtil.getInt(request, "id");
          stDb=stDb.getMSTemplateDb(id);
          stDb.set("type", type);
          stDb.set("content", content);
          return stDb.save();
      }

      public boolean del(String id) throws Exception {
          SMSTemplateDb stDb = new SMSTemplateDb();
          boolean re = false;
          stDb = stDb.getMSTemplateDb(StrUtil.toLong(id));
          re = stDb.del();
          return re;
      }

      /**
       * 全部删除
       * @param request HttpServletRequest
       * @throws ErrMsgException
       */
      public void delBatch(HttpServletRequest request) throws ErrMsgException {
          String strids = ParamUtil.get(request, "ids");
          String[] ids = StrUtil.split(strids, ",");
          if (ids == null)
              return;
          int len = ids.length;
          for (int i = 0; i < len; i++) {
              try {
                  del(ids[i]);
              } catch (Exception e) {
                  LogUtil.getLog(getClass()).error(e);
              }
          }
      }

}
