package com.pms.backend.service;

import com.pms.backend.config.AppProperties;
import okhttp3.ConnectionPool;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.logging.HttpLoggingInterceptor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import retrofit2.Retrofit;
import retrofit2.converter.jackson.JacksonConverterFactory;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Service
public class ChannexClient {
    private static final Logger log = LoggerFactory.getLogger(ChannexClient.class);
    private final ChannexApi channexApi;
    private final AppProperties appProperties;

    public ChannexClient(AppProperties appProperties) {
        this.appProperties = appProperties;

        HttpLoggingInterceptor loggingInterceptor = new HttpLoggingInterceptor(message -> log.info(message));
        loggingInterceptor.setLevel(HttpLoggingInterceptor.Level.BODY);

        ConnectionPool connectionPool = new ConnectionPool(10, 5, TimeUnit.MINUTES);

        OkHttpClient okHttpClient = new OkHttpClient.Builder()
                .connectionPool(connectionPool)
                .connectTimeout(10, TimeUnit.SECONDS)
                .readTimeout(30, TimeUnit.SECONDS)
                .addInterceptor(loggingInterceptor)
                .addInterceptor(chain -> {
                    Request original = chain.request();
                    Request request = original.newBuilder()
                            .header("user-api-key", appProperties.channex().apiKey())
                            .header("Content-Type", "application/json")
                            .header("Accept", "application/json")
                            .method(original.method(), original.body())
                            .build();
                    return chain.proceed(request);
                })
                .build();

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(appProperties.channex().baseUrl().endsWith("/") ? 
                         appProperties.channex().baseUrl() : 
                         appProperties.channex().baseUrl() + "/")
                .client(okHttpClient)
                .addConverterFactory(JacksonConverterFactory.create())
                .build();

        this.channexApi = retrofit.create(ChannexApi.class);
    }

    public Map<String, Object> getProperties() throws IOException {
        return channexApi.getProperties().execute().body();
    }

    public Map<String, Object> createProperty(Map<String, Object> propertyData) throws IOException {
        return channexApi.createProperty(Map.of("property", propertyData)).execute().body();
    }

    public Map<String, Object> createRoomType(Map<String, Object> roomTypeData) throws IOException {
        return channexApi.createRoomType(Map.of("room_type", roomTypeData)).execute().body();
    }

    public Map<String, Object> createRatePlan(Map<String, Object> ratePlanData) throws IOException {
        return channexApi.createRatePlan(Map.of("rate_plan", ratePlanData)).execute().body();
    }

    public void pushARI(Map<String, Object> ariData) throws IOException {
        channexApi.pushARI(ariData).execute();
    }

    public Map<String, Object> getBookingRevisions() throws IOException {
        return channexApi.getBookingRevisions().execute().body();
    }

    public void acknowledgeBookingRevision(String revisionId) throws IOException {
        channexApi.acknowledgeBookingRevision(revisionId).execute();
    }
}
