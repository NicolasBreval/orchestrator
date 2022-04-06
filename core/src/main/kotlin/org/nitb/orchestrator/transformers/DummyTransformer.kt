package org.nitb.orchestrator.transformers

class DummyTransformer<T>: Transformer<T, T>() {
    override fun transform(input: T): T {
        return input
    }
}