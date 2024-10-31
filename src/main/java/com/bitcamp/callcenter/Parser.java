package com.bitcamp.callcenter;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

public class Parser {
    private Document html;

    public Parser(URL url) {
        try {
            this.html = Jsoup.parse(url, 60);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void updateEurospinProducts(Elements anchors) {
        Element allFilter = anchors.removeFirst(); //remove *ALL filter -> Tutte le Categorie, Tutti i marchi
        anchors.parallelStream().forEach(anchor -> {
            Document filteredPage;
            try {
                filteredPage = Jsoup.connect(anchor.attr("href")).get();
            } catch (IOException e) {
                e.printStackTrace();
                return;
            }
            Elements grid_items = filteredPage.select(".sn_promo_grid_item_ct");
            if(!grid_items.isEmpty()) {
                List<Product> products = new ArrayList<>();
                grid_items.forEach(grid_item -> {
                    Product product;
                    if(allFilter.text().equalsIgnoreCase("Tutte le Categorie")) {
                        product = new Product(grid_item, anchor.text());
                    } else {
                        product = new Product(grid_item, null);
                    }
                    products.add(product);
                }); 
                Product.update(products);
            }
        });
    }

    public static List<Product> getEurospinProducts(String url) {
        Document html;
        try {
            html = Jsoup.connect(url).get();
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }

        //insert all elements
        Elements grid_items = html.select(".sn_promo_grid_item_ct");
        if(!grid_items.isEmpty()) {
            List<Product> products = Collections.synchronizedList(new ArrayList<>());
            grid_items.parallelStream().forEach(grid_item -> {
                Product product;
                product = new Product(grid_item, null);
                products.add(product);
            }); 
            Product.insert(products);
        }

        //no category attribute, so parse each filter and update their values to db
        Elements filters = html.select(".sn_filters .dropdown-menu");
        if(!filters.isEmpty()) {
            filters.forEach(filter -> {
                Elements a = filter.getElementsByTag("a");
                updateEurospinProducts(a);
            });
        }
        return Product.getAll();
    }

    public Document getHtml() {
        return html;
    }
}
