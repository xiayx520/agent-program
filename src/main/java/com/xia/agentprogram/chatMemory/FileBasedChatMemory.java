package com.xia.agentprogram.chatMemory;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import org.objenesis.strategy.StdInstantiatorStrategy;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.messages.Message;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.List;

public class FileBasedChatMemory implements ChatMemory {

    private final String BASE_DIR;
    private static final Kryo kryo = new Kryo();

    static {
        kryo.setRegistrationRequired(false);
        kryo.setInstantiatorStrategy(new StdInstantiatorStrategy());
    }

    public FileBasedChatMemory(String baseDir) {
        this.BASE_DIR = baseDir;
        File baseDirFile = new File(baseDir);
        if (!baseDirFile.exists()) {
            baseDirFile.mkdirs();
        }
    }

    @Override
    public void add(String conversationId, List<Message> messages) {
        List<Message> conversationMessages = getOrCreateConversation(conversationId);
        conversationMessages.addAll(messages);
        saveConversation(conversationId, conversationMessages);
    }

    private void saveConversation(String conversationId, List<Message> conversationMessages) {
        File conversationFile = conversationFile(conversationId);
        try (Output output = new Output(new FileOutputStream(conversationFile))) {
            kryo.writeObject(output, conversationMessages);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    private List<Message> getOrCreateConversation(String conversationId) {
        File conversationFile = conversationFile(conversationId);
        List<Message> messages = new ArrayList<>();
        if (conversationFile.exists()) {
            try (Input input = new Input(new FileInputStream(conversationFile))) {
                messages = kryo.readObject(input, ArrayList.class);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
        }
        return messages;
    }

    @Override
    public List<Message> get(String conversationId) {
        return getOrCreateConversation(conversationId);
    }

    @Override
    public void clear(String conversationId) {
        File conversationFile = conversationFile(conversationId);
        if (conversationFile.exists()) {
            conversationFile.delete();
        }

    }

    private File conversationFile(String conversationId) {
        return new File(BASE_DIR, conversationId + ".kryo");
    }


}
