# Knative Serverless Event Processing Demo

## Introduction

This project demonstrates how to do serverless event processing with Knative.

A special application, `message-generator` located in the source tree, produces lots of messages to the Kafka cluster.

KafkaSource fetches them and then sends them to the Knative service called `message-sink`, which is also located in the source tree.
`message-sink` is a very simple application that only logs the requests it receives.

Since `message-generator` is sending a lot of messages, Knative will scale the `message-sink` horizontally to multiple pods and kill
the pods once all messages are consumed from Kafka.

Following the diagram of the workflow:

```
                                ┌───────────┐                                   Knative Service
                                │           │        ┌───────────────┐    ┌─────────────────────────┐
┌──────────────────────┐        │           │        │               │    │                         │
│ Message Producer     ├────────►   Kafka   ├────────►  KafkaSource  ├────►─┐                       │
│ (message-generator)  │        │           │        │               │    │ │                       │
└──────────────────────┘        │           │        └───────────────┘    │ │                       │
                                └───────────┘                             │ │  ┌──────────────────┐ │
                                                                          │ ├──►       Pod        │ │
                                                                          │ │  │  (message-sink)  │ │
                                                                          │ │  └──────────────────┘ │
                                                                          │ │                       │
                                                                          │ │  ┌──────────────────┐ │
                                                                          │ └──►       Pod        │ │
                                                                          │    │  (message-sink)  │ │
                                                                          │    └──────────────────┘ │
                                                                          │                         │
                                                                          │                         │
                                                                          │         ...             │
                                                                          └─────────────────────────┘
```


## Prerequisites

* Docker
* `kind`: https://kind.sigs.k8s.io/
* `kubectl`: https://kubernetes.io/docs/tasks/tools/
* `stern`: https://github.com/wercker/stern
* A recent version of NodeJS for `message-generator` (demo presented with Node v12.14.1)
* No JDK is needed as the Java application is built within a container

## Prepare

Start your cluster:

```bash
kind create cluster
```

Install Knative, Strimzi; create a Kafka cluster:

```bash
./hack/01-kn-serving.sh && ./hack/02-kn-eventing.sh && ./hack/03-strimzi.sh && ./hack/04-kn-kafka.sh
```

Build `message-generator` and `message-sink` images:

```bash
## TODO DOCKER_HUB_USERNAME=<your username here>
DOCKER_HUB_USERNAME=aliok

npm --prefix ./message-generator install ./message-generator
docker build message-generator -t docker.io/${DOCKER_HUB_USERNAME}/message-generator
docker push docker.io/${DOCKER_HUB_USERNAME}/message-generator

cd message-sink
./mvnw package \
-Pnative \
-Dquarkus.native.container-build=true \
-Dquarkus.container-image.build=true \
-Dquarkus.container-image.push=true \
-Dquarkus.container-image.builder=docker \
-Dquarkus.container-image.image=docker.io/${DOCKER_HUB_USERNAME}/message-sink
cd ..

docker push docker.io/${DOCKER_HUB_USERNAME}/message-sink
```

## Run the demo

### Setting up common resources

Create the namespace, the source and the sink:

```bash
kubectl apply -f config/01-namespace.yaml
kubectl apply -f config/02-sink.yaml
kubectl apply -f config/03-topic.yaml
kubectl apply -f config/04-source.yaml
```

Start watching the sink Knative service logs:

```bash
stern -n my-namespace sink
```

### Sending messages

In another terminal, start watching the pods:

```bash
watch kubectl get pods -n my-namespace
```

Create the message-generator job, which sends many messages to Kafka:

```bash
kubectl apply -f config/05-message-generator.yaml
```

You should see a lot of pods coming up.

## Clean up

```bash
kubectl delete -f config/

kind delete cluster
```
