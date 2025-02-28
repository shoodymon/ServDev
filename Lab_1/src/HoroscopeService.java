import java.util.HashMap;
import java.util.Map;

public class HoroscopeService {

    private static final String BASE_HOROSCOPE_URL = "https://horoscopes.rambler.ru/";

    public String getBaseUrl() {
        return BASE_HOROSCOPE_URL;
    }

    public String getZodiacUrl(String zodiacSign) {
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

    public String getZodiacInfo(String zodiacSign) {
        Map<String, String> info = new HashMap<>();
        info.put("Овен", "Стихия: Огонь. \nУправляющая планета: Марс. \nХарактеристики: энергичность, инициативность, смелость, импульсивность.");
        info.put("Телец", "Стихия: Земля. \nУправляющая планета: Венера. \nХарактеристики: надежность, терпение, практичность, упрямство.");
        info.put("Близнецы", "Стихия: Воздух. \nУправляющая планета: Меркурий. \nХарактеристики: общительность, любознательность, адаптивность, непостоянство.");
        info.put("Рак", "Стихия: Вода. \nУправляющая планета: Луна. \nХарактеристики: эмоциональность, интуиция, заботливость, чувствительность.");
        info.put("Лев", "Стихия: Огонь. \nУправляющая планета: Солнце. \nХарактеристики: уверенность, творчество, великодушие, гордость.");
        info.put("Дева", "Стихия: Земля. \nУправляющая планета: Меркурий. \nХарактеристики: аналитичность, практичность, внимание к деталям, критичность.");
        info.put("Весы", "Стихия: Воздух. \nУправляющая планета: Венера. \nХарактеристики: дипломатичность, справедливость, гармония, нерешительность.");
        info.put("Скорпион", "Стихия: Вода. \nУправляющие планеты: Марс и Плутон. \nХарактеристики: страстность, решительность, проницательность, интенсивность.");
        info.put("Стрелец", "Стихия: Огонь. \nУправляющая планета: Юпитер. \nХарактеристики: оптимизм, искренность, любовь к свободе, импульсивность.");
        info.put("Козерог", "Стихия: Земля. \nУправляющая планета: Сатурн. \nХарактеристики: целеустремленность, ответственность, дисциплина, консерватизм.");
        info.put("Водолей", "Стихия: Воздух. \nУправляющие планеты: Уран и Сатурн. \nХарактеристики: изобретательность, независимость, оригинальность, эксцентричность.");
        info.put("Рыбы", "Стихия: Вода. \nУправляющие планеты: Нептун и Юпитер. \nХарактеристики: интуитивность, сострадание, творчество, мечтательность.");

        return info.getOrDefault(zodiacSign, "Информация недоступна.");
    }
}