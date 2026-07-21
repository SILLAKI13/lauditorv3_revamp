package com.digicoffer.lauditor.Dashboard.DahboardModels.MydayModels

class MeetingModel(
    var date: String?,
    var from_ts: String?,
    var to_ts: String?,
    var subject: String?,
    var count: String?
) {
    var time: String? = null

    fun setCount(count: Int) {
        this.count = count.toString()
    }
}
