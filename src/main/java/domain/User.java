package domain;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.sql.ResultSet;
import java.sql.SQLException;

@Getter
@Setter
@Builder
public class User {
    private long telegramId;
    private String telegramUsername;
    private boolean roleAdmin;

    public static User createNew(ResultSet resultSet){
        try {
            return User.builder()
                    .telegramId(resultSet.getLong("telegram_id"))
                    .telegramUsername(resultSet.getString("telegram_username"))
                    .build();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
}
