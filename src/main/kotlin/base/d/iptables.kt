package me.pixka.kt.base.d

import java.util.*
import javax.persistence.Entity

@Entity
class Iptableskt(var devicename: String? = null, var ip: String? = null, var mac: String? = null, var lastupdate: Date, var lastcheckin: Date) : En() {
    constructor() : this(lastupdate = Date(), lastcheckin = Date())

    override fun toString(): String {
        return "IP: ${ip}  Last check in ${lastupdate} ${lastupdate}"
    }
}

