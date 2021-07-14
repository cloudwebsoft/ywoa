package com.cloudweb.oa.controller;

import bsh.commands.dir;
import cn.js.fan.db.Conn;
import cn.js.fan.db.ResultIterator;
import cn.js.fan.db.ResultRecord;
import cn.js.fan.util.ErrMsgException;
import cn.js.fan.util.ParamUtil;
import cn.js.fan.util.StrUtil;
import cn.js.fan.web.Global;
import cn.js.fan.web.SkinUtil;
import com.alibaba.fastjson.JSONObject;
import com.cloudweb.oa.service.BasicDataService;
import com.cloudweb.oa.service.DataDictService;
import com.cloudweb.oa.utils.ConstUtil;
import com.cloudwebsoft.framework.db.Connection;
import com.cloudwebsoft.framework.db.JdbcTemplate;
import com.redmoon.oa.basic.SelectDb;
import com.redmoon.oa.basic.TreeSelectCache;
import com.redmoon.oa.basic.TreeSelectDb;
import com.redmoon.oa.basic.TreeSelectMgr;
import com.redmoon.oa.flow.FormDb;
import com.redmoon.oa.hr.SalaryMgr;
import com.redmoon.oa.pvg.Privilege;
import com.redmoon.oa.sys.DebugUtil;
import com.redmoon.oa.util.TransmitData;
import com.redmoon.oa.visual.FormDAO;
import com.redmoon.oa.visual.ModuleSetupDb;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import java.io.UnsupportedEncodingException;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

@Controller
@RequestMapping("/basicdata")
public class BasicDataController {

    @Autowired
    HttpServletRequest request;

    @Autowired
    BasicDataService basicDataService;

    @ResponseBody
    @RequestMapping(value = "/getTreeNodeUrl", method = RequestMethod.POST, produces = {"application/json;charset=UTF-8;"})
    public String getTreeNodeUrl(String basicCode, String nodeCode) {
        JSONObject json = new JSONObject();
        boolean re = false;
        String msg = "";
        TreeSelectDb leaf = new TreeSelectDb();
        leaf = leaf.getTreeSelectDb(nodeCode);

        // String formCode = leaf.getFormCode();
        int layer = leaf.getLayer();
        FormDAO fdao = null;
        try {
            fdao = basicDataService.getNodeDescByLayer(basicCode, layer);
            re = true;
        } catch (ErrMsgException e) {
            json.put("ret", "0");
            json.put("msg", e.getMessage());
            return json.toString();
        }

        if (re) {
            json.put("ret", "1");
            json.put("msg", "操作成功！");
            json.put("url", getLink(fdao, nodeCode));
        } else {
            json.put("ret", "0");
            json.put("msg", msg);
        }

        return json.toString();
    }

    /**
     * 取得链接
     *
     * @return
     */
    public String getLink(FormDAO fdao, String nodeCode) {
        int nodeType = StrUtil.toInt(fdao.getFieldValue("node_type"), ConstUtil.BASIC_TREE_NODE_TYPE_LINK);
        if (nodeType == ConstUtil.BASIC_TREE_NODE_TYPE_LINK) {
            String link = fdao.getFieldValue("link");
            if (link.indexOf("$") != -1) {
                Privilege pvg = new Privilege();
                try {
                    link = link.replaceFirst("\\$userName", java.net.URLEncoder.encode(pvg.getUser(request), "GBK"));
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                }
            }
            // 链接需替换路径变量$u
            link = link.replaceFirst("\\$u", request.getContextPath());
            if (!link.startsWith("http")) {
                link = request.getContextPath() + "/" + link;
            }
            return link;
        } else {
            String moduleField = fdao.getFieldValue("module_field");
            String pageType = fdao.getFieldValue("page_type");
            String moduleCode = fdao.getFieldValue("module_code");
            if (ConstUtil.BASIC_TREE_NODE_PAGE_TYPE_LIST.equals(pageType)) {
                return request.getContextPath() + "/visual/module_list.jsp?code=" + StrUtil.UrlEncode(moduleCode) + "&" + moduleField + "=" + nodeCode;
            } else {
                ModuleSetupDb msd = new ModuleSetupDb();
                msd = msd.getModuleSetupDb(moduleCode);
                String formCode = msd.getString("form_code");
                // 检查记录是否存在，如果不存在，则创建
                FormDb fd = new FormDb();
                fd = fd.getFormDb(formCode);
                FormDAO fdaoModule = new FormDAO(fd);
                List<FormDAO> list = basicDataService.listByNode(formCode, moduleField, nodeCode);
                if (list.size() == 0) {
                    fdaoModule.setFieldValue(moduleField, nodeCode);
                    fdaoModule.setCreator(ConstUtil.USER_SYSTEM);
                    fdaoModule.setUnitCode(ConstUtil.DEPT_ROOT);
                    // fdaoModule.setCwsId();
                    try {
                        fdaoModule.create();
                    } catch (SQLException throwables) {
                        throwables.printStackTrace();
                    }
                } else {
                    fdaoModule = list.get(0);
                }

                if (ConstUtil.BASIC_TREE_NODE_PAGE_TYPE_EDIT.equals(pageType)) {
                    return request.getContextPath() + "/visual/module_edit.jsp?parentId=" + fdaoModule.getId() + "&id=" + fdaoModule.getId() + "&isShowNav=1&moduleCode=" + moduleCode + "&" + moduleField + "=" + nodeCode;
                } else {
                    return request.getContextPath() + "/visual/module_show.jsp?parentId=" + fdaoModule.getId() + "&id=" + fdaoModule.getId() + "&isShowNav=1&moduleCode=" + moduleCode + "&" + moduleField + "=" + nodeCode;
                }
            }
        }
    }

