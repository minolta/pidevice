package me.pixka.kt.pidevice.t

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import me.pixka.kt.pibase.d.PiDevice
import org.junit.jupiter.api.Test
import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse
import java.net.http.HttpResponse.BodyHandlers




class TestGetDevices {
    val om = ObjectMapper()
    @Test
    fun testGetDevice()
    {
//        val request: HttpRequest = HttpRequest.newBuilder()
//            .uri(URI("http://192.168.88.21:3333/devices"))
//            .GET()
//            .build()
//
//        val response: HttpResponse<String> = HttpClient.newBuilder()
//            .build()
//            .send(request, BodyHandlers.ofString())
//
//        println(response.body())
//        var list = om.readValue<List<PiDevice>>(response.body())
//
//        list.forEach{
//            println(it)
//        }
    }
}