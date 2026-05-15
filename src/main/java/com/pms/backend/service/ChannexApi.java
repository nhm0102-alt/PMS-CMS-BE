package com.pms.backend.service;

import retrofit2.Call;
import retrofit2.http.*;
import java.util.Map;

public interface ChannexApi {
    @GET("properties")
    Call<Map<String, Object>> getProperties();

    @POST("properties")
    Call<Map<String, Object>> createProperty(@Body Map<String, Object> body);

    @POST("room_types")
    Call<Map<String, Object>> createRoomType(@Body Map<String, Object> body);

    @POST("rate_plans")
    Call<Map<String, Object>> createRatePlan(@Body Map<String, Object> body);

    @POST("ari")
    Call<Void> pushARI(@Body Map<String, Object> body);

    @GET("booking_revisions")
    Call<Map<String, Object>> getBookingRevisions();

    @POST("booking_revisions/{id}/ack")
    Call<Void> acknowledgeBookingRevision(@Path("id") String revisionId);
}