    @ResponseBody
    @RequestMapping(value = "/getNewNodeCode", method = RequestMethod.POST, produces = {"text/html;charset=UTF-8;", "application/json;charset=UTF-8;"})
    public String getNewNodeCode() {
        JSONObject json = new JSONObject();

        String root_code = ParamUtil.get(request, "root_code");
        if ("".equals(root_code)) {
            root_code = "root";
        }
        String parent_code = ParamUtil.get(request, "parent_code").trim();

        String newNodeCode = basicDataService.getNewNodeCode(root_code, parent_code);
        json.put("ret", 1);
        json.put("msg", newNodeCode);

        return json.toString();
    }

    @ResponseBody
    @RequestMapping(value = "/getParentNodeName", method = RequestMethod.POST, produces = {"text/html;charset=UTF-8;", "application/json;charset=UTF-8;"})
    public String getParentNodeName() {
        JSONObject json = new JSONObject();

        String parentCode = ParamUtil.get(request, "parentCode");
        TreeSelectDb leaf = new TreeSelectDb();
        leaf = leaf.getTreeSelectDb(parentCode);
        json.put("ret", "1");
        json.put("msg", leaf.getName());

        String code = ParamUtil.get(request, "code");
        leaf = leaf.getTreeSelectDb(code);
        json.put("preCode", leaf.getPreCode());
        json.put("link", leaf.getLink());
        json.put("formCode", leaf.getFormCode());
        json.put("isOpen", leaf.isOpen());
        json.put("isContextMenu", leaf.isContextMenu());

        return json.toString();
    }

    @ResponseBody
    @RequestMapping(value = "/createNode", method = RequestMethod.POST, produces = {"text/html;charset=UTF-8;", "application/json;charset=UTF-8;"})
    public String createNode() {
        JSONObject json = new JSONObject();
        String parent_code = ParamUtil.get(request, "parent_code").trim();
        String newName = ParamUtil.get(request, "name").trim();
        boolean re = false;
        try {
            TreeSelectMgr dir = new TreeSelectMgr();
            // 判断名称是否有重复
            Vector children = dir.getChildren(parent_code);
            Iterator ri = children.iterator();
            while (ri.hasNext()) {
                TreeSelectDb childlf = (TreeSelectDb) ri.next();
                String name = childlf.getName();
                if (name.equals(newName)) {
                    json.put("ret", 2);
                    json.put("msg", "请检查名称是否有重复！");
                    return json.toString();
                }
            }
            //增加节点
            re = dir.AddChild(request);
            if (!re) {
                json.put("ret", 2);
                json.put("msg", SkinUtil.LoadString(request, "res.label.cms.dir", "add_msg"));
                return json.toString();
            }
            json.put("ret", 1);
            json.put("code", dir.getCode());
        } catch (ErrMsgException e) {
            json.put("ret", 2);
            json.put("msg", e.getMessage());
        }
        return json.toString();
    }

