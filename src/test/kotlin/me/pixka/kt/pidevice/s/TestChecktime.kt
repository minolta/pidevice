package me.pixka.kt.pidevice.s

import me.pixka.kt.pibase.d.Pijob
import org.junit.jupiter.api.DisplayNameGenerator
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import java.text.SimpleDateFormat

@DataJpaTest
class TestChecktime {

    val df = SimpleDateFormat("hh:mm:ss a")
    @Autowired
    lateinit var ts:TaskService
    @Test
    fun testChecktime()
    {
        var job = Pijob()
        job.etimes
        ts.checktime(job)
    }
}