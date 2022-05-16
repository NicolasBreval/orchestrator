package org.nitb.orchestrator.transformers

import java.io.Serializable

abstract class Transformer<I: Serializable, O: Serializable> {

    abstract fun transform(input: I): O

}