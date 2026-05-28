package com.example.banking.Controller;

import com.example.banking.DTO.ChatMessageDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.HashMap;
import java.util.Map;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
public class ChatController {

    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    // A simple mock predefined Q&A dictionary for the Bot
    private static final Map<String, String> BOT_QA = new HashMap<>();
    static {
        BOT_QA.put("xin chào", "Chào bạn! Tôi là trợ lý ảo. Tôi có thể giúp gì cho bạn?");
        BOT_QA.put("hello", "Chào bạn! Tôi là trợ lý ảo. Tôi có thể giúp gì cho bạn?");
        BOT_QA.put("quên mật khẩu",
                "Nếu bạn quên mật khẩu, hãy nhấn vào nút 'Quên mật khẩu' ở trang đăng nhập và nhập email để nhận mã OTP.");
        BOT_QA.put("chuyển tiền", "Để chuyển tiền, vui lòng truy cập mục 'Chuyển tiền' (Banking) từ trang chủ.");
        BOT_QA.put("faceid",
                "Giao dịch trên 10,000,000 VND yêu cầu xác thực FaceID. Bạn có thể đăng ký FaceID trong mục Profile.");
        BOT_QA.put("pin", "Bạn có thể thiết lập mã PIN chuyển tiền trong mục Profile.");
    }

    // In-memory chat history for the current Admin session
    private static final Map<String, List<ChatMessageDTO>> chatHistory = new ConcurrentHashMap<>();

    public static void addMessageToHistory(String targetUsername, ChatMessageDTO msg) {
        chatHistory.computeIfAbsent(targetUsername, k -> new CopyOnWriteArrayList<>()).add(msg);
    }

    public static void clearChatHistory() {
        chatHistory.clear();
    }

    @GetMapping("/admin/chat/history")
    @ResponseBody
    public Map<String, List<ChatMessageDTO>> getChatHistory() {
        return chatHistory;
    }

    @MessageMapping("/chat.sendMessage")
    public void receiveMessage(@Payload ChatMessageDTO chatMessage) {
        // Save to history
        addMessageToHistory(chatMessage.getSender(), chatMessage);

        // Broadcast the user's message to the admin queue
        messagingTemplate.convertAndSend("/topic/admin", chatMessage);

        // Check if Bot can answer
        String contentLower = chatMessage.getContent().toLowerCase();
        boolean answeredByBot = false;

        for (Map.Entry<String, String> entry : BOT_QA.entrySet()) {
            if (contentLower.contains(entry.getKey())) {
                ChatMessageDTO botReply = new ChatMessageDTO(
                        "Bot",
                        entry.getValue(),
                        ChatMessageDTO.MessageType.BOT);
                // Save bot reply to history under sender's key
                addMessageToHistory(chatMessage.getSender(), botReply);
                messagingTemplate.convertAndSend("/topic/user." + chatMessage.getSender(), botReply);
                answeredByBot = true;
                break;
            }
        }

        if (!answeredByBot) {
            ChatMessageDTO botReply = new ChatMessageDTO(
                    "Bot",
                    "Câu hỏi của bạn đã được chuyển đến nhân viên hỗ trợ. Vui lòng đợi trong giây lát...",
                    ChatMessageDTO.MessageType.BOT);
            addMessageToHistory(chatMessage.getSender(), botReply);
            messagingTemplate.convertAndSend("/topic/user." + chatMessage.getSender(), botReply);
        }
    }

    @MessageMapping("/chat.adminReply")
    public void adminReply(@Payload ChatMessageDTO chatMessage) {
        // Save to history
        addMessageToHistory(chatMessage.getTargetUser(), chatMessage);

        // Broadcast the admin's reply to the specific target user's topic
        messagingTemplate.convertAndSend("/topic/user." + chatMessage.getTargetUser(), chatMessage);
    }

    @MessageMapping("/chat.adminBroadcast")
    public void adminBroadcast(@Payload ChatMessageDTO chatMessage) {
        // Save broadcast to history so it's not lost
        addMessageToHistory("BROADCAST", chatMessage);
        // Broadcast the admin's message to all users
        messagingTemplate.convertAndSend("/topic/broadcast", chatMessage);
    }

    @GetMapping("/admin/chat")
    public String adminChatPage() {
        // Simple mapping for the Admin Chat UI
        return "adminChat";
    }
}
