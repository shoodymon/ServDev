import java.util.Scanner;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

public class UserInputHandler {
    private Scanner scanner;

    public UserInputHandler(Scanner scanner) {
        this.scanner = scanner;
    }

    public String getName() {
        System.out.print("Введите ваше имя: ");
        return scanner.nextLine();
    }

    public LocalDate getBirthDate() {
        LocalDate birthDate = null;
        while (birthDate == null) {
            try {
                System.out.print("Введите дату рождения (дд.мм.гггг): ");
                String birthDateStr = scanner.nextLine();
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy");
                birthDate = LocalDate.parse(birthDateStr, formatter);
            } catch (DateTimeParseException e) {
                System.out.println("Ошибка: введен некорректный формат даты. Попробуйте снова.");
            }
        }
        return birthDate;
    }

    public String processName(String name) {
        if (name == null || name.trim().isEmpty()) {
            return "Привет, незнакомец!";
        } else if (name.length() < 3) {
            return "Привет, " + name + "! Какое короткое имя!";
        } else {
            return "Привет, " + name + "! Рад познакомиться с вами!";
        }
    }
}