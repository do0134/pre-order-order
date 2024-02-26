import requests
import redis


def redis_test():
    r = redis.Redis("localhost", 6379)

    p = r.pubsub()
    p.subscribe("User 1 order Item 1")
    cnt = 0
    
    requests.post("http://localhost:8085/api/v1/heartbeat/1/1")

    for message in p.listen():
        if message['type'] == 'message':
            m = message['data'].decode('utf-8')
            print(m)
            print("Received heartbeat:", m)

            if m == "User 1 order Item 1":
                r.publish('User 1 order Item 1', "alive")
                cnt += 1
                print(f"Send {cnt}th data:", "alive")
        if cnt == 10:
            break

    print(f"HeartBeat Count: {cnt}")


if __name__ == "__main__":
    redis_test()