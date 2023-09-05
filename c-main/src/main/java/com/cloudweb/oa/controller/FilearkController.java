package com.cloudweb.oa.controller;

import javax.imageio.ImageIO;
import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import cn.js.fan.db.ListResult;
import cn.js.fan.util.DateUtil;
import cn.js.fan.util.StrUtil;
import cn.js.fan.web.Global;
import cn.js.fan.web.SkinUtil;
import com.cloudweb.oa.service.IFileService;
import com.cloudweb.oa.utils.I18nUtil;
import com.cloudweb.oa.utils.ResponseUtil;
import com.cloudweb.oa.utils.SpringUtil;
import com.cloudwebsoft.framework.util.IPUtil;
import com.cloudwebsoft.framework.util.LogUtil;
import com.redmoon.kit.util.FileUpload;
import com.redmoon.oa.android.Privilege;
import com.redmoon.oa.basic.SelectDb;
import com.redmoon.oa.basic.SelectMgr;
import com.redmoon.oa.basic.SelectOptionDb;
import com.redmoon.oa.fileark.*;
import com.redmoon.oa.fileark.plugin.PluginMgr;
import com.redmoon.oa.fileark.plugin.PluginUnit;
import com.redmoon.oa.fileark.plugin.base.IPluginUI;
import com.redmoon.oa.flow.AttachmentLogDb;
import com.redmoon.oa.flow.AttachmentLogMgr;
import com.redmoon.oa.notice.NoticeAttachmentDb;
import com.redmoon.oa.person.UserDb;
import com.redmoon.oa.person.UserMgr;
import com.redmoon.oa.person.UserSetupDb;
import com.redmoon.oa.pvg.PrivDb;
import com.redmoon.oa.sys.DebugUtil;
import com.redmoon.oa.ui.SkinMgr;
import com.redmoon.oa.util.PdfUtil;
import com.redmoon.oa.util.WordUtil;
import org.apache.commons.lang3.StringUtils;
import org.htmlparser.Parser;
import org.htmlparser.filters.TagNameFilter;
import org.htmlparser.tags.ImageTag;
import org.htmlparser.util.NodeList;
import org.htmlparser.util.ParserException;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import cn.js.fan.util.ErrMsgException;
import cn.js.fan.util.ParamUtil;

import java.awt.image.BufferedImage;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.sql.SQLException;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Vector;

@Controller
@RequestMapping("/fileark")
public class FilearkController {
    @Autowired
    private HttpServletRequest request;

    @Autowired
    private IFileService fileService;

    @Autowired
    private I18nUtil i18nUtil;

    @Autowired
    private ResponseUtil responseUtil;

    /**
     * 下载前验证，如：打包下载
     *
     * @return
     */
    @ResponseBody
    @RequestMapping(value = "/downloadValidate", method = RequestMethod.POST, produces = {"text/html;charset=UTF-8;", "application/json;charset=UTF-8;"})
    public String downloadValidate() {
        JSONObject json = new JSONObject();
        com.redmoon.oa.pvg.Privilege pvg = new com.redmoon.oa.pvg.Privilege();

        boolean re = true;
        try {
            String ids = ParamUtil.get(request, "ids");
            String msg;
            boolean isZip = ParamUtil.getBoolean(request, "isZip", false);
            try {
                msg = Directory.onDownloadValidate(request, ids, pvg.getUser(request), isZip);
            } catch (ErrMsgException e) {
                re = false;
                msg = e.getMessage();
            }

            if (re) {
                if (msg == null) {
                    msg = "无验证脚本！";
                    json.put("ret", 2);
                } else {
                    json.put("ret", 1);
                }
                json.put("msg", msg);
            } else {
                json.put("ret", 0);
                json.put("msg", msg);
            }
        } catch (JSONException e) {
            LogUtil.getLog(getClass()).error(e);
        }
        return json.toString();
    }

    /**
     * 列出文件
     *
     * @param skey
     * @return
     */
    @ResponseBody
    @RequestMapping(value = "/list", method = RequestMethod.POST, produces = {"text/html;charset=UTF-8;", "application/json;charset=UTF-8;"})
    public String list(String skey) {
        JSONObject json = new JSONObject();

        Privilege privilege = new Privilege();
        boolean re = privilege.auth(request);
        if (!re) {
            try {
                json.put("res", "-2");
                json.put("msg", "时间过期");
                return json.toString();
            } catch (JSONException e) {
                LogUtil.getLog(getClass()).error(e);
            }
        }

        try {
            String dirCode = ParamUtil.get(request, "dirCode");
            String op = ParamUtil.get(request, "op");
            String cond = ParamUtil.get(request, "cond");
            String what = request.getParameter("what");
            // what = StrUtil.UnicodeToUTF8(what);
            Document doc = new Document();
            String sql = "select id from document where class1=" + StrUtil.sqlstr(dirCode) + " and examine<>" + Document.EXAMINE_DUSTBIN;
            LeafPriv lp = new LeafPriv();
            lp.setDirCode(dirCode);
            if (!lp.canUserModify(privilege.getUserName())) {
                sql += " and examine=" + Document.EXAMINE_PASS;
            }
            if ("search".equals(op)) {
                if ("title".equals(cond)) {
                    sql += " and title like " + StrUtil.sqlstr("%" + what + "%");
                }
            }
            sql += " order by doc_level desc, examine asc, id desc";

            int curpage = ParamUtil.getInt(request, "pagenum", 1);
            int pagesize = ParamUtil.getInt(request, "pagesize", 20);
            String groupKey = dirCode;
            DocBlockIterator ir = doc.getDocuments(sql, groupKey, (curpage - 1) * pagesize, curpage * pagesize - 1);

            json.put("res", "0");
            json.put("msg", "操作成功");
            int total = doc.getDocCount(sql);
            json.put("total", String.valueOf(total));

            JSONObject result = new JSONObject();
            result.put("count", String.valueOf(pagesize));

            UserMgr um = new UserMgr();
            JSONArray arr = new JSONArray();
            while (ir.hasNext()) {
                doc = (Document) ir.next();
                JSONObject jsonDoc = new JSONObject();
                jsonDoc.put("id", String.valueOf(doc.getId()));
                jsonDoc.put("icon", doc.getFileIcon());
                jsonDoc.put("title", doc.getTitle());
                String realName = doc.getAuthor();
                UserDb ud = um.getUserDb(doc.getAuthor());
                if (ud.isLoaded()) {
                    realName = ud.getRealName();
                }
                jsonDoc.put("author", realName);
                jsonDoc.put("createdate", DateUtil.format(doc.getCreateDate(), "MM-dd HH:mm"));
                arr.put(jsonDoc);
            }
            result.put("documents", arr);
            json.put("result", result);
        } catch (JSONException e) {
            com.cloudwebsoft.framework.util.LogUtil.getLog(getClass()).error(e.getMessage());
        }
        return json.toString();
    }

    /**
     * 获得文档，暂无用
     *
     * @param id
     * @return
     */
    @ResponseBody
    @RequestMapping(value = "/getDoc", method = RequestMethod.GET, produces = {"text/html;charset=UTF-8;", "application/json;charset=UTF-8;"})
    public String getDoc(int id) {
        JSONObject json = new JSONObject();
        Privilege privilege = new Privilege();
        boolean re = privilege.auth(request);
        if (!re) {
            try {
                json.put("res", "-2");
                json.put("msg", "时间过期");
                return json.toString();
            } catch (JSONException e) {
                LogUtil.getLog(getClass()).error(e);
            }
        }

        Document doc = new Document();
        doc = doc.getDocument(id);
        try {
            if (doc != null && doc.isLoaded()) {
                String userName = StrUtil.getNullStr(doc.getAuthor());
                UserDb user = new UserDb();
                user = user.getUserDb(userName);
                String author = userName;
                if (user.isLoaded()) {
                    author = user.getRealName();
                }
                JSONObject data = new JSONObject();
                data.put("title", doc.getTitle());
                data.put("author", author);
                data.put("createDate", DateUtil.format(doc.getCreateDate(), "yyyy-MM-dd"));
                data.put("content", doc.getContent(1));
                json.put("data", data);

/*                JSONArray attachments = new JSONArray();

                Iterator ir = doc.getAttachments(1).iterator();
                String downPath = "";
                while (ir.hasNext()) {
                    Attachment att = (Attachment)ir.next();
                    JSONObject attachment = new JSONObject();
                    attachment.put("id", String.valueOf(att.getId()));
                    attachment.put("name", att.getName());
                    downPath = "public/android/doc_getfile.jsp?attId="+att.getId()+"&id="+id;
                    attachment.put("url", downPath);
                    attachment.put("size", String.valueOf(att.getSize()));
                    attachments.put(attachment);
                }
                json.put("attachments", attachments);*/

                json.put("res", 0);
            } else {
                json.put("res", -1);
            }
        } catch (JSONException e) {
            LogUtil.getLog(getClass()).error(e);
        }
        return json.toString();
    }

    @ResponseBody
    @RequestMapping(value = "/moveAttachment", method = RequestMethod.POST, produces = {"text/html;charset=UTF-8;", "application/json;charset=UTF-8;"})
    public String moveAttachment(int attachId, String direction) {
        com.redmoon.oa.fileark.Attachment att = new com.redmoon.oa.fileark.Attachment(attachId);
        int docId = att.getDocId();
        Document doc = new Document();
        doc = doc.getDocument(docId);

        com.redmoon.oa.pvg.Privilege privilege = new com.redmoon.oa.pvg.Privilege();
        JSONObject json = new JSONObject();
        LeafPriv lp = new LeafPriv(doc.getDirCode());
        if (!lp.canUserModify(privilege.getUser(request))) {
            try {
                json.put("res", "0");
                json.put("msg", SkinUtil.LoadString(request, "pvg_invalid"));
                return json.toString();
            } catch (JSONException e) {
                LogUtil.getLog(getClass()).error(e);
            }
            return json.toString();
        }

        // 取得第一页的内容
        DocContent dc = new DocContent();
        dc = dc.getDocContent(docId, 1);
        boolean re = dc.moveAttachment(attachId, direction);
        try {
            if (re) {
                json.put("ret", 1);
                json.put("msg", "操作成功");
            } else {
                json.put("ret", 0);
                json.put("msg", "操作失败");
            }
        } catch (JSONException e) {
            LogUtil.getLog(getClass()).error(e);
        }
        return json.toString();
    }

