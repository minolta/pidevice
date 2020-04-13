package me.pixka.kt.base.io

import java.io.IOException
import java.io.OutputStream
import java.io.OutputStreamWriter
import java.net.Socket
import java.text.SimpleDateFormat


class EscPosWebPrinter {
    var sdf = SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
    var socketOut: OutputStream? = null
    var writer: OutputStreamWriter? = null
    var name: String? = null
    var id: Long? = null

    @Throws(IOException::class)
    constructor(printerIp: String, port: Int) {

        val socket = Socket(printerIp, port)
        socket.setSoTimeout(1000)
        socketOut = socket.getOutputStream()
        //writer = OutputStreamWriter(socketOut, "UTF8")
        //writer = OutputStreamWriter(socketOut)
        writer = OutputStreamWriter(socketOut, "CP874")
        socketOut?.write(27)
        socketOut?.write(27)

    }


    /**
     * resets all writer settings to default

     */
    @Throws(IOException::class)
    fun resetToDefault() {
        setInverse(false)
        setBold(false)
        setFontDefault()
        setUnderline(0)
        setJustification(0)

    }

    /**
     * Sets bold

     */
    @Throws(IOException::class)
    fun setBold(bool: Boolean?) {
        writer?.write(0x1B)
        writer?.write("E")
        writer?.write((if (bool!!) 1 else 0).toInt())

    }

    /**

     * @throws IOException
     */
    @Throws(IOException::class)
    fun setFontZoomIn() {
        /* �������򶼷Ŵ�һ�� */
        writer?.write(0x1c)
        writer?.write(0x21)
        writer?.write(12)
        writer?.write(0x1b)
        writer?.write(0x21)
        writer?.write(12)

    }

    /**

     * @throws IOException
     */
    @Throws(IOException::class)
    fun setFontZoomInWidth() {
        /* ����Ŵ�һ�� */
        writer?.write(0x1c)
        writer?.write(0x21)
        writer?.write(4)
        writer?.write(0x1b)
        writer?.write(0x21)
        writer?.write(4)

    }

    /**

     * @throws IOException
     */
    @Throws(IOException::class)
    fun setFontZoomInHeight() {
        /* ����Ŵ�һ�� */
        writer?.write(0x1c)
        writer?.write(0x21)
        writer?.write(8)
        writer?.write(0x1b)
        writer?.write(0x21)
        writer?.write(8)

    }

    /**

     * @throws IOException
     */
    @Throws(IOException::class)
    fun setFontDefault() {

        writer?.write(0x1c)
        writer?.write(0x21)
        writer?.write(1)

    }

    /**
     * Sets white on black printing

     */
    @Throws(IOException::class)
    fun setInverse(bool: Boolean?) {
        writer?.write(0x1D)
        writer?.write("B")
        writer?.write((if (bool!!) 1 else 0).toInt())

    }

    /**
     * Sets underline and weight

     * @param val
     * *            0 = no underline. 1 = single weight underline. 2 = double
     * *            weight underline.
     */
    @Throws(IOException::class)
    fun setUnderline(`val`: Int) {
        writer?.write(0x1B)
        writer?.write("-")
        writer?.write(`val`)

    }

    /**
     * Sets left, center, right justification

     * @param val
     * *            0 = left justify. 1 = center justify. 2 = right justify.
     */
    @Throws(IOException::class)
    fun setJustification(`val`: Int) {
        writer?.write(0x1B)
        writer?.write("a")
        writer?.write(`val`)

    }

    /**

     * @param str
     * *
     * @throws IOException
     */
    @Throws(IOException::class)
    fun printStr(str: String) {
        writer?.write(str)

    }

    /**

     * @param str
     * *
     * @throws IOException
     */
    @Throws(IOException::class)
    fun printlnStr(str: String) {
        writer?.write(str + "\n")

    }

    /**
     * print String value of obj

     * @param obj
     * *
     * @throws IOException
     */
    @Throws(IOException::class)
    fun printObj(obj: Any) {
        writer?.write(obj.toString())

    }

    @Throws(IOException::class)
    fun printlnObj(obj: Any) {
        writer?.write(obj.toString() + "\n")

    }

    /**
     * print String value of element, and separator fill the gap between
     * elements

     * @param lst
     * *
     * @param separator
     * *            for example, use "\n" to break line
     * *
     * @throws IOException
     */
    @Throws(IOException::class)
    fun printLst(lst: List<*>, separator: String) {
        for (o in lst) {
            this.printObj(o!!)
            this.printStr(separator)

        }
    }

