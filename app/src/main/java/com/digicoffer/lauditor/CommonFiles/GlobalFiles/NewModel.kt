package com.digicoffer.lauditor.CommonFiles.GlobalFiles

import android.view.View
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.digicoffer.lauditor.Webservice.AsyncTaskCompleteListener
import com.digicoffer.lauditor.Webservice.HttpResultDo

class NewModel : ViewModel(), AsyncTaskCompleteListener {
    private val selectItem = MutableLiveData<String>()

    fun setData(item: String) {
        selectItem.value = item
    }

    fun getselectedItem(): LiveData<String> {
        return selectItem
    }

    override fun onClick(view: View) {}

    override fun onAsyncTaskComplete(httpResult: HttpResultDo) {}
}
