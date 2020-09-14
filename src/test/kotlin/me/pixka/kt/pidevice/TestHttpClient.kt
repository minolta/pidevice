package me.pixka.kt.pidevice

import org.junit.jupiter.api.Test
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL
import java.util.concurrent.CompletableFuture
import java.util.concurrent.Future
import java.util.function.Supplier


class TestHttpClient
{

    fun  get(): String {
        println("call")
        var url = URL("http://192.168.89.98")
        var c = url.openConnection() as HttpURLConnection
        c.requestMethod ="GET"
        c.connectTimeout = 2000
        val buf = BufferedReader(
                InputStreamReader(
                        c.getInputStream()))
        val response = StringBuilder()
        var inputLine: String? =""

        while (buf.readLine().also({ inputLine = it }) != null)
            response.append(inputLine)
        return response.toString()

    }


    @Test
    fun testHttpClient()
    {


    }

}