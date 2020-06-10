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

/**
 * This class is client to mysql database
 * @author Kira
 *
 */
public class MySQLConnection {
	private Connection conn;
	
	/**
	 * creating connection
	 */
	public MySQLConnection() {
		try {
			Class.forName("com.mysql.cj.jdbc.Driver").newInstance();
			conn = DriverManager.getConnection(MySQLDBUtil.URL);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	/**
	 * closing connection
	 */
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
	
	/**
	 * Insert userid and selected item into table
	 * @param userId - the user selected item
	 * @param item - the selected item
	 */
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
	
	/**
	 * Delete user id and selected item from table
	 * @param userId - the user selected item
	 * @param itemId - selected item id
	 */
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
	
	/**
	 * Insert selected item into table items
	 * @param item - selected item
	 */
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
	
	/**
	 * Got the user's favorite item id set
	 * @param userId - authorized user
	 * @return set of user's selected item id
	 */
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
	
	/**
	 * Got the user's favorite item set
	 * @param userId - authorized user
	 * @return set of user's selected item
	 */
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
	
	/**
	 * Got item keywords set
	 * @param itemId - selected item id
	 * @return set of keywords
	 */
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
	
	/**
	 * Got full name of user
	 * @param userId - authorized user 
	 * @return string full name of user
	 */
	public String getFullname(String userId) {
		if (conn == null) {
			System.err.println("DB connection failed");
			return "";
		}
		
		String name = "";
		String sql = "SELECT first_name, last_name FROM users WHERE user_id = ?";
		try {
			PreparedStatement statement = conn.prepareStatement(sql);
			statement.setString(1, userId);
			ResultSet rs = statement.executeQuery();
			if(rs.next()) {
				name = rs.getString("first_name") + " " + rs.getString("last_name");
			}
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return name;
	}
	
	/**
	 * Checking if user is register
	 * @param userId - user input id
	 * @param password - unser input password
	 * @return true for match in database, otherwise false
	 */
	public boolean verifyLogin(String userId, String password) {
		if (conn == null) {
			System.err.println("DB connection failed");
			return false;
		}
		
		String sql = "SELECT user_id FROM users WHERE user_id = ? AND password = ?";
		try {
			PreparedStatement statement = conn.prepareStatement(sql);
			statement.setString(1, userId);
			statement.setString(2, password);
			ResultSet rs = statement.executeQuery();
			if(rs.next()) {
				return true;
			}
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return false;
	}
	
	/**
	 * Insert registered user into database
	 * @param userId - user input id
	 * @param password - user input password
	 * @param firstname - user's firstname
	 * @param lastname - user's lastname
	 * @return true for successfully insert. otherwise false
	 */
	public boolean addUser(String userId, String password, String firstname, String lastname) {
		if (conn == null) {
			System.err.println("DB connection failed");
			return false;
		}
		
		String sql = "INSERT IGNORE INTO users VALUE (?,?,?,?)";
		PreparedStatement statement;
		try {
			statement = conn.prepareStatement(sql);
			statement.setString(1, userId);
			statement.setString(2, password);
			statement.setString(3, firstname);
			statement.setString(4, lastname);
			
			return statement.executeUpdate() == 1;
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return false;
	}
}
