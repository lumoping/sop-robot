package com.majun.soprobot.message;

import org.springframework.kafka.annotation.KafkaListener;

public class LarkMessageConsumer {

    @KafkaListener(topics = "messageReceive")
    void messageReceive(){

    }



    void docEdit(){

    }

    void docDelete(){

    }
}
