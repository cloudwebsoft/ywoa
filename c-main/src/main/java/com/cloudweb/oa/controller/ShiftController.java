package com.cloudweb.oa.controller;

import java.sql.SQLException;
import java.util.Date;
import java.util.Iterator;
import java.util.Vector;

import javax.servlet.http.HttpServletRequest;

import com.cloudwebsoft.framework.util.LogUtil;
import com.redmoon.oa.hr.SignMgr;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.cloudwebsoft.framework.db.JdbcTemplate;
import com.redmoon.oa.attendance.AttendanceMgr;
import com.redmoon.oa.attendance.BMapUtil;
import com.redmoon.oa.flow.FormDb;
import com.redmoon.oa.pvg.Privilege;
import com.redmoon.oa.visual.FormDAO;

import cn.js.fan.db.SQLFilter;
import cn.js.fan.util.DateUtil;
import cn.js.fan.util.ErrMsgException;
import cn.js.fan.util.ParamUtil;
import cn.js.fan.util.StrUtil;

/**
 * @Description:
 * @author:
 * @Date: 2017-9-26下午03:09:04
 */
@Controller
@RequestMapping("/attendance")
public class ShiftController {
    @Autowired
    private HttpServletRequest request;

    @ResponseBody
    @RequestMapping(value = "/saveShiftAdjust", method = RequestMethod.POST, produces = {"text/html;charset=UTF-8;", "application/json;"})
    public String saveShiftAdjust() {
        boolean re = false;

        // 格式：userName:2017-9-3,1#2017-9-6,1;...
        String data = ParamUtil.get(request, "data");
        Privilege pvg = new Privilege();
        String creator = pvg.getUser(request);
        String unitCode = pvg.getUserUnitCode(request);

        String[] ary = StrUtil.split(data, ";");
        if (ary == null) {
            re = true;
        } else {
            for (String str : ary) {
                String[] aryShift = StrUtil.split(str, ":");
                String userName = aryShift[0];

                String userData = aryShift[1];
                String[] aryDateShift = StrUtil.split(userData, "#");
                for (String strDataShift : aryDateShift) {
                    String[] pair = StrUtil.split(strDataShift, ",");
                    String strDate = pair[0];
                    String strShift = "";
                    if (pair.length > 1) {
                        strShift = pair[1];
                    }
                    long shift = StrUtil.toLong(strShift, -1);
                    // 调整某用户某天的班次
                    re = adjustShift(userName, strDate, shift, creator, unitCode);
                }
            }
        }

        JSONObject json = new JSONObject();
        try {
            if (re) {
                json.put("ret", 1);
                json.put("msg", "操作成功！");
            } else {
                json.put("ret", 0);
                json.put("msg", "操作失败");
            }
        } catch (JSONException e) {
            LogUtil.getLog(getClass()).error(e);
        }

        return json.toString();
    }

    /**
     * 调整某用户某天的班次
     *
     * @param userName
     * @param strDate
     * @param shift    如果为-1，表示删除调整记录
     * @param creator
     * @param unitCode
     * @Description:
     */
    public boolean adjustShift(String userName, String strDate, long shift, String creator, String unitCode) {
        String sql = "select id from ft_shift_adjust where user_name=" + StrUtil.sqlstr(userName) + " and mydate=" + SQLFilter.getDateStr(strDate, "yyyy-MM-dd");
        String formCode = "shift_adjust";
        FormDb fd = new FormDb();
        fd = fd.getFormDb(formCode);
        FormDAO fdao = new FormDAO(fd);
        try {
            Vector v = fdao.list(formCode, sql);
            if (v.size() > 0) {
                // 已存在，则判断值是否相等，不相等则重写
                Iterator ir = v.iterator();
                if (ir.hasNext()) {
                    fdao = (FormDAO) ir.next();

                    // 删除调整记录
                    if (shift == -1) {
                        fdao.del();
                        return true;
                    }

                    long oldShift = StrUtil.toLong(fdao.getFieldValue("shift"));
                    if (oldShift != shift) {
                        fdao.setFieldValue("shift", String.valueOf(shift));
                        fdao.save();
                    }
                }
            } else {
                if (shift != -1) {
                    fdao.setFieldValue("user_name", userName);
                    fdao.setFieldValue("mydate", strDate);
                    fdao.setFieldValue("shift", String.valueOf(shift));
                    fdao.setCreator(creator);
                    fdao.setUnitCode(unitCode);
                    fdao.create();
                }
            }
        } catch (ErrMsgException | SQLException e) {
            LogUtil.getLog(getClass()).error(e);
            return false;
        }
        return true;
    }

