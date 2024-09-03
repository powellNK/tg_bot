package domain;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.util.Date;
@Getter
@Setter
@Builder
public class Game {
    private int id;
    private Date date;
    private String team1Title;
    private String team2Title;
    private String cityName;
    private String resultSet;
    private String resultGame;
}

