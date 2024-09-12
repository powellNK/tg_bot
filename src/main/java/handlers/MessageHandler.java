package handlers;

import domain.Game;
import domain.Team;
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
import java.util.HashMap;
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
                sendMessage(telegramId, "Команды");
                break;
            case "RESULT_SEASON":
                String tableResultText = "<pre>                      ИГРЫ  ПОБЕДЫ  ПОРАЖЕНИЯ ОЧКИ\n" + getTableResult().toString() + "</pre>";
                sendMessage(telegramId, tableResultText);
                createMainMenu(telegramId);
                break;
            case "BACK_TO_MENU":
                backToMenu(telegramId, messageId, isAdmin);
                break;
//            case "SHOW_TEAM_":
//                HashMap <String,int> teams = userService.getTeam();
//                createTeamMenu(telegramId, teams);
//                break;

        }
    }

    private StringBuilder getTableResult() {
        List<Team> teams = userService.getTableResult(season);
        StringBuilder teamsList = new StringBuilder();
        if (teams.isEmpty()) {
            teamsList.append("Команды отсутствуют");
        } else {
            for (Team team : teams) {
                teamsList.append(STR."\{team.toString()}\n ");
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

    private void createTeamMenu(Long telegramId, HashMap<String, Integer> teams) {

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
