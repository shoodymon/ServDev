import java.awt.Desktop;
import java.net.URI;

public class WebOpener {

    public void openWebsite(String url) {
        try {
            if (Desktop.isDesktopSupported() && Desktop.getDesktop().isSupported(Desktop.Action.BROWSE)) {
                Desktop.getDesktop().browse(new URI(url));
                System.out.println("Браузер открыт с адресом: " + url);
            } else {
                System.out.println("Автоматическое открытие браузера не поддерживается на вашей системе.");
                System.out.println("Пожалуйста, посетите следующий адрес вручную: " + url);
            }
        } catch (Exception e) {
            System.out.println("Не удалось открыть браузер автоматически.");
            System.out.println("Пожалуйста, посетите следующий адрес вручную: " + url);
        }
    }
}