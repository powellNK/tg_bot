package queries;

public class PlayerQueries {
    public static final String CREATE_NEW_PLAYER = "INSERT INTO players(fio, age, game_number, height, role) VALUES (?::varchar, ?::numeric, ?::numeric, ?::numeric, ?::varchar)";
    public static final String IS_PLAYER_EXISTS = "SELECT * FROM players WHERE fio = ?::varchar";
    public static final String ADD_NEW_SEASON_PLAYER = "INSERT INTO player_seasons (player_id, team_id, season) " +
            "SELECT players.player_id, ?, ?::numeric " +
            "FROM players " +
            "WHERE players.fio = ?" +
            "ON CONFLICT (player_id, team_id, season) DO NOTHING;";
    public static final String GET_PLAYERS = "SELECT fio, age, players.game_number, players.height, role FROM players" +
            " JOIN player_seasons USING (player_id)" +
            " WHERE season = ? and team_id = ?";
}
