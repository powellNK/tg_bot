package services;

import domain.Game;
import infrastructure.Db.repositories.GameRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.time.LocalDateTime;
import java.util.List;

public class GameService {
    private final GameRepository gameRepository;
    private final Logger logger = LoggerFactory.getLogger(GameService.class);

    public GameService(GameRepository gameRepository) {
        this.gameRepository = gameRepository;
    }

    public void createGame(LocalDateTime dateTime, Integer team1Id, Integer team2Id, String resultPoints, String resultSet, int season) {
        short team1IdSh = shortFromInteger(team1Id);
        short team2IdSh = shortFromInteger(team2Id);
        if (team1IdSh != -1 && team2IdSh != -1) {
            gameRepository.createGame(Game.builder().dateTime(dateTime).team1Id(team1IdSh).team2Id(team2IdSh).setResult(resultSet).pointResult(resultPoints).season(season).build());
        } else {
            logger.error("Игра не создана");
        }

    }

    private short shortFromInteger(Integer teamId) {
        if (teamId < Short.MIN_VALUE || teamId > Short.MAX_VALUE) {
            logger.error("Число выходит за границы типа short");
            return -1;

        }
        return teamId.shortValue();
    }

    public List<Game> getPastGames(int season) {
        return gameRepository.getPastGames(season);
    }

    public List<Game> getAllGames(int season) {
        return gameRepository.getAllGames(season);
    }

    public List<Game> getUpcomingGames(int season) {
        return gameRepository.getUpcomingGames(season);
    }
}
