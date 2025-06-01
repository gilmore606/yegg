@file:Suppress("NOTHING_TO_INLINE")

package com.dlfsystems.world

import com.dlfsystems.server.Yegg
import com.dlfsystems.value.Value
import com.dlfsystems.world.trait.Trait
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import java.util.*

@Serializable
@SerialName("PV")
data class Propval(
    // Inherited trait on which the value is found, if clear
    val traitID: Trait.ID,
    // The actual value, if not clear (null = get from trait)
    var v: Value? = null,
) {
    override fun toString() = "(\$${traitID.trait()?.name}) $v"
    override fun equals(other: Any?) = (other is Propval) && this.traitID == other.traitID && v == other.v
    override fun hashCode() = Objects.hash(traitID, v)

    inline fun get(name: String) = v ?: Yegg.world.traits[traitID]!!.getProp(name)
}
