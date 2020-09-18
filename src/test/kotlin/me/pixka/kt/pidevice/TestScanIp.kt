package me.pixka.kt.pidevice

import org.junit.jupiter.api.Test
import java.net.InetAddress


class TestScanIp {

    var ir = InetRange()
    fun rangeFromCidr(cidrIp: String): IntArray {
        val maskStub = 1 shl 31
        val atoms = cidrIp.split("/".toRegex()).toTypedArray()
        val mask = atoms[1].toInt()
        println(mask)
        val result = IntArray(2)
        result[0] = ir.ipToInt(atoms[0]) and (maskStub shr mask - 1) // lower bound
        result[1] = ir.ipToInt(atoms[0]) // upper bound
        System.out.println(ir.intToIp(result[0]))
        System.out.println(ir.intToIp(result[1]))
        return result
    }

    @Test
    fun scan() {
        rangeFromCidr("192.168.88.0/23")
    }
}


class InetRange {

    fun ipToInt(ipAddress: String?): Int {
//        return try {
//            val bytes = InetAddress.getByName(ipAddress).address
//            val octet1: Int = bytes[0] and 0xFF shl 24
//            val octet2: Int = bytes[1] and 0xFF shl 16
//            val octet3: Int = bytes[2] and 0xFF shl 8
//            val octet4: Int = bytes[3] and 0xFF
//            octet1 or octet2 or octet3 or octet4
//        } catch (e: Exception) {
//            e.printStackTrace()
//            0
//        }
        return 0
    }

    fun intToIp(ipAddress: Int): String? {
        val octet1 = ipAddress and -0x1000000 ushr 24
        val octet2 = ipAddress and 0xFF0000 ushr 16
        val octet3 = ipAddress and 0xFF00 ushr 8
        val octet4 = ipAddress and 0xFF
        return StringBuffer().append(octet1).append('.').append(octet2)
                .append('.').append(octet3).append('.')
                .append(octet4).toString()
    }
}