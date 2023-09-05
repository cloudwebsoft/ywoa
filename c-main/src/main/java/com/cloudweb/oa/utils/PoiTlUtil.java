package com.cloudweb.oa.utils;


import cn.js.fan.util.ErrMsgException;
import cn.js.fan.util.ResKeyException;
import cn.js.fan.util.StrUtil;
import cn.js.fan.web.Global;
import com.cloudwebsoft.framework.util.LogUtil;
import com.deepoove.poi.XWPFTemplate;
import com.deepoove.poi.util.PoitlIOUtils;
import com.deepoove.poi.xwpf.NiceXWPFDocument;
import com.fasterxml.jackson.databind.exc.InvalidFormatException;
import com.redmoon.oa.stamp.StampDb;
import com.redmoon.oa.stamp.StampLogDb;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.util.Units;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import org.apache.poi.xwpf.usermodel.XWPFRun;
import org.apache.xmlbeans.XmlException;
import org.openxmlformats.schemas.drawingml.x2006.main.CTGraphicalObject;
import org.openxmlformats.schemas.drawingml.x2006.wordprocessingDrawing.CTAnchor;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTBookmark;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTDrawing;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTMarkupRange;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTR;
import org.springframework.core.io.ClassPathResource;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

import java.io.*;
import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

//引用 poi poi-ooxml poi-ooxml-schemas 至少得4.1.2版本以上
/**
 * @author 7788
 * @version 12.0
 * @date 2021/1/6 下午 3:38
 * @location wuhan
 * @document http://deepoove.com/poi-tl/#_%E7%A4%BA%E4%BE%8B
 */
@Slf4j
public class PoiTlUtil {

    public static void main(String[] args) throws Exception {
        String templFilePath = "E:\\云网OA\\模板\\公文模板.docx";
        String destFilePath = "E:\\云网OA\\模板\\目标文档.docx";
        String contentFilePath = "E:\\云网OA\\模板\\正文.docx";
        Map<String, String> data = new HashMap<>(2);
        data.put("标题", "红头文件");
        data.put("文号", "王123");
        data.put("here", "some");
        toRedDocument(templFilePath, contentFilePath, data, destFilePath, false);
    }

    /**
     * @param ctGraphicalObject 图片数据
     * @param deskFileName      图片描述
     * @param width             宽
     * @param height            高
     * @param leftOffset        水平偏移 left
     * @param topOffset         垂直偏移 top
     * @param behind            文字上方，文字下方
     * @return
     * @throws Exception
     */
    public static CTAnchor getAnchorWithGraphic(CTGraphicalObject ctGraphicalObject,
                                                String deskFileName, int width, int height,
                                                int leftOffset, int topOffset, boolean behind) {
        String anchorXML =
                "<wp:anchor xmlns:wp=\"http://schemas.openxmlformats.org/drawingml/2006/wordprocessingDrawing\" "
                        + "simplePos=\"0\" relativeHeight=\"0\" behindDoc=\"" + ((behind) ? 1 : 0) + "\" locked=\"0\" layoutInCell=\"1\" allowOverlap=\"1\">"
                        + "<wp:simplePos x=\"0\" y=\"0\"/>"
                        + "<wp:positionH relativeFrom=\"column\">"
                        + "<wp:posOffset>" + leftOffset + "</wp:posOffset>"
                        + "</wp:positionH>"
                        + "<wp:positionV relativeFrom=\"paragraph\">"
                        + "<wp:posOffset>" + topOffset + "</wp:posOffset>" +
                        "</wp:positionV>"
                        + "<wp:extent cx=\"" + width + "\" cy=\"" + height + "\"/>"
                        + "<wp:effectExtent l=\"0\" t=\"0\" r=\"0\" b=\"0\"/>"
                        + "<wp:wrapNone/>"
                        + "<wp:docPr id=\"1\" name=\"Drawing 0\" descr=\"" + deskFileName + "\"/><wp:cNvGraphicFramePr/>"
                        + "</wp:anchor>";

        CTDrawing drawing = null;
        try {
            drawing = (CTDrawing)CTDrawing.Factory.parse(anchorXML);
        } catch (XmlException e) {
            LogUtil.getLog(PoiTlUtil.class).error(e);
        }
        CTAnchor anchor = drawing.getAnchorArray(0);
        anchor.setGraphic(ctGraphicalObject);
        return anchor;
    }

