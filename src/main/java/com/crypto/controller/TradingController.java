package com.crypto.controller;

import com.crypto.models.Asset;
import com.crypto.models.Transaction;
import com.crypto.models.User;
import com.crypto.service.PriceUpdateService;
import com.crypto.service.TradingService;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Controller
public class TradingController {
    private final TradingService tradingService;
    private final PriceUpdateService priceUpdateService;

    public TradingController(TradingService tradingService, PriceUpdateService priceUpdateService) {
        this.tradingService = tradingService;
        this.priceUpdateService = priceUpdateService;
    }

    @GetMapping("/")
    public String index(Model model, HttpSession session) {
        initializeUserSession(session);
        Integer userId = (Integer) session.getAttribute("userId");

        Map<String, Double> top20Prices = priceUpdateService.getPrices().entrySet()
                .stream()
                .limit(20)
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        Map.Entry::getValue
                ));

        model.addAttribute("prices", top20Prices); // Use the limited prices
        model.addAttribute("balance", tradingService.getUserBalance(userId));
        model.addAttribute("assets", tradingService.getUserAssets(userId));

        return "index";
    }

    @GetMapping("/buy")
    public String buyPage(Model model, HttpSession session) {
        initializeUserSession(session);
        Integer userId = (Integer) session.getAttribute("userId");

        Map<String, Double> top20Prices = priceUpdateService.getPrices().entrySet()
                .stream()
                .limit(20)
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        Map.Entry::getValue
                ));

        model.addAttribute("prices", top20Prices);
        model.addAttribute("balance", tradingService.getUserBalance(userId));
        return "buy";
    }

    @PostMapping("/buy")
    public String buy(@RequestParam String symbol,
                      @RequestParam Double amount,
                      HttpSession session,
                      RedirectAttributes redirectAttributes) {
        Integer userId = (Integer) session.getAttribute("userId");
        boolean success = tradingService.buyCrypto(userId, symbol, amount);

        if (!success) {
            redirectAttributes.addFlashAttribute("error",
                    "Failed to complete purchase. Check your balance or try again.");
        }

        return "redirect:/";
    }

    @GetMapping("/sell")
    public String sellPage(@RequestParam(required = false) String errorSymbol,
                           Model model,
                           HttpSession session) {
        initializeUserSession(session);
        Integer userId = (Integer) session.getAttribute("userId");

        List<Asset> assets = tradingService.getUserAssets(userId);
        Map<String, Double> currentPrices = priceUpdateService.getPrices();

        Map<String, AssetWithPrice> assetsWithPrices = assets.stream()
                .filter(asset -> currentPrices.containsKey(asset.getSymbol()))
                .collect(Collectors.toMap(
                        Asset::getSymbol,
                        asset -> new AssetWithPrice(asset, currentPrices.get(asset.getSymbol()))
                ));

        model.addAttribute("assets", assetsWithPrices);
        model.addAttribute("balance", tradingService.getUserBalance(userId));
        model.addAttribute("errorSymbol", errorSymbol); // Pass error symbol to view

        return "sell";
    }

    @PostMapping("/sell")
    public String sell(@RequestParam("symbol") String symbol,
                       @RequestParam("amount") String amount,
                       HttpSession session,
                       RedirectAttributes redirectAttributes) {

        Integer userId = (Integer) session.getAttribute("userId");

        try {
            double amountValue = Double.parseDouble(amount);
            if (amountValue <= 0) {
                throw new IllegalArgumentException("Amount must be positive");
            }

            boolean success = tradingService.sellCrypto(userId, symbol, amountValue);

            if (!success) {
                redirectAttributes.addFlashAttribute("error",
                        "Failed to complete sale. Check your holdings or try again.");
            }
        } catch (NumberFormatException e) {
            redirectAttributes.addFlashAttribute("error", "Invalid amount format");
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }

        return "redirect:/";
    }
    @PostMapping("/reset")
    public String reset(HttpSession session) {
        Integer userId = (Integer) session.getAttribute("userId");
        tradingService.resetAccount(userId);
        return "redirect:/";
    }

    @GetMapping("/transactions")
    public String transactions(Model model, HttpSession session) {
        Integer userId = (Integer) session.getAttribute("userId");
        List<Transaction> transactions = tradingService.getTransactionHistory(userId);
        model.addAttribute("transactions", transactions);
        return "transactions";
    }

    private void initializeUserSession(HttpSession session) {
        if (session.getAttribute("userId") == null) {
            User existingUser = tradingService.getUserByName("demo_user");


            if (existingUser == null) {
                User user = new User();
                user.setName("demo_user");
                user.setBalance(10000.0);
                user = tradingService.createUser(user);
                session.setAttribute("userId", user.getId());
            } else {
                session.setAttribute("userId", existingUser.getId());
            }
        }
    }

    private static class AssetWithPrice {
        private final Asset asset;
        private final Double currentPrice;

        public AssetWithPrice(Asset asset, Double currentPrice) {
            this.asset = asset;
            this.currentPrice = currentPrice;
        }

        public Asset getAsset() { return asset; }
        public Double getCurrentPrice() { return currentPrice; }
    }
}

