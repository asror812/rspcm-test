package org.example.rspcm.config;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.rspcm.exception.ErrorCodes;
import org.example.rspcm.exception.ErrorMessageException;
import org.example.rspcm.repository.ChatRepository;
import org.example.rspcm.security.JwtService;
import org.example.rspcm.security.UserDetailsServiceImpl;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Component;

import java.security.Principal;

@Slf4j
@Component
@RequiredArgsConstructor
public class WebscocketAuthChannelInterceptor implements ChannelInterceptor {

    private final JwtService jwtService;
    private final UserDetailsServiceImpl userDetailsServiceImpl;
    private final ChatRepository chatRepository;

    @Override
    public Message<?> preSend(
            @NonNull Message<?> message,
            @NonNull MessageChannel channel) {

        StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);
        if (accessor == null) {
            return message;
        }

        StompCommand command = accessor.getCommand();

        if (command == null) return message;

        if (command.equals(StompCommand.CONNECT)) {
            String header = accessor.getFirstNativeHeader("Authorization");

            if (header == null || !header.startsWith("Bearer ")) {
                throw new IllegalArgumentException("Missing or invalid Authorization header");
            }

            String token = header.substring(7);

            if (token.isEmpty()) {
                throw new IllegalArgumentException("Missing or invalid Authorization header");
            }

            String identifier = jwtService.extractUsername(token);
            UserDetails user = userDetailsServiceImpl.loadUserByUsername(identifier);

            if (!jwtService.isTokenValid(token, identifier)) {
                throw new UsernameNotFoundException("Invalid token");
            }

            UsernamePasswordAuthenticationToken authenticationToken =
                    new UsernamePasswordAuthenticationToken(
                            user.getUsername(),
                            null,
                            user.getAuthorities());

            accessor.setUser(authenticationToken);
            log.info("WS connect authorized sessionId={}", accessor.getSessionId());
        }

        if (command.equals(StompCommand.SEND) || command.equals(StompCommand.SUBSCRIBE)) {
            Principal principal = accessor.getUser();

            if (principal == null) {
                throw new ErrorMessageException("Unauthorized websocket session", ErrorCodes.Unauthorized);
            }

            validateChatMembership(principal.getName(), accessor.getDestination());
        }

        return message;
    }

    private void validateChatMembership(String name, String destination) {
        if (destination == null) {
            throw new ErrorMessageException("Unauthorized websocket session", ErrorCodes.Unauthorized);
        }

        Long chatId = extractChatId(destination);
        if (chatId == null) {
            return;
        }

        if (!chatRepository.existsByIdAndMemberIdentifier(chatId, name)) {
            throw new ErrorMessageException("You are not member of this group", ErrorCodes.Unauthorized);
        }
    }

    private Long extractChatId(String destination) {
        String[] prefixes = {"/topic/chats/", "/app/chats/"};

        for (String prefix : prefixes) {
            if (destination.startsWith(prefix)) {
                String suffix = destination.substring(prefix.length());

                if (suffix.endsWith("/messages")) {
                    suffix = suffix.substring(0, suffix.length() - "/messages".length());
                }
                if (suffix.contains("/")) {
                    suffix = suffix.substring(0, suffix.indexOf('/'));
                }

                try {
                    return Long.valueOf(suffix);
                } catch (NumberFormatException e) {
                    return null;
                }
            }
        }
        return null;
    }

}
