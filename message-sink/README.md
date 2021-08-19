# Run the application - with hot-reloading

```
./mvnw compile quarkus:dev
```

# Build native binary

```
./mvnw package -Pnative
```

# Build native binary using a build container, so that you don't have to have GraalVM installed

```
./mvnw package \
-Pnative \
-Dquarkus.native.container-build=true
```

# Run the binary

```
./target/message-sink-1.0.0-SNAPSHOT-runner
```

# Check the startup time of the native executable

```
date +%H:%M:%S.%N && ./target/message-sink-1.0.0-SNAPSHOT-runner
```

# Check resource usage

```
ps -e -o rss,args | grep message-sink-1.0.0-SNAPSHOT-runner
# 1st value in the output is the memory usage in Kb
# sample output
# 17504 ./target/message-sink-1.0.0-SNAPSHOT-runner

```

# Create the runnable binary, build a container with it, push it to container registry

```
./mvnw package \
-Pnative \
-Dquarkus.native.container-build=true \
-Dquarkus.container-image.build=true \
-Dquarkus.container-image.push=true \
-Dquarkus.container-image.builder=docker \
-Dquarkus.container-image.image=docker.io/aliok/message-sink
```

# Run the image

```
docker run -p 8080:8080 --rm --name=message-sink docker.io/aliok/message-sink:latest
```

# Make a test request

```
curl -v http://localhost:8080 \
  -H "Content-Type:application/json" \
  -H "Ce-Id:1" \
  -H "Ce-Source:cloud-event-example" \
  -H "Ce-Type:myCloudEventGreeting" \
  -H "Ce-Specversion:1.0" \
  -d "{\"hello\": \"world\"}"
```

You should see the following output in the logs:

```
__  ____  __  _____   ___  __ ____  ______
 --/ __ \/ / / / _ | / _ \/ //_/ / / / __/
 -/ /_/ / /_/ / __ |/ , _/ ,< / /_/ /\ \
--\___\_\____/_/ |_/_/|_/_/|_|\____/___/
2021-08-10 16:15:44,337 INFO  [io.quarkus] (Quarkus Main Thread) message-sink 1.0.0-SNAPSHOT on JVM (powered by Quarkus 1.13.5.Final) started in 1.476s. Listening on: http://localhost:8080
2021-08-10 16:15:44,339 INFO  [io.quarkus] (Quarkus Main Thread) Profile dev activated. Live Coding activated.
2021-08-10 16:15:44,339 INFO  [io.quarkus] (Quarkus Main Thread) Installed features: [cdi, resteasy]

Headers:
  Accept:*/*
  Ce-Id:1
  Ce-Source:cloud-event-example
  Ce-Specversion:1.0
  Ce-Type:myCloudEventGreeting
  Content-Length:18
  Content-Type:application/json
  Host:localhost:8080
  User-Agent:curl/7.64.1
Body:
  {"hello": "world"}
```
