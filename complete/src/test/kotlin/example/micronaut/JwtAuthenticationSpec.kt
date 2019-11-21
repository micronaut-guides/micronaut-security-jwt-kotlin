package example.micronaut

import com.nimbusds.jwt.JWTParser
import com.nimbusds.jwt.SignedJWT
import io.micronaut.context.ApplicationContext
import io.micronaut.http.HttpHeaders
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
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class BasicAuthSpec : Spek({
    describe("Verify JWT authentication works") {

        var embeddedServer: EmbeddedServer = ApplicationContext.run(EmbeddedServer::class.java) // <1>
        var client: RxHttpClient = RxHttpClient.create(embeddedServer.url) // <2>
        it("Accessing a secured URL without authenticating") {
            var exceptionThrown = false
            try {
                val request = HttpRequest.GET<Any>("/")
                client.toBlocking().exchange(request, String::class.java)
            } catch (e: HttpClientResponseException) {  // <3>
                exceptionThrown = true
            }
            assertTrue(exceptionThrown)
        }
        it("the endpoint can be access with JWT obtained when Login endpoint is called with valid credentials") {
            val creds = UsernamePasswordCredentials("sherlock", "password")
            val request = HttpRequest.POST("/login", creds) // <4>

            val rsp: HttpResponse<BearerAccessRefreshToken> = client.toBlocking().exchange(request,
                    BearerAccessRefreshToken::class.java) // <5>

            assertEquals(rsp.status()!!, HttpStatus.OK)
            assertNotNull(rsp.body()!!.accessToken)
            assertTrue(JWTParser.parse(rsp.body()!!.accessToken) is SignedJWT)
            assertNotNull(rsp.body()!!.refreshToken)
            assertNotNull(JWTParser.parse(rsp.body()!!.refreshToken) is SignedJWT)

            val accessToken: String = rsp.body()!!.accessToken
            val requestWithAuthorization = HttpRequest.GET<Any>("/").header(HttpHeaders.AUTHORIZATION, "Bearer $accessToken") // <6>
            val response: HttpResponse<String> = client.toBlocking().exchange(requestWithAuthorization, String::class.java)

            assertEquals(response.status()!!, HttpStatus.OK)
            assertTrue(response.body()!! == "sherlock") // <7>
        }
        afterGroup {
            client.close()
            embeddedServer.close()
        }

    }
})