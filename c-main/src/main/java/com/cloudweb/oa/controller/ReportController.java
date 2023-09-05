package com.cloudweb.oa.controller;

import cn.js.fan.util.ResKeyException;
import cn.js.fan.util.StrUtil;
import cn.js.fan.web.Global;
import com.alibaba.fastjson.JSONObject;
import com.cloudweb.oa.api.IReportService;
import com.cloudweb.oa.utils.SpringUtil;
import com.cloudwebsoft.framework.db.JdbcTemplate;
import com.cloudwebsoft.framework.util.LogUtil;
import com.redmoon.oa.db.SequenceManager;
import com.redmoon.oa.report.ReportManageDb;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.springframework.beans.factory.annotation.Autowired;
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

    @Autowired
    IReportService reportService;

    @ResponseBody
    @RequestMapping(value = "/create", produces = {"text/html;charset=UTF-8;"})
    public String create(String description, String privCode, String privDesc, @RequestParam(value = "upload", required = false) MultipartFile file) {
        return reportService.create(description, privCode, privDesc, file);
    }

    @ResponseBody
    @RequestMapping(value = "/update", produces = {"text/html;charset=UTF-8;"})
    public String update(@RequestParam(required = true) Integer id, String description, String privCode, String privDesc, @RequestParam(value = "upload", required = false) MultipartFile file) {
        return reportService.update(id, description, privCode, privDesc, file);
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

    @ResponseBody
    @RequestMapping(value = "/del", produces = {"application/json;charset=UTF-8;"})
    public String del(String ids) {
        JSONObject json = new JSONObject();
        String[] idArr = ids.split(",");
        boolean re = false;
        ReportManageDb rmb = new ReportManageDb();
        if (idArr.length > 0) {
            for (String id : idArr) {
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
                        LogUtil.getLog(getClass()).error(e);
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
