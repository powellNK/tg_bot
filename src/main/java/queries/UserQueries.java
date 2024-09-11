package queries;

public class UserQueries {
    public static final String CHECK_USER_EXISTS_BY_ID = "SELECT * FROM users WHERE telegram_id = ?";
    public static final String CHECK_IF_ITS_ADMIN_ = "SELECT role_admin FROM users WHERE telegram_id = ?";
    public static final String CREATE_NEW_USER = "INSERT INTO users (telegram_id, telegram_username) VALUES (?,?)";
}
