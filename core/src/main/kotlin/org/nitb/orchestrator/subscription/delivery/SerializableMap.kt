package org.nitb.orchestrator.subscription.delivery

import java.io.Serializable

class SerializableMap<K, V>(): HashMap<K, V>(), Serializable {
    constructor(map: Map<K, V>) : this() {
        this.putAll(map)
    }
}