package infrastructure.Db.repositories;

import domain.Player;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import queries.PlayerQueries;
import queries.TeamQueries;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class PlayerRepository {
    private final Connection connection;
    private final Logger logger = LoggerFactory.getLogger(PlayerRepository.class);
    public PlayerRepository(Connection connection) {
        this.connection = connection;
    }

    public void createPlayer(Player player) {
        try {
            final PreparedStatement preparedStatement = connection.prepareStatement(PlayerQueries.CREATE_NEW_PLAYER);

            preparedStatement.setShort(1, team.getId());
            preparedStatement.setNString(2, String.valueOf(team.getPointsWon()));
            preparedStatement.setNString(3, String.valueOf(team.getSetWon()));
            preparedStatement.setNString(4, String.valueOf(team.getPointsLost()));
            preparedStatement.setNString(5, String.valueOf(team.getSetLost()));
            preparedStatement.setInt(6, team.getWins());
            preparedStatement.setInt(7, team.getLoses());
            preparedStatement.setInt(8, team.getSeason());
            preparedStatement.execute();

        } catch (SQLException e) {
            logger.error(e.getMessage());
        }
    }

    public boolean isPlayerExists(Player player) {
        try (PreparedStatement statement = connection.prepareStatement(PlayerQueries.IS_PLAYER_EXISTS)) {
            statement.setString(1, player.getFio());
            ResultSet resultSet = statement.executeQuery();
            return resultSet.next();
        } catch (SQLException e) {
            logger.error(e.getMessage());
        }
        return false;
    }

    public void addNewSeasonPlayer(String fio, short teamId, int season) {

    }
}
