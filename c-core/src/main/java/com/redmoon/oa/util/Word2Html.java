package com.redmoon.oa.util;

import cn.js.fan.util.file.FileUtil;
import com.cloudwebsoft.framework.util.LogUtil;
import fr.opensagres.poi.xwpf.converter.core.BasicURIResolver;
import fr.opensagres.poi.xwpf.converter.core.FileImageExtractor;
import fr.opensagres.poi.xwpf.converter.core.IXWPFConverter;
import fr.opensagres.poi.xwpf.converter.xhtml.XHTMLConverter;
import fr.opensagres.poi.xwpf.converter.xhtml.XHTMLOptions;
import org.apache.poi.hwpf.HWPFDocument;
import org.apache.poi.hwpf.converter.PicturesManager;
import org.apache.poi.hwpf.converter.WordToHtmlConverter;
import org.apache.poi.hwpf.usermodel.PictureType;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.w3c.dom.Document;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.*;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.*;

public class Word2Html {

    /**
     * 将word2003转换为html文件，如果html文件已存在，则不作处理
     *
     * @throws IOException
     * @throws TransformerException
     * @throws ParserConfigurationException
     */
    public static void docToHtml(String filePath) {
        String wordName = FileUtil.getFileName(filePath);
        String wordPath = filePath.substring(0, filePath.lastIndexOf(wordName) - 1);
        String name = FileUtil.getFileNameWithoutExt(wordName);
        String htmlName = name + ".html";
        final String imagePath = wordPath + File.separator + name + ".files";
        // 判断html文件是否存在，如存在则不作处理
        File htmlFile = new File(wordPath + File.separator + htmlName);
        if (htmlFile.exists()) {
            // return htmlFile.getAbsolutePath();
            return;
        }

        // 原word文档
        final String file = wordPath + File.separator + wordName;
        InputStream is = null;
        try {
            is = new FileInputStream(new File(file));
            HWPFDocument wordDocument = new HWPFDocument(is);
            WordToHtmlConverter wordToHtmlConverter = new WordToHtmlConverter(
                    DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument());
            // 设置图片存放的位置
            wordToHtmlConverter.setPicturesManager(new PicturesManager() {
                @Override
                public String savePicture(byte[] content, PictureType pictureType, String suggestedName, float widthInches, float heightInches) {
                    File imgPath = new File(imagePath);
                    if (!imgPath.exists()) {// 图片目录不存在则创建
                        imgPath.mkdirs();
                    }
                    File file = new File(imagePath + File.separator + suggestedName);
                    try {
                        OutputStream os = new FileOutputStream(file);
                        os.write(content);
                        os.close();
                    } catch (FileNotFoundException e) {
                        LogUtil.getLog(Word2Html.class).error(e);
                    } catch (IOException e) {
                        LogUtil.getLog(Word2Html.class).error(e);
                    }
                    // 图片在html文件上的路径 映射路径
                    // return "http://" + ipAddress + vpath + "/xx/" + suggestedName;
                    return name + ".files/" + suggestedName;
                }
            });
            // 解析word文档
            wordToHtmlConverter.processDocument(wordDocument);
            Document htmlDocument = wordToHtmlConverter.getDocument();
            // 生成html文件上级文件夹
        /*File folder = new File(wordPath);
        if (!folder.exists()) {
            folder.mkdirs();
        }*/
            OutputStream outStream = new FileOutputStream(htmlFile);
            DOMSource domSource = new DOMSource(htmlDocument);
            StreamResult streamResult = new StreamResult(outStream);
            TransformerFactory factory = TransformerFactory.newInstance();
            Transformer serializer = factory.newTransformer();
            serializer.setOutputProperty(OutputKeys.ENCODING, "utf-8");
            serializer.setOutputProperty(OutputKeys.INDENT, "yes");
            serializer.setOutputProperty(OutputKeys.METHOD, "html");
            serializer.transform(domSource, streamResult);
        } catch (FileNotFoundException e) {
            LogUtil.getLog(Word2Html.class).error(e);
        } catch (IOException e) {
            LogUtil.getLog(Word2Html.class).error(e);
        } catch (TransformerConfigurationException e) {
            LogUtil.getLog(Word2Html.class).error(e);
        } catch (ParserConfigurationException e) {
            LogUtil.getLog(Word2Html.class).error(e);
        } catch (TransformerException e) {
            LogUtil.getLog(Word2Html.class).error(e);
        } finally {
            if (is!=null) {
                try {
                    is.close();
                } catch (IOException e) {
                    LogUtil.getLog(Word2Html.class).error(e);
                }
            }
        }
    }

