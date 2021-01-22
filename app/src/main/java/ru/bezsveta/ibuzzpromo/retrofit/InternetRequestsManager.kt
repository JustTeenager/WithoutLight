package ru.bezsveta.ibuzzpromo.retrofit

import com.google.gson.GsonBuilder
import org.apache.http.NameValuePair
import org.apache.http.client.HttpClient
import org.apache.http.client.entity.UrlEncodedFormEntity
import org.apache.http.client.methods.HttpPost
import org.apache.http.conn.scheme.Scheme
import org.apache.http.conn.scheme.SchemeRegistry
import org.apache.http.conn.ssl.SSLSocketFactory
import org.apache.http.conn.ssl.X509HostnameVerifier
import org.apache.http.impl.client.DefaultHttpClient
import org.apache.http.impl.conn.SingleClientConnManager
import org.apache.http.message.BasicNameValuePair
import retrofit2.Call
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import ru.bezsveta.ibuzzpromo.model.BatteryStatus
import java.io.InputStream
import java.util.*
import javax.net.ssl.HostnameVerifier
import javax.net.ssl.HttpsURLConnection


class InternetRequestsManager {
    companion object {
        //private const val BASE_URL = "https://bez-sveta.ru/"
        private const val BASE_URL = "https://bez-sveta.ru/api/"
        private var client: HttpClient? = null
        private var retrofit:Retrofit?=null
    }

    fun sendBatteryDataViaRetrofit(batteryStatus: BatteryStatus): Call<Void> {
        return retrofit!!.create(StatusLightAPI::class.java).sendData(
            batteryStatus.isCharging,
            batteryStatus.code
        )
    }

    fun initClient(){
        val hostnameVerifier: HostnameVerifier = SSLSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER
        val clientTemp = DefaultHttpClient()
        val registry = SchemeRegistry()
        val socketFactory: SSLSocketFactory = SSLSocketFactory.getSocketFactory()
        socketFactory.hostnameVerifier = hostnameVerifier as X509HostnameVerifier
        registry.register(Scheme("https", socketFactory, 443))
        val mgr = SingleClientConnManager(clientTemp.params, registry)
        client = DefaultHttpClient(mgr, clientTemp.params)

        HttpsURLConnection.setDefaultHostnameVerifier(hostnameVerifier)
    }

    fun initRetrofit(){
        retrofit=Retrofit.Builder().baseUrl(BASE_URL).addConverterFactory(
            GsonConverterFactory.create(
                GsonBuilder()
                    .setLenient().create()
            )
        ).build()

    }

    fun sendBatteryDataViaHTTPClient(batteryStatus: BatteryStatus){
        val post = HttpPost(BASE_URL + "sendData/")
        val pairs: MutableList<NameValuePair> = ArrayList<NameValuePair>()
        pairs.add(BasicNameValuePair("charge_status", batteryStatus.isCharging.toString()))
        pairs.add(BasicNameValuePair("device_serial", batteryStatus.code))
        post.entity=UrlEncodedFormEntity(pairs)

        val response = client!!.execute(post)
        val instream: InputStream = response.entity.content
        instream.close()
    }
}