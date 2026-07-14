package com.digicoffer.lauditor.Webservice

import android.view.View

interface AsyncTaskCompleteListener {
    fun onClick(view: View)
    fun onAsyncTaskComplete(httpResult: HttpResultDo)
}