    @ResponseBody
    @RequestMapping(value = "/move", method = RequestMethod.POST, produces = {"text/html;charset=UTF-8;", "application/json;charset=UTF-8;"})
    public String move() throws Exception {
        com.redmoon.oa.pvg.Privilege privilege = new com.redmoon.oa.pvg.Privilege();
        com.alibaba.fastjson.JSONObject json = new com.alibaba.fastjson.JSONObject();
        String code = ParamUtil.get(request, "code");
        String parent_code = ParamUtil.get(request, "parent_code");
        int position = Integer.parseInt(ParamUtil.get(request, "position"));
        if ("root".equals(code)) {
            json.put("ret", "0");
            json.put("msg", "根节点不能移动！");
            return json.toString();
        }
        if ("#".equals(parent_code)) {
            json.put("ret", "0");
            json.put("msg", "不能与根节点平级！");
            return json.toString();
        }

        Directory dir = new Directory();
        com.redmoon.oa.fileark.Leaf moveleaf = dir.getLeaf(code);
        int old_position = moveleaf.getOrders();//得到被移动节点原来的位置
        String old_parent_code = moveleaf.getParentCode();

        LeafPriv lp = new LeafPriv();
        lp.setDirCode(code);
        if (!lp.canUserExamine(privilege.getUser(request))) {
            json.put("ret", "0");
            json.put("msg", SkinUtil.LoadString(request, "pvg_invalid"));
            return json.toString();
        }

        if (!parent_code.equals(old_parent_code)) {
            lp.setDirCode(parent_code);
            if (!lp.canUserExamine(privilege.getUser(request))) {
                json.put("ret", "0");
                json.put("msg", SkinUtil.LoadString(request, "pvg_invalid"));
                return json.toString();
            }
        }

        com.redmoon.oa.fileark.Leaf newParentLeaf = dir.getLeaf(parent_code);

        int p = position + 1;
        moveleaf.setOrders(p);
        if (!parent_code.equals(old_parent_code)) {
            moveleaf.update(parent_code);
        } else {
            moveleaf.update();
        }

        // 重新梳理orders
        Iterator ir = newParentLeaf.getChildren().iterator();
        while (ir.hasNext()) {
            Leaf lf = (Leaf) ir.next();
            // 跳过自己
            if (lf.getCode().equals(code)) {
                continue;
            }
            if (p < old_position) {//上移
                if (lf.getOrders() >= p) {
                    lf.setOrders(lf.getOrders() + 1);
                    lf.update();
                }
            } else {//下移
                if (lf.getOrders() <= p && lf.getOrders() > old_position) {
                    lf.setOrders(lf.getOrders() - 1);
                    lf.update();
                }
            }
        }

        // 原节点下的孩子节点通过修复repairTree处理
        com.redmoon.oa.fileark.Leaf rootLeaf = dir.getLeaf(com.redmoon.oa.fileark.Leaf.ROOTCODE);
        Directory dm = new Directory();
        dm.repairTree(rootLeaf);

        json.put("ret", "1");
        json.put("msg", "操作成功！");
        return json.toString();
    }

    /**
     * 删除附件
     *
     * @param attachId
     * @return
     */
    @ResponseBody
    @RequestMapping(value = "/delAttach", method = RequestMethod.POST, produces = {"text/html;charset=UTF-8;", "application/json;"})
    public String delAttach(int attachId) {
        com.redmoon.oa.fileark.Attachment att = new com.redmoon.oa.fileark.Attachment(attachId);
        int docId = att.getDocId();
        Document doc = new Document();
        doc = doc.getDocument(docId);

        com.redmoon.oa.pvg.Privilege privilege = new com.redmoon.oa.pvg.Privilege();
        com.alibaba.fastjson.JSONObject json = new com.alibaba.fastjson.JSONObject();
        com.redmoon.oa.Config cfg = new com.redmoon.oa.Config();
        LeafPriv lp = new LeafPriv(doc.getDirCode());
        if (!lp.canUserModify(privilege.getUser(request))) {
            if (doc.getExamine() == Document.EXAMINE_NOT) {
                if (!lp.canUserModify(privilege.getUser(request))) {
                    json.put("res", "0");
                    json.put("msg", SkinUtil.LoadString(request, "文章正在审核中，不能编辑"));
                    return json.toString();
                }
            }

            // 判断是否本人编辑自己的文章
            boolean canModify = false;
            if (doc.getAuthor().equals(privilege.getUser(request))) {
                double filearkUserDelIntervalH = StrUtil.toDouble(cfg.get("filearkUserEditDelInterval"), 0);
                double intervalMinute = filearkUserDelIntervalH * 60;
                if (DateUtil.datediffMinute(new Date(), doc.getCreateDate()) < intervalMinute) {
                    canModify = true;
                } else {
                    json.put("res", "0");
                    json.put("msg", SkinUtil.LoadString(request, "已超时，发布后" + filearkUserDelIntervalH + "小时内可修改"));
                    return json.toString();

                }
            }
            if (!canModify) {
                json.put("res", "0");
                json.put("msg", SkinUtil.LoadString(request, "pvg_invalid"));
                return json.toString();
            }
        }

        DocContent dc = doc.getDocContent(1);
        boolean re = dc.delAttachment(attachId);
        if (re) {
            json.put("ret", 1);
            json.put("msg", "操作成功！");
        } else {
            json.put("ret", 0);
            json.put("msg", "操作失败！");
        }
        return json.toString();
    }

    /**
     * 更改附件名称
     *
     * @param attachId
     * @return
     */
    @ResponseBody
    @RequestMapping(value = "/changeAttachName", method = RequestMethod.POST, produces = {"text/html;charset=UTF-8;", "application/json;"})
    public String changeAttachName(int attachId) {
        com.redmoon.oa.fileark.Attachment att = new com.redmoon.oa.fileark.Attachment(attachId);
        int docId = att.getDocId();
        Document doc = new Document();
        doc = doc.getDocument(docId);

        com.redmoon.oa.pvg.Privilege privilege = new com.redmoon.oa.pvg.Privilege();
        JSONObject json = new JSONObject();
        LeafPriv lp = new LeafPriv(doc.getDirCode());
        if (!lp.canUserModify(privilege.getUser(request))) {
            try {
                json.put("res", "0");
                json.put("msg", SkinUtil.LoadString(request, "pvg_invalid"));
                return json.toString();
            } catch (JSONException e) {
                LogUtil.getLog(getClass()).error(e);
            }
            return json.toString();
        }

        try {
            String newName = ParamUtil.get(request, "newName");
            DocContent dc = doc.getDocContent(1);
            boolean re = dc.updateAttachmentName(attachId, newName);
            if (re) {
                json.put("ret", 1);
                json.put("msg", "操作成功！");
            } else {
                json.put("ret", 0);
                json.put("msg", "操作失败！");
            }
        } catch (JSONException e) {
            LogUtil.getLog(getClass()).error(e);
        }
        return json.toString();
    }

    /**
     * 复制权限
     *
     * @param sourceDirCode
     * @param destDirCode
     * @return
     */
    @ResponseBody
    @RequestMapping(value = "/copyPriv", method = RequestMethod.POST, produces = {"text/html;charset=UTF-8;", "application/json;"})
    public String copyPriv(String sourceDirCode, String destDirCode) {
        JSONObject json = new JSONObject();
        try {
            LeafPriv leafPriv = new LeafPriv();
            String sql = leafPriv.getListSqlForDir(sourceDirCode, "name", "asc");
            Vector v = leafPriv.list(sql);
            Iterator ir = v.iterator();
            boolean re = true;
            while (ir.hasNext()) {
                leafPriv = (LeafPriv) ir.next();

                // 判断是否已存在该权限
                LeafPriv leafPriv1 = leafPriv.getLeafPriv(destDirCode, leafPriv.getName(), leafPriv.getType());
                if (leafPriv1 == null) {
                    re = leafPriv.copy(destDirCode);
                } else {
                    // 如果存在则置成权限一样
                    leafPriv1.setAppend(leafPriv.getAppend());
                    leafPriv1.setDel(leafPriv.getDel());
                    leafPriv1.setExamine(leafPriv.getExamine());
                    leafPriv1.setModify(leafPriv.getModify());
                    leafPriv1.setDownLoad(leafPriv.getDownLoad());
                    leafPriv1.setSee(leafPriv.getSee());
                    leafPriv1.save();
                }
            }
            if (re) {
                json.put("ret", 1);
                json.put("msg", "操作成功！");
            } else {
                json.put("ret", 0);
                json.put("msg", "操作失败！");
            }
        } catch (JSONException e) {
            LogUtil.getLog(getClass()).error(e);
        } catch (ErrMsgException e) {
            LogUtil.getLog(getClass()).error(e);
            try {
                json.put("ret", 0);
                json.put("msg", e.getMessage());
            } catch (JSONException e1) {
                e1.printStackTrace();
            }
        }
        return json.toString();
    }

    /**
     * 批量删除文档
     *
     * @return
     */
    @ResponseBody
    @RequestMapping(value = "/delBatch", method = RequestMethod.POST, produces = {"text/html;charset=UTF-8;", "application/json;"})
    public String delBatch() {
        JSONObject json = new JSONObject();
        try {
            DocumentMgr documentMgr = new DocumentMgr();
            documentMgr.delBatch(request, true);
            json.put("ret", 1);
            json.put("msg", "操作成功！");
        } catch (JSONException e) {
            LogUtil.getLog(getClass()).error(e);
        } catch (ErrMsgException e) {
            LogUtil.getLog(getClass()).error(e);
            try {
                json.put("ret", 0);
                json.put("msg", e.getMessage());
            } catch (JSONException e1) {
                e1.printStackTrace();
            }
        }
        return json.toString();
    }

    /**
     * 批量通过文档
     *
     * @return
     */
    @ResponseBody
    @RequestMapping(value = "/passExamine", method = RequestMethod.POST, produces = {"text/html;charset=UTF-8;", "application/json;"})
    public String passExamine() {
        JSONObject json = new JSONObject();
        try {
            DocumentMgr documentMgr = new DocumentMgr();
            documentMgr.passExamineBatch(request);
            json.put("ret", 1);
            json.put("msg", "操作成功！");
        } catch (JSONException e) {
            LogUtil.getLog(getClass()).error(e);
        } catch (ErrMsgException e) {
            LogUtil.getLog(getClass()).error(e);
            try {
                json.put("ret", 0);
                json.put("msg", e.getMessage());
            } catch (JSONException e1) {
                e1.printStackTrace();
            }
        }
        return json.toString();
    }

