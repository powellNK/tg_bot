package handlers;

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
import state.UserState;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


public class MessageHandler implements LongPollingSingleThreadUpdateConsumer {
    private static final TelegramClient client = new OkHttpTelegramClient(SecretManager.getToken());
    private static final Logger logger = LoggerFactory.getLogger(MessageHandler.class);
    private final UserService userService;
    private static final int MAX_MESSAGE_LENGTH = 4096;
    private final UserState userState = UserState.getInstance();

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
        Integer messageId = callbackQuery.getMessage().getMessageId();
        processCallback(telegramId, messageId, callbackData);
    }

    public void processCallback(Long telegramId, Integer messageId, String callbackData) throws TelegramApiException, IOException {
        userState.handleCallback(telegramId, messageId, callbackData, this, userService);
    }


    public void handleTableResult(Long telegramId, StringBuilder gameData) throws TelegramApiException {
        String tableResultText = STR."<pre>                      ИГРЫ  ПОБЕДЫ  ПОРАЖЕНИЯ ОЧКИ\n\{gameData.toString()}</pre>";
        sendMessage(telegramId, tableResultText);
        createMainMenu(telegramId);
    }

    public void createMenuForSpecificTeam(Long telegramId, Integer messageId, String teamId) throws TelegramApiException {
        List<List<InlineKeyboardButton>> buttonRows = List.of(
                List.of(createButton("Игры и результаты команды", STR."SHOW_GAMES_\{teamId}")),
                List.of(createButton("Состав", STR."ROSTER_\{teamId}"),
                        createButton("Статистика", STR."MENUTEAM_STATISTICS_\{teamId}")),
                List.of(createButton("Вернуться", "MENUTEAM_TEAMS"))
        );
        sendMenu(telegramId, messageId, buttonRows);
    }

    public void createMenuWithAllTeams(Long telegramId, Integer messageId, int currentSeason) throws TelegramApiException {
        List<Team> teams = userService.getTeamsFromSeason(telegramId, currentSeason);
        List<InlineKeyboardRow> keyboard = new ArrayList<>();
        InlineKeyboardRow row = new InlineKeyboardRow();

        for (int i = 0; i < teams.size(); i++) {
            row.add(createButton(teams.get(i).getTitle(), STR."SHOWTEAM_NUM_\{teams.get(i).getId()}"));
            if ((i + 1) % 2 == 0 || i == teams.size() - 1) {
                keyboard.add(row);
                row = new InlineKeyboardRow();
            }
        }
        row = new InlineKeyboardRow(createButton("Вернуться", "BACK_TO_SEASON_MENU"));
        keyboard.add(row);
        InlineKeyboardMarkup inlineKeyboard = InlineKeyboardMarkup.builder()
                .keyboard(keyboard)
                .build();
        editMessage(telegramId, messageId, inlineKeyboard);
    }

    public void createMenuSeason(Long telegramId, Integer messageId) throws TelegramApiException {
        List<List<InlineKeyboardButton>> buttonRows = List.of(
                List.of(createButton("Все игры", "SHOW_ALL_GAMES"),
                        createButton("Ближайшие игры", "SHOW_FUTURE_GAMES"),
                        createButton("Прошедшие игры", "SHOW_LAST_GAMES")),
                List.of(createButton("Команды", "MENUTEAM_TEAMS"),
                        createButton("Таблица результатов", "MENUTEAM_RESULT_SEASON"),
                        createButton("Вернуться", "BACK_TO_MENU"))
        );
        sendMenu(telegramId, messageId, buttonRows);
    }

    public void createMainMenu(Long telegramId) throws TelegramApiException {
        boolean isAdmin = userService.isAdmin(telegramId);
        InlineKeyboardMarkup mainKeyboard = createMainKeyboard(isAdmin);

        SendMessage sendMessage = SendMessage.builder()
                .chatId(telegramId.toString())
                .text("Выберите сезон")
                .replyMarkup(mainKeyboard).build();

        client.execute(sendMessage);
    }

    public void backToMenu(Long telegramId, Integer messageId, boolean isAdmin) throws TelegramApiException {
        InlineKeyboardMarkup mainKeyboard = createMainKeyboard(isAdmin);
        editMessage(telegramId, messageId, mainKeyboard);
    }

    private InlineKeyboardMarkup createMainKeyboard(boolean isAdmin) {
        List<InlineKeyboardRow> keyboard = new ArrayList<>();
        keyboard.add(createRow(
                createButton("2024", "YEAR_2024"),
                createButton("2025", "YEAR_2025"),
                createButton("Общая статистика", "MENUTEAM_FULL_STATISTICS")
        ));
        if (isAdmin) {
            keyboard.add(createRow(
                    createButton("Загрузить обновления", "DOWNLOAD_UPDATES"),
                    createButton("Пользователи", "USERS_GET")
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