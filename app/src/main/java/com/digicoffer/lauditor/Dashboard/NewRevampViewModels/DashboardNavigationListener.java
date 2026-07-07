package com.digicoffer.lauditor.Dashboard.NewRevampViewModels;

import com.digicoffer.lauditor.Notifications.Models.Navigation;

public interface DashboardNavigationListener {
    void onNavigate(int cardType, Navigation navigation);
    void onNavigate(int cardType, Navigation navigation, String subType);
    void onPaySubscription(String planLabel, String validityText);
}