    /**
     * 批量不通过文档
     *
     * @return
     */
    @ResponseBody
    @RequestMapping(value = "/unpassExamine", method = RequestMethod.POST, produces = {"text/html;charset=UTF-8;", "application/json;"})
    public String unpassExamine() {
        JSONObject json = new JSONObject();
        try {
            DocumentMgr documentMgr = new DocumentMgr();
            documentMgr.unpassExamineBatch(request);
            json.put("ret", 1);
            json.put("msg", "操作成功！");
        } catch (JSONException e) {
            LogUtil.getLog(getClass()).error(e);
        } catch (ErrMsgException e) {
            LogUtil.getLog(getClass()).error(e);
            try {
                json.put("ret", 0);
                json.put("msg", e.getMessage());
            } catch (JSONException e1) {
                e1.printStackTrace();
            }
        }
        return json.toString();
    }

    /**
     * 获取孩子节点的htl，用于dir_sel.jsp中ajax显示目录
     *
     * @return
     */
    @ResponseBody
    @RequestMapping(value = "/getChildrenHtml", method = RequestMethod.POST, produces = {"text/html;charset=UTF-8;", "application/json;"})
    public String getChildrenHtml() {
        try {
            String parentCode = ParamUtil.get(request, "parentCode");
            if (parentCode.equals("")) {
                return "";
            }
            com.redmoon.oa.fileark.Leaf lf = new com.redmoon.oa.fileark.Leaf();
            lf = lf.getLeaf(parentCode);

            DirView tv = new DirView(request, lf);

            String root_code = ParamUtil.get(request, "root_code");
            request.setAttribute("root_code", root_code);

            String op = ParamUtil.get(request, "op");
            if (op.equals("singleSel")) {
                StringBuffer sb = new StringBuffer();
                tv.SelectSingleAjaxHtml(sb, "selectNode", "", "", false);
                return sb.toString();
            } else {
                return "";
            }
        } catch (ErrMsgException e) {
            LogUtil.getLog(getClass()).error(e);
        }
        return "";
    }

    /**
     * 批量删除文档
     *
     * @return
     */
    @ResponseBody
    @RequestMapping(value = "/operate", method = RequestMethod.POST, produces = {"text/html;charset=UTF-8;", "application/json;"})
    public String operate() {
        com.redmoon.oa.pvg.Privilege privilege = new com.redmoon.oa.pvg.Privilege();
        DocumentMgr docmanager = new DocumentMgr();
        com.alibaba.fastjson.JSONObject json = new com.alibaba.fastjson.JSONObject();
        ServletContext application = request.getSession().getServletContext();
        boolean re = false;
        try {
            re = docmanager.Operate(application, request, privilege);
        } catch (ErrMsgException e) {
            String action = ParamUtil.get(request, "action");
            if ("fckwebedit_new".equals(action)) {
                json.put("ret", 0);
                json.put("msg", e.getMessage());
            } else {
                json.put("ret", 0);
                json.put("msg", e.getMessage());
            }
            return json.toString();
        }
        if (re) {
            String action = ParamUtil.get(request, "action");
            if (action.equals("fckwebedit_new")) {
                com.redmoon.kit.util.FileUpload fu = docmanager.getFileUpload();
                String op = fu.getFieldValue("op");
                if (op.equals("edit")) {
                    json.put("ret", 1);
                    json.put("examineFlowId", docmanager.getExamineFlowId());
                    if (docmanager.getDocument().getExamine() == Document.EXAMINE_DRAFT) {
                        json.put("msg", "保存草稿成功！");
                    } else {
                        json.put("msg", "操作成功！");
                    }
                    return json.toString();
                } else {
                    String pageUrl = "";
                    if (docmanager.getDirCode().indexOf("cws_prj_") == 0) {
                        String projectId = docmanager.getDirCode().substring(8);
                        // 如果projectId中含有下划线_，则截取出其ID
                        int p = projectId.indexOf("_");
                        if (p != -1) {
                            projectId = projectId.substring(0, p);
                        }
                        pageUrl = "fileark/document_list_m.jsp?dir_code=" + StrUtil.UrlEncode(docmanager.getDirCode()) + "&projectId=" + projectId + "&parentId=" + projectId + "&formCode=project";
                    }
                    json.put("ret", 1);
                    json.put("docId", docmanager.getDocument().getId());
                    json.put("examineFlowId", docmanager.getExamineFlowId());
                    if (docmanager.getDocument().getExamine() == Document.EXAMINE_DRAFT) {
                        json.put("msg", "保存草稿成功！");
                    } else {
                        json.put("msg", "操作成功！");
                    }
                    json.put("redirectUri", pageUrl);
                    return json.toString();
                }
            } else {
                // 用于fwebedit.jsp
                Document doc = docmanager.getDocument();
                if (doc.getExamine() == Document.EXAMINE_NOT) {
                    return "操作成功，正在等待审核中...！";
                } else {
                    return "操作成功！";
                }
            }
        } else {
            json.put("ret", 0);
            json.put("msg", "操作失败！");
            return json.toString();
        }
    }

    @ResponseBody
    @RequestMapping(value = "/addComment", method = RequestMethod.POST, produces = {"text/html;charset=UTF-8;", "application/json;charset=UTF-8;"})
    public String addComment() {
        com.alibaba.fastjson.JSONObject json = new com.alibaba.fastjson.JSONObject();
        CommentMgr cm = new CommentMgr();
        boolean re = false;
        try {
            re = cm.insert(request);
        } catch (ErrMsgException e) {
            json.put("ret", "0");
            json.put("msg", e.getMessage());
            return json.toString();
        }

        if (re) {
            json.put("ret", "1");
            json.put("msg", "操作成功！");
        } else {
            json.put("ret", "0");
            json.put("msg", "操作失败！");
        }
        return json.toString();
    }

    @ResponseBody
    @RequestMapping(value = "/delComment", method = RequestMethod.POST, produces = {"text/html;charset=UTF-8;", "application/json;charset=UTF-8;"})
    public String delComment() {
        com.alibaba.fastjson.JSONObject json = new com.alibaba.fastjson.JSONObject();
        CommentMgr cm = new CommentMgr();
        boolean re = false;
        try {
            com.redmoon.oa.pvg.Privilege privilege = new com.redmoon.oa.pvg.Privilege();
            int cmtId = ParamUtil.getInt(request, "cmtId");
            re = cm.del(request, privilege, cmtId);
        } catch (ErrMsgException e) {
            json.put("ret", "0");
            json.put("msg", e.getMessage());
            return json.toString();
        }

        if (re) {
            json.put("ret", "1");
            json.put("msg", "操作成功！");
        } else {
            json.put("ret", "0");
            json.put("msg", "操作失败！");
        }
        return json.toString();
    }

    /**
     * 从草稿箱发布文档
     *
     * @return
     */
    @ResponseBody
    @RequestMapping(value = "/publish", method = RequestMethod.POST, produces = {"text/html;charset=UTF-8;", "application/json;"})
    public String publish() {
        JSONObject json = new JSONObject();
        try {
            com.redmoon.oa.pvg.Privilege privilege = new com.redmoon.oa.pvg.Privilege();
            String strids = ParamUtil.get(request, "ids");
            String[] ids = StrUtil.split(strids, ",");
            if (ids == null) {
                json.put("ret", "0");
                json.put("msg", "请选择记录！");
                return json.toString();
            }
            DocumentMgr documentMgr = new DocumentMgr();
            documentMgr.publish(request, privilege.getUser(request), ids);
            json.put("ret", 1);
            json.put("msg", "操作成功！");
        } catch (JSONException e) {
            LogUtil.getLog(getClass()).error(e);
        } catch (ErrMsgException e) {
            LogUtil.getLog(getClass()).error(e);
            try {
                json.put("ret", 0);
                json.put("msg", e.getMessage());
            } catch (JSONException e1) {
                e1.printStackTrace();
            }
        }
        return json.toString();
    }

    @ResponseBody
    @RequestMapping(value = "/delLeaf", method = RequestMethod.POST, produces = {"text/html;charset=UTF-8;", "application/json;charset=UTF-8;"})
    public String delLeaf() {
        com.alibaba.fastjson.JSONObject json = new com.alibaba.fastjson.JSONObject();
        String delcode = ParamUtil.get(request, "code");
        try {
            Directory dir = new Directory();
            dir.del(request, delcode);
        } catch (ErrMsgException e) {
            json.put("ret", 3);
            json.put("msg", e.getMessage());
            return json.toString();
        }
        json.put("ret", 1);
        return json.toString();
    }

    @ResponseBody
    @RequestMapping(value = "/uploadBatch", method = RequestMethod.POST, produces = {"text/html;charset=UTF-8;", "application/json;"})
    public String uploadBatch() {
        DocumentMgr dm = new DocumentMgr();
        com.alibaba.fastjson.JSONObject json = new com.alibaba.fastjson.JSONObject();
        ServletContext application = request.getSession().getServletContext();
        boolean re;
        try {
            re = dm.uploadBatch(application, request);
        } catch (ErrMsgException e) {
            json.put("ret", 0);
            json.put("msg", e.getMessage());
            return json.toString();
        }

        if (re) {
            json.put("ret", "1");
            json.put("msg", "操作成功！");
        } else {
            json.put("ret", "0");
            json.put("msg", "操作失败！");
        }
        return json.toString();
    }

