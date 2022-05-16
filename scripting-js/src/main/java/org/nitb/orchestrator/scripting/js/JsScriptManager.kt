package org.nitb.orchestrator.scripting.js

import org.graalvm.polyglot.Context
import org.graalvm.polyglot.HostAccess

object JsScriptManager {

    fun runScript(script: String) {
        val context: Context = Context.newBuilder("js")
            .allowHostAccess(HostAccess.ALL) //allows access to all Java classes
            .allowHostClassLookup { true }
            .build()

        context.eval("js", script)
    }

}