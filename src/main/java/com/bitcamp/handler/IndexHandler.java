package com.bitcamp.handler;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URLEncoder;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import com.bitcamp.callcenter.Main;
import com.bitcamp.callcenter.Product;
import com.bitcamp.callcenter.Userlog;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
public class IndexHandler implements HttpHandler {

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        String method = exchange.getRequestMethod();

        switch (method.toLowerCase()) {
        case "get":
            manageGetMethod(exchange);
            break;
        case "post":
            managePostMethod(exchange);
            break;
        }
    }

    private void managePostMethod(HttpExchange exchange) throws IOException {
        var data = Main.parsePostData(exchange);     
        Userlog.insert(data);
        StringBuilder newLocation = new StringBuilder("/sales");
        //sales?category=ACQUA%2C+BIBITE+E+SUCCHI
        if(data.containsKey("category")) {
            newLocation.append("?" + "category=" + URLEncoder.encode(data.get("category"), "utf-8"));
        } else if(data.containsKey("brand")) {
            newLocation.append("?" + "brand=" + URLEncoder.encode(data.get("brand"), "utf-8"));
        }
        exchange.getResponseHeaders().set("Location", newLocation.toString());
		exchange.sendResponseHeaders(302, -1);
    }

    private void manageGetMethod(HttpExchange exchange) throws IOException {
        exchange.getResponseHeaders().set("Content-Type", "text/html");

        File file = new File("callcenter/index.html");
        if(!file.exists() || !file.isFile()) {
            exchange.sendResponseHeaders(404, -1);
            return;
        }

        
        Document index = Jsoup.parse(file);
        Element categorySelect = index.getElementById("category");
        Element brandSelect = index.getElementById("brand");
        categorySelect.appendChildren(Product.parseAllTerms("category"));
        brandSelect.appendChildren(Product.parseAllTerms("brand"));
        //<option value="all">text</option>

        byte[] response = index.html().getBytes("UTF-8");
        exchange.sendResponseHeaders(200, response.length);

        OutputStream os = exchange.getResponseBody();
        BufferedOutputStream bos = new BufferedOutputStream(os, response.length);
        try {
            bos.write(response);
        } catch(IOException e) {
            e.printStackTrace();
            exchange.sendResponseHeaders(401, -1);
        } finally {
            os.close();
        }
        
    }

}
