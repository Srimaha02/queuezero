package com.queuezero.queuezero;

import java.time.Instant;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;

public class QueueModels {

    public static class Token {
        public int tokenNumber;
        public String visitorName;
        public boolean priority;
        public int counterId;
        public long createdAtEpochMs;
        public long expiresAtEpochMs;
        public String status;

        public Token(int tokenNumber, String visitorName, boolean priority, int counterId) {
            this.tokenNumber = tokenNumber;
            this.visitorName = visitorName;
            this.priority = priority;
            this.counterId = counterId;
            this.createdAtEpochMs = Instant.now().toEpochMilli();
            this.expiresAtEpochMs = this.createdAtEpochMs + (3 * 60 * 1000L);
            this.status = "WAITING";
        }
    }

    public static class CounterState {
        public int id;
        public String name;
        public Token currentlyServing;
        public ConcurrentLinkedQueue<Token> waitingQueue = new ConcurrentLinkedQueue<>();
        public int servedCount = 0;

        public CounterState(int id, String name) {
            this.id = id;
            this.name = name;
        }

        public int getQueueSize() {
            return waitingQueue.size() + (currentlyServing != null ? 1 : 0);
        }
    }

    public static class SystemSnapshot {
        public List<CounterState> counters;
        public int totalIssued;
        public int totalServed;
        public int totalExpired;
        public String latestEventLog;
        public long serverTimestampMs;

        public SystemSnapshot(List<CounterState> counters, int totalIssued, int totalServed, int totalExpired, String latestEventLog) {
            this.counters = counters;
            this.totalIssued = totalIssued;
            this.totalServed = totalServed;
            this.totalExpired = totalExpired;
            this.latestEventLog = latestEventLog;
            this.serverTimestampMs = Instant.now().toEpochMilli();
        }
    }
}