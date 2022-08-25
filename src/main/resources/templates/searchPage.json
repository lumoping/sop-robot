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
    <#if match()>
    {
      "tag": "markdown",
      "content": "*根据关键词【${keyword()}】查询到以下SOP：*"
    },
    {
      "tag": "hr"
    },
    {
      "tag": "markdown",
      "content": "<#list sops() as sop>[${sop.title()}](${sop.docUrl()})\n</#list>"
    },
    {
      "tag": "hr"
    },
    <#else>
    {
      "tag": "markdown",
      "content": "**根据关键词未查询到SOP**"
    },
    {
      "tag": "hr"
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
          "type": "primary",
          "value": {
            "chat_id": "${chatId()}",
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