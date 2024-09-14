package services;

import domain.Game;
import domain.Player;
import domain.Team;
import domain.User;
import infrastructure.Db.repositories.UserRepository;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Getter
public class UserService {
    private final UserRepository userRepository;
    private final ParserService parserService;
    private final GameService gameService;
    private final TeamService teamService;
    private final PlayerService playerService;
    private Map<Long, Map<Integer, List<Team>>> userTeamsCache = new HashMap<>();

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

    public List<Game> getAllGames(int season) {
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

    public List<Team> getTeamsFromSeason(long telegramId, int season) {
        // Проверяем, есть ли данные в кэше для данного пользователя и сезона
        if (userTeamsCache.containsKey(telegramId) && userTeamsCache.get(telegramId).containsKey(season)) {
            return userTeamsCache.get(telegramId).get(season); // Возвращаем из кэша
        }

        // Если данных нет в кэше, загружаем из базы данных
        List<Team> teams = teamService.getTeamsFromSeason(season);

        userTeamsCache.putIfAbsent(telegramId, new HashMap<>()); // Если для пользователя еще нет кэша, создаем новый
        userTeamsCache.get(telegramId).put(season, teams); // Сохранить команды для конкретного сезона

        return teams;
    }

    public List<Game> getGamesTeam(int season, short teamId) {
        return gameService.getGamesTeam(season,teamId);
    }

    public List<Team> getStatisticsTeam(int season, short teamId) {
        return teamService.getStatisticsTeam(season,teamId);
    }

    public List<Player> getPlayers(int season, short teamId) {
        return playerService.getPlayers(season, teamId);
    }

    public List<Team> getFullStatistic() {
        return teamService.getFullStatistic();
    }

    public List<User> getUsers() {
        return userRepository.getUsers();
    }
}

