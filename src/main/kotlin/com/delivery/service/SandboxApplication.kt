package com.delivery.service

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.cloud.openfeign.EnableFeignClients

@SpringBootApplication
@EnableFeignClients
class SandboxApplication

fun main(args: Array<String>) {
    runApplication<SandboxApplication>(*args)
}
