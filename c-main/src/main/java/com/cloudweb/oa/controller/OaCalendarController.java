package com.cloudweb.oa.controller;

import cn.js.fan.db.SQLFilter;
import cn.js.fan.util.*;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.cloudweb.oa.utils.I18nUtil;
import com.cloudweb.oa.vo.Result;
import com.redmoon.oa.oacalendar.OACalendarDb;
import com.redmoon.oa.oacalendar.OACalendarMgr;
import io.swagger.annotations.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Vector;

@Api(tags = "工作日历")
@RestController
public class OaCalendarController {
    @Autowired
    private HttpServletRequest request;

    @ApiOperation(value = "列表", notes = "列表", httpMethod = "POST")
    @ApiResponses({ @ApiResponse(code = 200, message = "操作成功") })
    @ResponseBody
    @RequestMapping(value = "/oacalender/list", method = RequestMethod.POST, produces = {"text/html;", "application/json;charset=UTF-8;"})
    public Result<Object> listPage(Integer year) {
        com.alibaba.fastjson.JSONObject jsonResult = new com.alibaba.fastjson.JSONObject();
        com.alibaba.fastjson.JSONArray rows = new com.alibaba.fastjson.JSONArray();
        String sql = "select oa_date from oa_calendar where " + SQLFilter.year("oa_date") + "=" + year;
        Date bd = DateUtil.parse(year + "-01-01", "yyyy-MM-dd");
        Date ed = DateUtil.addDate(bd, DateUtil.getDaysOfYear(year) - 1);
        OACalendarDb oacd = new OACalendarDb();
        Vector<OACalendarDb> v = oacd.list(sql);
        Map<Integer, OACalendarDb> map = new HashMap<>();
        for (OACalendarDb oaCalendarDb : v) {
            map.put(StrUtil.toInt(DateUtil.format(oaCalendarDb.getDate("oa_date"), "yyyyMMdd")), oaCalendarDb);
        }
        JSONArray arr = new JSONArray();
        Date curDate = bd;
        do {
            int curDay = StrUtil.toInt(DateUtil.format(curDate, "yyyyMMdd"), 0);
            JSONObject jsonDay = new JSONObject();
            JSONObject json = new JSONObject();
            if (map.containsKey(curDay)) {
                OACalendarDb oaCalendarDb = map.get(curDay);
                int dateType = oaCalendarDb.getInt("date_type");
                if (dateType == OACalendarDb.DATE_TYPE_SAT_SUN) {
                    dateType = OACalendarDb.DATE_TYPE_HOLIDAY;
                }
                json.put("dateType", dateType);
                json.put("work_time_begin_a", oaCalendarDb.getString("work_time_begin_a"));
                json.put("work_time_end_a", oaCalendarDb.getString("work_time_end_a"));
                json.put("work_time_begin_b", oaCalendarDb.getString("work_time_begin_b"));
                json.put("work_time_end_b", oaCalendarDb.getString("work_time_end_b"));
                json.put("work_time_begin_c", oaCalendarDb.getString("work_time_begin_c"));
                json.put("work_time_end_c", oaCalendarDb.getString("work_time_end_c"));
            }
            jsonDay.put("day", DateUtil.format(curDate, "yyyy-MM-dd"));
            jsonDay.put("content", json);
            arr.add(jsonDay);
            curDate = DateUtil.addDate(curDate, 1);
        }
        while (DateUtil.compare(curDate, ed) != 1);
        return new Result<>(arr);
    }

    @ApiOperation(value = "初始化", notes = "初始化", httpMethod = "POST")
    @ApiResponses({ @ApiResponse(code = 200, message = "操作成功") })
    @ResponseBody
    @RequestMapping(value = "/oacalender/init", method = RequestMethod.POST, produces = {"text/html;", "application/json;charset=UTF-8;"})
    public Result<JSONObject> init(Integer year) {
        OACalendarDb oacdb = new OACalendarDb();
        return new Result<>(oacdb.initCalendar(year));
    }

