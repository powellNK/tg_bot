package domain;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import java.sql.ResultSet;
import java.sql.SQLException;

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
    private int minPointsWon;
    private int minPointsLost;
    private int countGames;
    private int season;
    private boolean showFullStats;


    public static Team createTableResult(ResultSet resultSet) {
        try {
            return Team.builder()
                    .title(resultSet.getString("title"))
                    .countGames(resultSet.getInt("count_games"))
                    .wins(resultSet.getInt("wins"))
                    .loses(resultSet.getInt("loses"))
                    .pointsPerSeason(resultSet.getInt("points_per_season"))
                    .showFullStats(false)
                    .build();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public static Team createTeam(ResultSet resultSet) {
        try {
            return Team.builder()
                    .id(resultSet.getShort("team_id"))
                    .title(resultSet.getString("title"))
                    .build();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public static Team createFullStats(ResultSet resultSet) {
        try {
            return Team.builder()
                    .title(resultSet.getString("title"))
                    .wins(resultSet.getInt("wins"))
                    .loses(resultSet.getInt("loses"))
                    .pointsPerSeason(resultSet.getInt("points_per_season"))
                    .avgPointsWon(resultSet.getDouble("avg_point_won"))
                    .avgPointsLost(resultSet.getDouble("avg_point_lost"))
                    .avgSetWon(resultSet.getDouble("avg_set_won"))
                    .avgSetLost(resultSet.getDouble("avg_set_lost"))
                    .minPointsWon(resultSet.getInt("min_points_won"))
                    .minPointsLost(resultSet.getInt("min_points_lost"))
                    .showFullStats(true)
                    .build();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public String toString() {
        if (showFullStats) {
            return STR."<pre>            \{title} </pre>\n Победы: \{wins}  Поражения: \{loses}\n Очки: \{pointsPerSeason} Количество игр: \{wins+loses} \n Среднее количество выигранных очков: \{avgPointsWon}\n Среднее количество проигранных очков: \{avgPointsLost}\n Среднее количество выигранных сетов: \{avgSetWon}\n Среднее количество проигранных сетов: \{avgSetLost}\n Минимальное количество выигранных очков: \{minPointsWon}\n Минимальное количество проигранных очков: \{minPointsLost}";
        } else {
            return STR."\{title}\n                        \{countGames}      \{wins}      \{loses}      \{pointsPerSeason}";
        }
    }
}