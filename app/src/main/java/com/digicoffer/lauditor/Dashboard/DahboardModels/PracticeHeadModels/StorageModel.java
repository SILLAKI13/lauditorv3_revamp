package com.digicoffer.lauditor.Dashboard.DahboardModels.PracticeHeadModels;

public class StorageModel {
    String balanceStorage;
    String currentStorage;
    String totalStorage;

    public StorageModel(String balanceStorage, String currentStorage, String totalStorage) {
        this.balanceStorage = balanceStorage;
        this.currentStorage = currentStorage;
        this.totalStorage = totalStorage;
    }

    public String getBalanceStorage() {
        return balanceStorage;
    }

    public void setBalanceStorage(String balanceStorage) {
        this.balanceStorage = balanceStorage;
    }

    public String getCurrentStorage() {
        return currentStorage;
    }

    public void setCurrentStorage(String currentStorage) {
        this.currentStorage = currentStorage;
    }

    public String getTotalStorage() {
        return totalStorage;
    }

    public void setTotalStorage(String totalStorage) {
        this.totalStorage = totalStorage;
    }
}
