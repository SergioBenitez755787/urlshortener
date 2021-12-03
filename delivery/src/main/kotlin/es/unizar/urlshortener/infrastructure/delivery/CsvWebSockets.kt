package es.unizar.urlshortener.infrastructure.delivery

import es.unizar.urlshortener.core.ShortUrlProperties
import es.unizar.urlshortener.core.usecases.CreateCsvShortUrlUseCase
import es.unizar.urlshortener.core.usecases.CreateShortUrlUseCase
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.core.io.ByteArrayResource
import org.springframework.core.io.Resource
import org.springframework.hateoas.server.mvc.linkTo
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Component
import org.springframework.web.bind.annotation.*
import org.springframework.web.multipart.MultipartFile
import org.springframework.web.socket.config.annotation.EnableWebSocket
import org.springframework.web.socket.server.standard.ServerEndpointExporter
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import javax.inject.Inject
import javax.servlet.http.HttpServletRequest
import javax.websocket.*
import javax.websocket.server.ServerEndpoint

class CsvEndpoint() : Endpoint() {

    @Autowired
    lateinit var createShortUrlUseCase: CreateShortUrlUseCase

    override fun onOpen(session: Session, config: EndpointConfig) {
        println("Server Connected ... Session ${session.id}")
        synchronized(session) {
            with(session.basicRemote) {
                sendText("Send me the URLs")
            }
        }
        session.addMessageHandler(object : MessageHandler.Whole<String> {
            override fun onMessage(message: String) {
                println("Server Message ... Session ${session.id}")
                println("Server received $message")
                if (message != "That was the last URL") {
                    synchronized(session) {
                        with(session.basicRemote) {
                            sendText(shortener(message))
                        }
                    }
                } else {
                    session.close(CloseReason(CloseReason.CloseCodes.NORMAL_CLOSURE, "Alright then, goodbye!"))
                }
            }
        })
    }

    @OnClose
    override fun onClose(session: Session, closeReason: CloseReason) {
        println("Session ${session.id} closed because of $closeReason")
    }

    @OnError
    override fun onError(session: Session, errorReason: Throwable) {
        println("Session ${session.id} closed because of ${errorReason.javaClass.name}")
    }

    fun shortener(originalUrl: String): String {
        createShortUrlUseCase.create(
            url = originalUrl,
            data = ShortUrlProperties(
                ip = "remoteAddr",
                sponsor = null
            )
        )/*.let {
            val h = HttpHeaders()
            val url = linkTo<UrlShortenerControllerImpl> { redirectTo(it.hash, request) }.toUri()
            h.location = url
            val response = ShortUrlDataOut(
                url = url,
                properties = mapOf(
                    "safe" to it.properties.safe
                )
            )
            ResponseEntity<ShortUrlDataOut>(response, h, HttpStatus.CREATED)
        }*/
        return "url acortada";
    }
}