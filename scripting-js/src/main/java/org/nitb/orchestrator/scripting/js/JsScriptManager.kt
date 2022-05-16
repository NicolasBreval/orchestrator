package org.nitb.orchestrator.scripting.js

import org.graalvm.polyglot.Context
import org.graalvm.polyglot.HostAccess
import org.graalvm.polyglot.PolyglotAccess
import java.io.Serializable

object JsScriptManager {

    fun runScript(script: String, vararg args: Any?) {
        createContext().use { context ->
                context.getBindings("js").putMember("args", context.asValue(args))
                context.eval("js", script)
            }
    }

    fun runScriptAddingInput(script: String, input: Any?, vararg args: Any?) {
        createContext().use { context ->
                context.getBindings("js").putMember("args", context.asValue(args))
                context.getBindings("js").putMember("input", context.asValue(input))
                context.eval("js", script)
            }
    }

    fun runScriptWithResult(clazz: Class<*>, script: String, vararg args: Any?): Serializable {

        if (!Serializable::class.java.isAssignableFrom(clazz))
            throw IllegalArgumentException("Invalid class for result, it must be serializable")

        createContext().use { context ->
                context.getBindings("js").putMember("args", context.asValue(args))
                val result = context.eval("js", script)
                return result.`as`(clazz) as Serializable
            }
    }

    fun runScriptWithResultAddingInput(clazz: Class<*>, script: String, input: Any?, vararg args: Any?): Serializable {

        if (!Serializable::class.java.isAssignableFrom(clazz))
            throw IllegalArgumentException("Invalid class for result, it must be serializable")

        createContext().use { context ->
                context.getBindings("js").putMember("args", context.asValue(args))
                context.getBindings("js").putMember("input", context.asValue(input))
                val result = context.eval("js", script)
                return result.`as`(clazz) as Serializable
            }
    }

    private fun createContext(): Context {
        return Context.newBuilder("js")
            .allowHostAccess(HostAccess.ALL) //allows access to all Java classes
            .allowHostClassLookup { true }
            .allowPolyglotAccess(PolyglotAccess.ALL)
            .build();
    }
}