package practice.samay.ordermanagementsystem.service;

import practice.samay.ordermanagementsystem.dto.request.TrackingRequest;
import practice.samay.ordermanagementsystem.dto.response.TrackingResponse;

import java.util.List;

/**
 * Service interface for Tracking event business operations.
 */
public interface TrackingService {

    TrackingResponse addTrackingEvent(TrackingRequest request);

    TrackingResponse getTrackingById(Long id);

    List<TrackingResponse> getTrackingByShipmentId(Long shipmentId);
}
