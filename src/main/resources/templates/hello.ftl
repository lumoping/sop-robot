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
      "content": "[标准操作程序（SOP）](https://workflowautomation.net/blog/standard-operating-procedure-sop)是一套详细的分步说明，描述如何执行任何给定过程"
    },
    {
      "tag": "hr"
    },
    {
      "tag": "markdown",
      "content": "🥳**开始创建SOP文档吧**"
    },
    {
      "tag": "action",
      "actions": [
        {
          "tag": "button",
          "text": {
            "tag": "plain_text",
            "content": "创建SOP文档"
          },
          "type": "primary",
          "value": {
            "chat_id": "${chatId()}",
            "type": "CREATE_FILE",
            "folder_token": "${folderToken()}"
          }
        }
      ]
    }
  ]
}