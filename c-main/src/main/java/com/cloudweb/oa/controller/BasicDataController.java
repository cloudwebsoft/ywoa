package com.cloudweb.oa.controller;

import bsh.commands.dir;
import cn.js.fan.db.Conn;
import cn.js.fan.db.ListResult;
import cn.js.fan.db.ResultIterator;
import cn.js.fan.db.ResultRecord;
import cn.js.fan.util.ErrMsgException;
import cn.js.fan.util.ParamUtil;
import cn.js.fan.util.ResKeyException;
import cn.js.fan.util.StrUtil;
import cn.js.fan.web.SkinUtil;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.cloudweb.oa.entity.Menu;
import com.cloudweb.oa.service.BasicDataService;
import com.cloudweb.oa.utils.ConstUtil;
import com.cloudweb.oa.utils.ResponseUtil;
import com.cloudweb.oa.vo.BasicSelectVO;
import com.cloudweb.oa.vo.Result;
import com.cloudwebsoft.framework.util.LogUtil;
import com.redmoon.oa.basic.*;
import com.redmoon.oa.flow.FormDb;
import com.redmoon.oa.flow.Leaf;
import com.redmoon.oa.pvg.Privilege;
import com.redmoon.oa.security.SecurityUtil;
import com.redmoon.oa.visual.FormDAO;
import com.redmoon.oa.visual.ModuleSetupDb;
import io.swagger.annotations.*;
import org.dozer.DozerBeanMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import java.io.UnsupportedEncodingException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;

@Api(tags = "基础数据")
@Controller
@RequestMapping("/basicdata")
public class BasicDataController {

    @Autowired
    HttpServletRequest request;

    @Autowired
    BasicDataService basicDataService;

    @Autowired
    ResponseUtil responseUtil;

    @Autowired
    DozerBeanMapper dozerBeanMapper;

    @ResponseBody
    @RequestMapping(value = "/getTreeNodeUrl", method = RequestMethod.POST, produces = {"application/json;charset=UTF-8;"})
    public String getTreeNodeUrl(String basicCode, String nodeCode) {
        JSONObject json = new JSONObject();
        TreeSelectDb leaf = new TreeSelectDb();
        leaf = leaf.getTreeSelectDb(nodeCode);
        int layer = leaf.getLayer();
        FormDAO fdao = basicDataService.getNodeDescByLayer(basicCode, layer);
        if (fdao == null) {
            json.put("ret", "0");
            json.put("msg", "请设置节点描述");
            return json.toString();
        }

        json.put("ret", "1");
        json.put("msg", "操作成功！");
        json.put("url", getLink(fdao, nodeCode));
        return json.toString();
    }

