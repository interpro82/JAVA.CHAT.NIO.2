package chat.server;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import static chat.server.Constants.*;

/**
 * @author Iliya Nikolov
 * The class is used to write the chat history to MySQL.
 *
 */
class MySqlHistory {
	
	/** Connects to the MySQL database.
	 * @return
	 * @throws SQLException
	 */
	private static Connection getConnection() throws SQLException {
	    Connection conn = null;
	    String dbURL = "jdbc:" + DBMS + "://" + DB_SERVER_NAME + ":" + DB_PORT_NUMBER + "/";
	    conn = DriverManager.getConnection(dbURL, MYSQL_USERNAME, MYSQL_PASSWORD);
	    System.out.println("Connected to database");
	    return conn;
	}
		
	/** Writes the message to the MySQL DB for history.
	 * @param sender
	 * @param reciever
	 * @param text
	 * @throws SQLException
	 */
	static void insertQuery(String sender,String reciever,String text) throws SQLException{
		Connection myCon = getConnection();
		int senderID = getUserID(sender,myCon);
		int recieverID = getUserID(reciever,myCon);
		String query = "INSERT INTO `chat_history`.`History`"+ 
						"(`reciever`, `sender`, `text`,`date`,`time`)"+ 
						"VALUES ('" + senderID + "','" + recieverID +
						"', '"  + text + "', curdate(), curtime());";
		Statement stmt = null;
		stmt = myCon.createStatement();
		int affectedRows = stmt.executeUpdate(query);
		if (affectedRows == 0) {
		     throw new SQLException("Adding history failed, no rows affected.");
		    } else {
		       	System.out.println(affectedRows + " row(s) have been modified.");
		    }
		myCon.close();
	}
	
	/** Get's chat user's ID from his name.
	 * @param userName
	 * @param DBcon
	 * @return
	 * @throws SQLException
	 */
	private static int getUserID(String userName,Connection DBcon) throws SQLException{
		int iD = 0;
		String query = "SELECT `id` FROM chat_history.Users where `name`='" + userName + "';"; 
		Statement selectID = DBcon.createStatement();
		ResultSet resultSet = selectID.executeQuery(query);
		if (resultSet.next()) {
			iD = resultSet.getInt(1);
		}
		return iD;
	}
}
