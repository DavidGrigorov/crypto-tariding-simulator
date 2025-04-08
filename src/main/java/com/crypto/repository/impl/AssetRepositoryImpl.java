package com.crypto.repository.impl;

import com.crypto.models.Asset;
import com.crypto.repository.AssetRepository;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

@Repository
public class AssetRepositoryImpl implements AssetRepository {

    private final JdbcTemplate jdbc;

    public AssetRepositoryImpl(JdbcTemplate jdbcTemplate) {
        this.jdbc = jdbcTemplate;
    }


    @Override
    public Asset findById(int id) {
        String sql = "SELECT * FROM crypto_assets WHERE id = ?";
        return jdbc.queryForObject(sql, new AssetRowMapper(), id);
    }

    @Override
    public Asset findUserAndSymbol(int userId, String symbol) {
        try {
            String sql = "SELECT * FROM crypto_assets WHERE user_id = ? AND symbol = ?";
            return jdbc.queryForObject(sql, new AssetRowMapper(), userId, symbol);
        } catch (Exception e) {
            return null;
        }
    }

    @Override
    public List<Asset> findByUser(int userId) {
        String sql = "SELECT * FROM crypto_assets WHERE user_id = ?";
        return jdbc.query(sql, new AssetRowMapper(), userId);
    }

    @Override
    public Asset save(Asset asset) {
        String sql = "INSERT INTO crypto_assets(user_id, symbol, amount) VALUES(?, ?, ?) RETURNING id";
        int id = jdbc.queryForObject(sql, int.class,
                asset.getUserId(), asset.getSymbol(), asset.getAmount());
        asset.setId(id);
        return asset;
    }

    @Override
    public void updateAmount(int assetId, double newAmount) {

        String sql = "UPDATE crypto_assets SET amount = ? WHERE id = ?";
        jdbc.update(sql, newAmount, assetId);
    }

    @Override
    public void delete(int assetId) {
        String sql = "DELETE FROM crypto_assets WHERE id = ?";
        jdbc.update(sql, assetId);
    }

    @Override
    public void deleteAll(int userId) {
        String sql = "DELETE FROM crypto_assets WHERE user_id = ?";
        jdbc.update(sql, userId);
    }

    private static class AssetRowMapper implements RowMapper<Asset> {
        @Override
        public Asset mapRow(ResultSet rs, int rowNum) throws SQLException {
            Asset asset = new Asset();
            asset.setId(rs.getInt("id"));
            asset.setUserId(rs.getInt("user_id"));
            asset.setSymbol(rs.getString("symbol"));
            asset.setAmount(rs.getDouble("amount"));
            return asset;
        }
    }
}
