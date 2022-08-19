package com.majun.soprobot;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.embedded.netty.NettyReactiveWebServerFactory;
import org.springframework.boot.web.server.WebServerFactoryCustomizer;
import org.springframework.stereotype.Component;

@SpringBootApplication
public class SopRobotApplication {

    public static void main(String[] args) {
        SpringApplication.run(SopRobotApplication.class, args);
    }


    @Component
    public static class MyNettyWebServerCustomizer
            implements WebServerFactoryCustomizer<NettyReactiveWebServerFactory> {
        @Override
        public void customize(NettyReactiveWebServerFactory factory) {
            factory.addServerCustomizers(httpServer -> httpServer.wiretap(true));
        }
    }
}


