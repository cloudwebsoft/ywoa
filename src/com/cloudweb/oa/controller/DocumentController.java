package com.cloudweb.oa.controller;

import javax.servlet.http.HttpServletRequest;

import cn.js.fan.db.ListResult;
import cn.js.fan.util.DateUtil;
import cn.js.fan.util.StrUtil;
import cn.js.fan.web.SkinUtil;
import com.raq.dm.Param;
import com.redmoon.oa.android.MessageInnerBoxOrSysBoxAction;
import com.redmoon.oa.android.Privilege;
import com.redmoon.oa.fileark.*;
import com.redmoon.oa.flow.Leaf;
import com.redmoon.oa.flow.MyActionDb;
import com.redmoon.oa.flow.WorkflowDb;
import com.redmoon.oa.message.Attachment;
import com.redmoon.oa.message.MessageDb;
import com.redmoon.oa.notice.NoticeDb;
import com.redmoon.oa.person.UserDb;
import com.redmoon.oa.person.UserMgr;
import org.apache.log4j.Logger;
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

import java.util.Iterator;
import java.util.Vector;

@Controller
@RequestMapping("/fileark")
public class DocumentController {
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
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }

        try {
            String dirCode = ParamUtil.get(request, "dirCode");
            String op = ParamUtil.get(request, "op");
            String cond = ParamUtil.get(request, "cond");
            String what = request.getParameter("what");
            what = StrUtil.UnicodeToUTF8(what);
            Document doc = new Document();
            String sql = "select id from document where class1=" + StrUtil.sqlstr(dirCode) + " and examine<>" + Document.EXAMINE_DUSTBIN;
            LeafPriv lp = new LeafPriv();
            lp.setDirCode(dirCode);
            if (!lp.canUserModify(privilege.getUserName())) {
                sql += " and examine=" + Document.EXAMINE_PASS;
            }
            if (op.equals("search")) {
                if (cond.equals("title")) {
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
            // TODO Auto-generated catch block
            Logger.getLogger(MessageInnerBoxOrSysBoxAction.class).error(e.getMessage());
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
                // TODO Auto-generated catch block
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
                // TODO Auto-generated catch block
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
        }
        catch (JSONException e) {
            e.printStackTrace();
        }
        return json.toString();
    }

    /**
     * 删除附件
     * @param attachId
     * @return
     */
    @ResponseBody
    @RequestMapping(value = "/delAttach", method = RequestMethod.POST, produces = {"text/html;charset=UTF-8;", "application/json;"})
    public String delAttach(int attachId) {
        JSONObject json = new JSONObject();
        com.redmoon.oa.fileark.Attachment att = new com.redmoon.oa.fileark.Attachment(attachId);
        int docId = att.getDocId();
        Document doc = new Document();
        doc = doc.getDocument(docId);

        com.redmoon.oa.pvg.Privilege privilege = new com.redmoon.oa.pvg.Privilege();
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
            DocContent dc = doc.getDocContent(1);
            boolean re = dc.delAttachment(attachId);
            if (re) {
                json.put("ret", 1);
                json.put("msg", "操作成功！");
            } else {
                json.put("ret", 0);
                json.put("msg", "操作失败！");
            }
        }
        catch (JSONException e) {
            e.printStackTrace();
        }
        return json.toString();
    }

    /**
     * 更改附件名称
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
                // TODO Auto-generated catch block
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
        }
        catch (JSONException e) {
            e.printStackTrace();
        }
        return json.toString();
    }
}
