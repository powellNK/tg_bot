package state.actions;

import domain.Game;
import handlers.MessageHandler;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import services.UserService;
import state.UserState;

import java.util.List;

public class ShowGamesAction implements Executor {
    @Override
    public void execute(Long telegramId, Integer messageId, String callbackData, UserState userState, MessageHandler handler, UserService userService) throws TelegramApiException {
        int currentSeason = userState.getCurrentSeason(telegramId);
        StringBuilder gameData;
        switch (callbackData) {
            case "ALL_GAMES":
                gameData = getAllGames(currentSeason, userService);
                break;
            case "FUTURE_GAMES":
                gameData = getUpcomingGames(currentSeason, userService);
                break;
            case "LAST_GAMES":
                gameData = getPastGames(currentSeason, userService);
                break;
            default:
                if (callbackData.startsWith("GAMES_")) {
                    // Обработка команд, связанных с играми определенной команды
                    String teamId = callbackData.split("_")[1];
                    gameData = getGamesTeam(teamId, currentSeason, userService);
                    showGames(telegramId, gameData, handler);
                } else {
                    gameData = new StringBuilder("Неизвестная команда.");
                }
                break;
        }
        showGames(telegramId, gameData, handler);
    }

    void showGames(Long telegramId, StringBuilder gameData, MessageHandler handler) throws TelegramApiException {
        handler.sendMessage(telegramId, gameData.toString());
        handler.createMainMenu(telegramId);
    }

    private StringBuilder getPastGames(int currentSeason, UserService userService) {
        return buildGameList(userService.getPastGames(currentSeason));
    }

    public StringBuilder getUpcomingGames(int currentSeason, UserService userService) {
        return buildGameList(userService.getUpcomingGames(currentSeason));
    }

    public StringBuilder getAllGames(int currentSeason, UserService userService) {
        return buildGameList(userService.getAllGames(currentSeason));
    }

    private StringBuilder getGamesTeam(String teamId, int currentSeason, UserService userService) {
        return buildGameList(userService.getGamesTeam(currentSeason, Short.parseShort(teamId)));

    }

    private StringBuilder buildGameList(List<Game> games) {
        StringBuilder list = new StringBuilder();
        if (games.isEmpty()) {
            list.append("Игры отсутствуют");
        } else {
            for (Game game : games) {
                list.append(game).append("\n");
            }
        }
        return list;
    }
}
