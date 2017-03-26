//package servlet;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.DataOutputStream;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.TimerTask;
import java.util.Timer;
import java.util.Calendar;

import java.net.URI;
import java.net.URISyntaxException;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Date;
import java.sql.Timestamp;

import javax.servlet.ServletException;
import javax.servlet.http.*;

import org.json.JSONException;
import org.json.JSONObject;

import java.net.HttpURLConnection;
import java.net.URL;
import javax.net.ssl.HttpsURLConnection;

import java.text.SimpleDateFormat; 

import java.math.BigInteger;

public class CreateConnection extends HttpServlet 
{
	public JSONObject jsonObject;
	//private String connectionId;
	private static Connection con;
	private double asOfVersion = 37.0;
	private String SoapEndPoint = "/services/Soap/m/37.0";
	
	public void doPost(HttpServletRequest req, HttpServletResponse res) throws ServletException , IOException
	{
		//String empName = req.getParameter("Connection_Id");
		jsonObject = new JSONObject();
	
		res.setContentType("application/json");
		PrintWriter out = res.getWriter();
		
		//Read post request data
		String paramValue = readRequestBody(req);
		
		if(paramValue != null)
		{
			createRecord(paramValue);
		}
	
		out.print(jsonObject.toString());
		out.close();
	}
	private String readRequestBody(HttpServletRequest request) 
	{
		try 
		{
			// Read from request
			StringBuilder buffer = new StringBuilder();
			BufferedReader reader = request.getReader();
			String line;
			while ((line = reader.readLine()) != null) 
			{
				buffer.append(line);
			}
			return buffer.toString();
		} 
		catch (Exception ex1) 
		{
			try
			{
				jsonObject.put("status",  "Failed");	
				jsonObject.put("message",  "Connection creation failed due to JSON parsing");	
			}
			catch(JSONException ex2)
			{}
		}
		return null;
	}
	private void createRecord(String paramValue)
	{
		try
		{
			con = getConnection();
			
			JSONObject requestJSON = new JSONObject(paramValue);
			
			String Connection_Id = requestJSON.getString("Connection_Id");
			String Access_Token = requestJSON.getString("Access_Token");
			String Organization_Id = requestJSON.getString("Organization_Id");
			String Organization_URL = requestJSON.getString("Organization_URL");
			String project = requestJSON.getString("project");
			String Refresh_Token = requestJSON.getString("Refresh_Token");
			String Development_Environment_URL = requestJSON.getString("Development_Environment_URL");
			String Site_URL = requestJSON.getString("Site_URL");
							
			if(Connection_Id == null || Connection_Id.equals("") || Access_Token == null || Access_Token.equals("") || Organization_URL == null || Organization_URL.equals("") || Refresh_Token == null || Refresh_Token.equals("") || Development_Environment_URL == null || Development_Environment_URL.equals("") || Site_URL == null || Site_URL.equals(""))
			{
				try
				{
					jsonObject.put("status",  "Failed");
					jsonObject.put("message",  "Connection creation failed due to some missing parameters, please try again");
				}
				catch(JSONException ex2)
				{}
				return;
			}
			
			java.util.Date utilDate  = new java.util.Date();
			java.sql.Timestamp sqlDate = new java.sql.Timestamp(utilDate.getTime());
			
			String insertQuery = "INSERT INTO Connection(Connection_Id, Access_Token, Organization_Id, Organization_URL,  project, Refresh_Token, Modified_Date, Development_Environment_URL, Site_URL)"+ " VALUES (?, ?, ?, ?, ?, ?, ?,?,?)";
			PreparedStatement pstmt = con.prepareStatement(insertQuery);
			
			pstmt.setString(1, Connection_Id);
			pstmt.setString(2, Access_Token);
			pstmt.setString(3, Organization_Id);
			pstmt.setString(4, Organization_URL);
			pstmt.setString(5, project);
			pstmt.setString(6, Refresh_Token);
			pstmt.setTimestamp(7, sqlDate);
			pstmt.setString(8, Development_Environment_URL);
			//pstmt.setString(9, Site_URL);
			
			int rowCount = pstmt.executeUpdate();
			pstmt.close();
			
				
			if(rowCount > 0)
			{
				jsonObject.put("status",  "Success");	
				jsonObject.put("message",  "Connection created successfully");	
			}
			
			//close database connection
			con.close();
		}
		catch(Exception ex1)
		{
			try
			{
				jsonObject.put("status",  "Failed");
				jsonObject.put("message",  ex1.getMessage());
			}
			catch(JSONException ex2)
			{}
		}
	}
	
	private Connection getConnection() throws URISyntaxException, SQLException 
	{
		URI dbUri = new URI(System.getenv("DATABASE_URL"));
	
		String username = dbUri.getUserInfo().split(":")[0];
		String password = dbUri.getUserInfo().split(":")[1];
		String dbUrl = "jdbc:postgresql://" + dbUri.getHost() + dbUri.getPath();

		return DriverManager.getConnection(dbUrl, username, password);
    }
}
