package com.xia.agentprogram.app;

import com.xia.agentprogram.advisor.MyLoggerAdvisor;
import com.xia.agentprogram.rag.LoveAppRagCustomAdvisorFactory;
import com.xia.agentprogram.rag.QueryRewriter;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.client.advisor.api.Advisor;
import org.springframework.ai.chat.client.advisor.vectorstore.QuestionAnswerAdvisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.memory.InMemoryChatMemoryRepository;
import org.springframework.ai.chat.memory.MessageWindowChatMemory;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.tool.ToolCallback;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@Slf4j
public class LoveApp {

    private final ChatClient chatClient;

    private static final String SYSTEM_PROMPT = "扮演深耕恋爱心理领域的专家。开场向用户表明身份，告知用户可倾诉恋爱难题。" +
            "围绕单身、恋爱、已婚三种状态提问：单身状态询问社交圈拓展及追求心仪对象的困扰；" +
            "恋爱状态询问沟通、习惯差异引发的矛盾；已婚状态询问家庭责任与亲属关系处理的问题。" +
            "引导用户详述事情经过、对方反应及自身想法，以便给出专属解决方案。";

    public LoveApp(ChatModel dashScopeChatModel) {
        // 内存存储聊天记忆
        ChatMemory chatMemory = MessageWindowChatMemory.builder()
                .chatMemoryRepository(new InMemoryChatMemoryRepository())
                .maxMessages(10)
                .build();
        // 文件存储聊天记忆
//        String fileDir = System.getProperty("user.dir") + "/chat-memory";
//        ChatMemory chatMemory = new FileBasedChatMemory(fileDir);

        this.chatClient = ChatClient.builder(dashScopeChatModel)
                .defaultSystem(SYSTEM_PROMPT)
                .defaultAdvisors(MessageChatMemoryAdvisor.builder(chatMemory).build(),
                        // Spring Ai 日志拦截器
//                        new SimpleLoggerAdvisor(),
                        // 自定义的日志 Advisor
                        // 重读 Advisor
//                        new ReReadingAdvisor(),
                        // 自定义的日志 Advisor
                        new MyLoggerAdvisor())
                .build();
    }


    /**
     * 使用聊天模型生成回复，支持多轮对话
     *
     * @param message 用户问题
     * @param chatId  聊天ID
     * @return 回复内容
     */
    public String useChat(String message, String chatId) {
        ChatResponse response = chatClient.prompt()
                .user(message)
                .advisors(spec -> spec.param(ChatMemory.CONVERSATION_ID, chatId))
                .call()
                .chatResponse();

        String content = null;
        if (response != null) {
            content = response.getResult().getOutput().getText();
        }
        log.info("response: {}", content);
        return content;
    }


    record LoveReport(String title, List<String> suggestions) {
    }

    /**
     * 使用聊天模型生成回复，结构化输出，返回结果为 LoveReport 类型
     * @param message
     * @param chatId
     * @return
     */
    public LoveReport useChatWithReport(String message, String chatId) {
        LoveReport loveReport = chatClient.prompt()
                .user(message)
                .system(SYSTEM_PROMPT + "每次对话后都要生成恋爱结果，标题为{用户名}的恋爱报告，内容为建议列表")
                .advisors(spec -> spec.param(ChatMemory.CONVERSATION_ID, chatId))
                .call()
                .entity(LoveReport.class);

        log.info("loveReport: {}", loveReport);

        return loveReport;
    }

    @Resource
    private VectorStore loveAppVectorStore;

    /**
     * 使用聊天模型生成回复，使用Rag知识库回答
     * @param message
     * @param chatId
     * @return
     */

    public String useChatWithRag(String message, String chatId) {
        ChatResponse response = chatClient.prompt()
                .user(message)
                .advisors(spec -> spec.param(ChatMemory.CONVERSATION_ID, chatId))
                .advisors(QuestionAnswerAdvisor.builder(loveAppVectorStore).build())
                .call()
                .chatResponse();

        String content = null;
        if (response != null) {
            content = response.getResult().getOutput().getText();
        }
        log.info("response: {}", content);
        return content;
    }

    @Resource
    private Advisor loveAppRagCloudAdvisor;


    /**
     * 使用聊天模型生成回复，使用Cloud Rag知识库回答
     * @param message
     * @param chatId
     * @return
     */

    public String useChatWithCloudRag(String message, String chatId) {
        ChatResponse response = chatClient.prompt()
                .user(message)
                .advisors(spec -> spec.param(ChatMemory.CONVERSATION_ID, chatId))
                .advisors(loveAppRagCloudAdvisor)
                .call()
                .chatResponse();

        String content = null;
        if (response != null) {
            content = response.getResult().getOutput().getText();
        }
        log.info("response: {}", content);
        return content;
    }

    @Resource
    private VectorStore pgVectorVectorStore;

    /**
     * 使用聊天模型生成回复，使用PgVector数据库回答
     * @param message
     * @param chatId
     * @return
     */

    public String useChatWithPgVector(String message, String chatId) {
        ChatResponse response = chatClient.prompt()
                .user(message)
                .advisors(spec -> spec.param(ChatMemory.CONVERSATION_ID, chatId))
                .advisors(QuestionAnswerAdvisor.builder(pgVectorVectorStore).build())
                .call()
                .chatResponse();

        String content = null;
        if (response != null) {
            content = response.getResult().getOutput().getText();
        }
        log.info("response: {}", content);
        return content;
    }


    @Resource
    private QueryRewriter queryRewriter;

    /**
     * 使用查询重写器
     *
     * @param message 用户问题
     * @param chatId  聊天ID
     * @return 回复内容
     */
    public String useChatWithRetriever(String message, String chatId) {
        // 使用查询重写器
        String rewriteMessage = queryRewriter.doQueryRewrite(message);

        ChatResponse response = chatClient.prompt()
                .user(rewriteMessage)
                .advisors(spec -> spec.param(ChatMemory.CONVERSATION_ID, chatId))
                .call()
                .chatResponse();

        String content = null;
        if (response != null) {
            content = response.getResult().getOutput().getText();
        }
        log.info("response: {}", content);
        return content;
    }


    /**
     * 使用自定义的Advisor工厂,上下文查询增强器,用于根据状态过滤文档
     *
     * @param message 用户问题
     * @param chatId  聊天ID
     * @return 回复内容
     */
    public String useChatWithCustomAdvisorFactory(String message, String chatId) {
        ChatResponse response = chatClient.prompt()
                .user(message)
                .advisors(spec -> spec.param(ChatMemory.CONVERSATION_ID, chatId))
                .advisors(LoveAppRagCustomAdvisorFactory.createLoveAppRagCustomAdvisor(loveAppVectorStore, "单身"))
                .call()
                .chatResponse();

        String content = null;
        if (response != null) {
            content = response.getResult().getOutput().getText();
        }
        log.info("response: {}", content);
        return content;
    }

    /**
     * 使用工具
     *
     * @param message 用户问题
     * @param chatId  聊天ID
     * @return 回复内容
     */

    @Resource
    private ToolCallback[] allTools;

    public String doChatWithTools(String message, String chatId) {
        ChatResponse response = chatClient.prompt()
                .user(message)
                .advisors(spec -> spec.param(ChatMemory.CONVERSATION_ID, chatId))
                .toolCallbacks(allTools)
                .call()
                .chatResponse();

        String content = null;
        if (response != null) {
            content = response.getResult().getOutput().getText();
        }
        log.info("response: {}", content);
        return content;
    }

}
