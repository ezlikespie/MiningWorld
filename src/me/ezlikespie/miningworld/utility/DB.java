package me.ezlikespie.miningworld.utility;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import me.ezlikespie.miningworld.main.Main;

public class DB {

	public static Connection getConnection() {
		return Main.getConnection();
	}
	
	public static void update(String... args) {
		
		String totalUpdateMessage = "";
		for(int i = 0; i < args.length; i++) {
			totalUpdateMessage += args[i];
		}
		
		Statement s = null;
		try {
			s = getConnection().createStatement();
		} catch (SQLException e) {
		}
		
		if(s!=null) {
			
			try {
				s.executeUpdate(totalUpdateMessage);
			} catch (SQLException e) {
			}
			
		}
		
	}
	
	public static Boolean willDB = true;
	
	public static void setWillDB(Boolean f) {
		willDB = f;
	}
	
	public static ResultSet query(String... args) {
		
		if(willDB) {
			String totalUpdateMessage = "";
			for(int i = 0; i < args.length; i++) {
				totalUpdateMessage += args[i];
			}
			
			Statement s = null;
			try {
				s = getConnection().createStatement();
			} catch (SQLException e) {
			}
			
			if(s!=null) {
				
				try {
					ResultSet r = s.executeQuery(totalUpdateMessage);
					return r;
				} catch (SQLException e) {
				}
				
			}
		}
		
		return null;
		
	}
	
	public static Boolean next(ResultSet r) {
		try {
			if(r.next()) {
				return true;
			}
		} catch (SQLException e) {
		}
		return false;
	}
	
	public static Object value(ResultSet r, String columnName) {
		
		try {
			return r.getObject(columnName);
		} catch (SQLException e) {
		}
		
		return null;
	}
	
}
