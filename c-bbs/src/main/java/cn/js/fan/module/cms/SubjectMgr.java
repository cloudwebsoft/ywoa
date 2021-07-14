package cn.js.fan.module.cms;

import java.sql.*;

import javax.servlet.http.*;

import cn.js.fan.db.*;
import cn.js.fan.security.*;
import cn.js.fan.util.*;
import cn.js.fan.web.*;
import org.apache.log4j.*;
import java.util.Vector;
import java.util.Iterator;
import cn.js.fan.module.cms.IDirectory;
import java.io.IOException;
import cn.js.fan.util.file.FileUtil;
import com.cloudwebsoft.framework.db.JdbcTemplate;

/**
 *
 * <p>Title: </p>
 *
 * <p>Description: </p>
 *
 * <p>Copyright: Copyright (c) 2004</p>
 *
 * <p>Company: </p>
 * ╋ 女性话题      一级目录
 *   ├『花样年华』  二级目录
 *   ├『花样年华』
 *   ╋ 女性话题     二级目录
 *     ├『花样年华』 三级目录
 * @author not attributable
 * @version 1.0
 */

public class SubjectMgr {
    String connname = "";
    Logger logger = Logger.getLogger(SubjectMgr.class.getName());

    public SubjectMgr() {
        connname = Global.getDefaultDB();
        if (connname.equals(""))
            logger.info("Directory:默认数据库名不能为空");
    }

    public boolean AddChild(HttpServletRequest request) throws
            ErrMsgException {
        String name = "", code = "", parent_code = "";
        name = ParamUtil.get(request, "name", false);
        if (name == null)
            throw new ErrMsgException("名称不能为空！");
        code = ParamUtil.get(request, "code").trim();
        if (code.equals(""))
            throw new ErrMsgException("编码不能为空！");
        if (!StrUtil.isSimpleCode(code))
            throw new ErrMsgException("编码请使用字母、数字、-或_！");

        parent_code = ParamUtil.get(request, "parent_code").trim();
        if (parent_code.equals(""))
            throw new ErrMsgException("父结点不能为空！");
        String description = ParamUtil.get(request, "description");
        int type = ParamUtil.getInt(request, "type");
        int pageTemplateId = ParamUtil.getInt(request, "pageTemplateId");
        int templateId = ParamUtil.getInt(request, "templateId");
        boolean isHome = ParamUtil.get(request, "isHome").equals("true") ? true : false;

        SubjectDb lf = new SubjectDb();
        lf.setName(name);
        lf.setCode(code);
        lf.setParentCode(parent_code);
        lf.setDescription(description);
        lf.setType(type);
        lf.setPageTemplateId(pageTemplateId);
        lf.setTemplateId(templateId);
        lf.setIsHome(isHome);

        SubjectDb dd = getSubjectDb(parent_code);
        return dd.AddChild(lf);
    }

    public void del(String delcode) throws ErrMsgException,ResKeyException {
        SubjectDb lf = getSubjectDb(delcode);

        String htmlPath = lf.getListHtmlPath();

        lf.del(lf);

        // 删除列表页面
        Config cfg = new Config();
        boolean isDelHtml = cfg.getBooleanProperty("cms.html_auto");
        if (isDelHtml) {
            // 删除目录
            try {
                FileUtil.del(Global.realPath + "/" + htmlPath);
            }
            catch (IOException e) {
                logger.error("del:" + e.getMessage());
            }
        }

        // 删除该专题目录下面的所有的文章
        SubjectListDb sld = new SubjectListDb();
        String sql = SQLBuilder.getSubjectDocListSql(delcode);
        Iterator ir = sld.list(sql).iterator();
        while (ir.hasNext()) {
            sld = (SubjectListDb)ir.next();
            sld.del(new JdbcTemplate());
        }
    }

