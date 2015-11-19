--Drop Tables
--uncomment to re run .sql
/*
DROP TABLE Stock;
DROP TABLE LineItems;
DROP TABLE Items;
DROP TABLE Orders;
DROP TABLE Customers;
DROP TABLE DistStation;
DROP TABLE Warehouse;
*/


CREATE TABLE Warehouse (

WH_ID NUMBER(11) NOT NULL,

Warehouse_Name VARCHAR2(255) NULL,

Street_Address VARCHAR2(255) NULL,

City VARCHAR2(255) NULL,

State VARCHAR2(255) NULL,

Zip_Code VARCHAR2(255) NULL,

Tax_Rate NUMBER NULL,

YTD_Sales_Sum NUMBER(20, 2) NULL,

PRIMARY KEY (WH_ID) 

);

CREATE TABLE DistStation (

WH_ID NUMBER(11) NOT NULL,

DS_ID NUMBER(11) NOT NULL,

DS_Name VARCHAR2(255) NULL,

Street_Address VARCHAR2(255) NULL,

City VARCHAR2(255) NULL,

State VARCHAR2(255) NULL,

Zip_Code VARCHAR2(255) NULL,

Tax_Rate NUMBER NULL,

YTD_Sales_Sum NUMBER(20, 2) NULL,

PRIMARY KEY (WH_ID, DS_ID),

CONSTRAINT fk_DistStation_Warehouse_1 FOREIGN KEY (WH_ID) REFERENCES Warehouse (WH_ID)

);

CREATE TABLE Customers (

WH_ID NUMBER(11) NOT NULL,

DS_ID NUMBER(11) NOT NULL,

Cust_ID NUMBER(11) NOT NULL,

First_Name VARCHAR2(255) NULL,

Middle_Initial VARCHAR2(255) NULL,

Last_Name VARCHAR2(255) NULL,

Street_Address VARCHAR2(255) NULL,

City VARCHAR2(255) NULL,

State VARCHAR2(255) NULL,

Zip_code VARCHAR2(255) NULL,

Phone_Number NUMBER(11) NULL,

Sign_Up_Date DATE NULL,

Active_Discount NUMBER NULL,

Debt NUMBER(20, 2) NULL,

YTD_Purchase_Total NUMBER(20, 2) NULL,

Num_Payments NUMBER NULL,

Num_Deliveries NUMBER NULL,

PRIMARY KEY (WH_ID, DS_ID, Cust_ID),

CONSTRAINT fk_Customers_DistStation_1 FOREIGN KEY (WH_ID, DS_ID) REFERENCES DistStation(WH_ID, DS_ID)

);

CREATE TABLE Orders (

WH_ID NUMBER(11) NOT NULL,

DS_ID NUMBER(11) NOT NULL,

Cust_ID NUMBER(11) NOT NULL,

Order_ID NUMBER(11) NOT NULL,

Date_Placed DATE NULL,

Completed_Flag VARCHAR2(255) NULL,

Num_Items NUMBER NULL,

PRIMARY KEY (WH_ID, DS_ID, Cust_ID, Order_ID),

CONSTRAINT fk_Orders_Customers_1 FOREIGN KEY (WH_ID, DS_ID, Cust_ID) REFERENCES Customers (WH_ID, DS_ID, Cust_ID)

);

CREATE TABLE Items (

Item_ID NUMBER NOT NULL,

Item_Name VARCHAR2(255) NULL,

Price NUMBER NULL,

PRIMARY KEY (Item_ID) 

);

CREATE TABLE LineItems (

WH_ID NUMBER(11) NOT NULL,

DS_ID NUMBER(11) NOT NULL,

Cust_ID NUMBER(11) NOT NULL,

Order_ID NUMBER(11) NOT NULL,

LI_ID NUMBER(11) NOT NULL,

Item_ID NUMBER(11) NULL,

Quantity NUMBER NULL,

Total_Cost NUMBER(20, 2) NULL,

Date_Delivered DATE NULL,

PRIMARY KEY (WH_ID, DS_ID, Cust_ID, Order_ID, LI_ID),

CONSTRAINT fk_LineItems_Orders_1 FOREIGN KEY (WH_ID, DS_ID, Cust_ID, Order_ID) REFERENCES Orders (WH_ID, DS_ID, Cust_ID, Order_ID),

CONSTRAINT fk_LineItems_Items_1 FOREIGN KEY (Item_ID) REFERENCES Items (Item_ID)

);

CREATE TABLE Stock (

WH_ID NUMBER NOT NULL,

Item_ID NUMBER NOT NULL,

Quantity_Available NUMBER NULL,

Quantity_Sold NUMBER NULL,

Num_Orders NUMBER NULL,

PRIMARY KEY (WH_ID, Item_ID),

CONSTRAINT fk_Stock_Warehouse_1 FOREIGN KEY (WH_ID) REFERENCES Warehouse (WH_ID),

CONSTRAINT fk_Stock_Items_1 FOREIGN KEY (Item_ID) REFERENCES Items (Item_ID)

);

create or replace trigger UpdatePaidorDebt
	after insert or update on Orders
	for each row
	begin
      case
        when(new.COMPLETED_FLAG = "Completed")
          update Customers set YTD_PURCHASE_TOTAL =: old.YTD_PURCHASE_TOTAL + select sum(TOTAL_COST) FROM LineItems 
            WHERE ORDER_ID= new.ORDER_ID; 
        when (new.COMPLETED_FLAG = "Incomplete")
          update Customers set DEBT =: old.DEBT + select sum(TOTAL_COST) FROM LineItems 
            WHERE ORDER_ID= new.ORDER_ID; 
	end;
/	