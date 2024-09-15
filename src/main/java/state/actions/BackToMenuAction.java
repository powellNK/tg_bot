package state.actions;

import handlers.MessageHandler;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import services.UserService;
import state.UserState;

import java.io.IOException;

public class BackToMenuAction implements Executor {
    @Override
    public void execute(Long telegramId, Integer messageId, String callbackData, UserState userState, MessageHandler handler, UserService userService) throws TelegramApiException, IOException {
        if (callbackData.equals("TO_SEASON_MENU")) {
            handler.createMenuSeason(telegramId, messageId);
        } else {
            handler.backToMenu(telegramId, messageId, userService.isAdmin(telegramId));
        }
    }
}
