package com.majun.soprobot.lark.card;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.majun.soprobot.repo.po.Sop;
import com.majun.soprobot.repo.po.SopTodo;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.Arrays;

@SpringBootTest
class CardGeneratorTest {

    @Autowired
    private CardGenerator cardGenerator;

    @Test
    void searchPageCard() throws Exception {
        String s = cardGenerator.searchPageCard(
                new CardGenerator.SearchPageCardValues("123",
//                        Arrays.asList(new Sop(12, null, null, "http://123", "123", "描述123"), new Sop(123, null, null, "http://456", "456", "描述456")),
                        null,
                        false,
                        "关键词"));
        System.out.println(s);
    }


    @Test
    void detailPageTest() throws Exception {
        String s = cardGenerator.detailCard(new CardGenerator.DetailCardValues(
                new Sop(1, "chatId", "token", "http://url", "标题", "描述"),
                Arrays.asList(
                        new SopTodo(1, "token", "todo1"),
                        new SopTodo(2, "token", "todo2")
                )
        ));
        System.out.println(s);
    }

    private static String card = """
            {
              "config": {
                "wide_screen_mode": true
              },
              "header": {
                "title": {
                  "tag": "plain_text",
                  "content": "Standard Operating Procedure"
                },
                "template": "blue"
              },
              "elements": [
                {
                  "tag": "markdown",
                  "content": "*根据关键词【这是第一个sop文档】查询到以下SOP：*"
                },
                {
                  "tag": "hr"
                },
                {
                  "tag": "markdown",
                  "content": ""
                },
                {
                  "tag": "action",
                  "actions": [
                    {
                      "tag": "button",
                      "text": {
                        "tag": "plain_text",
                        "content": "查看全部"
                      },
                      "type": "primary",
                      "value": {
                        "chat_id": "123",
                        "type": "SEARCH_ALL"
                      }
                    }
                  ]
                },
                {
                  "tag": "hr"
                },
                {
                  "tag": "note",
                  "elements": [
                    {
                      "tag": "plain_text",
                      "content": "标准操作程序（SOP）是一套详细的分步说明，描述如何执行任何给定过程"
                    }
                  ]
                }
              ]
            }
            """;

    @Test
    void test() throws Exception {
        JsonNode jsonNode = new ObjectMapper().readTree(card);
        System.out.println(jsonNode);
    }
}