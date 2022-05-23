package org.nitb.orchestrator.http

import okhttp3.*
import okhttp3.HttpUrl.Companion.toHttpUrl
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import org.nitb.orchestrator.serialization.json.JSONSerializer

class HttpClient(
    private val url: String,
    private val params: Map<String, List<String>> = mapOf(),
    private val headers: Map<String, List<String>> = mapOf()
) {

    fun <T> jsonRequest(method: String, body: Any, clazz: Class<T>): T {
        val client = OkHttpClient()

        val requestBody = JSONSerializer.serialize(body).toRequestBody("application/json".toMediaType())

        val request = createRequestBuilder(method, requestBody)

        return client.newCall(request.build()).execute().body?.string()?.let { JSONSerializer.deserialize(it, clazz) } ?: error("Response doesn't valid")
    }

    fun <T> jsonRequest(method: String, clazz: Class<T>): T {
        val client = OkHttpClient()

        val request = createRequestBuilder(method, null)

        return client.newCall(request.build()).execute().body?.string()?.let { JSONSerializer.deserialize(it, clazz) } ?: error("Response doesn't valid")
    }

    fun jsonRequest(method: String, body: Any): Response {
        val client = OkHttpClient()

        val requestBody = JSONSerializer.serialize(body).toRequestBody("application/json".toMediaType())

        val request = createRequestBuilder(method, requestBody)

        return client.newCall(request.build()).execute()
    }

    fun jsonRequest(method: String): Response {
        val client = OkHttpClient()

        val request = createRequestBuilder(method, null)

        return client.newCall(request.build()).execute()
    }

    fun multipartFormRequest(method: String, valueParts: Map<String, String> = mapOf(), fileParts: List<Triple<String, String, RequestBody>> = listOf()): Response {
        val client = OkHttpClient()

        val requestBody = MultipartBody.Builder().setType(MultipartBody.FORM)

        for ((name, value) in valueParts) {
            requestBody.addFormDataPart(name, value)
        }

        for ((name, filename, value) in fileParts) {
            requestBody.addFormDataPart(name, filename, value)
        }

        val request = createRequestBuilder(method, requestBody.build())

        return client.newCall(request.build()).execute()
    }

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
}