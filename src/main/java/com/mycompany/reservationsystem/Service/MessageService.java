package com.mycompany.reservationsystem.Service;

import com.mycompany.reservationsystem.model.Message;
import com.mycompany.reservationsystem.repository.MessageRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class MessageService {

    private final MessageRepository messageRepository;

    public MessageService(MessageRepository messageRepository) {
        this.messageRepository = messageRepository;
    }

    // Save a new message
    public Message saveMessage(String label, String details) {
        Message message = messageRepository.findByMessageLabelIgnoreCase(label).orElseGet(() -> new Message(label, details));

        // UPDATE fields
        message.setMessageLabel(label);
        message.setMessageDetails(details);
        message.setUpdatedAt(LocalDateTime.now());

        return messageRepository.save(message);
    }

    // Get all messages
    public List<Message> getAllMessages() {
        return messageRepository.findAll();
    }




    // Search messages by label
    public List<Message> searchByLabel(String keyword) {
        return messageRepository.findByMessageLabelContainingIgnoreCase(keyword);
    }

    // Delete message by ID
    public void deleteMessage(Long id) {
        messageRepository.deleteById(id);
    }
}
