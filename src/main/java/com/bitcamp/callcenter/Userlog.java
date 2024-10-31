package com.bitcamp.callcenter;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class Userlog {
    private static Connection conn = getConnection();

    private int id;
    private String given_name;
    private String family_name;
    private String email;
    private String tel;
    private String category;
    private String brand;
    private LocalDateTime timestamp;

    private Userlog(int id, String given_name, String family_name, String email, String tel, String category,
            String brand, LocalDateTime timestamp) {
        this.id = id;
        this.given_name = given_name;
        this.family_name = family_name;
        this.email = email;
        this.tel = tel;
        this.category = category;
        this.brand = brand;
        this.timestamp = timestamp;
    }

    public Userlog(ResultSet rs) throws SQLException {
        this(
            rs.getInt("id"),
            rs.getString("given_name"),
            rs.getString("family_name"),
            rs.getString("email"),
            rs.getString("tel"),
            rs.getString("category"),
            rs.getString("brand"),
            rs.getTimestamp("timestamp").toLocalDateTime()
        );
    }

    public Userlog(Map<String, String> data) {
        this(
            -1,
            data.get("given-name"),
            data.get("family-name"),
            data.get("email"),
            data.get("tel"),
            data.get("category"),
            data.get("brand"),
            LocalDateTime.MIN
        );
    }

    public int getId() {
        return id;
    }

    public String getGiven_name() {
        return given_name;
    }

    public void setGiven_name(String given_name) {
        this.given_name = given_name;
    }

    public String getFamily_name() {
        return family_name;
    }

    public void setFamily_name(String family_name) {
        this.family_name = family_name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getTel() {
        return tel;
    }

    public void setTel(String tel) {
        this.tel = tel;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getBrand() {
        return brand;
    }

    public void setBrand(String brand) {
        this.brand = brand;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }

    public static int insert(Userlog obj) {
        String query = "INSERT INTO call_center.userlog ("
                + "given_name, family_name, email, tel, category, brand, timestamp) " + "VALUES(?,?,?,?,?,?,?)";

        try (PreparedStatement stat = conn.prepareStatement(query, Statement.RETURN_GENERATED_KEYS)) {
            Timestamp timestamp = Timestamp.valueOf(LocalDateTime.now());
            stat.setString(1, obj.given_name);
            stat.setString(2, obj.family_name);
            stat.setString(3, obj.email);
            stat.setString(4, obj.tel);
            stat.setString(5, obj.category);
            stat.setString(6, obj.brand);
            stat.setTimestamp(7, timestamp);

            int exec = stat.executeUpdate();
            conn.commit();

            ResultSet generatedKeys = stat.getGeneratedKeys();
            if (generatedKeys.next()) {
                obj.id = generatedKeys.getInt(1);
                obj.timestamp = timestamp.toLocalDateTime();
                return exec;
            }
            throw new SQLException("Could not retrieve id");
        } catch (SQLException e) {
            e.printStackTrace();
            if (conn != null) {
                try {
                    conn.rollback();
                } catch (SQLException e1) {
                    e1.printStackTrace();
                }
            }
        }
        return -1;
    }
    public static int insert(Map<String, String> data) {
       return insert(new Userlog(data));
    }

    public static boolean isEmpty() {
        String query = "SELECT * FROM call_center.userlog LIMIT 1";

        try (Statement stat = conn.createStatement()) {
            ResultSet rs = stat.executeQuery(query);
            conn.commit();
            if (rs.next())
                return false;

        } catch (SQLException e) {
            e.printStackTrace();
            if (conn != null) {
                try {
                    conn.rollback();
                } catch (SQLException e1) {
                    e1.printStackTrace();
                }
            }
        }
        return true;
    }

    public static Optional<Userlog> get(int id) {
        String query = "SELECT * FROM call_center.userlog WHERE id = ?";

        try (PreparedStatement stat = conn.prepareStatement(query)) {
            stat.setInt(1, id);
            ResultSet rs = stat.executeQuery(query);
            conn.commit();
            if (rs.next())
                return Optional.ofNullable(new Userlog(rs));

        } catch (SQLException e) {
            e.printStackTrace();
            if (conn != null) {
                try {
                    conn.rollback();
                } catch (SQLException e1) {
                    e1.printStackTrace();
                }
            }
        }
        return Optional.empty();
    }

    public static Connection getConnection() {
        try {
            Connection conn = Main.getMySQLConnection();
            return conn;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static List<Userlog> getAll() {
        String query = "SELECT * FROM call_center.userlog";
        List<Userlog> list = new ArrayList<>();
        try (PreparedStatement stat = conn.prepareStatement(query)) {
            ResultSet rs = stat.executeQuery(query);
            conn.commit();

            while (rs.next()) {
                list.add(new Userlog(rs));
            }

        } catch (SQLException e) {
            e.printStackTrace();
            if (conn != null) {
                try {
                    conn.rollback();
                } catch (SQLException e1) {
                    e1.printStackTrace();
                }
            }
        }
        return list;
    }

    public static int update(int id, Userlog obj) {
        String query = "UPDATE call_center.userlog SET id = ?, given_name = ?, family_name = ?, email = ?, tel = ?, category = ?, brand = ?, timestamp = ? WHERE id = ?";

        try (PreparedStatement stat = conn.prepareStatement(query)) {
            Timestamp timestamp = Timestamp.valueOf(LocalDateTime.now());
            stat.setString(1, obj.given_name);
            stat.setString(2, obj.family_name);
            stat.setString(3, obj.email);
            stat.setString(4, obj.tel);
            stat.setString(5, obj.category);
            stat.setString(6, obj.brand);
            stat.setTimestamp(7, timestamp);

            stat.setInt(8, id);
            int exec = stat.executeUpdate();
            conn.commit();

            ResultSet generatedKeys = stat.getGeneratedKeys();
            if (generatedKeys.next()) {
                obj.timestamp = timestamp.toLocalDateTime();
                return exec;
            }
            throw new SQLException("Could not retrieve id");
        } catch (SQLException e) {
            e.printStackTrace();
            if (conn != null) {
                try {
                    conn.rollback();
                } catch (SQLException e1) {
                    e1.printStackTrace();
                }
            }
        }
        return -1;

    }

    public static int delete(int id) {
        String query = "DELETE FROM call_center.userlog WHERE id = ?";
        try (PreparedStatement stat = conn.prepareStatement(query)) {

            stat.setInt(1, id);
            int exec = stat.executeUpdate();
            conn.commit();

            return exec;
        } catch (SQLException e) {
            e.printStackTrace();
            if (conn != null) {
                try {
                    conn.rollback();
                } catch (SQLException e1) {
                    e1.printStackTrace();
                }
            }
        }
        return -1;
    }
}
