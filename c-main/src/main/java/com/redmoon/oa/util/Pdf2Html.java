package com.redmoon.oa.util;

import cn.js.fan.util.file.FileUtil;
import com.cloudweb.oa.utils.ConstUtil;
import com.cloudweb.oa.utils.SpringUtil;
import com.cloudweb.oa.utils.SysUtil;
import com.cloudwebsoft.framework.util.LogUtil;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.rendering.PDFRenderer;
import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

public class Pdf2Html {

    public static void createPreviewHTML(String filePath) {
        String pdfName = FileUtil.getFileName(filePath);
        String pdfPath = filePath.substring(0, filePath.lastIndexOf(pdfName) - 1);
        String name = FileUtil.getFileNameWithoutExt(pdfName);
        String htmlName = name + ".html";
        final String imagePath = pdfPath + File.separator + name + ".files";
        // 判断html文件是否存在，如存在则不作处理
        File htmlFile = new File(pdfPath + File.separator + htmlName);
        if (htmlFile.exists()) {
            return;
        }

        int p = pdfPath.indexOf(ConstUtil.UPLOAD_BASE_DIR);
        String visualPath = pdfPath.substring(p + ConstUtil.UPLOAD_BASE_DIR.length());
        String os = System.getProperty("os.name");
        if (!os.toLowerCase().startsWith("linux")) {
            visualPath = visualPath.replaceAll("\\\\", "/");
        }
        visualPath = ConstUtil.UPLOAD_BASE_DIR + visualPath;

        FileOutputStream fos = null;
        int size;
        FileOutputStream out;

        // PDF转换成HTML保存的文件夹
        // String pdfPath = "E:\\Pdf2HTML";
        File htmlsDir = new File(imagePath);
        if (!htmlsDir.exists()) {
            htmlsDir.mkdirs();
        }

        try {
            StringBuffer buffer = new StringBuffer();
            buffer.append("<!doctype html>\r\n");
            buffer.append("<head>\r\n");
            buffer.append("<meta charset=\"UTF-8\">\r\n");
            buffer.append("</head>\r\n");
            buffer.append("<body style=\"background-color:gray;\">\r\n");
            buffer.append("<style>\r\n");
            buffer.append("img {background-color:#fff; text-align:center; width:100%; max-width:100%;margin-top:6px;}\r\n");
            buffer.append("</style>\r\n");
            //pdf附件
            File pdfFile = new File(filePath);
            PDDocument document = PDDocument.load(pdfFile, (String) null);
            size = document.getNumberOfPages();
            Long start = System.currentTimeMillis(), end = null;
            LogUtil.getLog(Pdf2Html.class).info("===>pdf : " + pdfFile.getName() + " , size : " + size);
            PDFRenderer reader = new PDFRenderer(document);
            BufferedImage image;
            SysUtil sysUtil = SpringUtil.getBean(SysUtil.class);
            for (int i = 0; i < size; i++) {
                //image = newPDFRenderer(document).renderImageWithDPI(i,130,ImageType.RGB);
                image = reader.renderImage(i, 1.5f);
                //生成图片,保存位置
                out = new FileOutputStream(imagePath + "/" + "image" + "_" + i + ".jpg");
                ImageIO.write(image, "png", out); //使用png的清晰度
                //将图片路径追加到网页文件里
                buffer.append("<img src=\"" + sysUtil.getRootPath() + "/" + visualPath + "/" + name + ".files/image" + "_" + i + ".jpg\"/>\r\n");
                out.flush();
                out.close();
            }

            document.close();
            buffer.append("</body>\r\n");
            buffer.append("</html>");
            end = System.currentTimeMillis() - start;
            LogUtil.getLog(Pdf2Html.class).info("===> Reading pdf times: " + (end / 1000));
            // 生成网页文件
            fos = new FileOutputStream(htmlFile);
            fos.write(buffer.toString().getBytes());
            fos.flush();
        } catch (Exception e) {
            LogUtil.getLog(Pdf2Html.class).error(e);
        }
        finally {
            if (fos!=null) {
                try {
                    fos.close();
                } catch (IOException e) {
                    LogUtil.getLog(Pdf2Html.class).error(e);
                }
            }
        }
    }

    /**
     * 附件删除后，预览文件对应删除
     *
     * @param filepath
     */
    public static void del(String filepath) {
        String htmlfile = filepath.substring(0, filepath.lastIndexOf(".")) + ".html";
        String imagesDir = filepath.substring(0, filepath.lastIndexOf(".")) + ".files";
        File htmlFile = new File(htmlfile);
        if (htmlFile.exists()) {
            if (htmlFile.isFile()) {
                htmlFile.delete();
            }
        }

        File f = new File(imagesDir);
        if (f.exists()) {
            fileDelete(f);
        }
    }

    /**
     * 删除文件夹及其下子文件
     *
     * @param file
     */
    public static void fileDelete(File file) {
        if (!file.exists()) {
            return;
        }
        if (file.isFile()) {
            file.delete();
        } else {
            for (File f : file.listFiles()) {
                fileDelete(f);
            }
            file.delete();
        }
    }

    public static void main(String[] args) {
        String pdf = "E:\\123.pdf";
        //传入PDF地址
        createPreviewHTML(pdf);
    }
}