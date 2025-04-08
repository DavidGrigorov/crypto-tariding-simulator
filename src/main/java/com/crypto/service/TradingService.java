package com.crypto.service;

import com.crypto.models.Asset;
import com.crypto.models.Transaction;
import com.crypto.models.User;
import com.crypto.repository.AssetRepository;
import com.crypto.repository.TransactionRepository;
import com.crypto.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.List;

@Service
public class TradingService {
    private final UserRepository userRepository;
    private final AssetRepository assetRepository;
    private final TransactionRepository transactionRepository;
    private final PriceUpdateService priceUpdateService;

    public TradingService(UserRepository userRepository,
                          AssetRepository assetRepository,
                          TransactionRepository transactionRepository,
                          PriceUpdateService priceUpdateService) {
        this.userRepository = userRepository;
        this.assetRepository = assetRepository;
        this.transactionRepository = transactionRepository;
        this.priceUpdateService = priceUpdateService;
    }
    @Transactional
    public boolean buyCrypto(Integer userId, String symbol, Double amount) {
        try {
            String normalizedSymbol = normalizeSymbol(symbol);


            Double price = getPriceWithFallback(normalizedSymbol);
            if (price == null) {
                System.err.println("[BUY] Price not available for: " + normalizedSymbol);
                return false;
            }

            Double totalCost = price * amount;
            System.out.println("[BUY] Attempting to buy " + amount + " " + normalizedSymbol +
                    " at $" + price + " (Total: $" + totalCost + ")");

            User user = userRepository.findById(userId);
            if (user == null) {
                System.err.println("[BUY] User not found: " + userId);
                return false;
            }

            if (user.getBalance() < totalCost) {
                System.err.println("[BUY] Insufficient funds. Balance: $" +
                        user.getBalance() + " < Needed: $" + totalCost);
                return false;
            }


            double newBalance = user.getBalance() - totalCost;
            userRepository.updateBalance(userId, newBalance);
            System.out.println("[BUY] Updated balance from $" +
                    user.getBalance() + " to $" + newBalance);


            Asset asset = assetRepository.findUserAndSymbol(userId, normalizedSymbol);
            if (asset == null) {
                asset = new Asset();
                asset.setUserId(userId);
                asset.setSymbol(normalizedSymbol);
                asset.setAmount(amount);
                assetRepository.save(asset);
                System.out.println("[BUY] Created new asset: " + normalizedSymbol +
                        " Amount: " + amount);
            } else {
                double newAmount = asset.getAmount() + amount;
                assetRepository.updateAmount(asset.getId(), newAmount);
                System.out.println("[BUY] Updated asset: " + normalizedSymbol +
                        " from " + asset.getAmount() + " to " + newAmount);
            }


            Transaction transaction = createTransaction(userId, normalizedSymbol,
                    "BUY", amount, price, totalCost);
            transactionRepository.save(transaction);
            System.out.println("[BUY] Recorded transaction: " + transaction);

            return true;
        } catch (Exception e) {
            System.err.println("[BUY ERROR] " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    @Transactional
    public boolean sellCrypto(int userId, String symbol, double amount) {
        try {
            String normalizedSymbol = normalizeSymbol(symbol);
            Asset asset = assetRepository.findUserAndSymbol(userId, normalizedSymbol);

            if (asset == null) {
                System.err.println("[SELL] No asset found: " + normalizedSymbol);
                return false;
            }

            if (asset.getAmount() < amount) {
                System.err.println("[SELL] Insufficient holdings. Available: " +
                        asset.getAmount() + " < Requested: " + amount);
                return false;
            }

            Double price = getPriceWithFallback(normalizedSymbol);
            if (price == null) {
                System.err.println("[SELL] Price not available for: " + normalizedSymbol);
                return false;
            }

            double totalValue = price * amount;
            System.out.println("[SELL] Attempting to sell " + amount + " " + normalizedSymbol +
                    " at $" + price + " (Total: $" + totalValue + ")");

            User user = userRepository.findById(userId);
            double newBalance = user.getBalance() + totalValue;
            userRepository.updateBalance(userId, newBalance);
            System.out.println("[SELL] Updated balance from $" +
                    user.getBalance() + " to $" + newBalance);

            double newAmount = asset.getAmount() - amount;
            if (newAmount <= 0.00000001) {
                assetRepository.delete(asset.getId());
                System.out.println("[SELL] Fully sold and deleted asset: " + normalizedSymbol);
            } else {
                assetRepository.updateAmount(asset.getId(), newAmount);
                System.out.println("[SELL] Updated asset: " + normalizedSymbol +
                        " from " + asset.getAmount() + " to " + newAmount);
            }

            Transaction transaction = createTransaction(userId, normalizedSymbol,
                    "SELL", amount, price, totalValue);
            transactionRepository.save(transaction);
            System.out.println("[SELL] Recorded transaction: " + transaction);

            return true;
        } catch (Exception e) {
            System.err.println("[SELL ERROR] " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }


    private String normalizeSymbol(String symbol) {
        return symbol.replace("/USD", "").toUpperCase();
    }

    private Double getPriceWithFallback(String symbol) {
        Double price = priceUpdateService.getPrice(symbol);
        if (price == null) {
            price = priceUpdateService.getPrice(symbol + "/USD");
        }
        return price;
    }

    private Transaction createTransaction(int userId, String symbol, String type,
                                          double amount, double price, double totalValue) {
        Transaction transaction = new Transaction();
        transaction.setUserId(userId);
        transaction.setSymbol(symbol);
        transaction.setTransactionType(type);
        transaction.setAmount(amount);
        transaction.setPrice(price);
        transaction.setTotalValue(totalValue);
        transaction.setTimestamp(Timestamp.from(Instant.now()));
        return transaction;
    }

    @Transactional
    public void resetAccount(int userId) {
        try {
            System.out.println("[RESET] Resetting account for user: " + userId);

            assetRepository.deleteAll(userId);
            System.out.println("[RESET] Deleted all assets");

            userRepository.updateBalance(userId, 10000.0);
            System.out.println("[RESET] Reset balance to $10,000");

        } catch (Exception e) {
            System.err.println("[RESET ERROR] " + e.getMessage());
            throw e;
        }
    }

    public List<Transaction> getTransactionHistory(int userId) {
        return transactionRepository.findByUser(userId);
    }

    public List<Asset> getUserAssets(int userId) {
        return assetRepository.findByUser(userId);
    }

    public User getUserByName(String name) {
        return userRepository.findByUsername(name);
    }

    @Transactional
    public User createUser(User user) {
        return userRepository.save(user);
    }

    public double getUserBalance(int userId) {
        User user = userRepository.findById(userId);
        if (user == null) {
            throw new IllegalArgumentException("User not found with ID: " + userId);
        }
        return user.getBalance();
    }
}
