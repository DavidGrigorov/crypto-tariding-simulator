package com.crypto.service;

import jakarta.annotation.PostConstruct;
import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class PriceUpdateService {
    private Map<String, Double> prices = new ConcurrentHashMap<>();
    private WebSocketClient client;

    @PostConstruct
    public void init() {
        try {
            client = new WebSocketClient(new URI("wss://ws.kraken.com/v2")) {
                @Override
                public void onOpen(ServerHandshake handshakedata) {
                    System.out.println("Connected to Kraken WebSocket");
                    subscribeToTickers();
                }

                @Override
                public void onMessage(String message) {
                    processMessage(message);
                }

                @Override
                public void onClose(int code, String reason, boolean remote) {
                    System.out.println("Disconnected - attempting reconnect in 5 seconds");
                    scheduleReconnect();
                }

                @Override
                public void onError(Exception ex) {
                    System.err.println("WebSocket error: " + ex.getMessage());
                }
            };
            client.connect();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void scheduleReconnect() {
        new Thread(() -> {
            try {
                Thread.sleep(5000);
                System.out.println("Attempting reconnect...");
                client.reconnect();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }

    private void subscribeToTickers() {
        String[] symbols = {
                "BTC/USD", "ETH/USD", "MATIC/USD", "SOL/USD", "ADA/USD",
                "DOT/USD", "AVAX/USD", "LINK/USD", "XRP/USD", "DOGE/USD"
        };

        JSONObject subscribeMessage = new JSONObject();
        subscribeMessage.put("method", "subscribe");

        JSONObject params = new JSONObject();
        params.put("channel", "ticker");

        JSONArray symbolsArray = new JSONArray();
        for (String symbol : symbols) {
            symbolsArray.put(symbol);
        }
        params.put("symbol", symbolsArray);

        subscribeMessage.put("params", params);

        System.out.println("Subscribing to: " + subscribeMessage);
        client.send(subscribeMessage.toString());
    }

    private void processMessage(String message) {
        try {
            JSONObject json = new JSONObject(message);

            if (json.has("channel") && json.getString("channel").equals("ticker")
                    && json.has("data")) {
                JSONArray dataArray = json.getJSONArray("data");
                for (int i = 0; i < dataArray.length(); i++) {
                    JSONObject data = dataArray.getJSONObject(i);
                    if (data.has("symbol") && data.has("last")) {
                        String fullSymbol = data.getString("symbol");
                        String baseSymbol = fullSymbol.replace("/USD", "");
                        double price = data.getDouble("last");

                        prices.put(fullSymbol, price);
                        prices.put(baseSymbol, price);

                        System.out.println("Updated prices for: " + fullSymbol +
                                " and " + baseSymbol + " = " + price);
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("Error processing message: " + e.getMessage());
        }
    }

    public Double getPrice(String symbol) {
        // Try exact match first
        Double price = prices.get(symbol);
        if (price == null) {
            // Try uppercase version
            price = prices.get(symbol.toUpperCase());
        }
        System.out.println("Price lookup for " + symbol + ": " + price);
        return price;
    }

    public Map<String, Double> getPrices() {
        return new ConcurrentHashMap<>(prices);
    }
}
