package me.pixka.kt.pidevice.s

import org.apache.http.NameValuePair
import org.apache.http.client.methods.HttpPost
import org.apache.http.impl.client.HttpClientBuilder
import org.springframework.stereotype.Service
import org.apache.http.client.methods.CloseableHttpResponse
import org.apache.http.client.entity.UrlEncodedFormEntity
import org.apache.http.message.BasicNameValuePair
import java.util.ArrayList
import org.apache.http.impl.client.HttpClients
import org.apache.http.impl.client.CloseableHttpClient



@Service
class NotifyService()
{

      var token = "agyv6NiReQGXMrTsTZPbHTmqj5EASjJM59lqit3lUlo"

     fun message(mes:String)
     {
         val client = HttpClients.createDefault()
         val httpPost = HttpPost("https://notify-api.line.me/api/notify")

         val params = ArrayList<NameValuePair>()
         params.add(BasicNameValuePair("message", mes))
         params.add(BasicNameValuePair("password", "pass"))
         httpPost.entity = UrlEncodedFormEntity(params)
         httpPost.addHeader("Content-type", "application/x-www-form-urlencoded")
         httpPost.addHeader("Authorization","Bearer ${token}")
         httpPost.addHeader("charset","UTF-8")

         val response = client.execute(httpPost)
         client.close()
     }


}