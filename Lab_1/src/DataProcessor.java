// Scanner используется для получения ввода с клавиатуры
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;
import java.time.LocalDate;
import java.time.Period;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.awt.Desktop;
import java.net.URI;

public class DataProcessor {

    // Константа для хранения базового URL
    private static final String BASE_HOROSCOPE_URL = "https://horoscopes.rambler.ru/";

    public static void main(String[] args) {
        // Создаем объект Scanner для получения ввода с клавиатуры
        Scanner scanner = new Scanner(System.in);

        System.out.println("Добро пожаловать в астрологическую программу!");

        System.out.print("Введите ваше имя: ");
        String name = scanner.nextLine();

        String greeting = processName(name);
        System.out.println(greeting);

        // Получение даты рождения
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

        // Расчет возраста
        int age = calculateAge(birthDate);
        String ageCategory = processAge(age);

        // Определение знака зодиака
        String zodiacSign = determineZodiacSign(birthDate);
        String zodiacUrl = getZodiacUrl(zodiacSign);

        // Базовая информация о знаке зодиака
        String zodiacInfo = getZodiacInfo(zodiacSign);

        // Анализ даты рождения
        Map<String, String> birthDateAnalysis = analyzeBirthDate(birthDate);

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

        // Меню выбора действий
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
                    openWebsite(zodiacUrl);
                    break;
                case "2":
                    openWebsite(BASE_HOROSCOPE_URL + "sovmestimost-znakov-zodiaka/");
                    break;
                case "3":
                    openWebsite(zodiacUrl + "/career/");
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

    private static String processName(String name) {
        if (name == null || name.trim().isEmpty()) {
            return "Привет, незнакомец!";
        } else if (name.length() < 3) {
            return "Привет, " + name + "! Какое короткое имя!";
        } else {
            return "Привет, " + name + "! Рад познакомиться с вами!";
        }
    }

    private static int calculateAge(LocalDate birthDate) {
        return Period.between(birthDate, LocalDate.now()).getYears();
    }

    private static String processAge(int age) {
        if (age < 0) {
            return "Некорректный возраст";
        } else if (age < 18) {
            return "Юный искатель";
        } else if (age < 30) {
            return "Молодая душа";
        } else if (age < 45) {
            return "Зрелый дух";
        } else if (age < 60) {
            return "Мудрый наставник";
        } else {
            return "Хранитель мудрости";
        }
    }

    private static String determineZodiacSign(LocalDate birthDate) {
        int day = birthDate.getDayOfMonth();
        int month = birthDate.getMonthValue();

        if ((month == 3 && day >= 21) || (month == 4 && day <= 20)) {
            return "Овен";
        } else if ((month == 4 && day >= 21) || (month == 5 && day <= 21)) {
            return "Телец";
        } else if ((month == 5 && day >= 22) || (month == 6 && day <= 21)) {
            return "Близнецы";
        } else if ((month == 6 && day >= 22) || (month == 7 && day <= 22)) {
            return "Рак";
        } else if ((month == 7 && day >= 23) || (month == 8 && day <= 23)) {
            return "Лев";
        } else if ((month == 8 && day >= 24) || (month == 9 && day <= 23)) {
            return "Дева";
        } else if ((month == 9 && day >= 24) || (month == 10 && day <= 23)) {
            return "Весы";
        } else if ((month == 10 && day >= 24) || (month == 11 && day <= 22)) {
            return "Скорпион";
        } else if ((month == 11 && day >= 23) || (month == 12 && day <= 22)) {
            return "Стрелец";
        } else if ((month == 12 && day >= 23) || (month == 1 && day <= 20)) {
            return "Козерог";
        } else if ((month == 1 && day >= 21) || (month == 2 && day <= 19)) {
            return "Водолей";
        } else {
            return "Рыбы";
        }
    }

    private static String getZodiacUrl(String zodiacSign) {
        Map<String, String> zodiacUrls = new HashMap<>();
        zodiacUrls.put("Овен", BASE_HOROSCOPE_URL + "aries");
        zodiacUrls.put("Телец", BASE_HOROSCOPE_URL + "taurus");
        zodiacUrls.put("Близнецы", BASE_HOROSCOPE_URL + "gemini");
        zodiacUrls.put("Рак", BASE_HOROSCOPE_URL + "cancer");
        zodiacUrls.put("Лев", BASE_HOROSCOPE_URL + "leo");
        zodiacUrls.put("Дева", BASE_HOROSCOPE_URL + "virgo");
        zodiacUrls.put("Весы", BASE_HOROSCOPE_URL + "libra");
        zodiacUrls.put("Скорпион", BASE_HOROSCOPE_URL + "scorpio");
        zodiacUrls.put("Стрелец", BASE_HOROSCOPE_URL + "sagittarius");
        zodiacUrls.put("Козерог", BASE_HOROSCOPE_URL + "capricorn");
        zodiacUrls.put("Водолей", BASE_HOROSCOPE_URL + "aquarius");
        zodiacUrls.put("Рыбы", BASE_HOROSCOPE_URL + "pisces");

        return zodiacUrls.getOrDefault(zodiacSign, BASE_HOROSCOPE_URL);
    }

    private static String getZodiacInfo(String zodiacSign) {
        Map<String, String> info = new HashMap<>();
        info.put("Овен", "Стихия: Огонь. Управляющая планета: Марс. Характеристики: энергичность, инициативность, смелость, импульсивность.");
        info.put("Телец", "Стихия: Земля. Управляющая планета: Венера. Характеристики: надежность, терпение, практичность, упрямство.");
        info.put("Близнецы", "Стихия: Воздух. Управляющая планета: Меркурий. Характеристики: общительность, любознательность, адаптивность, непостоянство.");
        info.put("Рак", "Стихия: Вода. Управляющая планета: Луна. Характеристики: эмоциональность, интуиция, заботливость, чувствительность.");
        info.put("Лев", "Стихия: Огонь. Управляющая планета: Солнце. Характеристики: уверенность, творчество, великодушие, гордость.");
        info.put("Дева", "Стихия: Земля. Управляющая планета: Меркурий. Характеристики: аналитичность, практичность, внимание к деталям, критичность.");
        info.put("Весы", "Стихия: Воздух. Управляющая планета: Венера. Характеристики: дипломатичность, справедливость, гармония, нерешительность.");
        info.put("Скорпион", "Стихия: Вода. Управляющие планеты: Марс и Плутон. Характеристики: страстность, решительность, проницательность, интенсивность.");
        info.put("Стрелец", "Стихия: Огонь. Управляющая планета: Юпитер. Характеристики: оптимизм, искренность, любовь к свободе, импульсивность.");
        info.put("Козерог", "Стихия: Земля. Управляющая планета: Сатурн. Характеристики: целеустремленность, ответственность, дисциплина, консерватизм.");
        info.put("Водолей", "Стихия: Воздух. Управляющие планеты: Уран и Сатурн. Характеристики: изобретательность, независимость, оригинальность, эксцентричность.");
        info.put("Рыбы", "Стихия: Вода. Управляющие планеты: Нептун и Юпитер. Характеристики: интуитивность, сострадание, творчество, мечтательность.");

        return info.getOrDefault(zodiacSign, "Информация недоступна.");
    }

    private static Map<String, String> analyzeBirthDate(LocalDate birthDate) {
        Map<String, String> analysis = new HashMap<>();

        // День недели
        String[] daysOfWeek = {"понедельник", "вторник", "среда", "четверг", "пятница", "суббота", "воскресенье"};
        int dayOfWeekIndex = birthDate.getDayOfWeek().getValue() - 1;
        analysis.put("День недели рождения", daysOfWeek[dayOfWeekIndex]);

        // Число судьбы (нумерология)
        int day = birthDate.getDayOfMonth();
        int month = birthDate.getMonthValue();
        int year = birthDate.getYear();

        int destinyNumber = calculateDestinyNumber(day + month + year);
        analysis.put("Число судьбы", String.valueOf(destinyNumber));

        // Стихия знака зодиака
        String zodiacSign = determineZodiacSign(birthDate);
        String element = determineZodiacElement(zodiacSign);
        analysis.put("Стихия знака", element);

        // Восточный гороскоп
        String chineseSign = determineChineseZodiac(birthDate.getYear());
        analysis.put("Восточный знак", chineseSign);

        // Сезон рождения
        String season = determineSeason(birthDate.getMonthValue());
        analysis.put("Сезон рождения", season);

        return analysis;
    }

    private static int calculateDestinyNumber(int sum) {
        while (sum > 9) {
            int newSum = 0;
            while (sum > 0) {
                newSum += sum % 10;
                sum /= 10;
            }
            sum = newSum;
        }
        return sum;
    }

    private static String determineZodiacElement(String zodiacSign) {
        if (zodiacSign.equals("Овен") || zodiacSign.equals("Лев") || zodiacSign.equals("Стрелец")) {
            return "Огонь";
        } else if (zodiacSign.equals("Телец") || zodiacSign.equals("Дева") || zodiacSign.equals("Козерог")) {
            return "Земля";
        } else if (zodiacSign.equals("Близнецы") || zodiacSign.equals("Весы") || zodiacSign.equals("Водолей")) {
            return "Воздух";
        } else {
            return "Вода";
        }
    }

    private static String determineChineseZodiac(int year) {
        String[] signs = {"Обезьяна", "Петух", "Собака", "Свинья", "Крыса", "Бык", "Тигр", "Кролик", "Дракон", "Змея", "Лошадь", "Коза"};
        return signs[(year - 4) % 12];
    }

    private static String determineSeason(int month) {
        if (month >= 3 && month <= 5) {
            return "Весна";
        } else if (month >= 6 && month <= 8) {
            return "Лето";
        } else if (month >= 9 && month <= 11) {
            return "Осень";
        } else {
            return "Зима";
        }
    }

    private static void openWebsite(String url) {
        try {
            openWebPage(url);
            System.out.println("Браузер открыт с адресом: " + url);
            System.out.println("Если страница не открылась автоматически, посетите указанный адрес вручную.");
        } catch (Exception e) {
            System.out.println("Не удалось открыть браузер автоматически.");
            System.out.println("Пожалуйста, посетите следующий адрес вручную: " + url);
        }
    }

    private static void openWebPage(String url) throws Exception {
        URI uri = new URI(url);
        if (Desktop.isDesktopSupported() && Desktop.getDesktop().isSupported(Desktop.Action.BROWSE)) {
            Desktop.getDesktop().browse(uri);
        } else {
            // Альтернативные способы открытия браузера на различных ОС
            String os = System.getProperty("os.name").toLowerCase();
            Runtime rt = Runtime.getRuntime();

            if (os.contains("win")) {
                // Windows
                rt.exec("rundll32 url.dll,FileProtocolHandler " + url);
            } else if (os.contains("mac")) {
                // macOS
                rt.exec("open " + url);
            } else if (os.contains("nix") || os.contains("nux")) {
                // Linux
                String[] browsers = {"google-chrome", "firefox", "mozilla", "opera", "epiphany", "konqueror", "netscape", "links", "lynx"};

                StringBuilder cmd = new StringBuilder();
                for (int i = 0; i < browsers.length; i++) {
                    if (i == 0) {
                        cmd.append(String.format("%s \"%s\"", browsers[i], url));
                    } else {
                        cmd.append(String.format(" || %s \"%s\"", browsers[i], url));
                    }
                }

                rt.exec(new String[]{"sh", "-c", cmd.toString()});
            }
        }
    }
}