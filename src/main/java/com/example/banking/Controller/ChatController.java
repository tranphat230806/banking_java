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

@Controller
public class ChatController {

    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    // A simple mock predefined Q&A dictionary for the Bot
    private static final Map<String, String> BOT_QA = new HashMap<>();
    static {
        BOT_QA.put("xin chào", "Chào bạn! Tôi là trợ lý ảo. Tôi có thể giúp gì cho bạn?");
        BOT_QA.put("hello", "Chào bạn! Tôi là trợ lý ảo. Tôi có thể giúp gì cho bạn?");
        BOT_QA.put("quên mật khẩu", "Nếu bạn quên mật khẩu, hãy nhấn vào nút 'Quên mật khẩu' ở trang đăng nhập và nhập email để nhận mã OTP.");
        BOT_QA.put("chuyển tiền", "Để chuyển tiền, vui lòng truy cập mục 'Chuyển tiền' (Banking) từ trang chủ.");
        BOT_QA.put("faceid", "Giao dịch trên 10,000,000 VND yêu cầu xác thực FaceID. Bạn có thể đăng ký FaceID trong mục Profile.");
        BOT_QA.put("pin", "Bạn có thể thiết lập mã PIN chuyển tiền trong mục Profile.");
    }

    @MessageMapping("/chat.sendMessage")
    public void receiveMessage(@Payload ChatMessageDTO chatMessage) {
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
                        ChatMessageDTO.MessageType.BOT
                );
                // Send reply specifically to the user's topic
                messagingTemplate.convertAndSend("/topic/user." + chatMessage.getSender(), botReply);
                answeredByBot = true;
                break;
            }
        }

        if (!answeredByBot) {
            // Forward to admin
            ChatMessageDTO botReply = new ChatMessageDTO(
                    "Bot", 
                    "Câu hỏi của bạn đã được chuyển đến nhân viên hỗ trợ. Vui lòng đợi trong giây lát...", 
                    ChatMessageDTO.MessageType.BOT
            );
            messagingTemplate.convertAndSend("/topic/user." + chatMessage.getSender(), botReply);
        }
    }

    @MessageMapping("/chat.adminReply")
    public void adminReply(@Payload ChatMessageDTO chatMessage) {
        // Admin's reply will have the target user in the 'sender' field (or we can assume content has a specific format, but let's assume sender is the target user for simplicity here, wait no, let's just send it to a topic based on a specific format. Wait, let's add a target field to ChatMessageDTO? No, minimal changes. We can just use the target username from the frontend and send directly to /topic/user.{target})
        // For simplicity, let's assume the frontend sends the target username in a specific way, or we can just broadcast to /topic/public for now. But requirement says "realtime admin-user chat".
        // Let's modify: admin sends to /topic/user.{targetUsername} directly via frontend, or we handle it here.
        // If frontend sends it here, we expect `chatMessage.getSender()` to be "Admin", but we need the target.
        // Let's just use SimpMessagingTemplate from the frontend or have the admin append the username.
        // Actually, the easiest way is for Admin to send messages to `/app/chat.adminReply` and put the target username in the sender field temporarily, or just let frontend send directly to `/topic/user.target`. 
        // We will just let the admin frontend send to `/topic/user.{targetUsername}` directly.
    }

    @GetMapping("/admin/chat")
    public String adminChatPage() {
        // Simple mapping for the Admin Chat UI
        return "adminChat";
    }
}
