package domain;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
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
    private String resultPoint;
    private String resultSet;
    private int season;
}

