package infrastructure.Db.repositories;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import domain.User;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import queries.UserQueries;

@Slf4j
public class UserRepository {
    private final Connection connection;
    private final Logger logger = LoggerFactory.getLogger(UserRepository.class);
    public UserRepository(Connection connection) {
        this.connection = connection;
    }

    public boolean isUserExists(long telegramId) {
        try (PreparedStatement statement = connection.prepareStatement(UserQueries.CHECK_USER_EXISTS_BY_ID)) {
            statement.setLong(1, telegramId);
            ResultSet resultSet = statement.executeQuery();
            return resultSet.next();
        } catch (SQLException e) {
            logger.error(e.getMessage());
        }
        return false;
    }


    public boolean isAdmin(long telegramId) {
        try (PreparedStatement statement = connection.prepareStatement(UserQueries.CHECK_IF_ITS_ADMIN_)) {
            statement.setLong(1, telegramId);
            ResultSet resultSet = statement.executeQuery();
            if (resultSet.next()) {
                return resultSet.getBoolean("role_admin");
            }
        } catch (SQLException e) {
            logger.error(e.getMessage());
        }
        return false;
    }

    public void createUser(User user) {
        try {
            final PreparedStatement preparedStatement = connection.prepareStatement(UserQueries.CREATE_NEW_USER);

            preparedStatement.setLong(1, user.getTelegramId());
            preparedStatement.setString(2, user.getTelegramUsername());
            preparedStatement.execute();

        } catch (SQLException e) {
            logger.error(e.getMessage());
        }
    }
}
