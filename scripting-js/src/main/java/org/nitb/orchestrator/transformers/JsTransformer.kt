package org.nitb.orchestrator.transformers

import org.nitb.orchestrator.scripting.js.JsScriptManager
import java.io.Serializable

class JsTransformer(
    private val className: String,
    private val script: String
): Transformer<Serializable, Serializable>() {

    override fun transform(input: Serializable): Serializable {
        return JsScriptManager.runScriptWithResult(Class.forName(className), script)
    }
}