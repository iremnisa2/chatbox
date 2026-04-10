package com.chatbox.domain.controller;

import com.chatbox.domain.service.WsTicketService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
public class WsTicketController {

    private final WsTicketService wsTicketService;

    public WsTicketController(WsTicketService wsTicketService) {
        this.wsTicketService = wsTicketService;
    }

    @PostMapping("/ws-ticket")
    @PreAuthorize("isAuthenticated()")
    public WsTicketService.IssueResponse issueTicket(Authentication authentication) {
        String username = authentication.getName();
        return wsTicketService.issueForUsername(username);
    }
}