package domain;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

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
    private double avgPointsWon;
    private double avgSetWon;
    private double avgPointsLost;
    private double avgSetLost;
    private double minPointsWon;
    private double minPointsLost;
    private int countGames;
    private int season;
}
