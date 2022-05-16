package org.nitb.orchestrator.subscription

import org.nitb.orchestrator.scripting.js.JsScriptManager
import org.nitb.orchestrator.subscription.delivery.DeliveryMultiInputSubscription
import org.nitb.orchestrator.subscription.delivery.SerializableMap
import java.io.Serializable

class DeliveryMultiInputJsSubscription(
    name: String,
    receivers: List<SubscriptionReceiver>,
    senders: List<String>,
    limit: Int = Int.MAX_VALUE,
    timeout: Long = -1,
    description: String? = null,
    private val resultClass: String,
    private val script: String,
    private val args: Array<Any?>
): DeliveryMultiInputSubscription<Serializable>(name, receivers, senders, limit, timeout, description) {

    override fun onEvent(sender: String, input: SerializableMap<String, Serializable>): Serializable {
        return JsScriptManager.runScriptWithResultAddingInput(Class.forName(resultClass), script, args)
    }
}