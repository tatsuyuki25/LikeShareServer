package com.likeshare.server;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;

public class SQLConnect {
	private Connection con = null; // Database objects
	// 連接object
	private Statement stat = null;
	// 執行,傳入之sql為完整字串
	private ResultSet rs = null;
	// 結果集
	private PreparedStatement pst = null;

	// 執行,傳入之sql為預儲之字申,需要傳入變數之位置

	public SQLConnect() {
		try {
			Class.forName("com.mysql.jdbc.Driver");
			// 註冊driver
			con = DriverManager
					.getConnection(
							"jdbc:mysql://localhost/sdt?useUnicode=true&characterEncoding=utf8",
							"sdt", "xMfLSvWh6W3Ej7GY");
			// 取得connection

			// jdbc:mysql://localhost/test?useUnicode=true&characterEncoding=Big5
			// localhost是主機名,sdt是database名
			// useUnicode=true&characterEncoding=utf8使用的編碼

		} catch (ClassNotFoundException e) {
			System.out.println("DriverClassNotFound :" + e.toString());
		}// 有可能會產生sqlexception
		catch (SQLException x) {
			System.out.println("Exception :" + x.toString());
		}
	}

	public boolean checkUser(String account, String pass) {
		try {
			stat = con.createStatement();
			rs = stat.executeQuery("SELECT * FROM user_info WHERE ui_account='"
					+ account + "' AND ui_pass=SHA1('" + pass + "')");
			if (rs.next()) {
				return true;
			} else {
				return false;
			}
		} catch (SQLException e) {
			e.printStackTrace();
			return false;
		} finally {
			Close();
		}
	}
	
	public boolean signUp(String account, String pass, String name)
	{
		try {
			stat = con.createStatement();
			rs = stat.executeQuery("SELECT * FROM user_info WHERE ui_account='"
					+ account + "'");
			if (rs.next()) {
				return false;
			} else {
				pst = con
						.prepareStatement("INSERT INTO user_info(ui_account,ui_pass,ui_id,ui_type) VALUES(?,SHA1(?),?,?)");
				pst.setString(1,account);
				pst.setString(2,pass);
				pst.setString(3,name);
				pst.setInt(4,1);
				pst.executeUpdate();
				return true;
			}
		} catch (SQLException e) {
			e.printStackTrace();
			return false;
		} finally {
			Close();
		}
	}

	public boolean addFriend(String myAccount,String fAccount)
	{
		try {
			stat = con.createStatement();
			rs = stat.executeQuery("SELECT * FROM user_info WHERE ui_account='"
					+ fAccount + "'");
			if (!rs.next()) {
				return false;
			} else {
				pst = con
						.prepareStatement("INSERT INTO friend(friend_ui_account,friend_account,friend_ui_id) VALUES(?,?,?)");
				pst.setString(1,myAccount);
				pst.setString(2,fAccount);
				pst.setString(3,rs.getString("ui_id"));
				pst.executeUpdate();
				stat = con.createStatement();
				rs = stat.executeQuery("SELECT * FROM user_info WHERE ui_account='"
						+ myAccount + "'");
				rs.next();
				pst = con
						.prepareStatement("INSERT INTO friend(friend_ui_account,friend_account,friend_ui_id) VALUES(?,?,?)");
				pst.setString(1,fAccount);
				pst.setString(2,myAccount);
				pst.setString(3,rs.getString("ui_id"));
				pst.executeUpdate();
				return true;
			}
		} catch (SQLException e) {
			e.printStackTrace();
			return false;
		} finally {
			Close();
		}
	}
	
	public void FriendRename(String myAccount,String fAccount,String name)
	{
		try {
			pst = con
					.prepareStatement("UPDATE friend SET friend_ui_id=?  WHERE friend_ui_account=? AND friend_account=?");
			pst.setString(1,name);
			pst.setString(2,myAccount);
			pst.setString(3,fAccount);
			pst.executeUpdate();
			
		} catch (SQLException e) {
			System.out.println("InsertDB Exception :" + e.toString());
		} finally {
			Close();
		}
	}
	
