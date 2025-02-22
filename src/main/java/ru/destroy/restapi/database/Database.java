package ru.destroy.restapi.database;

import ru.destroy.restapi.crypto.SCrypt;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class Database {

    private static final String DB_URL = "jdbc:sqlite:database.db";

    private static Database database = null;

    public static Database getDatabase() {
        if (database != null) return database;
        database = new Database();
        return database;
    }

    private Database() {
        try (Connection conn = connect()) {
            if (conn != null) {
                createTable(conn);
            }
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }

    private Connection connect() {
        Connection conn = null;
        try {
            conn = DriverManager.getConnection(DB_URL);
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
        return conn;
    }

    public boolean isUserExists(String user) {
        String sql = "SELECT name FROM users WHERE name='"+user+"'";
        try (Connection conn = connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            ResultSet set = pstmt.executeQuery();

            return set.next();

        } catch (SQLException e) {
            return false;
        }
    }

    public boolean isPasswordCorrect(String user, String password) {
        if (isUserExists(user)) {
            String sql = "SELECT name, password, salt, length FROM users WHERE name='"+user+"'";
            try (Connection conn = connect();
                 PreparedStatement pstmt = conn.prepareStatement(sql)) {
                ResultSet set = pstmt.executeQuery();

                if (set.next()) {
                    String pass = set.getString(2);
                    String salt = set.getString(3);
                    int length = set.getInt(4);

                    String hex = SCrypt.hashPassword(password,new SCrypt.Salt(SCrypt.Salt.saltToByteArray(salt),length));

                    return hex.equals(pass);
                }

            } catch (SQLException e) {
                return false;
            }
        }
        return false;
    }

    // для соли желательно иметь отдельную бд
    // на случай если эту взломают мамкины хацкеры

    private void createTable(Connection conn) {
        String sql = "CREATE TABLE IF NOT EXISTS users (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "name TEXT UNIQUE NOT NULL," +
                "password TEXT NOT NULL," +
                "email TEXT UNIQUE NOT NULL," +
                "salt TEXT NOT NULL," +
                "length INTEGER NOT NULL)";
        try (Statement stmt = conn.createStatement()) {
            stmt.execute(sql);
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }

    public Response insertUser(String name, String password, String email, SCrypt.Salt salt) {
        String sql = "INSERT INTO users(name, password, email, salt, length) VALUES(?, ?, ?, ?, ?)";
        try (Connection conn = connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, name);
            pstmt.setString(2, password);
            pstmt.setString(3, email);
            pstmt.setString(4, salt.saltToString());
            pstmt.setInt(5, salt.length());
            pstmt.executeUpdate();
        } catch (SQLException e) {
            if (e.getMessage().contains("users.email")) return Response.EMAIL_USED;
            if (e.getMessage().contains("users.user")) return Response.EMAIL_USED;
            System.out.println(e.getMessage());
        }
        return Response.APPLY;
    }
}
