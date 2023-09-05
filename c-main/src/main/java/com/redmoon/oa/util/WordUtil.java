package com.redmoon.oa.util;

import com.cloudwebsoft.framework.util.LogUtil;
import org.apache.poi.hwpf.HWPFDocument;
import org.apache.poi.hwpf.extractor.WordExtractor;
import org.apache.poi.poifs.filesystem.*;
import org.apache.poi.xwpf.usermodel.*;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTStyle;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTStyles;

import java.io.*;
import java.nio.charset.StandardCharsets;

public class WordUtil {

    public static void htmlToWord(String content, String fileName) throws Exception {
        ByteArrayInputStream bais = null;
        FileOutputStream fos = null;
        try {
            if (!"".equals(fileName)) {
                // File file = new File(fileName);
                byte b[] = content.getBytes("GBK");
                bais = new ByteArrayInputStream(b);
                POIFSFileSystem poifs = new POIFSFileSystem();
                DirectoryEntry directory = poifs.getRoot();
                DocumentEntry documentEntry = directory.createDocument("WordDocument", bais);
                fos = new FileOutputStream(fileName);
                poifs.writeFilesystem(fos);
                bais.close();
                fos.close();
            }
        } catch (IOException e) {
            LogUtil.getLog(WordUtil.class).error(e);
        } finally {
            if (fos != null) {
                fos.close();
            }
            if (bais != null) {
                bais.close();
            }
        }
    }

