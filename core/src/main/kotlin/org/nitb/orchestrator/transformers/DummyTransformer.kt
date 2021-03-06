package org.nitb.orchestrator.transformers

import java.io.Serializable

class DummyTransformer<T: Serializable>: Transformer<T>() {
    override fun transform(input: T): Serializable {
        return input
    }
}