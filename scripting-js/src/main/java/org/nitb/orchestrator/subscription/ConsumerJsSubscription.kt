package org.nitb.orchestrator.subscription

import org.nitb.orchestrator.scripting.js.JsScriptManager
import org.nitb.orchestrator.subscription.consumer.ConsumerSubscription
import java.io.Serializable

class ConsumerJsSubscription(
    name: String,
    timeout: Long = -1,
    description: String? = null,
    private val script: String,
    private val args: Array<Any?>
): ConsumerSubscription<Serializable>(name, timeout, description) {

    override fun onEvent(sender: String, input: Serializable) {
        JsScriptManager.runScriptAddingInput(script, input, args)
    }
}