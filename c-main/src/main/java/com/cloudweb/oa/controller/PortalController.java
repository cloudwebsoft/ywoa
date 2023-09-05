package com.cloudweb.oa.controller;

import cn.js.fan.db.ListResult;
import cn.js.fan.util.ErrMsgException;
import cn.js.fan.util.ParamUtil;
import cn.js.fan.util.ResKeyException;
import cn.js.fan.util.StrUtil;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.cloudweb.oa.api.IDesktopCard;
import com.cloudweb.oa.cache.UserCache;
import com.cloudweb.oa.entity.User;
import com.cloudweb.oa.exception.ValidateException;
import com.cloudweb.oa.module.desktop.DesktopCard;
import com.cloudweb.oa.module.desktop.DesktopCardFactory;
import com.cloudweb.oa.module.desktop.DesktopCardUtil;
import com.cloudweb.oa.security.AuthUtil;
import com.cloudweb.oa.service.IDocService;
import com.cloudweb.oa.service.PortalService;
import com.cloudweb.oa.utils.ConstUtil;
import com.cloudweb.oa.utils.SpringUtil;
import com.cloudweb.oa.vo.Result;
import com.cloudwebsoft.framework.db.JdbcTemplate;
import com.cloudwebsoft.framework.util.LogUtil;
import com.redmoon.oa.flow.FormDb;
import com.redmoon.oa.person.UserDb;
import com.redmoon.oa.person.UserDesktopSetupDb;
import com.redmoon.oa.pvg.Privilege;
import com.redmoon.oa.ui.LocalUtil;
import com.redmoon.oa.ui.PortalDb;
import com.redmoon.oa.visual.Attachment;
import com.redmoon.oa.visual.FormDAO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import java.sql.SQLException;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;

@Api(tags = "门户管理")
@Controller
@RequestMapping("/portal")
public class PortalController {
    @Autowired
    HttpServletRequest request;

    @Autowired
    PortalService portalService;

    @Autowired
    AuthUtil authUtil;

    @Autowired
    UserCache userCache;

    @Autowired
    IDocService docService;

    @RequestMapping(value = "/addDesktopSetupItem", method = RequestMethod.POST, produces = {"text/html;charset=UTF-8;", "application/json;charset=UTF-8;"})
    public String addDesktopSetupItem(Model model) {
        Privilege privilege = new Privilege();
        String msg = LocalUtil.LoadString(request, "res.common", "info_op_success");
        long portalId = ParamUtil.getLong(request, "portalId", -1);
        String userName = privilege.getUser(request);
        String title = ParamUtil.get(request, "title");
        String moduleCode = ParamUtil.get(request, "moduleCode");

        if ("".equals(moduleCode)) {
            return "../../user/desktop_setup.jsp?op=add&portalId=" + portalId + "&ret=0&msg=" + StrUtil.UrlEncode("请选择相应模块") + "&some";
        }
        String[] moduleItems = ParamUtil.getParameters(request, "moduleItem");
        int count = ParamUtil.getInt(request, "count", 6);

        boolean canDelete = ParamUtil.getInt(request, "canDelete", 0) == 1;
        String icon = ParamUtil.get(request, "icon");
        String metaData = ParamUtil.get(request, "metaData");
        if (moduleCode.startsWith("chart_")) {
            if ("".equals(metaData)) {
                return "../../user/desktop_setup.jsp?op=add&portalId=" + portalId + "&ret=0&msg=" + StrUtil.UrlEncode("请选择图表") + "&some";
            }
        }

        int td = ParamUtil.getInt(request, "td", 0);
        UserDesktopSetupDb udid = new UserDesktopSetupDb();
        udid.setUserName(userName);
        udid.setTitle(title);
        udid.setModuleCode(moduleCode);
        udid.setModuleItem(StringUtils.join(moduleItems, ","));
        udid.setTd(td);
        udid.setCount(count);
        udid.setPortalId(portalId);
        udid.setCanDelete(canDelete);
        udid.setIcon(icon);

        if ("visual".equals(moduleCode)) {
            String fieldTitle = ParamUtil.get(request, "fieldTitle");
            String fieldDate = ParamUtil.get(request, "fieldDate");
            metaData = "{\"fieldTitle\":\"" + fieldTitle + "\", \"fieldDate\":\"" + fieldDate + "\"}";
            udid.setMetaData(metaData);
        } else if ("flow".equals(moduleCode)) {
            String typeCode = ParamUtil.get(request, "typeCode");
            udid.setMetaData(typeCode);
        } else {
            udid.setMetaData(metaData);
        }

        udid.create();

        model.addAttribute("op", "add");
        model.addAttribute("ret", "1");
        model.addAttribute("msg", msg);

        return "../../user/desktop_setup";
    }