    /**
     * 将html转为二进制word文件并输出至流，word文件默认打开为页面视图
     * 注意输出文件中的编码需为：response.setContentType("application/msword; charset=gb2312"); 否则word中会出现乱码
     * @param html
     * @param bos
     * @throws Exception
     */
    public static void htmlToWord(String html, BufferedOutputStream bos) throws Exception {

        // 加上header，可便默认打开为页面视图而不是web视图
        String header = "<!DOCTYPE html PUBLIC \"-//W3C//DTD HTML 4.01 Transitional//EN\" \"http://www.w3.org/TR/html4/loose.dtd\">\n" +
                "<html xmlns:v=\"urn:schemas-microsoft-com:vml\" xmlns:o=\"urn:schemas-microsoft-com:office:office\"\n" +
                "xmlns:w=\"urn:schemas-microsoft-com:office:word\" xmlns:m=\"http://schemas.microsoft.com/office/2004/12/omml\"\n" +
                "xmlns=\"http://www.w3.org/TR/REC-html40\">\n" +
                "<head>\n" +
                "<meta http-equiv=\"Content-Type\" content=\"text/html; charset=gb2312\" />\n" +
                "<!--[if gte mso 9]><xml><w:WordDocument><w:View>Print</w:View><w:TrackMoves>false</w:TrackMoves><w:TrackFormatting/><w:ValidateAgainstSchemas/><w:SaveIfXMLInvalid>false</w:SaveIfXMLInvalid><w:IgnoreMixedContent>false</w:IgnoreMixedContent><w:AlwaysShowPlaceholderText>false</w:AlwaysShowPlaceholderText><w:DoNotPromoteQF/><w:LidThemeOther>EN-US</w:LidThemeOther><w:LidThemeAsian>ZH-CN</w:LidThemeAsian><w:LidThemeComplexScript>X-NONE</w:LidThemeComplexScript><w:Compatibility><w:BreakWrappedTables/><w:SnapToGridInCell/><w:WrapTextWithPunct/><w:UseAsianBreakRules/><w:DontGrowAutofit/><w:SplitPgBreakAndParaMark/><w:DontVertAlignCellWithSp/><w:DontBreakConstrainedForcedTables/><w:DontVertAlignInTxbx/><w:Word11KerningPairs/><w:CachedColBalance/><w:UseFELayout/></w:Compatibility><w:BrowserLevel>MicrosoftInternetExplorer4</w:BrowserLevel><m:mathPr><m:mathFont m:val=\"Cambria Math\"/><m:brkBin m:val=\"before\"/><m:brkBinSub m:val=\"--\"/><m:smallFrac m:val=\"off\"/><m:dispDef/><m:lMargin m:val=\"0\"/> <m:rMargin m:val=\"0\"/><m:defJc m:val=\"centerGroup\"/><m:wrapIndent m:val=\"1440\"/><m:intLim m:val=\"subSup\"/><m:naryLim m:val=\"undOvr\"/></m:mathPr></w:WordDocument></xml><![endif]--> ";
        String style = "<style> @font-face\n" +
                "\t{font-family:宋体;\n" +
                "\tpanose-1:2 1 6 0 3 1 1 1 1 1;}\n" +
                "@font-face\n" +
                "\t{font-family:\"Cambria Math\";\n" +
                "\tpanose-1:2 4 5 3 5 4 6 3 2 4;}\n" +
                "@font-face\n" +
                "\t{font-family:Calibri;\n" +
                "\tpanose-1:2 15 5 2 2 2 4 3 2 4;}\n" +
                "@font-face\n" +
                "\t{font-family:Consolas;\n" +
                "\tpanose-1:2 11 6 9 2 2 4 3 2 4;}\n" +
                "@font-face\n" +
                "\t{font-family:方正小标宋_GBK;}\n" +
                "@font-face\n" +
                "\t{font-family:方正黑体_GBK;}\n" +
                "@font-face\n" +
                "\t{font-family:方正仿宋简体;}\n" +
                "@font-face\n" +
                "\t{font-family:\"\\@宋体\";\n" +
                "\tpanose-1:2 1 6 0 3 1 1 1 1 1;}\n" +
                "@font-face\n" +
                "\t{font-family:\"\\@方正小标宋_GBK\";}\n" +
                "@font-face\n" +
                "\t{font-family:\"\\@方正黑体_GBK\";}\n" +
                "@font-face\n" +
                "\t{font-family:\"\\@方正仿宋简体\";}\n" +
                " /* Style Definitions */\n" +
                " p.MsoNormal, li.MsoNormal, div.MsoNormal\n" +
                "\t{margin:0cm;\n" +
                "\tmargin-bottom:.0001pt;\n" +
                "\ttext-align:justify;\n" +
                "\ttext-justify:inter-ideograph;\n" +
                "\tfont-size:10.5pt;\n" +
                "\tfont-family:\"Calibri\",\"sans-serif\";}\n" +
                "p.MsoHeader, li.MsoHeader, div.MsoHeader\n" +
                "\t{mso-style-link:\"页眉 Char\";\n" +
                "\tmargin:0cm;\n" +
                "\tmargin-bottom:.0001pt;\n" +
                "\ttext-align:center;\n" +
                "\tlayout-grid-mode:char;\n" +
                "\tborder:none;\n" +
                "\tpadding:0cm;\n" +
                "\tfont-size:9.0pt;\n" +
                "\tfont-family:\"Calibri\",\"sans-serif\";}\n" +
                "p.MsoFooter, li.MsoFooter, div.MsoFooter\n" +
                "\t{mso-style-link:\"页脚 Char\";\n" +
                "\tmargin:0cm;\n" +
                "\tmargin-bottom:.0001pt;\n" +
                "\tlayout-grid-mode:char;\n" +
                "\tfont-size:9.0pt;\n" +
                "\tfont-family:\"Calibri\",\"sans-serif\";}\n" +
                "a:link, span.MsoHyperlink\n" +
                "\t{color:#337AB7;\n" +
                "\ttext-decoration:none;}\n" +
                "a:visited, span.MsoHyperlinkFollowed\n" +
                "\t{color:#337AB7;\n" +
                "\ttext-decoration:none;}\n" +
                "strong\n" +
                "\t{color:black;}\n" +
                "code\n" +
                "\t{font-family:Consolas;\n" +
                "\tcolor:#C7254E;\n" +
                "\tbackground:#F9F2F4;}\n" +
                "kbd\n" +
                "\t{font-family:Consolas;\n" +
                "\tcolor:white;\n" +
                "\tbackground:#333333;}\n" +
                "samp\n" +
                "\t{font-family:Consolas;}\n" +
                "span.Char\n" +
                "\t{mso-style-name:\"页眉 Char\";\n" +
                "\tmso-style-link:页眉;\n" +
                "\tfont-family:\"Calibri\",\"sans-serif\";}\n" +
                "span.Char0\n" +
                "\t{mso-style-name:\"页脚 Char\";\n" +
                "\tmso-style-link:页脚;\n" +
                "\tfont-family:\"Calibri\",\"sans-serif\";}\n" +
                ".MsoChpDefault\n" +
                "\t{font-size:10.0pt;}\n" +
                " /* Page Definitions */\n" +
                " @page WordSection1\n" +
                "\t{size:595.3pt 841.9pt;\n" +
                "\tmargin:104.9pt 79.35pt 3.0cm 79.35pt;\n" +
                "\tlayout-grid:15.6pt;}\n" +
                "div.WordSection1\n" +
                "\t{page:WordSection1;}</style>";

        // html = header + style + "</head><body lang=ZH-CN link=\"#337AB7\" vlink=\"#337AB7\" style='text-justify-trim:punctuation'>" + html;
        html = header + "</head><body lang=ZH-CN link=\"#337AB7\" vlink=\"#337AB7\" style='text-justify-trim:punctuation'>" + html;
        html += "</body></html>";

        ByteArrayInputStream bais = null;
        try {
            byte b[] = html.getBytes("GBK");
            bais = new ByteArrayInputStream(b);
            POIFSFileSystem poifs = new POIFSFileSystem();
            DirectoryEntry directory = poifs.getRoot();
            DocumentEntry documentEntry = directory.createDocument("WordDocument", bais);

            // poi只能将转换为doc文件，不能转为docx文件，通过doc4j可以将html转换为docx，但是样式差异较大（而且规则多，需用jsoup过滤，详见HtmlConverter），所以弃用
            // 其它除了收费的，就只有通过jacob来做了
            // 如果一定要获得细线表格样式，那只能用word模板，然后用poi来替换字符(详见document目录下面的文档：使用POI替换word中的特定字符/文字改进版.htm)，如果有多条记录需转换的话，得再拼接文件

            // 使用HWPFDocument处理DOC，使用XWPFDocument处理docx
            /*InputStream is = getDocumentInputStream(documentEntry);
            HWPFDocument doc = new HWPFDocument(is);
            Range range = doc.getRange();//得到文档的读取范围
            TableIterator it = new TableIterator(range);
            while (it.hasNext()) {
                Table table = (Table) it.next();
            }*/

            /*XWPFDocument doc = new XWPFDocument(is);
            XWPFTable table = doc.getTables().get(0);
            XWPFStyles styles = doc.createStyles();
            XWPFStyle xwpfStyle = createTableStyle(styles, "ListTableStyle");
            table.setStyleID(xwpfStyle.getStyleId());*/

            poifs.writeFilesystem(bos);
        } catch (IOException e) {
            LogUtil.getLog(WordUtil.class).error(e);
        } finally {
            if (bais != null) {
                bais.close();
            }
        }
    }

