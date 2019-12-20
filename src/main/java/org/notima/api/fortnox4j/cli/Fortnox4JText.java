package org.notima.api.fortnox4j.cli;

import org.notima.api.fortnox.entities3.InvoiceSubset;

public class Fortnox4JText {

	public static String getColumnHeaders() {
		StringBuffer result = new StringBuffer();
		
		result.append("DocNo\t" + "Inv Date\t" + "CustNo\t" + "Customer name\t");
		result.append("ExtRef1\t" + "ExtRef2\t");
		result.append("Total\t" + "Balance\t" + "Currency");
		
		return result.toString();
	}
	
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
