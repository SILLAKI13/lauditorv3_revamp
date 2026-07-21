package com.digicoffer.lauditor.Dashboard.DahboardModels.PracticeHeadModels

class ApproxRevenueModel(
    var approxRevenue: String?,
    @get:JvmName("getCurrencySymbol") val currencySymbol: String?,
    @get:JvmName("getCurrencyCode") val currencyCode: String?
)
