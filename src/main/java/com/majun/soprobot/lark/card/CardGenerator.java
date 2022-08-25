package com.majun.soprobot.lark.card;

import com.majun.soprobot.repo.po.Sop;
import freemarker.template.Configuration;
import freemarker.template.TemplateException;
import org.springframework.stereotype.Component;
import org.springframework.ui.freemarker.FreeMarkerTemplateUtils;

import java.io.IOException;
import java.util.List;

@Component
public class CardGenerator {


    private final Configuration configuration;

    public CardGenerator(Configuration configuration) {
        this.configuration = configuration;
    }


    public String helloCard(HelloCardValues values) throws IOException, TemplateException {
        return FreeMarkerTemplateUtils.processTemplateIntoString(configuration.getTemplate("hello.json"), values);
    }


    public String searchPageCard(SearchPageCardValues values) throws IOException, TemplateException {
        return FreeMarkerTemplateUtils.processTemplateIntoString(configuration.getTemplate("searchPage.json"), values);
    }


    public record HelloCardValues(String chatId, String folderToken) {

    }

    public record SearchPageCardValues(String chatId, String keyword, List<Sop> sops, boolean match) {

    }
}
