import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class ClientQueries {

    /**
     * Получить список всех клиентов
     */
    public static List<String> getAllClients() throws SQLException {
        List<String> clients = new ArrayList<>();
        String sql = "SELECT ClientCode, FirstName, LastName, MiddleName, PassportNumber FROM Clients";

        try (Connection conn = DbConnectionManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                String clientInfo = String.format("ID: %d, %s %s %s, Паспорт: %s",
                        rs.getInt("ClientCode"),
                        rs.getString("FirstName"),
                        rs.getString("LastName"),
                        rs.getString("MiddleName"),
                        rs.getString("PassportNumber"));
                clients.add(clientInfo);
            }
        }
        return clients;
    }

    /**
     * Получить историю заселений конкретного клиента
     */
    public static List<String> getClientHistory(int clientCode) throws SQLException {
        List<String> history = new ArrayList<>();
        String sql = "SELECT Clients.FirstName, Clients.LastName, Rooms.Number, RoomTypes.Type, Archive.CheckInDate, Archive.CheckOutDate " +
                "FROM Archive " +
                "JOIN Clients ON Archive.ClientCode = Clients.ClientCode " +
                "JOIN Rooms ON Archive.RoomCode = Rooms.RoomCode " +
                "JOIN RoomTypes ON Rooms.TypeCode = RoomTypes.TypeCode " +
                "WHERE Clients.ClientCode = ?";

        try (Connection conn = DbConnectionManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, clientCode);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    String stayInfo = String.format("%s %s, Комната: %s (%s), Период: %s - %s",
                            rs.getString("FirstName"),
                            rs.getString("LastName"),
                            rs.getString("Number"),
                            rs.getString("Type"),
                            rs.getDate("CheckInDate"),
                            rs.getDate("CheckOutDate"));
                    history.add(stayInfo);
                }
            }
        }
        return history;
    }
}