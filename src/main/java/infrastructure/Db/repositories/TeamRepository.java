package infrastructure.Db.repositories;

import domain.Team;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import queries.TeamQueries;
import queries.UserQueries;
import services.GameService;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

@Slf4j
public class TeamRepository {
    private final Connection connection;
    private final Logger logger = LoggerFactory.getLogger(TeamRepository.class);

    public TeamRepository(Connection connection) {
        this.connection = connection;
    }

    public void createTeam(Team team) {
        try {
            final PreparedStatement preparedStatement = connection.prepareStatement(TeamQueries.CREATE_NEW_TEAM);

            preparedStatement.setShort(1, team.getId());
            preparedStatement.setString(2, team.getTitle());
            preparedStatement.execute();

        } catch (SQLException e) {
            logger.error(e.getMessage());
        }
    }

    //дублирует данные, если запустить повторно
    public void updateTeam(Team team) {
        try {
            final PreparedStatement preparedStatement = connection.prepareStatement(TeamQueries.UPDATE_INFO_TEAM);

            preparedStatement.setString(1, String.valueOf(team.getPointsWon()));
            preparedStatement.setString(2, String.valueOf(team.getSetWon()));
            preparedStatement.setString(3, String.valueOf(team.getPointsLost()));
            preparedStatement.setString(4, String.valueOf(team.getSetLost()));
            preparedStatement.setInt(5, team.getWins());
            preparedStatement.setInt(6, team.getLoses());
            preparedStatement.setShort(7, team.getId());
            preparedStatement.setInt(8, team.getSeason());
            preparedStatement.execute();

        } catch (SQLException e) {
            logger.error(e.getMessage());
        }
    }

    public void addTeamInSeason(Team team) {
        try {
            final PreparedStatement preparedStatement = connection.prepareStatement(TeamQueries.ADD_TEAM_IN_SEASON);

            preparedStatement.setShort(1, team.getId());
            preparedStatement.setString(2, String.valueOf(team.getPointsWon()));
            preparedStatement.setString(3, String.valueOf(team.getSetWon()));
            preparedStatement.setString(4, String.valueOf(team.getPointsLost()));
            preparedStatement.setString(5, String.valueOf(team.getSetLost()));
            preparedStatement.setInt(6, team.getWins());
            preparedStatement.setInt(7, team.getLoses());
            preparedStatement.setInt(8, team.getSeason());
            preparedStatement.execute();

        } catch (SQLException e) {
            logger.error(e.getMessage());
        }
    }


    public boolean isExistsTeamInSeason(short teamId, int season) {
        try (PreparedStatement statement = connection.prepareStatement(TeamQueries.CHECK_TEAM_IN_SEASON_EXISTS)) {
            statement.setShort(1, teamId);
            statement.setInt(2, season);
            ResultSet resultSet = statement.executeQuery();
            return resultSet.next();
        } catch (SQLException e) {
            logger.error(e.getMessage());
        }
        return false;
    }

    public boolean isExistsTeam(short teamId) {
        try (PreparedStatement statement = connection.prepareStatement(TeamQueries.CHECK_TEAM_EXISTS)) {
            statement.setShort(1, teamId);
            ResultSet resultSet = statement.executeQuery();
            return resultSet.next();
        } catch (SQLException e) {
            logger.error(e.getMessage());
        }
        return false;
    }
}
