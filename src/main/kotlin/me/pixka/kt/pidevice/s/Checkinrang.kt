package me.pixka.kt.pidevice.s

import me.pixka.kt.pibase.d.Pijob
import org.springframework.stereotype.Service

/**
 *
 * ใช้สำหรับ ตรวจสอบช่วงทำงานของ การตังเวลา
 */

@Service
class Checkinrang(val mtp: MactoipService) {

    fun checklow(value: Double, low: Double): Boolean {

        if (value >= low)
            return true

        return false

    }

    fun checkhigh(value: Double, high: Double): Boolean {

        if (value >= high)
            return true
        return false
    }
}