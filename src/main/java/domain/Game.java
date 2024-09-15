package domain;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Getter
@Setter
@Builder
public class Game {
    private int id;
    private LocalDateTime dateTime;
    private short team1Id;
    private short team2Id;
    private String team1Title;
    private String team2Title;
    private String pointResult;
    private String setResult;
    private int season;

    public static Game createGame(ResultSet resultSet) {
        try {
            return Game.builder()
                    .dateTime(resultSet.getTimestamp("date_time").toLocalDateTime())
                    .team1Title(resultSet.getString("team1_title"))
                    .team2Title(resultSet.getString("team2_title"))
                    .pointResult(resultSet.getString("points"))
                    .setResult(resultSet.getString("sets"))
                    .season(resultSet.getInt("season"))
                    .build();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
    @Override
    public String toString() {
        return STR."\{dateTime.format(DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm"))}\n\{team1Title} - \{team2Title}\n   \{setResult}   \{pointResult}   Сезон: \{season}\n";
    }

}

