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
import java.util.Scanner;
import java.util.Random;
public class GroceryDeliveryDB {
    private static Connection connection;
    private Statement statement;
    private PreparedStatement preparedStatement;
    private ResultSet resultSet;
    private String query;
   	public static void main(String[] args) throws SQLException, ClassNotFoundException {
        Scanner reader = new Scanner(System.in);  // Reading from System.in
       //Should we prompt user instead of hard coding this??
        System.out.print("Please enter your DB username: ");
        String username = reader.nextLine();
        System.out.print("Please enter your DB password: ");
        String password = reader.nextLine();
        DriverManager.registerDriver(new oracle.jdbc.OracleDriver());
        //Class.forName("oracle.jdbc.OracleDriver");
		
        String url = "jdbc:oracle:thin:@class3.cs.pitt.edu:1521:dbclass";
        
        connection = DriverManager.getConnection(url, username, password);
        System.out.println("Connection to Oracle established");
        
        while(true) {
            System.out.println("------------------------------------------------------\nWelcome to the Grocery Delivery System. Select how you \nwould like to interact with the system:\n------------------------------------------------------");
            System.out.print("1 - \tGenerate/Reset Data\n2 - \tPlace an order:\n3 - \tView databases:\n4 - \tblah:\n5 - \tExit Application:\nEnter a number: ");
            int n = reader.nextInt();
            switch(n) {
                case 1:
                    GenData();
                    break;
                case 2:
                    System.out.println("Not Implemented");
                    break;
                case 3:
                    viewDB();
                    break;
                case 4:
                    System.out.println("Not Implemented");
                    break;
                case 5:
                	connection.close();
                	System.out.println("Connection closed.");
                    System.exit(0);
                    break;   
            }
        }
    }
    public static void GenData() { 
        //create tables from .sql file
		try {
            System.out.println("Creating the database...");
			resetDatabase();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
        //gen the data for the db
		try{
            System.out.println("Generating data for the database...");
            GenItemData(50);//500 unique grocery items
	        GenWarehouseData(10); //we chose 20 warehouses as it is a logical number of warehouses
	        GenDistStationData(10, 10);//20 warehouses, 10 DS per HW
	        GenCustomerData(10, 10, 10);//20 warehouses, 10 DistStations per HW, 15 customers per DS
	        GenOrderData(10, 10, 10, 5);//20 warehouses, 10 DistStations per HW, 15 customers per DS, 5 orders per cust
            GenStockData(10, 50);	
            GenLineItemData(10, 10, 10, 5, 3);//20 warehouses, 10 DistStations per HW, 15 customers per DS, 5 orders per cust, 3 line items per cust

		}
		catch (SQLException e){
			e.printStackTrace();
		}
    }
    public static void GenWarehouseData(int numWH) throws SQLException{
        Random rand = new Random();
        int wh_ID;
        int zipcode;
        int taxrate; //this is sales tax, I changed it to an int, doesn't need to be a double
        float ytdSalesSum; //Making this an int for now as well, we don't need to be that specific yet
        for(int x = 0; x < numWH; x++) {
            //generate the data. I picked 7 as the length for the strings for no reason.
            wh_ID = x;
            String wh_Name = generateString(7);
            String street_Address = alphaNumString();
            String city = generateString(7);
            String state = generateString(2);
            state = state.toUpperCase();
            zipcode = rand.nextInt(90000) + 10000; //generates a zipcode from 10000-99999
            taxrate = rand.nextInt(8) + 1; //generates a taxrate from 1% to 8%
            int dollars = rand.nextInt(90000) + 1000;
            int cents = rand.nextInt(90)+10;
            String ytdSalesString = dollars + "." + cents;  
            ytdSalesSum = Float.parseFloat(ytdSalesString); //We should build a method to just give an integer plus decimal places. This only does values 0.0-1.0  FIXED 11/17 see above
            //do a sql insert here using the JDBC
            Statement st = connection.createStatement();
            String insQuery = ("INSERT INTO Warehouse VALUES (?,?,?,?,?,?,?,?)");
            PreparedStatement ps = connection.prepareStatement(insQuery);
            ps.setLong(1,wh_ID);
            ps.setString(2, wh_Name);
            ps.setString(3, street_Address);
            ps.setString(4, city);
            ps.setString(5, state);
            ps.setLong(6, zipcode);
            ps.setLong(7, taxrate);
            ps.setFloat(8, ytdSalesSum);
            ps.executeUpdate();
            ps.close();
            //System.out.println("WH_ID: "+ wh_ID+ "\nWarehouse_Name: "+ wh_Name+ "\nStreet_Address: "+ street_Address+ "\nCity: "+ city+ "\nState: "+ state + "\nZipcode: "+ zipcode + "\nTax Rate:" + taxrate+ "\nYTD_Sales_Sum: " +ytdSalesSum);
        }
    }
    public static void GenDistStationData(int numWH, int numDS) throws SQLException{
    	Random rand = new Random();
    	int wh_ID;
    	int ds_ID;
    	int zipcode;
    	int taxrate;
    	float ytdSalesSum;
    	for(int x = 0; x < numWH; x++) {
            for(int y = 0; y < numDS; y++) {
                //generate the data
                wh_ID = x; //In dealing with PK's and FK's, I assume we'll need a way to sync these for each table i.e. wh_ID's should match up
                ds_ID = y; //^^same as above, what should these really be?
                String ds_Name = generateString(7);
                String street_Address = alphaNumString();
                String city = generateString(7);
                String state = generateString(2);
                state = state.toUpperCase();
                zipcode = rand.nextInt(90000) + 10000; //generates a zipcode from 10000-99999
                taxrate = rand.nextInt(8) + 1; //generates a taxrate from 1% to 8%
                int dollars = rand.nextInt(90000) + 1000;
                int cents = rand.nextInt(90)+10;
                String ytdSalesString = dollars + "." + cents;  
                ytdSalesSum = Float.parseFloat(ytdSalesString);
                //do a sql insert here using the JDBC
                Statement st = connection.createStatement();
                String insQuery = ("INSERT INTO DistStation VALUES (?,?,?,?,?,?,?,?,?)");
                PreparedStatement ps = connection.prepareStatement(insQuery);
                ps.setLong(1,wh_ID);
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
            }
        }
    }
    public static void GenCustomerData(int numWH, int numDS, int numCust) throws SQLException{
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
        for(int x = 0; x < numWH; x++) {
            for(int y = 0; y < numDS; y++) {
                for(int z = 0; z < numCust; z++) {
                    //generate the data
                    wh_ID = x;
                    ds_ID = y;
                    cust_ID = z;
                    String first_Name = generateString(8); //could make first letter capital at some point
                    String middle_Init = generateString(1);
                    String last_Name = generateString(10);
                    String street_Address = alphaNumString();
                    String city = generateString(7);
                    String state = generateString(2);
                    state = state.toUpperCase();
                    zipcode = rand.nextInt(90000) + 10000; //generates a zipcode from 10000-99999
                    int num1 = rand.nextInt(900)+100;
                    int num2 = rand.nextInt(900)+100;
                    int num3 = rand.nextInt(9000)+100;
                    String phone_Num = num1 +"-"+ num2+"-"+num3;
                    String signup_Date = generateDate();
                    active_discount = rand.nextInt(20);
                    debt = rand.nextInt(40000); //arbitrary
                    ytdPurchaseTotal = rand.nextInt(200000);
                    num_payments = rand.nextInt(100);
                    num_deliveries = rand.nextInt(100);
                    //do a sql insert here using the JDBC
                    Statement st = connection.createStatement();
                    String insQuery = ("INSERT INTO Customers VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)");
                    PreparedStatement ps = connection.prepareStatement(insQuery);
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
                }
            }
        }
    }
    public static void GenOrderData(int numWH, int numDS, int numCust, int numOrders) throws SQLException{
    	Random rand = new Random();
       	int wh_ID;
        int ds_ID;
        int cust_ID;
    	int order_ID;
    	int num_Items;
        for(int w = 0; w < numWH; w++) {
            for(int x = 0; x < numDS; x++) {
                for(int y = 0; y < numCust; y++) {
                    for(int z = 0; z < numOrders; z++) {            
                        wh_ID = w;
                        ds_ID = x;
                        cust_ID = y;
                        order_ID = z;
                        String date_Placed = generateDate();
                        String completed_Flag = "Completed";
                        num_Items = rand.nextInt(80);
                        //do a sql insert here using the JDBC
                        Statement st= connection.createStatement();
                        String insQuery = ("INSERT INTO Orders VALUES (?,?,?,?,?,?,?)");
                        PreparedStatement ps = connection.prepareStatement(insQuery);
                        ps.setLong(1, wh_ID);
                        ps.setLong(2, ds_ID);
                        ps.setLong(3, cust_ID);
                        ps.setLong(4, order_ID);
                        ps.setString(5, date_Placed);
                        ps.setString(6, completed_Flag);
                        ps.setLong(7, num_Items);
                        ps.executeUpdate();
                        ps.close();
                    }
                }
            }
        }
    }
    public static void GenLineItemData(int numWH, int numDS, int numCust, int numOrders, int numLIs) throws SQLException{
    	Random rand = new Random();
        int wh_ID;
        int ds_ID;
        int cust_ID;
        int order_ID;
    	int li_ID;
    	int item_ID;
    	int quantity;
    	int total_Cost;
        for(int w = 0; w < numWH; w++) {
            for(int x = 0; x < numDS; x++) {
                for(int y = 0; y < numCust; y++) {
                    for(int z = 0; z < numOrders; z++) {
                        for(int a = 0; a < numLIs; a++) {
                            //generate the data
                            wh_ID = w;
                            ds_ID = x;
                            cust_ID = y;
                            order_ID = z;
                            li_ID = a;
                            item_ID = rand.nextInt(30);
                            quantity = rand.nextInt(3000);
                            total_Cost = rand.nextInt(2000);
                            String date_Delivered = generateDate();
                            //do a sql insert here using the JDBC
                            Statement st= connection.createStatement();
                            String insQuery = ("INSERT INTO LineItems VALUES (?,?,?,?,?,?,?,?,?)");
                            PreparedStatement ps = connection.prepareStatement(insQuery);
                            ps.setLong(1, wh_ID);
                            ps.setLong(2, ds_ID);
                            ps.setLong(3, cust_ID);
                            ps.setLong(4, order_ID);
                            ps.setLong(5, li_ID);
                            ps.setLong(6, item_ID);
                            ps.setLong(7, quantity);
                            ps.setLong(8, total_Cost);
                            ps.setString(9, date_Delivered);
                            ps.executeUpdate();
                            ps.close();
                        }
                    }
                }
            }
        }
    }
    public static void GenItemData(int numItems) throws SQLException{
    	Random rand = new Random();
        int item_ID;
        float price; //Also needs a function to generate dollars and cents
        for(int x = 0; x < numItems; x++) {
            //generate the data
            item_ID = x;
            String item_Name = generateString(15);
            int dollars = rand.nextInt(90000) + 1000;
            int cents = rand.nextInt(90)+10;
            String priceString = dollars + "." + cents;
            price = Float.parseFloat(priceString);
            //do a sql insert here using the JDBC
            Statement st= connection.createStatement();
            String insQuery = ("INSERT INTO Items VALUES (?,?,?)");
            PreparedStatement ps = connection.prepareStatement(insQuery);
            ps.setLong(1, item_ID);
            ps.setString(2, item_Name);
            ps.setFloat(3, price);
            ps.executeUpdate();
            ps.close();
        }
    }
    public static void GenStockData(int numWH, int numItems) throws SQLException{
    	Random rand = new Random();
        int wh_ID;
        int item_ID;
        int quantity_avail;
        int quantity_sold;
        int num_orders;
        for(int x = 0; x < numWH; x++) {
            for(int y = 0; y < numItems; y++) {
                //generate the data
                wh_ID = x;
                item_ID = x;
                quantity_avail = rand.nextInt(5000);
                quantity_sold = rand.nextInt(5000);
                num_orders = rand.nextInt(20000);
                //do a sql insert here using the JDBC
                Statement st= connection.createStatement();
                String insQuery = ("INSERT INTO Stock VALUES (?,?,?,?,?)");
                PreparedStatement ps = connection.prepareStatement(insQuery);
                ps.setLong(1, wh_ID);
                ps.setLong(2, item_ID);
                ps.setLong(3, quantity_avail);
                ps.setLong(4, quantity_sold);
                ps.setLong(5, num_orders);
                ps.executeUpdate();
                ps.close();
            }
        }
    }
    //This will generate a random string of characters
    public static String generateString(int length)
	{
		String characters = "abcdefghijklmnopqrstuvwxyz";
		Random rng = new Random();
    		char[] text = new char[length];
    		for (int i = 0; i < length; i++)
    		{
        		text[i] = characters.charAt(rng.nextInt(characters.length()));
    		}
    		return new String(text);
	}
	//This will generate alpha-numeric string for address
	//Double check if that's how to append a space
	public static String alphaNumString()
	{
		StringBuilder temp = new StringBuilder();
		Random rng = new Random();
		temp.append(rng.nextInt(999));
		temp.append(" ");
		temp.append(generateString(7));
		if (rng.nextInt(3) == 0)
		{
			temp.append(" Rd.");
		}
		else if (rng.nextInt(3) == 1)
		{
			temp.append(" St.");
		}
		else
			temp.append(" Ave.");
			
		return temp.toString();
	}
	//This will generate random dates
	public static String generateDate()
	{
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

    public static void viewDB() throws SQLException
    {
        Statement stmt = connection.createStatement();
        ResultSet rs = stmt.executeQuery("select * from Warehouse");
        ResultSetMetaData rsmd = rs.getMetaData();
        System.out.println("Warehouse");
        System.out.println("--------------------------------------------------------------------------------------------------");
        String format = "%-7s%-16s%-20s%-12s%-7s%-10s%-12s%-17s%n";
        System.out.printf(format, rsmd.getColumnName(1), rsmd.getColumnName(2), rsmd.getColumnName(3), rsmd.getColumnName(4), rsmd.getColumnName(5), rsmd.getColumnName(6), rsmd.getColumnName(7), rsmd.getColumnName(8));
        System.out.println("--------------------------------------------------------------------------------------------------");
        while(rs.next()){
            String format2 = "%-7s%-16s%-20s%-12s%-7s%-10s%-12s%-17s%n";
            System.out.printf(format2, rs.getLong(1), rs.getString(2), rs.getString(3), rs.getString(4), rs.getString(5), rs.getLong(6), rs.getLong(7), rs.getFloat(8));
        }
    }
      
	public static void resetDatabase() throws SQLException {
		System.out.println("Reseting the database...");
		String s = new String();
		StringBuffer sb = new StringBuffer();
		
		try {
			FileReader f = new FileReader(new File("GroceryDB.sql"));
			
			BufferedReader br = new BufferedReader(f);
			
			while((s=br.readLine())!= null) {
				sb.append(s);	//build the sql statement
			}
			br.close();
			
			
			//split each request
			String[] rqts = sb.toString().split(";");
			
			if(connection != null) {
				Statement st = connection.createStatement();
				
				for (int i=0; i<rqts.length; i++) {
					if(!rqts[i].trim().equals("")) {
						st.execute(rqts[i]);
						//System.out.println(">>"+rqts[i]);
					}
				}
			}
		}
		catch(Exception e) {
			System.out.println("Error: "+ e.toString());
			System.out.println("Error: ");
			e.printStackTrace();
			System.out.println(sb.toString());
		}
	
	}
}
