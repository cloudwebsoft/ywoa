package com.cloudweb.oa.controller;

import cn.js.fan.util.*;
import cn.js.fan.util.file.FileUtil;
import cn.js.fan.web.Global;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.cloudweb.oa.api.IModuleViewService;
import com.cloudweb.oa.utils.ConstUtil;
import com.cloudwebsoft.framework.db.JdbcTemplate;
import com.redmoon.oa.visual.ModuleRelateDb;
import com.redmoon.oa.visual.ModuleSetupDb;
import org.apache.commons.lang3.StringUtils;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import java.sql.SQLException;

@Controller
@RequestMapping("/visual")
public class ModuleSetupController {
    @Autowired
    private HttpServletRequest request;

    @Autowired
    private IModuleViewService moduleViewService;

    @ResponseBody
    @RequestMapping(value = "/colAdd", method = RequestMethod.POST, produces = {"text/html;charset=UTF-8;", "application/json;charset=UTF-8;"})
    public String colAdd() {
        String code = ParamUtil.get(request, "code");
        JSONObject json = new JSONObject();
        boolean re = false;
        try {
            try {
                re = moduleViewService.add(request, code);
            } catch (ErrMsgException e) {
                json.put("ret", "0");
                json.put("msg", e.getMessage());
                return json.toString();
            }
            if (re) {
                json.put("ret", 1);
                json.put("msg", "操作成功！");
            } else {
                json.put("ret", 0);
                json.put("msg", "操作失败");
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return json.toString();
    }

    @ResponseBody
    @RequestMapping(value = "/colModify", method = RequestMethod.POST, produces = {"text/html;charset=UTF-8;", "application/json;"})
    public String colModify() {
        String code = ParamUtil.get(request, "code");
        JSONObject json = new JSONObject();
        boolean re = false;
        try {
            try {
                re = moduleViewService.modify(request, code);
            } catch (ErrMsgException e) {
                json.put("ret", "0");
                json.put("msg", e.getMessage());
                return json.toString();
            }
            if (re) {
                json.put("ret", 1);
                json.put("msg", "操作成功！");
            } else {
                json.put("ret", 0);
                json.put("msg", "操作失败");
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return json.toString();
    }

    @ResponseBody
    @RequestMapping(value = "/colDel", method = RequestMethod.POST, produces = {"text/html;charset=UTF-8;", "application/json;"})
    public String colDel() {
        String code = ParamUtil.get(request, "code");
        JSONObject json = new JSONObject();
        boolean re = false;
        try {
            try {
                re = moduleViewService.del(request, code);
            } catch (ErrMsgException e) {
                json.put("ret", "0");
                json.put("msg", e.getMessage());
                return json.toString();
            }
            if (re) {
                json.put("ret", 1);
                json.put("msg", "操作成功！");
            } else {
                json.put("ret", 0);
                json.put("msg", "操作失败");
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return json.toString();
    }


    @ResponseBody
    @RequestMapping(value = "/linkModify", method = RequestMethod.POST, produces = {"text/html;charset=UTF-8;", "application/json;"})
    public String linkModify() {
        String code = ParamUtil.get(request, "code");
        JSONObject json = new JSONObject();
        boolean re = false;
        try {
            try {
                re = moduleViewService.modifyLink(request, code);
            } catch (ErrMsgException e) {
                json.put("ret", "0");
                json.put("msg", e.getMessage());
                return json.toString();
            }
            if (re) {
                json.put("ret", 1);
                json.put("msg", "操作成功！");
            } else {
                json.put("ret", 0);
                json.put("msg", "操作失败");
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return json.toString();
    }


    @ResponseBody
    @RequestMapping(value = "/linkDel", method = RequestMethod.POST, produces = {"text/html;charset=UTF-8;", "application/json;"})
    public String linkDel() {
        String code = ParamUtil.get(request, "code");
        JSONObject json = new JSONObject();
        boolean re = false;
        try {
            try {
                re = moduleViewService.delLink(request, code);
            } catch (ErrMsgException e) {
                json.put("ret", "0");
                json.put("msg", e.getMessage());
                return json.toString();
            }
            if (re) {
                json.put("ret", 1);
                json.put("msg", "操作成功！");
            } else {
                json.put("ret", 0);
                json.put("msg", "操作失败");
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return json.toString();
    }

    @ResponseBody
    @RequestMapping(value = "/linkSave", method = RequestMethod.POST, produces = {"text/html;charset=UTF-8;", "application/json;"})
    public String linkSave() {
        String code = ParamUtil.get(request, "code");
        String strResult = ParamUtil.get(request, "result");
        JSONObject json = new JSONObject();
        boolean re = false;
        try {
            try {
                ModuleSetupDb msd = new ModuleSetupDb();
                msd = msd.getModuleSetupDb(code);
                JSONObject result = new JSONObject(strResult);
                re = moduleViewService.saveLink(msd, result);
            } catch (ResKeyException e) {
                json.put("ret", "0");
                json.put("msg", e.getMessage(request));
                return json.toString();
            }
            if (re) {
                json.put("ret", 1);
                json.put("msg", "操作成功！");
            } else {
                json.put("ret", 0);
                json.put("msg", "操作失败");
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return json.toString();
    }


    @ResponseBody
    @RequestMapping(value = "/colSave", method = RequestMethod.POST, produces = {"text/html;charset=UTF-8;", "application/json;"})
    public String colSave() {
        String code = ParamUtil.get(request, "code");
        String strResult = ParamUtil.get(request, "result");
        JSONObject json = new JSONObject();
        boolean re = false;
        try {
            try {
                ModuleSetupDb msd = new ModuleSetupDb();
                msd = msd.getModuleSetupDb(code);
                JSONObject result = new JSONObject(strResult);
                re = moduleViewService.saveCol(msd, result);
            } catch (ResKeyException e) {
                json.put("ret", "0");
                json.put("msg", e.getMessage(request));
                return json.toString();
            }
            if (re) {
                json.put("ret", 1);
                json.put("msg", "操作成功！");
            } else {
                json.put("ret", 0);
                json.put("msg", "操作失败");
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return json.toString();
    }

    @ResponseBody
    @RequestMapping(value = "/linkAdd", method = RequestMethod.POST, produces = {"text/html;charset=UTF-8;", "application/json;"})
    public String linkAdd() {
        String code = ParamUtil.get(request, "code");
        JSONObject json = new JSONObject();
        boolean re = false;
        try {
            try {
                re = moduleViewService.addLink(request, code);
            } catch (ErrMsgException e) {
                json.put("ret", "0");
                json.put("msg", e.getMessage());
                return json.toString();
            }
            if (re) {
                json.put("ret", 1);
                json.put("msg", "操作成功！");
            } else {
                json.put("ret", 0);
                json.put("msg", "操作失败");
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return json.toString();
    }

    @ResponseBody
    @RequestMapping(value = "/condAdd", method = RequestMethod.POST, produces = {"text/html;charset=UTF-8;", "application/json;"})
    public String condAdd() {
        String code = ParamUtil.get(request, "code");
        JSONObject json = new JSONObject();
        boolean re = false;
        try {
            try {
                re = moduleViewService.addCond(request, code);
            } catch (ErrMsgException e) {
                json.put("ret", "0");
                json.put("msg", e.getMessage());
                return json.toString();
            }
            if (re) {
                json.put("ret", 1);
                json.put("msg", "操作成功！");
            } else {
                json.put("ret", 0);
                json.put("msg", "操作失败");
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return json.toString();
    }

    @ResponseBody
    @RequestMapping(value = "/condModify", method = RequestMethod.POST, produces = {"text/html;charset=UTF-8;", "application/json;"})
    public String condModify() {
        String code = ParamUtil.get(request, "code");
        JSONObject json = new JSONObject();
        boolean re = false;
        try {
            try {
                re = moduleViewService.modifyBtn(request, code);
            } catch (ErrMsgException e) {
                json.put("ret", "0");
                json.put("msg", e.getMessage());
                return json.toString();
            }
            if (re) {
                json.put("ret", 1);
                json.put("msg", "操作成功！");
            } else {
                json.put("ret", 0);
                json.put("msg", "操作失败");
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return json.toString();
    }

    @ResponseBody
    @RequestMapping(value = "/btnAdd", method = RequestMethod.POST, produces = {"text/html;charset=UTF-8;", "application/json;"})
    public String btnAdd() {
        String code = ParamUtil.get(request, "code");
        JSONObject json = new JSONObject();
        boolean re = false;
        try {
            try {
                String op = ParamUtil.get(request, "op");
                if ("addBtnBatch".equals(op)) {
                    re = moduleViewService.addBtnBatch(request, code);
                }
                else if ("addBtnFlow".equals(op)) {
                    re = moduleViewService.addBtnFlow(request, code);
                }
                else {
                    re = moduleViewService.addBtn(request, code);
                }
            } catch (ErrMsgException e) {
                json.put("ret", "0");
                json.put("msg", e.getMessage());
                return json.toString();
            }
            if (re) {
                json.put("ret", 1);
                json.put("msg", "操作成功！");
            } else {
                json.put("ret", 0);
                json.put("msg", "操作失败");
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return json.toString();
    }

    @ResponseBody
    @RequestMapping(value = "/btnModify", method = RequestMethod.POST, produces = {"text/html;charset=UTF-8;", "application/json;"})
    public String btnModify() {
        String code = ParamUtil.get(request, "code");
        JSONObject json = new JSONObject();
        boolean re = false;
        try {
            try {
                re = moduleViewService.modifyBtn(request, code);
            } catch (ErrMsgException e) {
                json.put("ret", "0");
                json.put("msg", e.getMessage());
                return json.toString();
            }
            if (re) {
                json.put("ret", 1);
                json.put("msg", "操作成功！");
            } else {
                json.put("ret", 0);
                json.put("msg", "操作失败");
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return json.toString();
    }

    @ResponseBody
    @RequestMapping(value = "/btnDel", method = RequestMethod.POST, produces = {"text/html;charset=UTF-8;", "application/json;"})
    public String btnDel() {
        String code = ParamUtil.get(request, "code");
        JSONObject json = new JSONObject();
        boolean re = false;
        try {
            try {
                re = moduleViewService.delBtn(request, code);
            } catch (ErrMsgException e) {
                json.put("ret", "0");
                json.put("msg", e.getMessage());
                return json.toString();
            }
            if (re) {
                json.put("ret", 1);
                json.put("msg", "操作成功！");
            } else {
                json.put("ret", 0);
                json.put("msg", "操作失败");
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return json.toString();
    }

    @ResponseBody
    @RequestMapping(value = "/btnSave", method = RequestMethod.POST, produces = {"text/html;charset=UTF-8;", "application/json;"})
    public String btnSave() {
        String code = ParamUtil.get(request, "code");
        String strResult = ParamUtil.get(request, "result");
        JSONObject json = new JSONObject();
        boolean re = false;
        try {
            try {
                ModuleSetupDb msd = new ModuleSetupDb();
                msd = msd.getModuleSetupDb(code);
                JSONObject result = new JSONObject(strResult);
                re = moduleViewService.saveBtn(msd, result);
            } catch (ResKeyException e) {
                json.put("ret", "0");
                json.put("msg", e.getMessage(request));
                return json.toString();
            }
            if (re) {
                json.put("ret", 1);
                json.put("msg", "操作成功！");
            } else {
                json.put("ret", 0);
                json.put("msg", "操作失败");
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return json.toString();
    }

    @ResponseBody
    @RequestMapping(value = "/condDel", method = RequestMethod.POST, produces = {"text/html;charset=UTF-8;", "application/json;"})
    public String condDel() {
        String code = ParamUtil.get(request, "code");
        JSONObject json = new JSONObject();
        boolean re = false;
        try {
            try {
                re = moduleViewService.delBtn(request, code);
            } catch (ErrMsgException e) {
                json.put("ret", "0");
                json.put("msg", e.getMessage());
                return json.toString();
            }
            if (re) {
                json.put("ret", 1);
                json.put("msg", "操作成功！");
            } else {
                json.put("ret", 0);
                json.put("msg", "操作失败");
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return json.toString();
    }

    @ResponseBody
    @RequestMapping(value = "/updatePropStat", method = RequestMethod.POST, produces = {"text/html;charset=UTF-8;", "application/json;"})
    public String updatePropStat(String code, String propStat) {
        JSONObject json = new JSONObject();
        boolean re = false;
        try {
            try {
                ModuleSetupDb msd = new ModuleSetupDb();
                msd = msd.getModuleSetupDb(code);
                msd.set("prop_stat", propStat);
                re = msd.save();
            } catch (ResKeyException e) {
                json.put("ret", "0");
                json.put("msg", e.getMessage(request));
                return json.toString();
            }
            if (re) {
                json.put("ret", 1);
                json.put("msg", "操作成功！");
            } else {
                json.put("ret", 0);
                json.put("msg", "操作失败");
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return json.toString();
    }

    @ResponseBody
    @RequestMapping(value = "/setMsgProp", method = RequestMethod.POST, produces = {"text/html;charset=UTF-8;", "application/json;charset=UTF-8;"})
    public String setMsgProp(String code) throws ResKeyException {
        com.alibaba.fastjson.JSONObject json = new com.alibaba.fastjson.JSONObject();
        ModuleSetupDb msd = new ModuleSetupDb();
        msd = msd.getModuleSetupDb(code);
        String msgProp = request.getParameter("msgProp");
        msd.set("msg_prop", msgProp);
        boolean re = msd.save();
        if (re) {
            json.put("ret", 1);
            json.put("msg", "操作成功！");
        }
        else {
            json.put("ret", 0);
            json.put("msg", "操作失败！");
        }
        return json.toString();
    }

    @ResponseBody
    @RequestMapping(value = "/setCols", method = RequestMethod.POST, produces = {"text/html;charset=UTF-8;", "application/json;charset=UTF-8;"})
    public String setCols(String code) {
        com.alibaba.fastjson.JSONObject json = new com.alibaba.fastjson.JSONObject();
        boolean re = false;
        try {
            re = moduleViewService.setCols(request, code);
        }
        catch (ErrMsgException e) {
            json.put("ret", "0");
            json.put("msg", e.getMessage());
            return json.toString();
        }
        if (re) {
            json.put("ret", "1");
            json.put("msg", "操作成功！");
        }
        else {
            json.put("ret", "0");
            json.put("msg", "操作失败！");
        }
        return json.toString();
    }

    @ResponseBody
    @RequestMapping(value = "/setPromptIcon", method = RequestMethod.POST, produces = {"text/html;charset=UTF-8;", "application/json;charset=UTF-8;"})
    public String setPromptIcon(String code) {
        com.alibaba.fastjson.JSONObject json = new com.alibaba.fastjson.JSONObject();
        boolean re = false;
        try {
            re = moduleViewService.setPromptIcon(request, code);
        }
        catch (ErrMsgException e) {
            json.put("ret", "0");
            json.put("msg", e.getMessage());
            return json.toString();
        }
        if (re) {
            json.put("ret", "1");
            json.put("msg", "操作成功！");
        }
        else {
            json.put("ret", "0");
            json.put("msg", "操作失败！");
        }
        return json.toString();
    }

    @ResponseBody
    @RequestMapping(value = "/addTag", method = RequestMethod.POST, produces = {"text/html;charset=UTF-8;", "application/json;charset=UTF-8;"})
    public String addTag(String code) {
        com.alibaba.fastjson.JSONObject json = new com.alibaba.fastjson.JSONObject();
        boolean re = false;
        try {
            re = moduleViewService.addTag(request, code);
        }
        catch (ErrMsgException e) {
            json.put("ret", "0");
            json.put("msg", e.getMessage());
            return json.toString();
        }

        if (re) {
            json.put("ret", "1");
            json.put("msg", "操作成功！");
        }
        else {
            json.put("ret", "0");
            json.put("msg", "操作失败！");
        }
        return json.toString();
    }

    @ResponseBody
    @RequestMapping(value = "/modifyTag", method = RequestMethod.POST, produces = {"text/html;charset=UTF-8;", "application/json;charset=UTF-8;"})
    public String modifyTag(String code) {
        com.alibaba.fastjson.JSONObject json = new com.alibaba.fastjson.JSONObject();
        boolean re = false;
        try {
            re = moduleViewService.modifyTag(request, code);
        }
        catch (ErrMsgException e) {
            json.put("ret", "0");
            json.put("msg", e.getMessage());
            return json.toString();
        }

        if (re) {
            json.put("ret", "1");
            json.put("msg", "操作成功！");
        }
        else {
            json.put("ret", "0");
            json.put("msg", "操作失败！");
        }
        return json.toString();
    }

    @ResponseBody
    @RequestMapping(value = "/delTag", method = RequestMethod.POST, produces = {"text/html;charset=UTF-8;", "application/json;charset=UTF-8;"})
    public String delTag(String code) {
        com.alibaba.fastjson.JSONObject json = new com.alibaba.fastjson.JSONObject();
        boolean re = false;
        try {
            re = moduleViewService.delTag(request, code);
        }
        catch (ErrMsgException e) {
            json.put("ret", "0");
            json.put("msg", e.getMessage());
            return json.toString();
        }

        if (re) {
            json.put("ret", "1");
            json.put("msg", "操作成功！");
        }
        else {
            json.put("ret", "0");
            json.put("msg", "操作失败！");
        }
        return json.toString();
    }

    @ResponseBody
    @RequestMapping(value = "/setModuleProps", method = RequestMethod.POST, produces = {"text/html;charset=UTF-8;", "application/json;charset=UTF-8;"})
    public String setModuleProps(String code) {
        com.alibaba.fastjson.JSONObject json = new com.alibaba.fastjson.JSONObject();
        boolean re = false;
        try {
            re = moduleViewService.setUse(request, code);
        }
        catch (ErrMsgException e) {
            json.put("ret", "0");
            json.put("msg", e.getMessage());
            return json.toString();
        }

        if (re) {
            json.put("ret", "1");
            json.put("msg", "操作成功！");
        }
        else {
            json.put("ret", "0");
            json.put("msg", "操作失败！");
        }
        return json.toString();
    }

    @ResponseBody
    @RequestMapping(value = "/setModuleFilter", method = RequestMethod.POST, produces = {"text/html;charset=UTF-8;", "application/json;charset=UTF-8;"})
    public String setModuleFilter(String code) {
        com.alibaba.fastjson.JSONObject json = new com.alibaba.fastjson.JSONObject();
        boolean re = false;
        try {
            re = moduleViewService.setFilter(request, code);
        }
        catch (ErrMsgException e) {
            json.put("ret", "0");
            json.put("msg", e.getMessage());
            return json.toString();
        }

        if (re) {
            json.put("ret", "1");
            json.put("msg", "操作成功！");
        }
        else {
            json.put("ret", "0");
            json.put("msg", "操作失败！");
        }
        return json.toString();
    }

    @ResponseBody
    @RequestMapping(value = "/addSubTag", method = RequestMethod.POST, produces = {"text/html;charset=UTF-8;", "application/json;charset=UTF-8;"})
    public String addSubTag(String code) {
        com.alibaba.fastjson.JSONObject json = new com.alibaba.fastjson.JSONObject();
        ModuleSetupDb msd = new ModuleSetupDb();
        msd = msd.getModuleSetupDb(code);
        double tagOrder = ParamUtil.getDouble(request, "tagOrder", -1);
        if (tagOrder==-1) {
            json.put("ret", 0);
            json.put("msg", "请填写顺序号！");
            return json.toString();
        }
        String tagUrl = ParamUtil.get(request, "tagUrl");
        if (tagUrl.equals("")) {
            tagUrl = "#"; // 宽度置为一个空格，以便于split时生成数组
        }
        String tagName = ParamUtil.get(request, "tagName");

        String tName = StrUtil.getNullStr(msd.getString("sub_nav_tag_name"));
        String tOrder = StrUtil.getNullStr(msd.getString("sub_nav_tag_order"));
        String tUrl = StrUtil.getNullStr(msd.getString("sub_nav_tag_url"));

        // 检查名称是否重复
        if (("|" + tName + "|").indexOf("|" + tagName + "|")!=-1) {
            json.put("ret", 0);
            json.put("msg", "名称：" + tagName + " 重复");
            return json.toString();
        }

        if (tName.equals("")) {
            tName = tagName;
            tUrl = tagUrl;
            tOrder = "" + tagOrder;
        }
        else {
            tName += "|" + tagName;
            tUrl += "|" + tagUrl;
            tOrder += "|" + tagOrder;
        }

        // 根据tagOrder排序
        String[] strOrderAry = StrUtil.split(tOrder, "\\|");
        int len = strOrderAry.length;
        double[] orderAry = new double[len];
        for (int i=0; i<len; i++) {
            orderAry[i] = StrUtil.toDouble(strOrderAry[i]);
        }
        String[] nameAry = StrUtil.split(tName, "\\|");
        String[] urlAry = tUrl.split("\\|");

        double temp;
        int size = len;
        String tempStr;
        // 外层循环，控制“冒泡”的最终位置
        for(int i=size-1; i>=1; i--){
            boolean end = true;
            // 内层循环，用于相临元素的比较
            for(int j=0; j < i; j++) {
                if(orderAry[j] > orderAry[j+1]) {
                    temp = orderAry[j];
                    orderAry[j] = orderAry[j+1];
                    orderAry[j+1] = temp;
                    end = false;

                    tempStr = nameAry[j];
                    nameAry[j] = nameAry[j+1];
                    nameAry[j+1] = tempStr;
                    tempStr = urlAry[j];
                    urlAry[j] = urlAry[j+1];
                    urlAry[j+1] = tempStr;
                }
            }
            if(end == true) {
                break;
            }
        }

        tName = "";
        tOrder = "";
        tUrl = "";

        for (int i=0; i<len; i++) {
            if (i==0) {
                tName = nameAry[i];
                tOrder = "" + orderAry[i];
                tUrl = urlAry[i];
            }
            else {
                tName += "|" + nameAry[i];
                tOrder += "|" + orderAry[i];
                tUrl += "|" + urlAry[i];
            }
        }

        msd.set("sub_nav_tag_name", tName);
        msd.set("sub_nav_tag_order", tOrder);
        msd.set("sub_nav_tag_url", tUrl);

        boolean re = false;
        try {
            re = msd.save();
        } catch (ResKeyException e) {
            e.printStackTrace();
        }
        if (re) {
            json.put("ret", "1");
            json.put("msg", "操作成功！");
        }
        else {
            json.put("ret", "0");
            json.put("msg", "操作失败！");
        }
        return json.toString();
    }

    @ResponseBody
    @RequestMapping(value = "/delSubTag", method = RequestMethod.POST, produces = {"text/html;charset=UTF-8;", "application/json;charset=UTF-8;"})
    public String delSubTag(String code) {
        com.alibaba.fastjson.JSONObject json = new com.alibaba.fastjson.JSONObject();
        ModuleSetupDb msd = new ModuleSetupDb();
        msd = msd.getModuleSetupDb(code);
        String tagName = ParamUtil.get(request, "tagName");

        String tName = StrUtil.getNullStr(msd.getString("sub_nav_tag_name"));
        String tUrl = StrUtil.getNullStr(msd.getString("sub_nav_tag_url"));
        String tOrder = StrUtil.getNullStr(msd.getString("sub_nav_tag_order"));
        String[] nameAry = StrUtil.split(tName, "\\|");
        String[] urlAry = StrUtil.split(tUrl, "\\|");
        String[] orderAry = StrUtil.split(tOrder, "\\|");

        tName = "";
        tUrl = "";
        tOrder = "";

        int len = nameAry.length;
        for (int i=0; i<len; i++) {
            if (nameAry[i].equals(tagName)) {
                continue;
            }
            if (tName.equals("")) {
                tName = nameAry[i];
                tUrl = urlAry[i];
                tOrder = orderAry[i];
            }
            else {
                tName += "|" + nameAry[i];
                tUrl += "|" + urlAry[i];
                tOrder += "|" + orderAry[i];
            }
        }
        msd.set("sub_nav_tag_name", tName);
        msd.set("sub_nav_tag_url", tUrl);
        msd.set("sub_nav_tag_order", tOrder);

        boolean re = false;
        try {
            re = msd.save();
        } catch (ResKeyException e) {
            json.put("ret", "0");
            json.put("msg", e.getMessage(request));
            return json.toString();
        }
        if (re) {
            json.put("ret", "1");
            json.put("msg", "操作成功！");
        }
        else {
            json.put("ret", "0");
            json.put("msg", "操作失败！");
        }
        return json.toString();
    }

    @ResponseBody
    @RequestMapping(value = "/modifySubTag", method = RequestMethod.POST, produces = {"text/html;charset=UTF-8;", "application/json;charset=UTF-8;"})
    public String modifySubTag(String code) {
        com.alibaba.fastjson.JSONObject json = new com.alibaba.fastjson.JSONObject();
        ModuleSetupDb msd = new ModuleSetupDb();
        msd = msd.getModuleSetupDb(code);
        String tagName = ParamUtil.get(request, "tagName");
        String newTagName = ParamUtil.get(request, "newTagName");

        String tagOrder = ParamUtil.get(request, "tagOrder");
        String tagUrl = ParamUtil.get(request, "tagUrl");
        if (tagUrl.equals("")) {
            tagUrl = "#"; // 宽度置为一个空格，以便于split时生成数组
        }

        String tName = StrUtil.getNullStr(msd.getString("sub_nav_tag_name"));
        String tUrl = StrUtil.getNullStr(msd.getString("sub_nav_tag_url"));
        String tOrder = StrUtil.getNullStr(msd.getString("sub_nav_tag_order"));
        String[] nameAry = StrUtil.split(tName, "\\|");
        String[] urlAry = StrUtil.split(tUrl, "\\|");
        String[] strOrderAry = StrUtil.split(tOrder, "\\|");

        tName = "";
        tUrl = "";
        tOrder = "";

        int len = nameAry.length;
        for (int i=0; i<len; i++) {
            if (nameAry[i].equals(tagName)) {
                nameAry[i] = newTagName;
                strOrderAry[i] = tagOrder;
                urlAry[i] = tagUrl;
            }
            if (tName.equals("")) {
                tName = nameAry[i];
                tUrl = urlAry[i];
                tOrder = strOrderAry[i];
            }
            else {
                tName += "|" + nameAry[i];
                tUrl += "|" + urlAry[i];
                tOrder += "|" + strOrderAry[i];
            }
        }

        // 根据fieldOrder排序
        double[] orderAry = new double[len];
        for (int i=0; i<len; i++) {
            orderAry[i] = StrUtil.toDouble(strOrderAry[i]);
        }

        double temp;
        int size = len;
        String tempStr;
        // 外层循环，控制“冒泡”的最终位置
        for(int i=size-1; i>=1; i--){
            boolean end = true;
            // 内层循环，用于相临元素的比较
            for(int j=0; j < i; j++) {
                if(orderAry[j] > orderAry[j+1]) {
                    temp = orderAry[j];
                    orderAry[j] = orderAry[j+1];
                    orderAry[j+1] = temp;
                    end = false;

                    tempStr = nameAry[j];
                    nameAry[j] = nameAry[j+1];
                    nameAry[j+1] = tempStr;
                    tempStr = urlAry[j];
                    urlAry[j] = urlAry[j+1];
                    urlAry[j+1] = tempStr;
                }
            }
            if(end == true) {
                break;
            }
        }

        tName = "";
        tUrl = "";
        tOrder = "";

        for (int i=0; i<len; i++) {
            if (i==0) {
                tName = nameAry[i];
                tUrl = urlAry[i];
                tOrder = ""+orderAry[i];
            }
            else {
                tName += "|" + nameAry[i];
                tUrl += "|" + urlAry[i];
                tOrder += "|" + orderAry[i];
            }
        }

        msd.set("sub_nav_tag_name", tName);
        msd.set("sub_nav_tag_url", tUrl);
        msd.set("sub_nav_tag_order", tOrder);

        boolean re = false;
        try {
            re = msd.save();
        } catch (ResKeyException e) {
            json.put("ret", "0");
            json.put("msg", e.getMessage(request));
            return json.toString();
        }
        if (re) {
            json.put("ret", "1");
            json.put("msg", "操作成功！");
        }
        else {
            json.put("ret", "0");
            json.put("msg", "操作失败！");
        }
        return json.toString();
    }

    @ResponseBody
    @RequestMapping(value = "/addRelateModule", method = RequestMethod.POST, produces = {"text/html;charset=UTF-8;", "application/json;charset=UTF-8;"})
    public String addRelateModule(String formCode) {
        com.alibaba.fastjson.JSONObject json = new com.alibaba.fastjson.JSONObject();
        double order = ParamUtil.getDouble(request, "order", -1);
        if (order == -1) {
            json.put("ret", "0");
            json.put("msg", "请填写顺序号");
            return json.toString();
        }
        String relateCode = ParamUtil.get(request, "relateCode");
        String field = ParamUtil.get(request, "field");
        int type = ParamUtil.getInt(request, "type", -1);
        int cwsStatus = ParamUtil.getInt(request, "cwsStatus", com.redmoon.oa.flow.FormDAO.STATUS_NOT);
        String conds = ParamUtil.get(request, "conds");

        ModuleRelateDb mrd = new ModuleRelateDb();
        mrd = mrd.getModuleRelateDb(formCode, relateCode);
        if (mrd != null) {
            json.put("ret", "0");
            json.put("msg", "模块已关联");
            return json.toString();
        }
        mrd = new ModuleRelateDb();
        boolean re = false;
        try {
            re = mrd.create(new JdbcTemplate(), new Object[]{formCode, relateCode, field, new Integer(type), new Double(order), new Integer(cwsStatus), conds});
        } catch (ResKeyException e) {
            json.put("ret", "0");
            json.put("msg", e.getMessage());
            return json.toString();
        }

        if (re) {
            json.put("ret", "1");
            json.put("msg", "操作成功！");
        }
        else {
            json.put("ret", "0");
            json.put("msg", "操作失败！");
        }
        return json.toString();
    }

    @ResponseBody
    @RequestMapping(value = "/delRelateModule", method = RequestMethod.POST, produces = {"text/html;charset=UTF-8;", "application/json;charset=UTF-8;"})
    public String delRelateModule(String formCode, String relateCode) {
        com.alibaba.fastjson.JSONObject json = new com.alibaba.fastjson.JSONObject();
        ModuleRelateDb mrd = new ModuleRelateDb();
        mrd = mrd.getModuleRelateDb(formCode, relateCode);
        boolean re = false;
        try {
            re = mrd.del();
        } catch (ResKeyException e) {
            json.put("ret", "0");
            json.put("msg", e.getMessage(request));
            return json.toString();
        }
        if (re) {
            json.put("ret", "1");
            json.put("msg", "操作成功！");
        }
        else {
            json.put("ret", "0");
            json.put("msg", "操作失败！");
        }
        return json.toString();
    }

    @ResponseBody
    @RequestMapping(value = "/modifyRelateModule", method = RequestMethod.POST, produces = {"text/html;charset=UTF-8;", "application/json;charset=UTF-8;"})
    public String modifyRelateModule(String formCode, String relateCode, Integer type, String order, String field, String conds) {
        com.alibaba.fastjson.JSONObject json = new com.alibaba.fastjson.JSONObject();
        int cwsStatus = ParamUtil.getInt(request, "cwsStatus", com.redmoon.oa.flow.FormDAO.STATUS_NOT);
        int isOnTab = ParamUtil.getInt(request, "is_on_tab", 0);

        ModuleRelateDb mrd = new ModuleRelateDb();
        mrd = mrd.getModuleRelateDb(formCode, relateCode);

        boolean re = false;
        try {
            re = mrd.save(new JdbcTemplate(), new Object[]{field, new Integer(type), new Double(order), new Integer(cwsStatus), isOnTab, conds, formCode, relateCode});
        } catch (SQLException e) {
            json.put("ret", "0");
            json.put("msg", e.getMessage());
            return json.toString();
        }
        if (re) {
            json.put("ret", "1");
            json.put("msg", "操作成功！");
        }
        else {
            json.put("ret", "0");
            json.put("msg", "操作失败！");
        }
        return json.toString();
    }

    @PreAuthorize("hasAnyAuthority('admin')")
    @ResponseBody
    @RequestMapping(value = "/putEditPageSetup", method = RequestMethod.POST, produces = {"text/html;charset=UTF-8;", "application/json;charset=UTF-8;"})
    public String putEditPageSetup(@RequestParam(required = true)String moduleCode) {
        com.alibaba.fastjson.JSONObject json = new com.alibaba.fastjson.JSONObject();
        boolean re = false;
        try {
            boolean isReloadAfterUpdate = ParamUtil.getBoolean(request, "isReloadAfterUpdate", true);
            boolean isTabStyleHor = ParamUtil.getBoolean(request, "isTabStyleHor", true);
            ModuleSetupDb msd = new ModuleSetupDb();
            msd = msd.getModuleSetupDb(moduleCode);
            String pageSetup = msd.getString("page_setup");
            if (StringUtils.isEmpty(pageSetup)) {
                pageSetup = "{}";
            }
            com.alibaba.fastjson.JSONObject jsonObject = com.alibaba.fastjson.JSONObject.parseObject(pageSetup);
            com.alibaba.fastjson.JSONObject editJson = new com.alibaba.fastjson.JSONObject();
            editJson.put("isReloadAfterUpdate", isReloadAfterUpdate);
            editJson.put("isTabStyleHor", isTabStyleHor);

            String strBtnProps = ParamUtil.get(request, "btnProps");
            com.alibaba.fastjson.JSONArray btnProps = JSONArray.parseArray(strBtnProps);
            editJson.put("btnProps", btnProps);

            jsonObject.put("editPage", editJson);
            msd.set("page_setup", jsonObject.toString());
            re = msd.save();
        } catch (ResKeyException e) {
            json.put("ret", "0");
            json.put("msg", e.getMessage());
            return json.toString();
        }
        if (re) {
            json.put("ret", 1);
            json.put("msg", "操作成功！");
        } else {
            json.put("ret", 0);
            json.put("msg", "操作失败");
        }

        return json.toString();
    }

    @PreAuthorize("hasAnyAuthority('admin')")
    @ResponseBody
    @RequestMapping(value = "/putShowPageSetup", method = RequestMethod.POST, produces = {"text/html;charset=UTF-8;", "application/json;charset=UTF-8;"})
    public String putShowPageSetup(@RequestParam(required = true)String moduleCode) {
        com.alibaba.fastjson.JSONObject json = new com.alibaba.fastjson.JSONObject();
        boolean re = false;
        try {
            boolean isTabStyleHor = ParamUtil.getBoolean(request, "isTabStyleHor", true);
            ModuleSetupDb msd = new ModuleSetupDb();
            msd = msd.getModuleSetupDb(moduleCode);
            String pageSetup = msd.getString("page_setup");
            if (StringUtils.isEmpty(pageSetup)) {
                pageSetup = "{}";
            }
            com.alibaba.fastjson.JSONObject jsonObject = com.alibaba.fastjson.JSONObject.parseObject(pageSetup);
            com.alibaba.fastjson.JSONObject showJson = new com.alibaba.fastjson.JSONObject();
            showJson.put("isTabStyleHor", isTabStyleHor);

            String strBtnProps = ParamUtil.get(request, "btnProps");
            com.alibaba.fastjson.JSONArray btnProps = JSONArray.parseArray(strBtnProps);
            showJson.put("btnProps", btnProps);

            jsonObject.put("showPage", showJson);
            msd.set("page_setup", jsonObject.toString());
            re = msd.save();
        } catch (ResKeyException e) {
            json.put("ret", "0");
            json.put("msg", e.getMessage());
            return json.toString();
        }
        if (re) {
            json.put("ret", 1);
            json.put("msg", "操作成功！");
        } else {
            json.put("ret", 0);
            json.put("msg", "操作失败");
        }

        return json.toString();
    }

    @PreAuthorize("hasAnyAuthority('admin')")
    @ResponseBody
    @RequestMapping(value = "/putCommonPageSetup", method = RequestMethod.POST, produces = {"text/html;charset=UTF-8;", "application/json;charset=UTF-8;"})
    public String putCommonPageSetup(@RequestParam(required = true)String moduleCode) {
        com.alibaba.fastjson.JSONObject json = new com.alibaba.fastjson.JSONObject();
        boolean re = false;
        try {
            int pageStyle = ParamUtil.getInt(request, "pageStyle", ConstUtil.PAGE_STYLE_DEFAULT);

            ModuleSetupDb msd = new ModuleSetupDb();
            msd = msd.getModuleSetupDb(moduleCode);
            String pageSetup = msd.getString("page_setup");
            if (StringUtils.isEmpty(pageSetup)) {
                pageSetup = "{}";
            }
            com.alibaba.fastjson.JSONObject jsonObject = com.alibaba.fastjson.JSONObject.parseObject(pageSetup);
            com.alibaba.fastjson.JSONObject editJson = new com.alibaba.fastjson.JSONObject();
            editJson.put("pageStyle", pageStyle);
            jsonObject.put("commonPage", editJson);
            msd.set("page_setup", jsonObject.toString());
            re = msd.save();
        } catch (ResKeyException e) {
            json.put("ret", "0");
            json.put("msg", e.getMessage());
            return json.toString();
        }
        if (re) {
            json.put("ret", 1);
            json.put("msg", "操作成功！");
        } else {
            json.put("ret", 0);
            json.put("msg", "操作失败");
        }

        return json.toString();
    }

    /**
     * 添加副模块
     * @return
     */
    @ResponseBody
    @RequestMapping(value = "/createSubModule", method = RequestMethod.POST, produces = {"text/html;charset=UTF-8;", "application/json;charset=UTF-8;"})
    public String createSubModule(@RequestParam(required = true)String formCode) {
        com.alibaba.fastjson.JSONObject json = new com.alibaba.fastjson.JSONObject();
        String code = RandomSecquenceCreator.getId(20);
        boolean re = false;
        try {
            String name = ParamUtil.get(request, "name");
            // list_field从主模块复制
            ModuleSetupDb vsd = new ModuleSetupDb();
            vsd = vsd.getModuleSetupDb(formCode);
            String list_field = vsd.getString("list_field");
            String list_field_width = vsd.getString("list_field_width");
            String list_field_order = vsd.getString("list_field_order");
            String query_field = vsd.getString("query_field");
            re = vsd.create(new JdbcTemplate(), new Object[]{code, list_field, query_field, name, new Integer(ModuleSetupDb.KIND_SUB), formCode, list_field_width, list_field_order, "", "", "", "", ModuleSetupDb.VIEW_DEFAULT, "", "", "", "", "", "", "", ""});
        } catch (ResKeyException e) {
            json.put("ret", "0");
            json.put("msg", e.getMessage(request));
            return json.toString();
        }
        if (re) {
            json.put("ret", 1);
            json.put("msg", "操作成功！");
            json.put("code", code);
        } else {
            json.put("ret", 0);
            json.put("msg", "操作失败");
        }
        return json.toString();
    }

    /**
     * 添加副模块
     * @return
     */
    @ResponseBody
    @RequestMapping(value = "/delModule", method = RequestMethod.POST, produces = {"text/html;charset=UTF-8;", "application/json;charset=UTF-8;"})
    public String delModule(@RequestParam(required = true)String code) {
        com.alibaba.fastjson.JSONObject json = new com.alibaba.fastjson.JSONObject();
        boolean re = false;
        try {
            ModuleSetupDb vsd = new ModuleSetupDb();
            vsd = vsd.getModuleSetupDbOrInit(code);
            re = vsd.del();
        } catch (ResKeyException e) {
            json.put("ret", "0");
            json.put("msg", e.getMessage(request));
            return json.toString();
        }
        if (re) {
            json.put("ret", 1);
            json.put("msg", "操作成功！");
        } else {
            json.put("ret", 0);
            json.put("msg", "操作失败");
        }
        return json.toString();
    }

    /**
     * 添加副模块
     * @return
     */
    @ResponseBody
    @RequestMapping(value = "/cloneModule", method = RequestMethod.POST, produces = {"text/html;charset=UTF-8;", "application/json;charset=UTF-8;"})
    public String cloneModule(@RequestParam(required = true)String formCode, @RequestParam(required = true)String code) {
        com.alibaba.fastjson.JSONObject json = new com.alibaba.fastjson.JSONObject();
        String newCode = RandomSecquenceCreator.getId(20);
        boolean re = false;
        try {
            String newName = ParamUtil.get(request, "name");
            // list_field从模块复制
            ModuleSetupDb vsd = new ModuleSetupDb();
            vsd = vsd.getModuleSetupDb(code);
            String list_field = vsd.getString("list_field");
            String query_field = vsd.getString("query_field");
            String name = newName;
            Integer kind = new Integer(ModuleSetupDb.KIND_SUB);
            String form_code = vsd.getString("form_code");

            String list_field_width = vsd.getString("list_field_width");
            String list_field_order = vsd.getString("list_field_order");
            String list_field_link = vsd.getString("list_field_link");
            String msg_prop = vsd.getString("msg_prop");
            String validate_prop = vsd.getString("validate_prop");
            String validate_msg = vsd.getString("validate_msg");
            String view_list = vsd.getString("view_list");
            String field_begin_date = vsd.getString("field_begin_date");
            String field_end_date = vsd.getString("field_end_date");
            String field_name = vsd.getString("field_name");
            String field_desc = vsd.getString("field_desc");
            String field_label = vsd.getString("field_label");
            String scale_default = vsd.getString("scale_default");
            String scale_min = vsd.getString("scale_min");
            String scale_max = vsd.getString("scale_max");
            ModuleSetupDb msd = new ModuleSetupDb();
            re = msd.create(new JdbcTemplate(), new Object[]{newCode, list_field, query_field, name, kind, formCode, list_field_width, list_field_order, list_field_link,
                    msg_prop, validate_prop, validate_msg, view_list, field_begin_date, field_end_date, field_name, field_desc, field_label, scale_default, scale_min, scale_max});

            msd = msd.getModuleSetupDb(newCode);
            String str = msd.getTable().getQuerySave();
            str = str.substring(str.indexOf(" set ") + 5);
            str = str.substring(0, str.indexOf(" where "));
            String[] ary = str.split(",");
            for (int i = 0; i < ary.length; i++) {
                String[] subAry = ary[i].split("=");
                String fieldName = subAry[0].trim();
                if (fieldName.equals("name")) {
                    continue;
                }
                if (fieldName.equals("description")) {
                    msd.set(fieldName, newName);
                    continue;
                }
                msd.set(fieldName, vsd.get(fieldName));
            }
            re = msd.save();
        } catch (ResKeyException e) {
            json.put("ret", "0");
            json.put("msg", e.getMessage(request));
            return json.toString();
        }
        if (re) {
            json.put("ret", 1);
            json.put("msg", "操作成功！");
            json.put("code", newCode);
        } else {
            json.put("ret", 0);
            json.put("msg", "操作失败");
        }
        return json.toString();
    }

    /**
     * 添加副模块
     * @return
     */
    @ResponseBody
    @RequestMapping(value = "/syncModule", method = RequestMethod.POST, produces = {"text/html;charset=UTF-8;", "application/json;charset=UTF-8;"})
    public String syncModule(@RequestParam(required = true) String moduleCode) {
        com.alibaba.fastjson.JSONObject json = new com.alibaba.fastjson.JSONObject();
        boolean re = false;
        try {
            String byModuleCode = ParamUtil.get(request, "byModuleCode");
            ModuleSetupDb msd = new ModuleSetupDb();
            msd = msd.getModuleSetupDbOrInit(moduleCode);
            ModuleSetupDb byMsd = new ModuleSetupDb();
            byMsd = byMsd.getModuleSetupDb(byModuleCode);

            int cols = ParamUtil.getInt(request, "cols", 0);
            int query = ParamUtil.getInt(request, "query", 0);
            if (cols == 1) {
                msd.set("list_field", byMsd.get("list_field"));
                msd.set("list_field_width", byMsd.getString("list_field_width"));
                msd.set("list_field_order", byMsd.getString("list_field_order"));
                msd.set("list_field_link", byMsd.getString("list_field_link"));
                msd.set("list_field_show", byMsd.getString("list_field_show"));
                msd.set("list_field_title", byMsd.getString("list_field_title"));
            }
            if (query == 1) {
                msd.set("btn_name", byMsd.getString("btn_name"));
                msd.set("btn_order", byMsd.getString("btn_order"));
                msd.set("btn_script", byMsd.getString("btn_script"));
                msd.set("btn_bclass", byMsd.getString("btn_bclass"));
                msd.set("btn_role", byMsd.getString("btn_role"));
            }
            re = msd.save();
        } catch (ResKeyException e) {
            json.put("ret", "0");
            json.put("msg", e.getMessage(request));
            return json.toString();
        }
        if (re) {
            json.put("ret", 1);
            json.put("msg", "操作成功！");
        } else {
            json.put("ret", 0);
            json.put("msg", "操作失败");
        }
        return json.toString();
    }

    /**
     * 保存事件脚本
     * @return
     */
    @ResponseBody
    @RequestMapping(value = "/updateScript", method = RequestMethod.POST, produces = {"text/html;charset=UTF-8;", "application/json;charset=UTF-8;"})
    public String updateScript(@RequestParam(required = true) String code, @RequestParam(required = true) String eventType) {
        com.alibaba.fastjson.JSONObject json = new com.alibaba.fastjson.JSONObject();
        boolean re = false;
        try {
            ModuleSetupDb msd = new ModuleSetupDb();
            msd = msd.getModuleSetupDb(code);
            String formCode = msd.getString("form_code");

            String script = ParamUtil.get(request, "script");
            if (eventType.equals("form_js")) {
                FileUtil.WriteFile(Global.getRealPath() + "/flow/form_js/form_js_" + formCode + ".jsp", script, "utf-8");
                re = true;
            }
            else {
                re = msd.saveScript(eventType, script);
            }
        } catch (ErrMsgException | ResKeyException e) {
            json.put("ret", "0");
            json.put("msg", e.getMessage());
            return json.toString();
        }
        if (re) {
            json.put("ret", 1);
            json.put("msg", "操作成功！");
        } else {
            json.put("ret", 0);
            json.put("msg", "操作失败");
        }
        return json.toString();
    }

    /**
     * 保存页面样式
     * @return
     */
    @ResponseBody
    @RequestMapping(value = "/updateCss", method = RequestMethod.POST, produces = {"text/html;charset=UTF-8;", "application/json;charset=UTF-8;"})
    public String updateCss(@RequestParam(required = true) String code, @RequestParam(required = true) String pageType) {
        com.alibaba.fastjson.JSONObject json = new com.alibaba.fastjson.JSONObject();
        boolean re = false;
        try {
            ModuleSetupDb msd = new ModuleSetupDb();
            msd = msd.getModuleSetupDb(code);
            String css = ParamUtil.get(request, "css");
            re = msd.saveCss(pageType, css);
        } catch (ErrMsgException | ResKeyException e) {
            json.put("ret", "0");
            json.put("msg", e.getMessage());
            return json.toString();
        }
        if (re) {
            json.put("ret", 1);
            json.put("msg", "操作成功！");
        } else {
            json.put("ret", 0);
            json.put("msg", "操作失败");
        }
        return json.toString();
    }
}
