package me.pixka.kt.pidevice;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.concurrent.CompletableFuture;

public class TestAsy {

    public String f()
    {

        try {
            URL u = new URL("http://192.168.89.98");
            HttpURLConnection con = (HttpURLConnection) u.openConnection();
            con.setConnectTimeout(5000);
            con.setRequestMethod("GET");

        } catch (IOException e) {
            e.printStackTrace();
        }
        return "cccccc";
    }
    @Test
    public void test()
    {
        CompletableFuture.supplyAsync(()->f()).thenAccept(i->System.out.println(i));
    }
}
