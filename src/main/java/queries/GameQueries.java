package queries;

public class GameQueries {
    public static final String CREATE_NEW_GAME = "INSERT INTO games (date_time, team1_id, team2_id, points, sets, season) " +
            "VALUES (?, ?, ? , ? , ? , ?::numeric);";
}