    /**
     * 批量通过文档
     *
     * @return
     */
    @ResponseBody
    @RequestMapping(value = "/setOnTop", method = RequestMethod.POST, produces = {"text/html;charset=UTF-8;", "application/json;"})
    public String setOnTop(int id) {
        JSONObject json = new JSONObject();
        try {
            DocumentMgr documentMgr = new DocumentMgr();
            Document doc = documentMgr.getDocument(id);
            com.redmoon.oa.pvg.Privilege privilege = new com.redmoon.oa.pvg.Privilege();
            LeafPriv lp = new LeafPriv(doc.getDirCode());
            if (!lp.canUserExamine(privilege.getUser(request))) {
                json.put("ret", 0);
                json.put("msg", SkinUtil.LoadString(request, "pvg_invalid"));
                return json.toString();
            }
            int level = ParamUtil.getInt(request, "level");
            doc.setLevel(level);
            boolean re = doc.UpdateLevel();
            if (re) {
                json.put("ret", 1);
                json.put("msg", "操作成功！");
            } else {
                json.put("ret", 0);
                json.put("msg", "操作失败！");
            }
        } catch (JSONException e) {
            LogUtil.getLog(getClass()).error(e);
        } catch (ErrMsgException e) {
            try {
                json.put("ret", 0);
                json.put("msg", e.getMessage());
            } catch (JSONException e1) {
                e1.printStackTrace();
            }
        }
        return json.toString();
    }

    /**
     * 批量通过文档
     *
     * @return
     */
    @ResponseBody
    @RequestMapping(value = "/changeDir", method = RequestMethod.POST, produces = {"text/html;charset=UTF-8;", "application/json;"})
    public String changeDir(String dirCode, String newDirCode) {
        JSONObject json = new JSONObject();
        try {
            String strIds = ParamUtil.get(request, "ids");
            String[] ids = StrUtil.split(strIds, ",");
            if (ids == null) {
                json.put("ret", 0);
                json.put("msg", "请选择文件！");
                return json.toString();
            }
            // 检查权限
            LeafPriv lp = new LeafPriv(dirCode);
            com.redmoon.oa.pvg.Privilege privilege = new com.redmoon.oa.pvg.Privilege();
            if (!lp.canUserExamine(privilege.getUser(request))) {
                json.put("ret", 0);
                json.put("msg", SkinUtil.LoadString(request, "pvg_invalid"));
                return json.toString();
            }
            DocumentMgr dm = new DocumentMgr();
            for (int i = 0; i < ids.length; i++) {
                Document doc = dm.getDocument(StrUtil.toInt(ids[i]));
                doc.UpdateDir(newDirCode);
            }

            json.put("ret", 1);
            json.put("msg", "操作成功！");
        } catch (JSONException e) {
            LogUtil.getLog(getClass()).error(e);
        } catch (ErrMsgException e) {
            LogUtil.getLog(getClass()).error(e);
            try {
                json.put("ret", 0);
                json.put("msg", e.getMessage());
            } catch (JSONException e1) {
                e1.printStackTrace();
            }
        }
        return json.toString();
    }

    /**
     * 修改目录权限
     *
     * @return
     */
    @ResponseBody
    @RequestMapping(value = "/modifyDirPriv", method = RequestMethod.POST, produces = {"text/html;charset=UTF-8;", "application/json;"})
    public String modifyDirPriv() {
        JSONObject json = new JSONObject();
        try {
            int id = ParamUtil.getInt(request, "id");
            int see = 0, append = 0, del = 0, modify = 0, examine = 0, download = 0;
            String strsee = ParamUtil.get(request, "see");
            if (StrUtil.isNumeric(strsee)) {
                see = Integer.parseInt(strsee);
            }
            String strappend = ParamUtil.get(request, "append");
            if (StrUtil.isNumeric(strappend)) {
                append = Integer.parseInt(strappend);
            }
            String strmodify = ParamUtil.get(request, "modify");
            if (StrUtil.isNumeric(strmodify)) {
                modify = Integer.parseInt(strmodify);
            }
            String strdel = ParamUtil.get(request, "del");
            if (StrUtil.isNumeric(strdel)) {
                del = Integer.parseInt(strdel);
            }
            String strexamine = ParamUtil.get(request, "examine");
            if (StrUtil.isNumeric(strexamine)) {
                examine = Integer.parseInt(strexamine);
            }
            String strdownload = ParamUtil.get(request, "downLoad");
            if (StrUtil.isNumeric(strdownload)) {
                download = Integer.parseInt(strdownload);
            }

            int exportWord = ParamUtil.getInt(request, "exportWord", 0);
            int exportPdf = ParamUtil.getInt(request, "exportPdf", 0);

            LeafPriv leafPriv = new LeafPriv();
            leafPriv.setId(id);
            leafPriv.setAppend(append);
            leafPriv.setModify(modify);
            leafPriv.setDel(del);
            leafPriv.setSee(see);
            leafPriv.setExamine(examine);
            leafPriv.setDownLoad(download);
            leafPriv.setExportWord(exportWord);
            leafPriv.setExportPdf(exportPdf);
            boolean re = leafPriv.save();
            if (re) {
                json.put("ret", 1);
                json.put("msg", "操作成功！");
            } else {
                json.put("ret", 0);
                json.put("msg", "操作失败！");
            }
        } catch (JSONException e) {
            LogUtil.getLog(getClass()).error(e);
        } catch (ErrMsgException e) {
            LogUtil.getLog(getClass()).error(e);
            try {
                json.put("ret", 0);
                json.put("msg", e.getMessage());
            } catch (JSONException e1) {
                e1.printStackTrace();
            }
        }
        return json.toString();
    }

    /**
     * 添加目录权限
     *
     * @return
     */
    @ResponseBody
    @RequestMapping(value = "/delDirPriv", method = RequestMethod.POST, produces = {"text/html;charset=UTF-8;"})
    public String delDirPriv() {
        JSONObject json = new JSONObject();
        try {
            int id = ParamUtil.getInt(request, "id");
            LeafPriv lp = new LeafPriv();
            lp = lp.getLeafPriv(id);
            boolean re = lp.del();
            if (re) {
                json.put("ret", 1);
                json.put("msg", "操作成功！");
            } else {
                json.put("ret", 0);
                json.put("msg", "操作失败！");
            }
        } catch (JSONException e) {
            LogUtil.getLog(getClass()).error(e);
        } catch (ErrMsgException e) {
            LogUtil.getLog(getClass()).error(e);
            try {
                json.put("ret", 0);
                json.put("msg", e.getMessage());
            } catch (JSONException e1) {
                e1.printStackTrace();
            }
        }
        return json.toString();
    }

    /**
     * 置部门、角色、用户组、用户权限，如有则保留，如无则添加，如未选中则删除
     *
     * @param dirCode
     * @param deptRoleGroupUser
     * @return
     */
    @ResponseBody
    @RequestMapping(value = "/setDeptRoleGroupUser", method = RequestMethod.POST, produces = {"text/html;charset=UTF-8;"})
    public String setDeptRoleGroupUser(String dirCode, String deptRoleGroupUser) {
        JSONObject json = new JSONObject();
        boolean re = true;
        try {
            if (deptRoleGroupUser != null && !"".equals(deptRoleGroupUser)) {
                // 清空原来的浏览权限，而具有其它权限的不删除，这些有可能是管理员设的
                LeafPriv leafPriv = new LeafPriv();
                String sql = leafPriv.getListSqlForDir(dirCode, "name", "asc");
                Vector v = leafPriv.list(sql);

                com.alibaba.fastjson.JSONArray arr = com.alibaba.fastjson.JSONArray.parseArray(deptRoleGroupUser);
                // 删除没被选择的权限
                Iterator<LeafPriv> ir = v.iterator();
                while (ir.hasNext()) {
                    leafPriv = ir.next();
                    boolean isFound = false;
                    for (int i = 0; i < arr.size(); i++) {
                        com.alibaba.fastjson.JSONObject jsonObject = arr.getJSONObject(i);
                        String code = jsonObject.getString("code");
                        String kind = jsonObject.getString("kind");
                        int type = StrUtil.toInt(kind, DocPriv.TYPE_DEPT);
                        if (leafPriv.getName().equals(code) && leafPriv.getType() == type) {
                            isFound = true;
                            break;
                        }
                    }
                    if (!isFound) {
                        leafPriv.del();
                        ir.remove();
                    }
                }

                for (int i = 0; i < arr.size(); i++) {
                    com.alibaba.fastjson.JSONObject jsonObject = arr.getJSONObject(i);
                    String code = jsonObject.getString("code");
                    String kind = jsonObject.getString("kind");
                    int type = StrUtil.toInt(kind, DocPriv.TYPE_DEPT);
                    // 判断原来是否已存在该权限
                    boolean isFound = false;
                    ir = v.iterator();
                    while (ir.hasNext()) {
                        leafPriv = ir.next();
                        if (leafPriv.getName().equals(code) && leafPriv.getType() == type) {
                            isFound = true;
                            break;
                        }
                    }

                    if (!isFound) {
                        LeafPriv lp = new LeafPriv(dirCode);
                        re = lp.add(code, type);
                    }
                }
            }

            if (re) {
                json.put("ret", 1);
                json.put("msg", "操作成功！");
            } else {
                json.put("ret", 0);
                json.put("msg", "操作失败！");
            }
        } catch (JSONException e) {
            LogUtil.getLog(getClass()).error(e);
        } catch (ErrMsgException e) {
            LogUtil.getLog(getClass()).error(e);
            try {
                json.put("ret", 0);
                json.put("msg", e.getMessage());
            } catch (JSONException e1) {
                e1.printStackTrace();
            }
        }
        return json.toString();
    }

    /**
     * 从回收站中彻底删除
     *
     * @return
     */
    @ResponseBody
    @RequestMapping(value = "/delBatchFromDustbin", method = RequestMethod.POST, produces = {"text/html;charset=UTF-8;"})
    public String delBatchFromDustbin(Integer id) throws ErrMsgException {
        JSONObject json = new JSONObject();
        try {
            DocumentMgr documentMgr = new DocumentMgr();
            boolean re = documentMgr.delBatch(request, false);
            if (re) {
                json.put("ret", 1);
                json.put("msg", "操作成功！");
            } else {
                json.put("ret", 0);
                json.put("msg", "操作失败！");
            }
        } catch (JSONException e) {
            LogUtil.getLog(getClass()).error(e);
        } catch (ErrMsgException e) {
            LogUtil.getLog(getClass()).error(e);
            try {
                json.put("ret", 0);
                json.put("msg", e.getMessage());
            } catch (JSONException e1) {
                e1.printStackTrace();
            }
        }
        return json.toString();
    }

