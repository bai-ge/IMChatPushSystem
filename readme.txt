1.端口
tcp 12056
cmd 12058
udp 12059

程序的入口有
无窗口com.baige.linux.Daemon
可多次启动，如果服务器已经启动则进入命令行界面，否则启动控制台
控制台命令有
exit 退出
close 关闭服务器

界面入口com.baige.imchat.MainFrame


linux注意事项
1.启动失败的原因可能是错误的判断服务器已经启动，启动控制台又连不上服务器，
原因可能是Linux文件夹 /etc/hosts 中未配置本地主机名
因此需要确保在/etc/hosts文件中存在着这么一条映射：
 <hostname> <local_ip>


2. Linux后台启动jar方式 https://blog.csdn.net/qq_30739519/article/details/51115075
方式一：
java -jar xxxxx.jar

特点：当前ssh窗口被锁定，可按CTRL + C打断程序运行，或直接关闭窗口，程序退出

那如何让窗口不锁定？

方式二:
java -jar xxxxx.jar &
&代表在后台运行。

特定：当前ssh窗口不被锁定，但是当窗口关闭时，程序中止运行。

继续改进，如何让窗口关闭时，程序仍然运行？

方式三

nohup java -jar xxxxx.jar &

nohup 意思是不挂断运行命令,当账户退出或终端关闭时,程序仍然运行

当用 nohup 命令执行作业时，缺省情况下该作业的所有输出被重定向到nohup.out的文件中，除非另外指定了输出文件。

方式四

nohup java -jar xxxxx.jar >temp.txt &
解释下 >temp.txt

command >out.file

command >out.file是将command的输出重定向到out.file文件，即输出内容不打印到屏幕上，而是输出到out.file文件中。

可通过jobs命令查看后台运行任务

jobs
那么就会列出所有后台执行的作业，并且每个作业前面都有个编号。
如果想将某个作业调回前台控制，只需要 fg + 编号即可。

fg 23
查看某端口占用的线程的pid

netstat -nlp |grep :9181