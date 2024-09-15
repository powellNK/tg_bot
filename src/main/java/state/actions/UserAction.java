package state.actions;

import domain.User;
import handlers.MessageHandler;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import services.UserService;
import state.UserState;

import java.util.List;

public class UserAction implements Executor {
    @Override
    public void execute(Long telegramId, Integer messageId, String callbackData, UserState userState, MessageHandler handler, UserService userService) throws TelegramApiException {
        handler.sendMessage(telegramId, getUsers(userService).toString());
        handler.createMainMenu(telegramId);
    }

    private StringBuilder getUsers(UserService userService) {
        List<User> users = userService.getUsers();
        StringBuilder usersList = new StringBuilder();
        if (users.isEmpty()) {
            usersList.append("Пользователи отсутствуют");
        } else {
            for (User user : users) {
                usersList.append(STR."\{user.toString()}\n ");
            }
        }
        return usersList;
    }
}
