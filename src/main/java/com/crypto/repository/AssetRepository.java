package com.crypto.repository;

import com.crypto.models.Asset;

import java.util.List;

public interface AssetRepository {
    Asset findById(int id);
    Asset findUserAndSymbol(int userId, String symbol);
    List<Asset> findByUser(int userId);
    Asset save(Asset asset);
    void updateAmount(int assetId, double newAmount);
    void delete(int assetId);
    void deleteAll(int userId);
}
