package org.nitb.orchestrator.subscription

import org.nitb.orchestrator.scripting.js.JsScriptManager
import org.nitb.orchestrator.subscription.consumer.ConsumerMultiInputSubscription
import org.nitb.orchestrator.subscription.delivery.SerializableMap
import java.io.Serializable

class ConsumerMultiInputJsSubscription(
    name: String,
    senders: List<String>,
    limit: Int = Int.MAX_VALUE,
    timeout: Long = -1,
    description: String? = null,
    private val script: String,
    private val args: Array<Any?>
): ConsumerMultiInputSubscription(name, senders, limit, timeout, description) {

    override fun onEvent(sender: String, input: SerializableMap<String, Serializable>) {
        JsScriptManager.runScriptAddingInput(script, args)
    }
}