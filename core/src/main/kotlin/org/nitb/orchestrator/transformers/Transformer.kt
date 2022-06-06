package org.nitb.orchestrator.transformers

import java.io.Serializable

abstract class Transformer<I: Serializable> {

    open fun transform(input: I): Serializable {
        throw NotImplementedError()
    }

}