/**
 *Jack Napolitano
 *Jess Egler
 *Nick Carone
 *CS 1555 Term Project
 * 
 * Must run the following script before
 * attempting to compile
 * $> source ~panos/1555/bash.env (or tcsh.env)
 * 
 * To compile use
 * $> javac GroceryDeliveryDB.java
 **/
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.sql.*;
import java.text.ParseException;
import java.sql.Date;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.Random;

public class GroceryDeliveryDB {
	private static Connection connection;
	private Statement statement;
	private PreparedStatement preparedStatement;
	private ResultSet resultSet;
	private String query;
	private static int warehouses;
	private static int distPerWarehouse;
	private static int custPerDist;

	public static void main(String[] args) throws SQLException,
			ClassNotFoundException {
		Scanner reader = new Scanner(System.in); // Reading from System.in
		// Should we prompt user instead of hard coding this??
		System.out.print("Please enter your DB username: ");
		String username = reader.nextLine();
		System.out.print("Please enter your DB password: ");
		String password = reader.nextLine();
		DriverManager.registerDriver(new oracle.jdbc.OracleDriver());
		// Class.forName("oracle.jdbc.OracleDriver");

		String url = "jdbc:oracle:thin:@class3.cs.pitt.edu:1521:dbclass";

		connection = DriverManager.getConnection(url, username, password);
		System.out.println("Connection to Oracle established");

		while (true) {
			System.out
					.println("------------------------------------------------------\nWelcome to the Grocery Delivery System. Select how you \nwould like to interact with the system:\n------------------------------------------------------");
			System.out
					.print("1 - \tCreate Database (no drop table statements)\n2 - \tReset DB, Generate Data\n3 - \tView databases\n4 - \tNew Order\n5 - \tMake a payment\n6- \tCheck order status\n7- \tDelivery transaction\n8- \tStock transaction\n9- \tExit Application\nEnter a number: ");
			int n = reader.nextInt();
			switch (n) {
			case 1:
				genDB();
				break;
			case 2:
				GenData();
				break;
			case 3:
				viewDB();
				break;
			case 4:
				newOrder();
				break;
			case 5:
				makePayment();
				break;
			case 6:
				System.out.println("Not Implemented");
				break;
			case 7:
				System.out.println("Not Implemented");
				break;
			case 8:
				System.out.println("Not Implemented");
				break;
			case 9:
				connection.close();
				System.out.println("Connection closed.");
				System.exit(0);
				break;
			}
		}
	}

