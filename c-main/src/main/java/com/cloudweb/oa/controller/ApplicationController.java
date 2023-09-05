package com.cloudweb.oa.controller;

import cn.js.fan.cache.jcs.RMCache;
import cn.js.fan.db.Conn;
import cn.js.fan.db.ResultIterator;
import cn.js.fan.db.ResultRecord;
import cn.js.fan.util.IpUtil;
import cn.js.fan.util.ParamUtil;
import cn.js.fan.util.StrUtil;
import cn.js.fan.util.XMLConfig;
import cn.js.fan.web.Global;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.cloudweb.oa.entity.Department;
import com.cloudweb.oa.exception.ValidateException;
import com.cloudweb.oa.service.IDepartmentService;
import com.cloudweb.oa.utils.ConfigUtil;
import com.cloudweb.oa.utils.ConstUtil;
import com.cloudweb.oa.utils.JwtUtil;
import com.cloudweb.oa.utils.ResponseUtil;
import com.cloudweb.oa.vo.Result;
import com.cloudwebsoft.framework.db.JdbcTemplate;
import com.cloudwebsoft.framework.util.LogUtil;
import com.redmoon.oa.Config;
import com.redmoon.oa.dept.DeptDb;
import com.redmoon.oa.flow.FormDb;
import com.redmoon.oa.flow.FormForm;
import com.redmoon.oa.pvg.Privilege;
import com.redmoon.oa.system.OaSysVerMgr;
import com.redmoon.oa.util.TwoDimensionCode;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import org.apache.commons.configuration.PropertiesConfiguration;
import org.apache.commons.jcs3.access.exception.CacheException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.net.URL;
import java.net.URLDecoder;
import java.sql.SQLException;
import java.util.Enumeration;
import java.util.Iterator;

@Controller
public class ApplicationController {
    @Autowired
    HttpServletRequest request;

    @Autowired
    ResponseUtil responseUtil;

    @Autowired
    IDepartmentService departmentService;

    @Autowired
    ConfigUtil configUtil;

    /**
     * @return
     * @throws ValidateException
     */
    @RequestMapping(value = "/setup/setupDbProp", produces = {"text/html;charset=UTF-8;", "application/json;charset=UTF-8;"})
    @ResponseBody
    public String setupDbProp() throws ValidateException {
        Privilege pvg = new Privilege();
        if (Global.getInstance().isFormalOpen()) {
            if (!pvg.isUserPrivValid(request, Privilege.ADMIN)) {
                throw new ValidateException("请以管理员身份登录以后再操作");
            }
        }

        try {
            String user = ParamUtil.get(request, "user");
            String ip = ParamUtil.get(request, "ip");
            int port = ParamUtil.getInt(request, "port");
            String database = ParamUtil.get(request, "database");
            String pwd = ParamUtil.get(request, "pwd");
            // String url = "jdbc:mysql://" + ip + ":" + port + "/" + database + "?useUnicode=true&characterEncoding=UTF-8&zeroDateTimeBehavior=CONVERT_TO_NULL&useSSL=false&serverTimezone=Asia/Shanghai";
            String url = "jdbc:mysql://" + ip + ":" + port + "/" + database + "?useUnicode=true&characterEncoding=UTF-8&zeroDateTimeBehavior=convertToNull&useSSL=false&connectTimeout=20000&socketTimeout=180000&serverTimezone=Asia/Shanghai";
            int maximumConnectionCount = ParamUtil.getInt(request, "maximum_connection_count");

            /*
            // 用Properties保存会破坏原来的格式，可读性差，故改为PropertiesConfiguration
            Properties properties = new Properties();
            try {
                properties.load(Thread.currentThread().getContextClassLoader().getResourceAsStream("application.properties"));
            } catch (IOException e) {
                LogUtil.getLog(getClass()).error(e);
            }
            properties.setProperty("spring.datasource.url", url);
            properties.setProperty("spring.datasource.username", user);
            properties.setProperty("spring.datasource.password", pwd);
            properties.setProperty("spring.datasource.maxActive", String.valueOf(maximumConnectionCount));
            URL confURL = getClass().getResource("/application.properties");
            String xmlpath = confURL.getFile();
            xmlpath = URLDecoder.decode(xmlpath);
            FileOutputStream fos = new FileOutputStream(xmlpath);
            properties.store(fos, "write properties");*/

            URL confURL = getClass().getResource("/application.properties");
            String xmlpath = confURL.getFile();
            xmlpath = URLDecoder.decode(xmlpath);
            FileInputStream fis = new FileInputStream(xmlpath);

            // 须用load及save加utf-8参数的形式，否则会乱码
            PropertiesConfiguration conf = new PropertiesConfiguration();
            conf.load(fis, "utf-8");
            fis.close();

            conf.setProperty("spring.datasource.url", url);
            conf.setProperty("spring.datasource.username", user);
            conf.setProperty("spring.datasource.password", pwd);
            conf.setProperty("spring.datasource.maxActive", String.valueOf(maximumConnectionCount));

            FileOutputStream fos = new FileOutputStream(xmlpath);
            conf.save(fos, "utf-8");
            fos.close();
        } catch (Exception e) {
            LogUtil.getLog(getClass()).error(e);
            return responseUtil.getResultJson(false, e.getMessage()).toString();
        }
        return responseUtil.getResultJson(true).toString();
    }

