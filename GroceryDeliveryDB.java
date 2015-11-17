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
                    //viewDB();
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
        GenWarehouseData(numDataToGen);
        GenDistStationData(numDataToGen);
        GenCustomerData(numDataToGen);
        GenOrderData(numDataToGen);
        GenLineItemData(numDataToGen);
        GenItemData(numDataToGen);
        GenStockData(numDataToGen);	
    }
    public static void GenWarehouseData(int numDataToGen) {
    //Should I initialize Strings with "" and then concat instead of assign?
        Random rand = new Random();
        int wh_ID;
        String wh_Name = "";
        String street_Address = "";
        String city = "";
        String state= "";
        int zipcode;
        int taxrate; //this is sales tax, I changed it to an int, doesn't need to be a double
        int ytdSalesSum; //Making this an int for now as well, we don't need to be that specific yet
        for(int x = 0; x < numDataToGen; x++) {
            //generate the data. I picked 7 as the length for the strings for no reason.
            wh_ID = x+1;
            wh_Name += generateString(7);
            street_Address += alphaNumString();
            city += generateString(7);
            state += generateString(2);
            state.toUpperCase();
            zipcode = rand.nextInt(90000) + 10000; //generates a zipcode from 10000-99999
            taxrate = rand.nextInt(8) + 1; //generates a taxrate from 1% to 8%
            ytdSalesSum = rand.nextInt(90000) + 1000; //We should build a method to just give an integer plus decimal places. This only does values 0.0-1.0
            //do a sql insert here using the JDBC
            statement = connection.createStatement();
            statement.executeUpdate("INSERT INTO Warehouse VALUES (wh_ID, wh_Name, street_Address, city, state, zipcode, taxrate, ytdSalesSum)");
            //System.out.println("WH_ID: "+ wh_ID+ "\nWarehouse_Name: "+ wh_Name+ "\nStreet_Address: "+ street_Address+ "\nCity: "+ city+ "\nState: "+ state + "\nZipcode: "+ zipcode + "\nTax Rate:" + taxrate+ "\nYTD_Sales_Sum: " +ytdSalesSum);
        }
    }
    public static void GenDistStationData(int numDataToGen) {
    	Random rand = new Random();
    	int wh_ID;
    	int ds_ID;
    	String ds_Name = "";
    	String street_Address = "";
    	String city = "";
    	String state = "";
    	int zipcode;
    	int taxrate;
    	int ytdSalesSum;
    	for(int x = 0; x < numDataToGen; x++) {
            //generate the data
            wh_ID = x+1; //In dealing with PK's and FK's, I assume we'll need a way to sync these for each table i.e. wh_ID's should match up
            ds_ID = x+2; //^^same as above, what should these really be?
            ds_Name += generateString(7);
            street_Address += alphaNumString();
            city += generateString(7);
            state += generateString(2);
            state.toUpperCase();
            zipcode = rand.nextInt(90000) + 10000; //generates a zipcode from 10000-99999
            taxrate = rand.nextInt(8) + 1; //generates a taxrate from 1% to 8%
            ytdSalesSum = rand.nextInt(90000) + 1000;
            //do a sql insert here using the JDBC
            statement = connection.createStatement();
            statement.executeUpdate("INSERT INTO DistStation VALUES (wh_ID, ds_ID ds_Name, street_Address, city, state, zipcode, taxrate, ytdSalesSum)");
        }
    }
    public static void GenCustomerData(int numDataToGen) {
    	Random rand = new Random();
        int wh_ID;
    	int ds_ID;
    	int cust_ID;
    	String first_Name = "";
    	String middle_init = "";
    	String last_Name = "";
    	String street_Address = "";
    	String city = "";
    	String state = "";
    	int zipcode;
    	int phone_Num;
    	int signup_Date;
    	//double active_discount;
    	int debt;
    	int ytdPurchaseTotal;
    	int num_payments;
    	int num_deliveries;
        for(int x = 0; x < numDataToGen; x++) {
            //generate the data
            //do a sql insert here using the JDBC
        }
    }
    public static void GenOrderData(int numDataToGen) {
    	Random rand = new Random();
       	int ds_ID;
    	int cust_ID;
    	int order_ID;
    	//int date_Placed;
    	//String completed_Flag;
    	int num_Items;
       	for(int x = 0; x < numDataToGen; x++) {
            //generate the data
            //do a sql insert here using the JDBC
        }
    }
    public static void GenLineItemData(int numDataToGen) {
    	Random rand = new Random();
        int cust_ID;
    	int order_ID;
    	int li_ID;
    	int item_ID;
    	int quantity;
    	int total_Cost;
    	//int date_Delivered;
        for(int x = 0; x < numDataToGen; x++) {
            //generate the data
            //do a sql insert here using the JDBC
        }
    }
    public static void GenItemData(int numDataToGen) {
    	Random rand = new Random();
        int item_ID;
        String item_Name = "";
        int price;
        for(int x = 0; x < numDataToGen; x++) {
            //generate the data
            //do a sql insert here using the JDBC
        }
    }
    public static void GenStockData(int numDataToGen) {
    	Random rand = new Random();
        int wh_ID;
        int item_ID;
        int quantity_avail;
        int quantity_sold;
        int num_orders;
        for(int x = 0; x < numDataToGen; x++) {
            //generate the data
            //do a sql insert here using the JDBC
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
		StringBuilder temp = new StringBuilder;
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
		StringBuilder temp = new StringBuilder;
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
    /* This will list all tables in the database, not just our workspace.
     * public static void viewDB() throws SQLException{
    	System.out.println("Showing all tables...");
    	DatabaseMetaData md = connection.getMetaData();
    	ResultSet rs = md.getTables(null, null, "%", null);
    	while (rs.next()) {
    		System.out.println(rs.getString(3));
    	}
    	rs.close();    	
    }*/
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
