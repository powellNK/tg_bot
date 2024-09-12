package services;

import domain.Game;
import domain.Team;
import domain.User;
import infrastructure.Db.repositories.UserRepository;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import java.io.IOException;
import java.util.List;

@Slf4j
@Getter
public class UserService {
    private final UserRepository userRepository;
    private final ParserService parserService;
    private final GameService gameService;
    private final TeamService teamService;
    private final PlayerService playerService;

    public UserService(UserRepository userRepository, ParserService parserService, GameService gameService, TeamService teamService, PlayerService playerService) {
        this.userRepository = userRepository;
        this.parserService = parserService;
        this.gameService = gameService;
        this.teamService = teamService;
        this.playerService = playerService;
    }

    private boolean isUserExists(long telegramId) {
        return userRepository.isUserExists(telegramId);
    }

    public void authorization(long telegramId, String telegramUsername) {
        if (!isUserExists(telegramId)) createUser(telegramId, telegramUsername);
    }

    private void createUser(long telegramId, String telegramUsername) {
        userRepository.createUser(User.builder().telegramId(telegramId).telegramUsername(telegramUsername).build());
    }

    public boolean isAdmin(long telegramId) {
        return userRepository.isAdmin(telegramId);
    }

    public void parsing() throws IOException {
        parserService.parsing();
    }

    public List<Game> getAllGames(int season){
        return gameService.getAllGames(season);
    }

    public List<Game> getUpcomingGames(int season) {
        return gameService.getUpcomingGames(season);
    }

    public List<Game> getPastGames(int season) {
        return gameService.getPastGames(season);
    }

    public List<Team> getTableResult(int season) {
        return teamService.getTableResult(season);
    }
}
