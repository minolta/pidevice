package me.pixka.kt.pidevice.t

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import me.pixka.base.line.s.NotifyService
import me.pixka.kt.pibase.d.IptableServicekt
import me.pixka.kt.pibase.d.Pijob
import me.pixka.kt.pibase.o.HObject
import me.pixka.kt.pibase.s.FindJob
import me.pixka.kt.pibase.s.HttpService
import me.pixka.kt.pibase.s.JobService
import me.pixka.kt.pibase.s.PijobService
import me.pixka.kt.pidevice.s.TaskService
import me.pixka.kt.pidevice.u.Dhtutil
import me.pixka.kt.run.D1hjobWorker
import me.pixka.kt.run.GroupRunService
import me.pixka.log.d.LogService
import org.slf4j.LoggerFactory
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
import org.springframework.stereotype.Service
import java.util.*

@Component
class Runhjobbyd1(val pjs: PijobService, val findJob: FindJob,
                  val js: JobService,
                  val task: TaskService,
                  val dhs: Dhtutil,
                  val iptableServicekt: IptableServicekt,
                  val groups: GroupRunService, val lgs: LogService,
                  val ntfs: NotifyService, val queue: QueueService,
                  val httpService: HttpService) {
    val om = ObjectMapper()

    fun rQ() {
        try {
//            queue.printqueue()
            //ถ้ามี job ใน คิวให้้ run ให้หมดก่อน
            if (queue.size() > 0) {
                runQueue()//พยาม run ที่อยู่ในคิวก่อน
            }
        } catch (e: Exception) {
            logger.error("ERROR ${e.message}")
            lgs.createERROR("${e.message}", Date(),
                    "Runhjobbyd1", "", "", "rQ()",
                    System.getProperty("mac"))
        }
    }

    @Scheduled(fixedDelay = 2000)
    fun run() {
        rQ()
        try {

            //Run ปกติ
            var list = findJob.loadjob("runhbyd1")
            list?.forEach {
                var job = it
                if (!task.checkrun(job)) {//ยังไม่มีใน list
                    if (task.checktime(it)) {//ถ้าอยู่ในเวลา
                        try {
                            var h = getHobj(it)
                            if (h != null) {
                                if (checkHtorun(it, h)) {
                                    if (groups.c(job)) {//ไม่มี job run อยู่ และในกลุม run ไม่มีใครใช้น้ำ
                                        var t = D1hjobWorker(job, dhs, httpService, task, ntfs, lgs)
                                        var run = task.run(t)
                                        logger.debug("H job is run ${run}")
                                        //ถ้าการ run อยู่จะเอาเข้าคิว
                                        if (!run && !queue.inqueue(job)) {
                                            queue.addtoqueue(job)
                                            logger.debug("Add to queue ${job}")
                                        }
                                    } else {//ถ้ามี job run อยู่แล้วก็ไปดูในคิวว่ามีเปล่า
                                        if (!queue.inqueue(job)) {
                                            queue.addtoqueue(job)
                                            logger.debug("Add to queue ${job}")
                                        } else {
                                            logger.debug("In queue ${job}")
                                        }
                                    }
                                }

                            }
                        } catch (e: Exception) {
                            logger.error("ERROR ${e.message}")
                        }

                    }
                } else {
                    logger.warn("Have job run ready ${job.name}")
                }
            }

        } catch (e: Exception) {
            logger.error("Read h by d1 ERROR ${e.message}")

        }
    }

    fun checkHtorun(pijob: Pijob, h: HObject): Boolean {

        var hl = pijob.hlow?.toDouble()
        var hh = pijob.hhigh?.toDouble()
        var hvalue = h.h?.toDouble()

        if (hl!! <= hvalue!! && hvalue <= hh!!)
            return true

        return false
    }

    fun getHobj(job: Pijob): HObject? {
        var ip = iptableServicekt.findByMac(job.desdevice?.mac!!)
        logger.debug("Call IP : ${ip} RunH")
        try {
            if (ip != null) {
                var re = httpService.get("http://${ip.ip}", 2000)
                var h = om.readValue<HObject>(re)
                return h
            }

        } catch (e: Exception) {
            throw e
        }

        return null
    }


    fun runfromQ(q: Qobject) {
        try {
            var t = D1hjobWorker(q.pijob!!, dhs, httpService, task, ntfs, lgs)
            var run = task.run(t)
            if (run) {
                q.message = "Run !! remove from Queue"
                q.addDate = Date()
                removeJobFromQ(q.pijob!!)
            }
        } catch (e: Exception) {
            lgs.createERROR("${e.message}", Date(),
                    "Runhjobbyd1", "", "", "runfromQ",
                    q.pijob?.desdevice?.mac,
                    q.pijob?.desdevice?.refid)
        }
    }

    fun removeJobFromQ(job: Pijob) {
        try {
            var removed = queue.remove(job)
            logger.debug("Remove ${job.name} from queue is ${removed}")
        } catch (e: Exception) {
            lgs.createERROR("${e.message}", Date(),
                    "Runhjobd1", "", "", "removeJobFromQ",
                    job.desdevice?.mac, job.refid)
            logger.error("Remove Q ${job.name} ERROR ${e.message}")
        }
    }

    /**
     * ต้อง clar คิวให้หมดก่อน
     */
    fun runQueue() {
        var mac:String?=null
        var refid:Long ?=0
        try {
            logger.debug("Run inqueue ${Date()}")

            for (q in queue.queue) {
                mac = q.pijob?.desdevice?.mac
                refid = q.pijob?.refid
                if (task.checktime(q.pijob!!)) {
                    if (groups.c(q.pijob!!))
                        runfromQ(q)
                    else {
                        q.message = "Wait for other use water ${Date()}"
                        logger.error("Some job use water")
                    }
                } else {//ถ้า job เกินเวลาแล้วเอาออกเลย
                    q.message = "Out of date !! ${q.pijob?.stimes} - ${q.pijob?.etimes}"
                    removeJobFromQ(q.pijob!!)
                }
                if (!task.checkrun(q.pijob!!)) {//ถ้ามีอยู่ในtask แล้วก็เอาออกจาก Queue เลย
                    removeJobFromQ(q.pijob!!)
                }
            }
        } catch (e: Exception) {
            logger.error("ERROR ${e.message}")
            lgs.createERROR("${e.message}",Date(),"Runhjobbyd1",
            "","","",mac,refid)
        }

    }

    companion object {
        internal var logger = LoggerFactory.getLogger(Runhjobbyd1::class.java)
    }
}

