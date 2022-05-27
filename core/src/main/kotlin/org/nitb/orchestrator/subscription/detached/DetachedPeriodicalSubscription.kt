package org.nitb.orchestrator.subscription.detached

import org.nitb.orchestrator.annotations.HeritableSubscription
import org.nitb.orchestrator.subscription.CyclicalSubscription
import org.nitb.orchestrator.subscription.entities.PeriodType

@HeritableSubscription
abstract class DetachedPeriodicalSubscription(
    name: String,
    timeout: Long = -1,
    description: String? = null,
    periodExpression: String,
    type: PeriodType
): CyclicalSubscription<Unit>(name, timeout, description, periodExpression, type)