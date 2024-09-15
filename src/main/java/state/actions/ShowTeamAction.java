package state.actions;

import handlers.MessageHandler;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import services.UserService;
import state.UserState;

import java.io.IOException;

public class ShowTeamAction implements Executor {
    @Override
    public void execute(Long telegramId, Integer messageId, String callbackData, UserState userState, MessageHandler handler, UserService userService) throws TelegramApiException, IOException {
        String teamId = callbackData.split("_")[1];
        handler.createMenuForSpecificTeam(telegramId, messageId, teamId);
    }
}

