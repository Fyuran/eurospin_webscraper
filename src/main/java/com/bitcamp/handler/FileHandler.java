package com.bitcamp.handler;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

public class FileHandler implements HttpHandler {

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

    private static void manageGetMethod(HttpExchange exchange) throws IOException {
        File file = new File("callcenter/" + exchange.getRequestURI().getPath());
        if(!file.exists() || !file.isFile()) {
            exchange.sendResponseHeaders(404, -1);
            return;
        }
        exchange.getResponseHeaders().set("Content-Type", Files.probeContentType(file.toPath()));
     
        OutputStream os = exchange.getResponseBody();
        try {
            exchange.sendResponseHeaders(200, file.length());
            Files.copy(file.toPath(), os);   
        } catch(IOException e) {
            exchange.sendResponseHeaders(401, -1);
        } finally {
            os.close();
        }
    }

    private static void managePostMethod(HttpExchange exchange) throws IOException {
        exchange.sendResponseHeaders(401, -1);
        throw new UnsupportedOperationException("Unimplemented method 'managePostMethod'");
    }
}
