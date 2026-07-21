package com.digicoffer.lauditor.Dashboard.NewRevampViewModels

import com.digicoffer.lauditor.Notifications.Models.Navigation

interface DashboardNavigationListener {
    fun onNavigate(cardType: Int, navigation: Navigation?)
    fun onNavigate(cardType: Int, navigation: Navigation?, subType: String?)
    fun onPaySubscription(planLabel: String?, validityText: String?)
}
