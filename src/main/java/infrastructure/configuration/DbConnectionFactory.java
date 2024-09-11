package infrastructure.configuration;

import lombok.extern.slf4j.Slf4j;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

@Slf4j
public class DbConnectionFactory {
    private static Connection connection;

    public static Connection createConnection() {
        if (connection == null) {
            try {
                connection = DriverManager.getConnection("jdbc:postgresql://83.147.246.87:5432/java_volley_bot_db", "java_volley_bot_user", "pow22nk");
                log.info("Connection to DB OK");
            } catch (SQLException e) {
                log.error("Connection to DB ERROR: {}", e.getMessage());
            }
        }

        return connection;
    }

}