    /**
     * @param contentFilePath  公文地址
     * @param data     需要填写的参数
     * @param destFilePath 保存的文件地址
     */
    public static void toRedDocument(String templFilePath, String contentFilePath, Map<String, String> data, String destFilePath, boolean isTemplTail) throws Exception {
        //模板文件地址
        // String templFilePath = "E:\\云网OA\\模板\\公文模板.docx";
        //模板文件 参数填写
        XWPFTemplate template = XWPFTemplate.compile(templFilePath).render(data);
        //获取模板文件  公文
        NiceXWPFDocument main = template.getXWPFDocument();

        NiceXWPFDocument sub = new NiceXWPFDocument(new FileInputStream(contentFilePath));
        // 合并两个文档
        NiceXWPFDocument newDoc;
        if (!isTemplTail) {
            newDoc = main.merge(sub);
        }
        else {
            newDoc = sub.merge(main);
        }
        // 生成新文档
        FileOutputStream out = new FileOutputStream(destFilePath);
        newDoc.write(out);
        newDoc.close();
        out.close();
    }

    /**
     * @param contentFilePath  公文地址
     * @param data     需要填写的参数
     * @param destFilePath 保存的文件地址
     */
    public static void sealDocumentByText(String templFilePath, String contentFilePath, Map<String, String> data, String destFilePath, boolean isTemplTail) throws Exception {
        //模板文件地址
        // String templFilePath = "E:\\云网OA\\模板\\公文模板.docx";
        //模板文件 参数填写
        XWPFTemplate template = XWPFTemplate.compile(templFilePath).render(data);
        //获取模板文件  公文
        NiceXWPFDocument main = template.getXWPFDocument();

        NiceXWPFDocument sub = new NiceXWPFDocument(new FileInputStream(contentFilePath));
        // 合并两个文档
        NiceXWPFDocument newDoc;
        if (!isTemplTail) {
            newDoc = main.merge(sub);
        }
        else {
            newDoc = sub.merge(main);
        }
        // 生成新文档
        FileOutputStream out = new FileOutputStream(destFilePath);

        // 在段落中寻找***签章，如果想要做得更完美，可以考虑通过targetRun.get(i).setText(...)方法替换掉***签章字符;
        XWPFParagraph paragraph = null;
        //XWPFRun targetRun = null;
        List<XWPFParagraph> xwpfParagraphList = newDoc.getParagraphs();
        for (XWPFParagraph x : xwpfParagraphList) {
            String text = x.getText();
            if (text.contains("盖章")) {
                paragraph = x;
                break;
            }
        }
        if (paragraph != null) {
            //添加印章图片
            XWPFRun targetRun = paragraph.createRun();
            //模板文件地址
            String stampPath = "E:\\云网OA\\模板\\印章.png";
            InputStream inputStream = new FileInputStream(stampPath);
            targetRun.addPicture(inputStream, XWPFDocument.PICTURE_TYPE_JPEG, "sign", Units.toEMU(100), Units.toEMU(100));
            inputStream.close();
            log.info(" 盖章...");

            CTR targetRunCtr = targetRun.getCTR();
            if (targetRunCtr.sizeOfDrawingArray() > 0) {
                CTDrawing drawing = targetRunCtr.getDrawingArray(0);
                if (drawing.sizeOfInlineArray() > 0) {
                    CTGraphicalObject graphic = drawing.getInlineArray(0).getGraphic();
                    //log.info("graphic   " + graphic.toString());
                    //拿到新插入的图片替换添加CTAnchor 设置浮动属性 删除inline属性
                    CTAnchor anchor = getAnchorWithGraphic(graphic, "Seal" + System.currentTimeMillis(),
                            Units.toEMU(100), Units.toEMU(100),
                            Units.toEMU(250), Units.toEMU(0), false);
                    //log.info("anchor   " + anchor.toString());
                    drawing.setAnchorArray(new CTAnchor[]{anchor});
                    drawing.removeInline(0);
                }
                //log.info("drawing   " + drawing.toString());
            }
        }

        //输出文件
        newDoc.write(out);
        newDoc.close();
        out.close();
    }

