// HashMap и Map используются для хранения пар ключ-значение (результаты математических операций)
// Scanner используется для получения ввода с клавиатуры
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

public class DataProcessor {

    public static void main(String[] args) {
        // Создаем объект Scanner для получения ввода с клавиатуры
        Scanner scanner = new Scanner(System.in);

        System.out.println("Добро пожаловать в программу обработки данных!");

        System.out.print("Введите ваше имя: ");
        String name = scanner.nextLine();

        int age = 0;
        double num1 = 0;
        double num2 = 0;

        try {
            System.out.print("Введите ваш возраст: ");
            age = Integer.parseInt(scanner.nextLine());

            System.out.print("Введите первое число: ");
            num1 = Double.parseDouble(scanner.nextLine());

            System.out.print("Введите второе число: ");
            num2 = Double.parseDouble(scanner.nextLine());
        } catch (NumberFormatException e) {
            System.out.println("Ошибка: введены некорректные числовые данные!");
            scanner.close();
            return;
        }

        String greeting = processName(name);
        String ageCategory = processAge(age);
        Map<String, String> mathResults = processNumbers(num1, num2);

        System.out.println("\nРезультаты обработки:");
        System.out.println(greeting);
        System.out.println("Возрастная категория: " + ageCategory);

        System.out.println("\nМатематические операции:");
        for (Map.Entry<String, String> entry : mathResults.entrySet()) {
            System.out.println(entry.getKey() + ": " + entry.getValue());
        }

        System.out.println("\nСводная информация:");
        System.out.println("------------------------------");
        System.out.println("Имя: " + name);
        System.out.println("Возраст: " + age + " лет (" + ageCategory + ")");
        System.out.println("Введенные числа: " + num1 + " и " + num2);
        System.out.println("------------------------------");

        scanner.close();
    }

    private static String processName(String name) {
        if (name == null || name.trim().isEmpty()) {
            return "Привет, незнакомец!";
        } else if (name.length() < 3) {
            return "Привет, " + name + "! Какое короткое имя!";
        } else {
            return "Привет, " + name + "! Рад встрече!";
        }
    }

    private static String processAge(int age) {
        if (age < 0) {
            return "Некорректный возраст";
        } else if (age < 18) {
            return "Несовершеннолетний";
        } else if (age < 30) {
            return "Молодой";
        } else if (age < 60) {
            return "Взрослый";
        } else {
            return "Пожилой";
        }
    }

    private static Map<String, String> processNumbers(double num1, double num2) {
        Map<String, String> results = new HashMap<>();

        results.put("Сумма", String.valueOf(num1 + num2));
        results.put("Разность", String.valueOf(num1 - num2));
        results.put("Произведение", String.valueOf(num1 * num2));

        if (num2 != 0) {
            results.put("Деление", String.valueOf(num1 / num2));
        } else {
            results.put("Деление", "Невозможно (деление на ноль)");
        }

        results.put("Квадрат первого числа", String.valueOf(Math.pow(num1, 2)));
        results.put("Квадрат второго числа", String.valueOf(Math.pow(num2, 2)));
        results.put("Среднее арифметическое", String.valueOf((num1 + num2) / 2));

        return results;
    }
}