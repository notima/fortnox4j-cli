package org.notima.api.fortnox4j.cli;

import java.io.IOException;
import java.util.List;

import org.notima.api.fortnox.entities3.Invoices;

/**
 * Interface to different output formats
 * 
 * @author Daniel Tamm
 *
 */
public interface Fortnox4JFormat {
	
	/**
	 * Sets the Fortnox Client for this formatting
	 * 
	 * @param fc3
	 */
	public void setFortnox4JClient(Fortnox4JClient f4jclient);
	
	/**
	 * Add invoices to report.
	 * 
	 * @param invoices
	 * @return		The number of invoices added
	 */
	public int reportCustomerInvoicesCompact(Invoices invoices);
	
	/**
	 * Writes the result and returns it
	 * 
	 * @return		A list of stringbuffers containing the result.
	 * 				If the result are files, the returned list is a list of 
	 * 				file paths.
	 * 				If the result is text, the result is returned as text.
	 * @throws IOException If something goes wrong.
	 */
	public List<StringBuffer>	writeResult() throws Exception;
	
}
