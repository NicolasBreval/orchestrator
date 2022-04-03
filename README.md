# Orchestrator
Library to automatize concurrent processes and their communication using message queues.

## Summary
This open source project is, at the same time, my final degree project, and it tries to find a 
practical solution for those developers who require a system to automate and interconnect tasks 
programmed by the developer himself. This project tries to be sufficiently reliable so that the 
developer can forget about the problem of managing the life of the tasks and can focus all his 
efforts on developing the logic that his project needs.

## Table of content
* [Project status](#project-status)
* [Environment requirements](#environment-requirements)
* [Technical stack](#technical-stack)
* [How to use](#how-to-use)
  * [Install GRAALVM](#install-graalvm)
  * [Set up project using IntelliJ](#set-up-project-using-intellij)
  * [Create your custom subscription](#create-your-custom-subscription)

## Project status
This project is currently on development. I will upload new releases when it can be used without any problematic errors.

## Environment requirements
TODO

## Technical stack

* **GRAALVM**: The project has been development to take advantage of native compilation with GRAALVM, so it's needed to compile it with this JVM. Official page: https://www.graalvm.org/
* **Kotlin**: The language used to this project has been Kotlin, because I think is a good Java-based language, allowing the full power of Java to be used, but with a more modern syntax and additions that greatly facilitate development time. As Kotlin is fully compatible with Java, you can use Java code when use this project. Official page: https://kotlinlang.org/
* RabbitMQ/ActiveMQ: To add consistency to the system's task interaction, Orchestrator uses a queues' system, using RabbitMQ or ActiveMQ protocol (this can be selected from properties file). Official pages: https://www.rabbitmq.com/ (RabbitMQ), https://activemq.apache.org/ (ActiveMQ)
* **SLF4J/Logback**: For system logging is used SLF4J library and their custom implementation, Logback. Official page: https://www.slf4j.org/
* **Fluentd**: Fluentd is used to collect logs and redirect it to another data sources. Orchestrator implements a custom logger appender to redirect all logs to a fluentd server and store your logs in another system. 
* **Exposed**: Exposed is an ORM written entirely in Kotlin, which allows using different databases, like Oracle, SQL Server, PostgreSQL or MySQL, among others, with same code. Exposed in used in Orchestrator to store all tasks and prevent the loss of running tasks. Exposed has been developed by Jetbrains, creators of IntelliJ IDE and Kotlin language. Official page: https://github.com/JetBrains/Exposed
* **Hikari**: This library is used for Exposed connections. Hikari allows creating pooled connections through JDBC drivers easily. Official page: https://github.com/brettwooldridge/HikariCP
* **Cron utils**: Some subscriptions are executed periodically, and some of them are executed by a cron expression. To allow this, Orchestrator uses a library to evaluate cron expressions called cron-utils. Official page: http://cron-parser.com/
* **Jackson**: All subscriptions are represented as a JSON with their configuration, with this JSON you can create a new instance. To make possible this operative, Orchestrator uses Jackson library to parser objects to JSON and vice-versa. Official page: https://github.com/FasterXML/jackson
* **Kryo**: For subscription interaction, all messages sent by queues is encoded to a byte array before sending, and decoded to an object again to read their information. To make this more fast and secure Orchestrator uses Kryo library. Official page: https://github.com/EsotericSoftware/kryo

## How to use

### Install GRAALVM
TODO

### Set up project using IntelliJ
TODO

### Create your custom subscription
TODO
