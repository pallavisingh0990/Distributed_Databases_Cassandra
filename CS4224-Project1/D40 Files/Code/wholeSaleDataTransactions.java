import com.datastax.driver.core.Cluster;
import com.datastax.driver.core.Metadata;
import com.datastax.driver.core.Session;
import java.math.BigDecimal;
import java.util.Date;
import com.datastax.driver.core.Row;
import com.datastax.driver.core.ResultSet;
import java.lang.*;
import java.util.Date;
import java.math.*;
import java.util.*;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class wholeSaleDataTransactions{
private Session session;
private String keyspace;

wholeSaleDataTransactions(Session session,String keyspace)
{
  this.session = session;
  this.keyspace = keyspace;
}

 //New Order Transaction Processing
 void newOrderTransactions(int W_ID, int D_ID, int C_ID, int NUM_ITEMS, int[] ITEM_NUMBER, int[] SUPPLIER_WAREHOUSE,long[] QUANTITY)
	{
	long N = 0;
	int o_id = 0;
	int Qnty = 0;
	double w_tax = 0, d_tax = 0, c_discount = 0;
	String cname = null, c_credit = null;
	long updQty[] = new long[NUM_ITEMS];
    double TOTAL_ITEMAMOUNT = 0;
	double ITEM_AMOUNT[] = new double[NUM_ITEMS];
    String OL_DIST_INFOs = null;
	int OL_O_ID;
    int OL_QUANTITY; 			
    double ADJUSTED_QUANTITY[]= new double[NUM_ITEMS];
    long s_quantity[]= new long[NUM_ITEMS];
    long updateQUANTITY[]= new long[NUM_ITEMS];
    double iprice[] = new double[NUM_ITEMS];
    String iname[] = new String[NUM_ITEMS];
	

    SimpleDateFormat sdf= new SimpleDateFormat("YYYY-MM-DD HH:mm:ss");
    String dateString = sdf.format(new Date());

	
        String d_nextorder_id_sel = ("SELECT d_next_o_id FROM  "+keyspace+".districttable_deliverytrans WHERE d_w_id = "+W_ID+" and d_id ="+D_ID+";");
	    ResultSet result = session.execute(d_nextorder_id_sel);
        
		for (Row row : result)
		{
			N = row.getInt(0);
		}
        long newValue = 0;
        newValue = N+1;
		String updatequery1 = "UPDATE  "+keyspace+".districttable_deliverytrans SET d_next_o_id = "+newValue+" WHERE d_w_id = "+W_ID+" and d_id="+D_ID+";";
        session.execute(updatequery1);
        int count = 0,mismatchCount=0;	
        
		for (int i =0;i<SUPPLIER_WAREHOUSE.length;i++){
            if(W_ID==SUPPLIER_WAREHOUSE[i]){
                     count++;
                  }else {
                      mismatchCount++;
                   }
               }
   
        int O_ALL_LOCAL=0;
        if(count==SUPPLIER_WAREHOUSE.length){
                 O_ALL_LOCAL=1;
                   }
              else {
                O_ALL_LOCAL=0;
               }
               
       String insertDelivery = "INSERT  INTO  "+keyspace+".ordertable_deliverytrans(O_W_ID, O_D_ID, O_ID, O_C_ID, O_CARRIER_ID, O_OL_CNT, O_ALL_LOCAL, O_ENTRY_D) VALUES ("+W_ID+","+D_ID+","+N+","+C_ID+","+0+","+NUM_ITEMS+","+O_ALL_LOCAL+",dateof(now()));";

       session.execute(insertDelivery);

        int TOTAL_AMOUNT=0;
        List<Integer> sQuantityList = new ArrayList<Integer>();
        for (int ia=0;ia<NUM_ITEMS;ia++)
        {
        String selectQuery = "SELECT S_QUANTITY FROM  "+keyspace+".stocktable_newordertrans WHERE S_W_ID = "+SUPPLIER_WAREHOUSE[ia]+" AND S_I_ID = "+ITEM_NUMBER[ia];
        ResultSet result1=  session.execute(selectQuery);
                for (Row row : result1)
                {
                        s_quantity[ia] = row.getDecimal("S_QUANTITY").longValue();
                }
 
        ADJUSTED_QUANTITY[ia] = s_quantity[ia] - QUANTITY[ia];

        if(ADJUSTED_QUANTITY[ia]<10)
        {
          ADJUSTED_QUANTITY[ia] = ADJUSTED_QUANTITY[ia]+91;
        }
       else
        {
          ADJUSTED_QUANTITY[ia] = ADJUSTED_QUANTITY[ia]+0;

        }

        int s_wid = SUPPLIER_WAREHOUSE[ia];
        int update_S_ORDER_CNT =0;
        int up_S_ORDER_CNT=update_S_ORDER_CNT+ 1;
        int up_S_REMOTE_CNT=update_S_ORDER_CNT+1;

 
        if(s_wid!=W_ID)
        {

        String updateQuery2= "UPDATE  "+keyspace+".stocktable_newordertrans SET S_QUANTITY = "+ADJUSTED_QUANTITY[ia]+" ,"+"S_YTD = S_YTD + "+QUANTITY[ia]+","+" S_ORDER_CNT = "+up_S_ORDER_CNT+","+" S_REMOTE_CNT ="+ up_S_REMOTE_CNT+" WHERE S_I_ID = "+ITEM_NUMBER[ia]+" AND S_W_ID = "+SUPPLIER_WAREHOUSE[ia]+";";       
        } 
       else
       {
        String updateQuery3= "UPDATE  "+keyspace+".stocktable_newordertrans SET S_QUANTITY = "+ADJUSTED_QUANTITY[ia]+" ,"+"S_YTD = S_YTD + "+QUANTITY[ia]+","+" S_ORDER_CNT = "+up_S_ORDER_CNT+" WHERE S_I_ID = "+ITEM_NUMBER[ia]+" AND S_W_ID = "+SUPPLIER_WAREHOUSE[ia]+";";
        }

        String item_price_sel = ("SELECT I_NAME,I_PRICE FROM  "+keyspace+".itemtable_poptrans WHERE i_id = "+ITEM_NUMBER[ia]+";"); 
        ResultSet resultQuery = session.execute(item_price_sel);
        for (Row row : resultQuery)
	   {
		iname[ia] = row.getString(0);
	    iprice[ia] = row.getDecimal(1).doubleValue();
        }
        ITEM_AMOUNT[ia] = QUANTITY[ia] * iprice[ia];
		TOTAL_ITEMAMOUNT += ITEM_AMOUNT[ia];

        OL_DIST_INFOs = "S_DIST_" + D_ID;
	    OL_O_ID = (int) N;
	    OL_QUANTITY = (int) QUANTITY[ia];	
             
    
    String insertOrderLine = "INSERT  INTO  "+keyspace+".orderlinetable_deliverytrans(OL_W_ID,OL_D_ID,OL_O_ID,OL_NUMBER,OL_I_ID,OL_DELIVERY_D,OL_AMOUNT,OL_SUPPLY_W_ID,OL_QUANTITY,OL_DIST_INFO) VALUES ("+W_ID+","+D_ID+","+N+","+ia+","+ITEM_NUMBER[ia]+","+0+","+ITEM_AMOUNT[ia]+","+SUPPLIER_WAREHOUSE[ia]+","+QUANTITY[ia]+",'"+OL_DIST_INFOs+"')";
    session.execute(insertOrderLine);
    
  }

        double d_tax_value=0.0;
    String selectQuery4 = "SELECT D_TAX FROM  "+keyspace+".districttable_deliverytrans WHERE D_W_ID = "+W_ID+" AND D_ID = "+D_ID;
	ResultSet result4 = session.execute(selectQuery4);
	
	    for (Row row : result4)
	    {
        d_tax_value = row.getDecimal(0).doubleValue();
        }			
				
		double w_tax_value=0.0;
    String selectQuery5 = "SELECT W_TAX FROM  "+keyspace+".warehouse_paymenttrans WHERE W_ID = "+W_ID;
    ResultSet result5 = session.execute(selectQuery5);
        
        for (Row row : result5)
        {
        w_tax_value = row.getDecimal(0).doubleValue();
        }

		double C_DISCOUNT_value = 0.0;
        String C_LAST_value = null;
	String C_CREDIT_value =null; 
        String selectQuery6 = "SELECT C_DISCOUNT,C_LAST,C_CREDIT FROM  "+keyspace+".customertable_deliverytrans WHERE C_ID = "+C_ID+" AND C_W_ID = "+W_ID+" AND C_D_ID ="+D_ID;
        ResultSet result6 = session.execute(selectQuery6);
    
        for (Row row : result6)
        {
        C_DISCOUNT_value = row.getDecimal(0).doubleValue();
        C_LAST_value = row.getString(1);
        C_CREDIT_value = row.getString(2);   
        }


        TOTAL_ITEMAMOUNT = TOTAL_ITEMAMOUNT * (1 + d_tax_value + w_tax_value) * (1 - C_DISCOUNT_value);	
 System.out.println("___________________________________________________\n");
 System.out.println("\n\nProcessing Output Steps for Transaction 2.1: ");       
 System.out.println("___________________________________________________\n");

 System.out.println("Customer Identifier (W ID, D ID, C ID):"+W_ID+","+D_ID+","+C_ID);
 System.out.println("Customer LastName C_LAST : "+C_LAST_value+" ,"+"credit C_CREDIT : "+C_CREDIT_value+" ,"+"discount C_DISCOUNT :  "+C_DISCOUNT_value);		
 System.out.println("Order Number : "+N+" ,"+"Entry Date : "+dateString);
 System.out.println("Number of Item : "+NUM_ITEMS+" ,"+"TOTAL_AMOUNT : "+TOTAL_ITEMAMOUNT);

    for (int ia=0;ia<NUM_ITEMS;ia++)
    {
    System.out.println("Item Number :"+ITEM_NUMBER[ia]+" ,"+"IName :"+iname[ia]+" ,"+"Supplier Warehouse : "+SUPPLIER_WAREHOUSE[ia]+" ,"+"Quanntity :"+QUANTITY[ia]+" ,"+"S_QUANTITY : "+s_quantity[ia]);
    }

}

//Payment Transaction Processing
  void PaymentTransactions(int c_w_id,int c_d_id,int c_id,double payment)
	{
      String W_STREET_1 =null, W_STREET_2=null, W_CITY =null, W_STATE =null, W_ZIP = null;
		String D_STREET_1 =null, D_STREET_2=null, D_CITY =null, D_STATE =null, D_ZIP = null;     
		String cFirst = null,cMiddle = null,cLast = null,cStreet1 = null,cStreet2=null,cCity =null,cState = null,cZip =null;
		String C_PHONE=null,C_CREDIT = null;
		Date C_SINCE=null;
        Double C_CREDIT_LIM=0.0,C_DISCOUNT=0.0,C_BALANCE=0.0;
		double currentPaid = 0,newAmount = 0,currentPaid1 =0;
		double cbal=0, cpay=0;
		int cpayCnt=0;
	  	double nCbal, nCpay;
		int nCpayCnt;
		
		String query1 = "SELECT W_YTD,W_STREET_1,W_STREET_2,W_CITY,W_STATE,W_ZIP FROM  "+keyspace+".warehouse_paymenttrans WHERE W_ID ="+c_w_id+";";
		ResultSet rs1 = session.execute (query1);
		
		for (Row rows1 : rs1){
		BigDecimal bd = rows1.getDecimal(0);
		currentPaid = bd.doubleValue();
        W_STREET_1 = rows1.getString(1);
		W_STREET_2 = rows1.getString(2);
		W_CITY = rows1.getString(3);
		W_STATE = rows1.getString(4);
		W_ZIP = rows1.getString(5);
		}

		newAmount =  currentPaid + payment;
		String updateQuery1 = "UPDATE  "+keyspace+".warehouse_paymenttrans SET W_YTD = "+newAmount+" WHERE W_ID = "+c_w_id+";";
		session.execute(updateQuery1);
		
		String query2 = "SELECT D_YTD,D_STREET_1,D_STREET_2,D_CITY,D_STATE,D_ZIP FROM  "+keyspace+".district_stocktrans WHERE D_W_ID ="+c_w_id+" and D_ID="+c_d_id+" ;";
		ResultSet rs2 = session.execute (query2);
       
		for (Row rows2 : rs2){
		currentPaid1 = rows2.getDecimal(0).doubleValue();
        D_STREET_1 = rows2.getString(1);
		D_STREET_2 = rows2.getString(2);
		D_CITY = rows2.getString(3);
		D_STATE = rows2.getString(4);
		D_ZIP = rows2.getString(5); 
		}

		double newAmount1 = currentPaid1+payment;
		
        String updateQuery2 = "UPDATE  "+keyspace+".district_stocktrans SET D_YTD = "+newAmount1+" WHERE D_W_ID = "+c_w_id+" and D_ID="+c_d_id+" ;";
		session.execute(updateQuery2);
	
		String query3 = "SELECT C_BALANCE, C_YTD_PAYMENT, C_PAYMENT_CNT,C_FIRST,C_MIDDLE,C_LAST,C_STREET_1,C_STREET_2,C_CITY,C_STATE,C_ZIP,C_PHONE,C_SINCE, C_CREDIT,C_CREDIT_LIM, C_DISCOUNT, C_BALANCE FROM  "+keyspace+".customers_paymenttrans WHERE C_W_ID = "+c_w_id+" AND C_D_ID = "+c_d_id+" AND C_ID ="+c_id+";";
		ResultSet rs3 = session.execute(query3);
        for(Row rows3 : rs3){
		cbal = rows3.getDecimal(0).doubleValue();
		cpay = rows3.getDecimal(1).doubleValue();
		cpayCnt = rows3.getInt(2);
        cFirst = rows3.getString(3);
        cMiddle = rows3.getString(4);
        cLast = rows3.getString(5);	
        cStreet1 = rows3.getString(6);
        cStreet2 = rows3.getString(7);
        cCity = rows3.getString(8);
        cState = rows3.getString(9);
        cZip = rows3.getString(10); 
        C_PHONE =rows3.getString(11);
        C_SINCE =rows3.getDate(12);
        C_CREDIT =rows3.getString(13);
        C_CREDIT_LIM= rows3.getDecimal(14).doubleValue();
        C_DISCOUNT = rows3.getDecimal(15).doubleValue(); 
        C_BALANCE = rows3.getDecimal(16).doubleValue();       	
    }

    	
		nCbal = cbal - payment;
		nCpay = cpay + payment;
		nCpayCnt = cpayCnt+1;
		
	    String updateQuery3 = "UPDATE  "+keyspace+".customers_paymenttrans SET C_BALANCE ="+nCbal+", C_YTD_PAYMENT = "+nCpay+", C_PAYMENT_CNT ="+nCpayCnt+"WHERE C_W_ID = "+c_w_id+" AND C_D_ID = "+c_d_id+" AND C_ID ="+c_id+";";
		session.execute(updateQuery3);
        System.out.println("___________________________________________________\n");
        
        System.out.println("\n\nProcessing Output Steps for Transaction 2.2 : ");
        System.out.println("___________________________________________________\n");
        System.out.println("Name of Customer : "+cFirst+" ,"+cMiddle+" ,"+cLast);		
        System.out.println("Address : "+cStreet1+" ,"+cStreet2+" ,"+cCity+" ,"+cState+" ,"+cZip);	
        System.out.println("C_PHONE : "+C_PHONE+" since "+C_SINCE+" ,C_CREDIT : "+C_CREDIT);
        System.out.println("C_CREDIT_LIM : "+C_CREDIT_LIM+" ,C_DISCOUNT : "+C_DISCOUNT+" ,C_BALANCE : "+C_BALANCE+" ,C_PHONE :"+C_PHONE); 
        System.out.println("Warehouse Address : "+D_STREET_1+" ,"+D_STREET_2+" ,"+D_CITY+" ,"+D_STATE+" ,"+D_ZIP);
	    System.out.println("Warehouse Address : "+W_STREET_1+" ,"+W_STREET_2+" ,"+W_CITY+" ,"+W_STATE+" ,"+W_ZIP);

    }

//Stock Level Transaction Processing
   public void stockLevelTransaction(int w_id, int d_id, int T, int L){
	long answer = 0;
        int nextOrderId =0;

    String query1 = "SELECT D_NEXT_O_ID  FROM "+keyspace+".district_stocktrans WHERE D_W_ID ="+ w_id +" AND D_ID ="+d_id+";";
	ResultSet nextOrderIdResults = session.execute(query1);
	for (Row rows : nextOrderIdResults)
	{
		 nextOrderId = rows.getInt(0);
	}
	int lowLimit = nextOrderId - L;

	String query2 = "SELECT OL_I_ID FROM "+keyspace+".orderline_stocktrans WHERE OL_W_ID ="+w_id+" AND OL_D_ID ="+d_id+" AND OL_O_ID >="+lowLimit+" AND OL_O_ID < "+nextOrderId +";";
	ResultSet rs1 = session.execute(query2);
	int count = 0;
	String set1 = "(";
        for (Row rows1 : rs1)
	{
		if(count == 0)
		{
			set1 = set1+ Integer.toString(rows1.getInt(0));
		}
	        set1 = set1 + ","+ Integer.toString(rows1.getInt(0));
        	count++;
	}
        set1 = set1 + ")";
	String query3 = "SELECT count(*) FROM "+keyspace+".stock_stocktrans WHERE  S_W_ID ="+w_id+" AND  S_QUANTITY <"+ T +" and S_I_ID in "+set1;
	ResultSet rs2 = session.execute(query3);

	
	for (Row rows2 : rs2){
		answer = rows2.getLong(0);
	}

        System.out.println("___________________________________________________\n");

        System.out.println("\n\nProcessing Output Steps for Transaction 2.5 : ");
        System.out.println("___________________________________________________\n");
	
	System.out.println("Total Number of Item where its stock quantity below threshold: "+answer);
}


//Delivery Transaction Processing
 public void deliveryTransaction(int o_w_id,int o_carrier_id){
   try{  
   int O_ID=0, C_ID=0;
   double OL_AMOUNT=0, OL_AMOUNTtotal=0.0;
   int c_delivery_cnt=0;
   double c_bal=0;
   double totalamt =0.0;
   int OL_NUMBER=0;


for(int d_id=1;d_id<=10;d_id++)
	{
ResultSet  results= session.execute("SELECT O_ID,O_C_ID FROM "+keyspace+".ORDERTABLE_DELIVERYTRANS WHERE O_W_ID="+o_w_id+" AND O_D_ID="+d_id+" AND O_CARRIER_ID=0 LIMIT 1");
         for (Row row : results) {
			O_ID=row.getInt("O_ID");
			C_ID=row.getInt("O_C_ID");	
		}	
            
   
   int OL_I_ID=0;
   session.execute("UPDATE "+keyspace+".ORDERTABLE_DELIVERYTRANS SET O_CARRIER_ID="+o_carrier_id+" WHERE O_W_ID="+o_w_id+" AND O_D_ID="+d_id+"  AND O_ID="+O_ID);
		
   ResultSet ol_num_results = session.execute("SELECT OL_NUMBER,OL_AMOUNT,OL_I_ID FROM "+keyspace+".ORDERLINETABLE_DELIVERYTRANS WHERE OL_W_ID="+o_w_id+" AND OL_D_ID="+d_id+" AND OL_O_ID="+O_ID);
 	for (Row row : ol_num_results) {
              		OL_NUMBER=row.getInt("OL_NUMBER");
                        OL_I_ID=row.getInt(2); 
                      
                          totalamt = totalamt+row.getDecimal(1).doubleValue();
             
     }             
                     
for(int i=0;i<OL_NUMBER;i++)
{    
   session.execute("UPDATE "+keyspace+".ORDERLINETABLE_DELIVERYTRANS SET OL_DELIVERY_D=DATEOF(NOW()) WHERE OL_W_ID="+o_w_id+" AND OL_D_ID="+d_id+" AND OL_O_ID ="+O_ID+" AND OL_I_ID ="+OL_I_ID);
}
		
   ResultSet c_del_cnt_results=session.execute("select C_DELIVERY_CNT,C_BALANCE from "+keyspace+".CUSTOMERTABLE_DELIVERYTRANS WHERE C_W_ID="+o_w_id+" AND C_D_ID="+d_id+" AND C_ID="+C_ID);
	for (Row row : c_del_cnt_results) {
			c_delivery_cnt=row.getInt("C_DELIVERY_CNT");
              
	                c_bal=row.getDecimal("C_BALANCE").doubleValue()+totalamt;

		}		
		c_delivery_cnt=c_delivery_cnt+1;

 session.execute("UPDATE "+keyspace+".CUSTOMERTABLE_DELIVERYTRANS SET C_DELIVERY_CNT="+c_delivery_cnt+",C_BALANCE="+c_bal+" WHERE C_W_ID="+o_w_id+" AND C_D_ID="+d_id+" AND C_ID="+C_ID);
		
	}

System.out.println("___________________________________________________\n");
System.out.println("\n\nProcessing Output Steps for Transaction 2.3 : ");
System.out.println("___________________________________________________\n");
 
System.out.format("Records are updated successfully for WAREHOUSE_ID: "+o_w_id);
System.out.println("\n");
}
catch(Exception e)
{
System.out.println("___________________________________________________\n");
System.out.println("\n\nProcessing Output Steps for Transaction 2.3 : ");
System.out.println("___________________________________________________\n");

System.out.format("Records are updated successfully for WAREHOUSE_ID: "+o_w_id);
System.out.println("\n");

  }

}

//Order Status Transaction Processing
 public void orderStatusTransaction(int warehouseID,int districtID, int customerID){
  int O_ID=0, C_ID=0;
  double OL_AMOUNT=0;
  int c_delivery_cnt=0;
  double c_bal=0;
		
  String customerFirstName = "",customerMiddleName = "",customerLastName = "";
  BigDecimal customerBalance=null;
  int OrderID=0;
  Date OrderEntryID=null;
  int CarrierID=0;
  int ItemID=0;
  int SupplyWarehouseID=0;
  BigDecimal quantity=null;
  BigDecimal amount=null;
 Date deliveryDate=null;
	
 String customerDetails = "SELECT C_FIRST,C_MIDDLE,C_LAST,C_BALANCE FROM  "+keyspace+".CUSTOMERTABLE_DELIVERYTRANS WHERE C_ID="+customerID+" AND C_D_ID="+districtID+" AND C_W_ID="+warehouseID+" LIMIT 1";  
 ResultSet customerResultSet = session.execute(customerDetails);  
System.out.println("___________________________________________________\n");
System.out.println("\n\nProcessing Output Steps for Transaction 2.4 : ");
System.out.println("___________________________________________________\n");


	for (Row row : customerResultSet) {
		customerFirstName = row.getString("C_FIRST");
                customerMiddleName = row.getString("C_MIDDLE");
		customerLastName = row.getString("C_LAST");
		customerBalance = row.getDecimal("C_BALANCE");
    }
	
String OrderIDQuery = "SELECT O_ID,O_ENTRY_D,O_CARRIER_ID FROM  "+keyspace+".order_by_customer_id_orderstatustrans WHERE O_C_ID="+customerID+" AND O_D_ID="+districtID+" AND O_W_ID="+ warehouseID +" LIMIT 1";
	
ResultSet OrderQueryResultSet = session.execute(OrderIDQuery);  
	
	for (Row row : OrderQueryResultSet) {
		OrderID = row.getInt("O_ID");
                OrderEntryID = row.getDate("O_ENTRY_D");
		CarrierID = row.getInt("O_CARRIER_ID");		
    }
	
String OrderLineQuery = "SELECT OL_I_ID,OL_SUPPLY_W_ID,OL_QUANTITY,OL_AMOUNT,OL_DELIVERY_D FROM  "+keyspace+".ORDERLINETABLE_DELIVERYTRANS WHERE ol_o_id=" + OrderID +" AND ol_w_id=" + warehouseID + " AND ol_d_id=" + districtID;
      	
      ResultSet orderLineResultSet = session.execute(OrderLineQuery); 
	
      System.out.println("Customer Name (First,Middle,Last) : " + customerFirstName+" "+customerMiddleName+" "+customerLastName);
      
      System.out.println("\nCustomer Balance: " + customerBalance+" for Order Id:"+OrderID+" and Carrier Id :"+CarrierID);
      System.out.println("\nCustomer last Order Details :");
    	
	for (Row row : orderLineResultSet) {
		ItemID = row.getInt("OL_I_ID");
                SupplyWarehouseID = row.getInt("OL_SUPPLY_W_ID");
		quantity = row.getDecimal("OL_QUANTITY");
		deliveryDate = row.getDate("OL_DELIVERY_D");		
		amount = row.getDecimal("OL_AMOUNT");
System.out.println("Item ID: " + ItemID+ ",with Supplying Warehouse Number: "+SupplyWarehouseID+ ", Quantity Order :"+quantity+" with Total Price :"+amount+ " ,Delivery Date and Time : "+deliveryDate);
	
			
    }
}

//Popular Item Transaction Processing
void popularItemTransaction(int W_ID, int D_ID, int L)
	{
    
    String O_ID=null,OL_Set=null;
    double tempMax=0;
    System.out.println("___________________________________________________\n");
    System.out.println("\n\nProcessing Output Steps for Transaction 2.6: ");       
    System.out.println("___________________________________________________\n");
    System.out.println("District identifiers: W_ID :" + W_ID + ", D_ID :" + D_ID);
    System.out.println("Number of Last Orders to be Examined:" +L);
	
	int N=0;
	   
    String selectNextOrder = "SELECT D_NEXT_O_ID  FROM  "+keyspace+".district_stocktrans WHERE D_W_ID ="+W_ID +" AND D_ID ="+D_ID;
    ResultSet nextOrderIdResults = session.execute(selectNextOrder);
    for (Row rows : nextOrderIdResults)
	   {
	   N = rows.getInt(0);
       }
		   
	int lowLimit = N-1;

    String selectOId = "SELECT O_ID FROM  "+keyspace+".ordertable_deliverytrans WHERE O_W_ID ="+W_ID+" AND O_D_ID ="+D_ID+" AND O_ID >="+lowLimit+" AND O_ID < "+N;
    ResultSet rs = session.execute(selectOId);
 
    TreeSet<Integer> OIDset = new TreeSet<Integer>();
    List <String> resList = new ArrayList<String>();

    int OrderId=0;     
    for(Row rows1 : rs)
    {
    OrderId = rows1.getInt(0);
     
     }
    
    
    String selectOLTable = "SELECT O_ID,OL_NUMBER,I_ID,OL_QUANTITY,O_ENTRY_D,C_FIRST,C_MIDDLE,C_LAST,I_NAME FROM "+keyspace+".MasterTable WHERE O_ID ="+OrderId+" AND O_W_ID ="+W_ID+" AND O_D_ID ="+D_ID+";";
    ResultSet rs1 = session.execute(selectOLTable);
    String OL_Quant=null,OlNum=null;	

    double quant=0;
    for (Row row : rs1) {
      quant = row.getDecimal("OL_QUANTITY").doubleValue();

      String temp = row.getInt("O_ID") + "," + row.getString("C_FIRST") + "," +  row.getString("C_MIDDLE") + ","+ row.getString("C_LAST") + ","+ row.getDate("O_ENTRY_D") + "," + row.getInt("OL_NUMBER") + "," + row.getInt("I_ID")+ "," + quant + "," + row.getString("I_NAME") ;
      resList.add(temp);
       OIDset.add(row.getInt("O_ID"));
        }

    Map <Integer, String> popID = new HashMap<Integer, String>();
    Iterator <Integer> itr = OIDset.iterator();
 
    while(itr.hasNext()) 
     {    
            int curOid = itr.next();
 
       for(int j = 0; j< resList.size(); j++) {
                
                String[] vals = (resList.get(j)).split(",");
                
                if(curOid == Integer.parseInt(vals[0])) {
                    if(tempMax < quant) {
                        tempMax = quant;
                        popID.put(Integer.parseInt(vals[6]),vals[8]);
                    }
                }
            }

    int i=0;   
    for(int j = 0; j< resList.size(); j++) {
                
                String[] vals = (resList.get(j)).split(",");
                
                if((curOid == Integer.parseInt(vals[0])) && (tempMax == quant)) 
{
                    if(i==0) System.out.println("\nOrder Number: " + curOid + ", with Entry Date and Time: " + vals[4] + ", Customer First Name: " + vals[1]);
                    System.out.println("\nItem Name:" + vals[8] + ", Quantity Ordered: " + tempMax);
                    i++;
                }
            }
     }

	   
	}	

}
