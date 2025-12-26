// BillingPlan.java

package com.chatbot.modules.tenant.billing.model;

public enum BillingPlan {

    FREE(0, 100, 1),      // 100 messages/month, 1 chatbot

    BASIC(9.99, 1000, 3), // 1000 messages/month, 3 chatbots

    PREMIUM(29.99, 5000, 10), // 5000 messages/month, 10 chatbots

    ENTERPRISE(99.99, -1, -1); // Unlimited

    private final double price;

    private final int maxMessages;

    private final int maxChatbots;

    BillingPlan(double price, int maxMessages, int maxChatbots) {

        this.price = price;

        this.maxMessages = maxMessages;

        this.maxChatbots = maxChatbots;

    }

    // Getters

    public double getPrice() { return price; }

    public int getMaxMessages() { return maxMessages; }

    public int getMaxChatbots() { return maxChatbots; }

}

