package queries;

public class GameQueries {
    public static final String CREATE_NEW_GAME = "INSERT INTO games (date_time, team1_id, team2_id, points, sets, season) " +
            "VALUES (?, ?, ? , ? , ? , ?::numeric);";

    public static final String GET_ALL_GAMES_IN_SEASON = "SELECT game_id,games.date_time, t1.title AS team1_title, t2.title AS team2_title, team1_id, team2_id, points, sets, season" +
            " FROM games" +
            " JOIN teams t1 ON games.team1_id = t1.team_id" +
            " JOIN teams t2 ON games.team2_id = t2.team_id" +
            " WHERE season = ?::numeric;";
    public static final String GET_UPCOMING_GAMES_IN_SEASON = "SELECT game_id,games.date_time, t1.title AS team1_title, t2.title AS team2_title, team1_id, team2_id, points, sets, season" +
            " FROM games" +
            " JOIN teams t1 ON games.team1_id = t1.team_id" +
            " JOIN teams t2 ON games.team2_id = t2.team_id" +
            " WHERE season = ?::numeric " +
            " AND games.date_time > ? ;";
    public static final String GET_PAST_GAMES_IN_SEASON = "SELECT game_id,games.date_time, t1.title AS team1_title, t2.title AS team2_title, team1_id, team2_id, points, sets, season" +
            " FROM games" +
            " JOIN teams t1 ON games.team1_id = t1.team_id" +
            " JOIN teams t2 ON games.team2_id = t2.team_id" +
            " WHERE season = ?::numeric " +
            " AND games.date_time < ? ;";
    public static final String GET_GAMES_TEAM = "SELECT game_id,games.date_time, t1.title AS team1_title, t2.title AS team2_title, team1_id, team2_id, points, sets, season" +
            " FROM games" +
            " JOIN teams t1 ON games.team1_id = t1.team_id" +
            " JOIN teams t2 ON games.team2_id = t2.team_id" +
            " WHERE season = ?::numeric AND (games.team1_id = ? OR games.team2_id = ?);";
}
