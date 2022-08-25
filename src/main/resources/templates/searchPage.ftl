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
   <#if all()>
       {
       "tag": "markdown",
       "content": "以下是所有的SOP文档"
       },
       <#else>
           {
           "tag": "markdown",
           "content": "*根据关键词【${keyword()}】查询到以下SOP：*"
           },
   </#if>
    {
        "tag": "hr"
    },
   <#if sops()?? && (sops()?size > 0)>
       {
            "tag": "action",
            "actions": [
                <#list sops() as sop>
                    {
                        "tag": "button",
                        "text": {
                            "tag": "plain_text",
                            "content": "${sop.title()}"
                        },
                        "type": "primary",
                        "value": {
                            "type": "DETAIL",
                            "sopId": ${sop.id()},
                            "chatId": "${sop.chatId()}"
                        }
                    }
                    <#if sop_has_next>
                        ,
                    </#if>
                </#list>
            ]
       },
       {
            "tag": "hr"
       },
       <#else>
           {
                "tag": "markdown",
                "content": "**暂无SOP**"
           },
   </#if>
    {
      "tag": "action",
      "actions": [
        {
          "tag": "button",
          "text": {
            "tag": "plain_text",
            "content": "查看全部"
          },
          "type": "default",
          "value": {
            "chat_id": "${chatId()}",
            "type": "SEARCH_ALL"
          }
        },
        {
          "tag": "button",
          "text": {
             "tag": "plain_text",
             "content": "创建SOP文档"
          },
          "type": "default",
          "value": {
             "chat_id": "${chatId()}",
             "type": "CREATE_FILE"
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