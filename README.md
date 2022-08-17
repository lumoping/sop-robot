# 简介
sop-robot是一个提供SOP（Standard Operating Procedure）服务的飞书机器人。

点击机器人新建SOP按钮，机器人新建云文档并订阅文档的编辑、删除，在赋予用户管理权限后返回文档地址。

# 使用到的API列表
- 凭证
  - [获取 tenant_access_token](https://open.feishu.cn/document/ukTMukTMukTM/ukDNz4SO0MjL5QzM/auth-v3/auth/tenant_access_token_internal)
- 云文档
  - [获取我的空间（root folder）元信息](https://open.feishu.cn/document/ukTMukTMukTM/ugTNzUjL4UzM14CO1MTN/get-root-folder-meta)
  - [获取文件夹下的清单](https://open.feishu.cn/document/uAjLw4CM/ukTMukTMukTM/reference/drive-v1/file/list)
  - [新建文件夹](https://open.feishu.cn/document/uAjLw4CM/ukTMukTMukTM/reference/drive-v1/file/create_folder)
  - [创建文档](https://open.feishu.cn/document/ukTMukTMukTM/ugDM2YjL4AjN24COwYjN)
  - [增加协作者权限](https://open.feishu.cn/document/uAjLw4CM/ukTMukTMukTM/reference/drive-v1/permission-member/create)
  - [判断当前用户对某文档是否有某权限](https://open.feishu.cn/document/ukTMukTMukTM/uYzN3UjL2czN14iN3cTN)
  - [获取文档所有块](https://open.feishu.cn/document/ukTMukTMukTM/uUDN04SN0QjL1QDN/document-docx/docx-v1/document-block/list)
  - [获取块](https://open.feishu.cn/document/ukTMukTMukTM/uUDN04SN0QjL1QDN/document-docx/docx-v1/document-block/get)
- 消息与群组
  - [发送消息](https://open.feishu.cn/document/uAjLw4CM/ukTMukTMukTM/reference/im-v1/message/create)
- 任务
  - [创建任务](https://open.feishu.cn/open-apis/task/v1/tasks)
