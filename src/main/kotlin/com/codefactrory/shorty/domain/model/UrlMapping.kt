package com.codefactrory.shorty.domain.model

import jakarta.persistence.*
import java.time.Instant

@Entity
@Table(name = "url")
class UrlMapping (

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null,

    @Column(nullable = false, unique = true)
    var originalUrl: String,

    @Column(nullable = false, unique = true)
    var shortUrlCode: String,

    var createdAt: Instant = Instant.now(),
)