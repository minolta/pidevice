package me.pixka.kt.pidevice.t

import com.fasterxml.jackson.databind.ObjectMapper
import me.pixka.c.HttpControl
import me.pixka.kt.pibase.d.Pijob
import me.pixka.kt.pidevice.s.TaskService
import me.pixka.kt.pidevice.u.Dhtutil
import me.pixka.kt.run.D1hjobWorker
import me.pixka.kt.run.GroupRunService
import me.pixka.pibase.s.DhtvalueService
import me.pixka.pibase.s.JobService
import me.pixka.pibase.s.PijobService
import org.slf4j.LoggerFactory
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
import java.util.*

@Component
class Runhjobbyd1(val pjs: PijobService,
                  val js: JobService,
                  val task: TaskService,
                  val dhs: Dhtutil, val httpControl: HttpControl,
                  val dhtvalueService: DhtvalueService, val groups: GroupRunService) {
    val om = ObjectMapper()


    //สำหรับจัดคิวให้ทุก job ได้ทำงานไม่ต้องแยงกัน
    var queue = LinkedList<Pijob>()

    @Scheduled(fixedDelay = 1000)
    fun run() {

        try {
            printqueue()

            //ถ้ามี job ใน คิวให้้ run ให้หมดก่อน

            if (queue.size > 0) {
                runQueue()//พยาม run ที่อยู่ในคิวก่อน
//                return //ออกจากระบบจนว่า
            }
            var list = loadjob()
            if (list != null) {
                logger.debug("Job for Runhjobbyd1 Hjobsize  ${list.size}")
                list.map {
                    logger.debug("RunH  ${it.name}")
                    t(it)
                }
            }
        } catch (e: Exception) {
            logger.error("Read h by d1 ERROR ${e.message}")
        }
    }

    //run Job
    fun t(job: Pijob): Boolean {


        if (!task.checkrun(job)) {
            logger.warn("this job is run now ${job.name}")
            return false // now runninsg
        }


//        if (groups.c(job)) {
        var t = D1hjobWorker(job, dhtvalueService, dhs, httpControl, task)
        if (task.checktime(job)) {
            if (t.checkCanrun()) {
                if (groups.c(job))
                    if (task.run(t)) {
                        logger.debug("Run h job ${job.name}")
                    } else
                        logger.warn("Not run h job ${job.name}")
                else {
                    addtoqueue(job)
                }
            }else
            {
                logger.warn("H not in rang ${job.name}")
            }
        }
        else
        {
            logger.warn("Time not rang ${job.name}")
        }


//        if (groups.c(job) && task.checktime(job) && t.checkCanrun()) {
//            //อยู่ในช่วงเวลาแต่ groups ใช้น้ำอยู่
//            logger.debug("${job} ********************** Somedeviceusewater ***************")
//            t.state = " Somedeviceusewate"
//            addtoqueue(job) //ถ้ามีคนใช้ให้เข้าคิวไว้ก่อน
//            return false
//
//
//        } else {
//            if (task.checktime(job)) {
//                if (!t.checkCanrun()) {
//                    t.state = "H not in ranger"
//                } else {
//                    var run = task.run(t)
//                    logger.debug("${job} RunJOB ${run}")
//                    return true
//                }
//            } else {
//                logger.debug("${job} Not in time rang ")
//                t.state = "Not in run in this time"
//            }
//        }


        return true
    }

    fun runQueue() {

        var it = queue.peek()
        if (groups.c(it)) {
            //กลุ่มว่างไม่มีใครใช้น้ำแล้วไม่ต้อง check เวลาแล้ว และไม่มี job นี้ทำงานนี้อยู่


            if (task.checkrun(it)) {

                var t = D1hjobWorker(it, dhtvalueService, dhs, httpControl, task)
                if (task.run(t)) {
                    var forremove = queue.poll()
                    logger.debug("Run inqueue ${forremove.name}")
                }

            } else {
                logger.warn("This job have already run ${it.name} queue")
            }
        } else {
            logger.warn("Someone use water in this group ${it.pijobgroup} queue")
        }


    }

    /**
     * เข้าคิวไว้ก่อน
     */
    fun addtoqueue(job: Pijob): Boolean {

        if (queue.size == 0) {
            queue.add(job)
            logger.debug("Add to queue")
            return true
        }


        if (queue.find { job.id == it.id } == null) {
            queue.add(job)
            logger.debug("Add more queue")
            return true
        }
        return false

    }

    fun printqueue() {
        println("Printqueue")
        logger.debug("queue size : ${queue.size} ${Date()}")
        queue.map {
            logger.debug("queue : ${it}")
            println("queue : ${it}")
        }
    }


    fun loadjob(): List<Pijob>? {
        var job = js.findByName("runhbyd1")

        if (job != null) {

            var jobs = pjs.findJob(job.id)
            return jobs
        }
        throw Exception("Not have JOB")
    }

    companion object {
        internal var logger = LoggerFactory.getLogger(Runhjobbyd1::class.java)
    }
}