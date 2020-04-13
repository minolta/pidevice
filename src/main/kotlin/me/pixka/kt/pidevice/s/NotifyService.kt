package me.pixka.kt.pidevice.s

import org.apache.http.NameValuePair
import org.apache.http.client.entity.UrlEncodedFormEntity
import org.apache.http.client.methods.HttpPost
import org.apache.http.impl.client.HttpClients
import org.apache.http.message.BasicNameValuePair
import org.springframework.stereotype.Service
import java.util.*


@Service
class NotifyService() {

    var token = "agyv6NiReQGXMrTsTZPbHTmqj5EASjJM59lqit3lUlo"

    fun message(mes: String) {
        val client = HttpClients.createDefault()
        val httpPost = HttpPost("https://notify-api.line.me/api/notify")

        val params = ArrayList<NameValuePair>()
        params.add(BasicNameValuePair("message", mes))
        params.add(BasicNameValuePair("password", "pass"))
        httpPost.entity = UrlEncodedFormEntity(params, "UTF-8")
        httpPost.addHeader("Content-type", "application/x-www-form-urlencoded; charset=UTF-8")
        httpPost.setHeader("Content-Type", "application/x-www-form-urlencoded; charset=UTF-8");
        httpPost.addHeader("Authorization", "Bearer ${token}")
        httpPost.addHeader("charset", "UTF-8")

        val response = client.execute(httpPost)
        client.close()
    }

    fun error(mes: String) {
        var tk = System.getProperty("errortoken")
        if(tk==null)
            throw Exception("eRROR Token not found")
        val client = HttpClients.createDefault()
        val httpPost = HttpPost("https://notify-api.line.me/api/notify")

        val params = ArrayList<NameValuePair>()
        params.add(BasicNameValuePair("message", mes))
        params.add(BasicNameValuePair("password", "pass"))
        httpPost.entity = UrlEncodedFormEntity(params, "UTF-8")
        httpPost.addHeader("Content-type", "application/x-www-form-urlencoded; charset=UTF-8")
        httpPost.setHeader("Content-Type", "application/x-www-form-urlencoded; charset=UTF-8");

        if (tk != null)
            httpPost.addHeader("Authorization", "Bearer ${tk}")

        httpPost.addHeader("charset", "UTF-8")

        val response = client.execute(httpPost)
        client.close()
    }
    fun message(mes: String, tokenii: String) {
        var tk = System.getProperty("linetoken")
        val client = HttpClients.createDefault()
        val httpPost = HttpPost("https://notify-api.line.me/api/notify")

        val params = ArrayList<NameValuePair>()
        params.add(BasicNameValuePair("message", mes))
        params.add(BasicNameValuePair("password", "pass"))
        httpPost.entity = UrlEncodedFormEntity(params, "UTF-8")
        httpPost.addHeader("Content-type", "application/x-www-form-urlencoded; charset=UTF-8")
        httpPost.setHeader("Content-Type", "application/x-www-form-urlencoded; charset=UTF-8");

        if (tk != null)
            httpPost.addHeader("Authorization", "Bearer ${tk}")
        else
            httpPost.addHeader("Authorization", "Bearer ${tokenii}")
        httpPost.addHeader("charset", "UTF-8")

        val response = client.execute(httpPost)
        client.close()
    }

}