    @RequestMapping(value = "/setup/checkPool", produces = {"text/html;charset=UTF-8;", "application/json;charset=UTF-8;"})
    @ResponseBody
    public String checkPool(String database) throws ValidateException {
        Privilege pvg = new Privilege();
        if (Global.getInstance().isFormalOpen()) {
            if (!pvg.isUserPrivValid(request, Privilege.ADMIN)) {
                throw new ValidateException("请以管理员身份登录以后再操作");
            }
        }
        boolean isValid = false;
        String sqlRedmoonid = "select * from redmoonid";
        Conn conn = new Conn(Global.getDefaultDB());
        if (conn.getCon() != null) {
            try {
                conn.executeQuery(sqlRedmoonid);
                isValid = true;
            } catch (SQLException e) {
                return responseUtil.getResultJson(false, e.getMessage()).toString();
            } finally {
                conn.close();
            }
        }
        if (isValid) {
            // 清除缓存
            RMCache.refresh();
            RMCache rmcache = RMCache.getInstance();
            try {
                rmcache.clear();
            } catch (CacheException e) {
                LogUtil.getLog(getClass()).error(e);
            }

            com.redmoon.oa.Config oaCfg = new com.redmoon.oa.Config();
            String strVersion = StrUtil.getNullStr(oaCfg.get("version"));
            double version = StrUtil.toDouble(strVersion, -1);
            try {
                if (version < 3) {
                    // 添加拉单及冲抵字段
                    String sql = "SELECT TABLE_NAME FROM INFORMATION_SCHEMA.TABLES WHERE TABLE_SCHEMA = '" + database + "'";
                    JdbcTemplate jt = new JdbcTemplate();
                    ResultIterator ri = jt.executeQuery(sql);
                    while (ri.hasNext()) {
                        ResultRecord rr = ri.next();
                        String tableName = rr.getString(1).toLowerCase();
                        if (tableName.startsWith("ft_")) {
                            sql = "ALTER TABLE `" + tableName + "` ADD COLUMN `cws_flag` TINYINT UNSIGNED NOT NULL DEFAULT 0 AFTER `cws_status`, ADD COLUMN `cws_quote_id` INTEGER UNSIGNED AFTER `cws_flag`";
                            try {
                                jt.executeUpdate(sql);
                            } catch (Exception e) {
                                LogUtil.getLog(getClass()).error(e);
                            }

                            sql = "ALTER TABLE `" + tableName + "` ADD COLUMN `cws_progress` INTEGER UNSIGNED NOT NULL DEFAULT 0 AFTER `cws_status`";
                            try {
                                jt.executeUpdate(sql);
                            } catch (Exception e) {
                                LogUtil.getLog(getClass()).error(e);
                            }
                        }
                    }
                } else if (version < 4.0) {
                    // 添加cws_parent_form抵字段
                    String sql = "SELECT TABLE_NAME FROM INFORMATION_SCHEMA.TABLES WHERE TABLE_SCHEMA = '" + database + "'";
                    JdbcTemplate jt = new JdbcTemplate();
                    ResultIterator ri = jt.executeQuery(sql);
                    while (ri.hasNext()) {
                        ResultRecord rr = (ResultRecord) ri.next();
                        String tableName = rr.getString(1).toLowerCase();
                        if (tableName.startsWith("ft_")) {
                            // sql = "ALTER TABLE `" + tableName + "` DROP COLUMN `cws_parent_form`";
                            sql = "ALTER TABLE `" + tableName + "` ADD COLUMN `cws_parent_form` varchar(20) AFTER `cws_status`";
                            try {
                                jt.executeUpdate(sql);
                            } catch (Exception e) {
                                LogUtil.getLog(getClass()).error(e);
                            }
                        }
                    }
                } else if (version < 5.0) {
                    // 添加cws_create_date cws_modify_date cws_finish_date抵字段
                    String sql = "SELECT TABLE_NAME FROM INFORMATION_SCHEMA.TABLES WHERE TABLE_SCHEMA = '" + database + "'";
                    JdbcTemplate jt = new JdbcTemplate();
                    ResultIterator ri = jt.executeQuery(sql);
                    while (ri.hasNext()) {
                        ResultRecord rr = ri.next();
                        String tableName = rr.getString(1).toLowerCase();
                        if (tableName.startsWith("ft_") && !tableName.endsWith("_log")) {
                            sql = "ALTER TABLE `" + tableName + "` ADD COLUMN `cws_create_date` datetime AFTER `cws_status`,  ADD COLUMN `cws_modify_date` datetime AFTER `cws_status`,  ADD COLUMN `cws_finish_date` datetime AFTER `cws_status`";
                            try {
                                jt.executeUpdate(sql);
                            } catch (Exception e) {
                                LogUtil.getLog(getClass()).error(e);
                            }
                        }
                    }
                } else if (version < 6.0) {
                    // 添加cws_quote_form
                    String sql = "SELECT TABLE_NAME FROM INFORMATION_SCHEMA.TABLES WHERE TABLE_SCHEMA = '" + database + "'";
                    JdbcTemplate jt = new JdbcTemplate();
                    ResultIterator ri = jt.executeQuery(sql);
                    while (ri.hasNext()) {
                        ResultRecord rr = ri.next();
                        String tableName = rr.getString(1).toLowerCase();
                        if (tableName.startsWith("ft_") && !tableName.endsWith("_log")) {
                            sql = "ALTER TABLE `" + tableName + "` ADD COLUMN `cws_quote_form` varchar(20) AFTER `cws_status`";
                            try {
                                jt.executeUpdate(sql);
                            } catch (Exception e) {
                                LogUtil.getLog(getClass()).error(e);
                            }
                        }
                    }
                } else if (version == 6.0) {
                    // 添加cws_visited抵字段
                    String sql = "SELECT TABLE_NAME FROM INFORMATION_SCHEMA.TABLES WHERE TABLE_SCHEMA = '" + database + "'";
                    JdbcTemplate jt = new JdbcTemplate();
                    ResultIterator ri = jt.executeQuery(sql);
                    while (ri.hasNext()) {
                        ResultRecord rr = ri.next();
                        String tableName = rr.getString(1).toLowerCase();
                        if ("ft_module_log".equals(tableName) || (tableName.startsWith("ft_") && !tableName.endsWith("_log"))) {
                            sql = "ALTER TABLE `" + tableName + "` ADD COLUMN `cws_visited` TINYINT UNSIGNED NOT NULL DEFAULT 0 AFTER `cws_status`";
                            try {
                                jt.executeUpdate(sql);
                            } catch (Exception e) {
                                LogUtil.getLog(getClass()).error(e);
                            }
                        }
                    }
                } else {
                    // 因嵌套表格2控件的version改为了2，所以需处理一下
                    String sql = "update form_field set description=defaultValue where description='' and macroType='nest_sheet'";
                    try {
                        JdbcTemplate jt = new JdbcTemplate();
                        jt.executeUpdate(sql);
                    } catch (Exception e) {
                        LogUtil.getLog(getClass()).error(e);
                    }
                }
            } catch (SQLException e) {
                return responseUtil.getResultJson(false, e.getMessage()).toString();
            }
        }
        return responseUtil.getResultJson(true).toString();
    }

