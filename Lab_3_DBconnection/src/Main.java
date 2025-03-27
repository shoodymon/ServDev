import java.sql.Date;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;
import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        boolean exit = false;

        while (!exit) {
            System.out.println("\n==== Система управления гостиницей ====");
            System.out.println("1. Список всех клиентов");
            System.out.println("2. История заселений клиента");
            System.out.println("3. Информация о комнатах");
            System.out.println("4. Свободные комнаты на дату");
            System.out.println("5. Текущие заселения");
            System.out.println("0. Выход");
            System.out.print("\nВыберите опцию: ");

            int choice = scanner.nextInt();
            scanner.nextLine(); // очистка буфера

            try {
                switch (choice) {
                    case 1:
                        displayAllClients();
                        break;
                    case 2:
                        System.out.print("Введите ID клиента: ");
                        int clientId = scanner.nextInt();
                        displayClientHistory(clientId);
                        break;
                    case 3:
                        displayAllRooms();
                        break;
                    case 4:
                        System.out.print("Введите дату (yyyy-MM-dd): ");
                        String dateStr = scanner.nextLine();
                        LocalDate localDate = LocalDate.parse(dateStr);
                        Date sqlDate = Date.valueOf(localDate);
                        displayAvailableRooms(sqlDate);
                        break;
                    case 5:
                        displayCurrentStays();
                        break;
                    case 0:
                        exit = true;
                        System.out.println("Программа завершена.");
                        break;
                    default:
                        System.out.println("Неверный выбор. Попробуйте снова.");
                }
            } catch (SQLException e) {
                System.err.println("Ошибка базы данных: " + e.getMessage());
                e.printStackTrace();
            } catch (Exception e) {
                System.err.println("Ошибка: " + e.getMessage());
                e.printStackTrace();
            }
        }

        scanner.close();
    }

    private static void displayAllClients() throws SQLException {
        System.out.println("\n==== Список всех клиентов ====");
        List<String> clients = ClientQueries.getAllClients();
        for (String client : clients) {
            System.out.println(client);
        }
    }

    private static void displayClientHistory(int clientId) throws SQLException {
        System.out.println("\n==== История заселений клиента ====");
        List<String> history = ClientQueries.getClientHistory(clientId);
        if (history.isEmpty()) {
            System.out.println("История не найдена для клиента с ID: " + clientId);
        } else {
            for (String stay : history) {
                System.out.println(stay);
            }
        }
    }

    private static void displayAllRooms() throws SQLException {
        System.out.println("\n==== Информация о комнатах ====");
        List<String> rooms = RoomQueries.getAllRoomsWithType();
        for (String room : rooms) {
            System.out.println(room);
        }
    }

    private static void displayAvailableRooms(Date date) throws SQLException {
        System.out.println("\n==== Свободные комнаты на " + date + " ====");
        List<String> availableRooms = RoomQueries.getAvailableRooms(date);
        if (availableRooms.isEmpty()) {
            System.out.println("Нет свободных комнат на указанную дату.");
        } else {
            for (String room : availableRooms) {
                System.out.println(room);
            }
        }
    }

    private static void displayCurrentStays() throws SQLException {
        System.out.println("\n==== Текущие заселения ====");
        List<String> currentStays = StatusQueries.getCurrentStays();
        if (currentStays.isEmpty()) {
            System.out.println("В настоящее время нет активных заселений.");
        } else {
            for (String stay : currentStays) {
                System.out.println(stay);
            }
        }
    }
}