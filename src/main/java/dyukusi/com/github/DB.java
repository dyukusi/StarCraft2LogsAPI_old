package dyukusi.com.github;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DB {
    private String serverName;
    private String databaseName;
    private String userName;
    private String password;

    DB(String serverName, String databaseName, String userName, String password) {
        this.serverName = serverName;
        this.databaseName = databaseName;
        this.userName = userName;
        this.password = password;
    }

    Connection connect() {
        Connection con = null;

        try {
            Class.forName("com.mysql.jdbc.Driver").newInstance();
            String url = "jdbc:mysql://" + this.serverName + "/" + this.databaseName + "?useSSL=false&requireSSL=false";
            con = DriverManager.getConnection(url, this.userName, this.password);
            con.setAutoCommit(false);
            con.setReadOnly(true);
            return con;
        } catch (SQLException e) {
            System.out.println("Connection Failed. : " + e.toString());
        } catch (ClassNotFoundException e) {
            System.out.println("Could not load db driver " + e);
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InstantiationException e) {
            e.printStackTrace();
        }

        return con;
    }
}