    @ResponseBody
    @RequestMapping(value = "/delNode", method = RequestMethod.POST, produces = {"text/html;charset=UTF-8;", "application/json;charset=UTF-8;"})
    public String delNode(String code) {
        JSONObject json = new JSONObject();
        boolean re = false;
        try {
            TreeSelectMgr dir = new TreeSelectMgr();
            TreeSelectDb delleaf = dir.getTreeSelectDb(code);
            if (delleaf != null) {
                dir.del(code);

                // 根据basic_tree_node树形结构节点描述，删除相关的记录
                FormDAO fdao = basicDataService.getNodeDescByLayer(delleaf.getRootCode(), delleaf.getLayer());

                String moduleField = fdao.getFieldValue("module_field");
                // String pageType = fdao.getFieldValue("page_type");
                String moduleCode = fdao.getFieldValue("module_code");
                // if (!ConstUtil.BASIC_TREE_NODE_PAGE_TYPE_LIST.equals(pageType)) {
                    ModuleSetupDb msd = new ModuleSetupDb();
                    msd = msd.getModuleSetupDb(moduleCode);
                    String formCode = msd.getString("form_code");
                    // 检查记录是否存在，如果存在，则删除
                    List<FormDAO> list = basicDataService.listByNode(formCode, moduleField, code);
                    for (FormDAO fdaoModule : list) {
                        fdaoModule.del();
                    }
                // }

                json.put("ret", "1");
                json.put("msg", "删除成功！");
            } else {
                json.put("ret", "2");
                json.put("msg", "节点不存在！");
            }
        } catch (ErrMsgException e) {
            json.put("ret", 2);
            json.put("msg", e.getMessage());
        }
        return json.toString();
    }


    @ResponseBody
    @RequestMapping(value = "/rename", method = RequestMethod.POST, produces = {"text/html;charset=UTF-8;", "application/json;charset=UTF-8;"})
    public String rename() {
        JSONObject json = new JSONObject();
        boolean re = false;
        try {
            TreeSelectMgr dir = new TreeSelectMgr();
            String code = ParamUtil.get(request, "code");
            String newName = ParamUtil.get(request, "newName").trim();
            // 判断名称是否有重复
            TreeSelectDb leaf = new TreeSelectDb();
            leaf = leaf.getTreeSelectDb(code);
            String parent_code = leaf.getParentCode();
            Vector children = dir.getChildren(parent_code);
            Iterator ri = children.iterator();
            while (ri.hasNext()) {
                TreeSelectDb childlf = (TreeSelectDb) ri.next();
                if (code.equals(childlf.getCode())) {
                    continue;
                }
                String name = childlf.getName();
                if (name.equals(newName)) {
                    json.put("ret", 0);
                    json.put("msg", "请检查名称是否有重复！");
                    return json.toString();
                }
            }

            if (!leaf.getName().equals(newName)) {
                leaf.setName(newName);
                re = leaf.save();
            }
            else {
                json.put("ret", 2);
                return json.toString();
            }

            if (re) {
                json.put("ret", 1);
                json.put("msg", "操作成功！");
            }
        } catch (ErrMsgException e) {
            json.put("ret", 2);
            json.put("msg", e.getMessage());
        }
        return json.toString();
    }

    @ResponseBody
    @RequestMapping(value = "/updateNode", method = RequestMethod.POST, produces = {"text/html;charset=UTF-8;", "application/json;charset=UTF-8;"})
    public String updateNode() {
        JSONObject json = new JSONObject();
        boolean re = false;
        try {
            TreeSelectMgr dir = new TreeSelectMgr();
            String code = ParamUtil.get(request, "code");
            String newName = ParamUtil.get(request, "name").trim();
            //判断名称是否有重复
            TreeSelectDb dept = new TreeSelectDb();
            dept = dept.getTreeSelectDb(code);
            String parent_code = dept.getParentCode();
            Vector children = dir.getChildren(parent_code);
            Iterator ri = children.iterator();
            while (ri.hasNext()) {
                TreeSelectDb childlf = (TreeSelectDb) ri.next();
                if (code.equals(childlf.getCode())) {
                    continue;
                }
                String name = childlf.getName();
                if (name.equals(newName)) {
                    json.put("ret", 2);
                    json.put("msg", "请检查名称是否有重复！");
                }
            }
            //修改节点
            re = dir.update(request);
            if (re) {
                json.put("ret", 1);
                json.put("msg", "操作成功！");
            }
            else {
                json.put("ret", 0);
                json.put("msg", "操作失败！");
            }
        } catch (ErrMsgException e) {
            json.put("ret", 2);
            json.put("msg", e.getMessage());
        }
        return json.toString();
    }

