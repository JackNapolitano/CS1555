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
 * $> javac GroceryDBDriver.java
 **/
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class GroceryDBThread extends Thread {
	private static int numThreads = 15; // 3 threads per tx
	// this must be changed manually here if you would like to test with a
	// shared connection
	static boolean share_connection = false;
	static int c_nextId = 1;
	static Connection s_conn = null;
	static String username = "";
	static String password = "";

	int threadID;
	int txID; // transactions IDed in same order as on M2 assignment pdf

	synchronized static int getNextId() {
		return c_nextId++;
	}

	public static void main(String args[]) {
		try {
			if (args.length < 2) {
				System.out
						.println("usage $> java GroceryDBDriver <db_username> <db_password>");
				System.exit(0);
			}
			username = args[0];
			password = args[1];

			if (share_connection) {
				try {
					DriverManager
							.registerDriver(new oracle.jdbc.OracleDriver());
					// Class.forName("oracle.jdbc.OracleDriver");

					String url = "jdbc:oracle:thin:@class3.cs.pitt.edu:1521:dbclass";

					s_conn = DriverManager.getConnection(url, username,
							password);
					// System.out.println("Connection to Oracle established");
				} catch (Exception e) {
					e.printStackTrace();
				}
			}

			Thread[] threadList = new Thread[numThreads];

			int tx_count = 0;
			for (int i = 0; i < numThreads; i++) {
				threadList[i] = new GroceryDBThread(tx_count);
				threadList[i].start();
				if (tx_count == 4)
					tx_count = 0;
				else
					tx_count++;
			}

			// start all at same time
			setStart();

			// wait for all threads to end
			for (int i = 0; i < numThreads; i++) {
				threadList[i].join();
			}

			if (share_connection) {
				s_conn.close();
				s_conn = null;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void run() {
		try {
			Connection connection = null;

			if (share_connection)
				connection = s_conn;
			else {
				try {
					DriverManager
							.registerDriver(new oracle.jdbc.OracleDriver());
					// Class.forName("oracle.jdbc.OracleDriver");

					String url = "jdbc:oracle:thin:@class3.cs.pitt.edu:1521:dbclass";

					connection = DriverManager.getConnection(url, username,
							password);
					// System.out.println("Connection to Oracle established");
				} catch (Exception e) {
					e.printStackTrace();
				}
			}

			while (!getStart()) {
				yield();
			}

			int wh_id = 1;
			int ds_id = 0;
			int custID = 0;
			String itemIDs = "1,2,4,6,5,23";
			String itemQuantities = "1,23,45,74,3,43";
			int totalItems = 189;
			double payAmt = 2000;
			int stockThreshold = 150;

			if (connection != null) {
				// execute the tx for that thread
				switch (this.txID) {
				case 0:
					newOrder(connection, wh_id, ds_id, custID, itemIDs,
							itemQuantities, totalItems);
					break;
				case 1:
					makePayment(connection, wh_id, ds_id, custID, payAmt);
					break;
				case 2:
					checkStockLevels(connection, wh_id, ds_id, stockThreshold);
					break;
				case 3:
					deliverItems(connection, wh_id);
					break;
				case 4:
					checkOrderStatus(connection, ds_id, custID);
					break;
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	static boolean start = false;

	static synchronized void setStart() {
		start = true;
	};

	synchronized boolean getStart() {
		return start;
	};

	public GroceryDBThread(int tx) {
		super();
		threadID = getNextId();
		txID = tx;
	}

	public static void newOrder(Connection connection, int wh_id, int ds_ID,
			int custID, String itemIDs, String itemQuantities, int totalItems)
			throws SQLException {
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

		// ///////////////////////////////
		// /START OF ACTUAL TRANSACTION///
		// ///////////////////////////////
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
					PreparedStatement insLI = connection
							.prepareStatement(liInsert);
					insLI.setLong(1, wh_id);
					insLI.setLong(2, ds_ID);
					insLI.setLong(3, custID);
					insLI.setLong(4, newOrderID);
					insLI.setLong(5, i);
					insLI.setLong(6, iIds.get(i));
					insLI.setLong(7, iQuantities.get(i));
					// calculate total cost
					String priceQuery = "SELECT PRICE FROM ITEMS WHERE ITEM_ID =?";
					PreparedStatement pricePS = connection
							.prepareStatement(priceQuery);
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
					// DATA CONSISTANCY CUSTOMER DEBT
					String updateCustomerDebt = "UPDATE CUSTOMERS set debt = debt + ? WHERE WH_ID = ? AND DS_ID = ? AND CUST_ID = ?";
					PreparedStatement updateCust = connection
							.prepareStatement(updateCustomerDebt);
					updateCust.setDouble(1, totalPrice);
					updateCust.setLong(2, wh_id);
					updateCust.setLong(3, ds_ID);
					updateCust.setLong(4, custID);
					updateCust.executeUpdate();
					updateCust.close();
					updateCust = null;
					// DATA CONSISTANCY STOCK
					String updateStock = "UPDATE STOCK set NUM_ORDERS = NUM_ORDERS + 1, QUANTITY_AVAILABLE = QUANTITY_AVAILABLE -  ?, quantity_sold = quantity_sold + ? WHERE ITEM_ID = ?";
					PreparedStatement updateStockPS = connection
							.prepareStatement(updateStock);
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

	// TX2
	public static void makePayment(Connection connection, int wh_id, int ds_id,
			int custID, double payAmt) throws SQLException {
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
			while (ytdRS.next()) {
				float ytdSalesSum = ytdRS.getFloat(1);

				if (payAmt >= debtAmt) {
					double extra = payAmt - debtAmt;
					String updateQuery = ("UPDATE CUSTOMERS SET NUM_PAYMENTS = NUM_PAYMENTS + 1, debt = ?, YTD_PURCHASE_TOTAL = YTD_PURCHASE_TOTAL + ? WHERE CUST_ID=? AND DS_ID=?");
					PreparedStatement prep = connection
							.prepareStatement(updateQuery);
					prep.setLong(1, 0);
					prep.setDouble(2, debtAmt);
					prep.setInt(3, custID);
					prep.setInt(4, ds_id);
					prep.executeUpdate();
					prep.close();
					prep = null;
					System.out
							.println("A payment of $"
									+ debtAmt
									+ " has been applied to your account and $"
									+ extra
									+ " has been refunded to you. Your current debt is $0.");
					ytdSalesSum = ytdSalesSum + (float) debtAmt;

					// DATA CONSISTANCY FOR WAREHOUSE AND DS YTD SALES SUM
					String updateYTD = ("UPDATE DISTSTATION SET YTD_SALES_SUM = ? WHERE WH_ID=? AND DS_ID=?");
					PreparedStatement ytdp = connection
							.prepareStatement(updateYTD);
					ytdp.setFloat(1, ytdSalesSum);
					ytdp.setLong(2, wh_id);
					ytdp.setLong(3, ds_id);
					ytdp.executeUpdate();
					ytdp.close();
					ytdp = null;
					String updateYTD2 = ("UPDATE WAREHOUSE SET YTD_SALES_SUM = YTD_SALES_SUM + ? WHERE WH_ID=?");
					PreparedStatement ytdps = connection
							.prepareStatement(updateYTD2);
					ytdps.setFloat(1, (float) debtAmt);
					ytdps.setLong(2, wh_id);
					ytdps.executeUpdate();
					ytdps.close();
					ytdps = null;
				} else {
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
					System.out
							.println("A payment of $"
									+ payAmt
									+ " has been applied to your account and your current debt is $"
									+ newDebt + ".");
					ytdSalesSum = ytdSalesSum + (float) payAmt;
					// DATA CONSISTANCY FOR WAREHOUSE AND DS YTD SALES SUM
					String updateYTD = ("UPDATE DISTSTATION SET YTD_SALES_SUM = ? WHERE WH_ID=? AND DS_ID=?");
					PreparedStatement ytdp = connection
							.prepareStatement(updateYTD);
					ytdp.setFloat(1, ytdSalesSum);
					ytdp.setLong(2, wh_id);
					ytdp.setLong(3, ds_id);
					ytdp.executeUpdate();
					ytdp.close();
					ytdp = null;
					String updateYTD2 = ("UPDATE WAREHOUSE SET YTD_SALES_SUM = YTD_SALES_SUM + ? WHERE WH_ID=?");
					PreparedStatement ytdps = connection
							.prepareStatement(updateYTD2);
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

	// question is very ambiguous and either way the metric is not very useful
	// due to the ambiguity of the returned result
	// TX3
	public static void checkStockLevels(Connection connection, int wh_id,
			int ds_id, int stockThreshold) throws SQLException {
		Statement st = connection.createStatement();
		st.executeQuery("SET TRANSACTION READ WRITE");
		// return the number of unique items sold recently that are below stock
		// threshold
		String top20 = "SELECT COUNT(QUANTITY_AVAILABLE) FROM (SELECT QUANTITY_AVAILABLE FROM STOCK NATURAL JOIN (SELECT ITEM_ID FROM LINEITEMS NATURAL JOIN (SELECT ORDER_ID FROM (SELECT * FROM ORDERS WHERE DS_ID = ? AND WH_ID =? ORDER BY DATE_PLACED DESC) WHERE ROWNUM <=20))) WHERE QUANTITY_AVAILABLE < ?";
		PreparedStatement top20Orders = connection.prepareStatement(top20);
		top20Orders.setLong(1, ds_id);
		top20Orders.setLong(2, wh_id);
		top20Orders.setLong(3, stockThreshold);
		ResultSet rs = top20Orders.executeQuery();
		while (rs.next()) {
			System.out.println("There are " + rs.getLong(1)
					+ " items in warehouse " + wh_id + " have a stock below "
					+ stockThreshold + ".");
		}

		top20Orders.close();
		top20Orders = null;

		// return the sum of quantities purchased recently for items below stock
		// threshold
		String pQuantities = "SELECT QUANTITY FROM STOCK NATURAL JOIN (SELECT ITEM_ID, QUANTITY FROM LINEITEMS NATURAL JOIN (SELECT ORDER_ID FROM (SELECT * FROM ORDERS WHERE DS_ID = ? AND WH_ID =? ORDER BY DATE_PLACED DESC) WHERE ROWNUM <=20)) WHERE QUANTITY_AVAILABLE < ?";
		PreparedStatement qTop20 = connection.prepareStatement(pQuantities);
		int sum = 0;
		qTop20.setLong(1, ds_id);
		qTop20.setLong(2, wh_id);
		qTop20.setLong(3, stockThreshold);
		ResultSet qRS = qTop20.executeQuery();
		while (qRS.next()) {
			sum = sum + qRS.getInt(1);
		}

		qRS.close();
		qRS = null;
		System.out
				.println(sum
						+ " items were purchase recently that are below the stock threshold, "
						+ stockThreshold + " in warehouse " + wh_id + ".");

		st.executeQuery("COMMIT");
		st.close();
		st = null;
	}

	// TX4
	public static void deliverItems(Connection connection, int wh_id)
			throws SQLException {
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
		while (resultSet.next()) {
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
			// updates lineitems with the date delivered
			String updateLineItemsQuery = ("UPDATE LINEITEMS SET DATE_DELIVERED = ? WHERE CUST_ID = ? AND DS_ID = ? AND ORDER_ID = ? AND WH_ID = ?");
			PreparedStatement updateLineItems = connection
					.prepareStatement(updateLineItemsQuery);
			java.sql.Date dateDeliv = new java.sql.Date(
					(new java.util.Date()).getTime());
			updateLineItems.setDate(1, dateDeliv);
			updateLineItems.setLong(2, custID);
			updateLineItems.setLong(3, ds_ID);
			updateLineItems.setLong(4, order_ID);
			updateLineItems.setLong(5, wh_id);
			updateLineItems.executeUpdate();
			updateLineItems.close();
			updateLineItems = null;

			// get debt for later use
			String selectQuery3 = ("SELECT DEBT FROM CUSTOMERS WHERE WH_ID = ? AND DS_ID = ? AND CUST_ID = ?");
			PreparedStatement ps3 = connection.prepareStatement(selectQuery3);
			ps3.setLong(1, wh_id);
			ps3.setLong(2, ds_ID);
			ps3.setLong(3, custID);
			ResultSet resultSet3 = ps3.executeQuery();
			resultSet3.next();
			double currentDebt = resultSet3.getDouble(1);

			// CUSTOMER DATA CONSISTANCY
			String updateCustomerQuery = ("UPDATE CUSTOMERS SET DEBT = 0, YTD_PURCHASE_TOTAL = YTD_PURCHASE_TOTAL + DEBT, NUM_PAYMENTS = NUM_PAYMENTS + 1, NUM_DELIVERIES = NUM_DELIVERIES + 1  WHERE CUST_ID = ? AND DS_ID = ? AND WH_ID = ?");
			PreparedStatement updateCustomers = connection
					.prepareStatement(updateCustomerQuery);
			updateCustomers.setLong(1, custID);
			updateCustomers.setLong(2, ds_ID);
			updateCustomers.setLong(3, wh_id);
			updateCustomers.executeUpdate();
			updateCustomers.close();
			updateCustomers = null;

			// Changes orders from incomplete to completed
			String updateOrdersQuery = ("UPDATE ORDERS SET COMPLETED_FLAG=? WHERE CUST_ID=? AND DS_ID=? AND ORDER_ID = ?  AND WH_ID = ?");
			PreparedStatement updateOrders = connection
					.prepareStatement(updateOrdersQuery);
			updateOrders.setString(1, completed);
			updateOrders.setLong(2, custID);
			updateOrders.setLong(3, ds_ID);
			updateOrders.setLong(4, order_ID);
			updateOrders.setLong(5, wh_id);
			updateOrders.executeUpdate();
			updateOrders.close();
			updateOrders = null;

			// DATA CONSISTANCY FOR WAREHOUSE AND DS YTD SALES SUM
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

	// TX5
	public static void checkOrderStatus(Connection connection, int ds_id,
			int custID) throws SQLException {
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
		System.out.print("Customer " + custID + " from Distribution Station "
				+ ds_id + "'s last order contained:\n");
		while (resultSet.next()) {
			for (int i = 1; i <= columnsNumber; i++) {
				if (i > 1)
					System.out.print(",       ");
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

}