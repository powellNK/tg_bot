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

    public static User createNew(ResultSet resultSet){
        try {
            return User.builder()
                    .telegramId(resultSet.getLong("telegram_id"))
                    .build();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
}
