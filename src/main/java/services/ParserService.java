package services;

import infrastructure.configuration.LastUpdateParsingProperties;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.HashMap;

public class ParserService {
    private final Logger logger = LoggerFactory.getLogger(ParserService.class);
    private static final String URL2024 = "https://volley.ru/calendar/01H81B30CJ2JJ30DR5E7GH1CX7/tsentr";
    private static final String URL2025 = "https://volley.ru/calendar/01J3YZCG8SGKBB188BNM6009Y9/tsentr";
    private static final String CSS_TEAMS2024 = "body > div.page.page--glb > main > div.main-content-internal > div:nth-child(2) > div > table:nth-child(1) > tbody >tr >td:nth-child(1)";
    private static final String CSS_GAMES2024 = "body > div.page.page--glb > main > div.main-content-internal > div:nth-child(4) > div > table > tbody > tr";

    private static final String CSS_TEAMS2025 = "body > div.page.page--glb > main > div.main-content-internal > div:nth-child(3) > div > table:nth-child(1) > tbody >tr >td:nth-child(1)";
    private static final String CSS_GAMES2025 = "body > div.page.page--glb > main > div.main-content-internal > div:nth-child(5) > div > table > tbody > tr";

    private static final String DATE_END_SEASON2024 = "28.04.2024 00:00";

    //    private static final String DATE_END_SEASON2025 = "27.04.2025 00:00";
    private static final int DATE_INDEX = 0;
    private static final int TIME_INDEX = 1;
    private static final int TEAM1_INDEX = 3;
    private static final int TEAM2_INDEX = 5;
    private static final int SCORES_INDEX = 7;
    private static final int SCORES_SET_INDEX = 1;
    private static final int SCORES_POINT_INDEX = 0;
    private final GameService gameService;
    private final TeamService teamService;
    private final PlayerService playerService;
    private static final HashMap<String, Integer> teamIdMap = new HashMap<>();
    private static final HashMap<Integer, String> teamHrefMap = new HashMap<>();

    public ParserService(GameService gameService, TeamService teamService, PlayerService playerService) {
        this.gameService = gameService;
        this.teamService = teamService;
        this.playerService = playerService;
    }


    //если экстренно прервать, а потом успешно загрузить, то некоторые данные дублируются
    public void parsing() throws IOException {
        LocalDateTime lastUpdate = LastUpdateParsingProperties.loadLastUpdate();
        if (lastUpdate == null || lastUpdate.isBefore(getFormattedDateTime(DATE_END_SEASON2024))) {
            parsingTeams(URL2024, CSS_TEAMS2024);
            parsingGames(URL2024, CSS_GAMES2024, 2024, lastUpdate);
            parsingPlayers(2024);
            parsingTeams(URL2025, CSS_TEAMS2025);
        }
        parsingGames(URL2025, CSS_GAMES2025, 2025, lastUpdate);
        parsingPlayers(2025);

        LastUpdateParsingProperties.saveLastUpdate(LocalDateTime.now());
    }

    private void parsingTeams(String url, String cssTeams) throws IOException {
        Document document = Jsoup.connect(url).get();
        Elements teams = document.select(cssTeams);
        for (Element team : teams) {
            int teamId = Integer.parseInt(team.attr("data-teamid"));
            Elements hrefs = team.select("td>a[href]");
            String href = STR."https://volley.ru\{hrefs.attr("href")}";
            String nameTeam = team.text();
            if (teamIdMap.containsKey(nameTeam)) {
                teamHrefMap.put(teamId, href);
            } else {
                teamId = teamIdMap.size();
                teamIdMap.put(nameTeam, teamId);
                teamHrefMap.put(teamId, href);
                teamService.createTeam(teamId, nameTeam);
            }
        }
    }

    private void parsingPlayers(int season) {
        for (int i = 0; i < teamHrefMap.size(); i++) {
            String url = teamHrefMap.get(i);
            try {
                Document document = Jsoup.connect(url).get();
                Elements players = document.select("#index-0 > div > a.team-card > div");
                for (Element player : players) {
                    Elements playerInfo = player.select("div");
                    String fio = playerInfo.select("div.team-card-info__name").text();
                    Elements numberElement = playerInfo.select("div.team-card-info__number");

                    String gameNumberStr = numberElement.text();
                    int gameNumber = checkIntOrEmpty(gameNumberStr, 2);

                    String ageStr = playerInfo.select("div.team-card-info__age").text();
                    int age = checkIntOrEmpty(ageStr, 2);

                    String heightStr = playerInfo.select("div.team-card-info__height").text();
                    int height = checkIntOrEmpty(heightStr, 3);
                    String role = playerInfo.select("div.team-card-info__role span").text();
                    playerService.createPlayer(fio, age, gameNumber, height, i, role, season);
                }
            } catch (IOException e) {
                System.err.println(STR."Ошибка при получении ссылки: \{url}");
                logger.error(e.getMessage());
            }
        }
    }

    private int checkIntOrEmpty(String text, int endSub) {
        if (!text.isEmpty()) {
            if (text.length() == 1) {
                return Integer.parseInt(text);
            }
            return Integer.parseInt(text.substring(0, endSub));
        }
        return 0;
    }

