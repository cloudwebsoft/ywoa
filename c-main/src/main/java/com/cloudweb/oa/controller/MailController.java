package com.cloudweb.oa.controller;

import cn.js.fan.util.ErrMsgException;
import cn.js.fan.util.StrUtil;
import com.cloudweb.oa.service.IFileService;
import com.cloudweb.oa.utils.ResponseUtil;
import com.cloudwebsoft.framework.util.LogUtil;
import com.redmoon.oa.emailpop3.Attachment;
import com.redmoon.oa.emailpop3.MailMsgDb;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

@Controller
@RequestMapping("/mail")
public class MailController {
    @Autowired
    HttpServletRequest request;

    @Autowired
    private IFileService fileService;

    @Autowired
    private ResponseUtil responseUtil;

    @RequestMapping("/download")
    public void download(HttpServletResponse response, @RequestParam(required = true) Integer id, @RequestParam(required = true) Integer attachId) throws IOException, ErrMsgException {
        try (BufferedOutputStream bos = new BufferedOutputStream(response.getOutputStream())) {
            MailMsgDb mmd = new MailMsgDb();
            mmd = mmd.getMailMsgDb(id);
            Attachment att = mmd.getAttachment(attachId);

            if (StrUtil.isImage(StrUtil.getFileExt(att.getDiskName()))) {
                /*因为不支持<img src='...'/>，故注释掉
                response.setContentType("text/html;charset=utf-8");
                String str = "<img src=\"" + request.getContextPath() + "/showImg.do?path=" + att.getVisualPath() + "/" + att.getDiskName() + "\" />";
                bos.write(str.getBytes(StandardCharsets.UTF_8));*/
                fileService.preview(response, att.getVisualPath() + "/" + att.getDiskName());
                return;
            }

            fileService.download(response, att.getName(), att.getVisualPath(), att.getDiskName());
        } catch (final IOException e) {
            LogUtil.getLog(getClass()).error(e);
        }
    }
}
