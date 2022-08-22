package com.majun.soprobot.lark.card;

import freemarker.template.Configuration;
import freemarker.template.TemplateException;
import org.springframework.stereotype.Component;
import org.springframework.ui.freemarker.FreeMarkerTemplateUtils;

import java.io.IOException;

@Component
public class CardGenerator {


    private final Configuration configuration;

    public CardGenerator(Configuration configuration) {
        this.configuration = configuration;
    }


    public String helloCard(HelloCardValues values) throws IOException, TemplateException {
        return FreeMarkerTemplateUtils.processTemplateIntoString(configuration.getTemplate("hello.json"), values);
    }


    public record HelloCardValues(String chatId, String folderToken) {

    }

}
