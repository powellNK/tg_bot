import handlers.MessageHandler;
import infrastructure.Db.DbManager;
import lombok.extern.slf4j.Slf4j;
import org.telegram.telegrambots.longpolling.TelegramBotsLongPollingApplication;
import secrets.SecretManager;

@Slf4j
public class Main {
    public static void main(String[] args) {
        var db = new DbManager();
        try (TelegramBotsLongPollingApplication botsApplication = new TelegramBotsLongPollingApplication()) {
            MessageHandler messageHandler = db.getMessageHandler();
            botsApplication.registerBot(SecretManager.getToken(), messageHandler);
            Thread.currentThread().join();
        } catch (Exception e) {
            log.error("Error while registering bot", e);
        }
    }
}
