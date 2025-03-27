import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class RoomQueries {

    /**
     * Получить информацию о всех комнатах с указанием типа
     */
    public static List<String> getAllRoomsWithType() throws SQLException {
        List<String> rooms = new ArrayList<>();
        String sql = "SELECT Rooms.RoomCode, Rooms.Number, Rooms.Floor, Rooms.WindowCount, RoomTypes.Type, RoomTypes.Price " +
                "FROM Rooms " +
                "JOIN RoomTypes ON Rooms.TypeCode = RoomTypes.TypeCode";

        try (Connection conn = DbConnectionManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                String roomInfo = String.format("Номер: %s, Этаж: %d, Тип: %s, Цена: %.2f, Окна: %d",
                        rs.getString("Number"),
                        rs.getInt("Floor"),
                        rs.getString("Type"),
                        rs.getDouble("Price"),
                        rs.getInt("WindowCount"));
                rooms.add(roomInfo);
            }
        }
        return rooms;
    }

    /**
     * Получить список свободных комнат на указанную дату
     */
    public static List<String> getAvailableRooms(java.sql.Date date) throws SQLException {
        List<String> availableRooms = new ArrayList<>();
        String sql = "SELECT Rooms.Number, Rooms.Floor, RoomTypes.Type, RoomTypes.Price " +
                "FROM Rooms " +
                "JOIN RoomTypes ON Rooms.TypeCode = RoomTypes.TypeCode " +
                "WHERE Rooms.RoomCode NOT IN (" +
                "    SELECT RoomCode FROM Status " +
                "    WHERE ? BETWEEN CheckInDate AND CheckOutDate" +
                ")";
        try (Connection conn = DbConnectionManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setDate(1, date);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    String roomInfo = String.format("Номер: %s, Этаж: %d, Тип: %s, Цена: %.2f",
                            rs.getString("Number"),
                            rs.getInt("Floor"),
                            rs.getString("Type"),
                            rs.getDouble("Price"));
                    availableRooms.add(roomInfo);
                }
            }
        }
        return availableRooms;
    }
}