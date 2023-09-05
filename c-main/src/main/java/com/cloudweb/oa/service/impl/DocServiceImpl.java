package com.cloudweb.oa.service.impl;

import cn.js.fan.db.ListResult;
import cn.js.fan.util.StrUtil;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.cloudweb.oa.service.IDocService;
import com.redmoon.oa.base.IAttachment;
import com.redmoon.oa.basic.SelectDb;
import com.redmoon.oa.basic.SelectMgr;
import com.redmoon.oa.basic.SelectOptionDb;
import com.redmoon.oa.basic.TreeSelectDb;
import com.redmoon.oa.flow.FormDb;
import com.redmoon.oa.visual.Attachment;
import com.redmoon.oa.visual.FormDAO;
import org.springframework.stereotype.Service;

import java.util.Vector;

@Service
public class DocServiceImpl implements IDocService {

    @Override
    public String getDirName(String dirCode) {
        TreeSelectDb tsd = new TreeSelectDb();
        tsd = tsd.getTreeSelectDb(dirCode);
        return tsd.getName();
    }

    @Override
    public JSONObject listDoc(String dirCode, int page, int pageSize) {
        JSONObject json = new JSONObject();
        String sql = "select id from " + FormDb.getTableName("document") + " where dir_code=" + StrUtil.sqlstr(dirCode) + " order by orders desc, id desc";
        FormDAO fdao = new FormDAO();
        ListResult lr = fdao.listResult("document", sql, page, pageSize);
        json.put("total", lr.getTotal());
        JSONArray ary = new JSONArray();
        for (Object o : lr.getResult()) {
            fdao = (FormDAO)o;
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("id", fdao.getId());
            jsonObject.put("title", fdao.getFieldValue("title"));
            jsonObject.put("userName", fdao.getFieldValue("user_name"));
            jsonObject.put("createDate", fdao.getFieldValue("create_date"));
            jsonObject.put("color", fdao.getFieldValue("color"));
            jsonObject.put("is_bold", fdao.getFieldValue("is_bold"));
            ary.add(jsonObject);
        }
        if (lr.getResult().size() > 0) {
            FormDAO topDao = (FormDAO)lr.getResult().get(0);
            for (IAttachment att : topDao.getAttachments()) {
                // 取得其中的第一张图片
                if (StrUtil.isImage(StrUtil.getFileExt(att.getDiskName()))) {
                    json.put("topImage", att.getVisualPath() + "/" + att.getDiskName());
                    json.put("topId", topDao.getId());
                    json.put("topTitle", topDao.getFieldValue("title"));
                    json.put("topCreateDate", topDao.getFieldValue("create_date"));
                    break;
                }
            }
        }
        json.put("list", ary);
        return json;
    }

    @Override
    public JSONArray listImage(String dirCode, int rowCount) {
        JSONArray ary = new JSONArray();
        FormDb fd = new FormDb();
        fd = fd.getFormDb("document");
        String sql = "select a.id from visual_attach a, ft_document d where a.visualId=d.id and d.dir_code=" + StrUtil.sqlstr(dirCode) + " and field_name='images' order by a.id desc";
        Attachment attachment = new Attachment();
        FormDAO fdao = new FormDAO();
        ListResult lr = attachment.listResult(sql, 1, rowCount);
        Vector<Attachment> result = lr.getResult();
        for (Attachment att : result) {
            JSONObject json = new JSONObject();
            json.put("id", att.getId());
            json.put("name", att.getName());
            json.put("path", "showImg?path=" + att.getVisualPath() + "/" + att.getDiskName());
            fdao = fdao.getFormDAO(att.getVisualId(), fd);
            json.put("title", fdao.getFieldValue("title"));
            json.put("docId", fdao.getId());
            ary.add(json);
        }
        return ary;
    }
}
