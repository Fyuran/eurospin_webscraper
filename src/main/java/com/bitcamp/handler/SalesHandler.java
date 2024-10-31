package com.bitcamp.handler;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.util.List;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import com.bitcamp.callcenter.Main;
import com.bitcamp.callcenter.Product;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

public class SalesHandler implements HttpHandler {

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        String method = exchange.getRequestMethod();

        switch (method.toLowerCase()) {
        case "get":
            manageGetMethod(exchange);
            break;
        default:
            managePostMethod(exchange);
            break;
        }
    }

    private static void managePostMethod(HttpExchange exchange) throws IOException {
        exchange.sendResponseHeaders(401, -1);
        throw new UnsupportedOperationException("Unimplemented method 'managePostMethod'");
    }

    private void manageGetMethod(HttpExchange exchange) throws IOException {
        exchange.getResponseHeaders().set("Content-Type", "text/html");

        File file = new File("callcenter/sales.html");
        if(!file.exists() || !file.isFile()) {
            exchange.sendResponseHeaders(404, -1);
            return;
        }
        
        Document sales = Jsoup.parse(file);
        Element product_panel = sales.selectFirst(".product-panel-grid");

        Element categorySelect = sales.getElementById("category");
        Element brandSelect = sales.getElementById("brand");
        categorySelect.appendChildren(Product.parseAllTerms("category"));
        brandSelect.appendChildren(Product.parseAllTerms("brand"));

        var data = Main.parseGetData(exchange);
        List<Product> products;
        if(!data.isEmpty()) {
            products = Product.filter(data.keySet(), data.values());
            for(var entry : data.entrySet()) {
                sales.getElementById(entry.getKey())
                .getElementsByAttributeValue("value", entry.getValue())
                .attr("selected", "selected");
            }
        } else {
            products = Product.getAll();
        }
        
        Elements productsHtml = products.parallelStream().map(p -> p.toHtml()).collect(Elements::new, (x,y)->x.add(y), (a,b)->a.addAll(b));
        product_panel.appendChildren(productsHtml);

        


        byte[] response = sales.html().getBytes("UTF-8");
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
