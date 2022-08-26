package me.pixka.kt.pidevice.t

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import me.pixka.base.line.s.NotifyService
import me.pixka.kt.pibase.d.Pijob
import me.pixka.kt.pibase.d.SensorinjobService
import me.pixka.kt.pibase.o.HObject
import me.pixka.kt.pibase.s.FindJob
import me.pixka.kt.pibase.s.PijobService
import me.pixka.kt.pidevice.d.ConfigdataService
import me.pixka.kt.pidevice.s.MactoipService
import me.pixka.kt.pidevice.s.TaskService
import me.pixka.kt.pidevice.s.WarterLowPressureService
import me.pixka.kt.run.D1hjobWorker
import me.pixka.kt.run.GroupRunService
import me.pixka.log.d.LogService
import org.slf4j.LoggerFactory
import org.springframework.context.annotation.Profile
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
import org.springframework.stereotype.Service
import java.util.*

/**
 * เป็น การ run แบบเรียงลำดับ
 */
@Component
@Profile("!test")
class Runhjobbyd1(
    val pjs: PijobService, val findJob: FindJob,
    val task: TaskService,
    val mtp: MactoipService,
    val groups: GroupRunService, val lgs: LogService,
    val ntfs: NotifyService, val queue: QueueService,val lps:WarterLowPressureService,
    val sipj:SensorinjobService,val cs:ConfigdataService
) {
    val om = ObjectMapper()

    fun rQ() {
        try {
            //ถ้ามี job ใน คิวให้้ run ให้หมดก่อน
            if (queue.size() > 0) {
                runQueue()//พยาม run ที่อยู่ในคิวก่อน
            }
        } catch (e: Exception) {
            logger.error("ERROR ${e.message}")
            lgs.createERROR(
                "${e.message}", Date(),
                "Runhjobbyd1", "", "", "rQ()",
                System.getProperty("mac")
            )
        }
    }

    @Scheduled(fixedDelay = 1000)
    fun run() {
//        rQ()

        try {
            runQ2()
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
                                        var t = D1hjobWorker(job, mtp, ntfs,lps,cs)
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
                            logger.error("ERROR ${e.message} JOB:${it.name}")
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
        try {
            var ip = job.desdevice?.ip
            logger.debug("Call IP : ${ip} RunH")
            if (ip != null) {
                try {
                    var re = mtp.http.get("http://${ip}", 12000)
                    var h = om.readValue<HObject>(re)
                    return h
                }catch (e:Exception)
                {
                   var sensorlist =  sipj.findByPijob_id(job.id)
                    if(sensorlist!=null &&sensorlist.size>0)
                    {
                        sensorlist.forEach{

                            try{
                                var re = mtp.http.get("http://${it.sensor!!.ip}", 12000)
                                var h = om.readValue<HObject>(re)
                                return h
                            }
                            catch (e:Exception)
                            {
                                //can not connect to target

                            }

                            throw Exception("Can not read")
                        }
                    }

                }
            }
            logger.error("getHobj IP is Null ")
            lgs.createERROR(
                "IP is null ", Date(),
                "Runhjobbyd1", "", "114", "getHobj", job.desdevice?.mac,
                job.refid
            )
            throw Exception("Ip is null")

        } catch (e: Exception) {
            logger.error("getHobj : ERROR: ${e.message} ")
            lgs.createERROR(
                "${e.message}", Date(),
                "Runhjobbyd1", "", "114", "getHobj", job.desdevice?.mac,
                job.refid
            )
            throw e
        }

//        return null
    }


    fun runfromQ(q: Qobject) {
        try {
            var t = D1hjobWorker(q.pijob!!, mtp, ntfs,lps,cs)
            if (!task.checkrun(q.pijob!!)) {
                var run = task.run(t)
                if (run) {
                    q.message = "Run !! remove from Queue"
                    q.addDate = Date()
                    removeJobFromQ(q.pijob!!)
                }
            } else {
                removeJobFromQ(q.pijob!!)
            }
        } catch (e: Exception) {
            logger.error("RUN From Q : ${e.message}")
            lgs.createERROR(
                "${e.message}", Date(),
                "Runhjobbyd1", Thread.currentThread().name, "140", "runfromQ",
                q.pijob?.desdevice?.mac,
                q.pijob?.desdevice?.refid
            )
        }
    }

    fun removeJobFromQ(job: Pijob) {
        try {
            var removed = queue.remove(job)
            logger.debug("Remove ${job.name} from queue is ${removed}")
        } catch (e: Exception) {
            logger.error("Remove Q ${job.name} ERROR ${e.message}")
            lgs.createERROR(
                "${e.message}", Date(),
                "Runhjobd1", Thread.currentThread().name, "156", "removeJobFromQ",
                job.desdevice?.mac, job.refid
            )

        }
    }

    /**
    //ใช้สำหรับตรวจสอบว่าอันไหนมีใน task บ้างถ้ามีแล้วเอาออกเลย
     */
    fun maskCurrentrun() {
        try {
            queue.queue.forEach {
                if (task.checkrun(it.pijob!!)) {
                    it.toremove = true
                }
            }
        } catch (e: Exception) {
            logger.error("ERROR maskCurrentrun ${e.message}")
            throw e
        }
    }

    /**
    //ใช้หาว่า หมดเวลายัง
     */
    fun maskbyTime() {
        try {
            queue.queue.forEach {
                if (!task.checktime(it.pijob!!)) {
                    it.toremove = true
                }
            }
        } catch (e: Exception) {
            logger.error("MakeByTime eRROR ${e.message}")
            throw e
        }
    }

    /**
     * เอาที่ toremove = true ออกให้หมด
     */
    fun remove() {
        try {
            queue.queue = queue.queue.filter { it.toremove == false } as ArrayList<Qobject>
        } catch (e: Exception) {
            logger.error("Error remove all ${e.message}")
            throw e
        }
    }

    /**
     * Run ใน คิวก่อน
     */
    fun runQ2() {
        try {
            maskCurrentrun()
            maskbyTime()
            remove()

            queue.queue.forEach {
                if (groups.c(it.pijob!!)) {//ไม่มีใครใช้น้ำ
                    var t = D1hjobWorker(it.pijob!!, mtp, ntfs,lps,cs)
                    if (task.run(t)) {
                        it.toremove = true //เพราะ run แล้ว
                        logger.debug("Run ${it.pijob?.name}")
                    }
                    //มีปัญหาไม่ run รอต่อไป
                }
                //else มีคนใช้น้ำอยู่ก็รอต่อไป


            }
        } catch (e: Exception) {
            logger.error("RunQ2 ${e.message}")

        }


    }

    /**
     * ต้อง clar คิวให้หมดก่อน
     */
    fun runQueue() {
        var mac: String? = null
        var refid: Long? = 0
        try {
            logger.debug("Run inqueue ${Date()}")
            if (queue.queue.size > 0)
                for (q in queue.queue) {
                    try {
                        if (q.pijob != null) {
                            mac = q.pijob?.desdevice?.mac
                            refid = q.pijob?.refid
                        }
                    } catch (e: Exception) {
                        logger.error("runQueue : ${e.message}")
                        lgs.createERROR(
                            "${e.message}", Date(), "Runhjobbyd1",
                            Thread.currentThread().name, "178", "runQueue()",
                            "${mac}", q.pijob?.refid
                        )
                    }

                    if (q.pijob != null) {
                        if (task.checktime(q.pijob!!)) {
                            if (groups.c(q.pijob!!))
                                runfromQ(q)
                            else {
                                q.message = "Wait for other use water ${Date()}"
//                                logger.error("Some job use water")
                            }
                        } else {//ถ้า job เกินเวลาแล้วเอาออกเลย
                            q.message = "Out of date !! ${q.pijob?.stimes} - ${q.pijob?.etimes}"
                            removeJobFromQ(q.pijob!!)
                        }
                        if (!task.checkrun(q.pijob!!)) {//ถ้ามีอยู่ในtask แล้วก็เอาออกจาก Queue เลย
                            q.message = "this job already run"
                            removeJobFromQ(q.pijob!!)
                        }
                    }
                }
        } catch (e: Exception) {
            logger.error("runQueue : ERROR ${e.message}")
            lgs.createERROR(
                "${e.message}", Date(), "Runhjobbyd1",
                "", "173", "runQueue()", mac, refid
            )
        }
    }
    var logger = LoggerFactory.getLogger(Runhjobbyd1::class.java)
}

@Service
class QueueService {
    //สำหรับจัดคิวให้ทุก job ได้ทำงานไม่ต้องแยงกัน
    var queue = ArrayList<Qobject>()
    fun inqueue(j: Pijob): Boolean {
        var ii = queue.find { it.pijob?.id?.toInt() == j.id.toInt() }
        logger.debug("inqueue have ${ii} in queue")
        if (ii != null)
            return true

        return false
    }


    fun remove(p: Pijob): Boolean {
        var toremove: Qobject? = null
        try {
            toremove = queue.find { it.pijob?.equals(p)!! }
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

    //    fun poll() = queue.poll()
    fun printqueue() {
        logger.debug("queue size : ${queue.size} ${Date()}")
        queue.map {
            logger.debug("queue : ${it}")
            println("queue : ${it}")
        }
    }

    var logger = LoggerFactory.getLogger(QueueService::class.java)
}

class Qobject(
    var pijob: Pijob? = null,
    var addDate: Date? = null,
    var message: String? = null,
    var toremove: Boolean = false
) {
    override fun equals(other: Any?): Boolean {

        if (other is Pijob) {
            if (other.equals(this.pijob))
                return true
        }
        if(other is Qobject)
        {
            if(other.pijob?.equals(this.pijob)!!)
                return true
        }

        return false
    }
}


