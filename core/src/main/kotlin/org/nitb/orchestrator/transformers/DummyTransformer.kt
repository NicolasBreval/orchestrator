package org.nitb.orchestrator.transformers

import java.io.Serializable

class DummyTransformer<T: Serializable>: Transformer<T, T>() {
    override fun transform(input: T): T {
        return input
    }
}