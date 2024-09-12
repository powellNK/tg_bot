package domain;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Getter
@Setter
@Builder
public class Team {
    private short id;
    private String title;
    private StringBuilder pointsWon;
    private StringBuilder setWon;
    private StringBuilder pointsLost;
    private StringBuilder setLost;
    private int wins;
    private int loses;
    private int pointsPerSeason;
    private double avgPointsWon;
    private double avgSetWon;
    private double avgPointsLost;
    private double avgSetLost;
    private double minPointsWon;
    private double minPointsLost;
    private int countGames;
    private int season;


    public static Team createTableResult(ResultSet resultSet) {
        try {
            return Team.builder()
                    .title(resultSet.getString("title"))
                    .countGames(resultSet.getInt("count_games"))
                    .wins(resultSet.getInt("wins"))
                    .loses(resultSet.getInt("loses"))
                    .pointsPerSeason(resultSet.getInt("points_per_season"))
                    .build();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public String toString() {
        return STR."\{title}\n                        \{countGames}      \{wins}      \{loses}      \{pointsPerSeason}";
    }
//    <pre>
}