@Service
class QueueService {
    //สำหรับจัดคิวให้ทุก job ได้ทำงานไม่ต้องแยงกัน
    var queue = LinkedList<Qobject>()
    fun inqueue(j: Pijob): Boolean {
        var ii = queue.find { it.pijob?.id?.toInt() == j.id.toInt() }
        logger.debug("inqueue have ${ii} in queue")
        if (ii != null)
            return true

        return false
    }

    fun peek(): Qobject? {
        return queue.peek()
    }

    fun remove(p: Pijob): Boolean {
        var toremove: Qobject? = null
        try {
            toremove = queue.find { it.pijob?.id == p.id }
            if (toremove != null)
                return queue.remove(toremove)
            return false
        } catch (e: Exception) {
            logger.error("ERROR remove h job ${p.name} ${e.message}")
            if (toremove != null) {
                toremove.message = e.message
            }
            throw e
        }
    }

    fun size(): Int {
        return queue.size
    }

    /**
     * เข้าคิวไว้ก่อน
     */
    fun addtoqueue(job: Pijob): Boolean {
        if (queue.size == 0) {
            queue.add(Qobject(job, Date()))
            logger.debug("Add ${job.name} to queue")
            return true
        }
        if (queue.find { job.id.toInt() == it.pijob?.id?.toInt() } == null) {
            queue.add(Qobject(job, Date()))
            logger.debug("Add more ${job.name} to  queue")
            return true
        }
        logger.debug("Not Add ${job.name} to  queue")
        return false
    }

    fun poll() = queue.poll()
    fun printqueue() {
        logger.debug("queue size : ${queue.size} ${Date()}")
        queue.map {
            logger.debug("queue : ${it}")
            println("queue : ${it}")
        }
    }

    companion object {
        internal var logger = LoggerFactory.getLogger(QueueService::class.java)
    }
}

class Qobject(var pijob: Pijob? = null, var addDate: Date? = null, var message: String? = null)


