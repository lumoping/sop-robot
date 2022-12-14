package com.majun.soprobot.lark.card;

import com.majun.soprobot.repo.po.Sop;
import com.majun.soprobot.repo.po.SopTodo;
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
        return FreeMarkerTemplateUtils.processTemplateIntoString(configuration.getTemplate("hello.ftl"), values);
    }


    public String searchPageCard(SearchPageCardValues values) throws IOException, TemplateException {
        return FreeMarkerTemplateUtils.processTemplateIntoString(configuration.getTemplate("searchPage.ftl"), values);
    }

    public String detailCard(DetailCardValues values) throws IOException, TemplateException {
        return FreeMarkerTemplateUtils.processTemplateIntoString(configuration.getTemplate("detail.ftl"), values);
    }


    public record HelloCardValues(String chatId, String folderToken) {

    }

    public record SearchPageCardValues(String chatId,
                                       List<Sop> sops,
                                       boolean all,
                                       String keyword
    ) {
    }

    public record DetailCardValues(Sop sop,
                                   List<SopTodo> todos) {
    }
}
