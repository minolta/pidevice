package me.pixka.kt.run

import com.pi4j.io.gpio.GpioPinDigitalOutput
import me.pixka.kt.pibase.c.Piio
import me.pixka.kt.pibase.d.DS18value
import me.pixka.kt.pibase.d.Pijob
import me.pixka.kt.pibase.d.Portstatusinjob
import me.pixka.kt.pibase.s.*
import org.joda.time.DateTime
import org.joda.time.Interval
import org.joda.time.Period
import org.joda.time.Seconds
import org.slf4j.LoggerFactory
import org.springframework.context.annotation.Profile
import java.math.BigDecimal
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit


@Profile("pi")
class Workercounter(var pijob: Pijob, var ps: PortstatusinjobService,
                    var gpio: GpioService, val ss: SensorService, val dps: DisplayService,
                    val ms: MessageService, val io: Piio, val dss: DS18sensorService) : Runnable, PijobrunInterface {

    var state: String? = " Create "
    var startRun: Date? = null
    var run: Long? = null
    var timeout: Long? = 10000 //สำหรับหมดเวลา
    var startdate: Date? = null
    var runtime: Date? = null
    var completerun: Int? = 0 //เวลาที่ run ไปแล้ว
    var finishrun: Date? = null //เวลาที่ เสร็จ
    var next3: Date? = null

    var isRun = true
    var period: Period? = null
    override fun setrun(p: Boolean) {
        isRun = p
    }
    override fun state(): String? {
        return state
    }

    override fun startRun(): Date? {
        return startRun
    }


    override fun getPJ(): Pijob {
        return pijob
    }

    override fun getPijobid(): Long {
        return pijob.id
    }

    override fun runStatus(): Boolean {
        return isRun
    }

    override fun setG(gpios: GpioService) {
        this.gpio = gpios
    }

    override fun setP(pijob: Pijob) {
        this.pijob = pijob
    }

    var messageto = false
    override fun run() {
        try {
            logger.debug("Start runcounter job ID ***${pijob.id}***")
            var timeoutcount = 0
            startRun = Date()
            var endcounter = false
            while (true) {
                var desid = pijob.desdevice_id
                var sensorid = pijob.ds18sensor_id
                //อ่านจาก DS เครื่องอื่น
                var value: BigDecimal? = readValue(pijob)

                logger.debug("Read value : ${value}")
                state = "Read value :${value}"
                if (value != null) {
                    //ถ้ามีข้อมูล
                    state = "Start run"

                    var inrange = checkrang(value)

                    if (inrange) {
                        //ถ้าข้อมูลอยู่ในช่วงที่กำหนด
                        if (startdate == null) {
                            startcounter()
                        }

                        var havetorun = run?.toInt()!!
                        var runcomplete = findrun()
                        completerun = runcomplete

                        if (runcomplete >= havetorun) {
                            endcounter = countok()
                            break
                        }
                    } else {
                        logger.error("Value not in range range ${pijob.tlow} <= ${value} => ${pijob.thigh} ")
                        timeoutcount++
                        if (timeoutcount >= timeout?.toInt()!!) {
                            endcounter = timeout(timeoutcount)
                            break
                        }

                    }
                }

                try {
                    display()
                } catch (e: Exception) {
                    logger.error("Display error ${e.message}")
                }
                logger.debug("Counter Wait in 1 minits")
                TimeUnit.MINUTES.sleep(1)
            }

            try {
                if (endcounter) {
                    runPorts(pijob)
                }
            } catch (e: Exception) {
                logger.error("Error ${e.message}")
            }

            isRun = false
        } catch (e: Exception) {
            logger.error("Have Error ${e.message}")
            isRun = false

        }

        isRun = false


    }

    fun timeout(timeoutcount: Int): Boolean {
        logger.error("Time out count exit ${timeoutcount}")
        state = "Value out of rang stop counter"
        isRun = false
        return false
    }

    fun countok(): Boolean {
        logger.debug("End run counter job ID ***${pijob.id}***")
        messageto = false
        isRun = false
        state = "Counter is in interrup"
        return true
    }

    fun findrun(): Int {
        var rt = DateTime()
        var st = DateTime(startdate)
        runtime = rt.toDate()
        period = Interval(st, rt).toPeriod() //ช่วงเวลา
        var r = Seconds.secondsBetween(st, rt)
        return r.seconds

    }

    fun startcounter() {
        startdate = Date()
        timeout = pijob.waittime //
        run = pijob.runtime //เวลาในการ run เอ็นวินาที
        finishrun = DateTime().plusSeconds(pijob.runtime?.toInt()!!).toDate() //เวลาเสร็จ
        next3 = DateTime().plusSeconds(pijob.runtime?.toInt()!! - 7200).toDate() //เวลาเสร็จ
    }

    fun checkrang(value: BigDecimal): Boolean {
        try {
            var v = value.toFloat()
            var l = pijob.tlow?.toFloat()
            var h = pijob.thigh?.toFloat()

            if (v >= l!! && v <= h!!)
                return true

        } catch (e: Exception) {
            logger.error(e.message)
        }
        return false

    }

    fun resetport(ports: List<Portstatusinjob>?) {

        if (ports != null)
            for (port in ports) {
                if (!port.status?.name.equals("check")) {
                    var pin = gpio.gpio.getProvisionedPin(port.portname?.name) as GpioPinDigitalOutput
                    logger.debug("Reset pin ${pin}")
                    gpio.resettoDefault(pin)
                    logger.debug("Reset Port to default")
                }
            }

    }

    fun readValue(job: Pijob): BigDecimal? {
        var v: DS18value? = null
        try {
            v = ss.readDsOther(job.desdevice_id!!, job.ds18sensor_id!!)
        } catch (e: Exception) {
            logger.error("Read other error ${e.message}")
        }
        logger.debug("Value ${v}")
        if (v != null) {
            return v.t

        } else {
            //logger.error("Can not read ds18value")
            //read loacl
            logger.debug("Read local  ID ${job.id}")
            var s = dss.find(job.ds18sensor_id!!)
            logger.debug("Found Sensor !! ${s}")
            if (s != null) {
                var tmp = io.readDs18()
                logger.debug("Read loacal value ${tmp}")
                // var tmp = rs.readTmpByjob(job)
                if (tmp != null) {
                    return tmp
                }
            }
        }
        return null
    }

    fun setport(ports: List<Portstatusinjob>) {
        try {
            logger.debug("Gpio : ${gpio}")
            for (port in ports) {
                if (port.enable == null || port.enable == false || port.status?.name.equals("check")) {//ถ้า Enable == null หรือ false ให้ไปทำงาน port ต่อไปเลย
                    logger.error("Not set port ${port}")
                } else {
                    logger.debug("Port for pijob ${port}")
                    var pin = gpio.gpio?.getProvisionedPin(port.portname?.name) as GpioPinDigitalOutput
                    logger.debug("Pin: ${pin}")

                    //save old state
                    //  var b = Pinbackup(pin, pin.state)
                    //   pinbackuplist.add(b)

                    var sn = port.status?.name
                    logger.debug("Set to " + sn)
                    if (sn?.indexOf("low") != -1) {
                        gpio.setPort(pin, false)
                        //pin.setState(false)
                    } else
                    // pin.setState(true)
                        gpio.setPort(pin, true)

                    logger.debug("Set pin state: ${pin.state}")
                }
            }
        } catch (e: Exception) {
            logger.error("Set port ${e.message}")
            throw e
        }
    }

    fun runPorts(pijob: Pijob) {
        var ports = ps.findByPijobid(pijob.id) as List<Portstatusinjob>
        Worker.logger.debug("Start run port ${ports}")
        var runtime = pijob.runtime
        var nextrun = pijob.waittime
        if (ports != null && ports.size > 0) {
            setport(ports)
            if (runtime != null)
                TimeUnit.SECONDS.sleep(runtime)
            resetport(ports)
            if (nextrun != null)
                TimeUnit.SECONDS.sleep(nextrun)


            logger.debug("Run port is end")


        }

    }

    var df = SimpleDateFormat("HH:mm:ss")

    fun display() {
        logger.debug("Display Counter info Compilete run ${completerun} in sec end run AT: ${finishrun}  ")
        var count = 0
        state = "Run ${completerun} Close at: ${finishrun}"
        while (dps.lock) {

            TimeUnit.MILLISECONDS.sleep(100)
            logger.debug("Wait for lock display lock by ==> ${dps.obj}")
            count++
            if (count >= 500) {
                logger.error("Lock display timeout")
                break
            }
        }

        if (!dps.lock) {
            try {
                logger.debug("Start lock display ")
                var display = dps.lockdisplay(this)
                display.showMessage("Counter in ${completerun} " +
                        " Close AT: ${df.format(finishrun)}  ")
                // ms.message("Counter in ${completerun}  Next gas 3 ${df.format(next3)} " +
                //          "Sec Close AT: ${df.format(finishrun)} ", "counterinfo")
                logger.debug("Display ok.")
                if (dps.lock)
                    dps.unlock(this)
            } catch (e: Exception) {
                logger.error("Error: ${e.message}")
            } finally {

            }


        }
    }

    companion object {
        internal var logger = LoggerFactory.getLogger(Workercounter::class.java)
    }
}