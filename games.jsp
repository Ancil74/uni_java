<%@ page language="java" contentType="text/html; charset=UTF-8" import="java.util.*" %>
<%!
    class Game {
        String title, genre, platform;
        double rating;
        Game(String t, String g, String p, double r) {
            title = t; genre = g; platform = p; rating = r;
        }
    }
%>
<%
    List<Game> games = new ArrayList<>();
    games.add(new Game("Elden Ring", "RPG", "PC/PS5", 9.6));
    games.add(new Game("Valorant", "FPS", "PC", 8.4));
    games.add(new Game("Hades", "Roguelike", "PC/Switch", 9.3));
    games.add(new Game("FIFA 24", "Sports", "PC/PS5", 7.5));
    games.add(new Game("Celeste", "Platformer", "PC/Switch", 9.1));

    String genre = request.getParameter("genre");
    if (genre == null) genre = "All";
%>
<!DOCTYPE html>
<html>
<head>
    <title>Game Library</title>
    <style>
        body { font-family: Arial, sans-serif; background: #111; color: #eee; padding: 20px; }
        h1 { color: #00e676; }
        table { border-collapse: collapse; width: 100%; }
        th, td { padding: 10px; border-bottom: 1px solid #333; text-align: left; }
        th { background: #1e1e1e; color: #00e676; }
        select, button { padding: 6px; }
        .high { color: #ffd600; font-weight: bold; }
    </style>
</head>
<body>
    <h1>🎮 Game Library</h1>

    <form method="get">
        Genre:
        <select name="genre">
            <option <%= genre.equals("All") ? "selected" : "" %>>All</option>
            <option <%= genre.equals("RPG") ? "selected" : "" %>>RPG</option>
            <option <%= genre.equals("FPS") ? "selected" : "" %>>FPS</option>
            <option <%= genre.equals("Roguelike") ? "selected" : "" %>>Roguelike</option>
            <option <%= genre.equals("Sports") ? "selected" : "" %>>Sports</option>
            <option <%= genre.equals("Platformer") ? "selected" : "" %>>Platformer</option>
        </select>
        <button type="submit">Filter</button>
    </form>
    <br>

    <table>
        <tr><th>Title</th><th>Genre</th><th>Platform</th><th>Rating</th></tr>
        <%
            int count = 0;
            for (Game g : games) {
                if (genre.equals("All") || g.genre.equals(genre)) {
                    count++;
        %>
        <tr>
            <td><%= g.title %></td>
            <td><%= g.genre %></td>
            <td><%= g.platform %></td>
            <td class="<%= g.rating >= 9.0 ? "high" : "" %>"><%= g.rating %></td>
        </tr>
        <%
                }
            }
        %>
    </table>

    <p>Showing <%= count %> game(s) · Generated: <%= new Date() %></p>
</body>
</html>
