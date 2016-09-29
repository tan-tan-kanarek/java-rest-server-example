package com.example;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;

public class Install {

	public static void main(String[] args) {
		Connection c = null;
		Statement stmt = null;
		try {
			Class.forName("org.sqlite.JDBC");
			c = DriverManager.getConnection("jdbc:sqlite:test.db");
			System.out.println("Opened database successfully");

			stmt = c.createStatement();
			String sql = "CREATE TABLE users (id INTEGER PRIMARY KEY, created_at INTEGER, updated_at INTEGER, first_name TEXT, last_name TEXT, email TEXT, status INTEGER)";
			stmt.executeUpdate(sql);
			stmt.close();
			
			c.close();
		} catch (Exception e) {
			System.err.println(e.getClass().getName() + ": " + e.getMessage());
			System.exit(0);
		}
		System.out.println("Table created successfully");
	}

}
