package com.digicoffer.lauditor.Webservice.core

import okhttp3.RequestBody
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.HTTP
import retrofit2.http.HeaderMap
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Url

interface ApiService {
    @GET
    fun executeGet(
        @Url url: String,
        @HeaderMap headers: Map<String, String>
    ): Call<ResponseBody>

    @POST
    fun executePost(
        @Url url: String,
        @HeaderMap headers: Map<String, String>,
        @Body body: RequestBody?
    ): Call<ResponseBody>

    @PUT
    fun executePut(
        @Url url: String,
        @HeaderMap headers: Map<String, String>,
        @Body body: RequestBody?
    ): Call<ResponseBody>

    @DELETE
    fun executeDelete(
        @Url url: String,
        @HeaderMap headers: Map<String, String>
    ): Call<ResponseBody>

    @HTTP(method = "DELETE", hasBody = true)
    fun executeDeleteWithBody(
        @Url url: String,
        @HeaderMap headers: Map<String, String>,
        @Body body: RequestBody?
    ): Call<ResponseBody>

    @PATCH
    fun executePatch(
        @Url url: String,
        @HeaderMap headers: Map<String, String>,
        @Body body: RequestBody?
    ): Call<ResponseBody>
}
