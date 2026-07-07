package com.example.autobookkeep.database;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

@Dao
public interface AssetAccountDao {
    @Insert
    void insert(AssetAccount account);

    @Update
    void update(AssetAccount account);

    @Delete
    void delete(AssetAccount account);

    @Query("SELECT * FROM asset_accounts ORDER BY type, name")
    LiveData<List<AssetAccount>> getAll();

    @Query("SELECT * FROM asset_accounts ORDER BY type, name")
    List<AssetAccount> getAllSync();

    @Query("SELECT * FROM asset_accounts WHERE type = :type")
    List<AssetAccount> getAssetsByTypeSync(int type);

    @Query("SELECT * FROM asset_accounts WHERE name = :name AND type = :type LIMIT 1")
    AssetAccount getAssetByNameAndType(String name, int type);

    @Query("SELECT * FROM asset_accounts WHERE id = :id LIMIT 1")
    AssetAccount getAssetByIdSync(int id);

    @Query("UPDATE asset_accounts SET amount = amount - :deductAmount WHERE id = :id AND amount >= :deductAmount")
    int decreaseBalanceSafe(int id, float deductAmount);
}