    private void parsingGames(String url, String cssGames, int season, LocalDateTime lastUpdate) throws IOException {
        Document document = Jsoup.connect(url).get();
        Elements elements = document.select(cssGames);
        for (Element element : elements) {
            Elements game = element.select("td");
            String dateTimePart = STR."\{game.get(DATE_INDEX).text()} \{game.get(TIME_INDEX).text().substring(0, 5)}";
            LocalDateTime dateTime = getFormattedDateTime(dateTimePart);
            if (lastUpdate != null && lastUpdate.minusHours(3).isBefore(dateTime)) {
                continue;
            }
            String team1 = game.get(TEAM1_INDEX).text();
            String team2 = game.get(TEAM2_INDEX).text();
            int team1Id, team2Id;
            if (teamIdMap.containsKey(team1)) {
                team1Id = teamIdMap.get(team1);
            } else {
                continue;
            }
            if (teamIdMap.containsKey(team2)) {
                team2Id = teamIdMap.get(team2);
            } else {
                continue;
            }

            Elements result = game.get(SCORES_INDEX).select("div > span");
            String resultText = result.text().trim();
            if (!resultText.isEmpty()) {
                String resultSet = result.get(SCORES_POINT_INDEX).text();
                String resultPoints = result.get(SCORES_SET_INDEX).text();

                int[] pointsScores = getNumbersFromString(resultPoints);
                int[] setScores = getNumbersFromString(resultSet);

                //подсчет количество выигранных и проигранных сетов у обеих команд, определение победителя и проигравшего
                StringBuilder team1SetWin = new StringBuilder();
                StringBuilder team1SetLost = new StringBuilder();
                StringBuilder team2SetWin = new StringBuilder();
                StringBuilder team2SetLost = new StringBuilder();
                int team1PointsPerSeason = 0;
                int team2PointsPerSeason = 0;
                int team1Win = 0, team2Win = 0, team1Lost = 0, team2Lost = 0;
                if (setScores[0] > setScores[1]) {
                    team1Win = 1;
                    team2Lost = 1;
                    team2SetWin.append(STR."{\{setScores[1]}}");
                    team1SetLost.append(STR."{\{setScores[1]}}");
                    team1SetWin.append(STR."{\{setScores[0]}}");
                    team2SetLost.append(STR."{\{setScores[0]}}");
                } else {
                    team2Win = 1;
                    team1Lost = 1;
                    team1SetWin.append(STR."{\{setScores[0]}}");
                    team2SetLost.append(STR."{\{setScores[0]}}");
                    team2SetWin.append(STR."{\{setScores[1]}}");
                    team1SetLost.append(STR."{\{setScores[1]}}");
                }

                if (setScores[0] + setScores[1] == 5) {
                    if (team1Win == 1) {
                        team1PointsPerSeason = 2;
                        team2PointsPerSeason = 1;
                    } else {
                        team1PointsPerSeason = 1;
                        team2PointsPerSeason = 2;
                    }
                } else {
                    if (team1Win == 1) {
                        team1PointsPerSeason = 3;
                    } else {
                        team2PointsPerSeason = 3;
                    }
                }
                // *конец*

                //Забитые и пропущенные очки обеих команд
                StringBuilder pointTeam1Win = new StringBuilder("{");
                StringBuilder pointTeam1Lost = new StringBuilder("{");
                StringBuilder pointTeam2Win = new StringBuilder("{");
                StringBuilder pointTeam2Lost = new StringBuilder("{");

                appendPoints(pointTeam1Win, pointsScores, true);
                appendPoints(pointTeam1Lost, pointsScores, false);
                appendPoints(pointTeam2Win, pointsScores, false);
                appendPoints(pointTeam2Lost, pointsScores, true);
                // *конец*

                teamService.updateTeam(team1Id, pointTeam1Win, team1SetWin, pointTeam1Lost, team1SetLost, team1Win, team1Lost, team1PointsPerSeason,season);
                teamService.updateTeam(team2Id, pointTeam2Win, team2SetWin, pointTeam2Lost, team2SetLost, team2Win, team2Lost, team2PointsPerSeason,season);
                gameService.createGame(dateTime, team1Id, team2Id, resultPoints, resultSet, season);
            } else {
                teamService.updateTeam(team1Id, new StringBuilder("{}"), new StringBuilder("{}"), new StringBuilder("{}"), new StringBuilder("{}"), 0, 0, 0, season);
                teamService.updateTeam(team2Id, new StringBuilder("{}"), new StringBuilder("{}"), new StringBuilder("{}"), new StringBuilder("{}"), 0, 0, 0, season);
                gameService.createGame(dateTime, team1Id, team2Id, "", "", season);
            }


        }
    }

    private void appendPoints(StringBuilder stringPoints, int[] points, boolean isWinner) {
        for (int i = 0; i < points.length; i += 2) {
            int team1Points = points[i];
            int team2Points = points[i + 1];
            stringPoints.append(isWinner ? team1Points : team2Points)
                    .append(", ");
        }
        if (stringPoints.length() > 2) {
            stringPoints.setLength(stringPoints.length() - 2); // удалить последнюю запятую и пробел
        }
        stringPoints.append('}');
    }

    private int[] getNumbersFromString(String text) {
        String[] str = text.replaceAll("\\D", " ").split("\\s+");
        return Arrays.stream(str)
                .filter(s -> !s.isEmpty())
                .mapToInt(Integer::parseInt)
                .toArray();
    }

    private LocalDateTime getFormattedDateTime(String dateTime) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm");
        return LocalDateTime.parse(dateTime, formatter);
    }


}


