package com.redmoon.oa.basic;

import cn.js.fan.util.ErrMsgException;
import cn.js.fan.util.ParamUtil;
import cn.js.fan.util.ResKeyException;
import cn.js.fan.util.StrUtil;
import com.cloudwebsoft.framework.db.JdbcTemplate;
import com.cloudwebsoft.framework.util.LogUtil;

import java.io.IOException;
import javax.servlet.http.HttpServletRequest;

public class RegionMgr
{
  public boolean create(HttpServletRequest request)
    throws ErrMsgException, ResKeyException, IOException
  {
    RegionDb rd = new RegionDb();
    String region_name = "";
    int parent_id = 0; int region_type = 0;

    region_name = ParamUtil.get(request, "region_name");
    parent_id = ParamUtil.getInt(request, "parent_id");
    region_type = ParamUtil.getInt(request, "region_type");

    return rd.create(new JdbcTemplate(), new Object[] { Integer.valueOf(parent_id), 
      region_name, Integer.valueOf(region_type) });
  }

  public boolean save(HttpServletRequest request) throws ErrMsgException, ResKeyException, IOException
  {
    RegionDb rd = new RegionDb();
    boolean re = false;
    String region_name = "";
    int parent_id = 0; int region_type = 0;

    region_name = ParamUtil.get(request, "region_name");
    parent_id = ParamUtil.getInt(request, "parent_id");
    region_type = ParamUtil.getInt(request, "region_type");

    int id = ParamUtil.getInt(request, "id");
    rd = rd.getRegionDB(id);
    rd.set("region_name", region_name);
    rd.set("parent_id", Integer.valueOf(parent_id));
    rd.set("region_type", Integer.valueOf(region_type));

    return rd.save();
  }

  public boolean del(String id) throws Exception
  {
    RegionDb rd = new RegionDb();
    boolean re = false;
    rd = rd.getRegionDB(StrUtil.toInt(id));
    re = rd.del();
    return re;
  }

  public void delBatch(HttpServletRequest request) throws ErrMsgException {
    String strids = ParamUtil.get(request, "ids");
    String[] ids = StrUtil.split(strids, ",");
    if (ids == null)
      return;
    int len = ids.length;
    for (int i = 0; i < len; ++i)
      try {
        del(ids[i]);
      } catch (Exception e) {
        LogUtil.getLog(getClass()).error(e);
      }
  }
}
