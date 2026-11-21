package com.queuezero.queuezero;

import com.queuezero.queuezero.QueueModels.*;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

@Service
public class QueueService {

    private final SimpMessagingTemplate messagingTemplate;
    private final AtomicInteger tokenGenerator = new AtomicInteger(1000);
    private final AtomicInteger totalServed = new AtomicInteger(0);
    private final AtomicInteger totalExpired = new AtomicInteger(0);
    private final Map<Integer, CounterState> counters = new ConcurrentHashMap<>();

    public QueueService(SimpMessagingTemplate messagingTemplate) {
        this.messagingTemplate = messagingTemplate;
        counters.put(1, new CounterState(1, "Counter 01 • Express & General"));
        counters.put(2, new CounterState(2, "Counter 02 • General Service"));
        counters.put(3, new CounterState(3, "Counter 03 • Priority & FastTrack"));
    }

    public synchronized Token claimToken(String visitorName, boolean priority) {
        int nextId = tokenGenerator.incrementAndGet();

        CounterState targetCounter = counters.values().stream()
                .min(Comparator.comparingInt(c -> {
                    int load = c.getQueueSize();
                    if (priority && c.id == 3) load -= 2;
                    return load;
                }))
                .orElse(counters.get(1));

        String cleanName = (visitorName == null || visitorName.isBlank()) ? "Guest-" + nextId : visitorName.trim();
        Token token = new Token(nextId, cleanName, priority, targetCounter.id);

        if (targetCounter.currentlyServing == null) {
            token.status = "SERVING";
            targetCounter.currentlyServing = token;
        } else {
            targetCounter.waitingQueue.add(token);
        }

        String log = String.format("Token #%d (%s) → Assigned Counter 0%d [Load: %d]",
                token.tokenNumber, priority ? "PRIORITY" : "STD", targetCounter.id, targetCounter.getQueueSize());
        broadcastState(log);
        return token;
    }

    public synchronized void serveNext(int counterId) {
        CounterState counter = counters.get(counterId);
        if (counter == null) return;

        if (counter.currentlyServing != null) {
            counter.servedCount++;
            totalServed.incrementAndGet();
        }

        Token next = counter.waitingQueue.poll();
        if (next != null) {
            next.status = "SERVING";
            counter.currentlyServing = next;
            broadcastState(String.format("Counter 0%d → Now Serving Token #%d", counterId, next.tokenNumber));
        } else {
            counter.currentlyServing = null;
            broadcastState(String.format("Counter 0%d → Queue Cleared (Idle)", counterId));
        }
    }

    @Scheduled(fixedRate = 10000)
    public synchronized void purgeExpiredNoShows() {
        long now = Instant.now().toEpochMilli();
        int expiredInCycle = 0;

        for (CounterState counter : counters.values()) {
            Iterator<Token> it = counter.waitingQueue.iterator();
            while (it.hasNext()) {
                Token t = it.next();
                if (now > t.expiresAtEpochMs) {
                    it.remove();
                    expiredInCycle++;
                    totalExpired.incrementAndGet();
                }
            }
        }

        if (expiredInCycle > 0) {
            broadcastState(String.format("Auto-Expiry Engine → Cleared %d no-show token(s) (>3m limit)", expiredInCycle));
        }
    }

    public synchronized void resetDemo() {
        counters.values().forEach(c -> {
            c.currentlyServing = null;
            c.waitingQueue.clear();
            c.servedCount = 0;
        });
        tokenGenerator.set(1000);
        totalServed.set(0);
        totalExpired.set(0);
        broadcastState("System Reset → All counters balanced to 0");
    }

    public SystemSnapshot getSnapshot(String eventLog) {
        return new SystemSnapshot(
                new ArrayList<>(counters.values()),
                tokenGenerator.get() - 1000,
                totalServed.get(),
                totalExpired.get(),
                eventLog
        );
    }

    private void broadcastState(String eventLog) {
        messagingTemplate.convertAndSend("/topic/queue-updates", getSnapshot(eventLog));
    }
}