    /**
     * 从回收站中恢复
     *
     * @return
     */
    @ResponseBody
    @RequestMapping(value = "/resume", method = RequestMethod.POST, produces = {"text/html;charset=UTF-8;"})
    public String resume() throws ErrMsgException {
        JSONObject json = new JSONObject();
        try {
            DocumentMgr documentMgr = new DocumentMgr();
            boolean re = documentMgr.resumeBatch(request);
            if (re) {
                json.put("ret", 1);
                json.put("msg", "操作成功！");
            } else {
                json.put("ret", 0);
                json.put("msg", "操作失败！");
            }
        } catch (JSONException e) {
            LogUtil.getLog(getClass()).error(e);
        } catch (ErrMsgException e) {
            LogUtil.getLog(getClass()).error(e);
            try {
                json.put("ret", 0);
                json.put("msg", e.getMessage());
            } catch (JSONException e1) {
                e1.printStackTrace();
            }
        }
        return json.toString();
    }

    /**
     * 清空回收站
     *
     * @return
     */
    @ResponseBody
    @RequestMapping(value = "/cleanUpDustbin", method = RequestMethod.POST, produces = {"text/html;charset=UTF-8;"})
    public String cleanUpDustbin() {
        com.alibaba.fastjson.JSONObject json = new com.alibaba.fastjson.JSONObject();
        try {
            DocumentMgr documentMgr = new DocumentMgr();
            documentMgr.clearDustbin(request);
        } catch (ErrMsgException e) {
            LogUtil.getLog(getClass()).error(e);
            json.put("ret", 0);
            json.put("msg", e.getMessage());
        }

        json.put("ret", 1);
        json.put("msg", "操作成功！");
        return json.toString();
    }

    /**
     * 下载文件或显示图片（如果为图片文件时）
     *
     * @param response
     * @throws IOException
     * @throws ErrMsgException
     * @throws JSONException
     */
    @RequestMapping("/download")
    public void download(HttpServletResponse response) throws IOException, ErrMsgException {
        BufferedOutputStream bos = null;
        try {
            bos = new BufferedOutputStream(response.getOutputStream());
            int id = ParamUtil.getInt(request, "id", -1);
            int attId = ParamUtil.getInt(request, "attachId");

            int pageNum = 1;
            String pn = ParamUtil.get(request, "pageNum");
            if (StrUtil.isNumeric(pn)) {
                pageNum = Integer.parseInt(pn);
            }

            Attachment att = null;
            Document mmd = new Document();
            if (id == -1) {
                att = new Attachment(attId);
                mmd = mmd.getDocument(att.getDocId());
            } else {
                mmd = mmd.getDocument(id);
                att = mmd.getAttachment(pageNum, attId);
            }

            com.redmoon.oa.pvg.Privilege privilege = new com.redmoon.oa.pvg.Privilege();
            String uName = privilege.getUser(request);
            String ip = IPUtil.getRemoteAddr(request);
            java.util.Date logDate = new java.util.Date();
            DocLogDb dld = new DocLogDb();
            DocAttachmentLogDb dad = new DocAttachmentLogDb();
            // 记录访问日志
            Leaf lf = new Leaf();
            lf = lf.getLeaf(mmd.getDirCode());
            if (lf == null) {
                response.setContentType("text/html;charset=utf-8");
                bos.write("目录不存在".getBytes(StandardCharsets.UTF_8));
                return;
            }
            if (lf.isLog()) {
                dad.setUserName(uName);
                dad.setAtt_id(attId);
                dad.setIp(ip);
                dad.setLogDate(logDate);
                dad.setDoc_id(id);
                dad.create();

                dld.setUserName(uName);
                dld.setDoc_id(id);
                dld.setIp(ip);
                dld.setLogDate(logDate);
                dld.create();
            }
            att.setDownloadCount(att.getDownloadCount() + 1);
            att.save();

            if (StrUtil.isImage(StrUtil.getFileExt(att.getDiskName()))) {
                /* 因为不支持<img src='...'/>，故注释掉
                response.setContentType("text/html;charset=utf-8");
                String str = "<img src=\"" + request.getContextPath() + "/showImg.do?path=" + att.getVisualPath() + "/" + att.getDiskName() + "\" />";
                bos.write(str.getBytes(StandardCharsets.UTF_8));
                */
                try {
                    Directory.onDownload(request, String.valueOf(attId), uName, false);
                } catch (ErrMsgException e) {
                    bos.write(e.getMessage().getBytes(StandardCharsets.UTF_8));
                    return;
                }
                fileService.preview(response, att.getVisualPath() + "/" + att.getDiskName());
                return;
            }

            fileService.download(response, att.getName(), att.getVisualPath(), att.getDiskName());
            try {
                Directory.onDownload(request, String.valueOf(attId), uName, false);
            } catch (ErrMsgException e) {
                bos.write(e.getMessage().getBytes(StandardCharsets.UTF_8));
            }
        } catch (final IOException e) {
            LogUtil.getLog(getClass()).error(e);
        } finally {
            if (bos != null) {
                bos.close();
            }
        }
    }

    @RequestMapping("/exportWord")
    public void exportWord(HttpServletResponse response, String id) throws IOException, ErrMsgException {
        BufferedOutputStream bos = new BufferedOutputStream(response.getOutputStream());
        try {
            int docId = -1;
            if (StrUtil.isNumeric(id)) {
                docId = StrUtil.toInt(id, -1);
            } else {
                Leaf lf = new Leaf();
                lf = lf.getLeaf(id);
                if (lf == null) {
                    response.setContentType("text/html;charset=utf-8");
                    bos.write(("目录" + id + "不存在").getBytes("utf-8"));
                    return;
                }

                docId = lf.getDocID();
                if (docId == Leaf.DOC_ID_NONE) {
                    response.setContentType("text/html;charset=utf-8");
                    bos.write(("文档" + id + "不存在").getBytes("utf-8"));
                    return;
                }
            }

            Document doc = new Document();
            doc = doc.getDocument(docId);
            if (!doc.isLoaded()) {
                response.setContentType("text/html;charset=utf-8");
                bos.write("文档不存在".getBytes("utf-8"));
                return;
            }

            LeafPriv lp = new LeafPriv(doc.getDirCode());
            if (!lp.canUserExportWord(SpringUtil.getUserName())) {
                response.setContentType("text/html;charset=utf-8");
                bos.write("权限非法".getBytes("utf-8"));
                return;
            }

            String content = doc.getContent(1);

            response.setHeader("Content-disposition", "attachment; filename=" + StrUtil.GBToUnicode(doc.getTitle()) + ".doc");
            response.setContentType("application/msword; charset=gb2312");

            // 将图片转为绝对链接
            String contentTmp = content;
            Parser parser;
            try {
                parser = new Parser(content);
                parser.setEncoding("utf-8");//
                TagNameFilter filter = new TagNameFilter("img");
                NodeList nodes = parser.parse(filter);
                if (nodes != null || nodes.size() > 0) {
                    StringBuffer sb = new StringBuffer();
                    int lastNodeEnd = 0;
                    for (int k = 0; k < nodes.size(); k++) {
                        ImageTag node = (ImageTag) nodes.elementAt(k);

                        // image/png;base64,
                        String imgUrl = node.getImageURL();
                        int p = imgUrl.indexOf("http");
                        if (p == -1) {
                            if (imgUrl.startsWith("/")) {
                                imgUrl = Global.getFullRootPath(request) + imgUrl;
                            } else {
                                imgUrl = Global.getFullRootPath(request) + "/" + imgUrl;
                            }

                            node.setImageURL(imgUrl);
                            int s = node.getStartPosition();
                            int e = node.getEndPosition();
                            String c = contentTmp.substring(lastNodeEnd, s);
                            c += node.toHtml();
                            sb.append(c);
                            lastNodeEnd = e;
                        } else {
                            int e = node.getEndPosition();
                            String c = contentTmp.substring(lastNodeEnd, e);
                            sb.append(c);
                            lastNodeEnd = e;
                        }
                    }
                    sb.append(StringUtils.substring(contentTmp, lastNodeEnd));
                    content = sb.toString();
                }

            } catch (ParserException e) {
                LogUtil.getLog(getClass()).error(e);
            }

            WordUtil.htmlToWord(content, bos);
        } catch (final Exception e) {
            LogUtil.getLog(getClass()).error(e);
        } finally {
            if (bos != null) {
                bos.close();
            }
        }
    }

    @RequestMapping("/exportPdf")
    public void exportPdf(HttpServletResponse response, String id) throws IOException, ErrMsgException {
        BufferedOutputStream bos = null;
        try {
            int docId = -1;
            if (StrUtil.isNumeric(id)) {
                docId = StrUtil.toInt(id);

            } else {
                Leaf lf = new Leaf();
                lf = lf.getLeaf(id);
                if (lf == null) {
                    response.setContentType("text/html;charset=utf-8");
                    bos = new BufferedOutputStream(response.getOutputStream());
                    bos.write(("目录" + id + "不存在").getBytes(StandardCharsets.UTF_8));
                    return;
                }

                docId = lf.getDocID();
                if (docId == Leaf.DOC_ID_NONE) {
                    response.setContentType("text/html;charset=utf-8");
                    bos = new BufferedOutputStream(response.getOutputStream());
                    bos.write(("文档" + id + "不存在").getBytes(StandardCharsets.UTF_8));
                    return;
                }
            }

            Document doc = new Document();
            doc = doc.getDocument(docId);
            if (!doc.isLoaded()) {
                response.setContentType("text/html;charset=utf-8");
                bos = new BufferedOutputStream(response.getOutputStream());
                bos.write("文章不存在".getBytes(StandardCharsets.UTF_8));
                return;
            }

            LeafPriv lp = new LeafPriv(doc.getDirCode());
            if (!lp.canUserExportWord(SpringUtil.getUserName())) {
                response.setContentType("text/html;charset=utf-8");
                bos = new BufferedOutputStream(response.getOutputStream());
                bos.write("权限非法".getBytes(StandardCharsets.UTF_8));
                return;
            }

            String content = doc.getContent(1);

            response.setHeader("Content-disposition", "attachment; filename=" + StrUtil.GBToUnicode(doc.getTitle()) + ".pdf");
            response.setContentType("application/pdf");

            PdfUtil.htmlToPdf(response, doc.getTitle(), content, doc.getAuthor());
        } catch (final Exception e) {
            LogUtil.getLog(getClass()).error(e);
        } finally {
            if (bos != null) {
                bos.close();
            }
        }
    }

