package org.notima.api.fortnox4j.cli;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.notima.api.fortnox.entities3.InvoiceSubset;
import org.notima.api.fortnox.entities3.Invoices;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;

public class Fortnox4JText implements Fortnox4JFormat {

	// If result should be written to outfile
	private File	outFile;	
	
	public static final int INVOICE_DATE_COL = 2;
	public static final int DUE_DATE_COL = 3; 
	
	public String[] invoiceLineHeaders = new String[] {
			"InvoiceNo",
			"InvoiceType",
			"Inv Date",
			"Due Date",
			"Cust No",
			"Cust Name",
			"Total",
			"Balance",
			"Currency",
			"TermsOfPayment",
			"ExtRef1",
			"ExtRef2",
			"Booked"
	};
	
	protected List<Object[]>	 invoiceReportLines;

	/**
	 * Creates header for invoice report.
	 * 
	 * @return		The invoice report header.
	 */
	public Object[] getInvoiceReportHeader() {
		
		Object[] reportLine = new Object[invoiceLineHeaders.length];
		for (int i=0; i<invoiceLineHeaders.length; i++) {
			reportLine[i] = invoiceLineHeaders[i];
		}
		
		return reportLine;
	}

	/**
	 * Default date format. Can be changed using property settings.
	 */
	public DateFormat	dateFormat = new SimpleDateFormat("yyyy-MM-dd");
	
	public DateFormat getDateFormat() {
		return dateFormat;
	}

	public void setDateFormat(DateFormat dateFormat) {
		this.dateFormat = dateFormat;
	}
	
	/**
	 * Sets out file if applicable 
	 * @param of
	 */
	public void setOutFile(File of) {
		outFile = of;
	}
	
	/**
	 * Create a compact invoice list
	 * 
	 * @param 	invoices	The invoices to create the list
	 */
	@Override
	public int reportCustomerInvoicesCompact(Invoices invoices) {
		
		if (invoices==null || invoices.getInvoiceSubset()==null)
			return 0;

		if (invoiceReportLines==null) {
			invoiceReportLines = new ArrayList<Object[]>();
		}
		
		int count = 0, col = 0;
		Object[] reportLine;
		List<InvoiceSubset> isList = invoices.getInvoiceSubset();
		for (InvoiceSubset is : isList) {
			col = 0;
			reportLine = new Object[invoiceLineHeaders.length];
			
			reportLine[col++] = is.getDocumentNumber();
			reportLine[col++] = is.getInvoiceType();
			reportLine[col++] = is.getInvoiceDate();
			reportLine[col++] = is.getDueDate();
			reportLine[col++] = is.getCustomerNumber();
			reportLine[col++] = is.getCustomerName();
			reportLine[col++] = new Double(is.getTotal());
			reportLine[col++] = new Double(is.getBalance());
			reportLine[col++] = is.getCurrency();
			reportLine[col++] = is.getTermsOfPayment();
			reportLine[col++] = is.getExternalInvoiceReference1();
			reportLine[col++] = is.getExternalInvoiceReference2();
			reportLine[col++] = new Boolean(is.isBooked());
			
			
			count++;
			invoiceReportLines.add(reportLine);
		}
		return count;
	}

	@Override
	public List<StringBuffer> writeResult() throws IOException {
		
		List<StringBuffer> result = new ArrayList<StringBuffer>();
		StringBuffer buf = new StringBuffer();
		result.add(buf);
		
		List<Object[]> rows = invoiceReportLines;
		
		CSVPrinter printer = new CSVPrinter(buf, CSVFormat.EXCEL);
		int rowCount = 0;
		for (Object[] r : rows) {
			// Convert date
			if (dateFormat!=null && rowCount>0) {
				if (r[INVOICE_DATE_COL]!=null && r[DUE_DATE_COL] instanceof Date) {
					r[INVOICE_DATE_COL] = dateFormat.format((Date)r[INVOICE_DATE_COL]);
				}
				if (r[DUE_DATE_COL]!=null && r[DUE_DATE_COL] instanceof Date) {
					r[DUE_DATE_COL] = dateFormat.format((Date)r[DUE_DATE_COL]);
				}
			}
			printer.printRecord(r);
			rowCount++;
		}
		
		printer.close();
		
		if (outFile!=null) {
			
			if (!outFile.getAbsolutePath().toLowerCase().endsWith(".csv")) {
				outFile = new File(outFile.getAbsolutePath() + ".csv");
			}
			
			PrintWriter fp = new PrintWriter(outFile);
			for (StringBuffer s : result) {
				fp.append(s.toString());
				fp.append("\n");
			}
			fp.close();
			result.clear();
			result.add(new StringBuffer(outFile.getAbsolutePath()));
		}
		
		return result;
		
	}
	
}
