<h1 align="center"> get-jobs-boss</h1>
<div align="center">
    帮你快速在Boss直聘平台上沟通以此来获得一份工作
</div>

## 如何使用
由于使用selenium，所以
### 第一步：下载ChromeDriver
> https://sites.google.com/a/chromium.org/chromedriver/home

### 第二步：配置ChromeDriver
```java
        System.setProperty("webdriver.chrome.driver", "/Users/beamstark/Desktop/chromedriver");
```
由于我是系统全局配置，所以这行代码没有在代码块中出现，需要的小伙伴自行添加代码配置

### 第三步：修改代码
打开 /src/main/java/ResumeSubmission.java <br>
修改 **baseUrl** 为自己的筛选条件 <br>
可以在 [boss直聘](https://www.zhipin.com/hangzhou/) 中筛选你的工作条件然后复制url ⚠️ 要为&page= 结尾<br>
**page** ：从第几页开始投递，page不能小于1<br>
**maxPage**：投递到第几页
### 最后一步：运行代码
需要扫码登陆
<br>

> 希望能够在现在的大环境下帮助你找到一份满意的工作

## 请我喝杯咖啡☕️
<img src="./src/public/IMG_6480.JPG" alt="" height="250">

<img src="./src/public/IMG_6479.JPG" alt="" height="250">