    /**
     * setup3.jsp调用，设置组织架构根节点名称
     *
     * @return
     * @throws ValidateException
     */
    @RequestMapping(value = "/setup/setupConfig", produces = {"text/html;charset=UTF-8;", "application/json;charset=UTF-8;"})
    @ResponseBody
    public String setupConfig() throws ValidateException {
        Privilege pvg = new Privilege();
        if (Global.getInstance().isFormalOpen()) {
            if (!pvg.isUserPrivValid(request, Privilege.ADMIN)) {
                throw new ValidateException("请以管理员身份登录以后再操作");
            }
        }

        try {
            /*XMLConfig cfg = new XMLConfig("config_sys.xml", false, "utf-8");

            String publicIp = ParamUtil.get(request, "publicIp");

            Enumeration e = request.getParameterNames();
            while (e.hasMoreElements()) {
                String fieldName = (String) e.nextElement();
                if (fieldName.startsWith("Application") || fieldName.startsWith("i18n")) {
                    String value = ParamUtil.get(request, fieldName);
                    cfg.set(fieldName, value);

                    LogUtil.getLog(getClass()).info(fieldName + "=" + value);
                }
            }

            // 将Application.server置为外网地址
            if (!"".equals(publicIp.trim())) {
                if (IpUtil.isDomain(publicIp)) {
                    cfg.set("Application.server", publicIp);
                } else {
                    if (!IpUtil.isInnerIP(publicIp)) {
                        cfg.set("Application.server", publicIp);
                    }
                }
            }

            cfg.writemodify();
            Global.getInstance().init();*/

            // 置realPath
            String realPath = ParamUtil.get(request, "realPath");
            if (realPath.endsWith("\\")) {
                realPath = realPath.substring(0, realPath.length() - 1 ) + "/";
            }
            else {
                // 在末尾自动加上 /
                if (!realPath.endsWith("/")) {
                    realPath += "/";
                }
            }

            configUtil.setApplicationProp("sys.web.uploadPath", realPath);
            Global.getInstance().setRealPath(realPath);

            String deptName = ParamUtil.get(request, "deptName");
            if (!"".equals(deptName)) {
                Department department = departmentService.getDepartment(ConstUtil.DEPT_ROOT);
                department.setName(deptName);
                departmentService.updateByCode(department);
            }

            String value = ParamUtil.get(request, "Application.name");
            Config myconfig = Config.getInstance();
            myconfig.put("enterprise", value);

            TwoDimensionCode.generate2DCodeByMobileClient();//生成手机端二维码
        } catch (Exception e) {
            LogUtil.getLog(getClass()).error(e);
        }

        // 过滤表单中的图片路径
        String sql = "select code from form";
        FormDb fd = new FormDb();
        Iterator ir = fd.list(sql).iterator();
        while (ir.hasNext()) {
            fd = (FormDb) ir.next();
            String content = fd.getContent();
            content = FormForm.initImgLink(content);
            fd.setContent(content);
            fd.saveContent();
        }
        return responseUtil.getResultJson(true).toString();
    }

