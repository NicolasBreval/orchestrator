package org.nitb.orchestrator.subscription

import org.nitb.orchestrator.scripting.js.JsScriptManager
import org.nitb.orchestrator.subscription.detached.DetachedPeriodicalSubscription

class DetachedPeriodicalJsSubscription(
    name: String,
    delay: Long,
    initialDelay: Long = 0,
    timeout: Long = -1,
    description: String? = null,
    private val script: String,
    private val args: Array<Any?>
): DetachedPeriodicalSubscription(name, delay, initialDelay, timeout, description) {

    override fun onEvent(sender: String, input: Unit) {
        JsScriptManager.runScript(script, args)
    }
}