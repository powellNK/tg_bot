package state.actions;

import handlers.MessageHandler;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import services.UserService;
import state.UserState;

public class YearCallbackAction implements Executor {
    @Override
    public void execute(Long telegramId, Integer messageId, String callbackData, UserState userState, MessageHandler handler, UserService userService) throws TelegramApiException {
            int season = Integer.parseInt(callbackData);
            userState.setCurrentSeason(telegramId, season);
            handler.createMenuSeason(telegramId,messageId);

    }
}
