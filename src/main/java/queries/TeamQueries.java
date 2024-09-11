package queries;

public class TeamQueries {
    public static final String CREATE_NEW_TEAM = "INSERT INTO teams (team_id, title) VALUES (?,?)";
    public static final String ADD_TEAM_IN_SEASON = "INSERT INTO team_seasons (team_id, points_won, set_won, lost_points,lost_set, wins, loses, season)" +
            "VALUES (?, '?', '?', '?', '?', ? ,?, ?::numeric)" +
            "ON CONFLICT (team_id, season)" +
            "DO NOTHING";
    public static final String CHECK_TEAM_IN_SEASON_EXISTS = "SELECT * FROM team_seasons WHERE team_id = ? and season = ?::numeric";

    public static final String UPDATE_INFO_TEAM = "UPDATE team_seasons" +
            "    SET" +
            "    points_won = array_cat(team_seasons.points_won, '?')," +
            "    set_won = array_cat(team_seasons.set_won, '?')," +
            "    lost_points = array_cat(team_seasons.lost_points, '?')," +
            "    lost_set = array_cat(team_seasons.lost_set, '?')," +
            "    wins = wins + ?," +
            "    loses = loses + ?" +
            "WHERE team_id = ? and season = ?::numeric;";
}
