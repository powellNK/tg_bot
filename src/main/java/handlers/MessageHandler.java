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
import java.util.Arrays;
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
            processMessage(update.getMessage());
        } else if (update.hasCallbackQuery()) {
            try {
                handleCallbackQuery(update.getCallbackQuery());
            } catch (TelegramApiException | IOException e) {
                logger.error("Ошибка при обработке CallbackQuery: {}", e.getMessage(), e);
                throw new RuntimeException(e);
            }
        }
    }

    private void processMessage(Message message) {
        Long telegramId = message.getChatId();
        String telegramUsername = message.getFrom().getUserName();

        userService.authorization(telegramId, telegramUsername);
        sendMessage(telegramId, "Привет. Я бот, следящий за высшей лигой Б. Присоединяйся! Ввод с клавиатуры невозможен, воспользуйся меню");

        try {
            createMainMenu(telegramId);
        } catch (TelegramApiException e) {
            logger.error("Ошибка при создании главного меню: {}", e.getMessage(), e);
        }
    }

    private void handleCallbackQuery(CallbackQuery callbackQuery) throws TelegramApiException, IOException {
        String callbackData = callbackQuery.getData();
        Long telegramId = callbackQuery.getMessage().getChatId();
        boolean isAdmin = userService.isAdmin(telegramId);
        Integer messageId = callbackQuery.getMessage().getMessageId();


        switch (callbackData) {
            case "YEAR2024":
            case "YEAR2025":
                season = Integer.parseInt(callbackData.substring(4));
                createMenuSeason(telegramId, messageId);
                break;
            case "DOWNLOAD_UPDATES":
                userService.parsing();
                sendMessage(telegramId, "Данные успешно обновлены");
                break;
            case "SHOW_ALL_GAMES":
                showGames(telegramId, getAllGames());
                break;
            case "SHOW_FUTURE_GAMES":
                showGames(telegramId, getUpcomingGames());
                break;
            case "SHOW_LAST_GAMES":
                showGames(telegramId, getPastGames());
                break;
            case "TEAMS":
                createMenuWithAllTeams(telegramId, messageId);
                break;
            case "RESULT_SEASON":
                handleTableResult(telegramId);
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
                break;
            default:
                handleTeamRelatedCallback(callbackData, telegramId, messageId);
        }
    }

    private void handleTeamRelatedCallback(String callbackData, Long telegramId, Integer messageId) throws TelegramApiException {
        if (callbackData.startsWith("SHOWTEAM_")) {
            String teamId = callbackData.split("_")[1];
            createMenuForSpecificTeam(telegramId, messageId, teamId);
        } else if (callbackData.startsWith("GAMES_")) {
            showGames(telegramId, getGamesTeam(callbackData.split("_")[1]));
        } else if (callbackData.startsWith("STATISTICS_")) {
            showGames(telegramId, getStatisticsTeam(callbackData.split("_")[1]));
        } else if (callbackData.startsWith("ROSTER_")) {
            sendMessage(telegramId, getPlayers(callbackData.split("_")[1]).toString());
            createMainMenu(telegramId);
        } else {
            logger.warn("Неизвестный callbackData: {}", callbackData);
        }
    }

    private void showGames(Long telegramId, StringBuilder gameData) throws TelegramApiException {
        sendMessage(telegramId, gameData.toString());
        createMainMenu(telegramId);
    }

    private void handleTableResult(Long telegramId) throws TelegramApiException {
        String tableResultText = STR."<pre>                      ИГРЫ  ПОБЕДЫ  ПОРАЖЕНИЯ ОЧКИ\n\{getTableResult()}</pre>";
        sendMessage(telegramId, tableResultText);
        createMainMenu(telegramId);
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
        return buildTeamList(userService.getFullStatistic());
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

    private StringBuilder getPlayers(String teamId) {
        return buildPlayerList(userService.getPlayers(season, Short.parseShort(teamId)));
    }

    private StringBuilder getStatisticsTeam(String teamId) {
        return buildTeamList(userService.getStatisticsTeam(season, Short.parseShort(teamId)));
    }

    private StringBuilder getGamesTeam(String teamId) {
        return buildGameList(userService.getGamesTeam(season, Short.parseShort(teamId)));

    }

    private void createMenuForSpecificTeam(Long telegramId, Integer messageId, String teamId) throws TelegramApiException {
        List<List<InlineKeyboardButton>> buttonRows = List.of(
                List.of(createButton("Игры и результаты команды", STR."GAMES_\{teamId}")),
                List.of(createButton("Состав", STR."ROSTER_\{teamId}"),
                        createButton("Статистика", STR."STATISTICS_\{teamId}")),
                List.of(createButton("Вернуться", "TEAMS"))
        );
        sendMenu(telegramId, messageId, buttonRows);
    }

    private StringBuilder getTableResult() {
        return buildTeamList(userService.getTableResult(season));
    }

    private StringBuilder getPastGames() {
        return buildGameList(userService.getPastGames(season));
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

    private StringBuilder getUpcomingGames() {
        return buildGameList(userService.getUpcomingGames(season));
    }

    private StringBuilder getAllGames() {
        return buildGameList(userService.getAllGames(season));
    }

    private void createMenuWithAllTeams(Long telegramId, Integer messageId) throws TelegramApiException {
        List<Team> teams = userService.getTeamsFromSeason(telegramId, season);
        List<InlineKeyboardRow> keyboard = new ArrayList<>();
        InlineKeyboardRow row = new InlineKeyboardRow();

        for (int i = 0; i < teams.size(); i++) {
            row.add(createButton(teams.get(i).getTitle(), STR."SHOWTEAM_\{teams.get(i).getId()}"));
            if ((i + 1) % 2 == 0 || i == teams.size() - 1) {
                keyboard.add(row);
                row = new InlineKeyboardRow();
            }
        }
        row = new InlineKeyboardRow(createButton("Вернуться", "BACK_TO_MENU_SEASON"));
        keyboard.add(row);
        InlineKeyboardMarkup inlineKeyboard = InlineKeyboardMarkup.builder()
                .keyboard(keyboard)
                .build();
        editMessage(telegramId, messageId, inlineKeyboard);
    }

    private void createMenuSeason(Long telegramId, Integer messageId) throws TelegramApiException {
        List<List<InlineKeyboardButton>> buttonRows = List.of(
                List.of(createButton("Все игры", "SHOW_ALL_GAMES"),
                        createButton("Ближайшие игры", "SHOW_FUTURE_GAMES"),
                        createButton("Прошедшие игры", "SHOW_LAST_GAMES")),
                List.of(createButton("Команды", "TEAMS"),
                        createButton("Таблица результатов", "RESULT_SEASON"),
                        createButton("Вернуться", "BACK_TO_MENU"))
        );
        sendMenu(telegramId, messageId, buttonRows);
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


    private void backToMenu(Long telegramId, Integer messageId, boolean isAdmin) throws TelegramApiException {
        InlineKeyboardMarkup mainKeyboard = createMainKeyboard(isAdmin);
        editMessage(telegramId, messageId, mainKeyboard);
    }

    private InlineKeyboardMarkup createMainKeyboard(boolean isAdmin) {
        List<InlineKeyboardRow> keyboard = new ArrayList<>();
        keyboard.add(createRow(
                createButton("2024", "YEAR2024"),
                createButton("2025", "YEAR2025"),
                createButton("Общая статистика", "FULL_STATISTICS")
        ));
        if (isAdmin) {
            keyboard.add(createRow(
                    createButton("Загрузить обновления", "DOWNLOAD_UPDATES"),
                    createButton("Пользователи", "SHOW_USERS")
            ));
        }
        return InlineKeyboardMarkup.builder()
                .keyboard(keyboard)
                .build();
    }

    private InlineKeyboardRow createRow(InlineKeyboardButton... buttons) {
        InlineKeyboardRow row = new InlineKeyboardRow();
        row.addAll(Arrays.asList(buttons));
        return row;
    }

    public void sendMessage(long chatId, String text) {
        if (text.length() <= MAX_MESSAGE_LENGTH) {
            sendShortMessage(chatId, text);
        } else {
            sendLongMessage(chatId, text);
        }
    }

    private void sendLongMessage(long chatId, String text) {
        int start = 0;
        while (start < text.length()) {
            int end = Math.min(start + MAX_MESSAGE_LENGTH, text.length());
            int newlineIndex = text.lastIndexOf("\n", end);

            if (newlineIndex > start && newlineIndex <= end) {
                end = newlineIndex + 1;
            }

            String messagePart = text.substring(start, end).trim();
            if (!messagePart.isEmpty()) {
                sendShortMessage(chatId, messagePart);
            }

            start = end;
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

    private InlineKeyboardMarkup createInlineKeyboard(List<List<InlineKeyboardButton>> buttonRows) {
        List<InlineKeyboardRow> keyboard = new ArrayList<>();
        for (List<InlineKeyboardButton> rowButtons : buttonRows) {
            InlineKeyboardRow row = new InlineKeyboardRow();
            row.addAll(rowButtons);
            keyboard.add(row);
        }
        return InlineKeyboardMarkup.builder().keyboard(keyboard).build();
    }

    private InlineKeyboardButton createButton(String text, String callbackData) {
        return InlineKeyboardButton.builder()
                .text(text)
                .callbackData(callbackData)
                .build();
    }
    private void sendMenu(Long telegramId, Integer messageId, List<List<InlineKeyboardButton>> buttonRows) throws TelegramApiException {
        EditMessageReplyMarkup editMessageReplyMarkup = EditMessageReplyMarkup.builder()
                .chatId(telegramId.toString())
                .messageId(messageId)
                .replyMarkup(createInlineKeyboard(buttonRows))
                .build();
        client.execute(editMessageReplyMarkup);
    }
    private void editMessage(Long chatId, Integer messageId, InlineKeyboardMarkup keyboard) throws TelegramApiException {
        EditMessageReplyMarkup editMessageReplyMarkup = EditMessageReplyMarkup.builder()
                .chatId(chatId.toString())
                .messageId(messageId)
                .replyMarkup(keyboard)
                .build();
        client.execute(editMessageReplyMarkup);
    }
}