    /**
     * 删除印章位置书签，以免重复盖章
     * @param para
     * @param bookMarkName
     */
    public static void removeParagraphBookMark(XWPFParagraph para, String bookMarkName) {
        List<CTBookmark> bookmarkStartList = para.getCTP()
                .getBookmarkStartList();
        if (bookmarkStartList == null) {
            return;
        }
        for (int i = bookmarkStartList.size() - 1; i >= 0; i--) {
            CTBookmark ctBookmark = bookmarkStartList.get(i);
            if (ctBookmark.getName().equals(bookMarkName)) {
                Node bookStartNode = ctBookmark.getDomNode();
                // printNodeAllAttributeValue(bookStartNode);
                bookStartNode.getParentNode().removeChild(bookStartNode);
                break;
            }
        }
        /*List<CTMarkupRange> bookmarkEndList = para.getCTP().getBookmarkEndList();
        if (bookmarkEndList == null) {
            return;
        }
        for (int i = bookmarkEndList.size() - 1; i >= 0; i--) {
            CTMarkupRange ctMarkupRange =  bookmarkEndList.get(i);
            Node bookEndNode = ctMarkupRange.getDomNode();
            // printNodeAllAttributeValue(bookEndNode);
            bookEndNode.getParentNode().removeChild(bookEndNode);
        }*/
    }

    public void printNodeAllAttributeValue(Node node) {
        NamedNodeMap nodeAttr = node.getAttributes();
        if (nodeAttr != null) {
            int numAttrs = nodeAttr.getLength();
            for (int i = 0; i < numAttrs; i++) {
                Node attr = nodeAttr.item(i);
                String attrName = attr.getNodeName();
                String attrValue = attr.getNodeValue();
            }
        }
    }

    public static void sealDocumentByBookmark(String filePath, int stampId) throws IOException, org.apache.poi.openxml4j.exceptions.InvalidFormatException {
        XWPFParagraph currentParagraph = null;
        File fileTem = new File(filePath);
        InputStream iss = new FileInputStream(fileTem);
        XWPFDocument doc = new XWPFDocument(iss);
        FileOutputStream fos = null;

        StampDb stampDb = new StampDb();
        stampDb = stampDb.getStampDb(stampId);

        // 通过书签盖章
        List<XWPFParagraph> xwpfParagraphList = doc.getParagraphs();
        for (XWPFParagraph x : xwpfParagraphList) {
            List<CTBookmark> bookmarkList = x.getCTP().getBookmarkStartList();
            for (CTBookmark ctBookmark : bookmarkList) {
                if (stampDb.getTitle().equals(ctBookmark.getName())) {
                    currentParagraph = x;
                    break;
                }
            }
        }
        try {
            if (currentParagraph != null) {
                //加盖印章图片
                XWPFRun run = currentParagraph.createRun();

                String imgFile1 = Global.getRealPath() + stampDb.getImageUrl();
                FileInputStream is1 = new FileInputStream(imgFile1);
                run.addPicture(is1, XWPFDocument.PICTURE_TYPE_JPEG, imgFile1, Units.toEMU(60), Units.toEMU(60));
                is1.close();

                CTDrawing drawing1 = run.getCTR().getDrawingArray(0);
                CTGraphicalObject graphicalobject1 = drawing1.getInlineArray(0).getGraphic();
                Random random = new Random();
                int number = random.nextInt(999) + 1;
                //拿到新插入的图片替换添加CTAnchor 设置浮动属性 删除inline属性
                CTAnchor anchor1 = getAnchorWithGraphic(graphicalobject1, "Seal" + number,
                        Units.toEMU(120), Units.toEMU(120),//图片大小
                        Units.toEMU(250), Units.toEMU(-50), true);//相对当前段落位置及偏移
                drawing1.setAnchorArray(new CTAnchor[]{anchor1});//添加浮动属性
                drawing1.removeInline(0);//删除行内属性

                // 删除印章位置书签，以免重复盖章
                removeParagraphBookMark(currentParagraph, stampDb.getTitle());

                fos = new FileOutputStream(filePath);
                doc.write(fos);

                // 记录印章使用日志
                StampLogDb sld = new StampLogDb();
                sld.create(new com.cloudwebsoft.framework.db.JdbcTemplate(), new Object[]{com.redmoon.oa.db.SequenceManager.nextID(com.redmoon.oa.db.SequenceManager.OA_STAMP_LOG),SpringUtil.getUserName(), stampId,new java.util.Date(), StrUtil.getIp(SpringUtil.getRequest())});
            } else {
                throw new ErrMsgException("文件已被盖章或未找到印章的加盖位置");
            }
        } catch (ResKeyException e) {
            LogUtil.getLog(PoiTlUtil.class).error(e);
        } finally {
            if (fos!=null) {
                fos.close();
            }
            iss.close();
            doc.close();
        }
    }

