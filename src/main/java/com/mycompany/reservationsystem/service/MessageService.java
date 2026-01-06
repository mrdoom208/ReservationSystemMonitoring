package com.mycompany.reservationsystem.service;

import com.mycompany.reservationsystem.model.Message;
import com.mycompany.reservationsystem.repository.MessageRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class MessageService {

    private final MessageRepository messageRepository;

    public MessageService(MessageRepository messageRepository) {
        this.messageRepository = messageRepository;
    }


    // Save a new message
    public Message saveMessage(Long id, String label, String details) {

        Message message;

        if (id == null) {
            // CREATE
            message = new Message();
            message.setCreatedAt(LocalDateTime.now());
        } else {
            // UPDATE
            message = messageRepository.findById(id)
                    .orElseThrow(() -> new IllegalArgumentException("Message not found"));
            message.setUpdatedAt(LocalDateTime.now());
        }

        message.setMessageLabel(label);
        message.setMessageDetails(details);

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
    public Optional<Message> findByLabel(String keyword) {
        return messageRepository.findByMessageLabelIgnoreCase(keyword);
    }

    @Transactional
    public void createIfMissing(String messageLabel) {
        messageRepository.findByMessageLabelIgnoreCase(messageLabel)
                .orElseGet(() -> {
                    Message msg = new Message();
                    msg.setMessageLabel(messageLabel);
                    msg.setMessageDetails("This is an auto-generated message for: " + messageLabel);
                    msg.setDefault(true); // mark as default
                    msg.setCreatedAt(LocalDateTime.now());
                    msg.setUpdatedAt(LocalDateTime.now());

                    System.out.println("Created default message: " + messageLabel);
                    return messageRepository.save(msg);
                });
    }

    @Transactional
    public void DefaultMessage() {
        if (messageRepository.count() == 0) {
            Message defaultMessage = new Message();
            defaultMessage.setMessageLabel("Default Message");
            defaultMessage.setMessageDetails("This is an auto-generated default message.");
            defaultMessage.setDefault(true);  // mark as default so it cannot be deleted
            defaultMessage.setCreatedAt(java.time.LocalDateTime.now());
            defaultMessage.setUpdatedAt(java.time.LocalDateTime.now());

            messageRepository.save(defaultMessage);
            System.out.println("Default message created automatically.");
        }
    }

    // Delete message by ID
    public void deleteMessage(Long id) {
        messageRepository.findById(id).ifPresent(msg -> {
            if (msg.isDefault()) {
                throw new IllegalArgumentException("The selected message is currently in use.");
            }
            messageRepository.delete(msg);
        });
    }
}
