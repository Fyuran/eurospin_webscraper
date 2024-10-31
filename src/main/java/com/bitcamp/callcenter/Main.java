package com.bitcamp.callcenter;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetSocketAddress;
import java.net.URLDecoder;
import java.nio.charset.Charset;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

import com.bitcamp.handler.FileHandler;
import com.bitcamp.handler.IndexHandler;
import com.bitcamp.handler.SalesHandler;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;

public class Main {
    private static Connection mysqlConnection = null;

    public static void main(String[] args) {
        try {
            getMySQLConnection();
            Product.clear();
            Parser.getEurospinProducts("https://www.eurospin.it/promozioni/");
            HttpServer server = HttpServer.create(new InetSocketAddress(7000), 0);
            server.createContext("/", new IndexHandler());
            server.createContext("/styles.css", new FileHandler());
            server.createContext("/sales.js", new FileHandler());
            server.createContext("/sales", new SalesHandler());
    
            server.start();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static Connection getMySQLConnection() throws SQLException {
        if(mysqlConnection == null) {
            mysqlConnection = DriverManager.getConnection("jdbc:mysql://localhost:1806", "root", "bitcampPassword");
            mysqlConnection.setAutoCommit(false);

            return mysqlConnection;
        }

        return mysqlConnection;
    }

    public static Map<String, String> parsePostData(HttpExchange exchange) {
        HashMap<String, String> data = new HashMap<>();
        
        try(BufferedReader br = new BufferedReader(new InputStreamReader(exchange.getRequestBody(), "utf-8"))) {
            String toDecode = br.readLine();
            if(toDecode == null) {
                return data;
            }
            String[] postData = URLDecoder.decode(toDecode, Charset.forName("UTF-8")).split("&");
            
            for(String pair : postData) {
                String[] kv = pair.split("=");
                data.put(kv[0], kv[1]);
            }
        } catch (IOException e) {
            e.printStackTrace();
            return data;
        }

        return data;
    }

    public static Map<String, String> parseGetData(HttpExchange exchange) {
        HashMap<String, String> data = new HashMap<>();
        
        String toDecode = exchange.getRequestURI().getQuery();
        if(toDecode == null) {
            return data;
        }
        String[] getData = URLDecoder.decode(toDecode, Charset.forName("UTF-8")).split("&");
        
        for(String pair : getData) {
            String[] kv = pair.split("=");
            data.put(kv[0], kv[1]);
        }

        return data;
    }
}
