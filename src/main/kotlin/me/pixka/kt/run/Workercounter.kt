package me.pixka.kt.run

import com.pi4j.io.gpio.GpioPinDigitalOutput
import me.pixka.kt.pibase.c.Piio
import me.pixka.kt.pibase.d.DS18value
import me.pixka.kt.pibase.d.Pijob
import me.pixka.kt.pibase.d.Portstatusinjob
import me.pixka.kt.pibase.s.DisplayService
import me.pixka.kt.pibase.s.GpioService
import me.pixka.kt.pibase.s.MessageService
import me.pixka.kt.pibase.s.SensorService
import me.pixka.pibase.s.DS18sensorService
import me.pixka.pibase.s.PijobService
import me.pixka.pibase.s.PortstatusinjobService
import org.joda.time.DateTime
import org.joda.time.Interval
import org.joda.time.Period
import org.joda.time.Seconds
import org.slf4j.LoggerFactory
import org.springframework.context.annotation.Profile
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit


@Profile("pi")
class Workercounter(var pijob: Pijob, var ps: PortstatusinjobService,
                    var gpio: GpioService, val ss: SensorService, val dps: DisplayService,
                    val ms: MessageService, val io: Piio, val dss: DS18sensorService) : Runnable, PijobrunInterface {
    override fun state(): String? {
        return state
    }

    override fun startRun(): Date? {
        return startRun
    }

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

        logger.debug("Start run counter job ID ***${pijob.id}***")
        var timeoutcount = 0
        startRun = Date()
        var endcounter = false
        while (true) {
            var desid = pijob.desdevice_id
            var sensorid = pijob.ds18sensor_id
            //อ่านจาก DS เครื่องอื่น
            var value: DS18value? = null

            var localsensor = dss.find(sensorid)
            logger.debug("find Sensor id : ${localsensor} Localhost Sensor ===============>  ${localsensor}")
            if (localsensor != null) {
                //ถ้าเจอ Senosr localhost
                var v = io.readDs18(localsensor.name!!)
                logger.debug("Value from ${localsensor} ${v}")
                if (v != null) {
                    value = DS18value()
                    value.t = v
                }
            }
            else
            {


            }


            if (value == null)
                value = ss.readDsOther(desid!!, sensorid!!)

            logger.debug("Read value : ${value}")
            state = "Read value :${value}"
            if (value != null) {
                //ถ้ามีข้อมูล
                state = "Start run"

                var v = value.t?.toInt()
                var l = pijob.tlow?.toInt()
                var h = pijob.thigh?.toInt()

                if (v!! >= l!! && v!! <= h!!) {
                    //ถ้าข้อมูลอยู่ในช่วงที่กำหนด
                    state = "Have Job to run"
                    logger.debug("Value in range ${pijob.tlow} <= ${v} => ${pijob.thigh}")
                    state = "Value in range ${pijob.tlow} <= ${v} => ${pijob.thigh}"
                    if (startdate == null) {
                        startdate = Date()
                        timeout = pijob.waittime //
                        run = pijob.runtime //เวลาในการ run เอ็นวินาที
                        finishrun = DateTime().plusSeconds(pijob.runtime?.toInt()!!).toDate() //เวลาเสร็จ
                        next3 = DateTime().plusSeconds(pijob.runtime?.toInt()!! - 7200).toDate() //เวลาเสร็จ
                    }
                    if (!messageto) {
                        messageto = true
                        ms.message("Start Counter 90  Close : ${finishrun}", "info")
                        state = "Start Count end at ${finishrun} "
                    }

                    //runtime = Date()

                    var rt = DateTime()
                    var st = DateTime(startdate)

                    runtime = rt.toDate()
                    period = Interval(st, rt).toPeriod() //ช่วงเวลา

                    var r = Seconds.secondsBetween(st, rt)
                    var havetorun = run?.toInt()!!
                    completerun = r.seconds
                    if (r.seconds >= havetorun) {
                        logger.debug("End run counter job ID ***${pijob.id}***")
                        messageto = false
                        ms.message("Interrup Counter", "info")
                        isRun = false
                        state = "Counter is in interrup"
                        endcounter = true
                        break
                    }

                    //var c = rt.minus(startdate?.time!!) //เวลาที่ run ไปแล้ว


                    //var d = startdate?.time!! - runtime?.time!! / 1000 // เวลาที่ทำงานไปแล้ว เป็นวินาที

                } else {
                    logger.error("Value not in range range ${pijob.tlow} <= ${v} => ${pijob.thigh} ")
                    timeoutcount++
                    if (timeoutcount >= timeout?.toInt()!!) {
                        logger.error("Time out count exit ${timeoutcount}")
                        ms.message("Tmp is under rang exits count", "error")
                        state = "Value out of rang stop counter"
                        endcounter = false // เพราะนับไม่สุด
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


        if (endcounter) {

            runPorts(pijob)
        }


    }

    fun resetport(ports: List<Portstatusinjob>?) {

        if (ports != null)
            for (port in ports) {
                if (!port.status?.name.equals("check")) {
                    var pin = gpio.gpio.getProvisionedPin(port.portname?.name) as GpioPinDigitalOutput
                    Worker.logger.debug("Reset pin ${pin}")
                    gpio.resettoDefault(pin)
                    Worker.logger.debug("Reset Port to default")
                }
            }

    }

    fun setport(ports: List<Portstatusinjob>) {
        try {
            Worker.logger.debug("Gpio : ${gpio}")


            for (port in ports) {

                if (port.enable == null || port.enable == false || port.status?.name.equals("check")) {//ถ้า Enable == null หรือ false ให้ไปทำงาน port ต่อไปเลย
                    Worker.logger.error("Not set port ${port}")
                } else {
                    Worker.logger.debug("Port for pijob ${port}")
                    var pin = gpio.gpio?.getProvisionedPin(port.portname?.name) as GpioPinDigitalOutput
                    Worker.logger.debug("Pin: ${pin}")

                    //save old state
                    //  var b = Pinbackup(pin, pin.state)
                    //   pinbackuplist.add(b)

                    var sn = port.status?.name
                    Worker.logger.debug("Set to " + sn)
                    if (sn?.indexOf("low") != -1) {
                        gpio.setPort(pin, false)
                        //pin.setState(false)
                    } else
                    // pin.setState(true)
                        gpio.setPort(pin, true)

                    Worker.logger.debug("Set pin state: ${pin.state}")
                }
            }
        } catch (e: Exception) {
            Worker.logger.error("Set port ${e.message}")
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


            Worker.logger.debug("Run port is end")


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
                display.showMessage("Counter in ${completerun}  Next gas 3 ${df.format(next3)}" +
                        " Sec Close AT: ${df.format(finishrun)}  ")
                ms.message("Counter in ${completerun}  Next gas 3 ${df.format(next3)} " +
                        "Sec Close AT: ${df.format(finishrun)} ", "counterinfo")
                logger.debug("Display ok.")
            } catch (e: Exception) {
                logger.error("Error: ${e.message}")
            } finally {
                if (dps.lock)
                    dps.unlock(this)
            }


        }
    }

    companion object {
        internal var logger = LoggerFactory.getLogger(Workercounter::class.java)
    }
}