    @ApiOperation(value = "修改某天", notes = "修改某天", httpMethod = "POST")
    @ApiResponses({ @ApiResponse(code = 200, message = "操作成功") })
    @ResponseBody
    @RequestMapping(value = "/oacalender/modifyDay", method = RequestMethod.POST, produces = {"text/html;", "application/json;charset=UTF-8;"})
    public Result<JSONObject> modifyDay() {
        OACalendarMgr qom = new OACalendarMgr();
        OACalendarDb oaCalendarDb = new OACalendarDb();
        Date date = DateUtil.parse(ParamUtil.get(request, "oa_date"), "yyyy-MM-dd");
        oaCalendarDb = (OACalendarDb) oaCalendarDb.getQObjectDb(date);

        boolean re = false;
        try {
            if (oaCalendarDb != null) {
                oaCalendarDb.del();
            }
            re = qom.create(request, new OACalendarDb(), "oa_calendar_create");
        } catch (ErrMsgException e) {
            return new Result<>(re, e.getMessage());
        } catch (ResKeyException e) {
            return new Result<>(re, e.getMessage(request));
        }
        return new Result<>(re);
    }

    @ApiOperation(value = "取得工作时间的配置信息", notes = "取得工作时间的配置信息）", httpMethod = "POST")
    @ApiResponses({ @ApiResponse(code = 200, message = "操作成功") })
    @ResponseBody
    @RequestMapping(value = "/oacalender/getConfig", method = RequestMethod.POST, produces = {"text/html;", "application/json;charset=UTF-8;"})
    public Result<JSONObject> getConfig() {
        com.redmoon.oa.Config cfg = com.redmoon.oa.Config.getInstance();
        JSONObject json = new JSONObject();
        json.put("morningBegin", cfg.get("morningbegin"));
        json.put("morningEnd", cfg.get("morningend"));
        json.put("afternoonBegin", cfg.get("afternoonbegin"));
        json.put("afternoonEnd", cfg.get("afternoonend"));
        json.put("nightBegin", cfg.get("nightbegin"));
        json.put("nightEnd", cfg.get("nightend"));
        return new Result<>(json);
    }

    @ApiOperation(value = "取得某天的信息", notes = "取得某天的信息）", httpMethod = "POST")
    @ApiImplicitParam(name = "date", value = "日期，格式需为：yyyy-MM-dd", required = false, dataType = "String")
    @ApiResponses({ @ApiResponse(code = 200, message = "操作成功") })
    @ResponseBody
    @RequestMapping(value = "/oacalender/getDayInfo", method = RequestMethod.POST, produces = {"text/html;", "application/json;charset=UTF-8;"})
    public Result<JSONObject> getDayInfo(String date) {
        OACalendarDb oacdb = new OACalendarDb();
        java.util.Date currentDate = DateUtil.parse(date, "yyyy-MM-dd");
        oacdb = (OACalendarDb) oacdb.getQObjectDb(currentDate);

        String work_time_begin_a = oacdb.getString("work_time_begin_a");
        String work_time_end_a = oacdb.getString("work_time_end_a");
        String work_time_begin_b = oacdb.getString("work_time_begin_b");
        String work_time_end_b = oacdb.getString("work_time_end_b");
        String work_time_begin_c = oacdb.getString("work_time_begin_c");
        String work_time_end_c = oacdb.getString("work_time_end_c");
        int dateType = oacdb.getInt("date_type");
        if (dateType == OACalendarDb.DATE_TYPE_SAT_SUN) {
            dateType = OACalendarDb.DATE_TYPE_HOLIDAY;
        }

        JSONObject json = new JSONObject();
        json.put("dateType", dateType);
        json.put("work_time_begin_a", work_time_begin_a);
        json.put("work_time_end_a", work_time_end_a);
        json.put("work_time_begin_b", work_time_begin_b);
        json.put("work_time_end_b", work_time_end_b);
        json.put("work_time_begin_c", work_time_begin_c);
        json.put("work_time_end_c", work_time_end_c);

        return new Result<>(json);
    }

    @ApiOperation(value = "批量修改", notes = "当type为1时批量修改日期类型；type为2时修改工作时间（仅针对所选时间段内的工作日进行修改）", httpMethod = "POST")
    @ApiResponses({ @ApiResponse(code = 200, message = "操作成功") })
    @ResponseBody
    @RequestMapping(value = "/oacalender/modifyDays", method = RequestMethod.POST, produces = {"text/html;", "application/json;charset=UTF-8;"})
    public Result<JSONObject> modifyDays() {
        OACalendarDb oaCalendarDb = new OACalendarDb();
        boolean re = false;
        try {
            re = oaCalendarDb.modifyDates(request);
        } catch (ErrMsgException e) {
            return new Result<>(re, e.getMessage());
        }
        return new Result<>(re);
    }
}