    /**
     * 设置或取消标题图片
     *
     * @return
     */
    @ResponseBody
    @RequestMapping(value = "/changeTitleImage", method = RequestMethod.POST, produces = {"text/html;charset=UTF-8;"})
    public String changeTitleImage(Integer attachId, Boolean isTitleImage) {
        com.alibaba.fastjson.JSONObject json = new com.alibaba.fastjson.JSONObject();

        com.redmoon.oa.fileark.Attachment att = new com.redmoon.oa.fileark.Attachment(attachId);
        if (!att.isLoaded()) {
            json.put("res", "0");
            json.put("msg", "文件不存在");
            return json.toString();
        }

        int docId = att.getDocId();
        Document doc = new Document();
        doc = doc.getDocument(docId);

        com.redmoon.oa.pvg.Privilege privilege = new com.redmoon.oa.pvg.Privilege();
        com.redmoon.oa.Config cfg = new com.redmoon.oa.Config();
        LeafPriv lp = new LeafPriv(doc.getDirCode());
        if (!lp.canUserModify(privilege.getUser(request))) {
            if (doc.getExamine() == Document.EXAMINE_NOT) {
                if (!lp.canUserModify(privilege.getUser(request))) {
                    json.put("res", "0");
                    json.put("msg", SkinUtil.LoadString(request, "文章正在审核中，不能编辑"));
                    return json.toString();
                }
            }

            // 判断是否本人编辑自己的文章
            boolean canModify = false;
            if (doc.getAuthor().equals(privilege.getUser(request))) {
                double filearkUserDelIntervalH = StrUtil.toDouble(cfg.get("filearkUserEditDelInterval"), 0);
                double intervalMinute = filearkUserDelIntervalH * 60;
                if (DateUtil.datediffMinute(new Date(), doc.getCreateDate()) < intervalMinute) {
                    canModify = true;
                } else {
                    json.put("res", "0");
                    json.put("msg", SkinUtil.LoadString(request, "已超时，发布后" + filearkUserDelIntervalH + "小时内可修改"));
                    return json.toString();
                }
            }
            if (!canModify) {
                json.put("res", "0");
                json.put("msg", SkinUtil.LoadString(request, "pvg_invalid"));
                return json.toString();
            }
        }

        att.setTitleImage(isTitleImage);
        boolean re = att.save();
        if (re) {
            json.put("ret", 1);
            json.put("msg", "操作成功！");
        } else {
            json.put("ret", 0);
            json.put("msg", "操作失败！");
        }
        return json.toString();
    }

    @RequestMapping("/getFile")
    public void getFile(HttpServletResponse response, @RequestParam(required = true) Integer docId, @RequestParam(required = true) Integer attId) throws IOException, ErrMsgException {
        Document doc = new Document();
        doc = doc.getDocument(docId);
        Attachment att = doc.getAttachment(1, attId);
        if (!att.isLoaded()) {
            throw new ErrMsgException("文件不存在");
        }
        String op = ParamUtil.get(request, "op");
        if ("preview".equals(op)) {
            fileService.download(response, att.getName(), att.getVisualPath(), att.getDiskName());
        } else {
            fileService.preview(response, att.getVisualPath() + "/" + att.getDiskName());
        }
    }

    /**
     * 预览
     *
     * @param docId
     * @param attId
     * @param model
     * @return
     */
    @RequestMapping(value = "/preview")
    public String preview(@RequestParam(required = true) Integer docId, @RequestParam(required = true) Integer attId, Model model) {
        Attachment att = new Attachment(attId);
        if (!att.isLoaded()) {
            model.addAttribute("msg", "文件不存在，docId=" + attId + " id=" + attId);
            return "th/error/error";
        }

        if (StrUtil.isImage(StrUtil.getFileExt(att.getDiskName()))) {
            // 下载记录存至日志
            model.addAttribute("imgPath", att.getVisualPath() + "/" + att.getDiskName());
            return "th/img_show";
        } else {
            return "forward:getFile.do?op=preview&docId=" + docId + "&attId=" + attId;
        }
    }

    @ResponseBody
    @RequestMapping(value = "/getImgForShow", method = RequestMethod.POST, produces = {"text/html;charset=UTF-8;", "application/json;"})
    public String getImgForShow(@RequestParam(required = true) Integer docId, @RequestParam(required = true) Integer attId) {
        com.alibaba.fastjson.JSONObject json = new com.alibaba.fastjson.JSONObject();
        try {
            Attachment att = new Attachment(attId);
            String realPath = Global.getRealPath() + att.getVisualPath() + "/" + att.getDiskName();
            File input = new File(realPath);
            BufferedImage image = ImageIO.read(input);
            if (image == null) {
                json.put("ret", 0);
                json.put("msg", "图片不存在！");
            } else {
                int w = image.getWidth();
                int h = image.getHeight();
                json.put("ret", 1);
                json.put("width", w);
                json.put("height", h);
                json.put("downloadUrl", "fileark/getFile.do?docId=" + docId + "&attId=" + attId);
            }
        } catch (IOException e) {
            json.put("ret", 0);
            json.put("msg", e.getMessage());
        }
        return json.toString();
    }

    @ResponseBody
    @RequestMapping(value = "/getNextImgForShow", method = RequestMethod.POST, produces = {"text/html;charset=UTF-8;", "application/json;"})
    public String getNextImgForShow(@RequestParam(required = true) Integer docId, @RequestParam(required = true) Integer attId) {
        com.alibaba.fastjson.JSONObject json = new com.alibaba.fastjson.JSONObject();
        String arrow = ParamUtil.get(request, "arrow");//判断显示上一张还是下一张图片
        int isImgSearch = ParamUtil.getInt(request, "isImgSearch", 0);
        int newId = 0;
        try {
            Attachment att = new Attachment();
            newId = att.showNextImg(attId, docId, arrow, isImgSearch);
            Attachment attNew = new Attachment(newId);
            String realPath = Global.getRealPath() + attNew.getVisualPath() + "/" + attNew.getDiskName();
            File input = new File(realPath);
            BufferedImage image = ImageIO.read(input);
            if (image == null) {
                json.put("ret", 0);
                json.put("msg", "图片不存在！");
            } else {
                int w = image.getWidth();
                int h = image.getHeight();
                json.put("ret", 1);
                json.put("newId", newId);
                json.put("width", w);
                json.put("height", h);
                json.put("downloadUrl", "fileark/getFile.do?docId=" + docId + "&attId=" + newId);
            }
        } catch (IOException e) {
            json.put("ret", 0);
            json.put("msg", e.getMessage());
        }
        return json.toString();
    }

    @RequestMapping("/zipFile")
    public void zipFile(HttpServletResponse response) throws IOException, ErrMsgException {
        String ids = ParamUtil.get(request, "ids");
        String fileDiskPath = "";
        String realPath = Global.getRealPath() + "upfile/zip";
        int id = -1;
        if ("".equals(ids)) {
            id = ParamUtil.getInt(request, "id", -1);
            if (id == -1) {
                throw new ErrMsgException("文章不存在！");
            }
        }

        com.redmoon.oa.pvg.Privilege pvg = new com.redmoon.oa.pvg.Privilege();
        boolean isZip = true;
        String attIds = ids;
        if (id != -1) {
            attIds = String.valueOf(id);
        }
        Directory.onDownloadValidate(request, attIds, pvg.getUser(request), isZip);

        try {
            if (id != -1) {
                fileDiskPath = FileBakUp.zipDocFiles(id, realPath);

            } else {
                fileDiskPath = FileBakUp.zipDocsFiles(ids, realPath);
            }
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }

        Directory.onDownload(request, attIds, pvg.getUser(request), isZip);

        // response.setContentType(MIMEMap.get(StrUtil.getFileExt(att.getDiskName())));
        response.setContentType("application/octet-stream");
        response.setHeader("Content-disposition", "attachment; filename=" + StrUtil.GBToUnicode("文档下载.zip"));

        BufferedInputStream bis = null;
        BufferedOutputStream bos = null;

        try {
            bis = new BufferedInputStream(new FileInputStream(fileDiskPath));
            bos = new BufferedOutputStream(response.getOutputStream());

            byte[] buff = new byte[2048];
            int bytesRead;

            while (-1 != (bytesRead = bis.read(buff, 0, buff.length))) {
                bos.write(buff, 0, bytesRead);
            }
        } catch (final IOException e) {
            LogUtil.getLog(getClass()).error(e);
        } finally {
            if (bis != null) {
                bis.close();
            }
            if (bos != null) {
                bos.close();
            }
        }
    }