    @ApiOperation(value = "获取基础数据大类", notes = "获取基础数据大类", httpMethod = "POST")
    @ApiResponses({ @ApiResponse(code = 200, message = "操作成功") })
    @ResponseBody
    @RequestMapping(value = "/listKind", method = {RequestMethod.POST, RequestMethod.GET}, produces = {"text/html;charset=UTF-8;", "application/json;charset=UTF-8;"})
    public Result<List<SelectKindDb>> listKind(String what) {
        SelectKindDb selectKindDb = new SelectKindDb();
        Vector<SelectKindDb> v;
        if (StrUtil.isEmpty(what)) {
            v = selectKindDb.list();
        }
        else {
            v = selectKindDb.list(selectKindDb.getListSql(what));
        }
        return new Result<>(v);
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
                    LogUtil.getLog(getClass()).error(e);
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
                return request.getContextPath() + "/visual/moduleListPage.do?code=" + StrUtil.UrlEncode(moduleCode) + "&" + moduleField + "=" + nodeCode;
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
                    return request.getContextPath() + "/visual/moduleEditPage.do?parentId=" + fdaoModule.getId() + "&id=" + fdaoModule.getId() + "&isShowNav=1&moduleCode=" + moduleCode + "&" + moduleField + "=" + nodeCode;
                } else {
                    String visitKey = SecurityUtil.makeVisitKey(fdaoModule.getId());
                    return request.getContextPath() + "/visual/moduleShowPage.do?parentId=" + fdaoModule.getId() + "&id=" + fdaoModule.getId() + "&isShowNav=1&moduleCode=" + moduleCode + "&" + moduleField + "=" + nodeCode + "&visitKey=" + visitKey;
                }
            }
        }
    }

    @ResponseBody
    @RequestMapping(value = "/getNewNodeCode", method = RequestMethod.POST, produces = {"text/html;charset=UTF-8;", "application/json;charset=UTF-8;"})
    public Result<Object> getNewNodeCode() {
        JSONObject json = new JSONObject();
        String root_code = ParamUtil.get(request, "root_code");
        if ("".equals(root_code)) {
            root_code = "root";
        }
        String parent_code = ParamUtil.get(request, "parent_code").trim();

        String newNodeCode = basicDataService.getNewNodeCode(root_code, parent_code);
        json.put("ret", 1);
        json.put("newNodeCode", newNodeCode);
        return new Result<>(json);
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
        json.put("description", leaf.getDescription());
        json.put("metaData", leaf.getMetaData());

        return json.toString();
    }

    @ResponseBody
    @RequestMapping(value = "/createNode", method = RequestMethod.POST, produces = {"text/html;charset=UTF-8;", "application/json;charset=UTF-8;"})
    public Result<Object> createNode() {
        boolean re;
        try {
            TreeSelectMgr dir = new TreeSelectMgr();
            // 判断名称是否有重复
            /*Vector<TreeSelectDb> children = dir.getChildren(parent_code);
            for (TreeSelectDb childlf : children) {
                String name = childlf.getName();
                if (name.equals(newName)) {
                    return new Result<>(false,"请检查名称是否有重复");
                }
            }*/
            //增加节点
            re = dir.addChild(request);
            if (!re) {
                return new Result<>(false, SkinUtil.LoadString(request, "res.label.cms.dir", "add_msg"));
            }
        } catch (ErrMsgException e) {
            return new Result<>(false, e.getMessage());
        }
        return new Result<>(re);
    }

    @ResponseBody
    @RequestMapping(value = "/delNode", method = RequestMethod.POST, produces = {"text/html;charset=UTF-8;", "application/json;charset=UTF-8;"})
    public Result<Object> delNode(String code) {
        boolean re = false;
        try {
            TreeSelectMgr dir = new TreeSelectMgr();
            TreeSelectDb delleaf = dir.getTreeSelectDb(code);
            if (delleaf != null) {
                if (delleaf.getParentCode().equals("-1")) {
                    return new Result<>(false, "根节点不能被删除");
                }
                dir.del(code);

                // 根据basic_tree_node树形结构节点描述，删除相关的记录
                FormDAO fdao = basicDataService.getNodeDescByLayer(delleaf.getRootCode(), delleaf.getLayer());
                if (fdao != null) {
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
                }

                re = true;
            } else {
                return new Result<>(false, "节点不存在");
            }
        } catch (ErrMsgException e) {
            return new Result<>(false, e.getMessage());
        }
        return new Result<>(re);
    }

    @ResponseBody
    @RequestMapping(value = "/rename", method = RequestMethod.POST, produces = {"text/html;charset=UTF-8;", "application/json;charset=UTF-8;"})
    public Result<Object> rename() {
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
            Vector<TreeSelectDb> children = dir.getChildren(parent_code);
            for (TreeSelectDb childlf : children) {
                if (code.equals(childlf.getCode())) {
                    continue;
                }
                String name = childlf.getName();
                if (name.equals(newName)) {
                    new Result<>(false, "请检查名称是否有重复");
                }
            }

            if (!leaf.getName().equals(newName)) {
                leaf.setName(newName);
                re = leaf.save();
            }
            else {
                new Result<>(false, "请检查名称是否重复");
            }
        } catch (ErrMsgException e) {
            return new Result<>(false, e.getMessage());
        }
        return new Result<>(re);
    }

    @ResponseBody
    @RequestMapping(value = "/updateNode", method = RequestMethod.POST, produces = {"text/html;charset=UTF-8;", "application/json;charset=UTF-8;"})
    public Result<Object> updateNode() {
        boolean re = false;
        try {
            TreeSelectMgr dir = new TreeSelectMgr();
            String code = ParamUtil.get(request, "code");
            String newName = ParamUtil.get(request, "name").trim();
            //判断名称是否有重复
            TreeSelectDb dept = new TreeSelectDb();
            dept = dept.getTreeSelectDb(code);
            String parentCode = dept.getParentCode();
            if (!"-1".equals(parentCode)) {
                Vector<TreeSelectDb> children = dir.getChildren(parentCode);
                for (TreeSelectDb childlf : children) {
                    if (code.equals(childlf.getCode())) {
                        continue;
                    }
                    String name = childlf.getName();
                    if (name.equals(newName)) {
                        return new Result<>(false, "请检查名称是否有重复");
                    }
                }
            }
            //修改节点
            re = dir.update(request);
        } catch (ErrMsgException e) {
            return new Result<>(false, e.getMessage());
        }
        return new Result<>(re);
    }

    @ResponseBody
    @RequestMapping(value = "/openNode", method = RequestMethod.POST, produces = {"text/html;charset=UTF-8;", "application/json;charset=UTF-8;"})
    public Result<Object> openNode(String code) {
        TreeSelectDb leaf = new TreeSelectDb();
        leaf = leaf.getTreeSelectDb(code);
        leaf.setOpen(true);
        return new Result<>(leaf.save());
    }

    @ResponseBody
    @RequestMapping(value = "/closeNode", method = RequestMethod.POST, produces = {"text/html;charset=UTF-8;", "application/json;charset=UTF-8;"})
    public Result<Object> closeNode(String code) {
        TreeSelectDb leaf = new TreeSelectDb();
        leaf = leaf.getTreeSelectDb(code);
        leaf.setOpen(false);
        return new Result<>(leaf.save());
    }

    @ResponseBody
    @RequestMapping(value = "/moveNode", method = RequestMethod.POST, produces = {"text/html;charset=UTF-8;", "application/json;charset=UTF-8;"})
    public Result<Object> moveNode() {
        JSONObject json = new JSONObject();
        String code = ParamUtil.get(request, "code");
        String parent_code = ParamUtil.get(request, "parent_code");
        int position = Integer.parseInt(ParamUtil.get(request, "position"));
        if ("office_equipment".equals(code)) {
            return new Result<>(false, "根节点不能移动");
        }
        if ("#".equals(parent_code)) {
            return new Result<>(false, "不能与根节点平级");
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
            TreeSelectDb rootDeptDb = dir.getTreeSelectDb("office_equipment");
            TreeSelectMgr dm = new TreeSelectMgr();
            try {
                dm.repairTree(rootDeptDb);
            } catch (Exception e) {
                LogUtil.getLog(getClass()).error(e);
                return new Result<>(false, e.getMessage());
            }

            TreeSelectCache dcm = new TreeSelectCache();
            dcm.removeAllFromCache();
        }

        return new Result<>(true, "移动成功");
    }

    @ApiOperation(value = "创建类型", notes = "创建类型", httpMethod = "POST")
    @PreAuthorize("hasAnyAuthority('admin.user', 'admin')")
    @ResponseBody
    @RequestMapping(value = "/createKind", method = RequestMethod.POST, produces = {"text/html;charset=UTF-8;", "application/json;charset=UTF-8;"})
    public Result<Object> createKind() {
        SelectKindMgr selectKindMgr = new SelectKindMgr();
        boolean re;
        try {
            re = selectKindMgr.create(request);
        }
        catch (ErrMsgException e) {
            return new Result<>(false, e.getMessage());
        }
        return new Result<>(re);
    }

    @ApiOperation(value = "删除类型", notes = "删除类型", httpMethod = "POST")
    @PreAuthorize("hasAnyAuthority('admin.user', 'admin')")
    @ResponseBody
    @RequestMapping(value = "/delKind", method = RequestMethod.POST, produces = {"text/html;charset=UTF-8;", "application/json;charset=UTF-8;"})
    public Result<Object> delKind() {
        SelectKindMgr selectKindMgr = new SelectKindMgr();
        boolean re;
        try {
            re = selectKindMgr.del(request);
        }
        catch (ErrMsgException e) {
            return new Result<>(false, e.getMessage());
        }
        return new Result<>(re);
    }

    @ApiOperation(value = "修改类型", notes = "修改类型", httpMethod = "POST")
    @PreAuthorize("hasAnyAuthority('admin.user', 'admin')")
    @ResponseBody
    @RequestMapping(value = "/updateKind", method = RequestMethod.POST, produces = {"text/html;charset=UTF-8;", "application/json;charset=UTF-8;"})
    public Result<Object> updateKind() {
        SelectKindMgr selectKindMgr = new SelectKindMgr();
        boolean re;
        try {
            re = selectKindMgr.modify(request);
        }
        catch (ErrMsgException e) {
            return new Result<>(false, e.getMessage());
        }
        return new Result<>(re);
    }

    @ApiOperation(value = "修改类型的序号", notes = "修改类型的序号", httpMethod = "POST")
    @PreAuthorize("hasAnyAuthority('admin.user', 'admin')")
    @ResponseBody
    @RequestMapping(value = "/changeKindOrder", method = RequestMethod.POST, produces = {"text/html;charset=UTF-8;", "application/json;charset=UTF-8;"})
    public Result<Object> changeKindOrder(int id, int order) {
        SelectKindDb selectKindDb = new SelectKindDb();
        selectKindDb = selectKindDb.getSelectKindDb(id);
        selectKindDb.setOrders(order);
        try {
            selectKindDb.save();
        } catch (ErrMsgException e) {
            LogUtil.getLog(getClass()).error(e);
            return new Result<>(false);
        }
        return new Result<>();
    }

    @ApiOperation(value = "获取基础数据列表", notes = "获取基础数据列表", httpMethod = "GET")
    @ApiResponses({ @ApiResponse(code = 200, message = "操作成功") })
    @ResponseBody
    @RequestMapping(value = "/list", method = {RequestMethod.GET}, produces = {"text/html;charset=UTF-8;", "application/json;charset=UTF-8;"})
    public Result<JSONObject> list(@RequestParam(defaultValue = "1") Integer page, @RequestParam(defaultValue = "20")Integer pageSize) {
        List<BasicSelectVO> list = new ArrayList<>();
        SelectKindDb selectKindDb = new SelectKindDb();
        JSONObject json = new JSONObject();
        Vector<SelectDb> v;
        ListResult lr;
        try {
            SelectDb selectDb = new SelectDb();
            lr = selectDb.listResult(SelectMgr.getSqlList(request), page, pageSize);
            v = lr.getResult();
        } catch (ErrMsgException e) {
            LogUtil.getLog(getClass()).error(e);
            return new Result<>(false, e.getMessage());
        }
        for (SelectDb selectDb : v) {
            BasicSelectVO basicSelectVO = dozerBeanMapper.map(selectDb, BasicSelectVO.class);
            selectKindDb = selectKindDb.getSelectKindDb(selectDb.getKind());
            basicSelectVO.setKindName(StrUtil.getNullStr(selectKindDb.getName()));
            basicSelectVO.setDefaultValue(selectDb.getDefaultValue());
            if (selectDb.getType() == SelectDb.TYPE_LIST) {
                basicSelectVO.setTypeName("列表");
            }
            else {
                basicSelectVO.setTypeName("树形");
                JSONArray jsonArray = new JSONArray();
                TreeSelectDb treeSelectDb = new TreeSelectDb();
                treeSelectDb = treeSelectDb.getTreeSelectDb(selectDb.getCode());
                // TreeSelectView treeSelectView = new TreeSelectView(treeSelectDb);
                // treeSelectView.getTree(request, treeSelectDb);
                jsonArray.add(treeSelectDb);
                basicSelectVO.setTreeData(jsonArray);
            }
            list.add(basicSelectVO);
        }
        json.put("total", lr.getTotal());
        json.put("list", list);
        return new Result<>(json);
    }

    @ApiOperation(value = "获取类别", notes = "获取类别", httpMethod = "GET")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "type", value = "类别", required = true, dataType = "String"),
    })
    @RequestMapping(value = "/getOptions", method = RequestMethod.GET, produces = {"text/html;charset=UTF-8;", "application/json;charset=UTF-8;"})
    @ResponseBody
    public Result<Vector<SelectOptionDb>> getOptions(@RequestParam(required = true) String code) {
        Result<Vector<SelectOptionDb>> result = new Result<>();
        SelectDb sd = new SelectDb();
        sd = sd.getSelectDb(code);
        Vector<SelectOptionDb> record = sd.getOptions(new com.cloudwebsoft.framework.db.JdbcTemplate());
        result.setData(record);
        return result;
    }

    @ApiOperation(value = "修改序号", notes = "修改序号", httpMethod = "POST")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "code", value = "基础数据编码", required = true, dataType = "String"),
            @ApiImplicitParam(name = "order", value = "基础数据排序号", required = true, dataType = "Integer"),
    })

    @ResponseBody
    @RequestMapping(value = "/changeOrder", method = RequestMethod.POST, produces = {"text/html;charset=UTF-8;", "application/json;charset=UTF-8;"})
    public Result<Object> changeOrder(String code, Integer order) {
        SelectDb selectDb = new SelectDb();
        selectDb = selectDb.getSelectDb(code);
        SelectMgr selectMgr = new SelectMgr();
        try {
            selectMgr.modify(code, selectDb.getName(), order, selectDb.getType(), String.valueOf(selectDb.getKind()));
        } catch (ResKeyException e) {
            return new Result<>(false, e.getMessage(request));
        }
        return new Result<>();
    }

    @ApiOperation(value = "增加基础数据", notes = "增加基础数据", httpMethod = "POST")
    @ResponseBody
    @RequestMapping(value = "/create", method = RequestMethod.POST, produces = {"text/html;charset=UTF-8;", "application/json;charset=UTF-8;"})
    public Result<Object> create() {
        SelectMgr selectMgr = new SelectMgr();
        boolean re;
        try {
            re = selectMgr.create(request);
        }
        catch (ErrMsgException e) {
            return new Result<>(false, e.getMessage());
        }
        return new Result<>(re);
    }

    @ApiOperation(value = "修改基础数据", notes = "修改基础数据", httpMethod = "POST")
    @ResponseBody
    @RequestMapping(value = "/update", method = RequestMethod.POST, produces = {"text/html;charset=UTF-8;", "application/json;charset=UTF-8;"})
    public Result<Object> update() {
        SelectMgr selectMgr = new SelectMgr();
        boolean re;
        try {
            re = selectMgr.modify(request);
        }
        catch (ErrMsgException e) {
            return new Result<>(false, e.getMessage());
        }
        return new Result<>(re);
    }

    @ApiOperation(value = "删除基础数据", notes = "删除基础数据", httpMethod = "POST")
    @PreAuthorize("hasAnyAuthority('admin.user', 'admin')")
    @ResponseBody
    @RequestMapping(value = "/delete", method = RequestMethod.POST, produces = {"text/html;charset=UTF-8;", "application/json;charset=UTF-8;"})
    public Result<Object> delete(String code) {
        SelectDb sd = new SelectDb();
        sd = sd.getSelectDb(code);
        try {
            return new Result<>(sd.del());
        } catch (ResKeyException e) {
            return new Result(false, e.getMessage(request));
        }
    }

    @ApiOperation(value = "增加基础数据选项", notes = "增加基础数据选项", httpMethod = "POST")
    @ResponseBody
    @RequestMapping(value = "/createOption", method = RequestMethod.POST, produces = {"text/html;charset=UTF-8;", "application/json;charset=UTF-8;"})
    public Result<Object> createOption() {
        SelectMgr selectMgr = new SelectMgr();
        boolean re;
        try {
            re = selectMgr.createOption(request);
        }
        catch (ErrMsgException e) {
            return new Result<>(false, e.getMessage());
        }
        return new Result<>(re);
    }

    @ApiOperation(value = "修改基础数据选项", notes = "增加基础数据选项", httpMethod = "POST")
    @ResponseBody
    @RequestMapping(value = "/updateOption", method = RequestMethod.POST, produces = {"text/html;charset=UTF-8;", "application/json;charset=UTF-8;"})
    public Result<Object> updateOption() {
        SelectMgr selectMgr = new SelectMgr();
        boolean re;
        try {
            re = selectMgr.modifyOption(request);
        }
        catch (ErrMsgException e) {
            return new Result<>(false, e.getMessage());
        }
        return new Result<>(re);
    }

    @ApiOperation(value = "删除基础数据选项", notes = "删除基础数据选项", httpMethod = "POST")
    @ResponseBody
    @RequestMapping(value = "/delOption", method = RequestMethod.POST, produces = {"text/html;charset=UTF-8;", "application/json;charset=UTF-8;"})
    public Result<Object> delOption(@RequestParam(required = true) Integer id) {
        return new Result<>(new SelectMgr().delOption(id));
    }

    @ApiOperation(value = "取得树形数据", notes = "取得树形数据，用以维护管理", httpMethod = "GET")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "code", value = "基础数据编码", required = true, dataType = "String"),
    })
    @RequestMapping(value = "/getTree", method = RequestMethod.GET, produces = {"text/html;charset=UTF-8;", "application/json;charset=UTF-8;"})
    @ResponseBody
    public Result<JSONObject> getTree(@RequestParam(defaultValue = "", required = true) String code) {
        Result<JSONObject> result = new Result<>();
        JSONObject jsonObject = new JSONObject();
        JSONArray jsonArray = new JSONArray();

        TreeSelectDb treeSelectDb = new TreeSelectDb();
        treeSelectDb = treeSelectDb.getTreeSelectDb(code);
        TreeSelectView treeSelectView = new TreeSelectView(treeSelectDb);
        treeSelectView.getTree(request, treeSelectDb);
        jsonArray.add(treeSelectDb);

        jsonObject.put("list",jsonArray);
        jsonObject.put("nodeSelected", code);
        result.setData(jsonObject);

        return result;
    }
}
