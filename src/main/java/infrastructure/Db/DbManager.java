package infrastructure.Db;

import handlers.MessageHandler;
import infrastructure.Db.repositories.*;
import infrastructure.configuration.DbConnectionFactory;
import lombok.Getter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import services.*;
import java.sql.Connection;
@Getter
public class DbManager {
    private final GameRepository gameRepository;
    private final TeamRepository teamRepository;
    private final TeamService teamService;
    private final PlayerRepository playerRepository;
    private final PlayerService playerService;
    private final Connection connection;
    private final UserRepository userRepository;
    private final UserService userService;
    private final MessageHandler messageHandler;
    private final ParserService parserService;
    private final GameService gameService;
    private final Logger logger = LoggerFactory.getLogger(DbManager.class);


    public DbManager() {
        this.connection = DbConnectionFactory.createConnection();
        this.gameRepository = new GameRepository(connection);
        this.gameService = new GameService(gameRepository);
        this.teamRepository = new TeamRepository(connection);
        this.teamService = new TeamService(teamRepository);

        this.playerRepository = new PlayerRepository(connection);
        this.playerService = new PlayerService(playerRepository, teamService);
        this.userRepository = new UserRepository(connection);
        this.parserService = new ParserService(gameService, teamService, playerService);
        this.userService = new UserService(userRepository, parserService, gameService, teamService ,playerService);
        this.messageHandler = new MessageHandler(userService);
        logger.info("DbManager initialized with UserService and UserRepository.");
    }
}

