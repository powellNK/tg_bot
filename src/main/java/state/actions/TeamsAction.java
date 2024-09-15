package state.actions;

import domain.Team;
import handlers.MessageHandler;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import services.UserService;
import state.UserState;

import java.util.List;

public class TeamsAction implements Executor {
    @Override
    public void execute(Long telegramId, Integer messageId, String callbackData, UserState userState, MessageHandler handler, UserService userService) throws TelegramApiException {
        int currentSeason;
        switch (callbackData) {
            case "TEAMS":
                currentSeason = userState.getCurrentSeason(telegramId);
                handler.createMenuWithAllTeams(telegramId, messageId, currentSeason);
                break;
            case "RESULT_SEASON":
                currentSeason = userState.getCurrentSeason(telegramId);
                handler.handleTableResult(telegramId, getTableResult(currentSeason, userService));
                break;
            case "FULL_STATISTICS":
                String fullStatisticsText = getFullStatistic(userService).toString();
                handler.sendMessage(telegramId, fullStatisticsText);
                handler.createMainMenu(telegramId);
                break;
            default:
                if (callbackData.startsWith("STATISTICS_")) {
                    currentSeason = userState.getCurrentSeason(telegramId);
                    // Обработка команд, связанных со статистикой определенной команды
                    String teamId = callbackData.split("_")[1];
                    StringBuilder gameData = getStatisticsTeam(teamId, currentSeason, userService);
                    handler.sendMessage(telegramId, gameData.toString());
                    handler.createMainMenu(telegramId);
                }
        }
    }

    private StringBuilder getTableResult(int currentSeason, UserService userService) {
        return buildTeamList(userService.getTableResult(currentSeason));
    }

    private StringBuilder buildTeamList(List<Team> teams) {
        StringBuilder list = new StringBuilder();
        if (teams.isEmpty()) {
            list.append("Команды отсутствуют");
        } else {
            for (Team team : teams) {
                list.append(team).append("\n");
            }
        }
        return list;
    }

    private StringBuilder getFullStatistic(UserService userService) {
        return buildTeamList(userService.getFullStatistic());
    }

    private StringBuilder getStatisticsTeam(String teamId, int currentSeason, UserService userService) {
        return buildTeamList(userService.getStatisticsTeam(currentSeason, Short.parseShort(teamId)));
    }
}