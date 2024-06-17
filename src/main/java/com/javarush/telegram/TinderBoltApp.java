package com.javarush.telegram;

import com.javarush.telegram.ChatGPTService;
import com.javarush.telegram.DialogMode;
import com.javarush.telegram.MultiSessionTelegramBot;
import com.javarush.telegram.UserInfo;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.api.objects.*;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.ArrayList;

public class TinderBoltApp extends MultiSessionTelegramBot {
    public static final String TELEGRAM_BOT_NAME = "Chat_GPT_JR_bot";
    public static final String TELEGRAM_BOT_TOKEN = loadToken("telegram");
    public static final String OPEN_AI_TOKEN = loadToken("openAI");

    private ChatGPTService chatGPT = new ChatGPTService(OPEN_AI_TOKEN);
    private DialogMode currentMode = null;
    private ArrayList<String> list = new ArrayList<>();

    private UserInfo me;
    private UserInfo target;
    private int questionCount;

    public TinderBoltApp() {
        super(TELEGRAM_BOT_NAME, TELEGRAM_BOT_TOKEN);
    }

    @Override
    public void onUpdateEventReceived(Update update) {
        String message = getMessageText();
        if (message.equals("/start")) {

            currentMode = DialogMode.MAIN;
            sendPhotoMessage("main");
            String text = loadMessage("main");
            sendTextMessage("Hello, it's your personal chat-bot");
            sendTextMessage(text);

            showMainMenu("Start", "/start",
                    "Profile", "/profile",
                    "Opener", "/opener",
                    "Message", "/message",
                    "Date", "/date",
                    "GPT", "/gpt");
            return;
        }

        if (message.equals("/gpt")) {
            currentMode = DialogMode.GPT;
            sendPhotoMessage("gpt");
            String text = loadMessage("gpt");
            sendTextMessage(text);
            return;
        }

        if (currentMode == DialogMode.GPT && !isMessageCommand()) {
            String prompt = loadPrompt("gpt");
            Message msg = sendTextMessage("Waiting for GPT to respond...");
            String answer = chatGPT.sendMessage(prompt, message);
            updateTextMessage(msg, answer);
            return;
        }

        if (message.equals("/date")) {
            currentMode = DialogMode.DATE;
            sendPhotoMessage("date");
            String text = loadMessage("date");
            sendTextButtonsMessage(text,
                    "Ариана Гранде", "date_grande",
                    "Марго Робби", "date_robbie",
                    "Зендея", "date_zendaya",
                    "Райан Гослинг", "date_gosling",
                    "Том Харди", "date_hardy");
            return;
        }

        if (currentMode == DialogMode.DATE && !isMessageCommand()) {
            String query = getCallbackQueryButtonKey();

            if (query.startsWith("date_")) {
                sendPhotoMessage(query);
                String prompt = loadPrompt(query);
                chatGPT.setPrompt(prompt);
                return;
            }

            Message msg = sendTextMessage("Waiting for GPT to respond...");
            String answer = chatGPT.addMessage(message);
            updateTextMessage(msg, answer);
            return;
        }

        if (message.equals("/message")) {
            currentMode = DialogMode.MESSAGE;
            sendPhotoMessage("message");
            sendTextButtonsMessage("Send your text history",
                    "Next message", "message_next",
                    "Invite to date", "message_date");
            return;
        }

        if (currentMode == DialogMode.MESSAGE && !isMessageCommand()) {
            String querty = getCallbackQueryButtonKey();
            if (querty.startsWith("message_")) {
                String prompt = loadPrompt(querty);
                String userChatHistory = String.join("\n\n", list);
                Message msg = sendTextMessage("Waiting for GPT to respond...");
                String answer = chatGPT.sendMessage(prompt, userChatHistory);
                updateTextMessage(msg, answer);
                return;
            }

            list.add(message);
            return;
        }

        if (message.equals("/profile")) {
            currentMode = DialogMode.PROFILE;
            sendPhotoMessage("profile");
            me = new UserInfo();
            questionCount = 0;
            sendTextMessage("Enter information about yourself: \n What's your name?");
            return;
        }

        if (currentMode == DialogMode.PROFILE) {
            switch (questionCount) {
                case 0:
                    me.name = message;
                    questionCount = questionCount + 1;
                    sendTextMessage("What is your sex?");
                    return;
                case 1:
                    me.sex = message;
                    questionCount = questionCount + 1;
                    sendTextMessage("What is your age?");
                    return;
                case 2:
                    me.age = message;
                    questionCount = questionCount + 1;
                    sendTextMessage("What is your city?");
                    return;
                case 3:
                    me.city = message;
                    questionCount = questionCount + 1;
                    sendTextMessage("What is your occupation?");
                    return;
                case 4:
                    me.occupation = message;
                    questionCount = questionCount + 1;
                    sendTextMessage("What is your hobby?");
                    return;
                case 5:
                    me.hobby = message;
                    questionCount = questionCount + 1;
                    sendTextMessage("How handsome are you? 1-10.");
                    return;
                case 6:
                    me.handsome = message;
                    questionCount = questionCount + 1;
                    sendTextMessage("What's your wealth?");
                    return;
                case 7:
                    me.wealth = message;
                    questionCount = questionCount + 1;
                    sendTextMessage("What annoys you?");
                    return;
                case 8:
                    me.annoys = message;
                    questionCount = questionCount + 1;
                    sendTextMessage("What is your goals for this profile?");
                    return;
                case 9:
                    me.goals = message;
                    questionCount = questionCount + 1;

                    String aboutMyself = me.toString();
                    String prompt = loadPrompt("profile");
                    Message msg = sendTextMessage("Waiting for GPT to respond...");
                    String answer = chatGPT.sendMessage(prompt, aboutMyself);
                    updateTextMessage(msg, answer);
                    return;

            }


        }

        if (message.equals("/opener")) {
            currentMode = DialogMode.OPENER;
            sendPhotoMessage("opener");

            target = new UserInfo();
            questionCount = 0;
            sendTextMessage("Send info about human you want to talk to: \n Name?");
            return;
        }

        if (currentMode == DialogMode.OPENER && !isMessageCommand()) {
            switch (questionCount) {
                case 0:
                    target.name = message;
                    questionCount = questionCount + 1;
                    sendTextMessage("What sex?");
                    return;
                case 1:
                    target.sex = message;
                    questionCount = questionCount + 1;
                    sendTextMessage("What age?");
                    return;
                case 2:
                    target.age = message;
                    questionCount = questionCount + 1;
                    sendTextMessage("What city?");
                    return;
                case 3:
                    target.city = message;
                    questionCount = questionCount + 1;
                    sendTextMessage("What occupation?");
                    return;
                case 4:
                    target.occupation = message;
                    questionCount = questionCount + 1;
                    sendTextMessage("What hobby?");
                    return;
                case 5:
                    target.hobby = message;
                    questionCount = questionCount + 1;
                    sendTextMessage("How handsome? 1-10.");
                    return;
                case 6:
                    target.handsome = message;
                    questionCount = questionCount + 1;
                    sendTextMessage("What's wealth?");
                    return;
                case 7:
                    target.wealth = message;
                    questionCount = questionCount + 1;
                    sendTextMessage("What annoys it?");
                    return;
                case 8:
                    target.annoys = message;
                    questionCount = questionCount + 1;
                    sendTextMessage("What's its goals for this profile?");
                    return;
                case 9:
                    target.goals = message;
                    questionCount = questionCount + 1;

                    String aboutFriend = target.toString();
                    String prompt = loadPrompt("opener");
                    Message msg = sendTextMessage("Waiting for GPT to respond...");
                    String answer = chatGPT.sendMessage(prompt, aboutFriend);
                    updateTextMessage(msg, answer);
                    return;

            }
            return;
        }


        sendTextMessage("*Hello world!*");
        sendTextMessage("_Hello world!_");

        sendTextMessage("You said " + message);
        sendTextButtonsMessage("Choose mode of operation:",
                "Start", "start",
                "Stop", "stop");
    }

    public static void main(String[] args) throws TelegramApiException {
        TelegramBotsApi telegramBotsApi = new TelegramBotsApi(DefaultBotSession.class);
        telegramBotsApi.registerBot(new TinderBoltApp());
    }
}
