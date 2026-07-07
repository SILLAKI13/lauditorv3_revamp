package com.digicoffer.lauditor.Webservice.core

import okhttp3.Interceptor

object NetworkConfig {
    var baseUrl: () -> String = { "http://10.0.2.2:8011/consumer/" }
    var tokenProvider: () -> String? = { null }
    var cofferIdProvider: () -> String? = { null }
    var clientTypeProvider: () -> String? = { "mobile" }
    var connectTimeoutSeconds: Long = 30
    var readTimeoutSeconds: Long = 30
    var writeTimeoutSeconds: Long = 30
    var isLoggingEnabled: Boolean = true
    
    val customInterceptors = mutableListOf<Interceptor>()
}
