package queries;

public class TeamQueries {
    public static final String CREATE_NEW_TEAM = "INSERT INTO teams (team_id, title) VALUES (?,?)";
    public static final String ADD_TEAM_IN_SEASON = "INSERT INTO team_seasons (team_id, points_won, set_won, lost_points, lost_set, wins, loses, season, points_per_season)" +
            " VALUES (?, ?::integer[], ?::integer[], ?::integer[], ?::integer[], ?, ?, ?::numeric, ?)" +
            " ON CONFLICT (team_id, season)" +
            " DO NOTHING;";
    public static final String CHECK_TEAM_IN_SEASON_EXISTS = "SELECT * FROM team_seasons WHERE team_id = ? AND season = ?::numeric";
    public static final String UPDATE_INFO_TEAM = "UPDATE team_seasons" +
            " SET" +
            " points_won = array_cat(team_seasons.points_won, ?::integer[])," +
            " set_won = array_cat(team_seasons.set_won, ?::integer[])," +
            " lost_points = array_cat(team_seasons.lost_points, ?::integer[])," +
            " lost_set = array_cat(team_seasons.lost_set, ?::integer[])," +
            " wins = wins + ?," +
            " loses = loses + ?," +
            " points_per_season = points_per_season + ?"  +
            " WHERE team_id = ? AND season = ?::numeric;";
    public static final String CHECK_TEAM_EXISTS = "SELECT * FROM teams WHERE team_id = ?";
    public static final String GET_TABLE_RESULT = "SELECT teams.title, (team_seasons.wins + team_seasons.loses) AS count_games, wins, loses, points_per_season FROM team_seasons" +
            " JOIN teams USING (team_id)" +
            " WHERE season = ?" +
            " ORDER BY points_per_season DESC;";
}
