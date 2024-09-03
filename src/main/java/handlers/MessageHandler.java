package handlers;

import services.UserService;

public class MessageHandler  implements LongPollingSingleThreadUpdateConsumer {
    private final TelegramClient client = new OkHttpTelegramClient(SecretManager.getToken());
    private final Logger logger = LoggerFactory.getLogger(MessageHandler.class);
    private final UserService userService;
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


    public MessageHandler(UserService userService) {
        this.userService = userService;
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

        }
