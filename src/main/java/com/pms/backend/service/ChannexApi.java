package com.pms.backend.service;

import retrofit2.Call;
import retrofit2.http.*;
import java.util.Map;

public interface ChannexApi {
    @GET("hotels")
    Call<Map<String, Object>> getProperties(@Header("user-api-key") String apiKey);

    @POST("hotels")
    Call<Map<String, Object>> createProperty(@Header("user-api-key") String apiKey, @Body Map<String, Object> body);

    @POST("room_types")
    Call<Map<String, Object>> createRoomType(@Header("user-api-key") String apiKey, @Body Map<String, Object> body);

    @POST("rate_plans")
    Call<Map<String, Object>> createRatePlan(@Header("user-api-key") String apiKey, @Body Map<String, Object> body);

    @POST("ari")
    Call<Void> pushARI(@Header("user-api-key") String apiKey, @Body Map<String, Object> body);

    @GET("booking_revisions")
    Call<Map<String, Object>> getBookingRevisions(@Header("user-api-key") String apiKey);

    @POST("booking_revisions/{id}/ack")
    Call<Void> acknowledgeBookingRevision(@Header("user-api-key") String apiKey, @Path("id") String revisionId);
}
