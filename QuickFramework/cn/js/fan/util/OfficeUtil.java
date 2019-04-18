package cn.js.fan.util;

/**
 * <p>Title: </p>
 *
 * <p>Description: </p>
 *
 * <p>Copyright: Copyright (c) 2005</p>
 *
 * <p>Company: </p>
 *
 * @author not attributable
 * @version 1.0
 */
import com.redmoon.oa.flow.FormDb;
import com.redmoon.oa.flow.FormField;
import com.redmoon.oa.flow.FormMgr;
import com.redmoon.oa.pvg.Privilege;
import com.redmoon.oa.visual.FormDAO;
import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import javax.servlet.http.HttpServletRequest;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Vector;

public class OfficeUtil {
	public OfficeUtil() {
		super();
	}

	/**
	 * @param request
	 * @return
	 * @throws ErrMsgException
	 * @throws ResKeyException
	 */
	public static boolean importFromExcel(HttpServletRequest request,
			String fullPath) throws ErrMsgException {
		boolean re = true;
		String formCode = ParamUtil.get(request, "formCode");
		FormMgr fm = new FormMgr();
		FormDb fd = fm.getFormDb(formCode);
		if (fd == null || !fd.isLoaded()) {
			throw new ErrMsgException("表单不存在！");
		}

		Privilege priv = new Privilege();
		FormDAO fdao = new FormDAO(fd);
		fdao.setCreator(priv.getUser(request));
		fdao.setUnitCode(priv.getUserUnitCode(request));
		Vector fields = fd.getFields();
		String ext = StrUtil.getFileExt(fullPath);
		ArrayList<HashMap<String, String>> list = null;
		StringBuffer errSb = new StringBuffer();
		errSb.append("导入有误！请检查");

		if (ext.equals("xls")) {
			try {
				list = getXlsContent(fullPath);
			} catch (FileNotFoundException ex) {
				throw new ErrMsgException("文件不存在！");
			} catch (IOException ex) {
				throw new ErrMsgException("文件读取失败！");
			} catch (InvalidFormatException ex) {
				try {
					list = getXlsxContent(fullPath);
				} catch (FileNotFoundException e) {
					throw new ErrMsgException("文件不存在！");
				} catch (IOException e) {
					throw new ErrMsgException("文件读取失败！");
				} catch (InvalidFormatException e) {
					throw new ErrMsgException("文件格式非法！");
				}
			}

		} else if (ext.equals("xlsx")) {
			try {
				list = getXlsxContent(fullPath);
			} catch (FileNotFoundException ex) {
				throw new ErrMsgException("文件不存在！");
			} catch (IOException ex) {
				throw new ErrMsgException("文件读取失败！");
			} catch (InvalidFormatException ex) {
				throw new ErrMsgException("文件格式非法！");
			}
		}

		for (int i = 0; i < list.size(); i++) {
			boolean flag = true;
			for (Iterator ir = fields.iterator(); ir.hasNext();) {
				FormField ff = (FormField) ir.next();
				Object object = ((HashMap<String, String>) list.get(i)).get(ff
						.getTitle());
				ff.setValue(object == null ? "" : object.toString());
			}
			fdao.setFields(fields);
			flag = fdao.createEmptyForm();
			if (!flag) {
				errSb.append(re ? "" : "、").append("第").append(i + 2).append(
						"行");
			}
			re = re & flag;
		}

		if (!re) {
			errSb.append("！关键数据为空或者该行数据已经存在！");
			throw new ErrMsgException(errSb.toString());
		}
		// Iterator iter = map.entrySet().iterator();
		// while (iter.hasNext()) {
		// Map.Entry entry = (Map.Entry) iter.next();
		// Object key = entry.getKey();
		// Object val = entry.getValue();
		// }
		return re;
	}

	/**
	 * @param path
	 */
	private static ArrayList<HashMap<String, String>> getXlsContent(String path)
			throws FileNotFoundException, InvalidFormatException, IOException {
		InputStream in = new FileInputStream(path);
		ArrayList<HashMap<String, String>> list = new ArrayList<HashMap<String, String>>();
		HSSFWorkbook w = (HSSFWorkbook) WorkbookFactory.create(in);
		for (int i = 0; i < w.getNumberOfSheets(); i++) {
			HSSFSheet sheet = w.getSheetAt(i);
			if (sheet != null) {
				HSSFRow head = sheet.getRow(0);
				int rowcount = sheet.getLastRowNum();
				for (int j = 1; j <= rowcount; j++) {
					HashMap<String, String> map = new HashMap<String, String>();
					HSSFRow row = sheet.getRow(j);
					if (row != null) {
						for (int k = 0; k < row.getLastCellNum(); k++) {
							HSSFCell headcell = head.getCell(k);
							HSSFCell cell = row.getCell(k);
							if (cell != null) {
								headcell.setCellType(HSSFCell.CELL_TYPE_STRING);
								cell.setCellType(HSSFCell.CELL_TYPE_STRING);
								String headValue = headcell
										.getStringCellValue().trim();
								String cellValue = cell.getStringCellValue()
										.trim();
								map.put(headValue, cellValue);
							}
						}
					}
					list.add(map);
				}
			}
		}
		return list;
	}

	/**
	 * @param path
	 */
	private static ArrayList<HashMap<String, String>> getXlsxContent(String path)
			throws FileNotFoundException, InvalidFormatException, IOException {
		InputStream in = new FileInputStream(path);
		;
		ArrayList<HashMap<String, String>> list = new ArrayList<HashMap<String, String>>();
		XSSFWorkbook w = (XSSFWorkbook) WorkbookFactory.create(in);
		for (int i = 0; i < w.getNumberOfSheets(); i++) {
			XSSFSheet sheet = w.getSheetAt(i);
			if (sheet != null) {
				XSSFRow head = sheet.getRow(0);
				int rowcount = sheet.getLastRowNum();
				for (int j = 1; j <= rowcount; j++) {
					HashMap<String, String> map = new HashMap<String, String>();
					XSSFRow row = sheet.getRow(j);
					if (row != null) {
						for (int k = 0; k < row.getLastCellNum(); k++) {
							XSSFCell headcell = head.getCell(k);
							XSSFCell cell = row.getCell(k);
							if (cell != null) {
								headcell.setCellType(XSSFCell.CELL_TYPE_STRING);
								cell.setCellType(XSSFCell.CELL_TYPE_STRING);
								String headValue = headcell
										.getStringCellValue().trim();
								String cellValue = cell.getStringCellValue()
										.trim();
								map.put(headValue, cellValue);

							}
						}
					}
				}
			}
		}
		return list;
	}
}
