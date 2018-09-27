package example.micronaut

import io.micronaut.http.annotation.Get
import io.micronaut.http.annotation.Header
import io.micronaut.http.client.annotation.Client

@Client("/")
interface AppClient {

    @Get("/")
    fun home(@Header authorization: String): String
}