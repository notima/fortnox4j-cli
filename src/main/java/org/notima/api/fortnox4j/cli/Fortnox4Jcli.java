package org.notima.api.fortnox4j.cli;

import java.io.File;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.util.Date;

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
import org.notima.api.fortnox.FortnoxUtil;
import org.notima.api.fortnox.clients.FortnoxClientInfo;
import org.notima.api.fortnox.clients.FortnoxClientList;
import org.notima.api.fortnox.entities3.FinancialYearSubset;
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
	public static final String CMD_LISTUNBOOKED_CUSTOMER_INVOICES = "listUnbookedCustomerInvoices";
	public static final String CMD_LIST_ALL_CUSTOMER_INVOICES = "listCustomerInvoices";
	public static final String CMD_COPY_INVOICES = "copyInvoices";
	public static final String CMD_COPY_UNPAID_AND_UNBOOKED_INVOICES = "copyUnpaidAndUnbookedInvoices";
	public static final String CMD_GET_LOCKED_PERIOD = "getLockedPeriod";
	public static final String CMD_GET_SIE4 = "getSie4";
	
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
	
	/**
	 * Parse destination auth details from command line.
	 * 
	 * @param cmd
	 * @return	An auth object with clientSecret and accessToken.
	 */
	private FortnoxClientInfo parseDestAuthDetails(CommandLine cmd) {
		
		FortnoxClientInfo auth = new FortnoxClientInfo();

		if (cmd.hasOption("destsecret")) {
			auth.setClientSecret(cmd.getOptionValue("destsecret"));
		} else {
			if (cmd.hasOption("s")) {
				auth.setClientSecret(cmd.getOptionValue("s"));
			} else {
				System.out.println("Client secret must be supplied. Use option s.");
				System.exit(1);
			}
		}
		
		if (cmd.hasOption("destaccesstoken")) {
			auth.setAccessToken(cmd.getOptionValue("destaccesstoken"));
		} else {
			System.out.println("Destination Access Token must be supplied. Use option -destaccesstoken.");
			System.exit(1);
		}

		return auth;
		
	}
	
	
	
	public static void main(String[] args) {

		Fortnox4Jcli cli = new Fortnox4Jcli();
		
		Options opts = new Options();
		opts.addOption("f", true, "Client file. A file containing credentials");
		opts.addOption("orgNo", true, "The org number to use when determining what credentials to use from the client file. If omitted, the first client is used.");
		opts.addOption("c", "cmd", true, "Command. Available commands: "
				+ CMD_GETACCESSTOKEN + ", "  
				+ CMD_GETCUSTOMERLIST + ", " 
				+ CMD_LISTUNPAID_CUSTOMER_INVOICES + ", " 
				+ CMD_LISTUNBOOKED_CUSTOMER_INVOICES + ", " 
				+ CMD_LIST_ALL_CUSTOMER_INVOICES + ", " 
				+ CMD_COPY_INVOICES + ", "
				+ CMD_COPY_UNPAID_AND_UNBOOKED_INVOICES + ", "
				+ CMD_GET_LOCKED_PERIOD + ", "
				+ CMD_GET_SIE4);
		opts.addOption("s", true, "Client Secret. This is the integrator's secret word.");
		opts.addOption("a", "apicode", true, "The API-code recieved from the Fortnox client when adding the integration. Must be combined with -s");
		opts.addOption("t", "accesstoken", true, "The access token to the Fortnox client");
		opts.addOption("destsecret", true, "Destination secret in transfer operations if different from source secret");
		opts.addOption("destaccesstoken", true, "Destination access token in transfer operations");
		opts.addOption("i", "invoiceno", true, "Get invoice with No");
		opts.addOption("o", "outfile", true, "Redirect output to file");
		opts.addOption("d", "fromdate", true, "Set from date");
		opts.addOption("untildate", true, "Set until date");
		opts.addOption("enrich", "Try to enrich information as much as possible, ie long format");
		opts.addOption("format", true, "Select output format");
		
		CommandLineParser parser = new DefaultParser();
		HelpFormatter formatter = new HelpFormatter();
		String format = null;
		Fortnox4JFormat outputFormat = null;
		
		PrintStream os = System.out;
		
		Date fromDate = null;
		Date untilDate = null;
		String fortnoxClientFile = null;
		String orgNo = null;
		FortnoxClientList clist = null;
		FortnoxClientInfo ci = null;
		File destinationFile = null;
		
		try {
			
			CommandLine cmd = parser.parse(opts, args);
			String cmdLine = null;
			String apiCode = null;
			
			if (cmd.hasOption("a")) {
				apiCode = cmd.getOptionValue("a");
			} else {
				if (!cmd.hasOption("c") && !cmd.hasOption("i")) {
					formatter.printHelp(Fortnox4Jcli.class.getSimpleName(), opts);
					System.exit(1);
				}
			}

			if (cmd.hasOption("f")) {
				fortnoxClientFile = cmd.getOptionValue("f");
			}
			
			if (cmd.hasOption("orgNo")) {
				orgNo = cmd.getOptionValue("orgNo");
			}
			
			if (fortnoxClientFile!=null) {
				clist = FortnoxUtil.readFortnoxClientListFromFile(fortnoxClientFile);
				if (orgNo!=null) {
					ci = clist.getClientInfoByOrgNo(orgNo);
				} else {
					ci = clist.getFirstClient();
				}
			}
			
			if (cmd.hasOption("d")) {
				fromDate = FortnoxClient3.s_dfmt.parse(cmd.getOptionValue("d"));
			}
			
			if (cmd.hasOption("untildate")) {
				untilDate = FortnoxClient3.s_dfmt.parse(cmd.getOptionValue("untildate"));
			}
			
			if (cmd.hasOption("o")) {
				destinationFile = new File(cmd.getOptionValue("o"));
				os = new PrintStream(new FileOutputStream(destinationFile));
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
			
			if (cmd.hasOption("c") || apiCode!=null || cmd.hasOption("i")) {
				
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
						os.println("Got access token:");
						os.println(accessToken);
					} catch (FortnoxException fe) {
						System.err.println(fe.toString());
					}
					
				} else if (CMD_LISTUNPAID_CUSTOMER_INVOICES.equalsIgnoreCase(cmdLine)) {

					if (ci==null) 
						ci = cli.parseAuthDetails(cmd);
					
					Fortnox4JClient cl = new Fortnox4JClient(ci);
					outputFormat.setFortnox4JClient(cl);
					
					Invoices invoices = cl.getClient().getUnpaidCustomerInvoices();
					
					if (invoices!=null && invoices.getInvoiceSubset()!=null) {

						if (cmd.hasOption("enrich")) {
							outputFormat.reportCustomerInvoices(invoices);
						} else {
							outputFormat.reportCustomerInvoicesCompact(invoices);
						}
						List<StringBuffer> out = outputFormat.writeResult();
						for (StringBuffer b : out) {
							os.println(b.toString());
						}
						
					} else {
						System.out.println("No unpaid customer invoices.");
					}
				} else if (CMD_LISTUNBOOKED_CUSTOMER_INVOICES.equalsIgnoreCase(cmdLine)) {
					
					if (ci==null) 
						ci = cli.parseAuthDetails(cmd);

					Fortnox4JClient cl = new Fortnox4JClient(ci);
					outputFormat.setFortnox4JClient(cl);
					
					Invoices invoices = cl.getClient().getInvoices(FortnoxClient3.FILTER_UNBOOKED);
					
					if (invoices!=null && invoices.getInvoiceSubset()!=null) {

						if (cmd.hasOption("enrich")) {
							outputFormat.reportCustomerInvoices(invoices);
						} else {
							outputFormat.reportCustomerInvoicesCompact(invoices);
						}
						List<StringBuffer> out = outputFormat.writeResult();
						for (StringBuffer b : out) {
							os.println(b.toString());
						}
						
					} else {
						System.out.println("No unbooked customer invoices.");
					}
					
					
				} else if (CMD_LIST_ALL_CUSTOMER_INVOICES.equalsIgnoreCase(cmdLine)) {
					
					if (ci==null) 
						ci = cli.parseAuthDetails(cmd);
					
					Fortnox4JClient cl = new Fortnox4JClient(ci);
					outputFormat.setFortnox4JClient(cl);
					
					if (fromDate==null) {
						System.out.println("Parameter --fromdate is missing");
						System.exit(1);
					}
					
					Invoices invoices = cl.getClient().getAllCustomerInvoicesByDateRange(fromDate, untilDate);
					
					if (invoices!=null && invoices.getInvoiceSubset()!=null) {

						if (cmd.hasOption("enrich")) {
							outputFormat.reportCustomerInvoices(invoices);
						} else {
							outputFormat.reportCustomerInvoicesCompact(invoices);
						}
						List<StringBuffer> out = outputFormat.writeResult();
						for (StringBuffer b : out) {
							os.println(b.toString());
						}
						
					} else {
						System.out.println("No customer invoices in given date range.");
					}
					
				} else if (CMD_COPY_INVOICES.equalsIgnoreCase(cmdLine)) {

					if (fromDate==null) {
						System.out.println("Parameter --fromdate is missing");
						System.exit(1);
					}
					
					if (ci==null) 
						ci = cli.parseAuthDetails(cmd);
					
					Fortnox4JClient cl = new Fortnox4JClient(ci);
					
					FortnoxClientInfo ciDst = cli.parseDestAuthDetails(cmd);
					Fortnox4JClient clDst = new Fortnox4JClient(ciDst);
					
					int copied = FortnoxUtil.copyCustomerInvoices(
							cl.getClient(), 
							clDst.getClient(), 
							fromDate, 
							untilDate, 
							os);
					os.println(copied + " invoices copied.");
					
				} else if (CMD_COPY_UNPAID_AND_UNBOOKED_INVOICES.equalsIgnoreCase(cmdLine)) {
					
					if (ci==null) 
						ci = cli.parseAuthDetails(cmd);
					
					Fortnox4JClient cl = new Fortnox4JClient(ci);
					
					FortnoxClientInfo ciDst = cli.parseDestAuthDetails(cmd);
					Fortnox4JClient clDst = new Fortnox4JClient(ciDst);

					Invoices invoices = cl.getClient().getUnpaidAndUnbookedCustomerInvoices();
					
					int copied = FortnoxUtil.copyCustomerInvoices(
							cl.getClient(), 
							clDst.getClient(), 
							invoices, 
							os);
					
					os.println(copied + " invoices copied.");
					
				} else if (CMD_GETCUSTOMERLIST.equalsIgnoreCase(cmdLine)) {

					if (ci==null) 
						ci = cli.parseAuthDetails(cmd);

					Fortnox4JClient cl = new Fortnox4JClient(ci);
					cl.getCustomerList(os);
					
				} else if (CMD_GET_LOCKED_PERIOD.equalsIgnoreCase(cmdLine)) {
					
					if (ci==null) 
						ci = cli.parseAuthDetails(cmd);
					
					Fortnox4JClient cl = new Fortnox4JClient(ci);
					Date lockedUntil = cl.getClient().getLockedPeriodUntil();
					if (lockedUntil == null) {
						os.println("No locked period found.");
					} else {
						os.println("Period locked until " + FortnoxClient3.s_dfmt.format(lockedUntil));
					}

				} else if (CMD_GET_SIE4.equalsIgnoreCase(cmdLine)) {

					if (fromDate==null) {
						System.out.println("Parameter --fromdate is missing");
						System.exit(1);
					}
					
					if (ci==null) 
						ci = cli.parseAuthDetails(cmd);
					
					Fortnox4JClient cl = new Fortnox4JClient(ci);
					
					FinancialYearSubset fs = cl.getClient().getFinancialYear(fromDate);
					int yearId = fs.getId();
					
					StringBuffer sieContent = cl.getClient().retrieveSieFile(4, yearId);

					os.print(sieContent);
					
					if (os!=System.out) {
						System.out.println("SIE4 file saved to " + destinationFile.getAbsolutePath());
					}
					
					
				} else if (cmd.hasOption("i")) {
					
					String invoiceNo = cmd.getOptionValue("i");
					if (ci==null) 
						ci = cli.parseAuthDetails(cmd);
					
					Fortnox4JClient cl = new Fortnox4JClient(ci);
					cl.printInvoice(os, invoiceNo);
					
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
