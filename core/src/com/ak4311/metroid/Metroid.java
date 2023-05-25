package com.ak4311.metroid;

//import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Game;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.Scanner;

import com.ak4311.metroid.utility.DatabaseUtility;

// an Entity-Component-System, ECS, can alleviate some issues with multiple classes having the same attributes.
// e.g. projectiles, collision tiles, enemies, and the player all have Sprite, Texture, Body, and so on, but collision
// tiles don't need Animation. we could have each of these be Entity objects and attach only Components each needs, 
// i.e. Sprite, Body, Animation, and so on. Systems would then process these Components and update the Entities.
// i did not implement an ECS for this project since the goal was to demonstrate three "advanced topics" in Java, and
// incorporating an ECS would've taken too long.

// TODO: make sure to dispose() textures, sprites, etc. where missed.
public class Metroid extends Game {
	public static final boolean DEBUG = true;	// TODO: make this a system parameter
	public static final int THREAD_COUNT = 4;
	
	public static final int WINDOW_WIDTH = 1200;
	public static final int WINDOW_HEIGHT = 800;
	public static final int VIEWPORT_WIDTH = 255;
	public static final int VIEWPORT_HEIGHT = 235;
	public static final float PPM = 16f;
	public static final float WORLD_GRAVITY = 22f;
	
	public static final short WORLD_BIT = 0x0001;
	public static final short SAMUS_BIT = 0x0002;
	public static final short ENEMY_BIT = 0x0004;
	public static final short ITEM_BIT = 0x0008;
	public static final short PROJECTILE_BIT = 0x0010;
	
	public SpriteBatch batch;
	private GameLoop gameLoop;	
	private DatabaseUtility dbUtility;
	
	public static Connection CONNECTION;
	public static String USERNAME;	
	public static final long SESSION_STARTTIME = System.currentTimeMillis();
	public static int SESSION_SCORE = 0;
	
	public Metroid() {
		try {
			CONNECTION = DriverManager.getConnection("jdbc:sqlite:metroid.db");
			System.out.println("Connected to database...");
			
			Statement stmt = CONNECTION.createStatement();
			stmt.execute("CREATE TABLE IF NOT EXISTS players "
				+ "(username		STRING NOT NULL, "
				+ "score 			INTEGER DEFAULT 0, "
				+ "playtime 		BIGINT DEFAULT 0, "
				+ "last_logged_in 	TIMESTAMP, "
				+ "PRIMARY KEY (username))");
			
			System.out.print("Enter username: ");
			Scanner in = new Scanner(System.in);
			String username = in.nextLine();
			
			PreparedStatement pstmt = CONNECTION.prepareStatement("SELECT * FROM players WHERE username = ?");
			pstmt.setString(1, username);
			ResultSet rs = pstmt.executeQuery();
			if (!rs.next()) {
				pstmt = CONNECTION.prepareStatement("INSERT INTO players (username) VALUES (?)");
				pstmt.setString(1, username);
				pstmt.executeUpdate();
			}
			USERNAME = username;
			System.out.println("Logged in as " + username + ". Launching game...");
			
			pstmt = CONNECTION.prepareStatement("UPDATE players SET last_logged_in = ? WHERE username = ?");
			pstmt.setTimestamp(1, new Timestamp(new java.util.Date().getTime()));
			pstmt.setString(2, username);
			pstmt.executeUpdate();
			
			stmt.close();
			pstmt.close();
			rs.close();
			in.close();
			
			dbUtility = new DatabaseUtility();
		} 
		catch (SQLException e) {
			e.printStackTrace();
			System.exit(0);
		}
	}
	
	@Override
	public void create() {
		batch = new SpriteBatch();
		this.gameLoop = new GameLoop(this);
		setScreen(this.gameLoop.getScreen());
	}

	@Override
	public void render() {
		super.render();
	}
	
	@Override
	public void dispose() {
		super.dispose();
		batch.dispose();
		dbUtility.dispose();
		
		try {
			CONNECTION.close();
			System.out.println("Disconnected from database...");
		} 
		catch (SQLException e) {
			e.printStackTrace();
		}
	}
}