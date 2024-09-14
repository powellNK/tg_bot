package infrastructure.Db.repositories;

import domain.Player;
import domain.Team;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import queries.PlayerQueries;
import queries.TeamQueries;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class PlayerRepository {
    private final Connection connection;
    private final Logger logger = LoggerFactory.getLogger(PlayerRepository.class);
    public PlayerRepository(Connection connection) {
        this.connection = connection;
    }

    public void createPlayer(Player player) {
        try {
            final PreparedStatement preparedStatement = connection.prepareStatement(PlayerQueries.CREATE_NEW_PLAYER);

            preparedStatement.setString(1, player.getFio());
            preparedStatement.setInt(2, player.getAge());
            preparedStatement.setInt(3, player.getGameNumber());
            preparedStatement.setInt(4, player.getHeight());
            preparedStatement.setString(5, player.getRole());
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

    public void addNewSeasonPlayer(Player player) {
        try {
            final PreparedStatement preparedStatement = connection.prepareStatement(PlayerQueries.ADD_NEW_SEASON_PLAYER);
            preparedStatement.setShort(1, player.getTeam_id());
            preparedStatement.setInt(2, player.getSeason());
            preparedStatement.setString(3, player.getFio());
            preparedStatement.execute();
        } catch (SQLException e) {
            logger.error(e.getMessage());
        }
    }

    public List<Player> getPlayers(int season, short teamId) {
        final List<Player> players = new ArrayList<>();
        try {
            final PreparedStatement preparedStatement = connection.prepareStatement(PlayerQueries.GET_PLAYERS);
            preparedStatement.setInt(1, season);
            preparedStatement.setShort(2, teamId);
            final ResultSet resultSet = preparedStatement.executeQuery();


            while (resultSet.next()) {
                players.add(Player.createPlayer(resultSet));
            }

        } catch (SQLException exception) {
            logger.error("SQLException: {}", exception.getMessage());
        }
        return players;
    }
}
