package com.example.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.example.entity.*;
import com.example.service.*;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * TODO: You will need to write your own endpoints and handlers for your controller using Spring. The endpoints you will need can be
 * found in readme.md as well as the test cases. You be required to use the @GET/POST/PUT/DELETE/etc Mapping annotations
 * where applicable as well as the @ResponseBody and @PathVariable annotations. You should
 * refer to prior mini-project labs and lecture materials for guidance on how a controller may be built.
 */

 @RestController
public class SocialMediaController {

    @Autowired
    private AccountService accountService;

    @Autowired
    private MessageService messageService;

    @Autowired
    public SocialMediaController(AccountService accountService, MessageService messageService){
        this.accountService = accountService;
        this.messageService = messageService;
    }

    @PostMapping("/register")
    public @ResponseBody ResponseEntity<?> register(@RequestBody Account account){
        try {    
            Account checkAccount = accountService.getAccountByUsername(account.getUsername());
            if(checkAccount == null){
                return new ResponseEntity<>(accountService.persistAccount(account), HttpStatus.OK);
            } else {
                return ResponseEntity.status(HttpStatus.CONFLICT).body("Username already exists");
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Failed to register");
        }     
    }

    @PostMapping("/login")
    public @ResponseBody ResponseEntity<?> login(@RequestBody Account account){
        try {
            Account checkAccount = accountService.getAccountByUsername(account.getUsername());
            if(checkAccount != null && account.getPassword().equals(checkAccount.getPassword())){
                return new ResponseEntity<>(checkAccount, HttpStatus.OK);
            }
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Unauthorized");

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Failed to login");
        }
    }

    @PostMapping("/messages")
    public @ResponseBody ResponseEntity<?> createMessage(@RequestBody Message message){
        Account checkAccount = accountService.getAccountById(message.getPostedBy());
        int textLength = message.getMessageText().length();
        if(checkAccount != null && (textLength <= 255 && !message.getMessageText().isBlank())){
                return new ResponseEntity<>(messageService.persistMessage(message), HttpStatus.OK);
        }
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Failed to create message");
    }

    @GetMapping("/messages")
    public @ResponseBody ResponseEntity<List<Message>> getAllMessages(){
        return new ResponseEntity<>(messageService.getAllMessages(), HttpStatus.OK);
    }

    @GetMapping("/messages/{messageId}")
    public @ResponseBody ResponseEntity<Message> getMessageByID(@PathVariable int messageId){
        return new ResponseEntity<>(messageService.getMessageById(messageId), HttpStatus.OK);
    }

    @DeleteMapping("/messages/{messageId}")
    public @ResponseBody ResponseEntity<?> deleteMessageByID(@PathVariable int messageId){
        Message checkMessage = messageService.getMessageById(messageId);
        if(checkMessage == null){
            return new ResponseEntity<>(null , HttpStatus.OK);
        }
        return new ResponseEntity<>(messageService.deleteMessage(messageId), HttpStatus.OK);
    }
    
    @PatchMapping("/messages/{messageId}")
    public @ResponseBody ResponseEntity<?> updateMessageByID(@PathVariable int messageId, @RequestBody String messageText){        
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            String extractedMessage = objectMapper.readTree(messageText).get("messageText").asText();
            Message checkMessage = messageService.getMessageById(messageId);
            if(checkMessage != null && !extractedMessage.isEmpty() && extractedMessage.length() <= 255){
                
                return new ResponseEntity<>(messageService.updateMessage(messageId, extractedMessage), HttpStatus.OK);
            }
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Failed to update message");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Invalid request format");
        }
        
    }

    @GetMapping("/accounts/{accountId}/messages")
    public @ResponseBody ResponseEntity<?> getAllMessagesByAccountId(@PathVariable int accountId){
        Account checkAccount = accountService.getAccountById(accountId);
        if(checkAccount != null){
            return new ResponseEntity<>(messageService.getAllMessagesByAccountId(accountId), HttpStatus.OK);
        }
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Failed to get all messages from account ID."); 
    }

}
