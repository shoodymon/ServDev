import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DbConnectionManager {
    private static final String CONNECTION_URL = "jdbc:sqlserver://localhost:1433;databaseName=Hotel;user=shoody;password=1234;trustServerCertificate=true";

    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(CONNECTION_URL);
    }
}