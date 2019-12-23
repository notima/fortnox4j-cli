package org.notima.api.fortnox4j.cli;

import org.notima.api.fortnox.entities3.InvoiceSubset;

public class Fortnox4JExcel extends Fortnox4JText {

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
	
	
	public static String formatInvoiceSubSetToLine(InvoiceSubset is) {

		StringBuffer result = new StringBuffer();

		result.append(is.getDocumentNumber() + "\t" + is.getInvoiceDate() + "\t");
		result.append(is.getCustomerNumber() + "\t" + is.getCustomerName() + "\t");
		result.append(is.getExternalInvoiceReference1() + "\t");
		result.append(is.getExternalInvoiceReference2() + "\t");
		result.append(is.getTotal() + "\t" + is.getBalance() + "\t" + is.getCurrency());
		
		return result.toString();
		
	}
	
}
