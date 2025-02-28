import java.time.LocalDate;
import java.time.Period;

public class AgeProcessor {

    public int calculateAge(LocalDate birthDate) {
        return Period.between(birthDate, LocalDate.now()).getYears();
    }

    public String processAge(int age) {
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
}