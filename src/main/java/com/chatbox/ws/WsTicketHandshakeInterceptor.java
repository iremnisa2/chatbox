package com.chatbox.ws;

import com.chatbox.domain.service.WsTicketService;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.HandshakeInterceptor;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.util.Map;

@Component
public class WsTicketHandshakeInterceptor implements HandshakeInterceptor {

    private final WsTicketService wsTicketService;

    public WsTicketHandshakeInterceptor(WsTicketService wsTicketService) {
        this.wsTicketService = wsTicketService;
    }

    @Override
    public boolean beforeHandshake(ServerHttpRequest request,
                                   ServerHttpResponse response,
                                   WebSocketHandler wsHandler,
                                   Map<String, Object> attributes) {

        URI uri = request.getURI();
        String ticket = UriComponentsBuilder.fromUri(uri)
                .build()
                .getQueryParams()
                .getFirst("ticket");

        if (ticket == null || ticket.isBlank()) {
            response.setStatusCode(HttpStatus.FORBIDDEN);
            return false;
        }

        WsTicketService.ValidationResult result = wsTicketService.validateAndConsume(ticket);
        if (!result.valid()) {
            response.setStatusCode(HttpStatus.FORBIDDEN);
            return false;
        }


        attributes.put("ticket", ticket);
        attributes.put("username", result.username());
        attributes.put("ticketIssuedAt", result.issuedAtEpochMs());

        return true;
    }

    @Override
    public void afterHandshake(ServerHttpRequest request, ServerHttpResponse response,
                               WebSocketHandler wsHandler, Exception exception) {
        // no-op
    }
}