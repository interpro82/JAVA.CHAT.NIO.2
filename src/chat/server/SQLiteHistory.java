package chat.server;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import static chat.server.Constants.*;

class SQLiteHistory {
	
	/** Connects to the MySQL database.
	 * @return
	 * @throws SQLException
	 * @throws ClassNotFoundException 
	 */
	private static Connection getConnection() throws SQLException, ClassNotFoundException {
		Connection conn = null;
		Class.forName("org.sqlite.JDBC");
		conn = DriverManager.getConnection("jdbc:sqlite:" + DB_FILE);
		return conn;
	}
		
	/** Writes the message to the SQLite DB for history.
	 * @param sender
	 * @param reciever
	 * @param text
	 * @throws SQLException
	 * @throws ClassNotFoundException 
	 */
	static void insertQuery(String sender,String reciever,String text) throws SQLException, ClassNotFoundException{
		Connection myCon = getConnection();
		DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm");
		Date date = new Date();
		String time = dateFormat.format(date);
		
		String query = " INSERT INTO main.History (Sender,Reciever,Text,Date)"
						+ " VALUES ('" + sender + "','" + reciever + "','" + text + "','"
						+ time + "');";
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
	
}
