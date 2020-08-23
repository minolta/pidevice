package me.pixka.kt.pidevice.t

import com.fasterxml.jackson.databind.ObjectMapper
import me.pixka.base.line.s.NotifyService
import me.pixka.kt.pibase.c.HttpControl
import me.pixka.kt.pibase.d.Pijob
import me.pixka.kt.pibase.s.DhtvalueService
import me.pixka.kt.pibase.s.JobService
import me.pixka.kt.pibase.s.PijobService
import me.pixka.kt.pidevice.s.TaskService
import me.pixka.kt.pidevice.u.Dhtutil
import me.pixka.kt.run.D1hjobWorker
import me.pixka.kt.run.GroupRunService
import org.slf4j.LoggerFactory
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
import org.springframework.stereotype.Service
import java.util.*

@Component
class Runhjobbyd1(val pjs: PijobService,
                  val js: JobService,
                  val task: TaskService,
                  val dhs: Dhtutil, val httpControl: HttpControl,
                  val dhtvalueService: DhtvalueService, val groups: GroupRunService,
                  val ntfs: NotifyService, val queue: QueueService) {
    val om = ObjectMapper()


    @Scheduled(fixedDelay = 1000)
    fun run() {

        try {
            queue.printqueue()

            //ถ้ามี job ใน คิวให้้ run ให้หมดก่อน

            if (queue.size() > 0) {
                runQueue()//พยาม run ที่อยู่ในคิวก่อน
//                return //ออกจากระบบจนว่า
            }

            //Run ปกติ
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
        if(queue.inqueue(job)) {
            logger.warn("Job alreay in queue ${job.name}")
            return false
        }

//        if (groups.c(job)) {
        var t = D1hjobWorker(job, dhtvalueService, dhs, httpControl, task, ntfs)
        if (task.checktime(job)) {
            if (t.checkCanrun()) {
                if (groups.c(job))
                    if (task.run(t)) {
                        logger.debug("Run h job ${job.name}")
                    } else
                        logger.warn("Not run h job ${job.name}")
                else {
                    logger.debug("This job can run but some job use water have to queue ${job.name}")
                    queue.addtoqueue(job)
                }
            } else {
                logger.warn("H not in rang ${job.name}")
            }
        } else {
            logger.warn("Time not rang ${job.name}")
        }
        return true
    }


    /**
     * ต้อง clar คิวให้หมดก่อน
     */
    fun runQueue() {


        logger.debug("Run inqueue ${Date()}")
        try {
            for (j in queue.queue) {
                logger.debug("inqueue to run  ${j.name}")
                if (groups.c(j) && task.checkrun(j)) {
                    var t = D1hjobWorker(j, dhtvalueService, dhs, httpControl, task, ntfs)
                    if (t.checkCanrun()) {
                        if(task.checktime(j)) {
                            if (task.run(t)) {
                                logger.debug("Run ${j.name} inqueue ")
                                if (queue.queue.remove(j))
                                    logger.debug("Run inqueue and remove ${j.name}")
                                else
                                    logger.error("Error  inqueue can not remove ${j.name}")
                            }
                        }
                        else
                        {
                            //ไม่อยู่ในช่วงเวลาแล้วออกจากการทำงานเลย
                            logger.debug("inqueue not in time have to remove")
                            if (queue.queue.remove(j))
                                logger.debug("Run inqueue and remove ${j.name}")
                            else
                                logger.error("Error  inqueue can not remove ${j.name}")
                        }
                    }
                    else
                    {
                        logger.debug("inqueue not in H have to remove")
                        if (queue.queue.remove(j))
                            logger.debug("Run inqueue and remove ${j.name}")
                        else
                            logger.error("Error  inqueue can not remove ${j.name}")
                    }
                } else {
                    logger.warn("Can not run this job now ${j.name} inqueue ")
                }
            }
        } catch (e: Exception) {
            logger.error("inqueue ${e.message}")
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

@Service
class QueueService {
    //สำหรับจัดคิวให้ทุก job ได้ทำงานไม่ต้องแยงกัน

    var queue = LinkedList<Pijob>()

    fun inqueue(j:Pijob): Boolean {
        var ii = queue.find { it.id.toInt() == j.id.toInt() }

        Runhjobbyd1.logger.debug("inqueue have ${ii} in queue")
        if(ii!=null)
            return true

        return false
    }
    fun peek(): Pijob? {
        return queue.peek()
    }

    fun remove(p: Pijob): Boolean {
        return queue.remove(p)
    }

    fun size(): Int {
        return queue.size
    }

    /**
     * เข้าคิวไว้ก่อน
     */
    fun addtoqueue(job: Pijob): Boolean {

        if (queue.size == 0) {
            queue.add(job)
            Runhjobbyd1.logger.debug("Add ${job.name} to queue")
            return true
        }


        if (queue.find { job.id.toInt() == it.id.toInt() } == null) {
            queue.add(job)
            Runhjobbyd1.logger.debug("Add more ${job.name} to  queue")
            return true
        }
        Runhjobbyd1.logger.debug("Not Add ${job.name} to  queue")
        return false

    }

    fun poll() = queue.poll()
    fun printqueue() {
        println("Printqueue")
        Runhjobbyd1.logger.debug("queue size : ${queue.size} ${Date()}")
        queue.map {
            Runhjobbyd1.logger.debug("queue : ${it}")
            println("queue : ${it}")
        }
    }
}