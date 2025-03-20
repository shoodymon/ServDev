import java.sql.*;

public class HotelDatabaseApp {

    // Параметры подключения - обновите их вашими реальными данными
    private static final String URL = "jdbc:sqlserver://localhost;databaseName=Hotels";
    private static final String USER = "YourUsername";
    private static final String PASSWORD = "YourPassword";

    public static void main(String[] args) {
        try {
            // Загружаем драйвер JDBC для SQL Server
            Class.forName("com.microsoft.sqlserver.jdbc.SQLServerDriver");

            // Устанавливаем соединение
            Connection connection = DriverManager.getConnection(URL, USER, PASSWORD);
            System.out.println("Успешное подключение к базе данных!");

            // Пример: Запрос на получение всех клиентов
            getClients(connection);

            // Пример: Запрос на получение всех комнат
            getRooms(connection);

            // Закрываем соединение
            connection.close();
            System.out.println("Соединение закрыто.");

        } catch (ClassNotFoundException e) {
            System.out.println("Драйвер JDBC не найден: " + e.getMessage());
        } catch (SQLException e) {
            System.out.println("Ошибка подключения к базе данных: " + e.getMessage());
        }
    }

    /**
     * Метод для получения всех клиентов
     */
    private static void getClients(Connection connection) throws SQLException {
        String query = "SELECT * FROM Clients";
        Statement statement = connection.createStatement();
        ResultSet resultSet = statement.executeQuery(query);

        System.out.println("\n--- Клиенты ---");
        System.out.println("Код клиента\tФамилия\t\tИмя\t\tОтчество\tНомер паспорта");
        System.out.println("-----------------------------------------------------------");

        // Обрабатываем результаты
        while (resultSet.next()) {
            String clientCode = resultSet.getString("ClientCode");
            String lastName = resultSet.getString("LastName");
            String firstName = resultSet.getString("FirstName");
            String middleName = resultSet.getString("MiddleName");
            String passportNumber = resultSet.getString("PassportNumber");

            System.out.println(clientCode + "\t\t" + lastName + "\t\t" +
                    firstName + "\t\t" + middleName + "\t\t" + passportNumber);
        }

        resultSet.close();
        statement.close();
    }

    /**
     * Метод для получения всех комнат с их типами
     */
    private static void getRooms(Connection connection) throws SQLException {
        String query = "SELECT r.RoomCode, r.Number, rt.Type, r.Floor, r.WindowCount, rt.Price " +
                "FROM Rooms r JOIN RoomTypes rt ON r.TypeCode = rt.TypeCode";
        Statement statement = connection.createStatement();
        ResultSet resultSet = statement.executeQuery(query);

        System.out.println("\n--- Комнаты с типами ---");
        System.out.println("Код комнаты\tНомер\tТип\tЭтаж\tОкна\tЦена");
        System.out.println("---------------------------------------------------");

        // Обрабатываем результаты
        while (resultSet.next()) {
            String roomCode = resultSet.getString("RoomCode");
            String number = resultSet.getString("Number");
            String type = resultSet.getString("Type");
            int floor = resultSet.getInt("Floor");
            int windows = resultSet.getInt("WindowCount");
            double price = resultSet.getDouble("Price");

            System.out.println(roomCode + "\t\t" + number + "\t" + type + "\t" +
                    floor + "\t" + windows + "\t" + price);
        }

        resultSet.close();
        statement.close();
    }
}