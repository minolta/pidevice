package me.pixka.ktbase.io

import me.pixka.kt.base.s.ErrorlogService
import org.springframework.stereotype.Component
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.IOException
import java.nio.file.Files
import java.nio.file.Paths
import java.security.MessageDigest
import java.security.NoSuchAlgorithmException
import java.util.*
import javax.xml.bind.DatatypeConverter


@Component
class Configfilekt (val es:ErrorlogService){

    var configfile = "config.properties"

    @Throws(IOException::class)
    fun load(file: String): Properties {
        val input = FileInputStream(File(file))
        val prop = Properties()
        prop.load(input)
        return prop
    }

    @Throws(IOException::class)
    fun save(p: Properties, tofile: String) {
        val fo = FileOutputStream(File(tofile))
        p.store(fo, "save")
        fo.close()
    }

    @Throws(NoSuchAlgorithmException::class, IOException::class)
    fun md5(file: String): String {

        val md = MessageDigest.getInstance("MD5")
        md.update(Files.readAllBytes(Paths.get(file)))
        val digest = md.digest()
        // String s = Arrays.toString(digest);
        val s = DatatypeConverter.printHexBinary(digest).toUpperCase()
        return s
    }

    fun getPropertie(name: String): String? {

        val p: Properties
        try {
            p = load(configfile)
            return p.getProperty(name)
        } catch (e: IOException) {
            println(e.message)
            es.n("configfile","56","${e.message}")
        }

        return null
    }

    fun getPropertie(name: String, defaultvalue: String): String {

        val p: Properties
        try {
            p = load(configfile)
            return p.getProperty(name)
        } catch (e: IOException) {
            println(e.message)
            es.n("configfile","70","${e.message}")
        }

        return defaultvalue
    }

}
