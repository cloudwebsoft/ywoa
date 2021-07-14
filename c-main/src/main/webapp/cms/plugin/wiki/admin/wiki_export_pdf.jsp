<%@ page contentType="text/html; charset=utf-8"%>
<%@ page import = "java.util.*"%>
<%@ page import = "cn.js.fan.util.*"%>
<%@ page import = "cn.js.fan.web.*"%>
<%@ page import = "cn.js.fan.module.cms.plugin.wiki.*"%>
<%@ page import = "com.redmoon.oa.fileark.Document"%>
<%@ page import = "com.redmoon.oa.fileark.Leaf"%>
<%@ page import = "com.redmoon.oa.fileark.LeafChildrenCacheMgr"%>
<%@ page import = "com.itextpdf.text.*"%>
<%@ page import = "com.itextpdf.text.pdf.*"%>
<%@ page import = "com.itextpdf.text.html.simpleparser.*"%>
<%@ page import = "java.io.*"%>
<jsp:useBean id="privilege" scope="page" class="cn.js.fan.module.pvg.Privilege"/>
<%
	// 本页格式不是很友好，半成品，待用
	response.reset();
    response.setContentType("application/pdf");

	String userName = ParamUtil.get(request,"userName");
	
	String dirCode = ParamUtil.get(request, "dirCode");
	if (dirCode.equals(""))
		dirCode = Leaf.CODE_WIKI;
	
	com.itextpdf.text.Document document = new com.itextpdf.text.Document(PageSize.A4);
	ByteArrayOutputStream buffer = new ByteArrayOutputStream();
	PdfWriter.getInstance(document, buffer);
	
	StyleSheet st = new StyleSheet();   
    st.loadTagStyle("body", "leading", "16,0");   

    document.open();
	
	BaseFont bf = BaseFont.createFont("C:/WINDOWS/Fonts/STXIHEI.TTF", BaseFont.IDENTITY_H,BaseFont.NOT_EMBEDDED);
	Font font = new Font(bf, 12, Font.NORMAL);
	LeafChildrenCacheMgr lccm = new LeafChildrenCacheMgr(dirCode);
	Iterator ir = lccm.getList().iterator();
	int m = 1;
	com.redmoon.oa.fileark.Document doc = new com.redmoon.oa.fileark.Document();
	%><%
	while (ir.hasNext()) {
		Leaf lf = (Leaf)ir.next();
		Chapter chapter = new Chapter(new Paragraph(lf.getName().replaceAll(" ",""), font), m++);
		
		Iterator irdoc = doc.getDocumentsByDirCode(lf.getCode()).iterator();
		while (irdoc.hasNext()) {
			doc = (com.redmoon.oa.fileark.Document)irdoc.next();
			
			Paragraph p = new Paragraph(doc.getTitle(), font);
			p.setSpacingBefore(10f);
			chapter.add(p);
			p.add(Chunk.NEWLINE);
			
			StringReader reader = new StringReader(doc.getContent(1));
			java.util.List<Element> al = HTMLWorker.parseToList( reader, st );
			for (int k = 0; k < al.size(); ++k) {
				// System.out.println(getClass() + " " + al.get(k).getClass());
				p.add((Element) al.get(k));
			}
			
			p.setSpacingBefore(10f);
			p.add(Chunk.NEWLINE);
			chapter.add(p);
		}
		
		LeafChildrenCacheMgr lccmch = new LeafChildrenCacheMgr(lf.getCode());
		Iterator irch = lccmch.getList().iterator();
		while (irch.hasNext()) {
			Leaf lfch = (Leaf)irch.next();
			Section section = chapter.addSection(new Paragraph(lfch.getName(), font));
			
			irdoc = doc.getDocumentsByDirCode(lfch.getCode()).iterator();
			while (irdoc.hasNext()) {
				doc = (com.redmoon.oa.fileark.Document)irdoc.next();
				
				Paragraph p = new Paragraph(doc.getTitle(), font);
				p.setSpacingBefore(10f);
				section.add(p);
				p.add(Chunk.NEWLINE);
				
				p = new Paragraph(doc.getContent(1), font);
				p.setSpacingBefore(10f);
				section.add(p);
				p.add(Chunk.NEWLINE);
			}
		}
		document.add(chapter);
	}
	document.close();
    out.clear();
    out = pageContext.pushBody();
    DataOutput output = new DataOutputStream(response.getOutputStream());
    byte[] bytes = buffer.toByteArray();
    response.setContentLength(bytes.length);
    for (int x=0; x<bytes.length; x++) {
    	output.writeByte(bytes[x]);
    }
%>