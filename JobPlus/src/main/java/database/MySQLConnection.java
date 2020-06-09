package database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashSet;
import java.util.Set;

import entity.Item;
import entity.Item.ItemBuilder;

public class MySQLConnection {
	private Connection conn;
	
	public MySQLConnection() {
		try {
			Class.forName("com.mysql.cj.jdbc.Driver").newInstance();
			conn = DriverManager.getConnection(MySQLDBUtil.URL);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public void close() {
		if(conn != null) {
			try {
				conn.close();
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
	
	public void setFavoriteItems(String userId, Item item) {
		if(conn == null) {
			System.err.println("DB connection failed");
			return;
		}
		saveItem(item);
		String sql = "INSERT INTO history (user_id, item_id) VALUES (?, ?)";
		try {
			PreparedStatement statement = conn.prepareStatement(sql);
			statement.setString(1, userId);
			statement.setString(2, item.getItemId());
			statement.executeUpdate();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	
	public void unsetFavoriteItems(String userId, String itemId) {
		if(conn == null) {
			System.err.println("DB connection failed");
			return;
		}
		String sql = "DELETE FROM history WHERE user_id = ? AND item_id = ?";
		try {
			PreparedStatement statement = conn.prepareStatement(sql);
			statement.setString(1, userId);
			statement.setString(2, itemId);
			statement.executeUpdate();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	
	public void saveItem(Item item) {
		if(conn == null) {
			System.err.println("DB connection failed");
			return;
		}
		String sql = "INSERT IGNORE INTO items VALUES (?,?,?,?,?)";
		try {
			PreparedStatement statement = conn.prepareStatement(sql);
			statement.setString(1, item.getItemId());
			statement.setString(2, item.getName());
			statement.setString(3, item.getAddress());
			statement.setString(4, item.getImageUrl());
			statement.setString(5, item.getUrl());
			statement.executeUpdate();
			
			sql = "INSERT IGNORE INTO keywords VALUES (?,?)";
			statement = conn.prepareStatement(sql);
			statement.setString(1,  item.getItemId());
			for(String keyword : item.getKeywords()) {
				statement.setString(2, keyword);
				statement.executeUpdate();
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	
	public Set<String> getFavoriteItemIds(String userId) {
		if(conn == null) {
			System.err.println("DB connection failed");
			return new HashSet<>();
		}
		
		Set<String> favoriteItems = new HashSet<>();
		try {
			String sql = "SELECT item_id FROM history WHERE user_id = ?";
			PreparedStatement statement = conn.prepareStatement(sql);
			statement.setString(1, userId);
			ResultSet rs = statement.executeQuery();
			while(rs.next()) {
				String itemId = rs.getString("item_id");
				favoriteItems.add(itemId);
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}

		return favoriteItems;
	}
	
	public Set<Item> getFavoriteItems(String userId) {
		if(conn == null) {
			System.err.println("DB connection failed");
			return new HashSet<>();
		}
		Set<Item> favoriteItems = new HashSet<>();
		Set<String> favoriteItemIds = getFavoriteItemIds(userId);
		
		String sql = "SELECT * FROM items WHERE item_id = ?";
		try {
			PreparedStatement statement = conn.prepareStatement(sql);
			for(String itemId : favoriteItemIds) {
				statement.setString(1, itemId);
				ResultSet rs = statement.executeQuery();
				
				ItemBuilder builder = new ItemBuilder();
				if(rs.next()) {
					builder.setItemId(rs.getString("item_id"))
					       .setName(rs.getString("name"))
					       .setAddress(rs.getString("address"))
					       .setImageUrl(rs.getString("image_url"))
					       .setUrl(rs.getString("url"))
					       .setKeywords(getKeywords(itemId));
					favoriteItems.add(builder.build());
				}
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return favoriteItems;
	}
	
	public Set<String> getKeywords(String itemId) {
		if(conn == null) {
			System.err.println("DB connection failed");
			return new HashSet<>();
		}
		Set<String> keywords = new HashSet<>();
		String sql = "SELECT keyword FROM keywords WHERE item_id = ?";
		try {
			PreparedStatement statement = conn.prepareStatement(sql);
			statement.setString(1, itemId);
			ResultSet rs = statement.executeQuery();
			while(rs.next()) {
				String keyword = rs.getString("keyword");
				keywords.add(keyword);
			}
		} catch(SQLException e) {
			e.printStackTrace();
		}
		return keywords;
	}
}
