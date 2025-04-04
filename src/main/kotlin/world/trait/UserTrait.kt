package com.dlfsystems.world.trait

import com.dlfsystems.value.VString
import com.dlfsystems.value.Value

class UserTrait : Trait("user") {

    override val props = mutableMapOf<String, Value>(
        "username" to VString(""),
        "password" to VString(""),
    )

}
