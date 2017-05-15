netty http 上传组件

部署相关修改处：
1.监听端口：
netty-server.properties中的tcp.port
2.数据库配置：
jdbc.properties
3.临时文件保存：
config.properties中的tempdir
4.上传组件唯一标识
config.properties中的process.num（在部署多台时，该标识必须各台之间不能出现相同）

接口定义：
访问地址：http:\\{服务器地址}:{在cofig文件夹中netty-server.properties配置的端口}
访问方式：POST
参数说明：
content处:
fileKey			：上传OSS的名称（可填）
callbackUri		：回调uri（可填,如果有,则必须有接收该请求的服务器）
fileMd5			：文件MD5值（必填）
yunType			：云类型(oss：阿里云，bos：百度云)
***_bucket		：想要上传到的云上bucket名称（必须有一个，如果***为oss时上传到阿里云，为bos时上传到百度云，此次***必须为yunType中的值）
***_accessid	：想要上传到的云上accessid（必须有一个，如果***为oss时上传到阿里云，为bos时上传到百度云，此次***必须为yunType中的值）
***_accesskey	：想要上传到的云上accesskey（必须有一个，如果***为oss时上传到阿里云，为bos时上传到百度云，此次***必须为yunType中的值）
header处（可不传）：
所有用户信息表头相关定义以[x-oss-meta-]开始

启动相关：
windows:双击目录下startup.bat文件进行启动。
Linux：执行startup.sh

停止相关：
Linux: 执行stop.sh

业务：
1.上传
1.1如果fileKey参数未传，则OSS上显示的文件名为上传时文件的名字
1.2如果fileKey参数有传，则OSS上显示的文件名为所传的fileKey
1.3如果fileMD5参数与上传到OSS云上的所得MD5值不一致，则重试（最多只重试三次）
1.3.1如果重试三次失败，这会发短信和邮件通知配置文件中的管理员
1.4如果fileMD5参数与上传到OSS云上的所得MD5值一致，则上传成功
2.回调（如果回调uri存在）
2.1上传结果信息可通过该uri发送给接收该uri的服务器、
2.2回调中出现异常会重试（最多重试三次）
2.2.1如果重试三次失败，这会发短信和邮件通知配置文件中的管理员
3.服务器重启时如果有任务未执行，则会继续执行那些任务
4.如果任务表中某个一直处于执行中的状态超过task.properties文件中的statustask.perion.seconds设置的值则会重新执行该任务
