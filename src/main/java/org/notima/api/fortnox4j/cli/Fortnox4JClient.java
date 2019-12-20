package org.notima.api.fortnox4j.cli;

import java.util.List;

import org.notima.api.fortnox.FortnoxClient3;
import org.notima.api.fortnox.entities3.Customer;
import org.notima.api.fortnox.entities3.CustomerSubset;
import org.notima.api.fortnox.entities3.Customers;

public class Fortnox4JClient {

	private FortnoxClient3	client;
	
	Fortnox4JClient(FortnoxClient3 c) {
		client = c;
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