    @ResponseBody
    @RequestMapping(value = "/editDesktopSetupItem", method = RequestMethod.POST, produces = {"text/html;charset=UTF-8;", "application/json;charset=UTF-8;"})
    public String editDesktopSetupItem() {
        JSONObject json = new JSONObject();
        String msg = LocalUtil.LoadString(request, "res.common", "info_op_success");

        boolean re = false;
        int id = ParamUtil.getInt(request, "id", -1);
        String title = ParamUtil.get(request, "title");
        int rows = ParamUtil.getInt(request, "count", 5);
        int td = ParamUtil.getInt(request, "td", 0);
        int wordCount = ParamUtil.getInt(request, "wordCount", 80);
        boolean canDelete = ParamUtil.getInt(request, "canDelete", 0) == 1;
        String icon = ParamUtil.get(request, "icon");
        String[] moduleItems = ParamUtil.getParameters(request, "moduleItem");
        /*for (String item : moduleItems) {
            Leaf lf = new Leaf();
            lf = lf.getLeaf(item);
            if (lf!=null) {
                DebugUtil.i(getClass(), "item", lf.getCode() + ":" + lf.getName());
            }
        }*/

        UserDesktopSetupDb udid = new UserDesktopSetupDb();
        udid = udid.getUserDesktopSetupDb(id);

        String metaData = ParamUtil.get(request, "metaData");
        if (udid.getModuleCode().startsWith("chart_")) {
            if ("".equals(metaData)) {
                json.put("ret", 0);
                json.put("msg", "请选择图表");
                return json.toString();
            }
        }

        udid.setTitle(title);
        udid.setCount(rows);
        udid.setTd(td);
        udid.setWordCount(wordCount);
        udid.setCanDelete(canDelete);
        udid.setIcon(icon);
        udid.setMetaData(metaData);
        udid.setModuleItem(StringUtils.join(moduleItems, ","));
        try {
            re = udid.save();
        } catch (ErrMsgException e) {
            msg = e.getMessage();
            LogUtil.getLog(getClass()).error(e);
        } catch (ResKeyException e) {
            msg = e.getMessage();
            LogUtil.getLog(getClass()).error(e);
        }
        json.put("ret", re ? 1 : 0);
        json.put("msg", msg);
        return json.toString();
    }

    @ResponseBody
    @RequestMapping(value = "/delDesktopSetupItem", method = RequestMethod.POST, produces = {"text/html;charset=UTF-8;", "application/json;charset=UTF-8;"})
    public String delDesktopSetupItem() {
        JSONObject json = new JSONObject();
        String msg = LocalUtil.LoadString(request, "res.common", "info_op_success");

        int id = ParamUtil.getInt(request, "id", -1);
        UserDesktopSetupDb udid = new UserDesktopSetupDb();
        udid = udid.getUserDesktopSetupDb(id);
        boolean re = false;
        try {
            re = udid.del();
        } catch (ErrMsgException e) {
            msg = e.getMessage();
            LogUtil.getLog(getClass()).error(e);
        } catch (ResKeyException e) {
            msg = e.getMessage();
            LogUtil.getLog(getClass()).error(e);
        }
        json.put("ret", re ? 1 : 0);
        json.put("msg", msg);
        return json.toString();
    }

    @ResponseBody
    @RequestMapping(value = "/copyDesktopSetupItem", method = RequestMethod.POST, produces = {"text/html;charset=UTF-8;", "application/json;charset=UTF-8;"})
    public String copyDesktopSetupItem() {
        JSONObject json = new JSONObject();
        String msg = LocalUtil.LoadString(request, "res.common", "info_op_success");

        int id = ParamUtil.getInt(request, "id", -1);
        UserDesktopSetupDb udid = new UserDesktopSetupDb();
        udid = udid.getUserDesktopSetupDb(id);
        udid.copyToUsersDesktop();
        json.put("ret", 1);
        json.put("msg", msg);
        return json.toString();
    }

