import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

public class BirthDateAnalyzer {

    private ZodiacService zodiacService;

    public BirthDateAnalyzer(ZodiacService zodiacService) {
        this.zodiacService = zodiacService;
    }

    public Map<String, String> analyzeBirthDate(LocalDate birthDate) {
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
        String zodiacSign = zodiacService.determineZodiacSign(birthDate);
        String element = zodiacService.determineZodiacElement(zodiacSign);
        analysis.put("Стихия знака", element);

        // Восточный гороскоп
        String chineseSign = zodiacService.determineChineseZodiac(birthDate.getYear());
        analysis.put("Восточный знак", chineseSign);

        // Сезон рождения
        String season = determineSeason(birthDate.getMonthValue());
        analysis.put("Сезон рождения", season);

        return analysis;
    }

    private int calculateDestinyNumber(int sum) {
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

    private String determineSeason(int month) {
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