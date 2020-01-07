package org.notima.api.fortnox4j.cli;

import java.util.List;

import org.notima.api.fortnox.FortnoxClient3;
import org.notima.api.fortnox.clients.FortnoxClientInfo;
import org.notima.api.fortnox.entities3.CompanySetting;
import org.notima.api.fortnox.entities3.Customer;
import org.notima.api.fortnox.entities3.CustomerSubset;
import org.notima.api.fortnox.entities3.Customers;
import org.notima.api.fortnox.entities3.Invoices;

public class Fortnox4JClient {

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
	 * Returns a list of unpaid customer invoices
	 * 
	 * @return	A list of unpaid customer invoices.
	 * @throws Exception	if something goes wrong
	 */
	public Invoices getUnpaidCustomerInvoices() throws Exception {
		Invoices result = client.getInvoices(FortnoxClient3.FILTER_UNPAID);
		return result;
	}
	
	public void getCustomerList() throws Exception {

		Customers customers = client.getCustomers();
		List<CustomerSubset> cslist = customers.getCustomerSubset();
		
		Customer customer;
		
		for (CustomerSubset cs : cslist) {
			
			customer = client.getCustomerByCustNo(cs.getCustomerNumber());
			System.out.println(printCustomerDetails(customer));
			
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