    @ApiOperation(value = "取得相应类型的图型配置", notes = "取得相应类型的图型配置", httpMethod = "POST")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "chartType", value = "图型的类型", required = true, dataType = "String"),
    })
    @ResponseBody
    @RequestMapping(value = "/getChartTypes", method = RequestMethod.POST, produces = {"text/html;charset=UTF-8;", "application/json;charset=UTF-8;"})
    public Result<JSONArray> getChartTypes(String chartType) {
        return new Result<>(portalService.getChartTypes(chartType));
    }

    @ApiOperation(value = "门户列表", notes = "门户列表", httpMethod = "POST")
    @ResponseBody
    @RequestMapping(value = "/list", method = RequestMethod.POST, produces = {"text/html;charset=UTF-8;", "application/json;charset=UTF-8;"})
    public Result<Object> list() {
        JSONArray arr = new JSONArray();
        PortalDb portalDb = new PortalDb();
        String sql = "select id from " + portalDb.getTable().getName() + " where user_name=? order by kind asc, orders asc";
        for (Object o : portalDb.list(sql, new Object[]{ConstUtil.USER_SYSTEM})) {
            portalDb = (PortalDb) o;

            JSONObject json = new JSONObject();
            json.put("isFixed", portalDb.getInt("is_fixed"));
            json.put("orders", portalDb.getInt("orders"));
            json.put("id", portalDb.getLong("id"));
            json.put("name", portalDb.getString("name"));
            json.put("depts", portalDb.getString("depts"));
            json.put("roles", portalDb.getString("roles"));
            json.put("icon", portalDb.getString("icon"));
            json.put("status", portalDb.getInt("status") == 1);
            json.put("kind", portalDb.getInt("kind"));
            arr.add(json);
        }
        return new Result<>(arr);
    }

    /*@ApiOperation(value = "取得默认的门户", notes = "取得默认的门户", httpMethod = "POST")
    @ResponseBody
    @RequestMapping(value = "/getDefaultPortal", method = RequestMethod.POST, produces = {"text/html;charset=UTF-8;", "application/json;charset=UTF-8;"})
    public Result<Object> getDefaultPortal() {

    }*/

    @ApiOperation(value = "用户可见的门户列表", notes = "用户可见的门户列表，不包含首页", httpMethod = "POST")
    @ResponseBody
    @RequestMapping(value = "/listPortal", method = RequestMethod.POST, produces = {"text/html;charset=UTF-8;", "application/json;charset=UTF-8;"})
    public Result<Object> listPortal() {
        JSONArray arr = new JSONArray();
        PortalDb pd = new PortalDb();
        pd = (PortalDb)pd.getQObjectDb(PortalDb.DESKTOP_DEFAULT_ID);
        boolean canSeeHome = pd.canUserSee(authUtil.getUserName());
        Vector<PortalDb> v = pd.listByKind(PortalDb.KIND_DESKTOP);
        int k = 0;
        for (PortalDb portalDb : v) {
            if (k==0 && !canSeeHome) {
                // 如果不能看到首页，则首页显示的是第一个门户，所以此处得跳过第一个门户
                continue;
            }
            boolean canSee = portalDb.canUserSee(authUtil.getUserName());
            if (canSee) {
                JSONObject json = new JSONObject();
                json.put("isFixed", portalDb.getInt("is_fixed"));
                json.put("orders", portalDb.getInt("orders"));
                json.put("id", portalDb.getLong("id"));
                json.put("name", portalDb.getString("name"));
                json.put("depts", portalDb.getString("depts"));
                json.put("roles", portalDb.getString("roles"));
                json.put("icon", portalDb.getString("icon"));
                json.put("status", portalDb.getInt("status") == 1);
                arr.add(json);
                k++;
            }
        }

        return new Result<>(arr);
    }

    @ApiOperation(value = "用于菜单的门户列表", notes = "用于菜单的门户列表", httpMethod = "POST")
    @ResponseBody
    @RequestMapping(value = "/listPortalForMenu", method = RequestMethod.POST, produces = {"text/html;charset=UTF-8;", "application/json;charset=UTF-8;"})
    public Result<Object> listPortalForMenu() {
        JSONArray arr = new JSONArray();
        PortalDb pd = new PortalDb();
        Vector<PortalDb> v = pd.listByKind(PortalDb.KIND_MENU);
        for (PortalDb portalDb : v) {
            boolean canSee = portalDb.canUserSee(authUtil.getUserName());
            if (canSee) {
                JSONObject json = new JSONObject();
                json.put("isFixed", portalDb.getInt("is_fixed"));
                json.put("orders", portalDb.getInt("orders"));
                json.put("id", portalDb.getLong("id"));
                json.put("name", portalDb.getString("name"));
                json.put("depts", portalDb.getString("depts"));
                json.put("roles", portalDb.getString("roles"));
                json.put("icon", portalDb.getString("icon"));
                json.put("status", portalDb.getInt("status") == 1);
                arr.add(json);
            }
        }

        return new Result<>(arr);
    }

    @ApiOperation(value = "取得门户的名称", notes = "取得门户的名称", httpMethod = "POST")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "portals", value = "门名ID，以逗号分隔", required = true, dataType = "String"),
    })
    @ResponseBody
    @RequestMapping(value = "/listPortalNames", method = RequestMethod.POST, produces = {"text/html;charset=UTF-8;", "application/json;charset=UTF-8;"})
    public Result<Object> listPortalNames(@RequestParam(required = true) String portals) {
        JSONArray arr = new JSONArray();
        PortalDb portalDb = new PortalDb();
        String[] ary = portals.split(",");
        for (String strId : ary) {
            long id = StrUtil.toLong(strId, -1);
            portalDb = (PortalDb) portalDb.getQObjectDb(id);
            JSONObject json = new JSONObject();
            json.put("id", portalDb.getLong("id"));
            json.put("name", portalDb.getString("name"));
            arr.add(json);
        }
        return new Result<>(arr);
    }

    @ApiOperation(value = "添加", notes = "添加", httpMethod = "POST")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "name", value = "门户名称", required = true, dataType = "String"),
            @ApiImplicitParam(name = "icon", value = "门户图标", required = true, dataType = "String"),
            @ApiImplicitParam(name = "depts", value = "可见部门", required = true, dataType = "String"),
            @ApiImplicitParam(name = "roles", value = "可见角色", required = true, dataType = "String"),
            @ApiImplicitParam(name = "status", value = "状态", required = false, dataType = "Boolean"),
            @ApiImplicitParam(name = "kind", value = "类型", required = false, dataType = "Integer"),
    })
    @ResponseBody
    @RequestMapping(value = "/create", method = RequestMethod.POST, produces = {"text/html;charset=UTF-8;", "application/json;charset=UTF-8;"})
    public Result<Object> create(@RequestParam(required = true) String name, @RequestParam(required = true) String icon, String depts, String roles, @RequestParam(defaultValue="true") Boolean status, @RequestParam(defaultValue="0")Integer kind) {
        PortalDb portalDb = new PortalDb();
        boolean re;
        try {
            int isFixed = 1;
            String setup = "";
            re = portalDb.create(new JdbcTemplate(), new Object[]{ConstUtil.USER_SYSTEM, name, portalDb.getNextOrders(ConstUtil.USER_SYSTEM), PortalDb.SYSTEM_ID_NONE, icon, isFixed, depts, roles, status, kind, setup});
        } catch (ResKeyException e) {
            LogUtil.getLog(getClass()).error(e);
            return new Result<>(false, e.getMessage(request));
        }
        return new Result<>(re);
    }

    @ApiOperation(value = "修改", notes = "修改", httpMethod = "POST")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "id", value = "门户ID", required = true, dataType = "Long"),
            @ApiImplicitParam(name = "name", value = "门户名称", required = true, dataType = "String"),
            @ApiImplicitParam(name = "icon", value = "门户图标", required = true, dataType = "String"),
            @ApiImplicitParam(name = "depts", value = "可见部门", required = true, dataType = "String"),
            @ApiImplicitParam(name = "roles", value = "可见角色", required = true, dataType = "String"),
            @ApiImplicitParam(name = "kind", value = "类型", required = false, dataType = "Integer"),
    })
    @ResponseBody
    @RequestMapping(value = "/update", method = RequestMethod.POST, produces = {"text/html;charset=UTF-8;", "application/json;charset=UTF-8;"})
    public Result<Object> update(@RequestParam(required = true) Long id, @RequestParam(required = true) String name, @RequestParam(required = true) String icon, String depts, String roles, @RequestParam(required = true) int orders, @RequestParam(defaultValue="true") Boolean status, @RequestParam(defaultValue="0")Integer kind) {
        PortalDb portalDb = new PortalDb();
        portalDb = (PortalDb) portalDb.getQObjectDb(id);
        int isFixed = 1;
        boolean re;
        String setup = portalDb.getString("setup");
        try {
            re = portalDb.save(new JdbcTemplate(), new Object[]{name, orders, icon, isFixed, depts, roles, setup, status ? 1 : 0, kind, id});
        } catch (SQLException e) {
            LogUtil.getLog(getClass()).error(e);
            return new Result<>(false, e.getMessage());
        }
        return new Result<>(re);
    }

    @ApiOperation(value = "复制门户", notes = "复制门户", httpMethod = "POST")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "id", value = "门户ID", required = true, dataType = "Long"),
    })
    @ResponseBody
    @RequestMapping(value = "/copy", method = RequestMethod.POST, produces = {"text/html;charset=UTF-8;", "application/json;charset=UTF-8;"})
    public Result<Object> copy(@RequestParam(required = true) Long id) {
        PortalDb portalDb = new PortalDb();
        portalDb = (PortalDb) portalDb.getQObjectDb(id);
        String name = portalDb.getString("name") + " 复制";
        String icon = portalDb.getString("icon");
        String depts = portalDb.getString("depts");
        String roles = portalDb.getString("roles");
        int status = portalDb.getInt("status");
        int kind = portalDb.getInt("kind");
        String setup = portalDb.getString("setup");

        int isFixed = 1;
        boolean re;
        try {
            re = portalDb.create(new JdbcTemplate(), new Object[]{ConstUtil.USER_SYSTEM, name, portalDb.getNextOrders(ConstUtil.USER_SYSTEM), PortalDb.SYSTEM_ID_NONE, icon, isFixed, depts, roles, status, kind, setup});
        } catch (ResKeyException e) {
            LogUtil.getLog(getClass()).error(e);
            return new Result<>(false, e.getMessage(request));
        }
        return new Result<>(re);
    }

    @ApiOperation(value = "取得门户设计", notes = "取得门户设计", httpMethod = "POST")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "id", value = "门户ID", required = true, dataType = "Long"),
    })
    @ResponseBody
    @RequestMapping(value = "/getSetup", method = RequestMethod.POST, produces = {"text/html;charset=UTF-8;", "application/json;charset=UTF-8;"})
    public Result<Object> getSetup(@RequestParam(required = true)Long id) {
        PortalDb portalDb = new PortalDb();
        portalDb = (PortalDb) portalDb.getQObjectDb(id);
        JSONObject json = new JSONObject();
        json.put("id", portalDb.getInt("id"));
        json.put("setup", portalDb.getString("setup"));
        return new Result<>(json);
    }

    @ApiOperation(value = "修改门户设计", notes = "修改门户设计", httpMethod = "POST")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "id", value = "门户ID", required = true, dataType = "Long"),
            @ApiImplicitParam(name = "setup", value = "门户设计内容", required = true, dataType = "Long"),
    })
    @ResponseBody
    @RequestMapping(value = "/updateSetup", method = RequestMethod.POST, produces = {"text/html;charset=UTF-8;", "application/json;charset=UTF-8;"})
    public Result<Object> updateSetup(@RequestParam(required = true)Long id, @RequestParam(required = true)String setup) {
        PortalDb portalDb = new PortalDb();
        portalDb = (PortalDb) portalDb.getQObjectDb(new Long(id));
        portalDb.set("setup", setup);
        try {
            return new Result<>(portalDb.save());
        } catch (ResKeyException e) {
            LogUtil.getLog(getClass()).error(e);
            return new Result<>(false, e.getMessage(request));
        }
    }

    @ApiOperation(value = "排序", notes = "排序", httpMethod = "POST")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "id", value = "门户ID", required = true, dataType = "Long"),
    })
    @ResponseBody
    @RequestMapping(value = "/sort", method = RequestMethod.POST, produces = {"text/html;charset=UTF-8;", "application/json;charset=UTF-8;"})
    public Result<Object> sort(@RequestParam(required = true) String ids) {
        boolean re = false;
        String[] arr = StrUtil.split(ids, ",");
        PortalDb portalDb = new PortalDb();
        try {
            for (int i = 0; i < arr.length; i++) {
                portalDb = (PortalDb) portalDb.getQObjectDb(StrUtil.toLong(arr[i]));
                portalDb.set("orders", i + 1);
                re = portalDb.save();
            }
        } catch (ResKeyException e) {
            LogUtil.getLog(getClass()).error(e);
            return new Result<>(false, e.getMessage());
        }
        return new Result<>(re);
    }

    @ApiOperation(value = "删除门户", notes = "删除门户", httpMethod = "POST")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "id", value = "门户ID", required = true, dataType = "Long"),
    })
    @ResponseBody
    @RequestMapping(value = "/del", method = RequestMethod.POST, produces = {"text/html;charset=UTF-8;", "application/json;charset=UTF-8;"})
    public Result<Object> del(@RequestParam(required = true) Long id) {
        boolean re = false;
        try {
            PortalDb portalDb = new PortalDb();
            portalDb = (PortalDb)portalDb.getQObjectDb(id);
            re = portalDb.del();
        } catch (ResKeyException e) {
            LogUtil.getLog(getClass()).error(e);
            return new Result<>(false, e.getMessage());
        }
        return new Result<>(re);
    }

    @ApiOperation(value = "取得门户中的卡片", notes = "取得门户中的卡片", httpMethod = "POST")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "id", value = "门户ID", required = true, dataType = "Long"),
    })
    @ResponseBody
    @RequestMapping(value = "/getCard", method = RequestMethod.POST, produces = {"application/json;charset=UTF-8;"})
    public Result<Object> getCard(@RequestParam(required = true)Long id) {
        String userName = authUtil.getUserName();
        boolean isAdmin = authUtil.isUserPrivValid(request, ConstUtil.PRIV_ADMIN);
        DesktopCardUtil desktopCardUtil = new DesktopCardUtil();
        DesktopCard desktopCard = desktopCardUtil.getCardById(id);
        IDesktopCard iDesktopCard = DesktopCardFactory.getIDesktopCard(desktopCard);
        if (iDesktopCard == null) {
            LogUtil.getLog(getClass()).warn(desktopCard.getTitle() + " " + desktopCard.getCardType() + " 不存在");
            return new Result<>(false, desktopCard.getTitle() + " " + desktopCard.getCardType() + " 不存在");
        }

        JSONObject json = new JSONObject();
        boolean canSeeCard = false;
        String roles = desktopCard.getRoles();
        if (isAdmin) {
            canSeeCard = true;
        }
        else {
            if (!StringUtils.isEmpty(roles)) {
                String[] arr = StrUtil.split(roles, ",");
                for (String roleCode : arr) {
                    if (userCache.isUserOfRole(userName, roleCode)) {
                        canSeeCard = true;
                        break;
                    }
                }
            }
            else {
                canSeeCard = true;
            }
        }
        if (!canSeeCard) {
            return new Result<>(false, desktopCard.getTitle() + " 无权查看");
        }

        json.put("icon", iDesktopCard.getIcon());
        json.put("title", iDesktopCard.getTitle());
        json.put("isLink", iDesktopCard.isLink());
        json.put("url", iDesktopCard.getUrl());
        json.put("startVal", iDesktopCard.getStartVal());
        json.put("endVal", iDesktopCard.getEndVal(request));
        json.put("unit", iDesktopCard.getUnit());
        json.put("type", desktopCard.getCardType());
        json.put("moduleCode", desktopCard.getModuleCode());
        json.put("bgColor", desktopCard.getBgColor());
        json.put("query", iDesktopCard.getQuery());
        json.put("style", desktopCard.getStyle());
        return new Result<>(json);
    }

    @ApiOperation(value = "取得门户中的卡片", notes = "取得门户中的卡片", httpMethod = "POST")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "id", value = "门户ID", required = true, dataType = "Long"),
    })
    @ResponseBody
    @RequestMapping(value = "/listCard", method = RequestMethod.POST, produces = {"application/json;charset=UTF-8;"})
    public Result<Object> listCard(@RequestParam(required = true)Long id) {
        JSONArray ary = new JSONArray();
        String userName = authUtil.getUserName();
        boolean isAdmin = authUtil.isUserPrivValid(request, ConstUtil.PRIV_ADMIN);
        DesktopCardUtil desktopCardUtil = new DesktopCardUtil();
        List<DesktopCard> cardList = desktopCardUtil.listByPortal(id);
        for (DesktopCard desktopCard : cardList) {
            IDesktopCard iDesktopCard = DesktopCardFactory.getIDesktopCard(desktopCard);
            if (iDesktopCard == null) {
                LogUtil.getLog(getClass()).warn(desktopCard.getTitle() + " " + desktopCard.getCardType() + " 不存在");
                continue;
            }

            JSONObject json = new JSONObject();
            ary.add(json);

            boolean canSeeCard = false;
            String roles = desktopCard.getRoles();
            if (isAdmin) {
                canSeeCard = true;
            }
            else {
                if (!StringUtils.isEmpty(roles)) {
                    String[] arr = StrUtil.split(roles, ",");
                    for (String roleCode : arr) {
                        if (userCache.isUserOfRole(userName, roleCode)) {
                            canSeeCard = true;
                            break;
                        }
                    }
                }
                else {
                    canSeeCard = true;
                }
            }
            if (!canSeeCard) {
                continue;
            }

            json.put("icon", iDesktopCard.getIcon());
            json.put("title", iDesktopCard.getTitle());
            json.put("isLink", iDesktopCard.isLink());
            if (iDesktopCard.isLink()) {
                json.put("url", desktopCard.getUrl());
            } else {
                json.put("url", iDesktopCard.getUrl());
            }
            json.put("startVal", iDesktopCard.getStartVal());
            json.put("endVal", iDesktopCard.getEndVal(request));
            json.put("unit", iDesktopCard.getUnit());
            json.put("type", desktopCard.getCardType());
            json.put("moduleCode", desktopCard.getModuleCode());
            json.put("bgColor", desktopCard.getBgColor());
            json.put("query", iDesktopCard.getQuery());
            json.put("style", desktopCard.getStyle());
            json.put("color", desktopCard.getColor());
        }
        return new Result<>(ary);
    }

    @ApiOperation(value = "取得所有的卡片", notes = "取得所有的卡片", httpMethod = "POST")
    @ResponseBody
    @RequestMapping(value = "/listAllCard", method = RequestMethod.POST, produces = {"application/json;charset=UTF-8;"})
    public Result<Object> listAllCard() {
        JSONArray ary = new JSONArray();
        String userName = authUtil.getUserName();
        boolean isAdmin = authUtil.isUserPrivValid(request, ConstUtil.PRIV_ADMIN);
        DesktopCardUtil desktopCardUtil = new DesktopCardUtil();
        List<DesktopCard> cardList = desktopCardUtil.listAll();
        for (DesktopCard desktopCard : cardList) {
            IDesktopCard iDesktopCard = DesktopCardFactory.getIDesktopCard(desktopCard);
            if (iDesktopCard == null) {
                LogUtil.getLog(getClass()).warn(desktopCard.getTitle() + " " + desktopCard.getCardType() + " 不存在");
                continue;
            }

            boolean canSeeCard = false;
            String roles = desktopCard.getRoles();
            if (isAdmin) {
                canSeeCard = true;
            }
            else {
                if (!StringUtils.isEmpty(roles)) {
                    String[] arr = StrUtil.split(roles, ",");
                    for (String roleCode : arr) {
                        if (userCache.isUserOfRole(userName, roleCode)) {
                            canSeeCard = true;
                            break;
                        }
                    }
                }
                else {
                    canSeeCard = true;
                }
            }
            if (!canSeeCard) {
                continue;
            }

            JSONObject json = new JSONObject();
            json.put("id", iDesktopCard.getId());
            json.put("name", iDesktopCard.getName());
            ary.add(json);
        }
        return new Result<>(ary);
    }

    @ApiOperation(value = "取得门户中的应用卡片", notes = "取得门户中的应用卡片", httpMethod = "POST")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "id", value = "门户ID", required = true, dataType = "Long"),
    })
    @ResponseBody
    @RequestMapping(value = "/listCardByApplication", method = RequestMethod.POST, produces = {"application/json;charset=UTF-8;"})
    public Result<Object> listCardByApplication(@RequestParam(required = true)Long id, Integer count) {
        JSONArray ary = new JSONArray();
        String userName = authUtil.getUserName();
        boolean isAdmin = authUtil.isUserPrivValid(request, ConstUtil.PRIV_ADMIN);
        DesktopCardUtil desktopCardUtil = new DesktopCardUtil();
        List<DesktopCard> cardList = desktopCardUtil.listByApplication(id, count);
        for (DesktopCard desktopCard : cardList) {
            IDesktopCard iDesktopCard = DesktopCardFactory.getIDesktopCard(desktopCard);
            if (iDesktopCard == null) {
                LogUtil.getLog(getClass()).warn(desktopCard.getTitle() + " " + desktopCard.getCardType() + " 不存在");
                continue;
            }

            JSONObject json = new JSONObject();
            ary.add(json);

            boolean canSeeCard = false;
            String roles = desktopCard.getRoles();
            if (isAdmin) {
                canSeeCard = true;
            }
            else {
                if (!StringUtils.isEmpty(roles)) {
                    String[] arr = StrUtil.split(roles, ",");
                    for (String roleCode : arr) {
                        if (userCache.isUserOfRole(userName, roleCode)) {
                            canSeeCard = true;
                            break;
                        }
                    }
                }
                else {
                    canSeeCard = true;
                }
            }
            if (!canSeeCard) {
                continue;
            }

            json.put("icon", iDesktopCard.getIcon());
            json.put("title", iDesktopCard.getTitle());
            json.put("isLink", iDesktopCard.isLink());
            json.put("url", iDesktopCard.getUrl());
            json.put("startVal", iDesktopCard.getStartVal());
            json.put("endVal", iDesktopCard.getEndVal(request));
            json.put("unit", iDesktopCard.getUnit());
            json.put("type", desktopCard.getCardType());
            json.put("moduleCode", desktopCard.getModuleCode());
            json.put("bgColor", desktopCard.getBgColor());
            json.put("query", iDesktopCard.getQuery());
        }
        return new Result<>(ary);
    }

    @ApiOperation(value = "取得模块中的卡片", notes = "取得模块中的卡片", httpMethod = "POST")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "moduleCode", value = "模块编码", required = true, dataType = "String"),
    })
    @ResponseBody
    @RequestMapping(value = "/listCardByModule", method = RequestMethod.POST, produces = {"application/json;charset=UTF-8;"})
    public Result<Object> listCardByModule(@RequestParam(required = true)String moduleCode) {
        JSONArray ary = new JSONArray();
        String userName = authUtil.getUserName();
        boolean isAdmin = authUtil.isUserPrivValid(request, ConstUtil.PRIV_ADMIN);
        DesktopCardUtil desktopCardUtil = new DesktopCardUtil();
        List<DesktopCard> cardList = desktopCardUtil.listByModule(moduleCode);
        for (DesktopCard desktopCard : cardList) {
            IDesktopCard iDesktopCard = DesktopCardFactory.getIDesktopCard(desktopCard);
            if (iDesktopCard == null) {
                LogUtil.getLog(getClass()).warn(desktopCard.getTitle() + " " + desktopCard.getCardType() + " 不存在");
                continue;
            }

            JSONObject json = new JSONObject();
            ary.add(json);

            boolean canSeeCard = false;
            String roles = desktopCard.getRoles();
            if (isAdmin) {
                canSeeCard = true;
            }
            else {
                if (!StringUtils.isEmpty(roles)) {
                    String[] arr = StrUtil.split(roles, ",");
                    for (String roleCode : arr) {
                        if (userCache.isUserOfRole(userName, roleCode)) {
                            canSeeCard = true;
                            break;
                        }
                    }
                }
                else {
                    canSeeCard = true;
                }
            }
            if (!canSeeCard) {
                continue;
            }

            json.put("icon", iDesktopCard.getIcon());
            json.put("title", iDesktopCard.getTitle());
            json.put("isLink", iDesktopCard.isLink());
            json.put("url", iDesktopCard.getUrl());
            json.put("startVal", iDesktopCard.getStartVal());
            json.put("endVal", iDesktopCard.getEndVal(request));
            json.put("unit", iDesktopCard.getUnit());
            json.put("type", desktopCard.getCardType());
            json.put("moduleCode", desktopCard.getModuleCode());
            json.put("bgColor", desktopCard.getBgColor());
            json.put("query", iDesktopCard.getQuery());
        }
        return new Result<>(ary);
    }

    @ApiOperation(value = "取得图片轮播", notes = "取得图片轮播", httpMethod = "POST")
    @ResponseBody
    @RequestMapping(value = "/listCarouselPicture", method = RequestMethod.POST, produces = {"application/json;charset=UTF-8;"})
    public Result<Object> listCarouselPicture() {
        JSONArray ary = new JSONArray();
        String formCode = "carousel_picture";
        FormDAO fdao = new FormDAO();
        String sql = "select id from " + FormDb.getTableName(formCode);
        Vector<FormDAO> v = fdao.list(formCode, sql);
        for (FormDAO dao : v) {
            JSONObject json = new JSONObject();
            ary.add(json);

            json.put("id", dao.getId());
            json.put("name", dao.getFieldValue("name"));
        }
        return new Result<>(ary);
    }

    @ApiOperation(value = "取得图片轮播", notes = "取得图片轮播", httpMethod = "POST")
    @ResponseBody
    @RequestMapping(value = "/getCarouselPictureInfo", method = RequestMethod.POST, produces = {"application/json;charset=UTF-8;"})
    public Result<Object> getCarouselPictureInfo(@RequestParam(required = true)Long id, Integer rowCount) {
        String formCode = "carousel_picture";
        FormDAO fdao = new FormDAO();
        String sql = "select id from " + FormDb.getTableName(formCode) + " where id=" + id;
        Vector<FormDAO> v = fdao.list(formCode, sql);
        if (v.size() == 0) {
            return new Result<>(false, "目录为空");
        }

        String dirCode = v.elementAt(0).getFieldValue("dir_code");
        return new Result<>(docService.listImage(dirCode, rowCount));
    }

}
