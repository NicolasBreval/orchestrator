package org.nitb.orchestrator.transformers

abstract class Transformer<I, O> {

    abstract fun transform(input: I): O

}