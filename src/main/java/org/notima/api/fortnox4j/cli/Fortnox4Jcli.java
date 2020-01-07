package org.notima.api.fortnox4j.cli;

/**
 * CLI for communicating with Fortnox.
 * 
 * Copyright 2019 Notima System Integration AB (Sweden)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * 
 * @author Daniel Tamm
 *
 */

import java.util.List;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.MissingOptionException;
import org.apache.commons.cli.Options;
import org.notima.api.fortnox.FortnoxClient3;
import org.notima.api.fortnox.FortnoxException;
import org.notima.api.fortnox.clients.FortnoxClientInfo;
import org.notima.api.fortnox.entities3.Invoices;

/**
 * This class contains the CLI-parser which in turn calls other methods. 
 * 
 * @author Daniel Tamm
 *
 */
public class Fortnox4Jcli {

	public static final String CMD_GETACCESSTOKEN = "getAccessToken";
	public static final String CMD_GETCUSTOMERLIST = "getCustomerList";
	public static final String CMD_LISTUNPAID_CUSTOMER_INVOICES = "listUnpaidCustomerInvoices";
	
	/**
	 * Parse auth details from command line.
	 * 
	 * @param cmd
	 * @return	An auth object with clientSecret and accessToken.
	 */
	private FortnoxClientInfo parseAuthDetails(CommandLine cmd) {
		
		FortnoxClientInfo auth = new FortnoxClientInfo();

		if (cmd.hasOption("s")) {
			auth.setClientSecret(cmd.getOptionValue("s"));
		} else {
			System.out.println("Client Secret must be supplied. Use option s.");
			System.exit(1);
		}
		
		if (cmd.hasOption("t")) {
			auth.setAccessToken(cmd.getOptionValue("t"));
		} else {
			System.out.println("Access Token must be supplied. Use option t.");
			System.exit(1);
		}

		return auth;
		
	}
	
	
	public static void main(String[] args) {

		Fortnox4Jcli cli = new Fortnox4Jcli();
		
		Options opts = new Options();
		opts.addOption("c", "cmd", true, "Command. Available commands: getAccessToken, getCustomerList, listUnpaidCustomerInvoices");
		opts.addOption("s", true, "Client Secret. This is the integrator's secret word.");
		opts.addOption("a", "apicode", true, "The API-code recieved from the Fortnox client when adding the integration. Must be combined with -s");
		opts.addOption("t", "accesstoken", true, "The access token to the Fortnox client");
		opts.addOption("format", true, "Select output format");
		
		CommandLineParser parser = new DefaultParser();
		HelpFormatter formatter = new HelpFormatter();
		String format = null;
		Fortnox4JFormat outputFormat = null;
		
		try {
			
			CommandLine cmd = parser.parse(opts, args);
			String cmdLine = null;
			String apiCode = null;
			
			if (cmd.hasOption("a")) {
				apiCode = cmd.getOptionValue("a");
			} else {
				if (!cmd.hasOption("c")) {
					formatter.printHelp(Fortnox4Jcli.class.getSimpleName(), opts);
					System.exit(1);
				}
			}

			// Check format option
			if (cmd.hasOption("format")) {
				format = cmd.getOptionValue("format");
				format = format.toLowerCase();
				if (!format.equals("xlsx") && 
					!format.equals("csv") && 
					!format.equals("json")) {
					throw new MissingOptionException("Available formats are: json, xlsx, csv. If format is omitted csv is used."); 
				}
				if (format.equalsIgnoreCase("xlsx")) {
					outputFormat = new Fortnox4JExcel();
				}
			}
			if (outputFormat==null) {
				outputFormat = new Fortnox4JText();
			}
			
			if (cmd.hasOption("c") || apiCode!=null) {
				
				cmdLine = cmd.getOptionValue("c");
				if (CMD_GETACCESSTOKEN.equalsIgnoreCase(cmdLine) || apiCode!=null) {
				
					String clientSecret = null;
					if (cmd.hasOption("s")) {
						clientSecret = cmd.getOptionValue("s");
					} else {
						System.out.println("Client Secret must be supplied. Use option s.");
						System.exit(1);
					}
					if (cmd.hasOption("a")) {
						apiCode = cmd.getOptionValue("a");
					} else {
						System.out.println("API-code must be supplied. Use option apicode.");
						System.exit(1);
					}

					FortnoxClient3 client = new FortnoxClient3();
					try {
						String accessToken = client.getAccessToken(apiCode, clientSecret);
						System.out.println("Got access token:");
						System.out.println(accessToken);
					} catch (FortnoxException fe) {
						System.err.println(fe.toString());
					}
					
				} else if (CMD_LISTUNPAID_CUSTOMER_INVOICES.equalsIgnoreCase(cmdLine)) {

					FortnoxClientInfo ci = cli.parseAuthDetails(cmd);
					Fortnox4JClient cl = new Fortnox4JClient(ci);
					outputFormat.setFortnox4JClient(cl);
					
					Invoices invoices = cl.getUnpaidCustomerInvoices();
					
					if (invoices!=null && invoices.getInvoiceSubset()!=null) {
						
						outputFormat.reportCustomerInvoicesCompact(invoices);
						List<StringBuffer> out = outputFormat.writeResult();
						for (StringBuffer b : out) {
							System.out.println(b.toString());
						}
						
					} else {
						System.out.println("No unpaid customer invoices.");
					}
					
				} else if (CMD_GETCUSTOMERLIST.equalsIgnoreCase(cmdLine)) {

					FortnoxClientInfo ci = cli.parseAuthDetails(cmd);
					
					Fortnox4JClient cl = new Fortnox4JClient(ci);
					cl.getCustomerList();
					
					
				} else {
					System.out.println("Unknown command: " + cmdLine);
				}
			}
			
			
		} catch (MissingOptionException me) {
			System.out.println(me.getMessage());
			formatter.printHelp(Fortnox4Jcli.class.getSimpleName(), opts);
		} catch (Exception e) {
			e.printStackTrace();
		}
		
	}

}
