package com.codefactrory.shorty.infrastructure.adapter.incoming

import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController

@RestController
class HealthController {

    @GetMapping("/health")
    fun healthCheck() = ResponseEntity.ok("Service is up and Running!!!")
}