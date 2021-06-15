package com.cloudweb.oa.controller;

import cn.js.fan.db.ListResult;
import cn.js.fan.util.DateUtil;
import cn.js.fan.util.ErrMsgException;
import cn.js.fan.util.StrUtil;
import cn.js.fan.web.Global;
import com.redmoon.kit.util.FileUpload;
import com.redmoon.oa.android.Privilege;
import com.redmoon.oa.netdisk.*;
import com.redmoon.oa.person.UserDb;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.util.Date;
import java.util.Iterator;
import java.util.Vector;

@Deprecated
@RestController
@RequestMapping("/public/android")
public class NetdiskController {

    @ResponseBody
    @RequestMapping(value = "/netdisk/getdircode", produces = {"application/json;charset=UTF-8;"})
    public String getDirCode(@RequestParam(defaultValue = "", required = true) String skey, String dircode) {
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

        if ("".equals(dircode)) {
            dircode = privilege.getUserName(skey);
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
                    children.put("dircode", lf_c.getCode());
                    children.put("name", lf_c.getName());
                    childrens.put(children);
                }
            }
            json.put("childrens", childrens);

        } catch (JSONException e) {
            e.printStackTrace();
        }
        return json.toString();
    }

    @ResponseBody
    @RequestMapping(value = "/netdisk/getlist", produces = {"application/json;charset=UTF-8;"})
    public String getList(@RequestParam(defaultValue = "", required = true) String skey, String dircode, Integer pagenum, Integer pagesize) {
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

        Document doc = new Document();

        if ("".equals(dircode)) {
            dircode = privilege.getUserName(skey);
            String root_code = dircode;
            Leaf leaf = new Leaf();
            leaf = leaf.getLeaf(root_code);
            if (leaf == null || !leaf.isLoaded()) {
                // 为用户初始化网盘
                leaf = new Leaf();
                try {
                    leaf.initRootOfUser(root_code);
                    doc.getIDOrCreateByCode(dircode, dircode);
                } catch (ErrMsgException e) {
                    e.printStackTrace();
                }
            }
        }


        doc = doc.getDocumentByDirCode(dircode);

        if (doc == null) {
            try {
                json.put("res", "-1");
                json.put("msg", "目录不存在");
                return json.toString();
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        String sql = "SELECT id FROM netdisk_document_attach WHERE doc_id=" + doc.getID() + " and page_num=1  and is_current=1 and is_deleted=0  order by ";
        sql += " uploadDate desc ";


        Attachment am = new Attachment();
        int curpage = pagenum;   //第几页

        try {
            ListResult lr = am.listResult(sql, curpage, pagesize);
            long total = lr.getTotal();
            Vector attachments = lr.getResult();
            Iterator ir = attachments.iterator();
            json.put("res", "0");
            json.put("msg", "操作成功");
            json.put("total", String.valueOf(total));

            JSONObject result = new JSONObject();
            result.put("count", String.valueOf(pagesize));
            JSONArray files = new JSONArray();
            long fileLength = -1;
            String downPath = "";
            while (ir.hasNext()) {
                am = (Attachment) ir.next();
                fileLength = (long) am.getSize() / 1024;
                if (fileLength == 0 && (long) am.getSize() > 0) {
                    fileLength = 1;
                }
                JSONObject file = new JSONObject();
                file.put("id", String.valueOf(am.getId()));
                file.put("doc_id", String.valueOf(am.getDocId()));
                file.put("title", am.getName());
                file.put("size", fileLength + "KB");
                file.put("createdate", DateUtil.format(am.getUploadDate(), "yyyy-MM-dd HH:mm:ss"));
                downPath = "public/android/netdisk_getfile.jsp?" + "id=" + am.getDocId() + "&attachId=" + am.getId();
                file.put("url", downPath);
                files.put(file);
            }
            result.put("files", files);

            Directory dir = new Directory();
            Leaf leaf = dir.getLeaf(dircode);

            JSONArray categorys = new JSONArray();
            Iterator irch = leaf.getChildren().iterator();
            while (irch.hasNext()) {
                Leaf clf = (Leaf) irch.next();
                JSONObject category = new JSONObject();
                category.put("id", clf.getCode());
                category.put("title", clf.getName());
                category.put("dircode", clf.getCode());
                category.put("createdate", clf.getAddDate());
                categorys.put(category);
            }
            result.put("categorys", categorys);
            json.put("result", result);
        } catch (ErrMsgException e) {
            e.printStackTrace();
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return json.toString();
    }

    @ResponseBody
    @RequestMapping(value = "/netdisk/upload", method = RequestMethod.POST, produces = {"application/json;charset=UTF-8;"})
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
                e.printStackTrace();
            }
        }

        String userName = privilege.getUserName(skey);

        UserDb user = new UserDb();
        user = user.getUserDb(userName);
        String dirCode = "";
        if("".equals(dircode)){
            dirCode = userName;
        }

        Leaf lf = new Leaf();
        lf = lf.getLeaf(dirCode);

        if(lf==null){
            try {
                json.put("res", "-1");
                json.put("msg", "目录不存在");
                return json.toString();
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        Document doc = new Document();
        doc = doc.getDocumentByDirCode(dirCode);

        FileOutputStream out;
        try {
            com.redmoon.oa.Config cfg = new com.redmoon.oa.Config();
            String filepath = cfg.get("file_netdisk")+"/"+lf.getFilePath();
            String path  = Global.getRealPath()+filepath;

            File file_path = new File(path);
            if(!file_path.exists()){ //创建文件夹
                file_path.mkdirs();
            }
            String real_path = file_path.getPath();
            Attachment att = new Attachment();

            if(files!=null){
                for (int i = 0 ; i < files.length ; i++) {
                    String diskName = FileUpload.getRandName();
                    String ext = StrUtil.getFileExt(files[i].getOriginalFilename());
                    diskName += "." + ext;

                    out = new FileOutputStream(real_path + File.separator + diskName);

                    InputStream in = files[i].getInputStream();
                    int size  = in.available(); //文件大小

                    byte buffer[] = new byte[1024 * 10];
                    int length = 0;
                    while ((length = in.read(buffer)) > 0) {
                        out.write(buffer, 0, length);
                    }
                    out.close();
                    att.setExt(ext);
                    att.setDocId(doc.getId());
                    att.setFullPath(real_path + "/"+ diskName);
                    att.setVisualPath(lf.getFilePath());
                    att.setName(files[i].getOriginalFilename());
                    att.setDiskName(diskName);
                    att.setSize(size);
                    att.setPageNum(1);
                    att.setUploadDate(new Date());
                    att.create();
                }
                json.put("res", "0");
                json.put("msg", "操作成功");
                json.put("categoryid", String.valueOf(doc.getId()));
                json.put("dircode", dirCode);
                json.put("createdate", DateUtil.format(att.getUploadDate(), "yyyy-MM-dd HH:mm:ss"));
            }
        } catch (FileNotFoundException e1) {
            e1.printStackTrace();
        } catch (IOException e1) {
            e1.printStackTrace();
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return json.toString();
    }

    @ResponseBody
    @RequestMapping(value = "/netdisk/delete", produces = {"application/json;charset=UTF-8;"})
    public String delete(@RequestParam(defaultValue = "", required = true) String skey, int doc_id, int attach_id) {
        JSONObject json = new JSONObject();

        Privilege privilege = new Privilege();
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
        doc = doc.getDocument(doc_id);
        LeafPriv lp = new LeafPriv(doc.getDirCode());
        if (!lp.canUserDel(privilege.getUserName(skey))) {
            try {
                json.put("res","-1");
                json.put("msg","权限非法");
                return json.toString();
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        Attachment att = new Attachment(attach_id);
        re = att.delAttLogic();
        if(re){
            try {
                json.put("res","0");
                json.put("msg","操作成功");
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return json.toString();
    }
}