    public static InputStream getDocumentInputStream(DocumentEntry document) throws IOException {
        if (!(document instanceof DocumentNode)) {
            throw new IOException("Cannot open internal document storage");
        }
        InputStream delegate;
        DocumentNode documentNode = (DocumentNode)document;
        DirectoryNode parentNode = (DirectoryNode)document.getParent();
        if(parentNode.getFileSystem() != null) {
            delegate = new DocumentInputStream(document);
        } else {
            parentNode.getFileSystem();
            delegate = new DocumentInputStream(document);
            // throw new IOException("No FileSystem bound on the parent, can't read contents");
        }
        return delegate;
    }

    /**
     * 读取html文件写至word文件
     *
     * @param inputPath
     *            html文件的路径
     * @return
     * @throws Exception
     */
    public boolean writeWordFile(String inputPath, String outputPath) throws Exception {
        InputStream is = null;
        FileOutputStream fos = null;

        // 1 找不到源文件, 则返回false
        File inputFile = new File(inputPath);
        if (!inputFile.exists()) {
            return false;
        }

        File outputFile = new File(outputPath);
        // 如果目标路径不存在 则新建该路径
        if (!outputFile.getParentFile().exists()) {
            outputFile.getParentFile().mkdirs();
        }

        try {
            // 将html文件内容写入doc文件
            is = new FileInputStream(inputFile);
            POIFSFileSystem poifs = new POIFSFileSystem();
            DirectoryEntry directory = poifs.getRoot();
            directory.createDocument("WordDocument", is);

            fos = new FileOutputStream(outputPath);
            poifs.writeFilesystem(fos);
            return true;
        } catch (IOException e) {
            LogUtil.getLog(WordUtil.class).error(e);
        } finally {
            if (fos != null) {
                fos.close();
            }
            if (is != null) {
                is.close();
            }
        }
        return false;
    }

