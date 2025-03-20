import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;


public class Main {
    public static void main(String[] args) {

        // Create a variable for the connection string.
        String connectionUrl = "jdbc:sqlserver://localhost:1433;databaseName=Hotel;user=shoody;password=1234;trustServerCertificate=true";
        try (Connection con = DriverManager.getConnection(connectionUrl); Statement stmt = con.createStatement();) {
            String SQL = "SELECT * FROM Clients";
            ResultSet rs = stmt.executeQuery(SQL);

            while (rs.next()) {
                System.out.println(
                        "ID: " + rs.getString("ClientCode") +
                                ", Фамилия: " + rs.getString("LastName") +
                                ", Имя: " + rs.getString("FirstName") +
                                ", Отчество: " + rs.getString("MiddleName") +
                                ", Номер паспорта: " + rs.getString("PassportNumber")
                );
            }
        }
        // Handle any errors that may have occurred.
        catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
