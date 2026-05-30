package org.example.rspcm.service;

import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class ChatPresenceService {

    private final Map<String, String> sessionUser = new ConcurrentHashMap<>();
    private final Map<String, Set<Long>> sessionChats = new ConcurrentHashMap<>();
    private final Map<Long, Set<String>> chatOnlineUsers = new ConcurrentHashMap<>();

    public void registerSession(String sessionId, String userIdentifier) {
        if (sessionId == null || userIdentifier == null) {
            return;
        }
        sessionUser.put(sessionId, userIdentifier);
    }

    public void trackSubscribe(String sessionId, Long chatId) {
        if (sessionId == null || chatId == null) {
            return;
        }
        String user = sessionUser.get(sessionId);
        if (user == null) {
            return;
        }

        sessionChats.computeIfAbsent(sessionId, key -> ConcurrentHashMap.newKeySet()).add(chatId);
        chatOnlineUsers.computeIfAbsent(chatId, key -> ConcurrentHashMap.newKeySet()).add(user);
    }

    public void trackUnsubscribe(String sessionId, Long chatId) {
        if (sessionId == null || chatId == null) {
            return;
        }
        String user = sessionUser.get(sessionId);
        if (user == null) {
            return;
        }

        Set<Long> chats = sessionChats.get(sessionId);
        if (chats != null) {
            chats.remove(chatId);
            if (chats.isEmpty()) {
                sessionChats.remove(sessionId);
            }
        }

        removeUserIfNoSessionSubscribed(user, chatId);
    }

    public void removeSession(String sessionId) {
        if (sessionId == null) {
            return;
        }
        String user = sessionUser.remove(sessionId);
        Set<Long> chats = sessionChats.remove(sessionId);
        if (user == null || chats == null || chats.isEmpty()) {
            return;
        }
        for (Long chatId : chats) {
            removeUserIfNoSessionSubscribed(user, chatId);
        }
    }

    public Map<Long, Integer> onlineCounts(Collection<Long> chatIds) {
        if (chatIds == null || chatIds.isEmpty()) {
            return Collections.emptyMap();
        }
        Map<Long, Integer> result = new HashMap<>();
        for (Long chatId : chatIds) {
            Set<String> users = chatOnlineUsers.get(chatId);
            result.put(chatId, users == null ? 0 : users.size());
        }
        return result;
    }

    private void removeUserIfNoSessionSubscribed(String user, Long chatId) {
        boolean stillSubscribed = sessionChats.entrySet().stream()
                .anyMatch(entry -> user.equals(sessionUser.get(entry.getKey())) && entry.getValue().contains(chatId));

        if (stillSubscribed) {
            return;
        }

        Set<String> users = chatOnlineUsers.get(chatId);
        if (users == null) {
            return;
        }
        users.remove(user);
        if (users.isEmpty()) {
            chatOnlineUsers.remove(chatId);
        }
    }
}