    /**
     * print String value of element, and separator fill the gap between
     * elements

     * @param map
     * *
     * @param kvSeparator
     * *
     * @param itemSeparator
     * *
     * @throws IOException
     */
    @Throws(IOException::class)
    fun printMap(map: Map<*, *>, kvSeparator: String, itemSeparator: String) {
        for (key in map.keys) {
            this.printObj(key!!)
            this.printStr(kvSeparator)
            this.printObj(map.get(key)!!)
            this.printStr(itemSeparator)

        }

    }

    /**
     * Encode and print QR code

     * @param str
     * *            String to be encoded in QR.
     * *
     * @param errCorrection
     * *            The degree of error correction. (48 <= n <= 51) 48 = level L /
     * *            7% recovery capacity. 49 = level M / 15% recovery capacity. 50
     * *            = level Q / 25% recovery capacity. 51 = level H / 30% recovery
     * *            capacity.
     * *
     * *
     * @param moduleSize
     * *            The size of the QR module (pixel) in dots. The QR code will
     * *            not print if it is too big. Try setting this low and
     * *            experiment in making it larger.
     */
    @Throws(IOException::class)
    fun printQR(str: String, errCorrect: Int, moduleSize: Int) {
        // save data function 80
        writer?.write(0x1D)// init
        writer?.write("(k")// adjust height of barcode
        writer?.write(str.length + 3) // pl
        writer?.write(0) // ph
        writer?.write(49) // cn
        writer?.write(80) // fn
        writer?.write(48) //
        writer?.write(str)

        // error correction function 69
        writer?.write(0x1D)
        writer?.write("(k")
        writer?.write(3) // pl
        writer?.write(0) // ph
        writer?.write(49) // cn
        writer?.write(69) // fn
        writer?.write(errCorrect) // 48<= n <= 51

        // size function 67
        writer?.write(0x1D)
        writer?.write("(k")
        writer?.write(3)
        writer?.write(0)
        writer?.write(49)
        writer?.write(67)
        writer?.write(moduleSize)// 1<= n <= 16

        // print function 81
        writer?.write(0x1D)
        writer?.write("(k")
        writer?.write(3) // pl
        writer?.write(0) // ph
        writer?.write(49) // cn
        writer?.write(81) // fn
        writer?.write(48) // m


    }

    /**
     * Encode and print barcode

     * @param code
     * *            String to be encoded in the barcode. Different barcodes have
     * *            different requirements on the length of data that can be
     * *            encoded.
     * *
     * @param type
     * *            Specify the type of barcode 65 = UPC-A. 66 = UPC-E. 67 =
     * *            JAN13(EAN). 68 = JAN8(EAN). 69 = CODE39. 70 = ITF. 71 =
     * *            CODABAR. 72 = CODE93. 73 = CODE128.
     * *
     * *
     * @param h
     * *            height of the barcode in points (1 <= n <= 255) @ param w
     * *            width of module (2 <= n <=6). Barcode will not print if this
     * *            value is too large. @param font Set font of HRI characters 0 =
     * *            font A 1 = font B
     * *
     * @param pos
     * *            set position of HRI characters 0 = not printed. 1 = Above
     * *            barcode. 2 = Below barcode. 3 = Both abo ve and below barcode.
     */
    @Throws(IOException::class)
    fun printBarcode(code: String, type: Int, h: Int, w: Int, font: Int,
                     pos: Int) {

        // need to test for errors in length of code
        // also control for input type=0-6
        // GS H = HRI position
        writer?.write(0x1D)
        writer?.write("H")
        writer?.write(pos) // 0=no print, 1=above, 2=below, 3=above & below

        // GS f = set barcode characters
        writer?.write(0x1D)
        writer?.write("f")
        writer?.write(font)

        // GS h = sets barcode height
        writer?.write(0x1D)
        writer?.write("h")
        writer?.write(h)

        // GS w = sets barcode width
        writer?.write(0x1D)
        writer?.write("w")
        writer?.write(w)// module = 1-6

        // GS k
        writer?.write(0x1D) // GS
        writer?.write("k") // k
        writer?.write(type)// m = barcode type 0-6
        writer?.write(code.length) // length of encoded string
        writer?.write(code)// d1-dk
        writer?.write(0)// print barcode


    }

