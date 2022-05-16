package org.nitb.orchestrator.subscription

import com.cronutils.model.CronType
import org.nitb.orchestrator.scripting.js.JsScriptManager
import org.nitb.orchestrator.subscription.detached.DetachedCronSubscription

class DetachedCronJsSubscription(
    name: String,
    cronExpression: String,
    cronType: CronType = CronType.UNIX,
    timeout: Long = -1,
    description: String? = null,
    private val script: String,
    private val args: Array<Any?>
): DetachedCronSubscription(name, cronExpression, cronType, timeout, description) {

    override fun onEvent(sender: String, input: Unit) {
        JsScriptManager.runScript(script, args)
    }
}