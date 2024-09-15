package state;

import handlers.MessageHandler;
import lombok.Getter;
import lombok.Setter;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import services.UserService;
import state.actions.*;
import java.io.IOException;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;


@Getter
@Setter
public class UserState {
    private static UserState instance = null;
    private ConcurrentHashMap<String, Executor> callbacks = new ConcurrentHashMap<>();
    private ConcurrentHashMap<Long, Integer> userSeasons = new ConcurrentHashMap<>();
    private ConcurrentMap<Long, Boolean> blockedUsers = new ConcurrentHashMap<>();

    @Getter
    @Setter
    private boolean updateInProgress = false;

    private UserState() {
        callbacks.put("YEAR", new YearCallbackAction());
        callbacks.put("SHOW", new ShowGamesAction());
        callbacks.put("DOWNLOAD", new DownloadUpdatesAction());
        callbacks.put("MENUTEAM", new TeamsAction());
        callbacks.put("USERS", new UserAction());
        callbacks.put("BACK", new BackToMenuAction());
        callbacks.put("ROSTER", new PlayerAction());
        callbacks.put("SHOWTEAM", new ShowTeamAction());
    }

    public void blockUser(Long userId) {
        blockedUsers.put(userId, true);
    }

    public void unblockUser(Long userId) {
        blockedUsers.remove(userId);
    }

    public boolean isUserBlocked(Long userId) {
        return blockedUsers.containsKey(userId);
    }

    public Set<Long> getBlockedUsers() {
        return blockedUsers.keySet();
    }

    public static UserState getInstance() {
        if (instance == null) {
            instance = new UserState();
        }
        return instance;
    }

    public int getCurrentSeason(Long userId) {
        return userSeasons.get(userId);
    }

    public void setCurrentSeason(Long userId, int season) {
        userSeasons.put(userId, season);
    }


    public void handleCallback(Long telegramId, Integer messageId, String callbackData, MessageHandler handler, UserService userService) throws TelegramApiException, IOException {
        if (updateInProgress) {
            handler.sendMessage(telegramId, "Обновление данных еще не завершено. Пожалуйста, подождите.");
            return;
        }

        if (isUserBlocked(telegramId)) {
            handler.sendMessage(telegramId, "Вы заблокированы на время обновления.");
            return;
        }
        String[] parts = callbackData.split("_", 2);
        if (parts.length < 2) {
            handler.sendMessage(telegramId, "Неизвестная команда.");
            return;
        }
        String prefix = parts[0];
        String parameter = parts[1];
        Executor action = callbacks.get(prefix);

        if (action != null) {
            action.execute(telegramId, messageId, parameter, this, handler, userService);
        } else {
            handler.sendMessage(telegramId, "Неизвестная команда.");
        }
    }
}