    /**
     * Encode and print PDF 417 barcode

     * @param code
     * *            String to be encoded in the barcode. Different barcodes have
     * *            different requirements on the length of data that can be
     * *            encoded.
     * *
     * @param type
     * *            Specify the type of barcode 0 - Standard PDF417 1 - Standard
     * *            PDF417
     * *
     * *
     * @param h
     * *            Height of the vertical module in dots 2 <= n <= 8. @ param w
     * *            Height of the horizontal module in dots 1 <= n <= 4. @ param
     * *            cols Number of columns 0 <= n <= 30. @ param rows Number of
     * *            rows 0 (automatic), 3 <= n <= 90. @ param error set error
     * *            correction level 48 <= n <= 56 (0 - 8).
     */
    @Throws(IOException::class)
    fun printPSDCode(code: String, type: Int, h: Int, w: Int, cols: Int,
                     rows: Int, error: Int) {

        // print function 82
        writer?.write(0x1D)
        writer?.write("(k")
        writer?.write(code.length) // pl Code length
        writer?.write(0) // ph
        writer?.write(48) // cn
        writer?.write(80) // fn
        writer?.write(48) // m
        writer?.write(code) // data to be encoded

        // function 65 specifies the number of columns
        writer?.write(0x1D)// init
        writer?.write("(k")// adjust height of barcode
        writer?.write(3) // pl
        writer?.write(0) // pH
        writer?.write(48) // cn
        writer?.write(65) // fn
        writer?.write(cols)

        // function 66 number of rows
        writer?.write(0x1D)// init
        writer?.write("(k")// adjust height of barcode
        writer?.write(3) // pl
        writer?.write(0) // pH
        writer?.write(48) // cn
        writer?.write(66) // fn
        writer?.write(rows) // num rows

        // module width function 67
        writer?.write(0x1D)
        writer?.write("(k")
        writer?.write(3)// pL
        writer?.write(0)// pH
        writer?.write(48)// cn
        writer?.write(67)// fn
        writer?.write(w)// size of module 1<= n <= 4

        // module height fx 68
        writer?.write(0x1D)
        writer?.write("(k")
        writer?.write(3)// pL
        writer?.write(0)// pH
        writer?.write(48)// cn
        writer?.write(68)// fn
        writer?.write(h)// size of module 2 <= n <= 8

        // error correction function 69
        writer?.write(0x1D)
        writer?.write("(k")
        writer?.write(4)// pL
        writer?.write(0)// pH
        writer?.write(48)// cn
        writer?.write(69)// fn
        writer?.write(48)// m
        writer?.write(error)// error correction

        // choose pdf417 type function 70
        writer?.write(0x1D)
        writer?.write("(k")
        writer?.write(3)// pL
        writer?.write(0)// pH
        writer?.write(48)// cn
        writer?.write(70)// fn
        writer?.write(type)// set mode of pdf 0 or 1

        // print function 81
        writer?.write(0x1D)
        writer?.write("(k")
        writer?.write(3) // pl
        writer?.write(0) // ph
        writer?.write(48) // cn
        writer?.write(81) // fn
        writer?.write(48) // m


    }

    /**
     * send command to open cashbox through printer

     * @throws IOException
     */
    @Throws(IOException::class)
    fun openCashbox() {
        writer?.write(27)
        writer?.write(112)
        writer?.write(0)
        writer?.write(10)
        writer?.write(10)


    }

    /**
     * To control the printer performing paper feed and cut paper finally

     * @throws IOException
     */
    @Throws(IOException::class)
    fun feedAndCut() {
        feed()
        cut()


    }

    /**
     * To control the printer performing paper feed

     * @throws IOException
     */
    @Throws(IOException::class)
    fun feed() {
        // ����ָ��Ϊ��ӡ��ɺ��Զ���ֽ
        writer?.write(27)
        writer?.write(100)
        writer?.write(4)
        writer?.write(10)


    }

    /**
     * Cut paper, functionality whether work depends on printer hardware

     * @throws IOException
     */
    @Throws(IOException::class)
    fun cut() {
        // cut
        writer?.write(0x1D)
        writer?.write("V")
        writer?.write(48)
        writer?.write(0)


    }

    fun flush() {
        writer?.flush()
    }

    fun setFontsize(size: Int) {
        writer?.write(0x1d);
        writer?.write(0x21);
        writer?.write(size);
    }

    /**
     * at the end, close writer and socketOut

     */
    fun closeConn() {

        try {
            writer?.close()
            socketOut?.close()
        } catch (ex: IOException) {
            ex.printStackTrace()
        }

    }


}