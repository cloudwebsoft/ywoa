package com.cloudweb.oa.controller;

import cn.js.fan.db.ResultIterator;
import cn.js.fan.db.ResultRecord;
import cn.js.fan.util.DateUtil;
import cn.js.fan.util.ErrMsgException;
import cn.js.fan.util.StrUtil;
import cn.js.fan.web.Global;
import com.cloudwebsoft.framework.db.JdbcTemplate;
import com.redmoon.kit.util.FileUpload;
import com.redmoon.oa.android.Privilege;
import com.redmoon.oa.fileark.*;
import com.redmoon.oa.person.UserDb;
import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import java.io.*;
import java.sql.SQLException;
import java.util.*;

@Deprecated
@RestController
@RequestMapping("/public/android")
public class FilecaseController {

    @Autowired
    HttpServletRequest request;

    @ResponseBody
    @RequestMapping(value = "/filecase/getdircode", method = RequestMethod.POST, produces = {"application/json;charset=UTF-8;"})
    public String getDirCode(@RequestParam(defaultValue = "", required = true) String skey, @RequestParam(defaultValue = "") String dircode) {
        JSONObject json = new JSONObject();

        Privilege privilege = new Privilege();
        boolean re = privilege.Auth(skey);
        if (re) {
            try {
                json.put("res", "-2");
                json.put("msg", "时间过期");
                return json.toString();
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        String userName = privilege.getUserName(skey);

        if ("".equals(dircode)) {
            dircode = Leaf.ROOTCODE;
        }

        try {
            json.put("res", "0");
            json.put("msg", "操作成功");
            json.put("dircode", dircode);

            JSONArray childrens = new JSONArray();

            Leaf lf = new Leaf();
            lf = lf.getLeaf(dircode);
            if (lf != null) {
                Vector vector = lf.getChildren();
                Iterator ri = vector.iterator();
                while (ri.hasNext()) {
                    Leaf lf_c = (Leaf) ri.next();
                    JSONObject children = new JSONObject();

                    LeafPriv lp = new LeafPriv(lf_c.getCode());
                    if (lp.canUserSee(userName)) {
                        children.put("dircode", lf_c.getCode());
                        children.put("name", lf_c.getName());
                        childrens.put(children);
                    }
                }
            }
            json.put("childrens", childrens);

        } catch (JSONException e) {
            e.printStackTrace();
        }
        return json.toString();
    }

    @ResponseBody
    @RequestMapping(value = "/filecase/getlist", method = RequestMethod.POST, produces = {"application/json;charset=UTF-8;"})
    public String getlist(
            @RequestParam(defaultValue = "", required = true) String skey,
            @RequestParam(defaultValue = "") String dircode,
            @RequestParam(defaultValue = "") String op,
            @RequestParam(defaultValue = "") String cond,
            @RequestParam(defaultValue = "") String what,
            Integer pagenum,
            Integer pagesize
    ) {
        JSONObject json = new JSONObject();

        Privilege privilege = new Privilege();
        boolean re = privilege.Auth(skey);
        if (re) {
            try {
                json.put("res", "-2");
                json.put("msg", "时间过期");
                return json.toString();
            } catch (JSONException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }

        String userName = privilege.getUserName(skey);

        if ("".equals(dircode)) {
            dircode = Leaf.ROOTCODE;
        }

        Leaf lf = new Leaf();
        lf = lf.getLeaf(dircode);
        LeafPriv lps = new LeafPriv(dircode);

        try {
            String sql = "select distinct d.id,class1,title,isHome,examine,modifiedDate,color,isBold,expire_date,type,doc_level,createDate,keywords from document as d, doc_content as c";
            sql += " where d.id=c.doc_id and d.examine<>" + Document.EXAMINE_DUSTBIN;
            if (lps.canUserSee(userName) && !lps.canUserExamine(userName)) {
                sql += " and d.id not in (";
                sql += "select doc_id from doc_priv ";
                sql += "where see= 0 and (";
                //当个用户
                sql += "name=" + StrUtil.sqlstr(userName);
                //角色
                sql += " or name in(select roleCode from user_of_role where userName =" + StrUtil.sqlstr(userName) + ")";
                //用户角色组
                sql += " or name in(select code from user_group g,user_of_group ug where g.code = ug.group_code and  user_name =" + StrUtil.sqlstr(userName) + ")";
                sql += " or name in( select code from user_group_of_role rg,user_group g,user_of_role r   where rg.userGroupCode = g.code and r.roleCode = rg.roleCode and r.userName =" + StrUtil.sqlstr(userName) + ")";
                sql += ")";
                sql += ")";

            }
            if (!lps.canUserModify(userName)) {
                sql += " and examine=" + Document.EXAMINE_PASS;
            }

            if (op.equals("search")) {
                if (cond.equals("title")) {
                    sql += " and title like " + StrUtil.sqlstr("%" + what + "%");
                } else if (cond.equals("content")) {
                    //sql = "select distinct id, class1,title,isHome,examine,modifiedDate,color,isBold,expire_date,type,doc_level,createDate from document as d, doc_content as c where d.id=c.doc_id and d.examine<>" + Document.EXAMINE_DUSTBIN;
                    sql += " and c.content like " + StrUtil.sqlstr("%" + what + "%");
                } else {
                    sql += " and keywords like " + StrUtil.sqlstr("%" + what + "%");
                }

            }
            sql += " and class1=" + StrUtil.sqlstr(dircode);
            sql += " order by doc_level desc, examine asc, createDate desc";

            int curpage = pagenum;   //第几页
            JdbcTemplate jt = new JdbcTemplate();
            ResultIterator ri = jt.executeQuery(sql, curpage, pagesize);
            ResultRecord rr = null;

            json.put("res", "0");
            json.put("msg", "操作成功");
            json.put("total", String.valueOf(ri.getTotal()));

            json.put("dirName", lf.getName());

            JSONObject result = new JSONObject();
            result.put("count", String.valueOf(pagesize));

            JSONArray filecases = new JSONArray();

            while (ri.hasNext()) {
                rr = (ResultRecord) ri.next();
                JSONObject filecase = new JSONObject();
                filecase.put("id", String.valueOf(rr.getInt("id")));
                filecase.put("title", rr.getString("title"));
                filecase.put("createdate", StrUtil.getNullStr(DateUtil.format(rr.getDate("modifiedDate"), "yyyy-MM-dd HH:mm")));
                filecases.put(filecase);
            }
            result.put("filecases", filecases);

            JSONArray childrens = new JSONArray();
            Directory dir = new Directory();
            if (lf != null) {
                DirView dirView = new DirView(request, lf);
                ArrayList<String> list = new ArrayList<String>();
                try {
                    dirView.getJsonByUser(dir, lf.getCode(), userName, list);
                    if (list != null && list.size() > 0) {
                        for (String dirCode : list) {
                            Leaf leaf_c = new Leaf(dirCode);
                            if (leaf_c.isLoaded()) {
                                if (leaf_c.getParentCode().equals(lf.getCode())) {
                                    JSONObject children = new JSONObject();
                                    children.put("dircode", leaf_c.getCode());
                                    children.put("name", leaf_c.getName());
                                    childrens.put(children);
                                }
                            }

                        }
                        result.put("childrens", childrens);
                    }
                } catch (Exception e) {
                    Logger.getLogger(getClass()).error(e.getMessage());
                }
            }
            json.put("result", result);
        } catch (JSONException e) {
            Logger.getLogger(getClass()).error(e.getMessage());
        } catch (SQLException e) {
            Logger.getLogger(getClass()).error(e.getMessage());
        }
        return json.toString();
    }

    @ResponseBody
    @RequestMapping(value = "/filecase/getdetail", method = RequestMethod.POST, produces = {"application/json;charset=UTF-8;"})
    public String getdetail(
            @RequestParam(defaultValue = "", required = true) String skey,
            @RequestParam(required = true)Integer id) {
        JSONObject json = new JSONObject();
        Privilege privilege = new Privilege();
        boolean re = privilege.Auth(skey);
        if(re){
            privilege.doLogin(request, skey);
            try {
                json.put("res","-2");
                json.put("msg","时间过期");
                return json.toString();
            } catch (JSONException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }

        try {
            Document doc = new Document();
            doc = doc.getDocument(id);
            if(doc!=null){
                json.put("res","0");
                json.put("msg","操作成功");
                json.put("id",String.valueOf(id));
                json.put("title",doc.getTitle());
                json.put("createdate",StrUtil.getNullStr(DateUtil.format(doc.getCreateDate(),"yyyy-MM-dd HH:mm")));
                // json.put("content",privilege.delHTMLTag(StrUtil.getAbstract(request, doc.getContent(1), 50000, "\r\n")));
                json.put("content", StrUtil.getAbstract(request, doc.getContent(1), 50000, "\r\n"));

                json.put("canComment", "" + doc.isCanComment());

                LeafPriv lp = new LeafPriv();
                lp.setDirCode(doc.getDirCode());
                DocPriv dp = new DocPriv();

                boolean canDownload = lp.canUserDownLoad(privilege.getUserName(skey)) && dp.canUserDownload(request, id);

                // 文件附件
                JSONArray files = new JSONArray();
                if (canDownload) {
                    String downPath = "";
                    Vector attachments = doc.getAttachments(1);
                    Iterator ri = attachments.iterator();
                    while (ri.hasNext()) {
                        Attachment am = (Attachment) ri.next();
                        JSONObject file = new JSONObject();
                        file.put("name",am.getName());
                        downPath = "public/android/doc_getfile.jsp?"+"id="+am.getDocId()+"&attachId="+am.getId();
                        file.put("url",downPath);
                        file.put("size",String.valueOf(am.getSize()));
                        files.put(file);
                    }
                    json.put("files", files);
                }
            }else{
                json.put("res","-1");
                json.put("msg","文档不存在");
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return json.toString();
    }

    @ResponseBody
    @RequestMapping(value = "/filecase/upload", method = RequestMethod.POST, produces = {"application/json;charset=UTF-8;"})
    public String upload(
            @RequestParam(defaultValue = "", required = true) String skey,
            @RequestParam(defaultValue = "") String dircode,
            String title,
            String content,
            MultipartFile[] files
    ) {
        JSONObject json = new JSONObject();

        Privilege privilege = new Privilege();
        boolean re = privilege.Auth(skey);
        if (re) {
            try {
                json.put("res", "-2");
                json.put("msg", "时间过期");
                return json.toString();
            } catch (JSONException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }

        String userName = privilege.getUserName(skey);

        UserDb user = new UserDb();
        user = user.getUserDb(userName);
        String dirCode = "";
        if("".equals(dirCode)){
            dirCode = Leaf.ROOTCODE;
        }

        Leaf lf = new Leaf();
        lf = lf.getLeaf(dirCode);

        if(lf==null){
            if (dirCode.equals("camera")) {
                lf = new Leaf();
                lf.setName("现场拍照");
                lf.setCode(dirCode);
                lf.setParentCode(Leaf.ROOTCODE);
                lf.setDescription("现场拍照");
                lf.setType(Leaf.TYPE_LIST);
                lf.setPluginCode("");
                lf.setSystem(true);
                lf.setIsHome(true);
                String target = "";
                lf.setTarget(target);
                lf.setShow(true);
                lf.setOfficeNTKOShow(false);

                Leaf rootleaf = lf.getLeaf(Leaf.ROOTCODE);
                try {
                    rootleaf.AddChild(lf);
                } catch (ErrMsgException e) {
                    e.printStackTrace();
                    try {
                        json.put("res", "-1");
                        json.put("msg", "添加目录失败！");
                        return json.toString();
                    } catch (JSONException e1) {
                        e1.printStackTrace();
                    }
                }
            }
            else {
                try {
                    json.put("res", "-1");
                    json.put("msg", "目录不存在");
                    return json.toString();
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }

        LeafPriv lp = new LeafPriv(lf.getCode());
        if (!lp.canUserAppend(userName)) {
            try {
                json.put("res", "-1");
                json.put("msg", "权限非法！");
                return json.toString();
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        Document doc = new Document();
        // doc.setUnitCode(privilege.getUserUnitCode(getSkey()));
        doc.setKeywords(lf.getName());
        doc.setExamine(2);
        re = doc.create(dirCode, title, content, 0, "", "", userName,
                -1, user.getRealName());

        if (re) {
            FileOutputStream out;
            try {
                Calendar cal = Calendar.getInstance();
                String year = "" + (cal.get(cal.YEAR));
                String month = "" + (cal.get(cal.MONTH) + 1);
                com.redmoon.oa.Config cfg = new com.redmoon.oa.Config();
                String filepath = cfg.get("file_folder") + "/" + year + "/" + month;
                String path  = Global.getRealPath()+ filepath;

                File file_path = new File(path);
                if(!file_path.exists()){ //创建文件夹
                    file_path.mkdirs();
                }
                String real_path = file_path.getPath();
                Attachment att = new Attachment();

                if(files!=null){
                    for (int i = 0 ; i < files.length ; i++) {
                        String diskName = FileUpload.getRandName();
                        diskName += "." + StrUtil.getFileExt(files[i].getOriginalFilename());
                        out = new FileOutputStream(real_path + File.separator + diskName);

                        InputStream in = files[i].getInputStream();
                        int size  = in.available(); //文件大小
                        byte buffer[] = new byte[1024 * 10];
                        int length = 0;
                        while ((length = in.read(buffer)) > 0) {
                            out.write(buffer, 0, length);
                        }
                        out.close();
                        in.close();

                        att.setDocId(doc.getId());
                        att.setFullPath(path + File.separator + diskName);
                        att.setVisualPath(filepath);
                        att.setSize(size);
                        att.setName(files[i].getOriginalFilename());
                        att.setDiskName(diskName);
                        att.setPageNum(1);
                        att.setUploadDate(new Date());
                        att.create();
                    }
                }
            } catch (FileNotFoundException e1) {
                e1.printStackTrace();
            } catch (IOException e1) {
                e1.printStackTrace();
            }

            try {
                json.put("res", "0");
                json.put("msg", "操作成功");
                json.put("id", doc.getId());
                json.put("title", title);
                json.put("content", content);
                json.put("createdate", doc.getCreateDate());

            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return json.toString();
    }

    @ResponseBody
    @RequestMapping(value = "/filecase/delete", method = RequestMethod.POST, produces = {"application/json;charset=UTF-8;"})
    public String delete(
            @RequestParam(defaultValue = "", required = true) String skey,
            Integer id) {
        JSONObject json = new JSONObject();

        com.redmoon.oa.android.Privilege privilege = new com.redmoon.oa.android.Privilege();
        boolean re = privilege.Auth(skey);
        if(re){
            try {
                json.put("res","-2");
                json.put("msg","时间过期");
                return json.toString();
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        Document doc = new Document();
        doc = doc.getDocument(id);
        if (doc==null || !doc.isLoaded()) {
            try {
                json.put("res","-1");
                json.put("msg","文件不存在");
                return json.toString();
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        LeafPriv lp = new LeafPriv(doc.getDirCode());
        if (lp.canUserDel(privilege.getUserName(skey))) {
            if (true) {
                try {
                    re = doc.UpdateExamine(Document.EXAMINE_DUSTBIN);
                    if(re){
                        json.put("res","0");
                        json.put("msg","操作成功");
                    }
                } catch (ErrMsgException e) {
                    e.printStackTrace();
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
            else {
                try {
                    re = doc.del();
                    if(re){
                        json.put("res","0");
                        json.put("msg","操作成功");
                    }
                } catch (ErrMsgException e) {
                    e.printStackTrace();
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }
        else{
            try {
                json.put("res","-1");
                json.put("msg", com.redmoon.oa.pvg.Privilege.MSG_INVALID);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return json.toString();
    }
}
