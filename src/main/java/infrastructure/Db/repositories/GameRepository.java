package infrastructure.Db.repositories;

import domain.Game;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import queries.GameQueries;
import queries.TeamQueries;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Timestamp;

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
            preparedStatement.setString(4, game.getResultPoint());
            preparedStatement.setString(5, game.getResultSet());
            preparedStatement.setInt(6, game.getSeason());
            preparedStatement.execute();

        } catch (SQLException e) {
            logger.error(e.getMessage());
        }
    }
}
