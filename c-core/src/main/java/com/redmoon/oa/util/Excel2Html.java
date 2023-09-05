package com.redmoon.oa.util;

import cn.js.fan.util.file.FileUtil;
import com.cloudwebsoft.framework.util.LogUtil;
import com.redmoon.oa.sys.DebugUtil;
import org.apache.commons.io.FileUtils;
import org.apache.poi.hssf.converter.ExcelToHtmlConverter;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.hwpf.usermodel.Picture;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.w3c.dom.Document;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.*;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.*;
import java.util.List;

public class Excel2Html {

    public static void xlsToHtml(String filePath) {
        String fileName = FileUtil.getFileName(filePath);
        String path = filePath.substring(0, filePath.lastIndexOf(fileName) - 1);
        String name = FileUtil.getFileNameWithoutExt(fileName);
        String htmlName = name + ".html";

        InputStream is = null;
        try {
            is = new FileInputStream(filePath);
            HSSFWorkbook excelBook = new HSSFWorkbook(is);
            ExcelToHtmlConverter excelToHtmlConverter = new ExcelToHtmlConverter(DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument());
            excelToHtmlConverter.processWorkbook(excelBook);
            List pics = excelBook.getAllPictures();
            if (pics != null) {
                for (int i = 0; i < pics.size(); i++) {
                    Picture pic = (Picture) pics.get(i);
                    try {
                        pic.writeImageContent(new FileOutputStream(path + pic.suggestFullFileName()));
                    } catch (FileNotFoundException e) {
                        LogUtil.getLog(Excel2Html.class).error(e);
                    }
                }
            }
            Document htmlDocument = excelToHtmlConverter.getDocument();
            ByteArrayOutputStream outStream = new ByteArrayOutputStream();
            DOMSource domSource = new DOMSource(htmlDocument);
            StreamResult streamResult = new StreamResult(outStream);
            TransformerFactory tf = TransformerFactory.newInstance();
            Transformer serializer = tf.newTransformer();
            serializer.setOutputProperty(OutputKeys.ENCODING, "GBK");
            serializer.setOutputProperty(OutputKeys.INDENT, "yes");
            serializer.setOutputProperty(OutputKeys.METHOD, "html");
            serializer.transform(domSource, streamResult);
            outStream.close();
            String content = new String(outStream.toByteArray());
            content = content.replace("<table ", "<table border='1' align='center' ").replace("<h2>Table1</h2>", "");
            FileUtils.writeStringToFile(new File(path, htmlName), content, "GBK");
        } catch (Exception e) {
            // poi对于xlsx的文件支持不好，会报空指针错误，为防止出现NullPointerException时捕获不了，故捕获Exception
            DebugUtil.e(Excel2Html.class, "xlsToHtml", filePath + "生成html预览失败");
            LogUtil.getLog(Excel2Html.class).error(e);
        } finally {
            if (is!=null) {
                try {
                    is.close();
                } catch (IOException e) {
                    LogUtil.getLog(Excel2Html.class).error(e);
                }
            }
        }
    }

    /**
     * excel07转html
     * filename:要读取的文件所在文件夹
     * filepath:文件名
     * htmlname:生成html名称
     * path:html存放路径
     */
    public static void xlsxToHtml(String filePath) {
        String fileName = FileUtil.getFileName(filePath);
        String path = filePath.substring(0, filePath.lastIndexOf(fileName) - 1);
        String name = FileUtil.getFileNameWithoutExt(fileName);
        String htmlName = name + ".html";

        Workbook workbook = null;
        InputStream is = null;
        try {
            is = new FileInputStream(filePath);
            String html = "";
            workbook = new XSSFWorkbook(is);
            for (int numSheet = 0; numSheet < workbook.getNumberOfSheets(); numSheet++) {
                Sheet sheet = workbook.getSheetAt(numSheet);
                if (sheet == null) {
                    continue;
                }
                // html += "=======================" + sheet.getSheetName() + "=========================<br><br>";

                int firstRowIndex = sheet.getFirstRowNum();
                int lastRowIndex = sheet.getLastRowNum();
                html += "<table border='1' align='center' style='border-collapse:collapse;border-spacing:0;'>";
                Row firstRow = sheet.getRow(firstRowIndex);
                for (int i = firstRow.getFirstCellNum(); i <= firstRow.getLastCellNum(); i++) {
                    Cell cell = firstRow.getCell(i);
                    String cellValue = getCellValue(cell, true);
                    html += "<th>" + cellValue + "</th>";
                }

                //行
                for (int rowIndex = firstRowIndex + 1; rowIndex <= lastRowIndex; rowIndex++) {
                    Row currentRow = sheet.getRow(rowIndex);
                    html += "<tr>";
                    if (currentRow != null) {

                        int firstColumnIndex = currentRow.getFirstCellNum();
                        int lastColumnIndex = currentRow.getLastCellNum();
                        //列
                        for (int columnIndex = firstColumnIndex; columnIndex <= lastColumnIndex; columnIndex++) {
                            Cell currentCell = currentRow.getCell(columnIndex);
                            String currentCellValue = getCellValue(currentCell, true);
                            html += "<td>" + currentCellValue + "</td>";
                        }
                    } else {
                        html += " ";
                    }
                    html += "</tr>";
                }
                html += "</table>";


                ByteArrayOutputStream outStream = new ByteArrayOutputStream();
                DOMSource domSource = new DOMSource();
                StreamResult streamResult = new StreamResult(outStream);

                TransformerFactory tf = TransformerFactory.newInstance();
                Transformer serializer = tf.newTransformer();
                serializer.setOutputProperty(OutputKeys.ENCODING, "GBK");
                serializer.setOutputProperty(OutputKeys.INDENT, "yes");
                serializer.setOutputProperty(OutputKeys.METHOD, "html");
                serializer.transform(domSource, streamResult);
                outStream.close();
                FileUtils.writeStringToFile(new File(path, htmlName), html, "GBK");
            }
        } catch (Exception e) {
            // poi对于xlsx的文件支持不好，会报空指针错误，为防止出现NullPointerException时捕获不了，故捕获Exception
            DebugUtil.e(Excel2Html.class, "xlsxToHtml", filePath + "生成html预览失败");
            LogUtil.getLog(Excel2Html.class).error(e);
        } finally {
            if (is != null) {
                try {
                    is.close();
                } catch (IOException e) {
                    LogUtil.getLog(Excel2Html.class).error(e);
                }
            }
        }
    }

    /**
     * 读取单元格
     */
    private static String getCellValue(Cell cell, boolean treatAsStr) {
        if (cell == null) {
            return "";
        }

        if (treatAsStr) {
            cell.setCellType(CellType.STRING);
        }

        if (cell.getCellType() == CellType.BOOLEAN) {
            return String.valueOf(cell.getBooleanCellValue());
        } else if (cell.getCellType() == CellType.NUMERIC) {
            return String.valueOf(cell.getNumericCellValue());
        } else {
            return String.valueOf(cell.getStringCellValue());
        }
    }

    public static void main(String args[]) throws Exception {
        xlsToHtml ("e:\\test\\2.xls");
        xlsxToHtml ("e:\\test\\3.xlsx");
    }
}
