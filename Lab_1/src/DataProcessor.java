// Scanner используется для получения ввода с клавиатуры
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;
import java.time.LocalDate;
import java.time.Period;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

public class DataProcessor {

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

        // Предсказание для знака зодиака
        String prediction = generatePrediction(zodiacSign);

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

        System.out.println("\nАстрологический прогноз:");
        System.out.println(prediction);

        System.out.println("\nАнализ даты рождения:");
        for (Map.Entry<String, String> entry : birthDateAnalysis.entrySet()) {
            System.out.println(entry.getKey() + ": " + entry.getValue());
        }

        System.out.println("\nСпасибо за использование нашей программы!");
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

    private static String generatePrediction(String zodiacSign) {
        Map<String, String> predictions = new HashMap<>();
        predictions.put("Овен", "Ваша энергия и инициативность помогут вам добиться успеха. Не бойтесь рисковать, но будьте внимательны к деталям. Сейчас благоприятное время для новых начинаний.");
        predictions.put("Телец", "Ваше упорство принесет плоды. Финансовая ситуация стабилизируется, а отношения с близкими укрепятся. Уделите внимание своему здоровью и питанию.");
        predictions.put("Близнецы", "Ваша коммуникабельность откроет новые двери. Будьте готовы к неожиданным предложениям и встречам. Интеллектуальная активность сейчас на пике.");
        predictions.put("Рак", "Семейные ценности выходят на первый план. Интуиция поможет вам принять правильные решения. Берегите эмоциональные ресурсы и учитесь отдыхать.");
        predictions.put("Лев", "Ваша харизма привлекает внимание окружающих. Используйте это для достижения целей. Творческий потенциал сейчас высок – не упустите возможность его реализовать.");
        predictions.put("Дева", "Аналитические способности помогут решить сложные задачи. Будьте внимательны к деталям, но не забывайте о целостной картине. Здоровье требует заботы.");
        predictions.put("Весы", "Гармония и баланс – ваши ключевые слова. Отношения развиваются в положительном направлении. Не бойтесь принимать решения, опираясь на интуицию.");
        predictions.put("Скорпион", "Ваша интенсивность и страсть помогут преодолеть любые препятствия. Будьте готовы к глубоким трансформациям. Доверяйте своей интуиции.");
        predictions.put("Стрелец", "Ваш оптимизм заразителен. Новые горизонты открываются перед вами. Путешествия и обучение принесут особую пользу. Расширяйте свои границы.");
        predictions.put("Козерог", "Ваша дисциплина и упорство приведут к успеху. Карьерные достижения не за горами. Уделите внимание долгосрочным планам и целям.");
        predictions.put("Водолей", "Ваша оригинальность мышления – ключ к успеху. Социальные связи укрепляются, новые идеи рождаются. Технологии сыграют важную роль в вашей жизни.");
        predictions.put("Рыбы", "Ваша чувствительность и интуиция на пике. Творческий потенциал высок. Духовные практики принесут особую пользу. Берегите свои эмоциональные ресурсы.");

        return predictions.getOrDefault(zodiacSign, "Индивидуальный прогноз недоступен.");
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
}