    @ApiOperation(value = "取是否启用职位", notes = "取是否启用职位", httpMethod = "POST")
    @RequestMapping(value = "/config/getConfigInfo", method = RequestMethod.POST, produces = {"text/html;charset=UTF-8;", "application/json;"})
    @ResponseBody
    public Result<Object> getConfigInfo() {
        JSONObject json = new JSONObject();
        Config cfg = Config.getInstance();
        com.redmoon.weixin.Config weixinCfg = com.redmoon.weixin.Config.getInstance();
        boolean isUseWx = weixinCfg.getBooleanProperty("isUse");
        json.put("isPostUsed", cfg.getBoolean("isPostUsed"));

        boolean isSyncWxToOa = weixinCfg.getBooleanProperty("isSyncWxToOA");
        com.redmoon.dingding.Config dingdingCfg = com.redmoon.dingding.Config.getInstance();
        boolean isUseDingDing = dingdingCfg.getBooleanProperty("isUse");
        boolean isSyncDingDingToOa = dingdingCfg.getBooleanProperty("isSyncDingDingToOA");

        json.put("isUseWx", isUseWx);
        json.put("isSyncWxToOA", isSyncWxToOa);
        json.put("isUseDingDing", isUseDingDing);
        json.put("isSyncDingDingToOA", isSyncDingDingToOa);

        return new Result<>(json);
    }

