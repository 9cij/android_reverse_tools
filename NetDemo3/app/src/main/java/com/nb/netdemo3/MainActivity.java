package com.nb.netdemo3;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.SecureRandom;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.security.interfaces.RSAPublicKey;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import okhttp3.Call;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class MainActivity extends AppCompatActivity {
    private Button btn1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        btn1 = findViewById(R.id.btn1);
        btn1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                doRequest();
            }
        });
    }


    private void doRequest() {
        X509TrustManager trustManager = new X509TrustManager() {
            @Override
            public void checkClientTrusted(X509Certificate[] chain, String authType) throws CertificateException {

            }

            @Override
            public void checkServerTrusted(X509Certificate[] chain, String authType) throws CertificateException {

                // 服务器返回的证书
                X509Certificate cf = chain[0];
                RSAPublicKey pubkey = (RSAPublicKey) cf.getPublicKey();
                String encoded = Base64.encodeToString(pubkey.getEncoded(), 0);
                Log.e("服务器返回证书：", encoded);

                // 读取客户端预设的证书
                InputStream client_input = getResources().openRawResource(R.raw.baidu);
                CertificateFactory finalcf = CertificateFactory.getInstance("X.509");
                X509Certificate realCertificate = (X509Certificate) finalcf.generateCertificate(client_input);
                String realPubKey = Base64.encodeToString(realCertificate.getPublicKey().getEncoded(), 0);
                Log.e("客户端预设证书：", realPubKey);

                cf.checkValidity();

                final boolean expected = realPubKey.equalsIgnoreCase(encoded);
                Log.e("eq=", String.valueOf(expected));
                if (!expected) {
                    throw new CertificateException("证书不一致");
                }

            }

            @Override
            public X509Certificate[] getAcceptedIssuers() {
                return new X509Certificate[0];
            }
        };


        SSLSocketFactory factory = null;

        try {
            SSLContext sslContext = SSLContext.getInstance("SSL");
            sslContext.init(null, new TrustManager[]{trustManager}, new SecureRandom());

            factory = sslContext.getSocketFactory();
        } catch (Exception e) {

        }

        SSLSocketFactory finalFactory = factory;
        new Thread() {
            @Override
            public void run() {
                try {
                    OkHttpClient client = new OkHttpClient.Builder().sslSocketFactory(finalFactory, trustManager).build();
                    Request req = new Request.Builder().url("https://www.baidu.com/?q=defaultCerts").build();
                    Call call = client.newCall(req);

                    Response res = call.execute();
                    Log.e("请求发送成功", "状态码：" + res.code());

                } catch (IOException ex) {
                    Log.e("Main", "网络请求异常" + ex);
                }
            }
        }.start();
    }
}