# QueueZero -- Real-Time Microservices-Driven Virtual Queue & Crowd Load-Balancing Engine

> **Built for INNOVATE Hackathon 2026** | Track: Web & Mobile Development  
> **Lead Developer:** Srimahalakshmi R ([@Srimaha02](https://github.com/Srimaha02))

---

## Overview

**QueueZero** is a high-throughput web infrastructure engineered to eliminate physical standing lines, balance service counter workloads dynamically, and stream sub-second queue position updates across high-density venues such as hospitals, banks, government offices, and large campus events.

Traditional manual queues suffer from severe crowd congestion, unbalanced counter loads (where one counter has 50+ waiting visitors while adjacent counters sit idle), and zero live visibility—resulting in 45–60 minutes of physical wait time per visitor. QueueZero replaces static physical lines with instant QR-driven virtual tokens, smart load-balanced counter routing, and persistent WebSocket telemetry.

---

## Key Features

- **Instant QR Virtual Token Issuance:** Visitors scan an on-screen dynamic QR code or enter their details on the kiosk to claim a mobile digital boarding pass—cutting physical standing wait times down to `0 minutes`.
- **Smart Crowd Load Balancer:** Dynamically evaluates active queue depth across all service counters in real time and routes incoming tokens to the least-congested counter (with dedicated fast-track handling for priority/accessibility passes).
- **Sub-Second WebSocket Synchronization (`<100ms`):** Employs **STOMP over SockJS** persistent event channels (`/topic/queue-updates`) to broadcast live counter states, position updates, and system telemetry without HTTP polling overhead.
- **High-Throughput Thread-Safe Concurrency:** Uses Java `AtomicInteger` token generators and synchronized state transitions to guarantee zero duplicate token assignments during sudden traffic spikes.
- **Automated 3-Minute No-Show Cleanup:** Runs a background `@Scheduled` executor every 10 seconds to automatically purge unredeemed tokens past their 3-minute window, preventing counter idle time.
- **Python Concurrent Crowd Simulator (`simulator.py`):** Multi-threaded Python load-injection engine using `ThreadPoolExecutor` to simulate high-density concurrent visitor spikes and validate load-balancing latency.

---

## System Architecture

```text
┌─────────────────────────┐      HTTP POST /api/claim       ┌──────────────────────────────────┐
│   Visitor Kiosk / UI    │ ──────────────────────────────► │      Spring Boot 3 Core          │
│ (Tailwind + STOMP/SockJS)│ ◄────────────────────────────── │  • AtomicInteger Token Engine    │
└─────────────────────────┘   WebSocket /topic/queue-updates│  • Least-Congested Load Balancer │
                                                            │  • @Scheduled Auto-Expiry (3m)   │
┌─────────────────────────┐      Concurrent Load Spike      └──────────────────────────────────┘
│ Python Crowd Simulator  │ ──────────────────────────────►                 │
│ (ThreadPoolExecutor)    │                                                 ▼
└─────────────────────────┘                                 ┌──────────────────────────────────┐
                                                            │   Docker Container (Cloud)       │
                                                            └──────────────────────────────────┘



## Technology Stack

| Layer | Technologies Used | Purpose |
| :--- | :--- | :--- |
| **Backend Core** | **Java 21**, **Spring Boot 3** (Spring Web, Scheduled Executors) | High-throughput REST APIs, thread-safe token sequencing (`AtomicInteger`), least-congested counter routing, and automated 3-minute no-show cleanup |
| **Real-Time Streaming** | **WebSockets (STOMP & SockJS)** | Persistent bi-directional event channels (`/topic/queue-updates`) for sub-second (`<100ms`) live queue updates without HTTP polling |
| **Frontend Dashboard** | **HTML5**, **Tailwind CSS**, **JavaScript (ES6)**, **QRCode.js** | Responsive Visitor Kiosk, dynamic QR generator, digital boarding pass with live countdown, and Counter Operator console |
| **Load & Traffic Simulator** | **Python 3.x** (`concurrent.futures`, `requests`) | Multi-threaded concurrent crowd injection engine (`simulator.py`) for stress-testing load balancing under traffic spikes |
| **DevOps & Cloud Hosting** | **Docker** (Multi-stage Maven + Eclipse Temurin 21 Alpine), **Render** | Containerized microservice packaging and live cloud deployment |
