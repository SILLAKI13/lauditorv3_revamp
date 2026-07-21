package com.digicoffer.lauditor.TimeSheets.Models

class Month_Model {
    var billable_week_1: String? = null
    var billable_week_2: String? = null
    var billable_week_3: String? = null
    var billable_week_4: String? = null
    var billable_week_5: String? = null
    var billable_week_tot: String? = null
    var non_billable_week_1: String? = null
    var non_billable_week_2: String? = null
    var non_billable_week_3: String? = null
    var non_billable_week_4: String? = null
    var non_billable_week_5: String? = null
    var non_billable_week_tot: String? = null
    var name: String? = null
    var id: String? = null
    
    @get:JvmName("getTotal")
    @set:JvmName("setTotal")
    var Total: String? = null
}
