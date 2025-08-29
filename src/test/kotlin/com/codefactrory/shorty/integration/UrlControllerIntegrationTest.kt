package com.codefactrory.shorty.integration

import com.codefactrory.shorty.infrastructure.adapter.incoming.UrlRequestDto
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.web.client.TestRestTemplate
import org.springframework.boot.test.web.server.LocalServerPort
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.http.client.SimpleClientHttpRequestFactory
import java.net.HttpURLConnection.setFollowRedirects

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class UrlControllerIntegrationTest {

        @LocalServerPort
        private var port: Int = 0

        private lateinit var restTemplate: TestRestTemplate
        private lateinit var baseUrl: String

        @BeforeEach
        fun setup() {
                baseUrl = "http://localhost:$port"
                restTemplate = TestRestTemplate().apply {
                        this.restTemplate.requestFactory = SimpleClientHttpRequestFactory().apply {
                            setFollowRedirects(false) // Disable automatic redirects
                        }
                    }
            }

        @Test
        fun `should create new short URL for valid original URL`() {
                val request = UrlRequestDto("www.example.com")
                val response: ResponseEntity<Map<*, *>> =
                    restTemplate.postForEntity("$baseUrl/shorten", request, Map::class.java)

                assertEquals(HttpStatus.CREATED, response.statusCode)
                val body = response.body!!
                val shortUrl = body["shortUrl"] as String
                val originalUrl = body["originalUrl"] as String

                assertTrue(shortUrl.startsWith("http://localhost"))
                assertEquals("https://www.example.com", originalUrl)
            }

        @Test
        fun `should return same short URL if original URL already exists`() {
                val request = UrlRequestDto("https://www.dkbcodefactory.com/")
                val first = restTemplate.postForEntity("$baseUrl/shorten", request, Map::class.java)
                val firstShort = first.body!!["shortUrl"]

                val second = restTemplate.postForEntity("$baseUrl/shorten", request, Map::class.java)
                val secondShort = second.body!!["shortUrl"]

                assertEquals(firstShort, secondShort)
            }

        @Test
        fun `should return 400 for invalid URL`() {
                val request = UrlRequestDto("invalid-url")
                val response = restTemplate.postForEntity("$baseUrl/shorten", request, String::class.java)

                assertEquals(HttpStatus.BAD_REQUEST, response.statusCode)
                assertTrue(response.body!!.contains("Invalid URL"))
            }

        @Test
        fun `should return 400 when original URL is blank`() {
                val request = UrlRequestDto("")
                val response = restTemplate.postForEntity("$baseUrl/shorten", request, String::class.java)

                assertEquals(HttpStatus.BAD_REQUEST, response.statusCode)
                assertTrue(response.body!!.contains("Url should not be blank"))
            }

        @Test
        fun `should redirect to original URL when short code exists`() {
                val request = UrlRequestDto("www.redirect.com")
                val createResponse = restTemplate.postForEntity("$baseUrl/shorten", request, Map::class.java)
                val shortUrl = createResponse.body!!["shortUrl"] as String

                val shortCode = shortUrl.substringAfterLast("/")

                // Use absolute URL for GET
                val redirectResponse = restTemplate.getForEntity("$baseUrl/$shortCode", String::class.java)

                assertEquals(HttpStatus.FOUND, redirectResponse.statusCode)
                assertEquals("https://www.redirect.com", redirectResponse.headers.location.toString())
            }

        @Test
        fun `should return 404 when short code does not exist`() {
                val response = restTemplate.getForEntity("$baseUrl/original/nonexistent", String::class.java)
                assertEquals(HttpStatus.NOT_FOUND, response.statusCode)
                assertTrue(response.body!!.contains("Not found"))
            }
}