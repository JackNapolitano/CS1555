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
       
       //Should we prompt user instead of hard coding this??
        String username = "jon18";
        String password = "3676118";
        DriverManager.registerDriver(new oracle.jdbc.OracleDriver());
        //Class.forName("oracle.jdbc.OracleDriver");
		
        String url = "jdbc:oracle:thin:@class3.cs.pitt.edu:1521:dbclass";
        
        connection = DriverManager.getConnection(url, username, password);
        System.out.println("Connection to Oracle established");
        
        while(true) {
            System.out.println("------------------------------------------------------\nWelcome to the Grocery Delivery System. Select how you \nwould like to interact with the system:\n------------------------------------------------------");
            Scanner reader = new Scanner(System.in);  // Reading from System.in
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
   		System.out.println("Generating data for the database...");
        int numDataToGen = 15; //generate 1000 items of data
        
        //create tables from .sql file
		try {
			resetDatabase();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
        //gen the data for the db
		try{
	        GenWarehouseData(numDataToGen);
	        GenDistStationData(numDataToGen);
	        GenCustomerData(numDataToGen);
	        GenOrderData(numDataToGen);
	        GenLineItemData(numDataToGen);
	        GenItemData(numDataToGen);
	        GenStockData(numDataToGen);	
		}
		catch (SQLException e){
			e.printStackTrace();
		}
    }
    public static void GenWarehouseData(int numDataToGen) throws SQLException{
    //Should I initialize Strings with "" and then concat instead of assign?
        Random rand = new Random();
        int wh_ID;
        int zipcode;
        int taxrate; //this is sales tax, I changed it to an int, doesn't need to be a double
        float ytdSalesSum; //Making this an int for now as well, we don't need to be that specific yet
        for(int x = 0; x < numDataToGen; x++) {
            //generate the data. I picked 7 as the length for the strings for no reason.
            wh_ID = x+1;
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
            System.out.println("");
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
            //System.out.println("WH_ID: "+ wh_ID+ "\nWarehouse_Name: "+ wh_Name+ "\nStreet_Address: "+ street_Address+ "\nCity: "+ city+ "\nState: "+ state + "\nZipcode: "+ zipcode + "\nTax Rate:" + taxrate+ "\nYTD_Sales_Sum: " +ytdSalesSum);
        }
    }
    public static void GenDistStationData(int numDataToGen) throws SQLException{
    	Random rand = new Random();
    	int wh_ID;
    	int ds_ID;
    	int zipcode;
    	int taxrate;
    	float ytdSalesSum;
    	for(int x = 0; x < numDataToGen; x++) {
            //generate the data
            wh_ID = x+1; //In dealing with PK's and FK's, I assume we'll need a way to sync these for each table i.e. wh_ID's should match up
            ds_ID = x+2; //^^same as above, what should these really be?
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
            System.out.println("");
            String insQuery = ("INSERT INTO DistStation VALUES (?,?,?,?,?,?,?,?,?)");
            PreparedStatement ps = connection.prepareStatement(insQuery);
            ps.setLong(1,wh_ID);
            ps.setString(2, ds_ID);
            ps.setString(3, ds_Name);
            ps.setString(4, street_Address);
            ps.setString(5, city);
            ps.setString(6, state);
            ps.setLong(7, zipcode);
            ps.setLong(8, taxrate);
            ps.setFloat(9, ytdSalesSum);
            ps.executeUpdate();
        }
    }
    public static void GenCustomerData(int numDataToGen) throws SQLException{
    	Random rand = new Random();
        int wh_ID;
    	int ds_ID;
    	int cust_ID;
    	String first_Name = "";
    	String middle_Init = "";
    	String last_Name = "";
    	String street_Address = "";
    	String city = "";
    	String state = "";
    	int zipcode;
    	String phone_Num = "";
    	String signup_Date = "";
    	int active_discount;
    	int debt;
    	int ytdPurchaseTotal;
    	int num_payments;
    	int num_deliveries;
        for(int x = 0; x < numDataToGen; x++) {
            //generate the data
            wh_ID = x+1;
            ds_ID = x+2;
            cust_ID = rand.nextInt(1000);
            first_Name += generateString(8); //could make first letter capital at some point
            middle_Init += generateString(1);
            last_Name += generateString(10);
            street_Address += alphaNumString();
            city += generateString(7);
            state += generateString(2);
            state.toUpperCase();
            zipcode = rand.nextInt(90000) + 10000; //generates a zipcode from 10000-99999
            int num1 = rand.nextInt(900)+100;
			int num2 = rand.nextInt(900)+100;
			int num3 = rand.nextInt(9000)+100;
			phone_Num = num1 +"-"+ num2+"-"+num3;
			//int is not large enough to hold a number that large
			//phone_Num = rand.nextInt(900000000) + 100000000; //generates a phone number from 1000000000-9999999999
            signup_Date += generateDate();
            active_discount = rand.nextInt(20);
            debt = rand.nextInt(40000); //arbitrary
            ytdPurchaseTotal = rand.nextInt(200000);
            num_payments = rand.nextInt(100);
            num_deliveries = rand.nextInt(100);
            //do a sql insert here using the JDBC
            Statement st = connection.createStatement();
            st.executeUpdate("INSERT INTO Customers VALUES (wh_ID, ds_ID, cust_ID, first_Name, middle_Init, last_name, street_Address, city, state, zipcode, phone_Num, signup_Date, active_discount, debt, ytdPurchaseTotal, num_payments, num_deliveries)");
        }
    }
    public static void GenOrderData(int numDataToGen) throws SQLException{
    	Random rand = new Random();
       	int ds_ID;
    	int cust_ID;
    	int order_ID;
    	String date_Placed = "";
    	String completed_Flag = "";
    	int num_Items;
       	for(int x = 0; x < numDataToGen; x++) {
            //generate the data
            ds_ID = x+2; //arbitrary
            cust_ID = rand.nextInt(1000);
            order_ID = rand.nextInt(50000);
            date_Placed += generateDate();
            completed_Flag += "Completed";
            num_Items = rand.nextInt(80);
            //do a sql insert here using the JDBC
            Statement st= connection.createStatement();
            st.executeUpdate("INSERT INTO Orders VALUES (ds_ID, cust_ID, order_ID, date_placed, completed_Flag, num_Items)");
        }
    }
    public static void GenLineItemData(int numDataToGen) throws SQLException{
    	Random rand = new Random();
        int cust_ID;
    	int order_ID;
    	int li_ID;
    	int item_ID;
    	int quantity;
    	int total_Cost;
    	String date_Delivered = "";
        for(int x = 0; x < numDataToGen; x++) {
            //generate the data
            cust_ID = rand.nextInt(1000);
            order_ID = rand.nextInt(50000);
            li_ID = rand.nextInt(50000);
            item_ID = rand.nextInt(100000);
            quantity = rand.nextInt(3000);
            total_Cost = rand.nextInt(2000);
            date_Delivered += generateDate();
            //do a sql insert here using the JDBC
            Statement st= connection.createStatement();
            st.executeUpdate("INSERT INTO LineItems VALUES (cust_ID, order_ID, li_ID, item_ID, quantity, total_Cost, date_Delivered)");
        }
    }
    public static void GenItemData(int numDataToGen) throws SQLException{
    	Random rand = new Random();
        int item_ID;
        String item_Name = "";
        int price; //Also needs a function to generate dollars and cents
        for(int x = 0; x < numDataToGen; x++) {
            //generate the data
            item_ID = rand.nextInt(100000);
            item_Name += generateString(15);
            price = rand.nextInt(50);
            //do a sql insert here using the JDBC
            Statement st= connection.createStatement();
            st.executeUpdate("INSERT INTO Items VALUES (item_ID, item_Name, price)");
        }
    }
    public static void GenStockData(int numDataToGen) throws SQLException{
    	Random rand = new Random();
        int wh_ID;
        int item_ID;
        int quantity_avail;
        int quantity_sold;
        int num_orders;
        for(int x = 0; x < numDataToGen; x++) {
            //generate the data
            wh_ID = x+1;
            item_ID = rand.nextInt(100000);
            quantity_avail = rand.nextInt(5000);
            quantity_sold = rand.nextInt(5000);
            num_orders = rand.nextInt(20000);
            //do a sql insert here using the JDBC
            Statement st= connection.createStatement();
            st.executeUpdate("INSERT INTO Stock VALUES (wh_ID, item_ID, quantity_avail, quantity_sold, num_orders)");
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
		int firstDigitDate = rng.nextInt(4);
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

      public static void viewDB() throws SQLException{
    	  Statement stmt = connection.createStatement();
    	  ResultSet rs = stmt.executeQuery("select * from Warehouse");
    	  ResultSetMetaData rsmd = rs.getMetaData();
    	  System.out.println("Warehouse");
    	  System.out.println("-----------------------------------------------------------------------------------");
    	  System.out.println(rsmd.getColumnName(1)+"\t"+rsmd.getColumnName(2)+"\t"+rsmd.getColumnName(3)+"\t"+rsmd.getColumnName(4)+"\t"+rsmd.getColumnName(5)+"\t"+
    			  rsmd.getColumnName(6)+"\t"+rsmd.getColumnName(7)+"\t"+rsmd.getColumnName(8));
    	  System.out.println("-----------------------------------------------------------------------------------");
    	  while(rs.next()){
    		  System.out.println(rs.getLong(1)+"\t"+rs.getString(2)+"\t"+rs.getString(3)+"\t"+rs.getString(4)+"\t"+rs.getString(5)+"\t"+
    				  rs.getLong(6)+"\t"+
    				  rs.getLong(7)+"\t"+
    				  rs.getFloat(8));
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
