package com.pms.backend.api.service;

import java.util.Map;

public interface RoomAllotmentService {
    void updateAllotmentFromReservation(Map<String, Object> reservationData, boolean isCancellation) throws Exception;
}
