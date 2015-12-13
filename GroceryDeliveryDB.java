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

	public static void main(String[] args) throws SQLException, ClassNotFoundException {
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
					.print("1 - \tCreate Database (no drop table statements, no data gen)\n2 - \tReset DB, Generate Data (Drops all tables, recreates DB, Gens Data)\n3 - \tView databases\n4 - \tNew Order\n5 - \tMake a payment\n6 - \tCheck order status\n7 - \tDelivery transaction\n8 - \tStock transaction\n9 - \tExit Application\n10 -\tDrop All Tables\nEnter a number: ");
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
				checkOrderStatus();
				break;
			case 7:
				deliverItems();
				break;
			case 8:
				checkStockLevels();
				break;
			case 9:
				connection.close();
				System.out.println("Connection closed.");
				System.exit(0);
				break;
			case 10:
				dropTables();
				break;
			case 11:
				printstuff();
				break;
			}
		}
	}

	/////////////////////////////////////////////////////
	//////////////INITILIZATION FUNCTIONS////////////////	
	/////////////////////////////////////////////////////
	public static void genDB() throws SQLException {
		System.out.println("Creating the database...");
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

			if (connection != null) 
			{
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
	public static void dropTables() throws SQLException {
		String selectQuery = ("DROP TABLE Stock");
		PreparedStatement ps = connection.prepareStatement(selectQuery);
		try {
			ps.executeQuery();
		} catch (SQLException e) {
		}
		ps.close();
		ps = null;
		selectQuery = ("DROP TABLE LineItems");
		ps = connection.prepareStatement(selectQuery);
		try {
			ps.executeQuery();
		} catch (SQLException e) {
		}
		ps.close();
		ps = null;
		selectQuery = ("DROP TABLE Items");
		ps = connection.prepareStatement(selectQuery);
		try {
			ps.executeQuery();
		} catch (SQLException e) {
		}
		ps.close();
		ps = null;
		selectQuery = ("DROP TABLE Orders");
		ps = connection.prepareStatement(selectQuery);
		try {
			ps.executeQuery();
		} catch (SQLException e) {
		}
		ps.close();
		ps = null;
		selectQuery = ("DROP TABLE Customers");
		ps = connection.prepareStatement(selectQuery);
		try {
			ps.executeQuery();
		} catch (SQLException e) {
		}
		ps.close();
		ps = null;
		selectQuery = ("DROP TABLE DistStation");
		ps = connection.prepareStatement(selectQuery);
		try {
			ps.executeQuery();
		} catch (SQLException e) {
		}
		ps.close();
		ps = null;
		selectQuery = ("DROP TABLE Warehouse");
		ps = connection.prepareStatement(selectQuery);
		try {
			ps.executeQuery();
		} catch (SQLException e) {
		}
		ps.close();
		ps = null;
	}
	//THIS IS THE FUNCTION WHERE YOU SET QUANTITES OF DATA GENERATION
	public static void GenData() throws SQLException, ClassNotFoundException
	{
		dropTables();
		genDB();

		// gen the data for the db
		try {
			// 1 warehouse
			// 8 distribution stations per warehouse
			// 100 customers per distribution station
			// 1000 items
			// 1000 stock listings per warehouse
			// Between 1 and 50 orders per customer
			// Between 3 and 10 line items per order

			System.out.println("Generating data for the database... (takes about 10 minutes)");
			System.out.println("Generating items...");
			GenItemData(1000);// 1000 unique grocery items
			System.out.println("\tDone.");
			System.out.println("Generating warehouse...");
			GenWarehouseData(1); // number of warehouses
			System.out.println("\tDone.");
			System.out.println("Generating stock entries...");
			GenStockData(1000, 1);
			System.out.println("\tDone.");
			System.out.println("Generating distribution stations...");
			GenDistStationData(8);// 8 DS per HW
			System.out.println("\tDone.");
			System.out.println("Generating customers...");
			GenCustomerData(100);// 100 customers per DS
			System.out.println("\tDone.");
			System.out.println("Generating orders and line items...");
			GenOrderData(50);// between 1-50 orders per cust
			System.out.println("\tDone.");

			System.out.println("Updating all data in DB for Data Consistency..");
			//DATA CONSISTENCY for post data gen (functions below)
			updateCustData(); //works perfectly
			updateDSData(); //works perfectly
			updateWHData(); //works perfectly
			updateStockData(); //works perfectly
			System.out.println("\tDone.");
			//all other data consistency is done within the transactions without triggers

			//data consistancy is done without triggers because I hate them
			//in each transaction, data consistencey is manually done.
			//triggers suck.
			

			//DATA CONSISTENCY NOTES
			
			//NEW ORDER TRANSACTION MUST UPDATE:
				//Customer Debt -done
				//Stock Quantities -done
				//Stock.Num_orders -done

			//MAKE PAYMENT TRANSACTION MUST UPDATE:
				//CUSTOMER.DEBT -done
				//CUSTOMER.YTD_PURCHASE_TOTAL -done
				//CUSTOMER.NUM_PAYMENTS -done
				//CUSTOMER.DELIVERIES?? we wont do this here, we will do it deliver items
				//DISTSTATION YTD-SALES-SUM -done
				//WAREHOUSE YTD-SALES-SUM -done

			//DELIVER EVERYTHING TRANSACTION MUST UPDATE:
				//Customer Debt -done
				//CUSTOMER.YTD_PURCHASE_TOTAL -done
				//CUSTOMER.NUM_PAYMENTS -done
				//CUSTOMER.NUM_DELIVERIES -done
				//DISTSTATION YTD-SALES-SUM -done
				//WAREHOUSE YTD-SALES-SUM -done
				//LINE ITEM DATE DELIVERED -done
				//ORDER  COMEPLETED FLAG -done
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	/////////////////////////////////////////////////////
	//////////////////TRANSACTIONS///////////////////////	
	/////////////////////////////////////////////////////
	//TX1
	public static void newOrder() throws SQLException 
	{
		// read in all required information
		Scanner read = new Scanner(System.in);
		System.out.print("Please enter your warehouse id: ");
		int wh_id = 0;
		try {
			wh_id = Integer.parseInt(read.nextLine());
		} catch (NumberFormatException e) {
			e.printStackTrace();
		}
		System.out.print("Please enter your distribution station id: ");
		int ds_ID = 0;
		try {
			ds_ID = Integer.parseInt(read.nextLine());
		} catch (NumberFormatException e) {
			e.printStackTrace();
		}

		System.out.print("Please enter the unique customer id: ");
		int custID = 0;
		try {
			custID = Integer.parseInt(read.nextLine());
		} catch (NumberFormatException e) {
			e.printStackTrace();
		}

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

		/////////////////////////////////
		///START OF ACTUAL TRANSACTION///
		/////////////////////////////////
		Statement st = connection.createStatement();
		st.executeQuery("SET TRANSACTION READ WRITE");
		// get WH_ID from Customers table
		String whQuery = "SELECT * FROM CUSTOMERS WHERE CUST_ID = ? AND DS_ID = ? AND WH_ID = ?";
		PreparedStatement getWHID = connection.prepareStatement(whQuery);
		getWHID.setLong(1, custID);
		getWHID.setLong(2, ds_ID);
		getWHID.setLong(3, wh_id);
		ResultSet whRS = getWHID.executeQuery();
		while (whRS.next()) {
			// get last order ID for customer and increment it by 1
			String ordIDQuery = "SELECT MAX(ORDER_ID) FROM ORDERS WHERE CUST_ID=? AND DS_ID=? AND WH_ID = ?";
			PreparedStatement maxOrdID = connection
					.prepareStatement(ordIDQuery);
			maxOrdID.setLong(1, custID);
			maxOrdID.setLong(2, ds_ID);
			maxOrdID.setLong(3, wh_id);
			ResultSet maxOrd = maxOrdID.executeQuery();
			while (maxOrd.next()) {
				int maxOrderID = maxOrd.getInt(1);
				int newOrderID = maxOrderID + 1;

				// create new order
				String insertOrder = "INSERT INTO Orders VALUES (?,?,?,?,?,?,?)";
				PreparedStatement insNewOrder = connection
						.prepareStatement(insertOrder);
				insNewOrder.setLong(1, wh_id);
				insNewOrder.setLong(2, ds_ID);
				insNewOrder.setLong(3, custID);
				insNewOrder.setLong(4, newOrderID);
				java.sql.Date datePurchased = new java.sql.Date(
						(new java.util.Date()).getTime());
				insNewOrder.setDate(5, datePurchased);
				insNewOrder.setString(6, "Incomplete");
				insNewOrder.setLong(7, totalItems);
				insNewOrder.executeUpdate();

				// create line item entries for order
				for (int i = 0; i < iIds.size(); i++) {
					String liInsert = "INSERT INTO LineItems VALUES (?,?,?,?,?,?,?,?,?)";
					PreparedStatement insLI = connection.prepareStatement(liInsert);
					insLI.setLong(1, wh_id);
					insLI.setLong(2, ds_ID);
					insLI.setLong(3, custID);
					insLI.setLong(4, newOrderID);
					insLI.setLong(5, i);
					insLI.setLong(6, iIds.get(i));
					insLI.setLong(7, iQuantities.get(i));
					// calculate total cost
					String priceQuery = "SELECT PRICE FROM ITEMS WHERE ITEM_ID =?";
					PreparedStatement pricePS = connection.prepareStatement(priceQuery);
					pricePS.setLong(1, iIds.get(i));
					ResultSet priceRS = pricePS.executeQuery();
					double price = 0;
					while (priceRS.next()) {
						price = priceRS.getDouble(1);
						break;
					}

					if (price == 0) {
						System.out
								.println("Could not retrieve item price. Please check the item ID.");
						return;
					}

					double totalPrice = price * iQuantities.get(i);
					insLI.setDouble(8, totalPrice);
					insLI.setDate(9, null);
					insLI.executeUpdate();
					insLI.close();
					insLI = null;
					//DATA CONSISTANCY CUSTOMER DEBT
					String updateCustomerDebt = "UPDATE CUSTOMERS set debt = debt + ? WHERE WH_ID = ? AND DS_ID = ? AND CUST_ID = ?";
					PreparedStatement updateCust = connection.prepareStatement(updateCustomerDebt);
					updateCust.setDouble(1, totalPrice);
					updateCust.setLong(2, wh_id);
					updateCust.setLong(3, ds_ID);
					updateCust.setLong(4, custID);
					updateCust.executeUpdate();
					updateCust.close();
					updateCust = null;
					//DATA CONSISTANCY STOCK
					String updateStock = "UPDATE STOCK set NUM_ORDERS = NUM_ORDERS + 1, QUANTITY_AVAILABLE = QUANTITY_AVAILABLE -  ?, quantity_sold = quantity_sold + ? WHERE ITEM_ID = ?";
					PreparedStatement updateStockPS = connection.prepareStatement(updateStock);
					updateStockPS.setLong(1, iQuantities.get(i));
					updateStockPS.setLong(2, iQuantities.get(i));
					updateStockPS.setLong(3, iIds.get(i));
					updateStockPS.executeUpdate();
					updateStockPS.close();
					updateStockPS = null;
				}
				insNewOrder.close();
				insNewOrder = null;
			}
			maxOrd.close();
			maxOrd = null;
		}
		whRS.close();
		whRS = null;
		st.executeQuery("COMMIT");
		st.close();
		st = null;
	}
	//TX2
	public static void makePayment() throws SQLException 
	{
		Scanner read = new Scanner(System.in);
		System.out.print("Please enter the id of your warehouse: ");
		int wh_id = read.nextInt();
		System.out.print("Please enter the id of your distribution center: ");
		int ds_id = read.nextInt();
		System.out.print("Please enter your unique customer id: ");
		int custID = read.nextInt();
		System.out.print("Please enter your payment amount: ");
		double payAmt = read.nextDouble();
		Statement st = connection.createStatement();
		st.executeUpdate("SET TRANSACTION READ WRITE");
		String selectQuery = ("SELECT * FROM CUSTOMERS WHERE CUST_ID = ? AND DS_ID = ?");
		PreparedStatement ps = connection.prepareStatement(selectQuery);

		ps.setLong(1, custID);
		ps.setLong(2, ds_id);
		ResultSet rs = ps.executeQuery();
		if (rs == null) {
			System.out.println("No matching entries found.");
			return;
		}
		int numPay;
		double debtAmt;
		String debt;
		while (rs.next()) {
			debt = rs.getString(14);
			debtAmt = Double.parseDouble(debt);
			numPay = rs.getInt(16);

			// get YTD_SALES_SUM for ds so that is may also be updated
			String ytdSales = ("SELECT YTD_SALES_SUM FROM DISTSTATION WHERE WH_ID=? AND DS_ID=?");
			PreparedStatement ytdPrep = connection.prepareStatement(ytdSales);
			ytdPrep.setLong(1, wh_id);
			ytdPrep.setLong(2, ds_id);
			ResultSet ytdRS = ytdPrep.executeQuery();
			while (ytdRS.next()) 
			{
				float ytdSalesSum = ytdRS.getFloat(1);

				if (payAmt > debtAmt) {
					double extra = payAmt - debtAmt;
					String updateQuery = ("UPDATE CUSTOMERS SET NUM_PAYMENTS = NUM_PAYMENTS + 1, debt = ?, YTD_PURCHASE_TOTAL = YTD_PURCHASE_TOTAL + ? WHERE CUST_ID=? AND DS_ID=?");
					PreparedStatement prep = connection.prepareStatement(updateQuery);
					prep.setLong(1, 0);
					prep.setDouble(2, debtAmt);
					prep.setInt(3, custID);
					prep.setInt(4, ds_id);
					prep.executeUpdate();
					prep.close();
					prep = null;
					System.out.println("A payment of $"
									+ debtAmt
									+ " has been applied to your account and $"
									+ extra
									+ " has been refunded to you. Your current debt is $0.");
					ytdSalesSum = ytdSalesSum + (float) debtAmt;
					
					//DATA CONSISTANCY FOR WAREHOUSE AND DS YTD SALES SUM
					String updateYTD = ("UPDATE DISTSTATION SET YTD_SALES_SUM = ? WHERE WH_ID=? AND DS_ID=?");
					PreparedStatement ytdp = connection.prepareStatement(updateYTD);
					ytdp.setFloat(1, ytdSalesSum);
					ytdp.setLong(2, wh_id);
					ytdp.setLong(3, ds_id);
					ytdp.executeUpdate();
					ytdp.close();
					ytdp = null;
					String updateYTD2 = ("UPDATE WAREHOUSE SET YTD_SALES_SUM = YTD_SALES_SUM + ? WHERE WH_ID=?");
					PreparedStatement ytdps = connection.prepareStatement(updateYTD2);
					ytdps.setFloat(1, (float)debtAmt);
					ytdps.setLong(2, wh_id);
					ytdps.executeUpdate();
					ytdps.close();
					ytdps = null;
				} 
				else {
					float newDebt = (float) debtAmt - (float) payAmt;
					String updateQuery = ("UPDATE CUSTOMERS SET NUM_PAYMENTS = NUM_PAYMENTS + 1, debt=?, YTD_PURCHASE_TOTAL = YTD_PURCHASE_TOTAL + ?  WHERE CUST_ID=? AND DS_ID=?");
					PreparedStatement prep = connection
							.prepareStatement(updateQuery);
					prep.setFloat(1, newDebt);
					prep.setDouble(2, payAmt);
					prep.setInt(3, custID);
					prep.setInt(4, ds_id);
					prep.executeUpdate();
					prep.close();
					prep = null;
					System.out.println("A payment of $"
									+ payAmt
									+ " has been applied to your account and your current debt is $"
									+ newDebt + ".");
					ytdSalesSum = ytdSalesSum + (float) payAmt;
					//DATA CONSISTANCY FOR WAREHOUSE AND DS YTD SALES SUM
					String updateYTD = ("UPDATE DISTSTATION SET YTD_SALES_SUM = ? WHERE WH_ID=? AND DS_ID=?");
					PreparedStatement ytdp = connection.prepareStatement(updateYTD);
					ytdp.setFloat(1, ytdSalesSum);
					ytdp.setLong(2, wh_id);
					ytdp.setLong(3, ds_id);
					ytdp.executeUpdate();
					ytdp.close();
					ytdp = null;
					String updateYTD2 = ("UPDATE WAREHOUSE SET YTD_SALES_SUM = YTD_SALES_SUM + ? WHERE WH_ID=?");
					PreparedStatement ytdps = connection.prepareStatement(updateYTD2);
					ytdps.setDouble(1, payAmt);
					ytdps.setLong(2, wh_id);
					ytdps.executeUpdate();
					ytdps.close();
					ytdps = null;
				}
			}
			ytdRS.close();
			ytdRS = null;
		}
		ps.close();
		ps = null;
		st.executeUpdate("COMMIT");
		st.close();
		st = null;
	}
	//question is very ambiguous and either way the metric is not very useful
	//due to the ambiguity of the returned result
	//TX3
	public static void checkStockLevels() throws SQLException {
		Scanner read = new Scanner(System.in);
		//this is for when there is more than 1 warehouse since ds_ids are not globally unique
		System.out.print("Please enter your warehouse id: ");
		int wh_id = read.nextInt();
		System.out.print("Please enter distribution station id: ");
		int ds_id = read.nextInt();
		System.out.print("Please enter the stock threshold you wish to check: ");
		int stockThreshold = read.nextInt();
		
		
		Statement st = connection.createStatement();
		st.executeQuery("SET TRANSACTION READ WRITE");
		//return the number of unique items sold recently that are below stock threshold
		String top20 = "SELECT COUNT(QUANTITY_AVAILABLE) FROM (SELECT QUANTITY_AVAILABLE FROM STOCK NATURAL JOIN (SELECT ITEM_ID FROM LINEITEMS NATURAL JOIN (SELECT ORDER_ID FROM (SELECT * FROM ORDERS WHERE DS_ID = ? AND WH_ID =? ORDER BY DATE_PLACED DESC) WHERE ROWNUM <=20))) WHERE QUANTITY_AVAILABLE < ?";
		PreparedStatement top20Orders = connection.prepareStatement(top20);
		top20Orders.setLong(1, ds_id);
		top20Orders.setLong(2, wh_id);
		top20Orders.setLong(3, stockThreshold);
		ResultSet rs = top20Orders.executeQuery();
		while(rs.next()){
			System.out.println("There are "+rs.getLong(1)+ " items in warehouse "+ wh_id+ " have a stock below "+ stockThreshold+".");
		}
		
		top20Orders.close();
		top20Orders = null;
		
		//return the sum of quantities purchased recently for items below stock threshold
		String pQuantities = "SELECT SUM(QUANTITY) FROM STOCK NATURAL JOIN (SELECT ITEM_ID, QUANTITY FROM LINEITEMS NATURAL JOIN (SELECT ORDER_ID FROM (SELECT * FROM ORDERS WHERE DS_ID = ? AND WH_ID =? ORDER BY DATE_PLACED DESC) WHERE ROWNUM <=20)) WHERE QUANTITY_AVAILABLE < ?";
		PreparedStatement qTop20 = connection.prepareStatement(pQuantities);
		int sum = 0;
		qTop20.setLong(1, ds_id);
		qTop20.setLong(2, wh_id);
		qTop20.setLong(3, stockThreshold);
		ResultSet qRS = qTop20.executeQuery();
		
		while (qRS.next()) 
		{
			System.out.println(qRS.getInt(1) + " items were purchase recently that are below the stock threshold, "+stockThreshold+ " in warehouse "+wh_id+".");    
		}
		qRS.close();
		qRS = null;
		st.executeQuery("COMMIT");
		st.close();
		st = null;
	}
	//TX4
	public static void deliverItems() throws SQLException {
		Scanner read = new Scanner(System.in);
		System.out.print("Please enter the id of your warehouse: ");
		int wh_id = read.nextInt();
		String incomplete = "Incomplete";
		String completed = "Completed";
		Statement st = connection.createStatement();
		st.executeUpdate("SET TRANSACTION READ WRITE");
		String selectQuery = ("SELECT DS_ID, CUST_ID, ORDER_ID, COMPLETED_FLAG FROM ORDERS WHERE WH_ID = ? AND COMPLETED_FLAG = ? ");
		PreparedStatement ps = connection.prepareStatement(selectQuery);		
		ps.setLong(1, wh_id);
		ps.setString(2, incomplete);
		ResultSet resultSet = ps.executeQuery();
		ResultSetMetaData rsmd = resultSet.getMetaData();
		int columnsNumber = rsmd.getColumnCount();
		while (resultSet.next())
		{
			// update order
			int custID = resultSet.getInt(2);
			int ds_ID = resultSet.getInt(1);
			int order_ID = resultSet.getInt(3);

			String selectQuery2 = ("SELECT TOTAL_COST FROM LINEITEMS WHERE WH_ID = ? AND DS_ID = ? AND CUST_ID = ? AND ORDER_ID = ? ");
			PreparedStatement ps2 = connection.prepareStatement(selectQuery2);		
			ps2.setLong(1, wh_id);
			ps2.setLong(2, ds_ID);
			ps2.setLong(3, custID);
			ps2.setLong(4, order_ID);
			ResultSet resultSet2 = ps2.executeQuery();
			resultSet2.next();
			double totalCostLI = resultSet2.getDouble(1);
			//updates lineitems with the date delivered
			String updateLineItemsQuery = ("UPDATE LINEITEMS SET DATE_DELIVERED = ? WHERE CUST_ID = ? AND DS_ID = ? AND ORDER_ID = ? AND WH_ID = ?");
			PreparedStatement updateLineItems = connection.prepareStatement(updateLineItemsQuery);
			java.sql.Date dateDeliv = new java.sql.Date((new java.util.Date()).getTime());
			updateLineItems.setDate(1, dateDeliv);
			updateLineItems.setLong(2, custID);
			updateLineItems.setLong(3, ds_ID);
			updateLineItems.setLong(4, order_ID);
			updateLineItems.setLong(5, wh_id);
			updateLineItems.executeUpdate();
			updateLineItems.close();
			updateLineItems = null;

			//get debt for later use
			String selectQuery3 = ("SELECT DEBT FROM CUSTOMERS WHERE WH_ID = ? AND DS_ID = ? AND CUST_ID = ?");
			PreparedStatement ps3 = connection.prepareStatement(selectQuery3);	
			ps3.setLong(1, wh_id);
			ps3.setLong(2, ds_ID);
			ps3.setLong(3, custID);
			ResultSet resultSet3 = ps3.executeQuery();
			resultSet3.next();
			double currentDebt = resultSet3.getDouble(1);


			//CUSTOMER DATA CONSISTANCY
			String updateCustomerQuery = ("UPDATE CUSTOMERS SET DEBT = 0, YTD_PURCHASE_TOTAL = YTD_PURCHASE_TOTAL + DEBT, NUM_PAYMENTS = NUM_PAYMENTS + 1, NUM_DELIVERIES = NUM_DELIVERIES + 1  WHERE CUST_ID = ? AND DS_ID = ? AND WH_ID = ?");
			PreparedStatement updateCustomers = connection.prepareStatement(updateCustomerQuery);
			updateCustomers.setLong(1, custID);
			updateCustomers.setLong(2, ds_ID);
			updateCustomers.setLong(3, wh_id);
			updateCustomers.executeUpdate();
			updateCustomers.close();
			updateCustomers = null;

			//Changes orders from incomplete to completed
			String updateOrdersQuery = ("UPDATE ORDERS SET COMPLETED_FLAG=? WHERE CUST_ID=? AND DS_ID=? AND ORDER_ID = ?  AND WH_ID = ?");
			PreparedStatement updateOrders = connection.prepareStatement(updateOrdersQuery);
			updateOrders.setString(1, completed);
			updateOrders.setLong(2, custID);
			updateOrders.setLong(3, ds_ID);
			updateOrders.setLong(4, order_ID);
			updateOrders.setLong(5, wh_id);
			updateOrders.executeUpdate();
			updateOrders.close();
			updateOrders = null;

			//DATA CONSISTANCY FOR WAREHOUSE AND DS YTD SALES SUM
			String updateYTD = ("UPDATE DISTSTATION SET YTD_SALES_SUM = YTD_SALES_SUM + ? WHERE WH_ID=? AND DS_ID=?");
			PreparedStatement ytdp = connection.prepareStatement(updateYTD);
			ytdp.setDouble(1, currentDebt);
			ytdp.setLong(2, wh_id);
			ytdp.setLong(3, ds_ID);
			ytdp.executeUpdate();
			ytdp.close();
			ytdp = null;
			String updateYTD2 = ("UPDATE WAREHOUSE SET YTD_SALES_SUM = YTD_SALES_SUM + ? WHERE WH_ID=?");
			PreparedStatement ytdps = connection.prepareStatement(updateYTD2);
			ytdps.setDouble(1, currentDebt);
			ytdps.setLong(2, wh_id);
			ytdps.executeUpdate();
			ytdps.close();
			ytdps = null;

			ps2.close();
			ps2 = null;
			ps3.close();
			ps3 = null;
		}
		ps.close();
		ps = null;

		st.executeUpdate("COMMIT");
		st.close();
		st = null;
	}
	//TX5
	public static void checkOrderStatus() throws SQLException {
		Scanner read = new Scanner(System.in);
		System.out.print("Please enter the id of your distribution center: ");
		int ds_id = read.nextInt();
		System.out.print("Please enter your unique customer id: ");
		int custID = read.nextInt();
		Statement st = connection.createStatement();
		st.executeUpdate("SET TRANSACTION READ WRITE");
		String selectQuery = ("SELECT ITEM_ID, QUANTITY, TOTAL_COST, DATE_DELIVERED FROM LineItems WHERE CUST_ID = ? AND DS_ID = ? AND ORDER_ID = (SELECT ORDER_ID AS O_ID FROM (SELECT * FROM ORDERS WHERE CUST_ID = ? AND DS_ID = ? ORDER BY DATE_PLACED DESC) WHERE ROWNUM <= 1) ORDER BY ITEM_ID ASC");
		PreparedStatement ps = connection.prepareStatement(selectQuery);		
		ps.setLong(1, custID);
		ps.setLong(2, ds_id);
		ps.setLong(3, custID);
		ps.setLong(4, ds_id);
		ResultSet resultSet = ps.executeQuery();
		ResultSetMetaData rsmd = resultSet.getMetaData();
		int columnsNumber = rsmd.getColumnCount();
		System.out.print("Customer "+custID+" from Distribution Station "+ds_id+"'s last order contained:\n");
		while (resultSet.next()) {
		    for (int i = 1; i <= columnsNumber; i++) {
		        if (i > 1) System.out.print(",       ");
		        String columnValue = resultSet.getString(i);
		        System.out.print(rsmd.getColumnName(i) + " " + columnValue);
		    }
		    System.out.println("");
		}
		st.executeUpdate("COMMIT");
		st.close();
		st = null;
		ps.close();
		ps = null;
	}
	/////////////////////////////////////////////////////
	//////////////////END TRANSACTIONS///////////////////	
	/////////////////////////////////////////////////////


	/////////////////////////////////////////////////////
	//////////////DATA GENERATION FUNCTIONS//////////////	
	/////////////////////////////////////////////////////
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
			ytdSalesSum = 0;
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
				ytdSalesSum = 0;
				// do a sql insert here using the JDBC
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
					debt = 0;
					ytdPurchaseTotal = 0;
					num_payments = 0;
					num_deliveries = 0;
					// do a sql insert here using the JDBC
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
		int num = 0;
		for (int w = 0; w < warehouses; w++) 
		{
			for (int x = 0; x < distPerWarehouse; x++) 
			{
				for (int y = 0; y < custPerDist; y++)
				{
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
						int numLItems = rand.nextInt(8) + 4; 	// generates
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
							int item_ID = rand.nextInt(1000);
							int quantity = rand.nextInt(70)+1;
							int total_Cost = rand.nextInt(2000);
							String date_Delivered = generateDate();
							// do a sql insert here using the JDBC
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
				num = x+1;
				System.out.println("\tData for Distribution Station " + (x+1) + " of " + distPerWarehouse + " generated.");
			}
		}
	}
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
	public static void GenStockData(int numItems, int warehouseCount) throws SQLException {
		Random rand = new Random();
		int wh_ID;
		int item_ID;
		int quantity_avail;
		int quantity_sold;
		int num_orders;
		for (int x = 0; x < warehouseCount; x++) {
			for (int y = 0; y < numItems; y++) {
				// generate the data
				wh_ID = x;
				item_ID = y;
				quantity_avail = rand.nextInt(100000)+100000;
				quantity_sold = 0;
				num_orders = 0;
				// do a sql insert here using the JDBC
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
	/////////////////////////////////////////////////////
	/////////////DATA CONSISTENCY FUNCTIONS//////////////	
	/////////////////////////////////////////////////////
	public static void updateCustData() throws SQLException {
		Scanner read = new Scanner(System.in);
		Statement st = connection.createStatement();
		st.executeUpdate("SET TRANSACTION READ WRITE");
		String selectQuery = ("SELECT * FROM ORDERS");
		PreparedStatement ps = connection.prepareStatement(selectQuery);		
		ResultSet resultSet = ps.executeQuery();
		ResultSetMetaData rsmd = resultSet.getMetaData();
		int columnsNumber = rsmd.getColumnCount();
		while (resultSet.next()) {
			// update Cust Data
			int wh_id = resultSet.getInt(1);
			int ds_ID = resultSet.getInt(2);
			int custID = resultSet.getInt(3);
			int order_ID = resultSet.getInt(4);
			String completed = "Completed";
			String incomplete = "Incomplete";

			String selectQuery2 = ("SELECT COUNT(*) FROM ORDERS WHERE WH_ID = ? AND DS_ID = ? AND CUST_ID = ? AND COMPLETED_FLAG = ? ");
			PreparedStatement ps2 = connection.prepareStatement(selectQuery2);		
			ps2.setLong(1, wh_id);
			ps2.setLong(2, ds_ID);
			ps2.setLong(3, custID);
			ps2.setString(4, completed);
			ResultSet resultSet2 = ps2.executeQuery();
			resultSet2.next();
			int ordercount = resultSet2.getInt(1);
			String selectQuery3 = ("SELECT SUM(TOTAL_COST) FROM LINEITEMS WHERE WH_ID = ? AND DS_ID = ? AND CUST_ID = ?");
			PreparedStatement ps3 = connection.prepareStatement(selectQuery3);		
			ps3.setLong(1, wh_id);
			ps3.setLong(2, ds_ID);
			ps3.setLong(3, custID);
			ResultSet resultSet3 = ps3.executeQuery();
			resultSet3.next();
			double sumOfAllOrders = resultSet3.getDouble(1);

			String updateCustomerQuery = ("UPDATE CUSTOMERS SET NUM_PAYMENTS=?, NUM_DELIVERIES = ?, YTD_PURCHASE_TOTAL = ? WHERE CUST_ID=? AND DS_ID=?");
			PreparedStatement updateCustomers = connection.prepareStatement(updateCustomerQuery);
			updateCustomers.setInt(1, ordercount);
			updateCustomers.setInt(2, ordercount);
			updateCustomers.setDouble(3, sumOfAllOrders);
			updateCustomers.setLong(4, custID);
			updateCustomers.setLong(5, ds_ID);
			updateCustomers.executeUpdate();
			updateCustomers.close();
			updateCustomers = null;
			ps2.close();
			ps2 = null;
			ps3.close();
			ps3 = null;

		}
		st.executeUpdate("COMMIT");
		st.close();
		st = null;
		ps.close();
		ps = null;
	}
	public static void updateDSData() throws SQLException {
		Scanner read = new Scanner(System.in);
		Statement st = connection.createStatement();
		st.executeUpdate("SET TRANSACTION READ WRITE");
		String selectQuery = ("SELECT * FROM ORDERS");
		PreparedStatement ps = connection.prepareStatement(selectQuery);		
		ResultSet resultSet = ps.executeQuery();
		ResultSetMetaData rsmd = resultSet.getMetaData();
		int columnsNumber = rsmd.getColumnCount();
		while (resultSet.next()) {
			// update order
			int wh_id = resultSet.getInt(1);
			int ds_ID = resultSet.getInt(2);
			int custID = resultSet.getInt(3);
			int order_ID = resultSet.getInt(4);

			String selectQuery3 = ("SELECT SUM(YTD_PURCHASE_TOTAL) FROM CUSTOMERS WHERE WH_ID = ? AND DS_ID = ?");
			PreparedStatement ps3 = connection.prepareStatement(selectQuery3);		
			ps3.setLong(1, wh_id);
			ps3.setLong(2, ds_ID);
			ResultSet resultSet3 = ps3.executeQuery();
			resultSet3.next();
			double ytdSalesSum = resultSet3.getDouble(1);

			String updateDSQuery = ("UPDATE DISTSTATION SET YTD_SALES_SUM = ? WHERE WH_ID=? AND DS_ID=?");
			PreparedStatement updateDS = connection.prepareStatement(updateDSQuery);
			updateDS.setDouble(1, ytdSalesSum);
			updateDS.setLong(2, wh_id);
			updateDS.setLong(3, ds_ID);
			updateDS.executeUpdate();
			updateDS.close();
			updateDS = null;
			ps3.close();
			ps3 = null;

		}
		st.executeUpdate("COMMIT");
		st.close();
		st = null;
		ps.close();
		ps = null;
	}
	public static void updateWHData() throws SQLException {
		Scanner read = new Scanner(System.in);
		Statement st = connection.createStatement();
		st.executeUpdate("SET TRANSACTION READ WRITE");
		String selectQuery = ("SELECT * FROM ORDERS");
		PreparedStatement ps = connection.prepareStatement(selectQuery);		
		ResultSet resultSet = ps.executeQuery();
		ResultSetMetaData rsmd = resultSet.getMetaData();
		int columnsNumber = rsmd.getColumnCount();
		while (resultSet.next()) {
			// update order
			int wh_id = resultSet.getInt(1);
			int ds_ID = resultSet.getInt(2);
			int custID = resultSet.getInt(3);
			int order_ID = resultSet.getInt(4);

			String selectQuery3 = ("SELECT SUM(YTD_SALES_SUM) FROM DISTSTATION WHERE WH_ID = ?");
			PreparedStatement ps3 = connection.prepareStatement(selectQuery3);		
			ps3.setLong(1, wh_id);
			ResultSet resultSet3 = ps3.executeQuery();
			resultSet3.next();
			double ytdSalesSum = resultSet3.getDouble(1);

			String updateDSQuery = ("UPDATE WAREHOUSE SET YTD_SALES_SUM = ? WHERE WH_ID=?");
			PreparedStatement updateDS = connection.prepareStatement(updateDSQuery);
			updateDS.setDouble(1, ytdSalesSum);
			updateDS.setLong(2, wh_id);
			updateDS.executeUpdate();
			updateDS.close();
			updateDS = null;
			ps3.close();
			ps3 = null;

		}
		st.executeUpdate("COMMIT");
		st.close();
		st = null;
		ps.close();
		ps = null;
	}
	public static void updateStockData() throws SQLException {
		Scanner read = new Scanner(System.in);
		Statement st = connection.createStatement();
		int wh_id = 0;
		st.executeUpdate("SET TRANSACTION READ WRITE");
		String selectQuery = ("SELECT ITEM_ID, COUNT(ORDER_ID) as \"Number of orders\", SUM(QUANTITY) as \"Number Sold\" FROM LINEITEMS WHERE WH_ID = ? GROUP BY ITEM_ID ORDER BY ITEM_ID ASC");
		PreparedStatement ps = connection.prepareStatement(selectQuery);		
		ps.setLong(1, wh_id);
		ResultSet resultSet = ps.executeQuery();
		while (resultSet.next())
		{
			long itemID = resultSet.getLong(1);
			long numOrders = resultSet.getLong(2);
			long qSold = resultSet.getLong(3);
			String updateSQuery = ("UPDATE STOCK SET QUANTITY_AVAILABLE = QUANTITY_AVAILABLE - ? , QUANTITY_SOLD = QUANTITY_SOLD + ?, NUM_ORDERS = NUM_ORDERS + ? WHERE WH_ID = ? AND ITEM_ID = ?");
			PreparedStatement updateS = connection.prepareStatement(updateSQuery);
			updateS.setDouble(1, qSold);
			updateS.setDouble(2, qSold);
			updateS.setDouble(3, numOrders);
			updateS.setLong(4, wh_id);
			updateS.setLong(5, itemID);
			updateS.executeUpdate();
			updateS.close();
			updateS = null;
		}
		ps.close();
		ps = null;
		st.executeUpdate("COMMIT");
		st.close();
		st = null;
	}


	/////////////////////////////////////////////////////
	/////////DATA GENERATION HELPER FUNCTIONS////////////	
	/////////////////////////////////////////////////////
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
		int month = rng.nextInt(12);
		if (month == 0)
		{
			int firstDigitDate = rng.nextInt(4);
			temp.append(firstDigitDate);
			if (firstDigitDate == 3)
				temp.append(rng.nextInt(2));
			else if (firstDigitDate == 2 || firstDigitDate == 1)
				temp.append(rng.nextInt(10));
			else
				temp.append(rng.nextInt(9)+1);
			temp.append("-");
			temp.append("JAN-");
		}
		else if (month == 1)
		{
			int firstDigitDate = rng.nextInt(3);
			temp.append(firstDigitDate);
			if (firstDigitDate == 2)
				temp.append(rng.nextInt(9));
			else if(firstDigitDate == 1)
				temp.append(rng.nextInt(10));
			else
				temp.append(rng.nextInt(9)+1);
			temp.append("-");
			temp.append("FEB-");
		}
		else if (month == 2)
		{
			int firstDigitDate = rng.nextInt(4);
			temp.append(firstDigitDate);
			if (firstDigitDate == 3)
				temp.append(rng.nextInt(2));
			else if (firstDigitDate == 2 || firstDigitDate == 1)
				temp.append(rng.nextInt(10));
			else
				temp.append(rng.nextInt(9)+1);
			temp.append("-");
			temp.append("MAR-");
		}
		else if (month == 3)
		{
			int firstDigitDate = rng.nextInt(4);
			temp.append(firstDigitDate);
			if (firstDigitDate == 3)
				temp.append(0);
			else if (firstDigitDate == 2 || firstDigitDate == 1)
				temp.append(rng.nextInt(10));
			else
				temp.append(rng.nextInt(9)+1);
			temp.append("-");
			temp.append("APR-");
		}
		else if (month == 4)
		{
			int firstDigitDate = rng.nextInt(4);
			temp.append(firstDigitDate);
			if (firstDigitDate == 3)
				temp.append(rng.nextInt(2));
			else if (firstDigitDate == 2 || firstDigitDate == 1)
				temp.append(rng.nextInt(10));
			else
				temp.append(rng.nextInt(9)+1);
			temp.append("-");
			temp.append("MAY-");
		}
		else if (month == 5)
		{
			int firstDigitDate = rng.nextInt(4);
			temp.append(firstDigitDate);
			if (firstDigitDate == 3)
				temp.append(0);
			else if (firstDigitDate == 2 || firstDigitDate == 1)
				temp.append(rng.nextInt(10));
			else
				temp.append(rng.nextInt(9)+1);
			temp.append("-");
			temp.append("JUN-");
		}
		else if (month == 6)
		{
			int firstDigitDate = rng.nextInt(4);
			temp.append(firstDigitDate);
			if (firstDigitDate == 3)
				temp.append(rng.nextInt(2));
			else if (firstDigitDate == 2 || firstDigitDate == 1)
				temp.append(rng.nextInt(10));
			else
				temp.append(rng.nextInt(9)+1);
			temp.append("-");
			temp.append("JUL-");
		}
		else if (month == 7)
		{
			int firstDigitDate = rng.nextInt(4);
			temp.append(firstDigitDate);
			if (firstDigitDate == 3)
				temp.append(rng.nextInt(2));
			else if (firstDigitDate == 2 || firstDigitDate == 1)
				temp.append(rng.nextInt(10));
			else
				temp.append(rng.nextInt(9)+1);
			temp.append("-");
			temp.append("AUG-");
		}
		else if (month == 8)
		{
			int firstDigitDate = rng.nextInt(4);
			temp.append(firstDigitDate);
			if (firstDigitDate == 3)
				temp.append(0);
			else if (firstDigitDate == 2 || firstDigitDate == 1)
				temp.append(rng.nextInt(10));
			else
				temp.append(rng.nextInt(9)+1);
			temp.append("-");
			temp.append("SEP-");
		}
		else if (month == 9)
		{
			int firstDigitDate = rng.nextInt(4);
			temp.append(firstDigitDate);
			if (firstDigitDate == 3)
				temp.append(rng.nextInt(2));
			else if (firstDigitDate == 2 || firstDigitDate == 1)
				temp.append(rng.nextInt(10));
			else
				temp.append(rng.nextInt(9)+1);
			temp.append("-");
			temp.append("OCT-");
		}
		else if (month == 10)
		{
			int firstDigitDate = rng.nextInt(4);
			temp.append(firstDigitDate);
			if (firstDigitDate == 3)
				temp.append(0);
			else if (firstDigitDate == 2 || firstDigitDate == 1)
				temp.append(rng.nextInt(10));
			else
				temp.append(rng.nextInt(9)+1);
			temp.append("-");
			temp.append("NOV-");
		}
		else if (month == 11)
		{
			int firstDigitDate = rng.nextInt(4);
			temp.append(firstDigitDate);
			if (firstDigitDate == 3)
				temp.append(rng.nextInt(2));
			else if (firstDigitDate == 2 || firstDigitDate == 1)
				temp.append(rng.nextInt(10));
			else
				temp.append(rng.nextInt(9)+1);
			temp.append("-");
			temp.append("DEC-");
		}

		int year = rng.nextInt(2);
		if (year == 0)
			temp.append(14);
		else
			temp.append(15);

		return temp.toString();
	}

	/////////////////////////////////////////////////////
	//////////////////OTHER FUNCTIONS////////////////////	
	/////////////////////////////////////////////////////
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
			System.out.printf(format, rs.getLong(1), rs.getString(2),
					rs.getString(3), rs.getString(4), rs.getString(5),
					rs.getLong(6), rs.getLong(7), rs.getFloat(8));
		}

		ResultSet rs1 = stmt.executeQuery("select * from DistStation");
		ResultSetMetaData rsmd1 = rs1.getMetaData();
		System.out.println("");
		System.out.println("DistStation");
		System.out
				.println("--------------------------------------------------------------------------------------------------------------");
		String format2 = "%-7s%-7s%-20s%-20s%-12s%-7s%-10s%-12s%-12s%n";
		System.out.printf(format2, rsmd1.getColumnName(1),
				rsmd1.getColumnName(2), rsmd1.getColumnName(3),
				rsmd1.getColumnName(4), rsmd1.getColumnName(5),
				rsmd1.getColumnName(6), rsmd1.getColumnName(7),
				rsmd1.getColumnName(8), rsmd1.getColumnName(9));
		System.out
				.println("--------------------------------------------------------------------------------------------------------------");
		while (rs1.next()) {
			System.out.printf(format2, rs1.getLong(1), rs1.getLong(2),
					rs1.getString(3), rs1.getString(4), rs1.getString(5),
					rs1.getString(6), rs1.getLong(7), rs1.getLong(8),
					rs1.getFloat(9));
		}

		ResultSet rs2 = stmt.executeQuery("select * from Customers");
		ResultSetMetaData rsmd2 = rs.getMetaData();
		System.out.println("");
		System.out.println("Customers");
		System.out
				.println("--------------------------------------------------------------------------------------------------");
		String format3 = "%-7s%-7s%-10s%-14s%-14s%-14s%-12s%-12s%-7s%-12s%-16s%-16s%-12s%-12s%-17s%-17s%-17s%n";
		System.out.printf(format3, rsmd2.getColumnName(1),
				rsmd2.getColumnName(2), rsmd2.getColumnName(3),
				rsmd2.getColumnName(4), rsmd2.getColumnName(5),
				rsmd2.getColumnName(6), rsmd2.getColumnName(7),
				rsmd2.getColumnName(8), rsmd2.getColumnName(9),
				rsmd2.getColumnName(10), rsmd2.getColumnName(11),
				rsmd2.getColumnName(12), rsmd2.getColumnName(13),
				rsmd2.getColumnName(14), rsmd2.getColumnName(15),
				rsmd2.getColumnName(16), rsmd2.getColumnName(17));
		System.out
				.println("--------------------------------------------------------------------------------------------------");
		while (rs2.next()) {
			System.out.printf(format3, rs2.getLong(1), rs2.getLong(2),
					rs2.getLong(3), rs2.getString(4), rs2.getString(5),
					rs2.getString(6), rs2.getString(7), rs2.getString(8),
					rs2.getString(9), rs2.getLong(10), rs2.getString(11),
					rs2.getString(12), rs2.getLong(13), rs2.getFloat(14),
					rs2.getFloat(15), rs2.getLong(16), rs2.getLong(17));
		}
		ResultSet rs3 = stmt.executeQuery("select * from Orders");
			ResultSetMetaData rsmd3 = rs.getMetaData();
			System.out.println("");
			System.out.println("Orders");
			System.out
					.println("--------------------------------------------------------------------------------------------------");
			String format4 = "%-7s%-7s%-10s%-14s%-14s%-14s%-12s%n";
			System.out.printf(format4, rsmd3.getColumnName(1),
					rsmd3.getColumnName(2), rsmd3.getColumnName(3),
					rsmd3.getColumnName(4), rsmd3.getColumnName(5),
					rsmd3.getColumnName(6), rsmd3.getColumnName(7));
			System.out
					.println("--------------------------------------------------------------------------------------------------");
			while (rs3.next()) {
				System.out.printf(format4, rs3.getLong(1), rs3.getLong(2),
						rs3.getLong(3), rs3.getLong(4), rs3.getString(5),
						rs3.getString(6), rs3.getLong(7));
			}
	}
	//helper function to print stuff
	public static void printstuff() throws SQLException {
		String completed = "Incomplete";
		Statement st = connection.createStatement();
		st.executeUpdate("SET TRANSACTION READ WRITE");

		//String selectQuery = ("SELECT * FROM LINEITEMS WHERE DS_ID = 1 AND WH_ID = 0 AND CUST_ID = 9");
		//String selectQuery = ("SELECT NUM_DELIVERIES, NUM_PAYMENTS, debt, YTD_PURCHASE_TOTAL FROM Customers WHERE WH_ID = 0 AND DS_ID = 1 AND CUST_ID = 1");
		//String selectQuery = ("SELECT * FROM STOCK WHERE WH_ID = 0");

		String selectQuery = ("SELECT DEBT FROM CUSTOMERs WHERE DEBT = 0");
		PreparedStatement ps = connection.prepareStatement(selectQuery);		
		ResultSet resultSet = ps.executeQuery();
		ResultSetMetaData rsmd = resultSet.getMetaData();
		int columnsNumber = rsmd.getColumnCount();
		while (resultSet.next()) {
		    for (int i = 1; i < columnsNumber+1; i++) {
		        if (i > 1) System.out.print(",       ");
		        String columnValue = resultSet.getString(i);
		        System.out.print(rsmd.getColumnName(i) + " " + columnValue);
		    }
		    System.out.println("");
		}
		ps.close();
		ps = null;
		st.executeUpdate("COMMIT");
		st.close();
		st = null;
	}
}
