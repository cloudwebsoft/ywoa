package com.cloudweb.oa.controller;

import cn.js.fan.util.ResKeyException;
import cn.js.fan.util.StrUtil;
import cn.js.fan.web.Global;
import com.alibaba.fastjson.JSONObject;
import com.cloudweb.oa.utils.SpringUtil;
import com.cloudwebsoft.framework.db.JdbcTemplate;
import com.redmoon.oa.db.SequenceManager;
import com.redmoon.oa.report.ReportManageDb;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.net.URLEncoder;
import java.util.Date;
import java.util.Vector;

@Slf4j
@Controller
@RequestMapping("/report")
public class ReportController {

    @ResponseBody
    @RequestMapping(value = "/create", produces = {"text/html;charset=UTF-8;"})
    public String create(String description, String privCode, String privDesc, @RequestParam(value = "upload", required = false) MultipartFile file) {
        JSONObject json = new JSONObject();
        if (file!=null){
            ReportManageDb rmDb = new ReportManageDb();
            int key = new Long(SequenceManager.nextID(SequenceManager.OA_REPORT_MANAGE)).intValue();
            String fileName = file.getOriginalFilename();
            String subName = fileName.substring(fileName.lastIndexOf("\\")+1);
            String fileType = StrUtil.getFileExt(fileName);
            if (!"raq".equals(fileType)) {
                json.put("ret", 0);
                json.put("msg", "上传文件类型错误，仅支持上传(*.raq)格式!");
                return json.toString();
            }
            String savePath = "/reportFiles";      //上传文件存储路径
            //String realPath = ServletActionContext.getServletContext().getRealPath(savePath);
            String realPath = Global.getRealPath() + savePath;
            //String report_date  = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());
            //String alter_date  = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());
            Date date = new Date();
            String sql = "select id from report_manage where name="+ StrUtil.sqlstr(subName);
            Vector v = rmDb.list(sql);
            if (v!=null&&v.size()!=0){
                json.put("ret", 0);
                json.put("msg", "文件名重复，请重命名");
                return "create";
            } else {
                try {
                    if (rmDb.create(new JdbcTemplate(), new Object[]{key,subName,savePath,description,date,privCode,privDesc, SpringUtil.getUserName(),date})){
                        File saveFile = new File(realPath,subName);
                        file.transferTo(saveFile);
                        json.put("ret", 1);
                        json.put("msg", "上传成功！");
                    };
                } catch (Exception e) {
                    json.put("ret", 0);
                    json.put("msg", "上传失败！");
                    log.error("报表上传失败！");
                }

            }
        } else {
            json.put("ret", 0);
            json.put("msg", "请选择上传文件！");
        }
        return json.toString();
    }

    @ResponseBody
    @RequestMapping(value = "/update", produces = {"text/html;charset=UTF-8;"})
    public String update(@RequestParam(required = true) Integer id, String description, String privCode, String privDesc, @RequestParam(value = "upload", required = false) MultipartFile file) {
        JSONObject json = new JSONObject();
        ReportManageDb rmDb = new ReportManageDb();
        rmDb = (ReportManageDb)rmDb.getQObjectDb(id);
        //String alter_date  = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());
        Date date = new Date();
        if(file==null){
            rmDb.set("priv_code",privCode);
            rmDb.set("priv_desc",privDesc);
            rmDb.set("description",description);
            rmDb.set("alter_date",date);
            try {
                rmDb.save();
                json.put("ret", 1);
                json.put("msg", "修改成功！");
            } catch (Exception e) {
                json.put("ret", 0);
                json.put("msg", "修改失败:" + e.getMessage());
                log.error("文件修改出现异常，保存失败");
            }
        } else {
            String oldName = rmDb.getString("name");
            String realPath = Global.getRealPath() + "reportFiles";
            File saveFile = new File(realPath,oldName);     //旧文件路径
            if (saveFile.exists()) {
                saveFile.delete();
            }
            String uploadFileName = file.getOriginalFilename();
            String subName = uploadFileName.substring(uploadFileName.lastIndexOf("\\")+1);
            String fileType = subName.substring(subName.indexOf(".")+1);
            if (!"raq".equals(fileType)){
                json.put("ret", 0);
                json.put("msg", "上传文件类型错误，仅支持上传(*.raq)格式!");
                return json.toString();
            }
            String sql = "select id from report_manage where name="+ StrUtil.sqlstr(subName)+" and id != "+id;
            Vector v = rmDb.list(sql);
            if (v!=null&&v.size()!=0){
                json.put("ret", 0);
                json.put("msg", "文件名重复，请重命名");
                return json.toString();
            } else {
                rmDb.set("name", subName);
                rmDb.set("priv_code", privCode);
                rmDb.set("priv_desc", privDesc);
                rmDb.set("description", description);
                rmDb.set("alter_date",date);
                try {
                    rmDb.save();
                    File sFile = new File(realPath,subName);    //新上传文件路径
                    file.transferTo(sFile);
                    json.put("ret", 1);
                    json.put("msg", "修改成功！");
                } catch (Exception e) {
                    json.put("msg", "修改失败:" + e.getMessage());
                    log.error("文件修改出现异常，保存失败");
                }
            }
        }
        return json.toString();
    }

    @RequestMapping("download")
    public void download(HttpServletResponse response, String fileName) throws IOException {
        response.setCharacterEncoding("UTF-8");
        response.setContentType("application/x-download");

        fileName = fileName.trim();
        response.addHeader("Content-Disposition", "attachment;filename=" + URLEncoder.encode(fileName.trim(), "UTF-8"));

        BufferedInputStream bis = null;
        BufferedOutputStream bos = null;

        try {
            bis = new BufferedInputStream(new FileInputStream(Global.getRealPath() + "reportFiles/" + fileName));
            bos = new BufferedOutputStream(response.getOutputStream());

            byte[] buff = new byte[2048];
            int bytesRead;

            while(-1 != (bytesRead = bis.read(buff, 0, buff.length))) {
                bos.write(buff,0,bytesRead);
            }
        } catch(final IOException e) {
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

    @ResponseBody
    @RequestMapping(value = "/del", produces = {"application/json;charset=UTF-8;"})
    public String del(String ids) {
        JSONObject json = new JSONObject();
        String[] idArr = ids.split(",");
        boolean re = false;
        ReportManageDb rmb = new ReportManageDb();
        if (idArr != null && idArr.length > 0) {
            for (int i = 0; i < idArr.length; i++) {
                String id = idArr[i];
                rmb = (ReportManageDb) rmb.getQObjectDb(id);
                if (rmb != null) {
                    String name = rmb.getString("name");
                    String path = rmb.getString("upload_path");
                    String savePath = Global.getRealPath() + path;
                    File file = new File(savePath, name);
                     if (file != null && file.exists()) {
                         file.delete();
                     }
                    try {
                        re = rmb.del();
                    } catch (ResKeyException e) {
                        e.printStackTrace();
                    }
                }
            }
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
}
