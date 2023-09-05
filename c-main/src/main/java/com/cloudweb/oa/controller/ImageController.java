package com.cloudweb.oa.controller;

import cn.js.fan.util.ErrMsgException;
import cn.js.fan.util.ImageUtil;
import cn.js.fan.util.MIMEMap;
import cn.js.fan.util.StrUtil;
import cn.js.fan.web.Global;
import com.alibaba.fastjson.JSONObject;
import com.cloudweb.oa.api.IObsService;
import com.cloudweb.oa.service.IFileService;
import com.cloudweb.oa.utils.I18nUtil;
import com.cloudweb.oa.utils.JarFileUtil;
import com.cloudweb.oa.vo.Result;
import com.cloudwebsoft.framework.util.LogUtil;
import com.qcloud.cos.exception.CosServiceException;
import com.qcloud.cos.model.COSObjectInputStream;
import com.redmoon.kit.util.FileUpload;
import com.redmoon.oa.pvg.Privilege;
import com.redmoon.oa.security.SecurityUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.util.Calendar;

@Controller
public class ImageController {

    @Autowired
    HttpServletRequest request;

    @Autowired
    I18nUtil i18nUtil;

    @Autowired
    private IFileService fileService;

    @Autowired
    private JarFileUtil jarFileUtil;

    @RequestMapping("/showImg")
    public void showImg(HttpServletResponse response, String path, String visitKey) throws IOException, ErrMsgException {
        Privilege privilege = new Privilege();
        if (!privilege.isUserLogin(request)) {
            boolean canUserView = false;
            if (!StrUtil.isEmpty(visitKey)) {
                if (SecurityUtil.validateVisitKey(visitKey, path) == 1) {
                    canUserView = true;
                }
            }

            if (!canUserView) {
                String errMsg = i18nUtil.get("err_visit_invalid");
                throw new ErrMsgException(errMsg);
            }
        }

        String pathLowerCase = path.toLowerCase();
        if (!(pathLowerCase.endsWith("png") || pathLowerCase.endsWith("jpg") || pathLowerCase.endsWith("gif") || pathLowerCase.endsWith("jpeg") || pathLowerCase.endsWith("bmp"))) {
            String errMsg = "格式非法";
            throw new ErrMsgException(errMsg);
        }

        if (path.startsWith(Global.getRootPath(request))) {
            path = path.substring(Global.getRootPath(request).length());
        } else if (path.startsWith("/")) {
            path = path.substring(1);
        }

        fileService.preview(response, path);
    }

    /**
     * 用于手机端访问
     *
     * @param response
     * @param path
     * @throws IOException
     * @throws ErrMsgException
     */
    @RequestMapping("/public/showImg")
    public void showImgPublic(HttpServletResponse response, String path) throws IOException, ErrMsgException {
        if (!(path.endsWith("png") || path.endsWith("jpg") || path.endsWith("gif") || path.endsWith("jpeg") || path.endsWith("bmp"))) {
            String errMsg = i18nUtil.get("err_visit_invalid");
            throw new ErrMsgException(errMsg);
        }

        if (path.startsWith(Global.getRootPath(request))) {
            path = path.substring(Global.getRootPath(request).length());
        } else if (path.startsWith("/")) {
            path = path.substring(1);
        }

        fileService.preview(response, path);
    }

    @RequestMapping("/showImgInJar")
    public void showImgInJar(HttpServletResponse response, String path) throws IOException, ErrMsgException {
        if (!(path.endsWith("png") || path.endsWith("jpg") || path.endsWith("gif") || path.endsWith("jpeg") || path.endsWith("bmp"))) {
            String errMsg = i18nUtil.get("err_visit_invalid");
            throw new ErrMsgException(errMsg);
        }

        if (!path.startsWith("/")) {
            path = "/" + path;
        }

        response.setContentType(MIMEMap.get(StrUtil.getFileExt(path)));
        BufferedInputStream bis = null;
        BufferedOutputStream bos = null;

        try {
            InputStream inputStream = jarFileUtil.getJarFile(path);
            if (inputStream == null) {
                throw new FileNotFoundException(path + " is not exist.");
            }
            bis = new BufferedInputStream(inputStream);
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

    @ResponseBody
    @RequestMapping(value = "/uploadMedia", produces = {"application/json;charset=UTF-8;"})
    public Result<Object> uploadMedia(String type, String formCode, @RequestParam(value = "uploader", required = true) MultipartFile file
    ) {
        String vPath;
        if ("flow".equals(type)) {
            com.redmoon.oa.flow.FormDAO fdao = new com.redmoon.oa.flow.FormDAO();
            vPath = fdao.getVisualPath();
        }
        else {
            com.redmoon.oa.visual.FormDAO fdao = new com.redmoon.oa.visual.FormDAO();
            fdao.setFormCode(formCode);
            vPath = fdao.getVisualPath();
        }
        String name = file.getOriginalFilename();
        String ext = StrUtil.getFileExt(name);
        String diskName = FileUpload.getRandName() + "." + ext;
        try {
            fileService.write(file, vPath, diskName);
        } catch (IOException e) {
            LogUtil.getLog(getClass()).error(e);
        }
        JSONObject json = new JSONObject();
        // json.put("path", vPath + "/" + diskName);
        // 生成Base64编码的图片
        json.put("image", "data:image/png;base64," + ImageUtil.getImgBase64Str(Global.getRealPath() + vPath + "/" + diskName));
        File f = new File(Global.getRealPath() + vPath + "/" + diskName);
        f.delete();

        return new Result<>(json);
    }
}
