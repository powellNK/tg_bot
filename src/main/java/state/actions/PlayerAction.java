package state.actions;

import domain.Player;
import handlers.MessageHandler;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import services.UserService;
import state.UserState;

import java.io.IOException;
import java.util.List;

public class PlayerAction implements Executor {
    @Override
    public void execute(Long telegramId, Integer messageId, String callbackData, UserState userState, MessageHandler handler, UserService userService) throws TelegramApiException, IOException {
        int currentSeason = userState.getCurrentSeason(telegramId);
            handler.sendMessage(telegramId, getPlayers(callbackData, currentSeason, userService).toString());
            handler.createMainMenu(telegramId);
    }

    private StringBuilder getPlayers(String teamId, int currentSeason, UserService userService) {
        return buildPlayerList(userService.getPlayers(currentSeason, Short.parseShort(teamId)));
    }

    private StringBuilder buildPlayerList(List<Player> players) {
        StringBuilder list = new StringBuilder();
        if (players.isEmpty()) {
            list.append("Игроки отсутствуют");
        } else {
            for (Player player : players) {
                list.append(player).append("\n");
            }
        }
        return list;
    }
}
