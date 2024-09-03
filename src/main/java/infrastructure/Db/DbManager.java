package infrastructure.Db;

import handlers.MessageHandler;
import infrastructure.Db.repositories.ParserRepository;
import infrastructure.Db.repositories.UserRepository;
import infrastructure.configuration.DbConnectionFactory;
import lombok.Getter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import services.ParserService;
import services.UserService;

import java.sql.Connection;

public class DbManager {
    private Connection connection;
    private final Logger log = LoggerFactory.getLogger(DbManager.class);

    private UserRepository userRepository;
    @Getter
    private UserService userService;
    private MessageHandler messageHandler;
    private ParserService parserService;
    private ParserRepository parserRepository;
    ;
    public DbManager() {
        this.connection = DbConnectionFactory.createConnection();
        this.userRepository = new UserRepository(connection);
        this.userService = new UserService(userRepository, parserService);
        this.parserRepository = new ParserRepository(connection);
        this.parserService = new ParserService(parserRepository);
        this.messageHandler = new MessageHandler(userService);
        log.info("DbManager initialized with UserService and UserRepository.");
    }


    public MessageHandler getMessageHandler() {
            return messageHandler;
    }
}

