import java.util.Map;
import java.util.Scanner;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class Main {
    public static void main(String[] args) {
        // Создаем объект Scanner для получения ввода с клавиатуры
        Scanner scanner = new Scanner(System.in);
        UserInputHandler inputHandler = new UserInputHandler(scanner);

        System.out.println("Добро пожаловать в астрологическую программу!");

        // Получение имени пользователя
        String name = inputHandler.getName();
        System.out.println(inputHandler.processName(name));

        // Получение даты рождения
        LocalDate birthDate = inputHandler.getBirthDate();

        // Расчет возраста
        AgeProcessor ageProcessor = new AgeProcessor();
        int age = ageProcessor.calculateAge(birthDate);
        String ageCategory = ageProcessor.processAge(age);

        // Определение знака зодиака
        ZodiacService zodiacService = new ZodiacService();
        String zodiacSign = zodiacService.determineZodiacSign(birthDate);

        // Получение информации о гороскопе
        HoroscopeService horoscopeService = new HoroscopeService();
        String zodiacUrl = horoscopeService.getZodiacUrl(zodiacSign);
        String zodiacInfo = horoscopeService.getZodiacInfo(zodiacSign);

        // Анализ даты рождения
        BirthDateAnalyzer birthAnalyzer = new BirthDateAnalyzer(zodiacService);
        Map<String, String> birthDateAnalysis = birthAnalyzer.analyzeBirthDate(birthDate);

        // Вывод результатов
        System.out.println("\nРезультаты анализа:");
        System.out.println("------------------------------");
        System.out.println("Имя: " + name);
        System.out.println("Дата рождения: " + birthDate.format(DateTimeFormatter.ofPattern("dd.MM.yyyy")));
        System.out.println("Возраст: " + age + " лет (" + ageCategory + ")");
        System.out.println("Знак зодиака: " + zodiacSign);
        System.out.println("------------------------------");

        System.out.println("\nКраткая информация о знаке:");
        System.out.println(zodiacInfo);

        System.out.println("\nАнализ даты рождения:");
        for (Map.Entry<String, String> entry : birthDateAnalysis.entrySet()) {
            System.out.println(entry.getKey() + ": " + entry.getValue());
        }

        // Обработка меню
        WebOpener webOpener = new WebOpener();
        boolean exit = false;
        while (!exit) {
            System.out.println("\nВыберите действие:");
            System.out.println("1. Получить ежедневный гороскоп на сайте");
            System.out.println("2. Получить астрологическую совместимость");
            System.out.println("3. Просмотреть финансовый гороскоп");
            System.out.println("4. Выход из программы");

            System.out.print("Ваш выбор: ");
            String choice = scanner.nextLine().trim();

            switch (choice) {
                case "1":
                    webOpener.openWebsite(zodiacUrl);
                    break;
                case "2":
                    webOpener.openWebsite(horoscopeService.getBaseUrl() + "sovmestimost-znakov-zodiaka/");
                    break;
                case "3":
                    webOpener.openWebsite(zodiacUrl + "/career/");
                    break;
                case "4":
                    exit = true;
                    System.out.println("Спасибо за использование нашей программы!");
                    break;
                default:
                    System.out.println("Неверный выбор. Пожалуйста, попробуйте снова.");
            }
        }

        scanner.close();
    }
}