package services;

import domain.Game;
import infrastructure.Db.repositories.GameRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;

public class GameService {
    private final GameRepository gameRepository;
    private final Logger logger = LoggerFactory.getLogger(GameService.class);
    public GameService(GameRepository gameRepository) {
        this.gameRepository = gameRepository;
    }

    public void createGame(LocalDateTime dateTime, Integer team1Id, Integer team2Id, String resultPoints, String resultSet, int season) {
        short team1IdSh = shortFromInteger(team1Id);
        short team2IdSh = shortFromInteger(team2Id);
        if (team1IdSh!=-1 && team2IdSh!=-1){
            gameRepository.createGame(Game.builder().dateTime(dateTime).team1Id(team1IdSh).team2Id(team2IdSh).resultSet(resultSet).resultPoint(resultPoints).season(season).build());
        }else {
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
}
