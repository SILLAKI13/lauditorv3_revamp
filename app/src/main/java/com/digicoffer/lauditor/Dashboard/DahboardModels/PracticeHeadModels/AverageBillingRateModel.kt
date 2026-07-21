package com.digicoffer.lauditor.Dashboard.DahboardModels.PracticeHeadModels

class AverageBillingRateModel(
    var averageBillingRate: String?,
    @get:JvmName("getCurrencySymbol") val currencySymbol: String?,
    @get:JvmName("getCurrencyCode") val currencyCode: String?
)