    @RequestMapping("/docListPage")
    public String docListPage(Model model, String curDeptCode) {
        String skinPath = SkinMgr.getSkinPath(request, false);
        model.addAttribute("skinPath", skinPath);

        com.redmoon.oa.pvg.Privilege privilege = new com.redmoon.oa.pvg.Privilege();
        String dirCode = ParamUtil.get(request, "dirCode"); // 从目录树上点击的目录

        int examine1 = ParamUtil.getInt(request, "examine1", -1);
        String kind = ParamUtil.get(request, "kind");

        Leaf fileLeaf = new Leaf();
        String filePath = fileLeaf.getFilePath();

        String orderBy = ParamUtil.get(request, "orderBy");
        if ("".equals(orderBy)) {
            orderBy = "id"; // "createDate";
        }
        String sort = ParamUtil.get(request, "sort");
        if ("".equals(sort)) {
            sort = "desc";
        }

        UserSetupDb usd = new UserSetupDb();
        usd = usd.getUserSetupDb(privilege.getUser(request));
        //String pageUrl = usd.isWebedit()?"fwebedit.jsp":"fwebedit_new.jsp";
        String pageUrl = "fwebedit_new.jsp";

        //swfUpload文件上传
        com.redmoon.oa.Config cfg = com.redmoon.oa.Config.getInstance();
        HashMap<String, String> explorerFileType = new HashMap<String, String>();
        String file_size_limit = cfg.get("file_size_limit");
        int file_upload_limit = cfg.getInt("file_upload_limit");

        model.addAttribute("orderBy", orderBy);
        model.addAttribute("sort", sort);
        model.addAttribute("dirCode", dirCode);
        model.addAttribute("pageUrl", pageUrl);

        if (!privilege.isUserPrivValid(request, PrivDb.PRIV_READ)) {
            model.addAttribute("msg", i18nUtil.get("pvg_invalid"));
            return "th/error/error";
        }

        // 如果dir_code为空，则需检查权限
        LeafPriv lp = new LeafPriv();
        if ("".equals(dirCode) && examine1 != Document.EXAMINE_DRAFT) {
            lp.setDirCode(Leaf.ROOTCODE);
            // 如果是管理员或者文章根目录节点上有审核的权限，则允许查看全部的文章列表
            if (privilege.isUserPrivValid(request, "admin") || lp.canUserExamine(privilege.getUser(request))) {
                ;
            } else {
                model.addAttribute("msg", "请选择目录");
                return "th/error/info";
            }
        }

        Directory dir = new Directory();
        Leaf leaf = null;
        if (!"".equals(dirCode) && !Leaf.CODE_DRAFT.equals(dirCode)) {
            leaf = dir.getLeaf(dirCode);
        }
        String viewPage = "";
        if (!"".equals(dirCode) && !Leaf.CODE_DRAFT.equals(dirCode)) {
            if (leaf == null) {
                model.addAttribute("msg", "目录" + dirCode + "不存在");
                return "th/error/error";
            }

            lp.setDirCode(dirCode);
            if (!lp.canUserSee(privilege.getUser(request))) {
                model.addAttribute("msg", "权限不足");
                return "th/error/error";
            }

            PluginMgr pm = new PluginMgr();
            PluginUnit pu = pm.getPluginUnitOfDir(dirCode);

            if (pu != null) {
                IPluginUI ipu = pu.getUI(request);
                viewPage = request.getContextPath() + "/" + ipu.getViewPage();
            }
        }
        if ("".equals(viewPage)) {
            viewPage = request.getContextPath() + "/doc_show.jsp";
        }

        String dirName = "";
        if (leaf != null) {
            dirName = leaf.getName();
        }
        model.addAttribute("dirName", dirName);

        String parentCode = ParamUtil.get(request, "parentCode");
        String uName = privilege.getUser(request);
        LeafPriv plp = null;
        if (!"".equals(parentCode)) {
            plp = new LeafPriv(parentCode);
        }

        model.addAttribute("kind", kind);
        DirKindDb dkd = new DirKindDb();
        Vector vkind = dkd.listOfDir(dirCode);
        SelectOptionDb sod = new SelectOptionDb();
        com.alibaba.fastjson.JSONArray aryKind = new com.alibaba.fastjson.JSONArray();
        for (Object o : vkind) {
            DirKindDb dirKindDb = (DirKindDb) o;
            com.alibaba.fastjson.JSONObject json = new com.alibaba.fastjson.JSONObject();
            json.put("kind", dirKindDb.getKind());
            json.put("kindName", sod.getOptionName("fileark_kind", dkd.getKind()));
            aryKind.add(json);
        }
        model.addAttribute("aryKind", aryKind);

        boolean canExamine = false;
        boolean isDraftBox = examine1 == Document.EXAMINE_DRAFT;
        if (!isDraftBox) {
            if (privilege.isUserPrivValid(request, "admin") || lp.canUserModify(privilege.getUser(request))) {
                canExamine = true;
            }
        }
        model.addAttribute("examine1", examine1);
        model.addAttribute("isDraftBox", isDraftBox);
        model.addAttribute("canExamine", canExamine);
        model.addAttribute("EXAMINE_NOT", Document.EXAMINE_NOT);
        model.addAttribute("EXAMINE_PASS", Document.EXAMINE_PASS);
        model.addAttribute("EXAMINE_NOTPASS", Document.EXAMINE_NOTPASS);
        model.addAttribute("EXAMINE_DRAFT", Document.EXAMINE_DRAFT);
        model.addAttribute("isRoot", "".equals(dirCode));

        com.alibaba.fastjson.JSONArray aryFilearkKind = new com.alibaba.fastjson.JSONArray();
        SelectMgr sm = new SelectMgr();
        SelectDb sd = sm.getSelect("fileark_kind");
        Vector<SelectOptionDb> vsd = sd.getOptions();
        for (SelectOptionDb selectOptionDb : vsd) {
            sod = selectOptionDb;
            com.alibaba.fastjson.JSONObject json = new com.alibaba.fastjson.JSONObject();
            json.put("value", sod.getValue());
            json.put("name", sod.getName());
            aryFilearkKind.add(json);
        }
        model.addAttribute("aryFilearkKind", aryFilearkKind);

        model.addAttribute("parentCode", parentCode);

        boolean isBtnAddShow = false;
        if (!dirCode.equals("") && !Leaf.CODE_DRAFT.equals(dirCode) && leaf.getType() == 2) {
            if (lp.canUserAppend(privilege.getUser(request))) {
                isBtnAddShow = true;
            }
        }
        model.addAttribute("isBtnAddShow", isBtnAddShow);

        boolean isBtnEditShow = false;
        int filearkUserEditDelInterval = cfg.getInt("filearkUserEditDelInterval");
        if (isDraftBox || lp.canUserModify(privilege.getUser(request)) || (lp.canUserAppend(privilege.getUser(request)) && filearkUserEditDelInterval > 0)) {
            isBtnEditShow = true;
        }
        model.addAttribute("isBtnEditShow", isBtnEditShow);

        boolean isBtnDelShow = false;
        if (examine1 == Document.EXAMINE_DRAFT || lp.canUserDel(privilege.getUser(request)) || (lp.canUserAppend(privilege.getUser(request)) && filearkUserEditDelInterval > 0)) {
            isBtnDelShow = true;
        }
        model.addAttribute("isBtnDelShow", isBtnDelShow);

        boolean isBtnZipShow = false;
        if (isDraftBox || lp.canUserDownLoad(privilege.getUser(request))) {
            isBtnZipShow = true;
        }
        model.addAttribute("isBtnZipShow", isBtnZipShow);

        boolean isBtnMoveShow = false;
        if (!"".equals(dirCode) && !Leaf.CODE_DRAFT.equals(dirCode) && (privilege.isUserPrivValid(request, "admin") || lp.canUserExamine(privilege.getUser(request)))) {
            isBtnMoveShow = true;
        }
        model.addAttribute("isBtnMoveShow", isBtnMoveShow);

        boolean isBtnUploadShow = false;
        if (!"".equals(dirCode) && !Leaf.CODE_DRAFT.equals(dirCode) && leaf.getType() == Leaf.TYPE_LIST) {
            if (lp.canUserAppend(privilege.getUser(request))) {
                isBtnUploadShow = true;
            }
        }
        model.addAttribute("isBtnUploadShow", isBtnUploadShow);

        boolean isBtnFulltextSearch = false;
        if (cfg.getBooleanProperty("fullTextSearchSupported")) {
            isBtnFulltextSearch = true;
        }
        model.addAttribute("isBtnFulltextSearch", isBtnFulltextSearch);

        boolean isBtnAllShow = false;
        if (leaf != null && leaf.getChildCount() > 0) {
            isBtnAllShow = true;
        }
        model.addAttribute("isBtnAllShow", isBtnAllShow);

        return "th/fileark/doc_list";
    }

