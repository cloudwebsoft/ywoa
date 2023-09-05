package com.cloudweb.oa.utils;

import cn.js.fan.util.file.FileUtil;
import com.cloudwebsoft.framework.util.LogUtil;
import com.jacob.activeX.ActiveXComponent;
import com.jacob.com.ComThread;
import com.jacob.com.Dispatch;
import com.jacob.com.Variant;
import com.redmoon.oa.Config;
import com.redmoon.oa.sys.DebugUtil;
import com.redmoon.oa.util.Excel2Html;
import com.redmoon.oa.util.Word2Html;

import java.io.File;

public class PreviewUtil {
    /**
     * 通过jacob生成预览html文件
     *
     * @param previewfile
     * @return
     */
    public static boolean createOfficeFilePreviewHTML(String previewfile) {
        boolean returnValue = false;
        com.redmoon.oa.Config cfg = Config.getInstance();
        boolean canOfficeFilePreview = cfg.getBooleanProperty("canOfficeFilePreview");
        if (canOfficeFilePreview) {
            int officeFilePreViewCreateMode = cfg.getInt("officeFilePreViewCreateMode");
            if (officeFilePreViewCreateMode==0) {
                poiFilePreview(previewfile);
                //判断是否成功生成html预览文件
                String htmlfile = previewfile.substring(0, previewfile.lastIndexOf(".")) + ".html";
                File fileExist = new File(htmlfile);
                if (fileExist.exists()) {
                    returnValue = true;
                }
            }
            else {
                //判断操作系统，windows可以生成预览文件
                if ("\\".equals(File.separator)) {
                    //根据上传文件生成预览文件
                    jacobFilePreview(previewfile);
                    //判断是否成功生成html预览文件
                    String htmlfile = previewfile.substring(0, previewfile.lastIndexOf(".")) + ".html";
                    File fileExist = new File(htmlfile);
                    if (fileExist.exists()) {
                        // 替换编码x-cp20936为gb2312，否则客户端webview将会乱码
                        String cont = cn.js.fan.util.file.FileUtil.ReadFile(htmlfile, "gb2312");
                        cont = cont.replaceAll("x-cp20936", "gb2312");
                        FileUtil.WriteFile(htmlfile, cont, "gb2312");
                        returnValue = true;
                    } else {
                        returnValue = false;
                    }
                }
            }
        }
        return returnValue;
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

    /**
     * 把word文件转换成html文件
     *
     * @param docfile  要转换的word文件路径
     * @param htmlfile 转换后的html文件路径
     */
    public static void wordToHtml(String docfile, String htmlfile) {
        ActiveXComponent app = null;
        try {
            app = new ActiveXComponent("Word.application");// 启动word
            //设置word不可见
            app.setProperty("Visible", new Variant(false));
            //获得document对象
            Dispatch docs = app.getProperty("Documents").toDispatch();
            // 注意文件路径中不能有空格，否则会找不到文件
            // docfile = "D:\\1487233498543581076690.docx";
            //打开文件
            Dispatch doc = Dispatch.invoke(docs, "Open", Dispatch.Method,
                    new Object[]{docfile, new Variant(false), new Variant(true)}, new int[1]).toDispatch();
            //保存成新文件
            // htmlfile = "d:\\1487233498543581076690.html";
            Dispatch.invoke(doc, "SaveAs", Dispatch.Method,
                    new Object[]{htmlfile, new Variant(8)}, new int[1]);
            Variant f = new Variant(false);
            Dispatch.call(doc, "Close", f);
        } catch (Exception e) {
            LogUtil.getLog(PreviewUtil.class).error(e);
        } catch (java.lang.UnsatisfiedLinkError e) {
            LogUtil.getLog(PreviewUtil.class).error(e);
        } catch (NoClassDefFoundError e) {
            LogUtil.getLog(PreviewUtil.class).error(e);
        } finally {
            if (app != null) {
                app.invoke("Quit", new Variant[]{});
            }
        }
    }

    /**
     * excel文件转换成html文件
     *
     * @param xlsfile
     * @param htmlfile
     */
    public static void excelToHtml(String xlsfile, String htmlfile) {
        ActiveXComponent app = null;
        try {
            app = new ActiveXComponent("Excel.application");
            app.setProperty("Visible", new Variant(false));
            Dispatch excels = app.getProperty("Workbooks").toDispatch();
            Dispatch excel = Dispatch.invoke(excels, "Open", Dispatch.Method,
                    new Object[]{xlsfile, new Variant(false), new Variant(true)}, new int[1]).toDispatch();
            Dispatch.invoke(excel, "SaveAs", Dispatch.Method,
                    new Object[]{htmlfile, new Variant(44)}, new int[1]);
            Variant f = new Variant(false);
            Dispatch.call(excel, "Close", f);
            ComThread.Release();//关闭进程
        } catch (Exception e) {
            LogUtil.getLog(PreviewUtil.class).error(e);
        } catch (UnsatisfiedLinkError | NoClassDefFoundError e) {
            LogUtil.getLog(PreviewUtil.class).error(e);
        } finally {
            if (app != null) {
                try {
                    app.invoke("Quit", new Variant[]{});
                } catch (Exception e) {
                    LogUtil.getLog(PreviewUtil.class).error(e);
                }
            }
        }
    }

    /**
     * 附件添加、修改后，预览文件对应操作（先删掉原有文件，再根据修改后内容生成新的文件）
     *
     * @param filepath
     */
    public static void jacobFilePreview(String filepath) {
        //生成的html文件
        int p = filepath.lastIndexOf(".");
        if (p==-1) {
            DebugUtil.e(PreviewUtil.class, "jacobFilePreview", filepath + " 文件名非法");
            return;
        }

        //获取上传文件后缀
        String fileType = filepath.substring(filepath.lastIndexOf(".") + 1);
        if (!(fileType.equals("doc") || fileType.equals("docx") || fileType.equals("xls") || fileType.equals("xlsx"))) {
            return;
        }

        String htmlfile = filepath.substring(0, p) + ".html";
        //word生成的htnl文件对应的files文件夹
        String existWordFile = filepath.substring(0, filepath.lastIndexOf(".")) + ".files";
        //excel生成的htnl文件对应的files文件夹
        String existExcelFile = filepath.substring(0, filepath.lastIndexOf(".")) + ".files";

        //先删除原有html文件
        File htmlFile = new File(htmlfile);
        if (htmlFile.exists()) {
            if (htmlFile.isFile()) {
                htmlFile.delete();
            }
        }

        //根据文件类型调用对应方法
        if (fileType.equals("doc") || fileType.equals("docx")) {
            File wordFile = new File(existWordFile);
            if (wordFile.exists()) {
                fileDelete(wordFile);
            }
            wordToHtml(filepath, htmlfile);
        }

        if (fileType.equals("xls") || fileType.equals("xlsx")) {
            File excelFile = new File(existExcelFile);
            if (excelFile.exists()) {
                fileDelete(excelFile);
            }
            excelToHtml(filepath, htmlfile);
        }
    }

    /**
     * 附件添加、修改后，预览文件对应操作（先删掉原有文件，再根据修改后内容生成新的文件）
     *
     * @param filepath
     */
    public static void poiFilePreview(String filepath) {
        //生成的html文件
        int p = filepath.lastIndexOf(".");
        if (p==-1) {
            DebugUtil.e(PreviewUtil.class, "poiFilePreview", filepath + " 文件名非法");
            return;
        }

        //获取上传文件后缀
        String fileType = filepath.substring(filepath.lastIndexOf(".") + 1);
        if (!(fileType.equals("doc") || fileType.equals("docx") || fileType.equals("xls") || fileType.equals("xlsx"))) {
            return;
        }

        String htmlfile = filepath.substring(0, p) + ".html";
        //word生成的htnl文件对应的files文件夹
        String existWordFile = filepath.substring(0, filepath.lastIndexOf(".")) + ".files";
        //excel生成的htnl文件对应的files文件夹
        String existExcelFile = filepath.substring(0, filepath.lastIndexOf(".")) + ".files";

        //先删除原有html文件
        File htmlFile = new File(htmlfile);
        if (htmlFile.exists()) {
            if (htmlFile.isFile()) {
                htmlFile.delete();
            }
        }

        //根据文件类型调用对应方法
        if ("doc".equals(fileType) || "docx".equals(fileType)) {
            File wordFile = new File(existWordFile);
            if (wordFile.exists()) {
                fileDelete(wordFile);
            }

            if ("doc".equals(fileType)) {
                Word2Html.docToHtml(filepath);
            } else {
                Word2Html.docxToHtml(filepath);
            }
        }

        if ("xls".equals(fileType) || "xlsx".equals(fileType)) {
            File excelFile = new File(existExcelFile);
            if (excelFile.exists()) {
                fileDelete(excelFile);
            }

            if ("xls".equals(fileType)) {
                Excel2Html.xlsToHtml(filepath);
            } else {
                Excel2Html.xlsxToHtml(filepath);
            }
        }
    }

    /**
     * 附件删除后，预览文件对应删除
     *
     * @param filepath
     */
    public static void jacobFileDelete(String filepath) {
        String htmlfile = filepath.substring(0, filepath.lastIndexOf(".")) + ".html";
        String existWordFile = filepath.substring(0, filepath.lastIndexOf(".")) + ".files";
        String existExcelFile = filepath.substring(0, filepath.lastIndexOf(".")) + ".files";

        String fileType = filepath.substring(filepath.lastIndexOf(".") + 1);
        File htmlFile = new File(htmlfile);
        if (htmlFile.exists()) {
            if (htmlFile.isFile()) {
                htmlFile.delete();
            }
        }

        if (fileType.equals("doc") || fileType.equals("docx")) {
            File wordFile = new File(existWordFile);
            if (wordFile.exists()) {
                fileDelete(wordFile);
            }
        }

        if (fileType.equals("xls") || fileType.equals("xlsx")) {
            File excelFile = new File(existExcelFile);
            if (excelFile.exists()) {
                fileDelete(excelFile);
            }
        }
    }
}
