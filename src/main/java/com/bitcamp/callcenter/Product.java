package com.bitcamp.callcenter;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

public class Product implements Comparable<Product>{
    private static Connection conn = getConnection();

    private int id = -1;
    private String title;
    private String brand;
    private String imageUrl;
    private String price;
    private String price_info;
    private String category;

    private Product(int id, String title, String brand, String imageUrl, String i_price, String i_price_info, String category) {  
        this.id = id;    
        this.title = title;
        this.brand = brand;
        this.imageUrl = imageUrl;
        this.price = i_price;
        this.price_info = i_price_info;
        this.category = category;
    }

    public Product(Element gridItem, String category) {
        try {
            if(!gridItem.className().equals("sn_promo_grid_item_ct")) throw new IllegalArgumentException("passed invalid class:" + gridItem.className()); 
            this.title = gridItem.selectFirst(".i_title").text();
            this.brand = gridItem.selectFirst(".i_brand").text();
            this.imageUrl = gridItem.selectFirst(".i_image").attr("src");
            this.price = gridItem.selectFirst(".i_price").text();
            
            Element i_price_info = gridItem.selectFirst(".i_price_info");
            if(i_price_info != null)
                this.price_info = i_price_info.text();
            if(category != null) {
                this.category = category;
            }

        } catch (Exception e) {
            System.err.println(e.getMessage());
        }
    }

    public Product(ResultSet rs) throws SQLException {
        this(
            rs.getInt("id"),
            rs.getString("title"),
            rs.getString("brand"),
            rs.getString("image_url"),
            rs.getString("price"),
            rs.getString("price_info"),
            rs.getString("category")
        );
    }

    public Element toHtml() {
        Element card = new Element("div");
        try {
            card.addClass("product-item");

            Element image = card.appendElement("img");
            image.addClass("product-item-image");
            image.attr("src", imageUrl);
            image.attr("alt", "https://placehold.co/150x90");
            image.attr("loading", "lazy");

            Element card_body = card.appendElement("div");
            card_body.addClass("product-item-body");
            
            Element card_title = card_body.appendElement("h5");
            if(this.title != null) 
                card_title.text(this.title);

            Element card_text_1 = card_body.appendElement("p");
            if(this.price != null) {
                String[] splitPrice = this.price.split(" ");
                if(splitPrice.length > 2) {
                    card_text_1.append("<span><s>" + splitPrice[0] + "</s></span>&nbsp;");
                    card_text_1.append("<span>" + splitPrice[1] + " " + splitPrice[2] + "</span>");
                } else {
                    card_text_1.text(this.price);
                }
            }

            Element card_text_2 = card_body.appendElement("p");
            if(this.price_info != null)
                card_text_2.text(this.price_info);

        } catch (Exception e) {
            e.printStackTrace();
        }

        return card;
    }
    public static Elements toHtml(List<Product> elements) {
        return new Elements(elements.parallelStream().map(e -> e.toHtml()).toList());
    }

    public static Elements parseAllTerms(String term) {
        List<String> terms = getAllTerms(term);
        if(terms.isEmpty()) return new Elements();

        return terms.parallelStream().collect(Elements::new, (x,y)-> {
            Element e = new Element("option");
            e.attr("value", y);
            e.text(y);

            x.add(e);
        }, (a,b)->a.addAll(b));
    }
    /* 
    public static List<Element> parseIntoColumns(List<Product> products, int quantity) {
        List<Element> cards = toHtml(products);

        List<Element> columns = new ArrayList<>();
        for(int i = 0; i < quantity; i++) {
            Element col = new Element("div");
            col.addClass("product-col");
            columns.add(col);
        }

        IntStream.range(0, cards.size()).forEach(i -> {
            Element col = columns.get(i % quantity);
            col.appendChild(cards.get(i));
        });
            
        return columns;
    } */

    public int getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getBrand() {
        return brand;
    }

