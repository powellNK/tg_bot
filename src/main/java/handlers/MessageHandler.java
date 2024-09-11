package handlers;

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
import java.util.Map;


public class MessageHandler  implements LongPollingSingleThreadUpdateConsumer {
    private final TelegramClient client = new OkHttpTelegramClient(SecretManager.getToken());
    private final Logger logger = LoggerFactory.getLogger(MessageHandler.class);
    private final UserService userService;
    private static final String YEAR2024 = "2024";
    private static final String YEAR2025 = "2025";
    private static final String SHOW_ALL_GAMES = "SHOW_ALL_GAMES";
    private static final String SHOW_FUTURE_GAMES = "SHOW_FUTURE_GAMES";
    private static final String SHOW_LAST_GAMES = "SHOW_LAST_GAMES";
    private static final String DOWNLOAD_UPDATES = "DOWNLOAD_UPDATES";
    private static final String TEAMS = "TEAMS";
    private static final String FULL_STATISTICS = "FULL_STATISTICS";
    private static final String STATISTICS_SEASON_ = "STATISTICS_SEASON_";
    private static final String SHOW_TEAM_ = "SHOW_TEAM_";
    private static final String ALL_USERS = "ALL_USERS";
    private static final String BACK_TO_MENU = "BACK_TO_MENU";
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
                sendMessage(telegramId, "Показаны все игры");
                break;
            case "SHOW_FUTURE_GAMES":
                sendMessage(telegramId, "Показаны ближайшие игры");
                break;
            case "TEAMS":
                sendMessage(telegramId, "Команды");
                break;
            case "STATISTICS_SEASON_":
                sendMessage(telegramId, "Статистика сезона");
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

    private void createTeamMenu(Long telegramId, HashMap<String,Integer> teams) {

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
                .text("Статистика за сезон")
                .callbackData("STATISTICS_SEASON_")
                .build());
        userRow2.add(InlineKeyboardButton
                .builder()
                .text("Вернуться")
                .callbackData("BACK_TO_MENU")
                .build());
        keyboard.add(userRow2);

        EditMessageReplyMarkup editMessageReplyMarkup  = EditMessageReplyMarkup.builder()
                .chatId(telegramId.toString())
                .messageId(messageId)
                .replyMarkup(InlineKeyboardMarkup
                        .builder()
                        .keyboard(keyboard).build()).build();

        client.execute(editMessageReplyMarkup );
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

    private void sendMessage(Long telegramId, String text) {
    SendMessage sendMessage = SendMessage.builder()
            .chatId(telegramId.toString())
            .text(text)
            .build();
    try {
        client.execute(sendMessage);
    } catch (TelegramApiException e) {
        logger.error("Не получилось отправить сообщение: {}", e.getMessage());
        throw new RuntimeException(e);
    }
}
}
