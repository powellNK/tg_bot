package queries;

public class PlayerQueries {
    public static final String CREATE_NEW_PLAYER = "INSERT INTO players(fio, age, game_number, height, role) VALUES (?, ?::numeric, ?::numeric, ?::numeric, ?)";
    public static final String IS_PLAYER_EXISTS = "SELECT * FROM players WHERE fio = ?::varchar";
}
