package org.nitb.orchestrator.web.filters

import io.micronaut.http.HttpRequest
import io.micronaut.http.MutableHttpResponse
import io.micronaut.http.annotation.Filter
import io.micronaut.http.filter.HttpServerFilter
import io.micronaut.http.filter.ServerFilterChain
import io.reactivex.Flowable
import io.reactivex.schedulers.Schedulers
import org.nitb.orchestrator.config.ConfigManager
import org.reactivestreams.Publisher

@Filter("/**")
class CorsFilter: HttpServerFilter {
    override fun doFilter(request: HttpRequest<*>?, chain: ServerFilterChain?): Publisher<MutableHttpResponse<*>> {
        return Flowable.fromCallable { true }.subscribeOn(Schedulers.io())
            .switchMap{ chain!!.proceed(request) }
            .doOnNext{ res ->
                    if (corsEnabled) {
                        res.headers.remove("Access-Control-Allow-Credentials")
                        res.headers.remove("Access-Control-Allow-Methods")
                        res.headers.remove("Access-Control-Allow-Origin")
                        res.headers.remove("Access-Control-Allow-Headers")

                        res.headers.add("Access-Control-Allow-Credentials", "true")
                        res.headers.add("Access-Control-Allow-Methods", "*")
                        res.headers.add("Access-Control-Allow-Origin", "*")
                        res.headers.add("Access-Control-Allow-Headers", "*")
                    }
                }
    }

    private val corsEnabled = ConfigManager.getBoolean("cors.enabled", true)
}