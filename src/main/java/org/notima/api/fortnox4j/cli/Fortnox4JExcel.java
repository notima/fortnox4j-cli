package org.notima.api.fortnox4j.cli;

import java.io.File;
import java.io.FileOutputStream;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;

import org.apache.poi.EncryptedDocumentException;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.CreationHelper;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;
import org.apache.poi.xssf.usermodel.XSSFFormulaEvaluator;
import org.notima.api.fortnox.entities3.CompanySetting;

public class Fortnox4JExcel extends Fortnox4JText {

	private CellStyle 	dateCellStyle;
	private CellStyle	headerCellStyle;
	private String		dateFormatStr;
	
	public final static String	defaultDateFormatStr = "yyyy-MM-dd"; 
	public final static String defaultFileName = "reconciliation-report";
	
	public static Integer[] colWidthInvoiceLines = new Integer[] {
		12*256,		// Fortnox Invoice Id
		12*256,		// Invoice Date
		12*256,		// Due Date 
		null,		// Customer No
		null,		// Customer Name
		40*256,		// Total
		30*256,		// Balance
		null,		// Currency
		20*256,		// ExtRef1
		20*256		// ExtRef2
	};
	
	private int rowNum;
	private int colNum;
	private Row		row;
	private Cell	cell;
	
	private File	outFile;
	private String	outFilePrefix;

	public String getDateFormatStr() {
		return dateFormatStr;
	}

	public void setDateFormatStr(String dateFormatStr) {
		this.dateFormatStr = dateFormatStr;
	}
	
	public String getOutFilePrefix() {
		return outFilePrefix;
	}

	public void setOutFilePrefix(String outFilePrefix) {
		this.outFilePrefix = outFilePrefix;
		if (this.outFilePrefix!=null) {
			if (this.outFilePrefix.endsWith(".json")) {
				this.outFilePrefix = this.outFilePrefix.substring(0, outFilePrefix.length()-5);
			}
		}
	}

	public File getOutFile() {
		return outFile;
	}
	
	public void setOutFile(File outFile) {
		this.outFile = outFile;
	}

	/**
	 * Call this to reset out file if outfile should be generated automatically. 
	 */
	public void reset() {
		outFile = null;
	}

	public File createExcelFile() throws ParseException, EncryptedDocumentException, InvalidFormatException, Exception {

		String outFileName = null;
		
		CompanySetting cs = this.fortnox4JClient != null ? this.fortnox4JClient.getCompanySetting() : null;
		
		// Make sure we have a reasonable file name
		if (outFile==null) {
			if (outFilePrefix!=null && outFilePrefix.trim().length()>0) {
				outFileName = outFilePrefix;
			} else {
				if (cs!=null) {
					if (cs.getOrganizationNumber()!=null && cs.getOrganizationNumber().trim().length()>0) {
						outFileName = cs.getOrganizationNumber();
					}
					if (cs.getName()!=null && cs.getName().trim().length()>0) {
						if (outFileName!=null && outFileName.trim().length()>0) {
							outFileName += "-" + cs.getName();
						} else {
							outFileName = cs.getName();
						}
					}
				}
				if (outFileName==null || outFileName.trim().length()==0) {
					outFileName = defaultFileName;
				}
			}
			outFile = new File(outFileName);
		}
		if (!outFile.getAbsolutePath().toLowerCase().endsWith(".xlsx")) {
			outFile = new File(outFile.getAbsolutePath() + ".xlsx");
		}
		
		SXSSFWorkbook wb = new SXSSFWorkbook(invoiceReportLines.size()+1);
		
		// Create date format
		if (dateFormatStr==null || dateFormatStr.trim().length()==0) {
			dateFormatStr = defaultDateFormatStr;
		}
		
		CreationHelper createHelper = wb.getCreationHelper(); 
		dateCellStyle = wb.createCellStyle();
		dateCellStyle.setDataFormat(createHelper.createDataFormat().getFormat(dateFormatStr));
		
		headerCellStyle = wb.createCellStyle();
		headerCellStyle.setWrapText(true);
		
		Font font = wb.createFont();
		font.setBold(true);
		headerCellStyle.setFont(font);
		
		Sheet sheet = wb.createSheet();
		
		// Initialize rowNum
		rowNum = 0;
		
		if (invoiceReportLines.size()>0) {
			createHeader(sheet, invoiceReportLines.get(0));
			
			for (int i = 1; i<invoiceReportLines.size(); i++) {
				createRow(sheet, invoiceReportLines.get(i));
			}
			
		}
		
		XSSFFormulaEvaluator.evaluateAllFormulaCells(wb);
		sheet.createFreezePane(0, 1);
		
		FileOutputStream out = new FileOutputStream(outFile);
		wb.write(out);
		out.close();
		wb.dispose();
		
		return outFile;
		
	}

	private void createRow(Sheet sh, Object[] srcRow) {

		colNum = 0;
		
		row = sh.createRow(rowNum++);
		Object srcColumn;
		
		for (int i=0; i<srcRow.length; i++) {
		
			srcColumn = srcRow[i];
			
			if (srcColumn instanceof String) {
				cell = row.createCell(colNum++);
				cell.setCellValue((String)srcColumn);
			} else if (srcColumn instanceof java.util.Date) {
				cell = row.createCell(colNum++);
				cell.setCellValue((java.util.Date)srcColumn);
				cell.setCellStyle(dateCellStyle);
			} else if (srcColumn instanceof Double || srcColumn instanceof Integer) {
				cell = row.createCell(colNum++);
				cell.setCellValue((double)srcColumn);
			} else {
				cell = row.createCell(colNum++);
				if (srcColumn!=null)
					cell.setCellValue(srcColumn.toString());
			}
			
		}
		
		
	}
	
	/**
	 * Creates a header in given sheet.
	 * @param sh				The sheet to create header in
	 * @param columnHeaders		The headers (an array of strings)
	 */
	private void createHeader(Sheet sh, Object[] columnHeaders) {

		colNum = 0;
		row = sh.createRow(rowNum++);
		row.setHeightInPoints(40);
		for (Object c : columnHeaders) {
			cell = row.createCell(colNum++);
			cell.setCellStyle(headerCellStyle);
			cell.setCellValue((String)c);
			
			// Set column width if specified
			if (colWidthInvoiceLines.length>(colNum-1) && colWidthInvoiceLines[colNum-1]!=null) {
				sh.setColumnWidth(colNum-1, colWidthInvoiceLines[colNum-1]);
			}
		}
		
	}
	
	/**
	 * @return		A list of file names
	 */
	@Override
	public List<StringBuffer> writeResult() throws Exception {
		
		List<StringBuffer> result = new ArrayList<StringBuffer>();
		File createdFile = createExcelFile();
		
		if (createdFile!=null) {
			result.add(new StringBuffer(createdFile.getAbsolutePath()));
		}
		
		return result;
	}
	
	
}