    public void test3() {
        POIFSFileSystem fs = null;

        try {
            fs = new POIFSFileSystem(new FileInputStream("C:/Users/312845/Desktop/a.doc"));

            HWPFDocument doc = new HWPFDocument(fs);
            WordExtractor we = new WordExtractor(doc);

            OutputStream file = new FileOutputStream(new File("C:/Users/312845/Desktop/test.docx"));
        } catch (Exception e) {
            LogUtil.getLog(WordUtil.class).error(e);
        }
    }
    
    public void test() throws Exception {
        XWPFDocument doc = new XWPFDocument(new FileInputStream("d:/123.docx"));
        XWPFTable table = doc.getTables().get(0);
        // XWPFTable table = doc.getTableArray(0);

        XWPFStyles styles = doc.createStyles();
        XWPFStyle style = createTableStyle(styles, "ListTableStyle");
        table.setStyleID(style.getStyleId());
/*
        //insert new row, which is a copy of row 2, as new row 3:
        XWPFTableRow oldRow = table.getRow(1);
        CTRow ctrow = CTRow.Factory.parse(oldRow.getCtRow().newInputStream());
        XWPFTableRow newRow = new XWPFTableRow(ctrow, table);

        int i = 1;
        for (XWPFTableCell cell : newRow.getTableCells()) {
            for (XWPFParagraph paragraph : cell.getParagraphs()) {
                for (XWPFRun run : paragraph.getRuns()) {
                    run.setText("New row 3 cell " + i++, 0);
                }
            }
        }

        table.addRow(newRow, 2);

        //insert new last row, which is a copy previous last row:
        XWPFTableRow lastRow = table.getRows().get(table.getNumberOfRows() - 1);
        ctrow = CTRow.Factory.parse(lastRow.getCtRow().newInputStream());
        newRow = new XWPFTableRow(ctrow, table);

        i = 1;
        for (XWPFTableCell cell : newRow.getTableCells()) {
            for (XWPFParagraph paragraph : cell.getParagraphs()) {
                for (XWPFRun run : paragraph.getRuns()) {
                    run.setText("New last row cell " + i++, 0);
                }
            }
        }

        table.addRow(newRow);*/

        doc.write(new FileOutputStream("d:/result.docx"));
        // doc.close();
    }