    public synchronized boolean update(HttpServletRequest request) throws
            ErrMsgException {
        String code = ParamUtil.get(request, "code", false);
        String name = ParamUtil.get(request, "name", false);
        String description = ParamUtil.get(request, "description");
        boolean isHome = ParamUtil.get(request, "isHome").equals("true") ? true : false;
        int type = ParamUtil.getInt(request, "type");
        if (code == null || name == null) {
            throw new ErrMsgException("code与name项必填！");
        }
        String parentCode = ParamUtil.get(request, "parentCode");
        int pageTemplateId = ParamUtil.getInt(request, "pageTemplateId");
        int templateId = ParamUtil.getInt(request, "templateId");

        SubjectDb leaf = getSubjectDb(code);
        if (code.equals(parentCode)) {
            throw new ErrMsgException("请选择正确的父节点！");
        }
        if (!parentCode.equals(leaf.getParentCode())) {
            // 节点不能改变其父节点为其子节点
            SubjectDb lf = getSubjectDb(parentCode); // 取得新的父节点
            while (lf!=null && !lf.getCode().equals(lf.ROOTCODE)) {
                // 从parentCode节点往上遍历，如果找到leaf.getParentCode()则证明不合法
                String pCode = lf.getParentCode();
                if (pCode.equals(leaf.getCode()))
                    throw new ErrMsgException("不能将其子节点更改为父节点");
                lf = getSubjectDb(pCode);
            }
        }

        leaf.setName(name);
        leaf.setDescription(description);
        leaf.setIsHome(isHome);
        leaf.setType(type);
        leaf.setPageTemplateId(pageTemplateId);
        leaf.setTemplateId(templateId);
        leaf.setIsHome(isHome);
        boolean re = false;
        if (parentCode.equals(leaf.getParentCode())) {
            // logger.info("update:name=" + name);
            re = leaf.save();
        }
        else
            re = leaf.save(parentCode);

        return re;
    }

    public synchronized boolean move(HttpServletRequest request) throws
            ErrMsgException {
        String code = ParamUtil.get(request, "code", false);
        String direction = ParamUtil.get(request, "direction", false);
        if (code == null || direction == null) {
            throw new ErrMsgException("编码与方向项必填！");
        }

        SubjectDb dd = getSubjectDb(code);
        return dd.move(direction);
    }

    public SubjectDb getSubjectDb(String code) {
        SubjectDb dd = new SubjectDb();
        return dd.getSubjectDb(code);
    }

    public SubjectDb getBrother(String code, String direction) throws
            ErrMsgException {
        SubjectDb dd = getSubjectDb(code);
        return dd.getBrother(direction);
    }

    public Vector getChildren(String code) throws ErrMsgException {
        SubjectDb dd = getSubjectDb(code);
        return dd.getChildren();
    }


    /**
     * 修复因为BUG或者误操作致树形结构被破坏的问题
     */
    public void repairLeaf(SubjectDb lf) {
        Vector children = lf.getChildren();
        // 重置孩子节点数
        lf.setChildCount(children.size());
        Iterator ir = children.iterator();
        int orders = 1;
        while (ir.hasNext()) {
            SubjectDb lfch = (SubjectDb)ir.next();
            // 重置孩子节点的排列顺序
            lfch.setOrders(orders);
            // System.out.println(getClass() + " leaf name=" + lfch.getName() + " orders=" + orders);

            lfch.save();
            orders ++;
        }
        // 重置层数
        int layer = 2;
        String parentCode = lf.getParentCode();
        if (lf.getCode().equals(lf.ROOTCODE)) {
            layer = 1;
        }
        else {
            if (parentCode.equals(lf.ROOTCODE))
                layer = 2;
            else {
                while (!parentCode.equals(lf.ROOTCODE)) {
                    // System.out.println(getClass() + "leaf parentCode=" + parentCode);
                    SubjectDb parentLeaf = getSubjectDb(parentCode);
                    if (parentLeaf == null || !parentLeaf.isLoaded())
                        break;
                    else {
                        parentCode = parentLeaf.getParentCode();
                    }
                    layer++;
                }
            }
        }
        lf.setLayer(layer);
        lf.save();
    }

    // 修复根结点为leaf的树
    public void repairTree(SubjectDb leaf) throws Exception {
        // System.out.println(getClass() + "leaf name=" + leaf.getName());
        repairLeaf(leaf);
        Vector children = getChildren(leaf.getCode());
        int size = children.size();
        if (size == 0)
            return;

        Iterator ri = children.iterator();
        // 写跟贴
        while (ri.hasNext()) {
            SubjectDb childlf = (SubjectDb) ri.next();
            repairTree(childlf);
        }
        // 刷新缓存
        SubjectCache dc = new SubjectCache();
        dc.removeAllFromCache();
    }
}

