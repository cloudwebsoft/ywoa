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
import com.alibaba.fastjson.JSONObject;
import com.cloudweb.oa.entity.Department;
import com.cloudweb.oa.exception.ValidateException;
import com.cloudweb.oa.service.IDepartmentService;
import com.cloudweb.oa.utils.ConstUtil;
import com.cloudweb.oa.utils.ResponseUtil;
import com.cloudwebsoft.framework.db.JdbcTemplate;
import com.redmoon.oa.Config;
import com.redmoon.oa.dept.DeptDb;
import com.redmoon.oa.flow.FormDb;
import com.redmoon.oa.flow.FormForm;
import com.redmoon.oa.util.TwoDimensionCode;
import org.apache.commons.configuration.PropertiesConfiguration;
import org.apache.jcs.access.exception.CacheException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
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

    /**
     *
     * @return
     * @throws ValidateException
     */
    @RequestMapping(value = "/setup/setupDbProp", produces = {"text/html;charset=UTF-8;", "application/json;charset=UTF-8;"})
    @ResponseBody
    public String setupDbProp() throws ValidateException {
        try {
            String user = ParamUtil.get(request, "user");
            String ip = ParamUtil.get(request, "ip");
            int port = ParamUtil.getInt(request, "port");
            String database = ParamUtil.get(request, "database");
            String pwd = ParamUtil.get(request, "pwd");
            String url = "jdbc:mysql://" + ip + ":" + port + "/" + database + "?useUnicode=true&characterEncoding=UTF-8&zeroDateTimeBehavior=CONVERT_TO_NULL&useSSL=false&serverTimezone=Asia/Shanghai";
            int maximumConnectionCount = ParamUtil.getInt(request, "maximum_connection_count");

            /*
            // 用Properties保存会破坏原来的格式，可读性差，故改为PropertiesConfiguration
            Properties properties = new Properties();
            try {
                properties.load(Thread.currentThread().getContextClassLoader().getResourceAsStream("application.properties"));
            } catch (IOException e) {
                e.printStackTrace();
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
            e.printStackTrace();
            return responseUtil.getResultJson(false, e.getMessage()).toString();
        }
        return responseUtil.getResultJson(true).toString();
    }

    @RequestMapping(value = "/setup/checkPool", produces = {"text/html;charset=UTF-8;", "application/json;charset=UTF-8;"})
    @ResponseBody
    public String checkPool(String database) {
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
                e.printStackTrace();
            }

            com.redmoon.oa.Config oaCfg = new com.redmoon.oa.Config();
            String strVersion = StrUtil.getNullStr(oaCfg.get("version"));
            double version = StrUtil.toDouble(strVersion, -1);
            try {
                // System.out.println("version=" + version);
                if (version < 3) {
                    // 添加拉单及冲抵字段
                    String sql = "SELECT TABLE_NAME FROM INFORMATION_SCHEMA.TABLES WHERE TABLE_SCHEMA = '" + database + "'";
                    JdbcTemplate jt = new JdbcTemplate();
                    ResultIterator ri = jt.executeQuery(sql);
                    while (ri.hasNext()) {
                        ResultRecord rr = (ResultRecord) ri.next();
                        String tableName = rr.getString(1).toLowerCase();
                        if (tableName.startsWith("form_table_")) {
                            sql = "ALTER TABLE `" + tableName + "` ADD COLUMN `cws_flag` TINYINT UNSIGNED NOT NULL DEFAULT 0 AFTER `cws_status`, ADD COLUMN `cws_quote_id` INTEGER UNSIGNED AFTER `cws_flag`";
                            try {
                                jt.executeUpdate(sql);
                            } catch (Exception e) {
                                e.printStackTrace();
                            }

                            sql = "ALTER TABLE `" + tableName + "` ADD COLUMN `cws_progress` INTEGER UNSIGNED NOT NULL DEFAULT 0 AFTER `cws_status`";
                            try {
                                jt.executeUpdate(sql);
                            } catch (Exception e) {
                                e.printStackTrace();
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
                        if (tableName.startsWith("form_table_")) {
                            // sql = "ALTER TABLE `" + tableName + "` DROP COLUMN `cws_parent_form`";
                            sql = "ALTER TABLE `" + tableName + "` ADD COLUMN `cws_parent_form` varchar(20) AFTER `cws_status`";
                            try {
                                jt.executeUpdate(sql);
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    }
                }
                else if (version < 5.0) {
                    // 添加cws_create_date cws_modify_date cws_finish_date抵字段
                    String sql = "SELECT TABLE_NAME FROM INFORMATION_SCHEMA.TABLES WHERE TABLE_SCHEMA = '" + database + "'";
                    JdbcTemplate jt = new JdbcTemplate();
                    ResultIterator ri = jt.executeQuery(sql);
                    while (ri.hasNext()) {
                        ResultRecord rr = (ResultRecord) ri.next();
                        String tableName = rr.getString(1).toLowerCase();
                        if (tableName.startsWith("form_table_") && !tableName.endsWith("_log")) {
                            sql = "ALTER TABLE `" + tableName + "` ADD COLUMN `cws_create_date` datetime AFTER `cws_status`,  ADD COLUMN `cws_modify_date` datetime AFTER `cws_status`,  ADD COLUMN `cws_finish_date` datetime AFTER `cws_status`";
                            try {
                                jt.executeUpdate(sql);
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    }
                }
                else if (version < 6.0) {
                    // 添加cws_quote_form
                    String sql = "SELECT TABLE_NAME FROM INFORMATION_SCHEMA.TABLES WHERE TABLE_SCHEMA = '" + database + "'";
                    JdbcTemplate jt = new JdbcTemplate();
                    ResultIterator ri = jt.executeQuery(sql);
                    while (ri.hasNext()) {
                        ResultRecord rr = (ResultRecord) ri.next();
                        String tableName = rr.getString(1).toLowerCase();
                        if (tableName.startsWith("form_table_") && !tableName.endsWith("_log")) {
                            sql = "ALTER TABLE `" + tableName + "` ADD COLUMN `cws_quote_form` varchar(20) AFTER `cws_status`";
                            try {
                                jt.executeUpdate(sql);
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    }
                }
                else {
                    // 因嵌套表格2控件的version改为了2，所以需处理一下
                    String sql = "update form_field set description=defaultValue where description='' and macroType='nest_sheet'";
                    try {
                        JdbcTemplate jt = new JdbcTemplate();
                        jt.executeUpdate(sql);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
            catch (SQLException e) {
                return responseUtil.getResultJson(false, e.getMessage()).toString();
            }
        }
        return responseUtil.getResultJson(true).toString();
    }

    @RequestMapping(value = "/setup/setupConfig", produces = {"text/html;charset=UTF-8;", "application/json;charset=UTF-8;"})
    @ResponseBody
    public String setupConfig() throws ValidateException {
        try {
            XMLConfig cfg = new XMLConfig("config_sys.xml", false, "utf-8");
            XMLConfig cfg_oa = new XMLConfig("config.xml", false, "utf-8");

            String publicIp = ParamUtil.get(request, "publicIp");

            Enumeration e = request.getParameterNames();
            while (e.hasMoreElements()) {
                String fieldName = (String) e.nextElement();
                if (fieldName.startsWith("Application") || fieldName.startsWith("i18n")) {
                    String value = ParamUtil.get(request, fieldName);
                    cfg.set(fieldName, value);

                    System.out.println(fieldName + "=" + value);
                }
            }

            // 将Application.server置为外网地址
            if (!publicIp.trim().equals("")) {
                if (IpUtil.isDomain(publicIp)) {
                    cfg.set("Application.server", publicIp);
                } else {
                    if (!IpUtil.isInnerIP(publicIp)) {
                        cfg.set("Application.server", publicIp);
                    }
                }

            }
            cfg.writemodify();
            Global.getInstance().init();

            String deptName = ParamUtil.get(request, "deptName");
            if (!deptName.equals("")) {
                Department department = departmentService.getDepartment(ConstUtil.DEPT_ROOT);
                department.setName(deptName);
                departmentService.updateByCode(department);
            }

            String value = ParamUtil.get(request, "Application.name");
            Config myconfig = Config.getInstance();
            myconfig.put("enterprise", value);

            TwoDimensionCode.generate2DCodeByMobileClient();//生成手机端二维码
        } catch (Exception e) {
            e.printStackTrace();
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
}
