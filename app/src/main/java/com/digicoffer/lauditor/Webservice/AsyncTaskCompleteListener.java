package com.digicoffer.lauditor.Webservice;

import android.view.View;

public  interface AsyncTaskCompleteListener {
    void onClick(View view);

    void onAsyncTaskComplete(HttpResultDo httpResult);
}