    @ResponseBody
    @RequestMapping(value = "/docList", method = RequestMethod.POST, produces = {"text/html;charset=UTF-8;", "application/json;charset=UTF-8;"})
    public String docList(@RequestParam(defaultValue = "") String dirCode) throws ErrMsgException {
        com.redmoon.oa.pvg.Privilege privilege = new com.redmoon.oa.pvg.Privilege();
        String myUserName = privilege.getUser(request);
        Document doc = new Document();

        int examine1 = ParamUtil.getInt(request, "examine1", -1);
        // 如果dir_code为空，则需检查权限
        LeafPriv lp = new LeafPriv();
        if ("".equals(dirCode) && examine1 != Document.EXAMINE_DRAFT) {
            lp.setDirCode(Leaf.ROOTCODE);
        }
        else if (!StrUtil.isEmpty(dirCode)) {
            lp.setDirCode(dirCode);
        }

        String parentCode = ParamUtil.get(request, "parentCode");
        String uName = privilege.getUser(request);
        LeafPriv plp = null;
        if (!"".equals(parentCode)) {
            plp = new LeafPriv(parentCode);
        }

        String op = StrUtil.getNullString(request.getParameter("op"));
        String searchKind = ParamUtil.get(request, "searchKind");
        String what = ParamUtil.get(request, "what");
        int examine = ParamUtil.getInt(request, "examine", -1);
        String kind = ParamUtil.get(request, "kind");
        String kind1 = ParamUtil.get(request, "kind1");
        String title = ParamUtil.get(request, "title");
        String content = ParamUtil.get(request, "content");
        String modifyType = ParamUtil.get(request, "dateType");
        String docSize = ParamUtil.get(request, "docSize");
        String fromDate = ParamUtil.get(request, "fromDate");
        String toDate = ParamUtil.get(request, "toDate");
        String keywords1 = ParamUtil.get(request, "keywords1");
        String author = ParamUtil.get(request, "author");
        String ext = ParamUtil.get(request, "ext");
        String checkbox_png = ParamUtil.get(request, "checkbox_png");
        String checkbox_ppt = ParamUtil.get(request, "checkbox_ppt");
        String checkbox_gif = ParamUtil.get(request, "checkbox_gif");
        String checkbox_zip = ParamUtil.get(request, "checkbox_zip");
        String checkbox_pdf = ParamUtil.get(request, "checkbox_pdf");
        String checkbox_doc = ParamUtil.get(request, "checkbox_doc");
        String checkbox_xlsx = ParamUtil.get(request, "checkbox_xlsx");
        String checkbox_txt = ParamUtil.get(request, "checkbox_txt");

        String orderBy = ParamUtil.get(request, "orderBy");
        if ("".equals(orderBy)) {
            orderBy = "id";
        }
        String sort = ParamUtil.get(request, "sort");
        if ("".equals(sort)) {
            sort = "desc";
        }

        String sql = doc.getListSql(request, privilege, lp, plp, uName, examine1, checkbox_png, checkbox_ppt, checkbox_gif,
                checkbox_zip, checkbox_pdf, checkbox_doc, checkbox_xlsx, checkbox_txt,
                ext, docSize, parentCode, dirCode, kind, op, searchKind, what, keywords1,
                fromDate, toDate, examine, title, content, author, kind1, modifyType, orderBy, sort);
        DebugUtil.i(getClass(), "docList", sql);

        boolean canExamine = false;
        if (examine1!=Document.EXAMINE_DRAFT) {
            canExamine = lp.canUserExamine(privilege.getUser(request));
        }
        DocPriv dp = new DocPriv();
        boolean isDraftBox = examine1 == Document.EXAMINE_DRAFT;
        Leaf clf = new Leaf();
        Directory dir = new Directory();
        UserDb ud = new UserDb();

        SelectOptionDb sod = new SelectOptionDb();
        com.alibaba.fastjson.JSONArray aryDoc = new com.alibaba.fastjson.JSONArray();
        int curpage = ParamUtil.getInt(request, "page", 1);
        int pagesize = ParamUtil.getInt(request, "limit", 20);
        PluginMgr pm = new PluginMgr();

        ListResult lr = doc.listResult(sql, curpage, pagesize);
        Vector<Document> v = lr.getResult();
        for (Document d : v) {
            doc = d;

            String color = doc.getColor();
            boolean isBold = doc.isBold();
            java.util.Date expireDate = doc.getExpireDate();
            int docId = doc.getId();
            String docTitle = doc.getTitle();

            // 判断是否有浏览文件的权限
            // 如果不是草稿箱
            if (examine1 != Document.EXAMINE_DRAFT) {
                // 如果不是作者
                if (!uName.equals(doc.getAuthor())) {
                    if (!canExamine) {
                        if (!dp.canUserSee(request, doc.getId())) {
                            continue;
                        }
                    }
                }
            }

            boolean canDownload = isDraftBox || (lp.canUserDownLoad(privilege.getUser(request)) && dp.canUserDownload(privilege.getUser(request), docId));

            long attId = -1;

            com.alibaba.fastjson.JSONObject json = new com.alibaba.fastjson.JSONObject();
            clf = clf.getLeaf(doc.getDirCode());
            json.put("dirName", clf.getName());
            json.put("icon", "<img src=\"images/" + doc.getFileIcon() + "\" class=\"file-icon\"/>");

            String titlePrefix = "";
            if (!"".equals(doc.getKind())) {
                titlePrefix = "<a href=\"docListPage.do?dirCode=" + doc.getDirCode() + "&kind=" + doc.getKind() + "\">[&nbsp;" + sod.getOptionName("fileark_kind", doc.getKind()) + "&nbsp;]</a>";
            }

            String viewPage = "";
            if (!doc.getDirCode().equals("") && !Leaf.CODE_DRAFT.equals(doc.getDirCode())) {
                PluginUnit pu = pm.getPluginUnitOfDir(doc.getDirCode());
                if (pu != null) {
                    IPluginUI ipu = pu.getUI(request);
                    viewPage = request.getContextPath() + "/" + ipu.getViewPage();
                }
            }
            if (viewPage.equals("")) {
                viewPage = request.getContextPath() + "/doc_show.jsp";
            }

            String myTitle = docTitle;
            if (DateUtil.compare(new java.util.Date(), expireDate) == 2) {
                myTitle = "<a href=\"javascript:\" linkType=\"view\" title=\"ID：" + doc.getId() + "\" data-id=\"" + doc.getId() + "\" viewPage=\"" + viewPage + "?id=" + doc.getId() + "\" doc-title=\"" + doc.getTitle() + "\">";
                if (isBold) {
                    myTitle += "<B>";
                }
                if (!"".equals(color)) {
                    myTitle += "<span style=\"color:" + doc.getColor() + "\">";
                }
                myTitle += docTitle;
                if (!"".equals(color)) {
                    myTitle += "</span>";
                }

                if (isBold) {
                    myTitle += "</B>";
                }
                myTitle += "</a>";
            }
            else {
                if (doc.getType() == Document.TYPE_FILE) {
                    Attachment am = null;
                    Vector<Attachment> attachments = doc.getAttachments(1);
                    Iterator<Attachment> ir = attachments.iterator();
                    if (ir.hasNext()) {
                        am = (Attachment) ir.next();
                        attId = am.getId();
                    }

                    boolean isImage = false;
                    if (attId != -1) {
                        if (StrUtil.isImage(StrUtil.getFileExt(am.getDiskName()))) {
                            isImage = true;
                        }
                        if (isImage) {
                            if (canDownload) {
                                myTitle = "<a target=\"_blank\" title=\"ID：" + docId + "\" href=\"download.do?pageNum=1&id=" + docId + "&attachId=" + am.getId() + "\">" + docTitle + "</a>";
                            } else {
                                myTitle = docTitle;
                            }
                        } else {
                            String fileExt = StrUtil.getFileExt(am.getDiskName());
                            boolean isOffice = "doc".equals(am.getExt())
                                    || "docx".equals(am.getExt())
                                    || "xls".equals(am.getExt())
                                    || "xlsx".equals(am.getExt());
                            if (isOffice) {
                                String s = Global.getRealPath() + am.getVisualPath() + "/" + am.getDiskName();
                                String htmlfile = s.substring(0, s.lastIndexOf(".")) + ".html";
                                File fileExist = new File(htmlfile);
                                if (fileExist.exists()) {
                                    myTitle = "<a target=\"_blank\" title=\"ID：" + docId + "\" href=\"../doc_show_preview.jsp?pageNum=1&id=" + docId + "&attachId=" + am.getId() + "\">";
                                    myTitle += docTitle;
                                    myTitle += "</a>";

                                } else {
                                    if (canDownload) {
                                        myTitle = "<a target=\"_blank\" title=\"ID：" + docId + "\" href=\"download.do?pageNum=1&id=" + docId + "&attachId=" + am.getId() + "\">" + docTitle + "</a>";
                                    } else {
                                        myTitle = "<a target=\"_blank\" title=\"ID：" + docId + "\" href=\"fileark_ntko_show.jsp?pageNum=1&docId=" + docId + "&attachId=" + am.getId() + "\">" + docId + "</a>";
                                    }
                                }
                            } else if ("pdf".equals(fileExt)) {
                                myTitle = "<a target=\"_blank\" title=\"ID：" + docId + "\" href=\"pdf_js/viewer.html?file=" + request.getContextPath() + "/" + am.getVisualPath() + "/" + am.getDiskName() + "\">" + docTitle + "</a>";
                            } else {
                                if (canDownload) {
                                    myTitle = "<a title=\"ID：" + docId + "\" href=\"javascript:;\" onclick=\"downLoadDoc(" + docId + ", " + am.getId() + ")\">" + docTitle + "</a>";
                                } else {
                                    myTitle = docTitle;
                                }
                            }
                        }
                    }
                } else {
                    myTitle = "<a href=\"javascript:\" linkType=\"doc\" title=\"ID：" + docId + "\" data-id=\"" + docId + "\" doc-title=\"" + docTitle + "\">";
                    myTitle += "<span style=\"color:" + doc.getColor() + "\">";
                    if (isBold) {
                        myTitle += "<b>" + docTitle + "</b>";
                    } else {
                        myTitle += docTitle;
                    }
                    myTitle += "</span>";
                    myTitle += "</a>";
                }
                DocLogDb dldb = new DocLogDb();
                if (!dldb.isUserReaded(myUserName, doc.getId())) {
                    myTitle += "&nbsp;<img src=\"../images/icon_new.gif\"/>";
                }
            }
            json.put("title", titlePrefix + myTitle);
            json.put("attId", attId);
            json.put("docTitle", docTitle);
            json.put("id", docId);

            ud = ud.getUserDb(doc.getNick());
            String realName = "";
            if (ud != null && ud.isLoaded()) {
                realName = StrUtil.getNullStr(ud.getRealName());
            }
            else {
                realName = doc.getNick();
            }
            json.put("author", realName);
            json.put("createDate", DateUtil.format(doc.getCreateDate(), "yy-MM-dd HH:mm"));
            json.put("modifiedDate", DateUtil.format(doc.getModifiedDate(), "yy-MM-dd HH:mm"));

            String examineStatus = "";
            int ex = doc.getExamine();
            if (ex == Document.EXAMINE_NOT) {
                examineStatus = "<font color='blue'>未审核</font>";
            } else if (ex == Document.EXAMINE_NOTPASS) {
                examineStatus = "<font color='red'>未通过</font>";
            } else if (ex == Document.EXAMINE_PASS) {
                examineStatus = "已通过";
            } else if (ex == Document.EXAMINE_DRAFT) {
                examineStatus = "草稿";
            }
            json.put("status", examineStatus);

            String operate = "";

            Leaf lf6 = dir.getLeaf(doc.getDirCode());
            if (lf6 == null) {
                operate += "目录不存在";
                json.put("operate", operate);
                continue;
            }

            lp = new LeafPriv(lf6.getCode());
            if (examine1!=Document.EXAMINE_DRAFT && lp.canUserExamine(privilege.getUser(request))) {
                if (doc.getLevel() != Document.LEVEL_TOP) {
                    operate += "<a href=\"javascript:\" onclick=\"isTop(" + Document.LEVEL_TOP + "," + docId + ")\">置顶</a>";
                } else {
                    operate += "<a href=\"javascript:\" onclick=\"noTop(" + docId + ")\">取消置顶</a>";
                }
            }

            if (examine1==Document.EXAMINE_DRAFT || lp.canUserExamine(privilege.getUser(request)) || dp.canUserManage(request, docId)) {
                operate += "<a href=\"javascript:\" linkType=\"priv\" data-id=\"" + docId + "\" title=\"" + docTitle + "\" dirCode=\"" + lf6.getCode() + "\" dirName=\"" + lf6.getName() + "\">权限</a>";
                if (lf6.isLog()) {
                    operate += "<a href=\"javascript:\" linkType=\"log\" data-id=\"" + docId + "\" title=\"" + docTitle + "\">日志</a>";
                }
                if (canDownload) {
                    if (doc.getType() == Document.TYPE_FILE) {
                        operate += "<a href=\"javascript:\" onclick=\"downLoadDoc(" + docId + ", " + attId + ")\" title=\"ID：" + docTitle + "\">下载</a>";
                    }
                }
            }
            json.put("operate", operate);

            aryDoc.add(json);
        }

        com.alibaba.fastjson.JSONObject jsonObject = responseUtil.getResJson(true);
        int total = doc.getDocCount(sql);
        jsonObject.put("errCode", 0);
        jsonObject.put("total", String.valueOf(total));
        jsonObject.put("data", aryDoc);
        return jsonObject.toString();
    }
}