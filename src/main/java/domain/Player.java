package domain;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class Player {
    private int id;
    private String fio;
    private int team_id;
}
