package com.ak4311.metroid.utility;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import com.ak4311.metroid.Metroid;
import com.badlogic.gdx.utils.Disposable;

public class DatabaseUtility {
	private static PreparedStatement updateScoreStmt;
	private static PreparedStatement updatePlaytimeStmt;
	
	public DatabaseUtility() {
		try {
			updateScoreStmt = Metroid.CONNECTION.prepareStatement("UPDATE players SET score = score + ? WHERE username = ?");
			updatePlaytimeStmt = Metroid.CONNECTION.prepareStatement("UPDATE players SET playtime = playtime + ? / 1000 WHERE username = ?");
		} 
		catch (SQLException e) {
			e.printStackTrace();
			System.exit(0);
		}
	}
	
	public static void updatePlayerScore(int sessionScore) {
		try {
			updateScoreStmt.setInt(1, sessionScore);
			updateScoreStmt.setString(2, Metroid.USERNAME);
			updateScoreStmt.executeUpdate();
		} 
		catch (SQLException e) {
			e.printStackTrace();
			System.exit(0);
		}
	}
	
	public static void updatePlayerPlaytime(long sessionPlaytime) {
		try {
			updatePlaytimeStmt.setLong(1, sessionPlaytime - Metroid.SESSION_STARTTIME);
			updatePlaytimeStmt.setString(2, Metroid.USERNAME);
			updatePlaytimeStmt.executeUpdate();
		} 
		catch (SQLException e) {
			e.printStackTrace();
			System.exit(0);
		}
	}

	public void dispose() {
		try {
			updatePlayerScore(Metroid.SESSION_SCORE);
			updatePlayerPlaytime(System.currentTimeMillis());
			ResultSet rs = Metroid.CONNECTION.createStatement().executeQuery("SELECT * FROM players");
			while (rs.next()) System.out.println(rs.getString(1) + "\t" + rs.getInt(2) + "\t" + rs.getInt(3) + "\t" + rs.getTimestamp(4));
			
			updateScoreStmt.close();
			updatePlaytimeStmt.close();
		} 
		catch (SQLException e) {
			e.printStackTrace();
		}
	}
}
