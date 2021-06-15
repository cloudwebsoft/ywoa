package com.cloudweb.oa.controller;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import cn.js.fan.util.DateUtil;
import cn.js.fan.util.StrUtil;
import cn.js.fan.web.Global;
import cn.js.fan.web.SkinUtil;
import com.cloudweb.oa.utils.SpringUtil;
import com.cloudwebsoft.framework.util.IPUtil;
import com.redmoon.kit.util.FileUpload;
import com.redmoon.oa.android.Privilege;
import com.redmoon.oa.fileark.*;
import com.redmoon.oa.person.UserDb;
import com.redmoon.oa.person.UserMgr;
import com.redmoon.oa.util.PdfUtil;
import com.redmoon.oa.util.WordUtil;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.htmlparser.Parser;
import org.htmlparser.filters.TagNameFilter;
import org.htmlparser.tags.ImageTag;
import org.htmlparser.util.NodeList;
import org.htmlparser.util.ParserException;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import cn.js.fan.util.ErrMsgException;
import cn.js.fan.util.ParamUtil;

import java.io.*;
import java.util.Date;
import java.util.Iterator;
import java.util.Vector;

@Controller
@RequestMapping("/fileark")
public class FilearkController {
    @Autowired
    private HttpServletRequest request;

    /**
     * 下载前验证，如：打包下载
     *
     * @return
     */
    @ResponseBody
    @RequestMapping(value = "/downloadValidate", method = RequestMethod.POST, produces = {"text/html;charset=UTF-8;", "application/json;"})
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
            e.printStackTrace();
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
    @RequestMapping(value = "/list", method = RequestMethod.POST, produces = {"text/html;", "application/json;charset=UTF-8;"})
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
                e.printStackTrace();
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
            Logger.getLogger(getClass()).error(e.getMessage());
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
    @RequestMapping(value = "/getDoc", method = RequestMethod.GET, produces = {"text/html;", "application/json;charset=UTF-8;"})
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
                e.printStackTrace();
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
            e.printStackTrace();
        }
        return json.toString();
    }

    @ResponseBody
    @RequestMapping(value = "/move", method = RequestMethod.POST, produces = {"text/html;charset=UTF-8;", "application/json;"})
    public String move(int attachId, String direction) {
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
                e.printStackTrace();
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
            e.printStackTrace();
        }
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
                e.printStackTrace();
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
            e.printStackTrace();
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
            e.printStackTrace();
        } catch (ErrMsgException e) {
            e.printStackTrace();
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
            e.printStackTrace();
        } catch (ErrMsgException e) {
            e.printStackTrace();
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
            e.printStackTrace();
        } catch (ErrMsgException e) {
            e.printStackTrace();
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
            e.printStackTrace();
        } catch (ErrMsgException e) {
            e.printStackTrace();
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
            e.printStackTrace();
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
            } else if (action.equals("wikiPost")) {
                String pageUrl = "fileark/wiki_list.jsp?dir_code=" + StrUtil.UrlEncode(docmanager.getDirCode());
                json.put("ret", 1);
                json.put("msg", "操作成功！");
                json.put("redirectUri", pageUrl);
                return json.toString();
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
            e.printStackTrace();
        } catch (ErrMsgException e) {
            e.printStackTrace();
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

    @ResponseBody
    @RequestMapping(value = "/uploadBatch", method = RequestMethod.POST, produces = {"text/html;charset=UTF-8;", "application/json;"})
    public String uploadBatch() {
        DocumentMgr dm = new DocumentMgr();
        com.alibaba.fastjson.JSONObject json = new com.alibaba.fastjson.JSONObject();
        ServletContext application = request.getSession().getServletContext();
        int uploadType = ParamUtil.getInt(request, "uploadType", 0); // 1表示通过webedit上传
        boolean re;
        try {
            if (uploadType == 1) {
                re = dm.uploadByWebedit(application, request);
            } else {
                re = dm.uploadBatch(application, request);
            }
        } catch (ErrMsgException e) {
            json.put("ret", 0);
            json.put("msg", e.getMessage());
            return json.toString();
        }

        if (uploadType == 1) {
            if (re) {
                return "上传成功!";
            } else {
                return "操作失败!";
            }
        } else {
            if (re) {
                json.put("ret", "1");
                json.put("msg", "操作成功！");
            } else {
                json.put("ret", "0");
                json.put("msg", "操作失败！");
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
            e.printStackTrace();
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
            e.printStackTrace();
        } catch (ErrMsgException e) {
            e.printStackTrace();
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
            e.printStackTrace();
        } catch (ErrMsgException e) {
            e.printStackTrace();
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
            e.printStackTrace();
        } catch (ErrMsgException e) {
            e.printStackTrace();
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
            e.printStackTrace();
        } catch (ErrMsgException e) {
            e.printStackTrace();
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
            e.printStackTrace();
        } catch (ErrMsgException e) {
            e.printStackTrace();
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
            e.printStackTrace();
        } catch (ErrMsgException e) {
            e.printStackTrace();
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
            e.printStackTrace();
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
    public void download(HttpServletResponse response) throws IOException, ErrMsgException, JSONException {
        BufferedInputStream bis = null;
        BufferedOutputStream bos = null;
        try {
            bos = new BufferedOutputStream(response.getOutputStream());
            com.redmoon.oa.pvg.Privilege privilege = new com.redmoon.oa.pvg.Privilege();
            if (!privilege.isUserPrivValid(request, "read")) {
                response.setContentType("text/html;charset=utf-8");
                String str = SkinUtil.LoadString(request, "pvg_invalid");
                bos.write(str.getBytes("utf-8"));
                return;
            }

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

            String uName = privilege.getUser(request);
            String ip = IPUtil.getRemoteAddr(request);
            java.util.Date logDate = new java.util.Date();
            DocLogDb dld = new DocLogDb();
            DocAttachmentLogDb dad = new DocAttachmentLogDb();
            // 记录访问日志
            Leaf lf = new Leaf();
            lf = lf.getLeaf(mmd.getDirCode());
            if (lf==null) {
                response.setContentType("text/html;charset=utf-8");
                bos.write("目录不存在".getBytes("utf-8"));
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

            String s = Global.getRealPath() + att.getVisualPath() + "/" + att.getDiskName();

            att.setDownloadCount(att.getDownloadCount() + 1);
            att.save();

            if (StrUtil.isImage(StrUtil.getFileExt(att.getDiskName()))) {
                response.setContentType("text/html;charset=utf-8");
                String str = "<img src=\"" + request.getContextPath() + "/" + att.getVisualPath() + "/" + att.getDiskName() + "\" />";
                bos.write(str.getBytes("utf-8"));
                try {
                    Directory.onDownload(request, String.valueOf(attId), uName, false);
                } catch (ErrMsgException e) {
                    bos.write(e.getMessage().getBytes("utf-8"));
                    return;
                }
                return;
            }

            java.io.File f = new java.io.File(s);
            if (!f.exists()) {
                response.setContentType("text/html;charset=utf-8");
                String str = "文件不存在";
                bos.write(str.getBytes("utf-8"));
                return;
            }

            // 用下句会使IE在本窗口中打开文件
            // response.setContentType(MIMEMap.get(StrUtil.getFileExt(att.getDiskName())));
            // 使客户端直接下载，上句会使IE在本窗口中打开文件，下句也一样
            response.setContentType("application/octet-stream");
            response.setHeader("Content-disposition", "attachment; filename=" + StrUtil.GBToUnicode(att.getName()));

            bis = new BufferedInputStream(new FileInputStream(Global.realPath + att.getVisualPath() + "/" + att.getDiskName()));

            byte[] buff = new byte[2048];
            int bytesRead;

            while (-1 != (bytesRead = bis.read(buff, 0, buff.length))) {
                bos.write(buff, 0, bytesRead);
            }

            try {
                Directory.onDownload(request, String.valueOf(attId), uName, false);
            } catch (ErrMsgException e) {
                bos.write(e.getMessage().getBytes("utf-8"));
                return;
            }
        } catch (final IOException e) {
            e.printStackTrace();
        } finally {
            if (bis != null) {
                bis.close();
            }
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
                    for (int k=0; k<nodes.size(); k++) {
                        ImageTag node = (ImageTag) nodes.elementAt(k);

                        // image/png;base64,
                        String imgUrl = node.getImageURL();
                        int p = imgUrl.indexOf("http");
                        if (p == -1) {
                            if (imgUrl.startsWith("/")) {
                                imgUrl = Global.getFullRootPath(request) + imgUrl;
                            }
                            else {
                                imgUrl = Global.getFullRootPath(request) + "/" + imgUrl;
                            }

                            node.setImageURL(imgUrl);
                            int s = node.getStartPosition();
                            int e = node.getEndPosition();
                            String c = contentTmp.substring(lastNodeEnd, s);
                            c += node.toHtml();
                            sb.append(c);
                            lastNodeEnd = e;
                        }
                        else {
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
                e.printStackTrace();
            }

            WordUtil.htmlToWord(content, bos);
        } catch (final Exception e) {
            e.printStackTrace();
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
                    bos.write(("目录" + id + "不存在").getBytes("utf-8"));
                    return;
                }

                docId = lf.getDocID();
                if (docId == Leaf.DOC_ID_NONE) {
                    response.setContentType("text/html;charset=utf-8");
                    bos = new BufferedOutputStream(response.getOutputStream());
                    bos.write(("文档" + id + "不存在").getBytes("utf-8"));
                    return;
                }
            }

            Document doc = new Document();
            doc = doc.getDocument(docId);
            if (!doc.isLoaded()) {
                response.setContentType("text/html;charset=utf-8");
                bos = new BufferedOutputStream(response.getOutputStream());
                bos.write("文章不存在".getBytes("utf-8"));
                return;
            }

            LeafPriv lp = new LeafPriv(doc.getDirCode());
            if (!lp.canUserExportWord(SpringUtil.getUserName())) {
                response.setContentType("text/html;charset=utf-8");
                bos = new BufferedOutputStream(response.getOutputStream());
                bos.write("权限非法".getBytes("utf-8"));
                return;
            }

            String content = doc.getContent(1);

            response.setHeader("Content-disposition", "attachment; filename=" + StrUtil.GBToUnicode(doc.getTitle()) + ".pdf");
            response.setContentType("application/pdf");

            PdfUtil.htmlToPdf(response, doc.getTitle(), content, doc.getAuthor());
        } catch (final Exception e) {
            e.printStackTrace();
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
}
