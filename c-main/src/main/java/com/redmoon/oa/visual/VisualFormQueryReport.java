package com.redmoon.oa.visual;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.logging.Logger;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.eclipse.birt.core.exception.BirtException;
import org.eclipse.birt.core.framework.IPlatformContext;
import org.eclipse.birt.core.framework.Platform;
import org.eclipse.birt.core.framework.PlatformServletContext;
import org.eclipse.birt.report.engine.api.EngineConfig;
import org.eclipse.birt.report.engine.api.HTMLRenderOption;
import org.eclipse.birt.report.engine.api.HTMLServerImageHandler;
import org.eclipse.birt.report.engine.api.IReportEngine;
import org.eclipse.birt.report.engine.api.IReportEngineFactory;
import org.eclipse.birt.report.engine.api.IReportRunnable;
import org.eclipse.birt.report.engine.api.IRunAndRenderTask;
import org.eclipse.birt.report.model.api.CellHandle;
import org.eclipse.birt.report.model.api.DataItemHandle;
import org.eclipse.birt.report.model.api.ElementFactory;
import org.eclipse.birt.report.model.api.LabelHandle;
import org.eclipse.birt.report.model.api.OdaDataSetHandle;
import org.eclipse.birt.report.model.api.OdaDataSourceHandle;
import org.eclipse.birt.report.model.api.PropertyHandle;
import org.eclipse.birt.report.model.api.ReportDesignHandle;
import org.eclipse.birt.report.model.api.RowHandle;
import org.eclipse.birt.report.model.api.StructureFactory;
import org.eclipse.birt.report.model.api.TableHandle;
import org.eclipse.birt.report.model.api.activity.SemanticException;
import org.eclipse.birt.report.model.api.elements.structures.ComputedColumn;

/**
 * 准备根据模板动态生成报表，运用于流程的查询中，以便于导出成各种格式
 * 但是发现这个类只能以servlet方式运行，而不能以frameset方式，frameset的方便性不能体现
 * 而一般只需导出为xls即可，所以放弃使用此类
 * @author Administrator
 *
 */
public class VisualFormQueryReport extends HttpServlet {
 
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	/**
	 * Constructor of the object.
	 */
	private IReportEngine birtReportEngine = null;
	protected static Logger logger = Logger.getLogger( "org.eclipse.birt" );
	
	public VisualFormQueryReport() {
		super();
	}

	/**
	 * Destruction of the servlet. 
	 */
	public void destroy() {
		super.destroy(); 
		birtReportEngine.destroy();
	}


