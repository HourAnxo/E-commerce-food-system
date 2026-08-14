package com.example.E_commerce_food_system.Telegram.handler;

public interface CommandHandler {

    /** The command text, lowercase, including the slash. Example: "/track" */
    String command();

    /** Shown by /help. Example: "check the status of your order" */
    String description();

    void handle(CommandContext ctx);
}
