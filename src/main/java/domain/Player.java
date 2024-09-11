package domain;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

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
}