	/**
	 * The doGet method of the servlet. 
	 *
	 */
 	public void doGet(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {

		//get report name and launch the engine
		// resp.setContentType("text/html");

		resp.setContentType("application/ms-excel");
		resp.setHeader ("Content-Disposition", "attachment; filename=test.xls");
		
		//resp.setContentType( "application/pdf" ); 
		//resp.setHeader ("Content-Disposition","inline; filename=test.pdf");		
		String reportName = req.getParameter("ReportName");
		String[] cols = (String[])req.getParameterMap().get("dyna1");
		ServletContext sc = req.getSession().getServletContext();
		this.birtReportEngine = getBirtEngine(sc);

		IReportRunnable design;
		try
		{
			//Open report design
			design = birtReportEngine.openReportDesign( sc.getRealPath("/Report")+"/"+reportName );
 			ReportDesignHandle report = (ReportDesignHandle) design.getDesignHandle( ); 
			buildReport( cols,  report );
			
			//create task to run and render report
			IRunAndRenderTask task = birtReportEngine.createRunAndRenderTask( design );		
			// task.setAppContext( contextMap );
			
			// set output options
			HTMLRenderOption options = new HTMLRenderOption();
			options.setImageHandler(new HTMLServerImageHandler());
			options.setImageDirectory(sc.getRealPath("/images"));
			options.setBaseImageURL(req.getContextPath() + "/images");
			//options.setOutputFormat(HTMLRenderOption.OUTPUT_FORMAT_HTML);

			// options.setOutputFileName("C:/aa.xls"); 
			options.setOutputFormat("xls");
			
			//IRenderOption ioptions = new RenderOption();		
			//ioptions.setOutputFormat("html");
			
			
			// options.setOutputFormat(HTMLRenderOption.OUTPUT_FORMAT_PDF);
			options.setOutputStream(resp.getOutputStream());
			task.setRenderOption(options);
			
			//run report
			task.run();
			task.close();
		}catch (Exception e){
 			e.printStackTrace();
 			throw new ServletException( e );
		}
	}
	
	public void buildReport(String[]  cols, ReportDesignHandle designHandle){
		try {
		ElementFactory designFactory = designHandle.getElementFactory( );

		buildDataSource(designFactory, designHandle);

		//ArrayList cols = new ArrayList();
		//cols.add("OFFICECODE");
		//cols.add("CITY");
		//cols.add("COUNTRY");

		buildDataSet(cols, "From Offices", designFactory, designHandle);

		TableHandle table = designFactory.newTableItem( "table", cols.length );
		table.setWidth( "100%" );
		table.setDataSet( designHandle.findDataSet( "ds" ) );

		PropertyHandle computedSet = table.getColumnBindings( ); 
		ComputedColumn  cs1 = null;

		for( int i=0; i < cols.length; i++){
 			cs1 = StructureFactory.createComputedColumn();
			cs1.setName((String)cols[i]);
			cs1.setExpression("dataSetRow[\"" + (String)cols[i] + "\"]");
			computedSet.addItem(cs1);
		}


		// table header
		RowHandle tableheader = (RowHandle) table.getHeader( ).get( 0 );


		for( int i=0; i < cols.length; i++){
			LabelHandle label1 = designFactory.newLabel( (String)cols[i] );	
			label1.setText((String)cols[i]);
			CellHandle cell = (CellHandle) tableheader.getCells( ).get( i );
			cell.getContent( ).add( label1 );
		}							

		// table detail
		RowHandle tabledetail = (RowHandle) table.getDetail( ).get( 0 );
		for( int i=0; i < cols.length; i++){
			CellHandle cell = (CellHandle) tabledetail.getCells( ).get( i );
			DataItemHandle data = designFactory.newDataItem( "data_"+(String)cols[i] );
			data.setResultSetColumn( (String)cols[i]);
			cell.getContent( ).add( data );
		}


 
		designHandle.getBody( ).add( table );
		}catch(Exception e){
			e.printStackTrace();
		}
 
	}

	void buildDataSource( ElementFactory designFactory, ReportDesignHandle designHandle ) throws SemanticException
	{
		OdaDataSourceHandle dsHandle = designFactory.newOdaDataSource(
				"Data Source", "org.eclipse.birt.report.data.oda.jdbc" );
		dsHandle.setProperty( "odaDriverClass", "com.mysql.cj.jdbc.Driver" );
		dsHandle.setProperty( "odaURL", "jdbc:mysql://localhost:3306/redmoonoa3" );
		dsHandle.setProperty( "odaUser", "root" );
		dsHandle.setProperty( "odaPassword", "myoa888" );

		designHandle.getDataSources( ).add( dsHandle );
	}

	void buildDataSet(String[] cols, String fromClause, ElementFactory designFactory, ReportDesignHandle designHandle ) throws SemanticException
	{
		OdaDataSetHandle dsHandle = designFactory.newOdaDataSet( "ds",
		"org.eclipse.birt.report.data.oda.jdbc.JdbcSelectDataSet" );
		dsHandle.setDataSource( "Data Source" );
		String qry = "Select ";
		for( int i=0; i < cols.length; i++){
			qry += " " + cols[i];
			if( i != (cols.length -1) ){
				qry += ",";
			}

		}
		qry += " " + fromClause;

		dsHandle.setQueryText( qry );
 
 		designHandle.getDataSets( ).add( dsHandle );

 
	}	 
 	/**
 	 * The doPost method of the servlet. 
 	 *
 	 */
 	public void doPost(HttpServletRequest request, HttpServletResponse response)
 			throws ServletException, IOException {

		response.setContentType("text/html");
		PrintWriter out = response.getWriter();
		out.println("<!DOCTYPE HTML PUBLIC \"-//W3C//DTD HTML 4.01 Transitional//EN\">");
		out.println("<HTML>");
		out.println("  <HEAD><TITLE>A Servlet</TITLE></HEAD>");
		out.println("  <BODY>");
		out.println(" Post Not Supported");
		out.println("  </BODY>");
		out.println("</HTML>");
		out.flush();
		out.close();
	}

	/**
	 * Initialization of the servlet. 
	 *
	 * @throws ServletException if an error occure
	 */
 	public void init() throws ServletException {
	}
 	
 	/*
 	public void toreport()    
    {   
        ServletContext sc = ServletActionContext.getServletContext();   
        String filePath = sc.getRealPath("/report") + "/";   
        HttpServletResponse resp = sc.getResponse();   
        resp.setContentType("text/html;charset=utf-8");   
           
        this.birtReportEngine = getBirtEngine(sc, filePath);   
        IReportRunnable design;   
        try {   
            design = birtReportEngine.openReportDesign(filePath + reportName);   
            IRunTask runTask = birtReportEngine.createRunTask(design);   
            HashMap paramMap = new HashMap();   
            paramMap.put("agencyNum", agencyNum);   
            paramMap.put("weekofyear", weekOfYear);   
            paramMap.put("year", year);            
            runTask.setParameterValues(paramMap);   
            runTask.validateParameters();   
            runTask.run(filePath + "temp.rptdocument");   
            runTask.close();   
            IReportDocument rptDoc = birtReportEngine.openReportDocument(filePath + "temp.rptdocument");   
            totalPage = rptDoc.getPageCount();     
            IRenderTask rendTask = birtReportEngine.createRenderTask(rptDoc);   
            HTMLRenderOption options = new HTMLRenderOption();   
            options.setHtmlPagination(true);   
            options.setEmbeddable(true);   
            options.setOutputFormat(HTMLRenderOption.OUTPUT_FORMAT_HTML);   
            options.setOutputStream(resp.getOutputStream());   
            rendTask.setRenderOption(options);   
            rendTask.setPageNumber(this.currentPage);   
            rendTask.render();   
            rendTask.close();   
        } catch (Exception e) {   
            e.printStackTrace();   
            throw new ServletException(e);   
        }   
    }
    */
  
 	public IReportEngine getBirtEngine(ServletContext sc) {   
        if (birtReportEngine == null) {   
            EngineConfig config = new EngineConfig();   
            config.setEngineHome(sc.getRealPath("/WEB-INF/platform"));   
            //config.setLogConfig(path, Level.OFF);   
            IPlatformContext context = new PlatformServletContext(sc);   
            config.setPlatformContext(context);   
  
            try {   
                Platform.startup(config);   
            } catch (BirtException e) {   
                e.printStackTrace();   
            }   
  
            IReportEngineFactory factory = (IReportEngineFactory) Platform   
                    .createFactoryObject(IReportEngineFactory.EXTENSION_REPORT_ENGINE_FACTORY);   
            birtReportEngine = factory.createReportEngine(config);   
  
        }   
        return birtReportEngine;   
    }  
 	

}