    @ResponseBody
    @RequestMapping(value = "/openNode", method = RequestMethod.POST, produces = {"text/html;charset=UTF-8;", "application/json;charset=UTF-8;"})
    public String openNode(String code) {
        JSONObject json = new JSONObject();
        boolean re = false;
        TreeSelectDb leaf = new TreeSelectDb();
        leaf = leaf.getTreeSelectDb(code);

        leaf.setOpen(true);
        re = leaf.save();
        if (re) {
            json.put("ret", 1);
            json.put("msg", "启用成功！");
        }
        else {
            json.put("ret", 0);
            json.put("msg", "操作失败！");
        }
        return json.toString();
    }

    @ResponseBody
    @RequestMapping(value = "/closeNode", method = RequestMethod.POST, produces = {"text/html;charset=UTF-8;", "application/json;charset=UTF-8;"})
    public String closeNode(String code) {
        JSONObject json = new JSONObject();
        boolean re = false;
        TreeSelectDb leaf = new TreeSelectDb();
        leaf = leaf.getTreeSelectDb(code);

        leaf.setOpen(false);
        re = leaf.save();
        if (re) {
            json.put("ret", 1);
            json.put("msg", "停用成功！");
        }
        else {
            json.put("ret", 0);
            json.put("msg", "操作失败！");
        }
        return json.toString();
    }

    @ResponseBody
    @RequestMapping(value = "/moveNode", method = RequestMethod.POST, produces = {"text/html;charset=UTF-8;", "application/json;charset=UTF-8;"})
    public String moveNode() {
        JSONObject json = new JSONObject();
        String code = ParamUtil.get(request, "code");
        String parent_code = ParamUtil.get(request, "parent_code");
        int position = Integer.parseInt(ParamUtil.get(request, "position"));
        if ("office_equipment".equals(code)) {
            json.put("ret", "0");
            json.put("msg", "根节点不能移动！");
            return json.toString();
        }
        if ("#".equals(parent_code)) {
            json.put("ret", "0");
            json.put("msg", "不能与根节点平级！");
            return json.toString();
        }

        TreeSelectMgr dir = new TreeSelectMgr();
        TreeSelectDb moveleaf = dir.getTreeSelectDb(code);
        String old_parent_code = moveleaf.getParentCode();
        TreeSelectDb newParentLeaf = dir.getTreeSelectDb(parent_code);
        int p = position + 1;
        // 异级目录
        if (!newParentLeaf.getCode().equals(old_parent_code)) {
            moveleaf.save(parent_code, p);
        } else {
            // 新节点
            Iterator ir = newParentLeaf.getChildren().iterator();

            moveleaf.setOrders(p);
            moveleaf.setParentCode(parent_code);
            moveleaf.save();
            while (ir.hasNext()) {
                TreeSelectDb lf = (TreeSelectDb) ir.next();
                // 跳过自己
                if (lf.getCode().equals(code)) {
                    continue;
                }

                if (lf.getOrders() >= p) {
                    lf.setOrders(lf.getOrders() + 1);
                    lf.save();
                }
            }

            // 原节点下的孩子节点通过修复repairTree处理
            TreeSelectDb rootDeptDb = dir
                    .getTreeSelectDb("office_equipment");
            TreeSelectMgr dm = new TreeSelectMgr();
            try {
                dm.repairTree(rootDeptDb);
            } catch (Exception e) {
                e.printStackTrace();
                json.put("ret", "0");
                json.put("msg", e.getMessage());
            }

            TreeSelectCache dcm = new TreeSelectCache();
            dcm.removeAllFromCache();
        }

        json.put("ret", "1");
        json.put("msg", "移动成功！");
        return json.toString();
    }
}
