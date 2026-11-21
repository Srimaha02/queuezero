package com.queuezero.queuezero;

import com.queuezero.queuezero.QueueModels.*;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "*")
public class QueueController {

    private final QueueService queueService;

    public QueueController(QueueService queueService) {
        this.queueService = queueService;
    }

    @GetMapping("/state")
    public SystemSnapshot getState() {
        return queueService.getSnapshot("Connected to QueueZero Live Telemetry");
    }

    @PostMapping("/claim")
    public Token claimToken(@RequestBody(required = false) Map<String, Object> body) {
        String name = body != null && body.get("name") != null ? body.get("name").toString() : "";
        boolean priority = body != null && Boolean.TRUE.equals(body.get("priority"));
        return queueService.claimToken(name, priority);
    }

    @PostMapping("/serve/{counterId}")
    public void serveNext(@PathVariable int counterId) {
        queueService.serveNext(counterId);
    }

    @PostMapping("/reset")
    public void reset() {
        queueService.resetDemo();
    }
}