package me.pixka.kt.pidevice.t

import org.junit.jupiter.api.Test
import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse

class Testopenpump2 {

    @Test
    fun run()
    {
        val client: HttpClient = HttpClient.newHttpClient()
        val request: HttpRequest = HttpRequest.newBuilder()
            .uri(URI.create("http://192.168.88.40/run?delay=120"))
            .GET() // GET is default
            .build()

        val response: HttpResponse<Void> = client.send(
            request,
            HttpResponse.BodyHandlers.discarding()
        )

        System.out.println(response.statusCode())
    }
}