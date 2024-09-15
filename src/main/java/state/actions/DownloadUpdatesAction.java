package state.actions;

import handlers.MessageHandler;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import services.UserService;
import state.UserState;

import java.io.IOException;

public class DownloadUpdatesAction implements Executor {

    @Override
    public void execute(Long telegramId, Integer messageId, String callbackData, UserState userState, MessageHandler handler, UserService userService) throws TelegramApiException, IOException {
        if (userService.isAdmin(telegramId)) {
            if (userState.isUpdateInProgress()) {
                handler.sendMessage(telegramId, "Обновление уже выполняется. Пожалуйста, подождите.");
            } else {
                userState.setUpdateInProgress(true);
                userState.blockUser(telegramId);
                handler.sendMessage(telegramId, "Обновление данных начато. Пожалуйста, подождите.");

                userService.parsing();

                userState.setUpdateInProgress(false);
                userState.unblockUser(telegramId);

                handler.sendMessage(telegramId, "Данные успешно обновлены.");
                unblockAllUsers(handler, userState); // Разблокировать всех пользователей
            }
        } else {
            handler.sendMessage(telegramId, "У вас нет прав для выполнения этой команды.");
        }
    }

    private void unblockAllUsers(MessageHandler handler, UserState userState) {
        for (Long userId : userState.getBlockedUsers()) {
            if (userState.isUserBlocked(userId)) {
                userState.unblockUser(userId);
                handler.sendMessage(userId, "Обновление данных завершено. Вы можете продолжить.");
            }
        }
    }
}