    /**
     * 2007版本word转换成html，如果html文件已存在，则不作处理
     *
     * @return
     * @throws IOException
     */
    public static void docxToHtml(String filePath) {
        String wordName = FileUtil.getFileName(filePath);
        String wordPath = filePath.substring(0, filePath.lastIndexOf(wordName) - 1);
        String name = FileUtil.getFileNameWithoutExt(wordName);
        String htmlName = name + ".html";

        String imagePath = wordPath + File.separator + name + ".files";
        // 判断html文件是否存在，如存在则不作处理
        File htmlFile = new File(wordPath + File.separator + htmlName);
        if (htmlFile.exists()) {
            // return htmlFile.getAbsolutePath();
            return;
        }
        // word文件
        File wordFile = new File(wordPath + File.separator + wordName);
        // 1) 加载word文档生成 XWPFDocument对象
        InputStream in = null;
        try {
            in = new FileInputStream(wordFile);
            XWPFDocument document = new XWPFDocument(in);
            // 2) 解析 XHTML配置 (这里设置IURIResolver来设置图片存放的目录)
            File imgFolder = new File(imagePath);
            XHTMLOptions options = XHTMLOptions.create();
            // html中图片的路径 相对路径
            // options.URIResolver(new BasicURIResolver("http://" + vpath + conpath + "/xxx/"));
            options.URIResolver(new BasicURIResolver(name + ".files"));
            options.setExtractor(new FileImageExtractor(imgFolder));
            options.setIgnoreStylesIfUnused(false);
            options.setFragment(true);
            // 3) 将 XWPFDocument转换成XHTML
            // 生成html文件上级文件夹
            /*File folder = new File(wordPath);
            if (!folder.exists()) {
                folder.mkdirs();
            }*/
            OutputStream out = new FileOutputStream(htmlFile);
            IXWPFConverter<XHTMLOptions> instance = XHTMLConverter.getInstance();
            // 该方法执行时 会 自动创建 /word/media 的子文件夹 用来存放word中的图片
            instance.convert(document, out, options);

            // 将word文件夹内的文件拷贝至.files目录，但是如果这样做，需对内容中的图片链接地址进行替换
            /*File src = new File(imagePath + File.separator + "word" + File.separator + "media");
            File dic = new File(imagePath);
            File[] files = src.listFiles();
            for (File s : files) {
                FileUtils.copyFileToDirectory(s, dic);
                FileUtils.deleteQuietly(s);
            }
            src.delete();*/
        // 当instance.convert中出现NullPointerException时，捕获不了，故改为捕获Exception
        // } catch (NullPointerException | IOException e) {
        } catch (Exception e) {
            LogUtil.getLog(Word2Html.class).error(filePath + "生成html预览失败");
            LogUtil.getLog(Word2Html.class).error(e);
        } finally {
            if (in != null) {
                try {
                    in.close();
                } catch (IOException e) {
                    LogUtil.getLog(Word2Html.class).error(e);
                }
            }
        }
    }

    public static void main(String[] args) throws Exception {
        // docToHtml("e:\\test\\1.doc");
        docxToHtml("e:\\test\\1.docx");
    }

}

