package com.cloudweb.oa.controller;

import cn.js.fan.util.ErrMsgException;
import cn.js.fan.util.ResKeyException;
import cn.js.fan.util.StrUtil;
import com.cloudweb.oa.service.IFileService;
import com.cloudweb.oa.utils.ResponseUtil;
import com.redmoon.oa.person.UserDb;
import com.redmoon.oa.stamp.StampDb;
import com.redmoon.oa.stamp.StampLogDb;
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
@RequestMapping("/stamp")
public class StampController {
    @Autowired
    HttpServletRequest request;

    @Autowired
    private IFileService fileService;

    @Autowired
    private ResponseUtil responseUtil;
/*
    通过getImageUrl
    @RequestMapping("/download")
    public void download(HttpServletResponse response, @RequestParam(required = true) String stampId) throws IOException, ErrMsgException {
        try (BufferedOutputStream bos = new BufferedOutputStream(response.getOutputStream())) {
            if (!com.redmoon.oa.kernel.License.getInstance().isPlatform()) {
                response.setContentType("text/html;charset=utf-8");
                String str = "请使用平台版";
                bos.write(str.getBytes(StandardCharsets.UTF_8));
                return;
            }
            String userName = StrUtil.UnicodeToGB(request.getParameter("user"));
            String pwd = StrUtil.UnicodeToGB(request.getParameter("pwd"));
            String filename = "";

            if (userName == null) {
                response.setContentType("text/html;charset=utf-8");
                String str = "用户不能为空";
                bos.write(str.getBytes(StandardCharsets.UTF_8));
            } else {
                UserDb user = new UserDb();
                user = user.getUserDb(userName);
                if (!user.isLoaded()) {
                    response.setContentType("text/html;charset=utf-8");
                    String str = "用户不存在";
                    bos.write(str.getBytes(StandardCharsets.UTF_8));
                    return;
                }
                boolean re = user.Auth(userName, pwd);
                if (!re) {
                    response.setContentType("text/html;charset=utf-8");
                    String str = "用户名或密码错误";
                    bos.write(str.getBytes(StandardCharsets.UTF_8));
                    return;
                }

                StampDb stamp = new StampDb();
                int intStampId = StrUtil.toInt(stampId, -1);
                if (intStampId != -1) {
                    stamp = stamp.getStampDb(intStampId);
                    if (!stamp.isLoaded()) {
                        response.setContentType("text/html;charset=utf-8");
                        String str = "印章" + request.getParameter("stampId") + "不存在";
                        bos.write(str.getBytes(StandardCharsets.UTF_8));
                        return;
                    }
                } else {
                    stamp = stamp.getStampDbByName(stampId);
                    if (stamp == null) {
                        response.setContentType("text/html;charset=utf-8");
                        String str = "印章" + stampId + "不存在";
                        bos.write(str.getBytes(StandardCharsets.UTF_8));
                        return;
                    }
                    intStampId = stamp.getId();
                }
                if (!stamp.canUserUse(userName)) {
                    response.setContentType("text/html;charset=utf-8");
                    String str = "您无权使用印章";
                    bos.write(str.getBytes(StandardCharsets.UTF_8));
                    return;
                }

                StampLogDb sld = new StampLogDb();
                sld.create(new com.cloudwebsoft.framework.db.JdbcTemplate(), new Object[]{com.redmoon.oa.db.SequenceManager.nextID(com.redmoon.oa.db.SequenceManager.OA_STAMP_LOG), userName, intStampId, new java.util.Date(), StrUtil.getIp(request)});

                fileService.download(response, stamp.getImage(), "upfile/" + StampDb.LINK_BASE_PATH, stamp.getImage());
            }
        } catch (final IOException | ResKeyException e) {
            LogUtil.getLog(getClass()).error(e);
        }
    }*/
}