    public void test1() throws Exception {
        XWPFDocument document = new XWPFDocument();

        XWPFParagraph paragraph = document.createParagraph();
        // XWPFRun run = paragraph.createRun();
        // run.setText("The table");
        XWPFTable table = document.createTable(6, 4);
        for (int r = 0; r < 6; r++) {
            for (int c = 0; c < 4; c++) {
                XWPFTableCell cell = table.getRow(r).getCell(c);
                cell.setText("row " + (r+1) + ", col " + (c+1));
            }
        }

        // table.removeBorders();

        XWPFStyles styles = document.createStyles();
        XWPFStyle style = createTableStyle(styles, "ListTableStyle");
        table.setStyleID(style.getStyleId());

        FileOutputStream out = new FileOutputStream("d:/CreateWordTable.docx");
        document.write(out);
        out.close();
    }

    private static XWPFStyle createTableStyle(XWPFStyles styles, String styleId) throws Exception {
        if (styles == null || styleId == null) {
            return null;
        }
        String tableStyleXML =
                "<w:style xmlns:w=\"http://schemas.openxmlformats.org/wordprocessingml/2006/main\" w:styleId=\"" + styleId + "\" w:type=\"table\">"
                        + "<w:name w:val=\"" + styleId + "\"/>"
                        + "<w:pPr><w:spacing w:lineRule=\"auto\" w:line=\"240\" w:after=\"0\"/></w:pPr>"
                        + "<w:tblPr>"
                        + "<w:tblStyleRowBandSize w:val=\"1\"/><w:tblStyleColBandSize w:val=\"1\"/>"
                        + "<w:tblBorders>"
                        + "<w:top w:val=\"single\" w:themeTint=\"99\" w:themeColor=\"text1\" w:color=\"666666\" w:space=\"0\" w:sz=\"4\"/>"
                        + "<w:bottom w:val=\"single\" w:themeTint=\"99\" w:themeColor=\"text1\" w:color=\"666666\" w:space=\"0\" w:sz=\"4\"/>"
                        + "<w:insideH w:val=\"single\" w:themeTint=\"99\" w:themeColor=\"text1\" w:color=\"666666\" w:space=\"0\" w:sz=\"4\"/>"
                        + "</w:tblBorders>"
                        + "</w:tblPr>"
                        + "<w:tblStylePr w:type=\"firstRow\"><w:rPr><w:b/><w:bCs/></w:rPr></w:tblStylePr>"
                        + "<w:tblStylePr w:type=\"lastRow\"><w:rPr><w:b/><w:bCs/></w:rPr></w:tblStylePr>"
                        + "<w:tblStylePr w:type=\"firstCol\"><w:rPr><w:b/><w:bCs/></w:rPr></w:tblStylePr>"
                        + "<w:tblStylePr w:type=\"lastCol\"><w:rPr><w:b/><w:bCs/></w:rPr></w:tblStylePr>"
                        + "<w:tblStylePr w:type=\"band1Vert\"><w:tblPr/><w:tcPr><w:shd w:val=\"clear\" w:color=\"auto\" w:themeFillTint=\"33\" w:themeFill=\"text1\" w:fill=\"CCCCCC\"/></w:tcPr></w:tblStylePr>"
                        + "<w:tblStylePr w:type=\"band1Horz\"><w:tblPr/><w:tcPr><w:shd w:val=\"clear\" w:color=\"auto\" w:themeFillTint=\"33\" w:themeFill=\"text1\" w:fill=\"CCCCCC\"/></w:tcPr></w:tblStylePr>"
                        + "</w:style>";

        CTStyles ctStyles = (CTStyles)CTStyles.Factory.parse(tableStyleXML);
        CTStyle ctStyle = ctStyles.getStyleArray(0);

        XWPFStyle style = styles.getStyle(styleId);
        if (style == null) {
            style = new XWPFStyle(ctStyle, styles);
            styles.addStyle(style);
        } else {
            style.setStyle(ctStyle);
        }

        return style;
    }

    public static void main(String[] args) throws Exception {

        // new WordUtil().writeWordFile("d:/111.htm", "d:/111.doc");
        new WordUtil().test();
    }
}
