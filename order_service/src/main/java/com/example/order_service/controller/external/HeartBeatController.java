package com.example.order_service.controller.external;

import com.example.order_service.service.HeartBeatService;
import com.example.order_service.utils.Response;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/heartbeat")
public class HeartBeatController {
    private final HeartBeatService heartBeatService;

    @PostMapping("/{userId}/{salesItemId}")
    public Response<Void> heartbeatTest(@PathVariable("userId") Long userId, @PathVariable("salesItemId") Long salesItemId) {
        heartBeatService.subscribeHeartbeat(userId, salesItemId);
        return Response.success();
    }

}
