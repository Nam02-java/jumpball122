package com.example.Selenium.Package02;

import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;

public class ControlText_SloveCaptcha {
    private String text;

    private Boolean flag;
    private Update update;

    public ControlText_SloveCaptcha(Boolean flag, String text, Update update) {
        this.flag = flag;
        this.text = text;
        this.update = update;  // Initialize the update field
    }


    public void getText_Telegram() {
        Message message = update.getMessage();
        String text = message.getText();
        System.out.println(text);
        controlText(text, flag);

    }

    public static boolean controlText(String text, Boolean flag) {
        if (text == null || text.isEmpty() || text.length() != 6) {
            return false;
        }
        for (int i = 0; i < text.length(); i++) {
            if (!Character.isDigit(text.charAt(i))) {
                return false;
            }
        }
        flag = true;
        return true;
    }
}

class test {
    public static void main(String[] args) {
        isValidInput("asd");
    }

    public static boolean isValidInput(String input) {
        if (input == null || input.isEmpty() || input.length() != 6) {
            return false;
        }
        for (int i = 0; i < input.length(); i++) {
            if (!Character.isDigit(input.charAt(i))) {
                return false;
            }
        }
        System.out.println("true");
        return true;
    }
}