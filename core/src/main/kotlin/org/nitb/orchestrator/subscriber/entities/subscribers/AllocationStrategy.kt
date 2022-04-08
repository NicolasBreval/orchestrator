package org.nitb.orchestrator.subscriber.entities.subscribers

/**
 * Enum to define all types of allocation strategies
 */
enum class AllocationStrategy {
    /**
     * This allocation strategy allows to re-allocate subscriptions only in same node where it was. To use this allocation
     * type it's necessary to put a name for each node in their properties.
     */
    FIXED,

    /**
     * When strategy is OCCUPATION, main subscriber puts new subscriptions to subscribers with less subscriptions in
     * their pools.
     */
    OCCUPATION,

    /**
     * CPU strategy allocates subscriptions in nodes with less use of CPU.
     */
    CPU,

    /**
     * MEMORY strategy allocates subscriptions in nodes with more memory available.
     */
    MEMORY,

    /**
     * CPU_MEMORY strategy allocates subscriptions in nodes with less CPU usage, if two nodes has same CPU usage, allocates them
     * in node with more memory available.
     */
    CPU_MEMORY,

    /**
     * MEMORY_CPU strategy allocates subscriptions in nodes with more memory available, if two nodes has same memory available,
     * allocates them in node with less CPU usage.
     */
    MEMORY_CPU
}