    // 插入印章及签名图片
    public void testSealDocument() throws IOException, InvalidFormatException, org.apache.poi.openxml4j.exceptions.InvalidFormatException {
        XWPFParagraph currentParagraph = null;
        File fileTem = new File("d:\\关于在全市开展活动的通知.docx");
        InputStream iss = new FileInputStream(fileTem);
        XWPFDocument doc = new XWPFDocument(iss);

        // 通过书签盖章
        List<XWPFParagraph> xwpfParagraphList = doc.getParagraphs();
        for (XWPFParagraph x : xwpfParagraphList) {
            List<CTBookmark> bookmarkList = x.getCTP().getBookmarkStartList();
            for (CTBookmark ctBookmark : bookmarkList) {
                if ("sign".equals(ctBookmark.getName())) {
                    currentParagraph = x;
                    break;
                }
            }
        }
        if (currentParagraph != null) {
            //添加印章图片
            XWPFRun run = currentParagraph.createRun();

            String imgFile1 = "d:\\t2.jpg";
            FileInputStream is1 = new FileInputStream(imgFile1);
            run.addPicture(is1, XWPFDocument.PICTURE_TYPE_JPEG, imgFile1, Units.toEMU(60), Units.toEMU(60));
            is1.close();

            CTDrawing drawing1 = run.getCTR().getDrawingArray(0);
            CTGraphicalObject graphicalobject1 = drawing1.getInlineArray(0).getGraphic();
            Random random = new Random();
            int number = random.nextInt(999) + 1;
            //拿到新插入的图片替换添加CTAnchor 设置浮动属性 删除inline属性
            CTAnchor anchor1 = getAnchorWithGraphic(graphicalobject1, "Seal" + number,
                    Units.toEMU(60), Units.toEMU(60),//图片大小
                    Units.toEMU(250), Units.toEMU(0), true);//相对当前段落位置及偏移
            drawing1.setAnchorArray(new CTAnchor[]{anchor1});//添加浮动属性
            drawing1.removeInline(0);//删除行内属性
            //添加签名图片
            run = currentParagraph.createRun();
            imgFile1 = "d:\\t1.jpg";
            FileInputStream is2 = new FileInputStream(imgFile1);
            run.addPicture(is2, XWPFDocument.PICTURE_TYPE_JPEG, imgFile1, Units.toEMU(60), Units.toEMU(60));
            is2.close();

            random = new Random();
            CTDrawing drawing2 = run.getCTR().getDrawingArray(0);
            CTGraphicalObject graphicalobject2 = drawing2.getInlineArray(0).getGraphic();
            number = random.nextInt(999) + 1;
            CTAnchor anchor2 = getAnchorWithGraphic(graphicalobject2, "Seal" + number,
                    Units.toEMU(60), Units.toEMU(40),//图片大小
                    Units.toEMU(300), Units.toEMU(-5), false);
            drawing2.setAnchorArray(new CTAnchor[]{anchor2});//添加浮动属性
            drawing2.removeInline(0);//删除行内属性

            doc.write(new FileOutputStream("d:\\关于在全市开展活动的通知2.docx"));
            iss.close();
        }
        doc.close();
    }
}

