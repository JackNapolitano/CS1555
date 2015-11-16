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
        int wh_ID = 1;
        String wh_Name = "adfljh";
        String street_Address = "123 asc rd";
        String city = "Victor";
        String state = "NY";
        int zipcode = 14564;
        double taxrate = 0;
        double ytdSalesSum = 0;
        for(int x = 0; x < numDataToGen; x++) {
            //generate the data
            //do a sql insert here using the JDBC
            System.out.println("WH_ID: "+ wh_ID+ "\nWarehouse_Name: "+ wh_Name+ "\nStreet_Address: "+ street_Address+ "\nCity: "+ city+ "\nState: "+ state + "\nZipcode: "+ zipcode + "\nTax Rate:" + taxrate+ "\nYTD_Sales_Sum: " +ytdSalesSum);
        }
    }
    public static void GenDistStationData(int numDataToGen) {
        for(int x = 0; x < numDataToGen; x++) {
            //generate the data
            //do a sql insert here using the JDBC
        }
    }
    public static void GenCustomerData(int numDataToGen) {
        for(int x = 0; x < numDataToGen; x++) {
            //generate the data
            //do a sql insert here using the JDBC
        }
    }
    public static void GenOrderData(int numDataToGen) {
        for(int x = 0; x < numDataToGen; x++) {
            //generate the data
            //do a sql insert here using the JDBC
        }
    }
    public static void GenLineItemData(int numDataToGen) {
        for(int x = 0; x < numDataToGen; x++) {
            //generate the data
            //do a sql insert here using the JDBC
        }
    }
    public static void GenItemData(int numDataToGen) {
        for(int x = 0; x < numDataToGen; x++) {
            //generate the data
            //do a sql insert here using the JDBC
        }
    }
    public static void GenStockData(int numDataToGen) {
        for(int x = 0; x < numDataToGen; x++) {
            //generate the data
            //do a sql insert here using the JDBC
        }
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