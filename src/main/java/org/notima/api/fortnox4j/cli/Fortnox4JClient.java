package org.notima.api.fortnox4j.cli;

import java.io.PrintStream;
import java.util.List;

import javax.xml.bind.JAXB;

import org.notima.api.fortnox.Fortnox4JSettings;
import org.notima.api.fortnox.FortnoxClient3;
import org.notima.api.fortnox.clients.FortnoxClientInfo;
import org.notima.api.fortnox.entities3.CompanySetting;
import org.notima.api.fortnox.entities3.Customer;
import org.notima.api.fortnox.entities3.CustomerSubset;
import org.notima.api.fortnox.entities3.Customers;
import org.notima.api.fortnox.entities3.Invoice;
import org.notima.api.fortnox.entities3.Invoices;
import org.notima.api.fortnox.entities3.Supplier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Fortnox4JClient {

	protected Logger	log = LoggerFactory.getLogger(Fortnox4JClient.class);	
	
	private FortnoxClient3		client;
	private FortnoxClientInfo	clientInfo;
	
	/**
	 * Create a new Fortnox4JClient from FortnoxClientInfo
	 * 
	 * @param ci
	 */
	Fortnox4JClient(FortnoxClientInfo ci) {

		clientInfo = ci;
		client = new FortnoxClient3(clientInfo.getAccessToken(), clientInfo.getClientSecret());
		
	}
	
	public FortnoxClient3 getClient() {
		return client;
	}

	public void setClient(FortnoxClient3 client) {
		this.client = client;
	}



	/**
	 * Gets company settings for this client
	 * 
	 * @return		Company Settings
	 * @throws Exception	If something goes wrong
	 */
	public CompanySetting getCompanySetting() throws Exception {
		if (clientInfo.getCompanySetting()==null && client!=null) {
			clientInfo.setCompanySetting(client.getCompanySetting());
		}
		return clientInfo.getCompanySetting();
	}

	/**
	 * Set company setting. This is not persisted to Fortnox
	 * 
	 * @param companySetting
	 */
	public void setCompanySetting(CompanySetting companySetting) {
		clientInfo.setCompanySetting(companySetting);
	}

	/**
	 * Writes a setting to supplier
	 * 
	 * @param orgNo				Org number of supplier
	 * @param key				The key of the setting.
	 * @param value				The value of the setting.
	 * @return	The supplier if successful. Null if supplier is not found.
	 * @throws Exception 
	 */
	public Supplier writeSettingToSupplier(String orgNo, String key, String value) throws Exception {
		
		Supplier supplier = client.getSupplierByTaxId(orgNo, true);
		if (supplier==null) {
			log.warn("Supplier with tax id {} doesn't exist.", orgNo);
			return null;
		}
		
		Fortnox4JSettings settings = new Fortnox4JSettings(client);
		
		return settings.writeSettingToSupplierByOrgNo(orgNo, key, value);
		
	}
	

	/**
	 * Returns a specific invoice
	 * 
	 * @param invoiceNo
	 * @return
	 * @throws Exception
	 */
	public Invoice printInvoice(PrintStream os, String invoiceNo) throws Exception {
		
		Invoice result = client.getInvoice(invoiceNo);
		JAXB.marshal(result, os);
		return result;
		
	}
	
	/**
	 * Returns a list of unpaid customer invoices
	 * 
	 * @return	A list of unpaid customer invoices.
	 * @throws Exception	if something goes wrong
	 */
	public Invoices getUnpaidCustomerInvoices() throws Exception {
		Invoices result = client.getInvoices(FortnoxClient3.FILTER_UNPAID);
		return result;
	}
	
	public void getCustomerList(PrintStream os) throws Exception {

		Customers customers = client.getCustomers();
		List<CustomerSubset> cslist = customers.getCustomerSubset();
		
		Customer customer;
		
		for (CustomerSubset cs : cslist) {
			
			customer = client.getCustomerByCustNo(cs.getCustomerNumber());
			os.println(printCustomerDetails(customer));
			
		}
		
	}
	
	public String printCustomerDetails(Customer customer) {

		StringBuffer buf = new StringBuffer();
		buf.append(customer.getCustomerNumber() + ";");
		buf.append(customer.getName() + ";");
		if (customer.getDeliveryAddress1()!=null && customer.getDeliveryAddress1().trim().length()>0)
			buf.append(customer.getDeliveryAddress1() + ";");
		else
			buf.append(customer.getAddress1() + ";");
		buf.append(customer.getEmail() + ";");
		buf.append(customer.getOrganisationNumber() + ";");
		buf.append(customer.getPhone1() + ";");
		buf.append(customer.getTermsOfPayment() + ";");

		return buf.toString();
		
	}
	
}
