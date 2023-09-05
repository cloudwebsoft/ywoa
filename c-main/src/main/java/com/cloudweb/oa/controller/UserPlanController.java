package com.cloudweb.oa.controller;

import cn.js.fan.db.ListResult;
import cn.js.fan.db.SQLFilter;
import cn.js.fan.util.DateUtil;
import cn.js.fan.util.ErrMsgException;
import cn.js.fan.util.ParamUtil;
import cn.js.fan.util.StrUtil;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.cloudweb.oa.security.AuthUtil;
import com.cloudweb.oa.utils.I18nUtil;
import com.cloudweb.oa.vo.Result;
import com.cloudwebsoft.framework.util.LogUtil;
import com.redmoon.oa.person.PlanDb;
import com.redmoon.oa.person.PlanMgr;
import io.swagger.annotations.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Api(tags = "日程安排")
@RestController
public class UserPlanController {
    @Autowired
    private HttpServletRequest request;

    @Autowired
    private I18nUtil i18nUtil;

    @Autowired
    private AuthUtil authUtil;

    @ApiOperation(value = "日程列表", notes = "日程列表", httpMethod = "POST")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "page", value = "页数", dataType = "Integer"),
            @ApiImplicitParam(name = "pageSize", value = "每页条数", dataType = "Integer"),
    })
    @ApiResponses({@ApiResponse(code = 200, message = "操作成功")})
    @ResponseBody
    @RequestMapping(value = "/plan/list", method = RequestMethod.POST, produces = {"text/html;", "application/json;charset=UTF-8;"})
    public Result<JSONObject> list(@RequestParam(defaultValue = "1") Integer page, @RequestParam(defaultValue = "20") Integer pageSize) {
        com.alibaba.fastjson.JSONObject jsonResult = new com.alibaba.fastjson.JSONObject();

        int isClosed = ParamUtil.getInt(request, "isClosed", -1);

        String userName = authUtil.getUserName();
        String sql = "select id from user_plan where username=" + StrUtil.sqlstr(userName);
        if (isClosed != -1) {
            sql += " and is_closed=" + isClosed;
        }

        String y = ParamUtil.get(request, "year");
        String m = ParamUtil.get(request, "month");
        String d = ParamUtil.get(request, "day");
        if (!"".equals(y)) {
            sql += " and " + SQLFilter.year("myDate") + "=" + y + " and " + SQLFilter.month("myDate") + "=" + m + " and " + SQLFilter.day("myDate") + "=" + d;
        }

        String op = ParamUtil.get(request, "op");
        String what = ParamUtil.get(request, "what");
        String strBeginDate = ParamUtil.get(request, "beginDate");
        String strEndDate = ParamUtil.get(request, "endDate");
        Date beginDate = DateUtil.parse(strBeginDate, "yyyy-MM-dd");
        Date endDate = DateUtil.parse(strEndDate, "yyyy-MM-dd");
        if (endDate != null) {
            endDate = DateUtil.addDate(endDate, 1);
        }

        if ("search".equals(op)) {
            if (!"".equals(what)) {
                sql += " and title like " + StrUtil.sqlstr("%" + what + "%");
            }
            if (beginDate != null) {
                sql += " and myDate>=" + SQLFilter.getDateStr(strBeginDate, "yyyy-MM-dd");
            }
            if (endDate != null) {
                sql += " and myDate<" + SQLFilter.getDateStr(DateUtil.format(endDate, "yyyy-MM-dd"), "yyyy-MM-dd");
            }
        }
        sql += " order by myDate desc";

        PlanDb pd = new PlanDb();
        try {
            ListResult lr = pd.listResult(sql, page, pageSize);
            jsonResult.put("list", lr.getResult());
            jsonResult.put("page", page);
            jsonResult.put("total", lr.getTotal());
        } catch (ErrMsgException e) {
            LogUtil.getLog(getClass()).error(e);
        }
        return new Result<>(jsonResult);
    }

    @ApiOperation(value = "列出某一阶段的日程", notes = "列出某一阶段的日程", httpMethod = "POST")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "beginDate", value = "开始日期", dataType = "String"),
            @ApiImplicitParam(name = "endDate", value = "结束日期", dataType = "String"),
    })
    @ApiResponses({@ApiResponse(code = 200, message = "操作成功")})
    @ResponseBody
    @RequestMapping(value = "/plan/listPhase", method = RequestMethod.POST, produces = {"text/html;", "application/json;charset=UTF-8;"})
    public Result<JSONObject> listPhase(String beginDate, String endDate, @RequestParam(defaultValue = "20") Integer pageSize, @RequestParam(defaultValue = "1") Integer page) {
        com.alibaba.fastjson.JSONObject jsonResult = new com.alibaba.fastjson.JSONObject();
        // 判断是否为日
        Date bd = DateUtil.parse(beginDate, "yyyy-MM-dd");
        Date ed = DateUtil.parse(endDate, "yyyy-MM-dd");
        // 视图模式：0 日 1 周 2 月
        int mode = DateUtil.isSameDay(bd,ed) ? 0 : 2;
        if (mode != 0 ) {
            if (DateUtil.datediff(ed, bd) <= 7) {
                mode = 1;
            }
        }

        String userName = authUtil.getUserName();
        String sql = "select id from user_plan where username=" + StrUtil.sqlstr(userName);
        if (beginDate != null) {
            sql += " and myDate>=" + SQLFilter.getDateStr(beginDate, "yyyy-MM-dd");
        }
        if (endDate != null) {
            sql += " and myDate<" + SQLFilter.getDateStr(DateUtil.format(DateUtil.addDate(ed, 1), "yyyy-MM-dd"), "yyyy-MM-dd");
        }
        sql += " order by myDate asc";

        PlanDb pd = new PlanDb();
        try {
            ListResult lr = pd.listResult(sql, page, pageSize);
            jsonResult.put("list", getPlans(mode, bd, ed, lr.getResult()));
            jsonResult.put("page", page);
            jsonResult.put("total", lr.getTotal());
        } catch (ErrMsgException e) {
            LogUtil.getLog(getClass()).error(e);
        }
        return new Result<>(jsonResult);
    }

    public JSONArray getPlans(int mode, Date beginDate, Date endDate, List<PlanDb> list) {
        if (mode == 0) {
            return getPlansInDay(list);
        }
        else if (mode == 1) {
            JSONArray arr = new JSONArray();
            Date curDate = beginDate;
            do {
                int curDay = StrUtil.toInt(DateUtil.format(curDate, "yyyyMMdd"), 0);
                JSONObject jsonDay = new JSONObject();
                List<PlanDb> curDayList = new ArrayList<>();
                for (PlanDb plan : list) {
                    Date myDate = plan.getMyDate();
                    int myD = StrUtil.toInt(DateUtil.format(myDate, "yyyyMMdd"), 0);
                    if (curDay == myD) {
                        curDayList.add(plan);
                    }
                    else if (curDay > myD) {
                        break;
                    }
                }
                jsonDay.put(DateUtil.format(curDate, "yyyy-MM-dd"), getPlansInDay(curDayList));
                arr.add(jsonDay);
                curDate = DateUtil.addDate(curDate, 1);
            }
            while (DateUtil.compare(curDate, endDate) != 1);
            return arr;
        }
        else {
            // 月视图
            JSONArray arr = new JSONArray();
            Date curDate = beginDate;
            do {
                int curDay = StrUtil.toInt(DateUtil.format(curDate, "yyyyMMdd"), 0);
                JSONObject jsonDay = new JSONObject();
                // 取得curDate那一天中的日程
                List<PlanDb> curDayList = new ArrayList<>();
                for (PlanDb plan : list) {
                    Date myDate = plan.getMyDate();
                    int myD = StrUtil.toInt(DateUtil.format(myDate, "yyyyMMdd"), 0);
                    if (curDay == myD) {
                        curDayList.add(plan);
                    }
                    else if (curDay > myD) {
                        break;
                    }
                }
                jsonDay.put(DateUtil.format(curDate, "yyyy-MM-dd"), curDayList);
                arr.add(jsonDay);
                curDate = DateUtil.addDate(curDate, 1);
            }
            while (DateUtil.compare(curDate, endDate) != 1);
            return arr;
        }
    }

    /**
     * 取得某天的日程数组，根据小时形成数组
     * @param dayList 某天的日程列表
     * @return
     */
    public JSONArray getPlansInDay(List<PlanDb> dayList) {
        JSONArray arr = new JSONArray();
        for (int i=0; i<24; i++) {
            JSONObject jsonH = new JSONObject();
            List<PlanDb> listH = new ArrayList<>();
            for (PlanDb planDb : dayList) {
                // 同一小时
                int curH = StrUtil.toInt(DateUtil.format(planDb.getMyDate(), "HH"), 0);
                if (curH == i) {
                    listH.add(planDb);
                }
                else if (curH > i) {
                    break;
                }
            }
            String hh = i<10 ? "0" + i + ":00" : i + ":00";
            jsonH.put(hh, listH);
            arr.add(jsonH);
        }
        return arr;
    }

    @ApiOperation(value = "取得日程", notes = "取得日程", httpMethod = "POST")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "id", value = "ID", dataType = "Integer"),
    })
    @ApiResponses({@ApiResponse(code = 200, message = "操作成功")})
    @ResponseBody
    @RequestMapping(value = "/plan/getPlan", method = RequestMethod.POST, produces = {"text/html;", "application/json;charset=UTF-8;"})
    public Result<Object> getPlan(@RequestParam(required = true) Integer id) throws ErrMsgException {
        PlanMgr planMgr = new PlanMgr();
        return new Result<>(planMgr.getPlanDb(id));
    }

    @ApiOperation(value = "添加日程", notes = "添加日程", httpMethod = "POST")
    @ApiResponses({@ApiResponse(code = 200, message = "操作成功")})
    @ResponseBody
    @RequestMapping(value = "/plan/create", method = RequestMethod.POST, produces = {"text/html;", "application/json;charset=UTF-8;"})
    public Result<JSONObject> create() throws ErrMsgException {
        PlanMgr planMgr = new PlanMgr();
        return new Result<>(planMgr.create(request));
    }

    @ApiOperation(value = "修改日程", notes = "修改日程", httpMethod = "POST")
    @ApiResponses({@ApiResponse(code = 200, message = "操作成功")})
    @ResponseBody
    @RequestMapping(value = "/plan/update", method = RequestMethod.POST, produces = {"text/html;", "application/json;charset=UTF-8;"})
    public Result<JSONObject> update() throws ErrMsgException {
        PlanMgr planMgr = new PlanMgr();
        return new Result<>(planMgr.modify(request));
    }

    @ApiOperation(value = "修改日程", notes = "修改日程", httpMethod = "POST")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "id", value = "ID", dataType = "Integer"),
    })
    @ApiResponses({@ApiResponse(code = 200, message = "操作成功")})
    @ResponseBody
    @RequestMapping(value = "/plan/del", method = RequestMethod.POST, produces = {"text/html;", "application/json;charset=UTF-8;"})
    public Result<JSONObject> del(Integer id) throws ErrMsgException {
        PlanMgr planMgr = new PlanMgr();
        return new Result<>(planMgr.del(request));
    }

    @ApiOperation(value = "关闭日程", notes = "关闭日程", httpMethod = "POST")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "id", value = "ID", dataType = "Integer"),
    })
    @ApiResponses({@ApiResponse(code = 200, message = "操作成功")})
    @ResponseBody
    @RequestMapping(value = "/plan/close", method = RequestMethod.POST, produces = {"text/html;", "application/json;charset=UTF-8;"})
    public Result<JSONObject> close(Integer id) throws ErrMsgException {
        PlanMgr planMgr = new PlanMgr();
        PlanDb planDb = planMgr.getPlanDb(id);
        planDb.setClosed(true);
        return new Result<>(planDb.save());
    }

    @ApiOperation(value = "打开日程", notes = "打开日程", httpMethod = "POST")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "id", value = "ID", dataType = "Integer"),
    })
    @ApiResponses({@ApiResponse(code = 200, message = "操作成功")})
    @ResponseBody
    @RequestMapping(value = "/plan/open", method = RequestMethod.POST, produces = {"text/html;", "application/json;charset=UTF-8;"})
    public Result<JSONObject> open(Integer id) throws ErrMsgException {
        PlanMgr planMgr = new PlanMgr();
        PlanDb planDb = planMgr.getPlanDb(id);
        planDb.setClosed(false);
        return new Result<>(planDb.save());
    }
}
