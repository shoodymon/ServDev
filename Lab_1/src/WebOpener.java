import java.awt.Desktop;
import java.io.IOException;
import java.net.URI;

public class WebOpener {
    public static void openWebsite(String url) {
        try {
            Desktop.getDesktop().browse(URI.create(url));
        } catch (IOException e) {
            System.out.println("Не удалось открыть сайт.");
        }
    }
}
