package org.nitb.orchestrator.subscription

import org.nitb.orchestrator.scripting.js.JsScriptManager
import org.nitb.orchestrator.subscription.delivery.DeliveryPeriodicalSubscription
import java.io.Serializable

class DeliveryPeriodicalJsSubscription(
    name: String,
    delay: Long,
    initialDelay: Long = 0,
    receivers: List<SubscriptionReceiver> = listOf(),
    timeout: Long = -1,
    description: String? = null,
    private val resultClass: String,
    private val script: String,
    private val args: Array<Any?>
): DeliveryPeriodicalSubscription<Serializable>(name, delay, initialDelay, receivers, timeout, description) {

    override fun onEvent(sender: String, input: Unit): Serializable {
        return JsScriptManager.runScriptWithResult(Class.forName(resultClass), script, input, args)
    }
}