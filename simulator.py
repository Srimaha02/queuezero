import concurrent.futures
import requests
import time
import random

BASE_URL = "http://localhost:8080/api/claim"
TOTAL_VISITORS = 30

NAMES = ["Aarav", "Diya", "Karthik", "Meera", "Vikram", "Ananya", "Rohan", "Priya", "Siddharth", "Kavya"]

def simulate_visitor(i):
    payload = {
        "name": f"{random.choice(NAMES)} #{i+1}",
        "priority": (i % 7 == 0)
    }
    start = time.time()
    res = requests.post(BASE_URL, json=payload)
    latency = round((time.time() - start) * 1000, 1)
    data = res.json()
    print(f"[Visitor {i+1:02d}] Token #{data['tokenNumber']} -> Counter 0{data['counterId']} ({latency}ms)")

if __name__ == "__main__":
    print(f"🚀 Injecting {TOTAL_VISITORS} concurrent visitors into QueueZero...")
    t0 = time.time()
    with concurrent.futures.ThreadPoolExecutor(max_workers=15) as executor:
        executor.map(simulate_visitor, range(TOTAL_VISITORS))
    print(f"✅ Completed {TOTAL_VISITORS} concurrent requests in {round(time.time() - t0, 2)}s!")