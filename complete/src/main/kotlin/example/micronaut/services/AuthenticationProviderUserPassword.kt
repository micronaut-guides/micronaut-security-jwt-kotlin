package example.micronaut.services

import io.micronaut.security.authentication.AuthenticationFailed
import io.micronaut.security.authentication.AuthenticationProvider
import io.micronaut.security.authentication.AuthenticationRequest
import io.micronaut.security.authentication.AuthenticationResponse
import io.micronaut.security.authentication.UserDetails
import io.reactivex.Flowable
import org.reactivestreams.Publisher
import java.util.*
import javax.inject.Singleton

@Singleton // <1>
class AuthenticationProviderUserPassword : AuthenticationProvider { // <2>
    override fun authenticate(authenticationRequest: AuthenticationRequest<*, *>?): Publisher<AuthenticationResponse> {
        if (authenticationRequest != null && authenticationRequest.identity != null && authenticationRequest.secret != null) {
            if (authenticationRequest.identity == "sherlock" && authenticationRequest.secret == "password") {
                return Flowable.just<AuthenticationResponse>(UserDetails(authenticationRequest.identity as String, ArrayList()))
            }
        }
        return Flowable.just<AuthenticationResponse>(AuthenticationFailed())
    }
}