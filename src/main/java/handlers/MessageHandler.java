CREATE OR REPLACE FUNCTION update_team_ids()
        RETURNS TRIGGER AS $$
        BEGIN
        -- Установка team1_id на основе team1_title
        NEW.team1_id := (SELECT team_id FROM teams WHERE team_title = NEW.team1_title);

        -- Если team1_title не найден, выброс исключения
        IF NEW.team1_id IS NULL THEN
        RAISE EXCEPTION 'Team title "%" not found in teams table for team1', NEW.team1_title;
        END IF;

        -- Установка team2_id на основе team2_title
        NEW.team2_id := (SELECT team_id FROM teams WHERE team_title = NEW.team2_title);

        -- Если team2_title не найден, выброс исключения
        IF NEW.team2_id IS NULL THEN
        RAISE EXCEPTION 'Team title "%" not found in teams table for team2', NEW.team2_title;
        END IF;

        RETURN NEW;
        END;
        $$ LANGUAGE plpgsql;


        CREATE TRIGGER trg_update_team_ids
        BEFORE INSERT OR UPDATE ON games
        FOR EACH ROW
        EXECUTE FUNCTION update_team_ids();




        package handlers;

import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.telegram.telegrambots.client.okhttp.OkHttpTelegramClient;
import org.telegram.telegrambots.longpolling.util.LongPollingSingleThreadUpdateConsumer;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.message.Message;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardRow;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.meta.generics.TelegramClient;
import secrets.SecretManager;
import services.ParserService;
import services.UserService;

import java.util.ArrayList;
import java.util.List;

@Slf4j
public class MessageHandler implements LongPollingSingleThreadUpdateConsumer {
    private final TelegramClient client = new OkHttpTelegramClient(SecretManager.getToken());
    private final Logger logger = LoggerFactory.getLogger(MessageHandler.class);
    private final UserService userService;
    private final ParserService parserService;


    private static final String YEAR2023 = "2023";
    private static final String YEAR2024 = "2024";
    private static final String SHOW_ALL_GAMES = "SHOW_ALL_GAMES";
    private static final String SHOW_FUTURE_GAMES = "SHOW_FUTURE_GAMES";
    private static final String SHOW_LAST_GAMES = "SHOW_LAST_GAMES";
    private static final String DOWNLOAD_UPDATES = "DOWNLOAD_UPDATES";
    private static final String TEAMS = "TEAMS";
    private static final String FULL_STATISTICS = "FULL_STATISTICS";
    private static final String STATISTICS_SEASON_ = "STATISTICS_SEASON_";
    private static final String SHOW_TEAM_ = "SHOW_TEAM_";
    private static final String ALL_USERS = "ALL_USERS";


    public MessageHandler(UserService userService, ParserService parserService) {
        this.userService = userService;
        this.parserService = parserService;
    }


    @Override
    public void consume(Update update) {

        if (update.hasMessage()) {
            Message message = update.getMessage();
            Long telegramId = message.getChatId();
            userService.authorization(telegramId);

            try {
                createMainMenu(telegramId);
            } catch (TelegramApiException e) {
                throw new RuntimeException(e);
            }
        }
        if (update.hasCallbackQuery()) {
            handleCallbackQuery(update.getCallbackQuery());
        }
    }

    private void handleCallbackQuery(CallbackQuery callbackQuery) {
        String callbackData = callbackQuery.getData();
        Long telegramId = callbackQuery.getMessage().getChatId();
        switch (callbackData) {
            case "YEAR2023":
                createMenuSeason(2023);
                sendMessage(telegramId, "Выбран 2023");
                break;
            case "YEAR2024":
                createMenuSeason(2024);
                sendMessage(telegramId, "Выбран 2024");
                break;
            case "DOWNLOAD_UPDATES":
                userService.parsing();
                sendMessage(telegramId, "Загрузить обновления");
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
        }
    }

    private void createMenuSeason(int i) {
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
        keyboard.add(userRow2);
        SendMessage sendMessage = SendMessage.builder()
                .chatId(telegramId.toString())
                .text("Выберите сезон")
                .replyMarkup(InlineKeyboardMarkup
                        .builder()
                        .keyboard(keyboard).build()).build();

        client.execute(sendMessage);
    }

}

private void createMainMenu(Long telegramId) throws TelegramApiException {
    boolean isAdmin = userService.isAdmin(telegramId);

    sendMessage(telegramId, "Привет. Я бот, следящий за высшей лигой Б. " +
            "Присоединяйся! Извини, ввод с клавиатуры невозможен, воспользуйся меню");
    List<InlineKeyboardRow> keyboard = new ArrayList<>();

    InlineKeyboardRow userRow = new InlineKeyboardRow();
    userRow.add(InlineKeyboardButton
            .builder()
            .text("2023")
            .callbackData("YEAR2023")
            .build());
    userRow.add(InlineKeyboardButton
            .builder()
            .text("2024")
            .callbackData("YEAR2024")
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
    SendMessage sendMessage = SendMessage.builder()
            .chatId(telegramId.toString())
            .text("Выберите сезон")
            .replyMarkup(InlineKeyboardMarkup
                    .builder()
                    .keyboard(keyboard).build()).build();

    client.execute(sendMessage);
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

















