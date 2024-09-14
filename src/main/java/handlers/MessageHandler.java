package handlers;

import domain.Game;
import domain.Player;
import domain.Team;
import domain.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.telegram.telegrambots.client.okhttp.OkHttpTelegramClient;
import org.telegram.telegrambots.longpolling.util.LongPollingSingleThreadUpdateConsumer;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageReplyMarkup;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.message.Message;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardRow;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.meta.generics.TelegramClient;
import secrets.SecretManager;
import services.UserService;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;


public class MessageHandler implements LongPollingSingleThreadUpdateConsumer {
    private final TelegramClient client = new OkHttpTelegramClient(SecretManager.getToken());
    private final Logger logger = LoggerFactory.getLogger(MessageHandler.class);
    private final UserService userService;
    private static final int MAX_MESSAGE_LENGTH = 4096;
    private int season;

    public MessageHandler(UserService userService) {
        this.userService = userService;
    }


    @Override
    public void consume(Update update) {

        if (update.hasMessage()) {
            Message message = update.getMessage();
            Long telegramId = message.getChatId();
            String telegramUsername = message.getFrom().getUserName();
            userService.authorization(telegramId, telegramUsername);
            sendMessage(telegramId, "Привет. Я бот, следящий за высшей лигой Б. " +
                    "Присоединяйся! Извини, ввод с клавиатуры невозможен, воспользуйся меню");
            try {
                createMainMenu(telegramId);
            } catch (TelegramApiException e) {
                throw new RuntimeException(e);
            }
        }
        if (update.hasCallbackQuery()) {
            try {
                handleCallbackQuery(update.getCallbackQuery());
            } catch (TelegramApiException | IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

    private void handleCallbackQuery(CallbackQuery callbackQuery) throws TelegramApiException, IOException {
        String callbackData = callbackQuery.getData();
        Long telegramId = callbackQuery.getMessage().getChatId();
        boolean isAdmin = userService.isAdmin(telegramId);
        Integer messageId = callbackQuery.getMessage().getMessageId();


        switch (callbackData) {
            case "YEAR2024":
                season = Integer.parseInt(callbackQuery.getData().substring(4));
                createMenuSeason(telegramId, messageId);
                break;
            case "YEAR2025":
                try {
                    season = Integer.parseInt(callbackQuery.getData().substring(4));
                    createMenuSeason(telegramId, messageId);
                } catch (TelegramApiException e) {
                    throw new RuntimeException(e);
                }
                break;
            case "DOWNLOAD_UPDATES":
                userService.parsing();
                sendMessage(telegramId, "Данные успешно обновлены");
                break;
            case "SHOW_ALL_GAMES":
                String allGamesText = getAllGames().toString();
                sendMessage(telegramId, allGamesText);
                createMainMenu(telegramId);
                break;
            case "SHOW_FUTURE_GAMES":
                String upcomingGamesText = getUpcomingGames().toString();
                sendMessage(telegramId, upcomingGamesText);
                createMainMenu(telegramId);
                break;
            case "SHOW_LAST_GAMES":
                String pastGamesText = getPastGames().toString();
                sendMessage(telegramId, pastGamesText);
                createMainMenu(telegramId);
                break;
            case "TEAMS":
                createMenuWithAllTeams(telegramId,messageId);
                break;
            case "RESULT_SEASON":
                String tableResultText = "<pre>                      ИГРЫ  ПОБЕДЫ  ПОРАЖЕНИЯ ОЧКИ\n" + getTableResult() + "</pre>";
                sendMessage(telegramId, tableResultText);
                createMainMenu(telegramId);
                break;
            case "BACK_TO_MENU":
                backToMenu(telegramId, messageId, isAdmin);
                break;
            case "BACK_TO_MENU_SEASON":
                createMenuSeason(telegramId, messageId);
                break;
            case "FULL_STATISTICS":
                String fullStatisticsText = getFullStatistic().toString();
                sendMessage(telegramId, fullStatisticsText);
                createMainMenu(telegramId);
                break;
            case "SHOW_USERS":
                String allUsersText = getUsers().toString();
                sendMessage(telegramId, allUsersText);
                createMainMenu(telegramId);

            default:
                if (callbackData.startsWith("SHOWTEAM_")) {                    // Достать id команды
                    String teamId = callbackData.split("_")[1];
                    createMenuForSpecificTeam(telegramId, messageId, teamId);
                } else if (callbackData.startsWith("GAMES_")) {                       //  Информация об играх команды
                    String teamId = callbackData.split("_")[1];
                    String gamesTeamText = getGamesTeam(teamId).toString();
                    sendMessage(telegramId, gamesTeamText);
                    createMainMenu(telegramId);
                } else if (callbackData.startsWith("STATISTICS_")) {                  // Статистика команды
                    String teamId = callbackData.split("_")[1];
                    String statisticsTeamText = getStatisticsTeam(teamId).toString();
                    sendMessage(telegramId, statisticsTeamText);
                    createMainMenu(telegramId);
                } else if (callbackData.startsWith("ROSTER_")) {                      // Состав команды
                    String teamId = callbackData.split("_")[1];
                    String playersTeamText = getPlayers(teamId).toString();
                    sendMessage(telegramId, playersTeamText);
                    createMainMenu(telegramId);
                } else {
                    // Обработка неизвестного callbackData
                    System.out.println("Неизвестный callbackData: " + callbackData);
                }
                break;

        }
    }

    private StringBuilder getUsers() {
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

    private StringBuilder getFullStatistic() {
        List<Team> teams = userService.getFullStatistic();
        StringBuilder teamsList = new StringBuilder();
        if (teams.isEmpty()) {
            teamsList.append("Команда отсутствует");
        } else {
            for (Team team : teams) {
                teamsList.append(STR."\{team.toString()}\n ");
            }
        }
        return teamsList;
    }

    private StringBuilder getPlayers(String teamId) {
        List<Player> players = userService.getPlayers(season, Short.parseShort(teamId));
        StringBuilder playersList = new StringBuilder();
        if (players.isEmpty()) {
            playersList.append("Игроки отсутствуют");
        } else {
            for (Player player : players) {
                playersList.append(STR."\{player.toString()}");
            }
        }
        return playersList;
    }

    private StringBuilder getStatisticsTeam(String teamId) {
        List<Team> teams = userService.getStatisticsTeam(season, Short.parseShort(teamId));
        StringBuilder teamsList = new StringBuilder();
        if (teams.isEmpty()) {
            teamsList.append("Команда отсутствует");
        } else {
            for (Team team : teams) {
                teamsList.append(STR."\{team.toString()}\n ");
            }
        }
        return teamsList;
    }

    private StringBuilder getGamesTeam(String teamId) {
        List<Game> games = userService.getGamesTeam(season, Short.parseShort(teamId));
        StringBuilder gamesList = new StringBuilder();
        if (games.isEmpty()) {
            gamesList.append("Игры отсутствуют");
        } else {
            for (Game game : games) {
                gamesList.append(STR."\{game.toString()}\n ");
            }
        }
        return gamesList;

    }

    private void createMenuForSpecificTeam (Long telegramId, Integer messageId, String teamId) throws TelegramApiException {
        List<InlineKeyboardRow> keyboard = new ArrayList<>();

        InlineKeyboardRow userRow1 = new InlineKeyboardRow();
        userRow1.add(InlineKeyboardButton
                .builder()
                .text("Игры и результаты команды")
                .callbackData("GAMES_" + teamId)
                .build());
        userRow1.add(InlineKeyboardButton
                .builder()
                .text("Статистика")
                .callbackData("STATISTICS_" + teamId)
                .build());
        keyboard.add(userRow1);
        InlineKeyboardRow userRow2 = new InlineKeyboardRow();
        userRow2.add(InlineKeyboardButton
                .builder()
                .text("Состав")
                .callbackData("ROSTER_" + teamId)
                .build());
        userRow2.add(InlineKeyboardButton
                .builder()
                .text("Вернуться")
                .callbackData("TEAMS")
                .build());
        keyboard.add(userRow2);

        EditMessageReplyMarkup editMessageReplyMarkup = EditMessageReplyMarkup.builder()
                .chatId(telegramId.toString())
                .messageId(messageId)
                .replyMarkup(InlineKeyboardMarkup
                        .builder()
                        .keyboard(keyboard).build()).build();

        client.execute(editMessageReplyMarkup);
    }

    private StringBuilder getTableResult() {
        List<Team> teams = userService.getTableResult(season);
        StringBuilder teamsList = new StringBuilder();
        if (teams.isEmpty()) {
            teamsList.append("Команды отсутствуют");
        } else {
            int i = 0;
            for (Team team : teams) {
                teamsList.append(STR."\{++i}. \{team.toString()}\n ");
            }
        }
        return teamsList;
    }

    private StringBuilder getPastGames() {
        List<Game> games = userService.getPastGames(season);
        StringBuilder gamesList = new StringBuilder();
        if (games.isEmpty()) {
            gamesList.append("Игры отсутствуют");
        } else {
            for (Game game : games) {
                gamesList.append(STR."\{game.toString()}\n ");
            }
        }
        return gamesList;
    }

    private StringBuilder getUpcomingGames() {
        List<Game> games = userService.getUpcomingGames(season);
        StringBuilder gamesList = new StringBuilder();
        if (games.isEmpty()) {
            gamesList.append("Игры отсутствуют");
        } else {
            for (Game game : games) {
                gamesList.append(STR."\{game.toString()}\n ");
            }
        }
        return gamesList;
    }

    private StringBuilder getAllGames() {
        List<Game> games = userService.getAllGames(season);
        StringBuilder gamesList = new StringBuilder();
        if (games.isEmpty()) {
            gamesList.append("Игры отсутствуют");
        } else {
            for (Game game : games) {
                gamesList.append(STR."\{game.toString()}\n ");
            }
        }
        return gamesList;
    }

    private void createMenuWithAllTeams(Long telegramId, Integer messageId) throws TelegramApiException {
        List<Team> teams = userService.getTeamsFromSeason(telegramId, season);

        List<InlineKeyboardRow> keyboard = new ArrayList<>();
        InlineKeyboardRow row = new InlineKeyboardRow();

        for (int i = 0; i < teams.size(); i++) {
            Team team = teams.get(i);

            InlineKeyboardButton teamButton = InlineKeyboardButton.builder()
                    .text(team.getTitle())
                    .callbackData("SHOWTEAM_" + team.getId())
                    .build();

            row.add(teamButton);

            if ((i+1) % 2 == 0 || i == teams.size() - 1) {
                keyboard.add(row);
                row = new InlineKeyboardRow();
            }
        }
        if (!keyboard.isEmpty() && keyboard.getLast().size() == 1) {
            InlineKeyboardButton backButton = InlineKeyboardButton.builder()
                    .text("Вернуться")
                    .callbackData("BACK_TO_MENU_SEASON")
                    .build();
            keyboard.getLast().add(backButton);
        } else {
            InlineKeyboardRow backRow = new InlineKeyboardRow();
            InlineKeyboardButton backButton = InlineKeyboardButton.builder()
                    .text("Вернуться")
                    .callbackData("BACK_TO_MENU_SEASON")
                    .build();
            backRow.add(backButton);
            keyboard.add(backRow);
        }
        EditMessageReplyMarkup editMessageReplyMarkup = EditMessageReplyMarkup.builder()
                .chatId(telegramId.toString())
                .messageId(messageId)
                .replyMarkup(InlineKeyboardMarkup
                        .builder()
                        .keyboard(keyboard).build()).build();

        client.execute(editMessageReplyMarkup);
    }

    private void createMenuSeason(Long telegramId, Integer messageId) throws TelegramApiException {
        List<InlineKeyboardRow> keyboard = new ArrayList<>();

        InlineKeyboardRow userRow1 = new InlineKeyboardRow();
        userRow1.add(InlineKeyboardButton
                .builder()
                .text("Все игры")
                .callbackData("SHOW_ALL_GAMES")
                .build());
        userRow1.add(InlineKeyboardButton
                .builder()
                .text("Ближайшие игры")
                .callbackData("SHOW_FUTURE_GAMES")
                .build());
        userRow1.add(InlineKeyboardButton
                .builder()
                .text("Прошедшие игры")
                .callbackData("SHOW_LAST_GAMES")
                .build());
        keyboard.add(userRow1);

        InlineKeyboardRow userRow2 = new InlineKeyboardRow();
        userRow2.add(InlineKeyboardButton
                .builder()
                .text("Команды")
                .callbackData("TEAMS")
                .build());
        userRow2.add(InlineKeyboardButton
                .builder()
                .text("Таблица результатов")
                .callbackData("RESULT_SEASON")
                .build());
        userRow2.add(InlineKeyboardButton
                .builder()
                .text("Вернуться")
                .callbackData("BACK_TO_MENU")
                .build());
        keyboard.add(userRow2);

        EditMessageReplyMarkup editMessageReplyMarkup = EditMessageReplyMarkup.builder()
                .chatId(telegramId.toString())
                .messageId(messageId)
                .replyMarkup(InlineKeyboardMarkup
                        .builder()
                        .keyboard(keyboard).build()).build();

        client.execute(editMessageReplyMarkup);
    }


    private void createMainMenu(Long telegramId) throws TelegramApiException {
        boolean isAdmin = userService.isAdmin(telegramId);
        InlineKeyboardMarkup mainKeyboard = createMainKeyboard(isAdmin);


        SendMessage sendMessage = SendMessage.builder()
                .chatId(telegramId.toString())
                .text("Выберите сезон")
                .replyMarkup(mainKeyboard).build();

        client.execute(sendMessage);
    }


    private void backToMenu(Long chatId, Integer messageId, boolean isAdmin) throws TelegramApiException {
        InlineKeyboardMarkup mainKeyboard = createMainKeyboard(isAdmin);

        EditMessageReplyMarkup editMessageReplyMarkup = EditMessageReplyMarkup.builder()
                .chatId(chatId.toString())
                .messageId(messageId)
                .replyMarkup(mainKeyboard)
                .build();

        client.execute(editMessageReplyMarkup);
    }

    private InlineKeyboardMarkup createMainKeyboard(boolean isAdmin) {

        List<InlineKeyboardRow> keyboard = new ArrayList<>();

        InlineKeyboardRow userRow = new InlineKeyboardRow();
        userRow.add(InlineKeyboardButton
                .builder()
                .text("2024")
                .callbackData("YEAR2024")
                .build());
        userRow.add(InlineKeyboardButton
                .builder()
                .text("2025")
                .callbackData("YEAR2025")
                .build());
        userRow.add(InlineKeyboardButton
                .builder()
                .text("Общая статистика")
                .callbackData("FULL_STATISTICS")
                .build());
        keyboard.add(userRow);

        if (isAdmin) {
            InlineKeyboardRow adminRow = new InlineKeyboardRow();
            adminRow.add(InlineKeyboardButton.builder()
                    .text("Загрузить обновления")
                    .callbackData("DOWNLOAD_UPDATES")
                    .build());
            keyboard.add(adminRow);
            InlineKeyboardRow adminRow2 = new InlineKeyboardRow();
            adminRow.add(InlineKeyboardButton.builder()
                    .text("Пользователи")
                    .callbackData("SHOW_USERS")
                    .build());
            keyboard.add(adminRow2);
        }
        return InlineKeyboardMarkup
                .builder()
                .keyboard(keyboard).build();
    }

    public void sendMessage(long chatId, String text) {
        if (text.length() <= MAX_MESSAGE_LENGTH) {
            if (!text.trim().isEmpty()) {
                sendShortMessage(chatId, text);
            }
        } else {
            int start = 0;
            while (start < text.length()) {
                // индекс следующего символа \n в пределах допустимой длины сообщения
                int end = Math.min(start + MAX_MESSAGE_LENGTH, text.length());
                int newlineIndex = text.lastIndexOf(" \n", end);

                if (newlineIndex >= start && newlineIndex <= end) {
                    end = newlineIndex + 1;
                }

                String messagePart = text.substring(start, end).trim();
                if (!messagePart.isEmpty()) {
                    sendShortMessage(chatId, messagePart);
                }

                start = end;
            }
        }
    }

    private void sendShortMessage(Long telegramId, String text) {
        SendMessage sendMessage = SendMessage.builder()
                .chatId(telegramId.toString())
                .text(text)
                .parseMode("HTML")
                .build();
        try {
            client.execute(sendMessage);
        } catch (TelegramApiException e) {
            logger.error("Не получилось отправить сообщение: {}", e.getMessage());
            throw new RuntimeException(e);
        }
    }
}
