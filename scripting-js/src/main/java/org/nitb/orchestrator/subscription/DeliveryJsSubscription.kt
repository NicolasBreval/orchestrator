package org.nitb.orchestrator.subscription

import org.nitb.orchestrator.scripting.js.JsScriptManager
import org.nitb.orchestrator.subscription.delivery.DeliverySubscription
import java.io.Serializable

class DeliveryJsSubscription(
    name: String,
    receivers: List<SubscriptionReceiver> = listOf(),
    timeout: Long = -1,
    description: String? = null,
    private val resultClass: String,
    private val script: String,
    private val args: Array<Any?>
): DeliverySubscription<Serializable, Serializable>(name, receivers, timeout, description) {

    override fun onEvent(sender: String, input: Serializable): Serializable {
        return JsScriptManager.runScriptWithResultAddingInput(Class.forName(resultClass), script, args)
    }
}