	public static void GenData() {
		// create tables from .sql file
		try {
			resetDatabase();
			System.out.println("Creating the database...");
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		// gen the data for the db
		try {
			System.out
					.println("Generating data for the database... (takes a few minutes)");
			System.out.println("Generating items...");
			GenItemData(10000);// 10000 unique grocery items
			System.out.println("Generating warehouse...");
			GenWarehouseData(1); // we chose 20 warehouses as it is a logical
									// number of warehouses
			System.out.println("Generating distribution stations...");
			GenDistStationData(8);// 8 DS per HW
			System.out.println("Generating customers...");
			GenCustomerData(3000);// 3000 customers per DS
			System.out.println("Generating orders and line items...");
			GenOrderData(100);// between 1-100 orders per cust
//			System.out.println("Generating line items...");
//			GenLineItemData(5, 3);// 5 orders per cust, 3 line items per cust
			System.out.println("Generating stock entries...");
			GenStockData(10000);// 10000 stock listings per warehouse

		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	public static void GenWarehouseData(int numWH) throws SQLException {
		warehouses = numWH;
		Random rand = new Random();
		int wh_ID;
		int zipcode;
		int taxrate; // this is sales tax, I changed it to an int, doesn't need
						// to be a double
		float ytdSalesSum; // Making this an int for now as well, we don't need
							// to be that specific yet
		for (int x = 0; x < numWH; x++) {
			// generate the data. I picked 7 as the length for the strings for
			// no reason.
			wh_ID = x;
			String wh_Name = generateString(7);
			String street_Address = alphaNumString();
			String city = generateString(7);
			String state = generateString(2);
			state = state.toUpperCase();
			zipcode = rand.nextInt(90000) + 10000; // generates a zipcode from
													// 10000-99999
			taxrate = rand.nextInt(8) + 1; // generates a taxrate from 1% to 8%
			int dollars = rand.nextInt(90000) + 1000;
			int cents = rand.nextInt(90) + 10;
			String ytdSalesString = dollars + "." + cents;
			ytdSalesSum = Float.parseFloat(ytdSalesString); // We should build a
															// method to just
															// give an integer
															// plus decimal
															// places. This only
															// does values
															// 0.0-1.0 FIXED
															// 11/17 see above
			// do a sql insert here using the JDBC
			Statement st = connection.createStatement();
			String insQuery = ("INSERT INTO Warehouse VALUES (?,?,?,?,?,?,?,?)");
			PreparedStatement ps = connection.prepareStatement(insQuery);
			ps.setLong(1, wh_ID);
			ps.setString(2, wh_Name);
			ps.setString(3, street_Address);
			ps.setString(4, city);
			ps.setString(5, state);
			ps.setLong(6, zipcode);
			ps.setLong(7, taxrate);
			ps.setFloat(8, ytdSalesSum);
			ps.executeUpdate();
			ps.close();
			ps = null;
			// System.out.println("WH_ID: "+ wh_ID+ "\nWarehouse_Name: "+
			// wh_Name+ "\nStreet_Address: "+ street_Address+ "\nCity: "+ city+
			// "\nState: "+ state + "\nZipcode: "+ zipcode + "\nTax Rate:" +
			// taxrate+ "\nYTD_Sales_Sum: " +ytdSalesSum);
		}
	}

	public static void GenDistStationData(int numDS) throws SQLException {
		distPerWarehouse = numDS;
		Random rand = new Random();
		int wh_ID;
		int ds_ID;
		int zipcode;
		int taxrate;
		float ytdSalesSum;
		for (int x = 0; x < warehouses; x++) {
			for (int y = 0; y < numDS; y++) {
				// generate the data
				wh_ID = x; // In dealing with PK's and FK's, I assume we'll need
							// a way to sync these for each table i.e. wh_ID's
							// should match up
				ds_ID = y; // ^^same as above, what should these really be?
				String ds_Name = generateString(7);
				String street_Address = alphaNumString();
				String city = generateString(7);
				String state = generateString(2);
				state = state.toUpperCase();
				zipcode = rand.nextInt(90000) + 10000; // generates a zipcode
														// from 10000-99999
				taxrate = rand.nextInt(8) + 1; // generates a taxrate from 1% to
												// 8%
				int dollars = rand.nextInt(90000) + 1000;
				int cents = rand.nextInt(90) + 10;
				String ytdSalesString = dollars + "." + cents;
				ytdSalesSum = Float.parseFloat(ytdSalesString);
				// do a sql insert here using the JDBC
				Statement st = connection.createStatement();
				String insQuery = ("INSERT INTO DistStation VALUES (?,?,?,?,?,?,?,?,?)");
				PreparedStatement ps = connection.prepareStatement(insQuery);
				ps.setLong(1, wh_ID);
				ps.setLong(2, ds_ID);
				ps.setString(3, ds_Name);
				ps.setString(4, street_Address);
				ps.setString(5, city);
				ps.setString(6, state);
				ps.setLong(7, zipcode);
				ps.setLong(8, taxrate);
				ps.setFloat(9, ytdSalesSum);
				ps.executeUpdate();
				ps.close();
				ps = null;
			}
		}
	}

	public static void GenCustomerData(int numCust) throws SQLException {
		custPerDist = numCust;
		Random rand = new Random();
		int wh_ID;
		int ds_ID;
		int cust_ID;
		int zipcode;
		int active_discount;
		int debt;
		int ytdPurchaseTotal;
		int num_payments;
		int num_deliveries;
		for (int x = 0; x < warehouses; x++) {
			for (int y = 0; y < distPerWarehouse; y++) {
				for (int z = 0; z < numCust; z++) {
					// generate the data
					wh_ID = x;
					ds_ID = y;
					cust_ID = z;
					String first_Name = generateString(8); // could make first
															// letter capital at
															// some point
					String middle_Init = generateString(1);
					String last_Name = generateString(10);
					String street_Address = alphaNumString();
					String city = generateString(7);
					String state = generateString(2);
					state = state.toUpperCase();
					zipcode = rand.nextInt(90000) + 10000; // generates a
															// zipcode from
															// 10000-99999
					int num1 = rand.nextInt(900) + 100;
					int num2 = rand.nextInt(900) + 100;
					int num3 = rand.nextInt(9000) + 100;
					String phone_Num = num1 + "-" + num2 + "-" + num3;
					String signup_Date = generateDate();
					active_discount = rand.nextInt(20);
					debt = rand.nextInt(40000); // arbitrary
					ytdPurchaseTotal = rand.nextInt(200000);
					num_payments = rand.nextInt(100);
					num_deliveries = rand.nextInt(100);
					// do a sql insert here using the JDBC
					Statement st = connection.createStatement();
					String insQuery = ("INSERT INTO Customers VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)");
					PreparedStatement ps = connection
							.prepareStatement(insQuery);
					ps.setLong(1, wh_ID);
					ps.setLong(2, ds_ID);
					ps.setLong(3, cust_ID);
					ps.setString(4, first_Name);
					ps.setString(5, middle_Init);
					ps.setString(6, last_Name);
					ps.setString(7, street_Address);
					ps.setString(8, city);
					ps.setString(9, state);
					ps.setLong(10, zipcode);
					ps.setString(11, phone_Num);
					ps.setString(12, signup_Date);
					ps.setLong(13, active_discount);
					ps.setLong(14, debt);
					ps.setLong(15, ytdPurchaseTotal);
					ps.setLong(16, num_payments);
					ps.setLong(17, num_deliveries);
					ps.executeUpdate();
					ps.close();
					ps = null;
				}
			}
		}
	}

	public static void GenOrderData(int numOrders) throws SQLException {
		Random rand = new Random();
		int wh_ID;
		int ds_ID;
		int cust_ID;
		int order_ID;
		int num_Items;
		for (int w = 0; w < warehouses; w++) {
			for (int x = 0; x < distPerWarehouse; x++) {
				for (int y = 0; y < custPerDist; y++) {
					int numOrd = rand.nextInt(numOrders) + 1; // generates a
																// number of
																// orders from
																// 1-100 for
																// each cust
					for (int z = 0; z < numOrd; z++) {
						wh_ID = w;
						ds_ID = x;
						cust_ID = y;
						order_ID = z;
						String date_Placed = generateDate();
						String completed_Flag = "Completed";
						num_Items = rand.nextInt(80);
						String insQuery = ("INSERT INTO Orders VALUES (?,?,?,?,?,?,?)");
						PreparedStatement ps = connection
								.prepareStatement(insQuery);
						ps.setLong(1, wh_ID);
						ps.setLong(2, ds_ID);
						ps.setLong(3, cust_ID);
						ps.setLong(4, order_ID);
						ps.setString(5, date_Placed);
						ps.setString(6, completed_Flag);
						ps.setLong(7, num_Items);
						ps.executeUpdate();
						ps.close();
						ps = null;
						int numLItems = rand.nextInt((15 - 5) + 1) + 5; // generates
																		// random
																		// number
																		// of
																		// line
																		// items
																		// 1-15
						for (int a = 0; a < numLItems; a++) {
							// generate the data
							wh_ID = w;
							ds_ID = x;
							cust_ID = y;
							order_ID = z;
							int li_ID = a;
							int item_ID = rand.nextInt(30);
							int quantity = rand.nextInt(3000);
							int total_Cost = rand.nextInt(2000);
							String date_Delivered = generateDate();
							// do a sql insert here using the JDBC
							Statement st = connection.createStatement();
							String insLIQuery = ("INSERT INTO LineItems VALUES (?,?,?,?,?,?,?,?,?)");
							PreparedStatement psLI = connection
									.prepareStatement(insLIQuery);
							psLI.setLong(1, wh_ID);
							psLI.setLong(2, ds_ID);
							psLI.setLong(3, cust_ID);
							psLI.setLong(4, order_ID);
							psLI.setLong(5, li_ID);
							psLI.setLong(6, item_ID);
							psLI.setLong(7, quantity);
							psLI.setLong(8, total_Cost);
							psLI.setString(9, date_Delivered);
							psLI.executeUpdate();
							psLI.close();
							psLI = null;
						}

					}
				}
			}
		}
	}

//	public static void GenLineItemData(int numOrders, int numLIs)
//			throws SQLException {
//		Random rand = new Random();
//		int wh_ID;
//		int ds_ID;
//		int cust_ID;
//		int order_ID;
//		int li_ID;
//		int item_ID;
//		int quantity;
//		int total_Cost;
//		for (int w = 0; w < warehouses; w++) {
//			for (int x = 0; x < distPerWarehouse; x++) {
//				for (int y = 0; y < custPerDist; y++) {
//					for (int z = 0; z < numOrders; z++) {
//						int numLItems = rand.nextInt((15 - 5) + 1) + 5; // generates
//																		// random
//																		// number
//																		// of
//																		// line
//																		// items
//																		// from
//																		// 5-15
//						for (int a = 0; a < numLItems; a++) {
//							// generate the data
//							wh_ID = w;
//							ds_ID = x;
//							cust_ID = y;
//							order_ID = z;
//							li_ID = a;
//							item_ID = rand.nextInt(30);
//							quantity = rand.nextInt(3000);
//							total_Cost = rand.nextInt(2000);
//							String date_Delivered = generateDate();
//							// do a sql insert here using the JDBC
//							Statement st = connection.createStatement();
//							String insQuery = ("INSERT INTO LineItems VALUES (?,?,?,?,?,?,?,?,?)");
//							PreparedStatement ps = connection
//									.prepareStatement(insQuery);
//							ps.setLong(1, wh_ID);
//							ps.setLong(2, ds_ID);
//							ps.setLong(3, cust_ID);
//							ps.setLong(4, order_ID);
//							ps.setLong(5, li_ID);
//							ps.setLong(6, item_ID);
//							ps.setLong(7, quantity);
//							ps.setLong(8, total_Cost);
//							ps.setString(9, date_Delivered);
//							ps.executeUpdate();
//							ps.close();
//							ps = null;
//						}
//					}
//				}
//			}
//		}
//	}

	public static void GenItemData(int numItems) throws SQLException {
		Random rand = new Random();
		int item_ID;
		float price; // Also needs a function to generate dollars and cents
		for (int x = 0; x < numItems; x++) {
			// generate the data
			item_ID = x;
			String item_Name = generateString(15);
			int dollars = rand.nextInt(90000) + 1000;
			int cents = rand.nextInt(90) + 10;
			String priceString = dollars + "." + cents;
			price = Float.parseFloat(priceString);
			// do a sql insert here using the JDBC
			Statement st = connection.createStatement();
			String insQuery = ("INSERT INTO Items VALUES (?,?,?)");
			PreparedStatement ps = connection.prepareStatement(insQuery);
			ps.setLong(1, item_ID);
			ps.setString(2, item_Name);
			ps.setFloat(3, price);
			ps.executeUpdate();
			ps.close();
			ps = null;
		}
	}

	public static void GenStockData(int numItems) throws SQLException {
		Random rand = new Random();
		int wh_ID;
		int item_ID;
		int quantity_avail;
		int quantity_sold;
		int num_orders;
		for (int x = 0; x < warehouses; x++) {
			for (int y = 0; y < numItems; y++) {
				// generate the data
				wh_ID = x;
				item_ID = y;
				quantity_avail = rand.nextInt(5000);
				quantity_sold = rand.nextInt(5000);
				num_orders = rand.nextInt(20000);
				// do a sql insert here using the JDBC
				Statement st = connection.createStatement();
				String insQuery = ("INSERT INTO Stock VALUES (?,?,?,?,?)");
				PreparedStatement ps = connection.prepareStatement(insQuery);
				ps.setLong(1, wh_ID);
				ps.setLong(2, item_ID);
				ps.setLong(3, quantity_avail);
				ps.setLong(4, quantity_sold);
				ps.setLong(5, num_orders);
				ps.executeUpdate();
				ps.close();
				ps = null;
			}
		}
	}

	// This will generate a random string of characters
	public static String generateString(int length) {
		String characters = "abcdefghijklmnopqrstuvwxyz";
		Random rng = new Random();
		char[] text = new char[length];
		for (int i = 0; i < length; i++) {
			text[i] = characters.charAt(rng.nextInt(characters.length()));
		}
		return new String(text);
	}

	// This will generate alpha-numeric string for address
	// Double check if that's how to append a space
	public static String alphaNumString() {
		StringBuilder temp = new StringBuilder();
		Random rng = new Random();
		temp.append(rng.nextInt(999));
		temp.append(" ");
		temp.append(generateString(7));
		if (rng.nextInt(3) == 0) {
			temp.append(" Rd.");
		} else if (rng.nextInt(3) == 1) {
			temp.append(" St.");
		} else
			temp.append(" Ave.");

		return temp.toString();
	}

	// This will generate random dates
	public static String generateDate() {
		StringBuilder temp = new StringBuilder();
		Random rng = new Random();
		int firstDigitDate = rng.nextInt(3);
		temp.append(firstDigitDate);
		if (firstDigitDate < 3)
			temp.append(rng.nextInt(10));
		else
			temp.append(rng.nextInt(1));
		temp.append("-");
		int month = rng.nextInt(12);
		if (month == 0)
			temp.append("JAN-");
		else if (month == 1)
			temp.append("FEB-");
		else if (month == 2)
			temp.append("MAR-");
		else if (month == 3)
			temp.append("APR-");
		else if (month == 4)
			temp.append("MAY-");
		else if (month == 5)
			temp.append("JUN-");
		else if (month == 6)
			temp.append("JUL-");
		else if (month == 7)
			temp.append("AUG-");
		else if (month == 8)
			temp.append("SEP-");
		else if (month == 9)
			temp.append("OCT-");
		else if (month == 10)
			temp.append("NOV-");
		else if (month == 11)
			temp.append("DEC-");

		int year = rng.nextInt(2);
		if (year == 0)
			temp.append(14);
		else
			temp.append(15);

		return temp.toString();
	}

	public static void viewDB() throws SQLException {
		Statement stmt = connection.createStatement();
		ResultSet rs = stmt.executeQuery("select * from Warehouse");
		ResultSetMetaData rsmd = rs.getMetaData();
		System.out.println("Warehouse");
		System.out
				.println("--------------------------------------------------------------------------------------------------");
		String format = "%-7s%-16s%-20s%-12s%-7s%-10s%-12s%-17s%n";
		System.out.printf(format, rsmd.getColumnName(1), rsmd.getColumnName(2),
				rsmd.getColumnName(3), rsmd.getColumnName(4),
				rsmd.getColumnName(5), rsmd.getColumnName(6),
				rsmd.getColumnName(7), rsmd.getColumnName(8));
		System.out
				.println("--------------------------------------------------------------------------------------------------");
		while (rs.next()) {
			String format2 = "%-7s%-16s%-20s%-12s%-7s%-10s%-12s%-17s%n";
			System.out.printf(format2, rs.getLong(1), rs.getString(2),
					rs.getString(3), rs.getString(4), rs.getString(5),
					rs.getLong(6), rs.getLong(7), rs.getFloat(8));
		}
	}

	public static void resetDatabase() throws SQLException {
		System.out.println("Reseting the database...");
		String s = new String();
		StringBuffer sb = new StringBuffer();

		try {
			FileReader f = new FileReader(new File("GroceryDB.sql"));

			BufferedReader br = new BufferedReader(f);

			while ((s = br.readLine()) != null) {
				sb.append(s); // build the sql statement
			}
			br.close();

			// split each request
			String[] rqts = sb.toString().split(";");

			if (connection != null) {
				Statement st = connection.createStatement();

				for (int i = 0; i < rqts.length; i++) {
					if (!rqts[i].trim().equals("")) {
						st.execute(rqts[i]);
						// System.out.println(">>"+rqts[i]);
					}
				}
			}
		} catch (Exception e) {
			System.out.println("Error: " + e.toString());
			System.out.println("Error: ");
			e.printStackTrace();
			System.out.println(sb.toString());
		}

	}

	public static void genDB() throws SQLException {
		System.out.println("Reseting the database...");
		String s = new String();
		StringBuffer sb = new StringBuffer();

		try {
			FileReader f = new FileReader(new File("GroceryDB1.sql"));

			BufferedReader br = new BufferedReader(f);

			while ((s = br.readLine()) != null) {
				sb.append(s); // build the sql statement
			}
			br.close();

			// split each request
			String[] rqts = sb.toString().split(";");

			if (connection != null) {
				Statement st = connection.createStatement();

				for (int i = 0; i < rqts.length; i++) {
					if (!rqts[i].trim().equals("")) {
						st.execute(rqts[i]);
						// System.out.println(">>"+rqts[i]);
					}
				}
			}
		} catch (Exception e) {
			System.out.println("Error: " + e.toString());
			System.out.println("Error: ");
			e.printStackTrace();
			System.out.println(sb.toString());
		}

	}

	public static void newOrder() throws SQLException {
		// read in all required information
		Scanner read = new Scanner(System.in);
		System.out.print("Please enter the unique customer id: ");
		int custID = read.nextInt();
		System.out
				.print("Enter the ids of the items in the order separated by commas: ");
		String itemIDs = read.nextLine();
		System.out
				.print("Enter the quantities of each item separated by commas: ");
		String itemQuantities = read.nextLine();
		System.out.print("Enter the total number of items ordered: ");
		int totalItems = read.nextInt();

		// parse itemIDs and quantities
		itemIDs = itemIDs.replaceAll("\\s+", ""); // first remove any spaces
		String[] items = itemIDs.split(",");
		itemQuantities = itemQuantities.replaceAll("\\s+", "");
		String[] quantities = itemQuantities.split(",");

		List<Integer> iIds = new ArrayList<Integer>();
		for (String s : items) {
			iIds.add(Integer.parseInt(s));
		}
		List<Integer> iQuantities = new ArrayList<Integer>();
		for (String s : quantities) {
			iQuantities.add(Integer.parseInt(s));
		}

		int tItems = 0;
		for (int i : iQuantities) {
			tItems = tItems + i;
		}

		if ((iIds.size() != iQuantities.size())) {
			System.out
					.println("The number of item ids and quantites must match. Please try the order again.");
			return;
		} else if (tItems != totalItems) {
			System.out
					.println("The total number of items and the sum of all quantities must match. Please try the order again.");
			return;
		}

	}

	public static void makePayment() throws SQLException {
		Scanner read = new Scanner(System.in);
		System.out.print("Please enter your unique customer id: ");
		int custID = read.nextInt();
		System.out.print("Please enter your payment amount: ");
		double payAmt = read.nextDouble();

		String selectQuery = ("SELECT * FROM CUSTOMERS WHERE CUST_ID = ?");
		PreparedStatement ps = connection.prepareStatement(selectQuery);
		ps.setLong(1, custID);
		ResultSet rs = ps.executeQuery();
		ps.close();
		ps = null;
		String debt = rs.getString(15);
		double debtAmt = Double.parseDouble(debt);
		int numPay = rs.getInt(17);
		int wh_id = rs.getInt(1);
		int ds_id = rs.getInt(2);

		// get YTD_SALES_SUM for ds so that is may also be updated
		String ytdSales = ("SELECT YTD_SALES_SUM FROM DISTSTATION WHERE WH_ID=? AND DS_ID=?");
		PreparedStatement ytdPrep = connection.prepareStatement(ytdSales);
		ytdPrep.setLong(1, wh_id);
		ytdPrep.setLong(2, ds_id);
		ResultSet ytdRS = ytdPrep.executeQuery();
		double ytdSalesSum = ytdRS.getDouble(1);

		if (payAmt >= debtAmt) {
			double extra = payAmt - debtAmt;
			String updateQuery = ("UPDATE CUSTOMERS SET debt=? WHERE CUST_ID=?");
			PreparedStatement prep = connection.prepareStatement(updateQuery);
			prep.setLong(1, 0);
			prep.setInt(2, custID);
			prep.executeUpdate();
			prep.close();
			prep = null;
			System.out.println("A payment of $" + debtAmt
					+ " has been applied to your account and $" + extra
					+ " has been refunded to you. Your current debt is $0.");
		} else {
			double newDebt = debtAmt - payAmt;

		}
	}
}
