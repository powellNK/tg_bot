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
    public static final String GET_TEAMS_FROM_SEASON = "SELECT team_id, title FROM teams" +
            " JOIN team_seasons USING (team_id)" +
            " WHERE season = ?;";
    public static final String GET_STATISTICS_TEAM_IN_SEASON = "SELECT" +
            "    title, wins, loses,points_per_season," +
            "    avg_point_won, avg_point_lost," +
            "    avg_set_won, avg_set_lost," +
            "    min_points_won, min_points_lost" +
            " FROM" +
            "         team_seasons as ts" +
            "        LEFT JOIN LATERAL ( SELECT round(avg(points_won_table),3) AS avg_point_won," +
            "                                   min(points_won_table) AS min_points_won" +
            "                            FROM unnest(points_won) AS points_won_table) AS pw ON TRUE" +
            "        LEFT JOIN LATERAL ( SELECT round(avg(points_lost_table),3) AS avg_point_lost," +
            "                                   min(points_lost_table) AS min_points_lost" +
            "                            FROM unnest(lost_points) AS points_lost_table) AS pl ON TRUE" +
            "        LEFT JOIN LATERAL ( SELECT round(avg(set_won_table),3) AS avg_set_won" +
            "                            FROM unnest(set_won) AS set_won_table) AS sw ON TRUE" +
            "        LEFT JOIN LATERAL ( SELECT round(avg(set_lost_table),3) AS avg_set_lost" +
            "                            FROM unnest(lost_set) AS set_lost_table) AS sl ON TRUE" +
            "     JOIN teams t ON ts.team_id = t.team_id" +
            " WHERE ts.team_id = ? and season = ? ;";
    public static final String GET_FULL_STATISTICS = "SELECT" +
            "    title, " +
            "    SUM(ts.wins) AS wins, " +
            "    SUM(ts.loses) AS loses, " +
            "    SUM(ts.points_per_season) AS points_per_season, " +
            "    ROUND(AVG(pw.avg_point_won), 3) AS avg_point_won, " +
            "    ROUND(AVG(pl.avg_point_lost), 3) AS avg_point_lost, " +
            "    ROUND(AVG(sw.avg_set_won), 3) AS avg_set_won, " +
            "    ROUND(AVG(sl.avg_set_lost), 3) AS avg_set_lost, " +
            "    MIN(pw.min_points_won) AS min_points_won, " +
            "    MIN(pl.min_points_lost) AS min_points_lost " +
            " FROM" +
            "         team_seasons as ts" +
            "        LEFT JOIN LATERAL ( SELECT round(avg(points_won_table),3) AS avg_point_won," +
            "                                   min(points_won_table) AS min_points_won" +
            "                            FROM unnest(points_won) AS points_won_table) AS pw ON TRUE" +
            "        LEFT JOIN LATERAL ( SELECT round(avg(points_lost_table),3) AS avg_point_lost," +
            "                                   min(points_lost_table) AS min_points_lost" +
            "                            FROM unnest(lost_points) AS points_lost_table) AS pl ON TRUE" +
            "        LEFT JOIN LATERAL ( SELECT round(avg(set_won_table),3) AS avg_set_won" +
            "                            FROM unnest(set_won) AS set_won_table) AS sw ON TRUE" +
            "        LEFT JOIN LATERAL ( SELECT round(avg(set_lost_table),3) AS avg_set_lost" +
            "                            FROM unnest(lost_set) AS set_lost_table) AS sl ON TRUE" +
            "     JOIN teams t ON ts.team_id = t.team_id" +
            "  GROUP BY t.title, ts.team_id";
}
