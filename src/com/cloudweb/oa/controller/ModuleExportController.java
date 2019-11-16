package com.cloudweb.oa.controller;

import java.io.*;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Vector;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.ModelAndView;

import cn.js.fan.util.ErrMsgException;
import cn.js.fan.util.ParamUtil;
import cn.js.fan.util.ResKeyException;
import cn.js.fan.util.StrUtil;

import com.cloudwebsoft.framework.db.JdbcTemplate;
import com.cloudwebsoft.framework.util.LogUtil;
import com.redmoon.oa.basic.SelectDb;
import com.redmoon.oa.basic.SelectMgr;
import com.redmoon.oa.basic.SelectOptionDb;
import com.redmoon.oa.dept.DeptUserDb;
import com.redmoon.oa.flow.FormDb;
import com.redmoon.oa.flow.FormField;
import com.redmoon.oa.flow.macroctl.MacroCtlMgr;
import com.redmoon.oa.flow.macroctl.MacroCtlUnit;
import com.redmoon.oa.person.UserCache;
import com.redmoon.oa.person.UserDb;
import com.redmoon.oa.pvg.Privilege;
import com.redmoon.oa.visual.FormDAO;
import com.redmoon.oa.visual.ModuleExportTemplateDb;
import com.redmoon.oa.visual.ModuleRelateDb;
import com.redmoon.oa.visual.ModuleSetupDb;

@Controller
@RequestMapping("/visual")
public class ModuleExportController {
    @Autowired
    private HttpServletRequest request;

    @RequestMapping(value = "/module_export_list", method = RequestMethod.GET)
    public String getModuleExportList(@RequestParam(value = "code", required = false, defaultValue = "")
                                              String code, @RequestParam String formCode, Model model) {
        model.addAttribute("code", code);
        model.addAttribute("formCode", formCode);

        ModuleExportTemplateDb mitd = new ModuleExportTemplateDb();
        String sql = mitd.getTable().getSql("listForForm");
        Vector v = mitd.list(sql, new Object[]{formCode});

        model.addAttribute("items", v);

        return "/visual/module_export_list";
    }

    /**
     * @param id
     * @return
     * @Description:
     */
    @ResponseBody
    @RequestMapping(value = "/delExport", method = RequestMethod.GET, produces = {"text/html;charset=UTF-8;", "application/json;"})
    public String delExport(@RequestParam(value = "id", required = true)
                                    long id) {
        // 注意在注解中加入produces是为了防止乱码
        ModuleExportTemplateDb mid = new ModuleExportTemplateDb();
        mid = mid.getModuleExportTemplateDb(id);
        boolean re = false;
        try {
            re = mid.del();
        } catch (ResKeyException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        JSONObject json = new JSONObject();
        try {
            if (re) {
                json.put("ret", 1);
                json.put("msg", "删除成功！");
            } else {
                json.put("ret", 0);
                json.put("msg", "删除失败");
            }
        } catch (JSONException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        return json.toString();
    }

    @ResponseBody
    @RequestMapping(value = "/editExport", method = RequestMethod.POST, produces = {"text/html;charset=UTF-8;", "application/json;"})
    public String editExport(@RequestParam(value = "code", required = false, defaultValue = "")
                                     String code, @RequestParam String formCode, @RequestParam(value = "id", required = true) long id, Model model) {
        boolean re = false;

        String name = ParamUtil.get(request, "name");
        String cols = ParamUtil.get(request, "cols");
        String font_family = ParamUtil.get(request, "font_family");
        int font_size = ParamUtil.getInt(request, "font_size", 14);
        int is_bold = ParamUtil.getInt(request, "is_bold", 0);
        int line_height = ParamUtil.getInt(request, "line_height", 30);
        String fore_color = ParamUtil.get(request, "fore_color");
        String back_color = ParamUtil.get(request, "back_color");
        String roles = ParamUtil.get(request, "roleCodes");

        String bar_font_family = ParamUtil.get(request, "bar_font_family");
        int bar_font_size = ParamUtil.getInt(request, "bar_font_size", 14);
        int bar_is_bold = ParamUtil.getInt(request, "bar_is_bold", 0);
        int bar_line_height = ParamUtil.getInt(request, "bar_line_height", 30);
        String bar_fore_color = ParamUtil.get(request, "bar_fore_color");
        String bar_back_color = ParamUtil.get(request, "bar_back_color");
        String bar_name = ParamUtil.get(request, "bar_name");

        int is_serial_no = ParamUtil.getInt(request, "is_serial_no", 0);

        ModuleExportTemplateDb mid = new ModuleExportTemplateDb();
        mid = mid.getModuleExportTemplateDb(id);
        try {
            mid.set("name", name);
            mid.set("cols", cols);
            mid.set("font_family", font_family);
            mid.set("font_size", font_size);
            mid.set("is_bold", is_bold);
            mid.set("line_height", line_height);
            mid.set("fore_color", fore_color);
            mid.set("back_color", back_color);
            mid.set("roles", roles);

            mid.set("bar_font_family", bar_font_family);
            mid.set("bar_font_size", bar_font_size);
            mid.set("bar_is_bold", bar_is_bold);
            mid.set("bar_line_height", bar_line_height);
            mid.set("bar_fore_color", bar_fore_color);
            mid.set("bar_back_color", bar_back_color);
            mid.set("bar_name", bar_name);

            mid.set("is_serial_no", is_serial_no);

            re = mid.save();
        } catch (ResKeyException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
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
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        return json.toString();
    }

    @ResponseBody
    @RequestMapping(value = "/addExport", method = RequestMethod.POST, produces = {"text/html;charset=UTF-8;", "application/json;"})
    public String addExport(@RequestParam(value = "code", required = false, defaultValue = "")
                                    String code, @RequestParam String formCode) {
        String name = ParamUtil.get(request, "name");
        String cols = ParamUtil.get(request, "cols");

        String font_family = ParamUtil.get(request, "font_family");
        int font_size = ParamUtil.getInt(request, "font_size", 14);
        int is_bold = ParamUtil.getInt(request, "is_bold", 0);
        int height = ParamUtil.getInt(request, "line_height", 30);
        String fore_color = ParamUtil.get(request, "fore_color");
        String back_color = ParamUtil.get(request, "back_color");
        String roles = ParamUtil.get(request, "roleCodes");

        String bar_font_family = ParamUtil.get(request, "bar_font_family");
        int bar_font_size = ParamUtil.getInt(request, "bar_font_size", 14);
        int bar_is_bold = ParamUtil.getInt(request, "bar_is_bold", 0);
        int bar_line_height = ParamUtil.getInt(request, "bar_line_height", 30);
        String bar_fore_color = ParamUtil.get(request, "bar_fore_color");
        String bar_back_color = ParamUtil.get(request, "bar_back_color");
        String bar_name = ParamUtil.get(request, "bar_name");

        int is_serial_no = ParamUtil.getInt(request, "is_serial_no", 0);

        boolean re = false;
        ModuleExportTemplateDb mid = new ModuleExportTemplateDb();
        try {
            re = mid.create(new JdbcTemplate(), new Object[]{name, formCode, cols, font_family, font_size, is_bold, height, fore_color, back_color, roles, bar_font_family, bar_font_size, bar_is_bold, bar_line_height, bar_fore_color, bar_back_color, bar_name, is_serial_no});
        } catch (ResKeyException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
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
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        return json.toString();
    }
}
