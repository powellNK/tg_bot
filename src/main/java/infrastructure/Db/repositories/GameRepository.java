package infrastructure.Db.repositories;

import domain.Game;
import domain.Team;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import queries.GameQueries;
import queries.UserQueries;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class GameRepository {
    private final Connection connection;
    private final Logger logger = LoggerFactory.getLogger(GameRepository.class);

    public GameRepository(Connection connection) {
        this.connection = connection;
    }

    public void createGame(Game game) {
        try {
            final PreparedStatement preparedStatement = connection.prepareStatement(GameQueries.CREATE_NEW_GAME);

            preparedStatement.setTimestamp(1, Timestamp.valueOf(game.getDateTime()));
            preparedStatement.setShort(2, game.getTeam1Id());
            preparedStatement.setShort(3, game.getTeam2Id());
            preparedStatement.setString(4, game.getPointResult());
            preparedStatement.setString(5, game.getSetResult());
            preparedStatement.setInt(6, game.getSeason());
            preparedStatement.execute();

        } catch (SQLException e) {
            logger.error(e.getMessage());
        }
    }

    public List<Game> getAllGames(int season) {
        final List<Game> games = new ArrayList<>();
        try {
            final PreparedStatement preparedStatement = connection.prepareStatement(GameQueries.GET_ALL_GAMES_IN_SEASON);
            preparedStatement.setInt(1, season);
            final ResultSet resultSet = preparedStatement.executeQuery();


            while (resultSet.next()) {
                games.add(Game.createGame(resultSet));
            }

        } catch (SQLException exception) {
            logger.error("SQLException: {}", exception.getMessage());
        }
        return games;
    }

    public List<Game> getUpcomingGames(int season) {
        final List<Game> games = new ArrayList<>();
        try {
            final PreparedStatement preparedStatement = connection.prepareStatement(GameQueries.GET_UPCOMING_GAMES_IN_SEASON);
            preparedStatement.setInt(1, season);
            preparedStatement.setTimestamp(2, Timestamp.valueOf(LocalDateTime.now()));
            final ResultSet resultSet = preparedStatement.executeQuery();


            while (resultSet.next()) {
                games.add(Game.createGame(resultSet));
            }

        } catch (SQLException exception) {
            logger.error("SQLException: {}", exception.getMessage());
        }
        return games;
    }

    public List<Game> getPastGames(int season) {
        final List<Game> games = new ArrayList<>();
        try {
            final PreparedStatement preparedStatement = connection.prepareStatement(GameQueries.GET_PAST_GAMES_IN_SEASON);
            preparedStatement.setInt(1, season);
            preparedStatement.setTimestamp(2, Timestamp.valueOf(LocalDateTime.now()));
            final ResultSet resultSet = preparedStatement.executeQuery();


            while (resultSet.next()) {
                games.add(Game.createGame(resultSet));
            }

        } catch (SQLException exception) {
            logger.error("SQLException: {}", exception.getMessage());
        }
        return games;
    }

}
