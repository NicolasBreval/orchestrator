package org.nitb.orchestrator.scripting.java

import org.nitb.orchestrator.annotations.NoArgsConstructor
import java.io.Serializable

@NoArgsConstructor
abstract class ScriptBaseClass<I: Serializable, O: Serializable> {

    abstract fun onEvent(input: I): O

}