    @ResponseBody
    @RequestMapping(value = "getDistance", method = RequestMethod.POST, produces = {"text/html;charset=UTF-8;", "application/json;"})
    public String getDistance() {
        double lng = ParamUtil.getDouble(request, "lng", 0);
        double lat = ParamUtil.getDouble(request, "lat", 0);

        double lngLoc = ParamUtil.getDouble(request, "lngLoc", 0);
        double latLoc = ParamUtil.getDouble(request, "latLoc", 0);
        double r = BMapUtil.getDistanceFromTwoPoints(lat, lng, latLoc, lngLoc);
        double r2 = BMapUtil.distanceOfTwoPoints(lat, lng, latLoc, lngLoc);
        double r1 = BMapUtil.getDistance(lat, lng, latLoc, lngLoc);
        JSONObject json = new JSONObject();
        try {
            if (r > 0) {
                json.put("ret", 1);
                json.put("distance", r);
                json.put("msg", "操作成功！");
            } else {
                json.put("ret", 0);
                json.put("msg", "操作失败");
            }
        } catch (JSONException e) {
            LogUtil.getLog(getClass()).error(e);
        }

        return json.toString();
    }

    @ResponseBody
    @RequestMapping(value = "punch", method = RequestMethod.POST, produces = {"text/html;charset=UTF-8;", "application/json;"})
    public String punch(@RequestParam String userName, @RequestParam double lat, @RequestParam double lng, @RequestParam String address, @RequestParam int type) {
        JSONObject json = new JSONObject();
        long[] r = new long[3];
        try {
            try {
                r = AttendanceMgr.punch(userName, lat, lng, address, type);
            } catch (ErrMsgException e) {
                json.put("ret", 0);
                json.put("msg", e.getMessage());
                return json.toString();
            }

            if (r[0] != -1) {
                json.put("ret", 1);
                json.put("result", r[0]);
                json.put("min", r[1]);
                json.put("id", r[2]); // 打卡記錄的ID
                json.put("msg", "操作成功！");
            } else {
                json.put("ret", 0);
                json.put("msg", "操作失败");
            }
        } catch (JSONException e) {
            LogUtil.getLog(getClass()).error(e);
        }

        return json.toString();
    }

    /**
     * 報告原因
     *
     * @param userName
     * @param id
     * @param remark
     * @return
     */
    @ResponseBody
    @RequestMapping(value = "punchRemark", method = RequestMethod.POST, produces = {"text/html;charset=UTF-8;", "application/json;"})
    public String punchRemark(@RequestParam String userName, @RequestParam long id, @RequestParam String remark) {
        JSONObject json = new JSONObject();
        long[] r = new long[3];
        try {
            try {
                FormDb fd = new FormDb();
                fd = fd.getFormDb("kaoqin_time_sign");
                FormDAO fdao = new FormDAO();
                fdao = fdao.getFormDAO(id, fd);
                fdao.setFieldValue("remark", remark);
                fdao.save();
            } catch (ErrMsgException e) {
                // TODO Auto-generated catch block
                LogUtil.getLog(getClass()).error(e);
                json.put("ret", 0);
                json.put("msg", e.getMessage());
                return json.toString();
            }

            if (r[0] != -1) {
                json.put("ret", 1);
                json.put("msg", "操作成功！");
            } else {
                json.put("ret", 0);
                json.put("msg", "操作失败");
            }
        } catch (JSONException e) {
            LogUtil.getLog(getClass()).error(e);
        }

        return json.toString();
    }

    @ResponseBody
    @RequestMapping(value = "dataCollect", method = RequestMethod.POST, produces = {"text/html;charset=UTF-8;", "application/json;"})
    public String dataCollect(@RequestParam String begin_date, @RequestParam String end_date) {
        JSONObject json = new JSONObject();
        try {
            if ("".equals(begin_date) || "".equals(end_date)) {
                json.put("ret", 0);
                json.put("msg", "请填写开始及结束日期");
                return json.toString();
            }

            Date endDate = DateUtil.parse(end_date, "yyyy-MM-dd");
            Date now = new java.util.Date();
            if (endDate.after(now)) {
                end_date = DateUtil.format(now, "yyyy-MM-dd");
            }

            SignMgr sm = new SignMgr();
            // 清空
            sm.delDataCollectByDate(begin_date, end_date);
            // 工作日数据汇总
            sm.dataCollect(begin_date, end_date);

            json.put("ret", 1);
            json.put("msg", "操作成功！");
        } catch (JSONException e) {
            LogUtil.getLog(getClass()).error(e);
        }

        return json.toString();
    }

}
