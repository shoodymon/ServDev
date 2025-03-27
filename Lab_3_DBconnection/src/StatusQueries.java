import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class StatusQueries {

    /**
     * Получить информацию о текущих заселениях
     */
    public static List<String> getCurrentStays() throws SQLException {
        List<String> currentStays = new ArrayList<>();
        String sql = "SELECT Clients.FirstName, Clients.LastName, Clients.PassportNumber, " +
                "Rooms.Number, Rooms.Floor, RoomTypes.Type, RoomTypes.Price, " +
                "Status.CheckInDate, Status.CheckOutDate " +
                "FROM Status " +
                "JOIN Clients ON Status.ClientCode = Clients.ClientCode " +
                "JOIN Rooms ON Status.RoomCode = Rooms.RoomCode " +
                "JOIN RoomTypes ON Rooms.TypeCode = RoomTypes.TypeCode " +
                "WHERE Status.CheckOutDate >= GETDATE()";
        try (Connection conn = DbConnectionManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                String stayInfo = String.format("%s %s (Паспорт: %s), Комната: %s (%s), Этаж: %d, Цена: %.2f, Период: %s - %s",
                        rs.getString("FirstName"),
                        rs.getString("LastName"),
                        rs.getString("PassportNumber"),
                        rs.getString("Number"),
                        rs.getString("Type"),
                        rs.getInt("Floor"),
                        rs.getDouble("Price"),
                        rs.getDate("CheckInDate"),
                        rs.getDate("CheckOutDate"));
                currentStays.add(stayInfo);
            }
        }
        return currentStays;
    }
}