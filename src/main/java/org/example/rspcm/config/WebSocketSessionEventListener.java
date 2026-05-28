package org.example.rspcm.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.messaging.SessionConnectEvent;
import org.springframework.web.socket.messaging.SessionConnectedEvent;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;

import java.security.Principal;

@Slf4j
@Component
public class WebSocketSessionEventListener {

    @EventListener
    public void onSessionConnect(SessionConnectEvent event) {
        StompHeaderAccessor accessor = StompHeaderAccessor.wrap(event.getMessage());
        Principal user = accessor.getUser();
        log.info("WS SessionConnect sessionId={} user={}", accessor.getSessionId(), user == null ? "anonymous" : user.getName());
    }

    @EventListener
    public void onSessionConnected(SessionConnectedEvent event) {
        StompHeaderAccessor accessor = StompHeaderAccessor.wrap(event.getMessage());
        Principal user = accessor.getUser();
        log.info("WS SessionConnected sessionId={} user={}", accessor.getSessionId(), user == null ? "anonymous" : user.getName());
    }

    @EventListener
    public void onSessionDisconnect(SessionDisconnectEvent event) {
        Principal user = event.getUser();
        log.info("WS SessionDisconnect sessionId={} user={} closeStatus={}",
                event.getSessionId(),
                user == null ? "anonymous" : user.getName(),
                event.getCloseStatus());
    }
}