    public void setBrand(String brand) {
        this.brand = brand;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public String getPrice() {
        return price;
    }

    public void setPrice(String price) {
        this.price = price;
    }

    public String getPrice_info() {
        return price_info;
    }

    public void setPrice_info(String price_info) {
        this.price_info = price_info;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    @Override
    public int compareTo(Product o) {
        return title.compareTo(o.title);
    }

    public static int[] insert(Product... objs) {
        String query = "INSERT INTO call_center.product ("
                + "title, brand, image_url, price, price_info, category) VALUES(?,?,?,?,?,?)";

        try (PreparedStatement stat = conn.prepareStatement(query, Statement.RETURN_GENERATED_KEYS)) {
            for(Product obj : objs) {
                stat.setString(1, obj.title);
                stat.setString(2, obj.brand);
                stat.setString(3, obj.imageUrl);
                stat.setString(4, obj.price);
                stat.setString(5, obj.price_info);
                stat.setString(6, obj.category);
                
                stat.addBatch();
            }

            int[] exec = stat.executeBatch();
            conn.commit();

            ResultSet generatedKeys = stat.getGeneratedKeys();
            while (generatedKeys.next()) {
                for(Product obj : objs) {
                    obj.id = generatedKeys.getInt(1);
                }
            }

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
        return new int[] {-1};
    }
    public static int[] insert(List<Product> objs) {
        return insert(objs.toArray(new Product[objs.size()]));
    }

    public static boolean isEmpty() {
        String query = "SELECT * FROM call_center.product LIMIT 1";

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

    public static Optional<Product> get(int id) {
        String query = "SELECT * FROM call_center.product WHERE id = ?";

        try (PreparedStatement stat = conn.prepareStatement(query)) {
            stat.setInt(1, id);
            ResultSet rs = stat.executeQuery(query);
            conn.commit();
            if (rs.next())
                return Optional.ofNullable(new Product(rs));

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
            if(conn == null) throw new SQLException("can't connect to database");
            return conn;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static List<Product> getAll() {
        String query = "SELECT * FROM call_center.product ORDER BY title ASC";
        List<Product> list = new ArrayList<>();
        try (PreparedStatement stat = conn.prepareStatement(query)) {
            ResultSet rs = stat.executeQuery(query);
            conn.commit();

            while (rs.next()) {
                list.add(new Product(rs));
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
        //Collections.sort(list);
        return list;
    }

    public static List<Product> filter(Set<String> colNames, Collection<String> values) {
        if(values.size() != colNames.size() || values.size() <= 0 || colNames.size() <= 0) {
            throw new IllegalArgumentException("seach and terms length are bad: should be equal and > 0");
        }

        boolean isFirst = true;
        String query = "SELECT * FROM call_center.product WHERE ";

        for(String col : colNames) {
            if(!isFirst) {
                query += " AND ";
            } else {
                isFirst = false;
            }
            
            query += col + " = ?";
        }
        for(String value : values) {
            if(value.equalsIgnoreCase("ALL")) {
                return getAll();
            }
            query = query.replaceFirst("\\?", "'" + value + "'");
        }
       // query += " ORDER BY title ASC";

        List<Product> list = new ArrayList<>();
        try (PreparedStatement stat = conn.prepareStatement(query)) {     
            ResultSet rs = stat.executeQuery(query);
            conn.commit();

            while (rs.next()) {
                list.add(new Product(rs));
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
        //Collections.sort(list);
        return list;
    }

    public static List<String> getAllTerms(String term) {
        String query = "SELECT DISTINCT " + term +" FROM call_center.product ORDER BY " + term + " ASC";
        List<String> list = new ArrayList<>();
        try (PreparedStatement stat = conn.prepareStatement(query)) {

            ResultSet rs = stat.executeQuery(query);
            conn.commit();

            while (rs.next()) {
                String s = rs.getString(1);
                if(s!= null && !s.isEmpty() && !s.isBlank()) {
                    list.add(s);
                }
            }

        } catch (Exception e) {
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

    public static int[] update(Product... objs) {
        String query = "UPDATE call_center.product SET "
                + "title = COALESCE(?, title), "
                + "brand = COALESCE(?, brand), "
                + "image_url = COALESCE(?, image_url), "
                + "price = COALESCE(?, price), "
                + "price_info = COALESCE(?, price_info), "
                + "category = COALESCE(?, category) "
                + "WHERE title = ?";

        try (PreparedStatement stat = conn.prepareStatement(query)) {
            for(Product obj : objs) {
                stat.setString(1, obj.title);
                stat.setString(2, obj.brand);
                stat.setString(3, obj.imageUrl);
                stat.setString(4, obj.price);
                stat.setString(5, obj.price_info);
                stat.setString(6, obj.category);

                stat.setString(7, obj.title);
                stat.addBatch();
            }

            int[] exec = stat.executeBatch();
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
        return new int[] {-1};

    }
    public static int[] update(List<Product> objs) {
        return update(objs.toArray(new Product[objs.size()]));
    }
    public static int clear() {
        String query1 = "DELETE FROM call_center.product";
        String query2 = "ALTER TABLE call_center.product AUTO_INCREMENT = 1";
        try (
                PreparedStatement stat1 = conn.prepareStatement(query1); 
                PreparedStatement stat2 = conn.prepareStatement(query2); 
            ) {
            
            int exec = stat1.executeUpdate();
            stat2.execute();
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

    public static int delete(int id) {
        String query = "DELETE FROM call_center.product WHERE id = ?";
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
