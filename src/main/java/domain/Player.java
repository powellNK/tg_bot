package domain;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.format.DateTimeFormatter;

@Getter
@Setter
@Builder
public class Player {
    private String fio;
    private int age;
    private int gameNumber;
    private int height;
    private String role;
    private short team_id;
    private String teamTitle;
    private int season;


    public static Player createPlayer(ResultSet resultSet) {
        try {
            return Player.builder()
                    .fio(resultSet.getString("fio"))
                    .age(resultSet.getInt("age"))
                    .gameNumber(resultSet.getInt("game_number"))
                    .height(resultSet.getInt("height"))
                    .role(resultSet.getString("role"))
                    .build();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("<pre>").append(fio).append("</pre>\n");

        if (age > 0) {
            sb.append("Возраст: ").append(age).append("\n");
        }

        if (gameNumber > 0) {
            sb.append("Игровой номер: ").append(gameNumber).append("\n");
        }

        if (height > 0) {
            sb.append("Рост: ").append(height).append("\n");
        }

        if (!role.isEmpty()) {
            sb.append("Позиция: ").append(role).append("\n");
        }
        return sb.toString();
    }
}
