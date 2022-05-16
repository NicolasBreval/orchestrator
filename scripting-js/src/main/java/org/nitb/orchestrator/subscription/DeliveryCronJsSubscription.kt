package org.nitb.orchestrator.subscription

import com.cronutils.model.CronType
import org.nitb.orchestrator.scripting.js.JsScriptManager
import org.nitb.orchestrator.subscription.delivery.DeliveryCronSubscription
import java.io.Serializable

class DeliveryCronJsSubscription(
    name: String,
    cronExpression: String,
    receivers: List<SubscriptionReceiver> = listOf(),
    cronType: CronType = CronType.UNIX,
    timeout: Long = -1,
    description: String? = null,
    private val resultClass: String,
    private val script: String,
    private val args: Array<Any?>
): DeliveryCronSubscription<Serializable>(name, cronExpression, receivers, cronType, timeout, description) {

    override fun onEvent(sender: String, input: Unit): Serializable {
        return JsScriptManager.runScriptWithResult(Class.forName(resultClass), script, args)
    }
}