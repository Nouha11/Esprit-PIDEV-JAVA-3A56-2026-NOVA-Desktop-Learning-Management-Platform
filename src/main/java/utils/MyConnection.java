package utils;

import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Properties;

public class MyConnection {
    private final String URL;
    private final String USER;
    private final String PWD;
    private Connection cnx;
    private static MyConnection instance;

    private MyConnection() {
        // Load DB credentials from config.properties
        Properties props = new Properties();
        String url  = "mysql://uhzvlai4a0uuce2l:eV3waSr2QObzP7C0uqUz@basxv5iidl2hqph9dbiu-mysql.services.clever-cloud.com:3306/basxv5iidl2hqph9dbiu"; // fallback
        String user = "rootuhzvlai4a0uuce2l";
        String pwd  = "eV3waSr2QObzP7C0uqUz";
        try (InputStream in = MyConnection.class.getResourceAsStream("/config.properties")) {
            if (in != null) {
                props.load(in);
                String cfgUrl  = props.getProperty("DB_URL");
                String cfgUser = props.getProperty("DB_USER");
                String cfgPwd  = props.getProperty("DB_PASSWORD");
                if (cfgUrl != null && !cfgUrl.isBlank())  url  = cfgUrl;
                if (cfgUser != null && !cfgUser.isBlank()) user = cfgUser;
                if (cfgPwd  != null)                       pwd  = cfgPwd;
            }
        } catch (IOException e) {
            System.err.println("[MyConnection] Could not load config.properties, using defaults.");
        }
        URL  = url;
        USER = user;
        PWD  = pwd;

        try {
            cnx = DriverManager.getConnection(URL, USER, PWD);
            System.out.println("Database Connection Successful.");
            runMigrations();
        } catch (SQLException e) {
            System.err.println("Database Connection Failed!");
            System.err.println(e.getMessage());
        }
    }

    public static MyConnection getInstance() {
        if (instance == null) {
            instance = new MyConnection();
        }
        return instance;
    }

    public Connection getCnx() {
        return cnx;
    }

    /**
     * Auto-migration: adds columns that may be missing on teammates' databases.
     * Compatible with MySQL 5.7+ — checks information_schema before altering.
     */
    private void runMigrations() {
        if (cnx == null) return;
        // Each entry: { tableName, columnName, columnDefinition }
        String[][] columns = {
            { "user", "profile_picture", "VARCHAR(500) NULL DEFAULT NULL" },
            { "user", "totp_enabled",    "TINYINT(1) NOT NULL DEFAULT 0"  },
            { "user", "totp_secret",     "VARCHAR(255) NULL DEFAULT NULL"  },
            { "user", "face_token",      "VARCHAR(255) NULL DEFAULT NULL"  },
            { "user", "country_code",    "VARCHAR(5) NULL DEFAULT NULL"    }
        };
        String checkSql = "SELECT COUNT(*) FROM information_schema.COLUMNS " +
                          "WHERE TABLE_SCHEMA = DATABASE() " +
                          "AND TABLE_NAME = ? AND COLUMN_NAME = ?";
        try {
            for (String[] col : columns) {
                try (java.sql.PreparedStatement check = cnx.prepareStatement(checkSql)) {
                    check.setString(1, col[0]);
                    check.setString(2, col[1]);
                    java.sql.ResultSet rs = check.executeQuery();
                    if (rs.next() && rs.getInt(1) == 0) {
                        // Column does not exist — add it
                        String alter = "ALTER TABLE " + col[0] + " ADD COLUMN " + col[1] + " " + col[2];
                        try (Statement st = cnx.createStatement()) {
                            st.execute(alter);
                            System.out.println("[Migration] Added column: " + col[0] + "." + col[1]);
                        }
                    }
                }
            }
            System.out.println("[Migration] Schema up to date.");
        } catch (SQLException e) {
            System.err.println("[Migration] Failed: " + e.getMessage());
        }
    }
}