    @ApiOperation(value = "取得界面配置", notes = "取得界面配置", httpMethod = "POST")
    @RequestMapping(value = "/setup/getUiSetup", method = RequestMethod.POST, produces = {"text/html;charset=UTF-8;", "application/json;"})
    @ResponseBody
    public Result<Object> getUiSetup() {
        String applicationCode = ParamUtil.get(request, "applicationCode");
        com.redmoon.oa.Config cfg = com.redmoon.oa.Config.getInstance();
        // 前端指定的applicationCode优先
        if (StrUtil.isEmpty(applicationCode)) {
            applicationCode = cfg.get("applicationCode");
        }

        String uiSetup = "{}";
        OaSysVerMgr oaSysVerMgr = new OaSysVerMgr();
        String uiSetups = oaSysVerMgr.getOaSysVer().getUiSetup();
        JSONArray ary = JSONArray.parseArray(uiSetups);
        if (ary == null) {
            ary = new JSONArray();
        }

        boolean isMenuGroupByApplication = cfg.getBooleanProperty("isMenuGroupByApplication");
        if (isMenuGroupByApplication) {
            // 从ary中取出原来对应的设置索引
            for (Object o : ary) {
                JSONObject jsonObject = (JSONObject) o;
                if (jsonObject.containsKey("applicationCode")) {
                    String appCode = jsonObject.getString("applicationCode");
                    if (appCode.equals(applicationCode)) {
                        uiSetup = jsonObject.toString();
                        break;
                    }
                } else {
                    if (StrUtil.isEmpty(applicationCode)) {
                        uiSetup = jsonObject.toString();
                        break;
                    }
                }
            }
        } else {
            if (ary.size() > 0) {
                uiSetup = ary.get(0).toString();
            }
        }
        return new Result<>(uiSetup);
    }

    @ApiOperation(value = "更新界面配置", notes = "更新界面配置", httpMethod = "POST")
    @ApiImplicitParam(name = "uiSetup", value = "模块编码", dataType = "String")
    @RequestMapping(value = "/setup/updateUiSetup", method = RequestMethod.POST, produces = {"text/html;charset=UTF-8;", "application/json;"})
    @ResponseBody
    public Result<Object> updateUiSetup(@RequestParam(required = true) String uiSetup) {
        String applicationCode = ParamUtil.get(request, "applicationCode");
        com.redmoon.oa.Config cfg = com.redmoon.oa.Config.getInstance();
        // 前端指定的applicationCode优先
        if (StrUtil.isEmpty(applicationCode)) {
            applicationCode = cfg.get("applicationCode");
        }
        JSONObject json = JSONObject.parseObject(uiSetup);
        json.put("applicationCode", applicationCode);

        boolean isMenuGroupByApplication = cfg.getBooleanProperty("isMenuGroupByApplication");
        OaSysVerMgr oaSysVerMgr = new OaSysVerMgr();
        String uiSetupDb = oaSysVerMgr.getOaSysVer().getUiSetup();
        JSONArray ary = JSONArray.parseArray(uiSetupDb);
        if (ary == null) {
            ary = new JSONArray();
        }
        if (isMenuGroupByApplication) {
            // 从ary中取出原来对应的设置索引
            int k = 0;
            boolean isFound = false;
            for (Object o : ary) {
                JSONObject jsonObject = (JSONObject) o;
                if (jsonObject.containsKey("applicationCode")) {
                    String appCode = jsonObject.getString("applicationCode");
                    if (appCode.equals(applicationCode)) {
                        isFound = true;
                        break;
                    }
                } else {
                    // 如果json中没有applicationCode，且applicationCode为空，则说明为默认的uiSetup
                    if ("".equals(applicationCode)) {
                        isFound = true;
                        break;
                    }
                }
                k++;
            }
            // 删除原来的设置
            if (isFound) {
                ary.remove(k);
            }
            ary.add(json);
        } else {
            ary.clear();
            ary.add(json);
        }
        return new Result<>(oaSysVerMgr.updateUiSetup(ary.toString()));
    }
}
