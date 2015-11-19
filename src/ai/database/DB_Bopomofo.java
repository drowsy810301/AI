package ai.database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.Arrays;
import java.util.HashMap;

import org.json.JSONArray;

import ai.exception.BopomofoException;
import ai.net.BopomofoCrawler;

public class DB_Bopomofo {
	private static final int Start_id = 261057;
	private static final int End_id = 470000 ;
	public static void main(String[] argv){
		Connection c = null;
	    Statement stmt = null;
	    HashMap<String, String[]> isRecorded = new HashMap<String,String[]>();
	    try {
	      Class.forName("org.sqlite.JDBC");
	      c = DriverManager.getConnection("jdbc:sqlite:ConceptNet.db");
	      c.setAutoCommit(false);
	      System.out.println("Opened database successfully");

	      stmt = c.createStatement();
	      ResultSet rs = stmt.executeQuery( "SELECT * FROM Relation where id>261057;" );
	      
	      while ( rs.next()) {
	    	  
		         int id = rs.getInt("id");
		         if(id < Start_id)
		        	 continue;
		         /*if(id > End_id)
		        	 break;*/
		         System.out.println(id);
		         String  Start = rs.getString("Start");	         
		         String  End = rs.getString("End");
		         //String  Relation = rs.getString("Relation");
		         String  SurfaceText = rs.getString("SurfaceText");
		         String[] Bopomofo_Start;
		         String[] Bopomofo_End;
		         try{
		        	 if(isRecorded.containsKey(Start)) Bopomofo_Start=isRecorded.get(Start);
		        	 else 
		        	 {
		        		 Bopomofo_Start = BopomofoCrawler.getBopomofo(Start);
		        		 isRecorded.put(Start, Bopomofo_Start);
		        	 }
		        	 if(isRecorded.containsKey(End)) Bopomofo_End=isRecorded.get(End);
		        	 else 
		        	 {
		        		 Bopomofo_End = BopomofoCrawler.getBopomofo(End);
		        		 isRecorded.put(End, Bopomofo_End);
		        	 }
		         }
		         catch (BopomofoException e){
		        	 System.err.println(e.getMessage());
					continue;	
		         }		         
		         
	         	//System.out.println( "ID = " + id );
	         	//System.out.println( "Start = " + Start );
	         	//System.out.println( "End = " + End );	         
	         	//System.out.println( "SurfaceText = " + SurfaceText );	    
	         	
	         	//System.out.println(Arrays.toString(Bopomofo_Start));
	         	//System.out.println(Arrays.toString(Bopomofo_End));
	         	stmt = c.createStatement();
	         	String sql = "UPDATE Relation set Bopomofo_Start = \"" + Arrays.toString(Bopomofo_Start) + "\" where ID=" + String.valueOf(id) + ";";
	            
	         	stmt.executeUpdate(sql);
	            c.commit();
	            stmt = c.createStatement();
	            sql = "UPDATE Relation set Bopomofo_End = \"" + Arrays.toString(Bopomofo_End) + "\" where ID=" + String.valueOf(id) + ";";
	            stmt.executeUpdate(sql);
	            c.commit();	        	
	         	
	      	}
	      	
	      	rs.close();
	      	stmt.close();
	      	c.close();
	    } catch ( Exception e ) {
	    	System.err.println( e.getClass().getName() + ": " + e.getMessage() );
	    	System.exit(0);
	    }
	    System.out.println("Operation done successfully");		
	}
}
