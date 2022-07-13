package org.nitb.orchestrator.http

import com.fasterxml.jackson.core.type.TypeReference
import okhttp3.*
import okhttp3.HttpUrl.Companion.toHttpUrl
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.ResponseBody.Companion.toResponseBody
import org.nitb.orchestrator.config.ConfigManager
import org.nitb.orchestrator.config.ConfigNames
import org.nitb.orchestrator.logging.LoggingManager
import org.nitb.orchestrator.serialization.json.JSONSerializer
import java.io.IOException

class HttpClient(
    private val url: String,
    private val params: Map<String, List<String>> = mapOf(),
    private val headers: Map<String, List<String>> = mapOf(),
    private val retries: Int = ConfigManager.getInt(ConfigNames.HTTP_CLIENT_RETRIES, ConfigNames.HTTP_CLIENT_RETRIES_DEFAULT),
    private val timeBetweenRetries: Long = ConfigManager.getLong(ConfigNames.HTTP_CLIENT_TIME_BETWEEN_RETRIES, ConfigNames.HTTP_CLIENT_TIME_BETWEEN_RETRIES_DEFAULT)
) {

    fun <T> jsonRequest(method: String, body: Any, clazz: Class<T>): T {
        val client = createClient()

        val requestBody = JSONSerializer.serialize(body).toRequestBody("application/json".toMediaType())

        val request = createRequestBuilder(method, requestBody)

        return client.newCall(request.build()).execute().use {res ->
            res.body?.use { responseBody ->
                responseBody.string().let {
                    logger.debug("Response received from $url: $it")
                    JSONSerializer.deserialize(it, clazz)
                }
            }
        } ?: error("Response doesn't valid")
    }

    fun <T> jsonRequest(method: String, body: Any, typeReference: TypeReference<T>): T {
        val client = createClient()

        val requestBody = JSONSerializer.serialize(body).toRequestBody("application/json".toMediaType())

        val request = createRequestBuilder(method, requestBody)

        return client.newCall(request.build()).execute().use {res ->
            res.body?.use { responseBody ->
                responseBody.string().let {
                    logger.debug("Response received from $url: $it")
                    JSONSerializer.deserialize(it, typeReference)
                }
            }
        } ?: error("Response doesn't valid")
    }

    fun <T> jsonRequest(method: String, clazz: Class<T>): T {
        val client = createClient()

        val request = createRequestBuilder(method, null)

        return client.newCall(request.build()).execute().use {res ->
            val code = res.code
            res.body?.use { responseBody ->
                responseBody.string().let {
                    logger.debug("Response received from $url: [$code] $it")
                    JSONSerializer.deserialize(it, clazz)
                }
            }
        } ?: error("Response doesn't valid")
    }

    fun <T> jsonRequest(method: String, typeReference: TypeReference<T>): T {
        val client = createClient()

        val request = createRequestBuilder(method, null)

        return client.newCall(request.build()).execute().use {res ->
            res.body?.use { responseBody ->
                responseBody.string().let {
                    logger.debug("Response received from $url: $it")
                    JSONSerializer.deserialize(it, typeReference) }
            }
        } ?: error("Response doesn't valid")
    }

    fun basicRequest(method: String, body: Any): Response {
        val client = createClient()

        val requestBody = JSONSerializer.serialize(body).toRequestBody("application/json".toMediaType())

        val request = createRequestBuilder(method, requestBody)

        return client.newCall(request.build()).execute()
    }

    fun basicRequest(method: String): Response {
        val client = createClient()

        val request = createRequestBuilder(method, null)

        return client.newCall(request.build()).execute()
    }

    private val logger = LoggingManager.getLogger(HttpClient::class.java)

    private fun createRequestBuilder(method: String, body: RequestBody? = null): Request.Builder {
        val requestUrl = url.toHttpUrl().newBuilder()

        for ((name, values) in params) {
            for (value in values) {
                requestUrl.addQueryParameter(name, value)
            }
        }

        val request = Request.Builder().url(requestUrl.build())

        for ((name, values) in headers) {
            for (value in values) {
                request.addHeader(name, value)
            }
        }

        request.method(method, body)

        return request
    }

    private fun createClient(): OkHttpClient {
        val clientBuilder = OkHttpClient.Builder()

        clientBuilder.addInterceptor(Interceptor { chain ->
            val req = chain.request()

            var res: Response = if (retries == 0) chain.proceed(req) else Response.Builder().code(0).request(req).protocol(Protocol.HTTP_2).message("")
                .body( "".toResponseBody("plain/text".toMediaType())).build()

            var tryCount = 0
            while (tryCount < retries) {
                try {
                    res = chain.proceed(req)
                    tryCount = retries
                } catch (e: IOException) {
                    Thread.sleep(timeBetweenRetries)
                    tryCount++
                }
            }

            res
        })

        return clientBuilder.build()
    }

}