	public void FriendDelete(String myAccount,String fAccount)
	{
		try {
			pst = con
					.prepareStatement("DELETE FROM friend  WHERE friend_ui_account=? AND friend_account=?");
			pst.setString(1,myAccount);
			pst.setString(2,fAccount);
			pst.executeUpdate();
			
		} catch (SQLException e) {
			System.out.println("InsertDB Exception :" + e.toString());
		} finally {
			Close();
		}
	}
	
	
	public void setLogin(String account, String mac, String ip, String name) {
		try {
			stat = con.createStatement();
			rs = stat
					.executeQuery("SELECT * FROM device WHERE device_ui_account='"
							+ account + "' AND device_mac='" + mac + "'");
			if (rs.next()) {
				pst = con
						.prepareStatement("UPDATE device,user_info SET device_type=1,ui_type=1,device_ip=? WHERE device_ui_account=ui_account AND"
								+ " ui_account=? AND device_mac=?");
				pst.setString(1, ip);
				pst.setString(2, account);
				pst.setString(3, mac);
				pst.executeUpdate();
			} else {
				pst = con
						.prepareStatement("INSERT INTO device(device_ui_account,device_id,device_name,device_mac,device_ip,device_type) VALUES(?,?,?,?,?,?)");
				pst.setString(1, account);
				pst.setString(2, name);
				pst.setString(3, name);
				pst.setString(4, mac);
				pst.setString(5, ip);
				pst.setInt(6, 1);
				pst.executeUpdate();
				pst = con
						.prepareStatement("UPDATE user_info SET ui_type=1 WHERE ui_account=?");
				pst.setString(1, account);
				pst.executeUpdate();
			}
		} catch (SQLException e) {
			System.out.println("InsertDB Exception :" + e.toString());
		} finally {
			Close();
		}
	}

	public void logout(String mac) {
		try {
			pst = con
					.prepareStatement("UPDATE device SET device_type=0 WHERE device_mac=?");
			pst.setString(1, mac);
			pst.executeUpdate();
			pst = con
					.prepareStatement("UPDATE device,user_info,(SELECT device_ui_account AS acc,SUM(device_type)AS type FROM device,(SELECT device_ui_account AS ac FROM device " +
							"WHERE device_mac=?) AS C WHERE device_ui_account=C.ac) AS B SET ui_type=IF(B.type>0,1,0)  WHERE ui_account=B.acc");
			pst.setString(1, mac);
			pst.executeUpdate();
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			Close();
		}
	}

	public ArrayList<String> getDevices(String account, String mac) {
		ArrayList<String> devices = new ArrayList<String>();
		try {
			if(!mac.equals("1"))
			{
				stat = con.createStatement();
				rs = stat
					.executeQuery("SELECT * FROM device WHERE device_ui_account='"
							+ account + "' AND device_mac ='" + mac + "'");
				rs.next();
				devices.add(rs.getString("device_id") + ","
					+ rs.getString("device_name") + ","
					+ rs.getString("device_mac") + ","
					+ rs.getString("device_type"));
			}
			stat = con.createStatement();
			rs = stat
					.executeQuery("SELECT * FROM device WHERE device_ui_account='"
							+ account + "' AND device_mac !='" + mac + "'");
			while (rs.next()) {
				devices.add(rs.getString("device_id") + ","
						+ rs.getString("device_name") + ","
						+ rs.getString("device_mac") + ","
						+ rs.getString("device_type"));
			}
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			Close();
		}
		return devices;
	}

	public ArrayList<String> getFriends(String account) {
		ArrayList<String> friends = new ArrayList<String>();
		try {
			stat = con.createStatement();
			rs = stat
					.executeQuery("SELECT * FROM friend,user_info WHERE friend_ui_account='"
							+ account + "' AND friend_account=ui_account");
			while (rs.next()) {
				friends.add(rs.getString("friend_account") + ","
						+ rs.getString("friend_ui_id") + ","
						+ rs.getString("ui_type"));
			}
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			Close();
		}
		return friends;
	}

	public String getLoginDevices(String account, String mac) {
		String devices = "";
		try {
			stat = con.createStatement();
			rs = stat
					.executeQuery("SELECT * FROM device WHERE device_ui_account='"
							+ account
							+ "' AND device_mac !='"
							+ mac
							+ "' AND device_type=1");
			while (rs.next()) {
				devices += rs.getString("device_mac");
				if (rs.next())
					devices += ",";
			}
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			Close();
		}
		if (!devices.equals(""))
			return devices;
		else
			return "no";
	}

	private void Close() {
		try {
			if (rs != null) {
				rs.close();
				rs = null;
			}
			if (stat != null) {
				stat.close();
				stat = null;
			}
			if (pst != null) {
				pst.close();
				pst = null;
			}
		} catch (SQLException e) {
			System.out.println("Close Exception :" + e.toString());
		}
	}
}
