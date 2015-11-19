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

Tax_Rate VARCHAR2(255) NULL,

YTD_Sales_Sum VARCHAR2(255) NULL,

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

Tax_Rate VARCHAR2(255) NULL,

YTD_Sales_Sum VARCHAR2(255) NULL,

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

Phone_Number VARCHAR(14) NULL,

Sign_Up_Date DATE NULL,

Active_Discount VARCHAR2(255) NULL,

Debt VARCHAR2(255) NULL,

YTD_Purchase_Total VARCHAR2(255) NULL,

Num_Payments VARCHAR2(255) NULL,

Num_Deliveries VARCHAR2(255) NULL,

PRIMARY KEY (DS_ID, Cust_ID),

CONSTRAINT fk_Customers_DistStation_1 FOREIGN KEY (WH_ID, DS_ID) REFERENCES DistStation(WH_ID, DS_ID)

);

CREATE TABLE Orders (

DS_ID NUMBER(11) NOT NULL,

Cust_ID NUMBER(11) NOT NULL,

Order_ID NUMBER(11) NOT NULL,

Date_Placed DATE NULL,

Completed_Flag VARCHAR2(255) NULL,

Num_Items VARCHAR2(255) NULL,

PRIMARY KEY (Cust_ID, Order_ID),

CONSTRAINT fk_Orders_Customers_1 FOREIGN KEY (DS_ID, Cust_ID) REFERENCES Customers (DS_ID, Cust_ID)

);

CREATE TABLE Items (

Item_ID NUMBER NOT NULL,

Item_Name VARCHAR2(255) NULL,

Price NUMBER NULL,

PRIMARY KEY (Item_ID) 

);

CREATE TABLE LineItems (

Cust_ID NUMBER(11) NOT NULL,

Order_ID NUMBER(11) NOT NULL,

LI_ID NUMBER(11) NOT NULL,

Item_ID NUMBER(11) NULL,

Quantity VARCHAR2(255) NULL,

Total_Cost NUMBER NULL,

Date_Delivered DATE NULL,

PRIMARY KEY (Order_ID, LI_ID),

CONSTRAINT fk_LineItems_Orders_1 FOREIGN KEY (Cust_ID, Order_ID) REFERENCES Orders (Cust_ID, Order_ID),

CONSTRAINT fk_LineItems_Items_1 FOREIGN KEY (Item_ID) REFERENCES Items (Item_ID)

);

CREATE TABLE Stock (

WH_ID NUMBER NOT NULL,

Item_ID NUMBER NOT NULL,

Quantity_Available VARCHAR2(255) NULL,

Quantity_Sold VARCHAR2(255) NULL,

Num_Orders VARCHAR2(255) NULL,

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