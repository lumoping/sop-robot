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
      "content": "**${sop().title()}**"
    },
    {
      "tag": "hr"
    },
    {
      "tag": "markdown",
      "content": "**简介：**${sop().description()}"
    },
    {
      "tag": "hr"
    },
    {
      "tag": "markdown",
      "content": "**Steps:**\n<#list todos() as todo>  ${todo_index + 1}. ${todo.description()}\n</#list>"
    },
    {
      "tag": "hr"
    },
    {
      "tag": "action",
      "actions": [
        <#if todos()?? && (todos()?size > 0)>
            {
                "tag": "button",
                "text": {
                    "tag": "plain_text",
                    "content": "开始执行"
                },
                "type": "primary",
                "value": {
                    "sopId": "${sop().id()}",
                    "type": "START_TODO"
                }
            },
        </#if>
        {
          "tag": "button",
          "text": {
            "tag": "plain_text",
            "content": "查看云文档"
          },
          "url": "${sop().docUrl()}",
          "type": "default"
        }
      ]
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