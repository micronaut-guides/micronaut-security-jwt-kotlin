package example.micronaut

import io.micronaut.context.ApplicationContext
import io.micronaut.http.HttpRequest
import io.micronaut.http.HttpResponse
import io.micronaut.http.HttpStatus
import io.micronaut.http.client.RxHttpClient
import io.micronaut.http.client.exceptions.HttpClientResponseException
import io.micronaut.runtime.server.EmbeddedServer
import io.micronaut.security.authentication.UsernamePasswordCredentials
import io.micronaut.security.token.jwt.render.BearerAccessRefreshToken
import org.spekframework.spek2.Spek
import org.spekframework.spek2.style.specification.describe

import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class DeclarativeHttpClientWithJwtSpec : Spek({
    describe("Verify JWT authentication works with declarative @Client") {

        var embeddedServer: EmbeddedServer = ApplicationContext.run(EmbeddedServer::class.java) // <1>
        var client: RxHttpClient = RxHttpClient.create(embeddedServer.url) // <2>

        var appClient: AppClient? = null
        it("AppClient Bean can be retrieved from Application context") {
            var exceptionThrown = false
            try {
                appClient = embeddedServer.applicationContext.getBean(AppClient::class.java) // <3>

            } catch (e: HttpClientResponseException) {
                exceptionThrown = true
            }
            assertFalse(exceptionThrown)
        }
        it("Accessing a secured URL without authenticating returns unauthorized") {
            var exceptionThrown = false
            try {
                val request = HttpRequest.GET<Any>("/")
                client.toBlocking().exchange(request, String::class.java)
            } catch (e: HttpClientResponseException) {
                exceptionThrown = true
            }
            assertTrue(exceptionThrown)
        }
        var accessToken: String? = null
        it("User can login") {
            val creds = UsernamePasswordCredentials("sherlock", "password")
            val request = HttpRequest.POST("/login", creds) // <4>

            val rsp: HttpResponse<BearerAccessRefreshToken> = client.toBlocking().exchange(request,
                    BearerAccessRefreshToken::class.java) // <5>

            assertEquals(rsp.status()!!, HttpStatus.OK)
            assertNotNull(rsp.body()!!.accessToken)

            accessToken = rsp.body()!!.accessToken // <6>
        }
        it("Access / endpoint with appClient") {
            var msg: String = appClient!!.home("Bearer ${accessToken!!}") // <7>
            assertTrue(msg == "sherlock")
        }

        afterGroup {
            client.close()
            embeddedServer.close()
        }
    }
})