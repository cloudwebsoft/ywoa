package com.cloudweb.oa.controller;

import cn.js.fan.util.ErrMsgException;
import cn.js.fan.util.ParamUtil;
import cn.js.fan.util.ResKeyException;
import com.redmoon.oa.flow.FormDb;
import com.redmoon.oa.flow.FormParser;
import com.redmoon.oa.visual.ModuleSetupDb;
import com.redmoon.oa.visual.ModuleViewMgr;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;

@Controller
@RequestMapping("/visual")
public class ModuleSetupController {
    @Autowired
    private HttpServletRequest request;

    @ResponseBody
    @RequestMapping(value = "/colAdd", method = RequestMethod.POST, produces = {"text/html;charset=UTF-8;", "application/json;"})
    public String colAdd() {
        String code = ParamUtil.get(request, "code");
        ModuleViewMgr mvm = new ModuleViewMgr();
        JSONObject json = new JSONObject();
        boolean re = false;
        try {
            try {
                re = mvm.add(request, code);
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
        ModuleViewMgr mvm = new ModuleViewMgr();
        JSONObject json = new JSONObject();
        boolean re = false;
        try {
            try {
                re = mvm.modify(request, code);
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
        ModuleViewMgr mvm = new ModuleViewMgr();
        JSONObject json = new JSONObject();
        boolean re = false;
        try {
            try {
                re = mvm.del(request, code);
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
        ModuleViewMgr mvm = new ModuleViewMgr();
        JSONObject json = new JSONObject();
        boolean re = false;
        try {
            try {
                re = mvm.modifyLink(request, code);
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
        ModuleViewMgr mvm = new ModuleViewMgr();
        JSONObject json = new JSONObject();
        boolean re = false;
        try {
            try {
                re = mvm.delLink(request, code);
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
        ModuleViewMgr mvm = new ModuleViewMgr();
        JSONObject json = new JSONObject();
        boolean re = false;
        try {
            try {
                ModuleSetupDb msd = new ModuleSetupDb();
                msd = msd.getModuleSetupDb(code);
                JSONObject result = new JSONObject(strResult);
                re = mvm.saveLink(msd, result);
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
        ModuleViewMgr mvm = new ModuleViewMgr();
        JSONObject json = new JSONObject();
        boolean re = false;
        try {
            try {
                ModuleSetupDb msd = new ModuleSetupDb();
                msd = msd.getModuleSetupDb(code);
                JSONObject result = new JSONObject(strResult);
                re = mvm.saveCol(msd, result);
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
        ModuleViewMgr mvm = new ModuleViewMgr();
        JSONObject json = new JSONObject();
        boolean re = false;
        try {
            try {
                re = mvm.addLink(request, code);
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
        ModuleViewMgr mvm = new ModuleViewMgr();
        JSONObject json = new JSONObject();
        boolean re = false;
        try {
            try {
                re = mvm.addCond(request, code);
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
        ModuleViewMgr mvm = new ModuleViewMgr();
        JSONObject json = new JSONObject();
        boolean re = false;
        try {
            try {
                re = mvm.modifyBtn(request, code);
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
        ModuleViewMgr mvm = new ModuleViewMgr();
        JSONObject json = new JSONObject();
        boolean re = false;
        try {
            try {
                String op = ParamUtil.get(request, "op");
                if (op.equals("addBtnBatch")) {
                    re = mvm.addBtnBatch(request, code);
                }
                else {
                    re = mvm.addBtn(request, code);
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
        ModuleViewMgr mvm = new ModuleViewMgr();
        JSONObject json = new JSONObject();
        boolean re = false;
        try {
            try {
                re = mvm.modifyBtn(request, code);
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
        ModuleViewMgr mvm = new ModuleViewMgr();
        JSONObject json = new JSONObject();
        boolean re = false;
        try {
            try {
                re = mvm.delBtn(request, code);
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
        ModuleViewMgr mvm = new ModuleViewMgr();
        JSONObject json = new JSONObject();
        boolean re = false;
        try {
            try {
                ModuleSetupDb msd = new ModuleSetupDb();
                msd = msd.getModuleSetupDb(code);
                JSONObject result = new JSONObject(strResult);
                re = mvm.saveBtn(msd, result);
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
        ModuleViewMgr mvm = new ModuleViewMgr();
        JSONObject json = new JSONObject();
        boolean re = false;
        try {
            try {
                re = mvm.delBtn(request, code);
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
}

