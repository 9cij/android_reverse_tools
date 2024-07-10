[TOC]

整理不易，请支持一下！

<img src="README.assets/919ab1ffc277675a60b1203c1cafb43.jpg" alt="919ab1ffc277675a60b1203c1cafb43" style="zoom: 25%;" />          <img src="README.assets/8b80d1f39813ad07a8eac6ab11b8d5b.jpg" alt="8b80d1f39813ad07a8eac6ab11b8d5b" style="zoom:25%;" />

# 常用基础

## adb命令

### frida, 启动! 

```
如果报错 {"type":"error","description":"Error: invalid address","stack":"Error: invalid address\n    at Object.value [as patchCode] (frida/runtime/core.js:170:.....}
>>> setenforce 0
```


```
>>> adb shell
>>> su
>>> cd /data/local/tmp
>>> ./frida    ./fd14
```

```
>>> adb forward tcp:27042 tcp:27042
>>> adb forward tcp:27043 tcp:27043
```
### 操作app

**启动app到某个activity**

`am start 包名/活动名`

eg. `am start com.jamcz.test/com.jamcz.test.MainActivity`

如果活动中含有包名，包名可以用 `.` 替代

eg. `am start com.jamcz.test/.MainActivity`

**提取apk**

1. 获取apk路径`pm path com.xx.xx`  `exit`
2. 推送  `adb pull /phone_path window_path`

### 输入

`adb shell input text "......"`

`input text ......`

## 关于版本

`安卓78/frida12`	`安卓10/frida14`	`安卓12/frida16`

## :red_circle:frida, 启动！

```
  >>> -o xx.txt  hook保存日志到本地txt
```
```
    ①运行ps查看手机端进程列表
    frida-ps -R

    ②附加某个进程
    frida -R com.demo.fridahook

    ③-> 写脚本
```
```
    这种模式重启app, 停在了开头 需要输入 %resume 继续
    frida -U -f com.gdufs.xman -l hook_saveSN.js
```
```
    命令行附加应用进程然后敲代码进程注入
    frida -U -f com.demo.fridahook
```
```
    附加模式 (attach)
    应用不会重启 从执行命令起注入
    frida -U -l hook.js 15.2.2版不要写包名 要写应用名称
    frida -UF -l hook.js  这就可以了 后面不用写名称
```
```
    重启一个Android进程并注入脚本
    frida -U -l hook.js -f com.xxx.xxxx --no-pause
```
```
    spawn 模式 15.2.2版本不可以这样写
    frida -Uf com.xxx.xxxx -l hook.js
```
```
    ①查看现在的进程
    frida-ps -R
    
    ②用pid附加上去  可以实时在js文件中修改脚本 控制台会输出错误语法的日志 不用管
    frida -U -p [PID] -l hook_saveSN.js
```
adb无线连接手机 校园网(内网隔离)无效 开热点解决
```
>>> adb tcpip 5555
>>> adb connect 192.168.100.20

>>> ./frida -l 0.0.0.0:6666  
>>> frida -H 192.168.2.102:6666 -f com.wangtietou.test_activity -l C:\\Users\\wangtietou\\Desktop\\hook_activity.js --no-pause
```

多个frida脚本同时运行

```
其中一个从一开始就hook上，用于过检测
>>> frida -U -f com.hupu.shihuo -l load_so.js --no-pause  14.2.18版
```

```
另一个以附加的形式hook
>>> frida -UF -l hook.js --no-pause
```



## 过frida检测

*详见另一个md文件*

### 更改端口

服务端 

```
cd /data/local/tmp
./frida -l 0.0.0.0:9999
```

转发端口 `adb forward tcp:9999 tcp:9999`

> 更改端口 `frida-ps -H 127.0.0.1:9999`

加载脚本 `frida -H 127.0.0.1:9999 com.xxx.xxx -l hook.js`

### 修改服务器进程名

`frida-server` -> `ns`

### 使用hluda

```
cd /data/local/tmp
./hluda
```

其它一样

### D-Bus通信协议检测

> App向每一个端口都发送了D-Bus认证消息，那肯定会利用strcmp( )或者strstr( )函数进行检测回复的消息

```javascript
function hook_strcmp() {
    var strcmp = Module.findExportByName('libc.so', "strcmp");
    Interceptor.attach(strcmp, {
        onEnter : function (args) {
            if (args[1].readCString().indexOf("REJECT")!=-1){
            console.log(args[0].readCString());
            console.log(args[1].readCString());
            }
        }, onLeave : function(retval) {
        }
    })
}
```

>有的app检测非常恶心，只要是maps和fd中存在/data/local/tmp/，甚至只有tmp的字段，app就给kill掉。因为这个目录对于安卓逆向工作来说，是一个比较敏感的目录。hluda-server和frida-server都会在/data/local/tmp/目录下生成一个包含frida所需要的so库等文件。所以当app一旦发现了加载了/data/local/tmp下的任何东西，直接就挂掉。
>————————————————
>版权声明：本文为CSDN博主「octopus_father」的原创文章，遵循CC 4.0 BY-SA版权协议，转载请附上原文出处链接及本声明。
>原文链接：https://blog.csdn.net/weixin_43889136/article/details/127713563

```javascript
function main() {
    const openPtr = Module.getExportByName('libc.so', 'open');
    const open = new NativeFunction(openPtr, 'int', ['pointer', 'int']);
    var readPtr = Module.findExportByName("libc.so", "read");
    var read = new NativeFunction(readPtr, 'int', ['int', 'pointer', "int"]);
    var fakePath = "/data/data/******/maps";
    var file = new File(fakePath, "w");
    var buffer = Memory.alloc(512);
    Interceptor.replace(openPtr, new NativeCallback(function (pathnameptr, flag) {
        var pathname = Memory.readUtf8String(pathnameptr);
        var realFd = open(pathnameptr, flag);
        if (pathname.indexOf("maps") != 0) {
            while (parseInt(read(realFd, buffer, 512)) !== 0) {
                var oneLine = Memory.readCString(buffer);
                if (oneLine.indexOf("tmp") === -1) {
                    file.write(oneLine);
                }
            }
            var filename = Memory.allocUtf8String(fakePath);
            return open(filename, flag);
        }
        var fd = open(pathnameptr, flag);
        return fd;
    }, 'int', ['pointer', 'int']));
}
setImmediate(main)
```

### frida反调试

**P-trace占坑**

查看手机上正在运行的进程 `ps -A`

过滤关键字 `ps -A | grep [app包名]` app包名不用写全，有一点就行

只要看到有两个进程的，其中之一就是用了p-trace占坑，另一个是主进程

注意，包名后面有`:`的不算

![image-20240420223737249](README.assets/image-20240420223737249.png)

**解决方案**

1. 尝试把占坑的进程杀掉，但不一定成功 `kill -9 [进程id]`
2. 使用`spawn`方法启动frida，让hook脚本早于占坑的进程
3. aosp刷机改源码，让app自己附加的进程都启动失败 :laughing:
4. 输出用到的so文件（代码见“打印用到的so文件”），看执行到哪里程序终端，把那个so删了逝世。系统so不需要管。



## 找进程名和pid

```python
# 枚举所有的进程
processes = rdev.enumerate_processes()
for process in processes:
    print(process)

# 获取在前台运行的APP
# Application(identifier="com.che168.autotradercloud", name="车智赢+", pid=3539, parameters={})
front_app = rdev.get_frontmost_application()
print(front_app)
```



## 搜索tips

**看Node, 如果不是该app相关包可以无视**
**treeMap.put(xxx)**

**search**

`sign`  `"sign" `  `&sign=`  `&sign`  `sign=`

**搜索同一请求的其它有个性的关键字**
**搜索独有信息**
网址 拿后缀搜       retrofit发送的请求，找reportClick 查找用例，或直接搜索"reportClick"

当不同网址，使用很多相同的参数时，可能会用拦截器。搜索的时候优先查看带有`Interceptor`的

**搜不到：如果这个字符串是在so中生成，可以去Hook内部：NewStringUTF 方法，该方法将C中的字符串转换为jstring，再返回给Java。hook脚本见下 `hookNewStringUTF`**

**hook拦截器/TreeMap/StringBuilder定位**

**猜**

如果是加密的数据，看它长得像哪种加密方式，hook验证，打印调用栈追踪

有些app（如B站心跳请求）封装了请求的类，在发请求.xxx()后面这样子添加了东西，这时就像上面那样搜索关键字，hook验证是不是走了某个位置

**如果jadx搜不到**：在app首次启动时注册设备`reg`/`register`，将设备指纹信息发送给后端，后端生成设备ID



## 有用的网址

https://1024tools.com/hash 各种加密 方便查看

https://curlconverter.com/  curl转其它语言

安卓逆向开发11期  https://www.aliyundrive.com/s/kyWMLGcVte3 
【看雪论坛】Unicorn高级逆向与反混淆 https://www.aliyundrive.com/s/yFMEJtGERt9
各大网课教程合集    https://www.aliyundrive.com/s/xatt4pVJFDp -1.30 TB





## 配置自动补全

进入项目文件

```
npm i  @types/frida-gum
```

```
npm i module_name 　# 安装模块到项目目录下
npm i module_name -g 　　# -g 的意思是将模块安装到全局，具体安装到磁盘哪个位置，要看 npm config prefix的位置。 
npm i module_name -S(-save) 　# --save 的意思是将模块安装到项目目录下，并在package文件的dependencies节点写入依赖。 
npm i module_name -D(--save-dev)　 # --save-dev 的意思是将模块安装到项目目录下，并在package文件的devDependencies节点写入依赖。
```

## so寻找函数方法

静态注册 -> `Java_包名_类型_方法名()`

动态注册 -> `JNI_OnLoad` -> `RegisterNatives(Jni对象, 类, 对应关系, 数量)`

如果遇到嵌套了很多层函数找不到RegisterNatives的解决方法：**hook系统底层函数 libart.so**

得到输出的十六进制地址后，`ida` -> `Jump` -> `Jump to address`

## 分析tips

### 构造方法 遇到this.x

```java
public final class SignedQuery {
    ...    
	// 搜索sign= 或&sign 找到 
     // hook它看是谁创建了这个对象
    public String toString() {
        String str = this.a;
        if (str == null) {
            return "";
        }
        if (this.b == null) {
            return str;
        }
        return this.a + "&sign=" + this.b; // 生成sign的地方
                                            // aid=xxx&... + "&sign=" + ths.b
    }
}
```

目标：找`this.b`，也就是生成`sign`的地方

注意到该类构造方法

```java
public final class SignedQuery {
    ...
// 有地方实例化了这个对象 传入了这两个参数 或者hook这个构造方法 $init
    public SignedQuery(String str, String str2) {
        this.a = str;
        this.b = str2; // sign
    }
    ...
}
```

**方法：输出调用栈** -> 找到上一层，谁调用了`toString()`

```java
aVar.s(h).l(c0.create(w.d("application/x-www-form-urlencoded; charset=utf-8"), h(hashMap).toString()));
```

`请求体 = h(hashMap).toString()`相当于`请求体 =SignedQuery对象.toString() `，前面肯定有new`SignedQuery`对象的地方

```java
    // 返回了SignedQuery 传入了map
    public SignedQuery h(Map<String, String> map) {
        return LibBili.g(map); // 看看是怎么生成SignedQuery的 关注实例化它的时候第二个参数 即是sign
    }
```

```java
    // 相当于把原来的map转成Treemap(有序的) 然后调用s
    public static SignedQuery g(Map<String, String> map) {
        return s(map == null ? new TreeMap() : new TreeMap(map));
    }
```

```java
static native SignedQuery s(SortedMap<String, String> sortedMap);
```

是在so层创建的对象，返回SignedQuery对象

参考正向开发-静态方法

### 抽象方法/接口

开发角度 https://youtu.be/HvPlEJ3LHgE?si=v-h88b1bsKmSv6i0 看完就会

`public abstract a_func();`

首先往上滑，找到`a_func`是哪个类(假设是`BClass`)，然后查找类的用例或者全局搜索

同时，看看调用这个函数的时候创建的是什么类（由什么类调用这个函数的）可以为之前的查找作为借鉴

格式应该是 `public class AClass extends BClass(){}` 进入这里面后 查询`a_func`方法就能找到它的具体代码

### 反射

```java
clazz = KeyInfo.class; // 类
object = KeyInfo.class.newInstance();  // 实例化
method = clazz.getMethod("getInfo", Context.class, String.class);
return (String) method.invoke(object, context, str);
```

双击进入类`KeyInfo`去寻找`getInfo`方法

### so

1. ida
2. hook看看返回值是否固定 清除数据
3. 抓包看是否固定

### :blue_heart:so实例化对象相关:green_heart:

```c
  v22 = a1->functions->FindClass(a1, "javax/crypto/Cipher");
  v23 = a1->functions->GetStaticMethodID(a1, v22, "getInstance", "(Ljava/lang/String;)Ljavax/crypto/Cipher;");
  v24 = a1->functions->NewStringUTF(a1, "AES/CBC/PKCS5Padding");
  v58 = a1->functions->CallStaticObjectMethod((JNIEnv *)a1, v22, v23, v24);
```

这个方法签名表示getInstance()方法接受一个java.lang.String类型的参数，并且返回一个javax.crypto.Cipher类型的对象

相当于 `Cipher obj = Cipher.getInstance("AES/CBC/PKCS5Padding")`

```c
    v25 = a1->functions->NewStringUTF(a1, "AES");
    v26 = a1->functions->FindClass(a1, "javax/crypto/spec/SecretKeySpec");
    v27 = a1->functions->GetMethodID(a1, v26, "<init>", "([BLjava/lang/String;)V");
    v21 = a1->functions->NewObject((JNIEnv *)a1, v26, v27, v20, v25);
    a1->functions->DeleteLocalRef((JNIEnv *)a1, v20);
```

相当于 `SecretKeySpec secretKeySpec = new SecretKeySpec(字节数组"key", string "AES")`

```c
  v19 = (const jbyte *)j_getMD516(v60, s, v18);
  v20 = a1->functions->NewByteArray(a1, 16);
  v21 = 0;
//使用JNI中的SetByteArrayRegion()方法，设置Java中一个byte[]数组的值。具体来说，此函数将v19数组的前16个字节复制到Java中v20所代表的byte数组的前16个字节处。其中，v20是一个byte[]数组对象的引用，a1是一个JNIEnv对象的指针，v19是一个指针，指向一个长度至少为16个字节的缓冲区，该缓冲区保存了将作为初始化向量使用的字节序列。
  a1->functions->SetByteArrayRegion((JNIEnv *)a1, v20, 0, 16, v19);
  v59[0] = 0LL;
  v59[1] = 0LL;
  v59[2] = 0LL;
  v59[3] = 0LL;
  j_rand16Str();
  v22 = a1->functions->FindClass(a1, "javax/crypto/Cipher");
  v23 = a1->functions->GetStaticMethodID(a1, v22, "getInstance", "(Ljava/lang/String;)Ljavax/crypto/Cipher;");
  v24 = a1->functions->NewStringUTF(a1, "AES/CBC/PKCS5Padding");
  v58 = a1->functions->CallStaticObjectMethod((JNIEnv *)a1, v22, v23, v24);
  if ( v20 )
  {
    v25 = a1->functions->NewStringUTF(a1, "AES");
    v26 = a1->functions->FindClass(a1, "javax/crypto/spec/SecretKeySpec");
    v27 = a1->functions->GetMethodID(a1, v26, "<init>", "([BLjava/lang/String;)V");
    v21 = a1->functions->NewObject((JNIEnv *)a1, v26, v27, v20, v25);
    a1->functions->DeleteLocalRef((JNIEnv *)a1, v20);
  }
```

分析可知，`v20`是key.获取key有两种方法.用hook java的方法，和hook`getMD516`的返回值:

hook指定长度的方法，见下面类型转换输出-控制长度输出

### 遇到了动态的iv

有时可能遇到动态的iv，服务器需要解密就要知道key和iv，如何让服务器解密？可能会把iv添加在密文的前面，随着请求一起发送过去。可以hook base64，看看传入的参数里有没有出现iv. 也有可能把变化的key或iv放到请求头的某个参数中去

### 不要完全相信反编译出来的结果

ida有时候会漏参数，或者有函数没传值进去<-没显示出来 be like :

```c
// function(v26) vs function() 这里是空的
```

### 输出太多

```java
package com.yoloho.controller.api;

import java.security.MessageDigest;

public class PeriodAPIV2 extends NetAPI {
    ...
    public void setDeviceCode() {
        ...
        MessageDigest messageDigest = MessageDigest.getInstance("sha-1");
        messageDigest.update(str6.getBytes());
        byte[] digest = messageDigest.digest();
        ...
    }
}
```

```javascript
var PeriodAPIV2 = Java.use("com.yoloho.controller.api.PeriodAPIV2");
var flag = false;  //防止输出太多东西

PeriodAPIV2.setDeviceCode.implementation = function () {
    console.log("-------------------------setDeviceCode-------------------------")
    flag = true;  //防止输出太多东西
    return this.setDeviceCode();
}

var MessageDigest = Java.use("java.security.MessageDigest");
var ByteString = Java.use("com.android.okhttp.okio.ByteString");
MessageDigest.update.overload("[B").implementation = function (data) {
    if (flag) {
        console.log(ByteString.of(data).utf8(), '\n' );
        console.log("---------------")
    }
    return this.update(data);
}
```

### 打印log

```
adb logcat -s ActivityThread
```

很好用，可惜不能在运行时分析package，只能退出后再把结果导入wireshark



### interface

某个方法在interface里

```java
public interrface c {
    // ...
    String f(Context context, Long l);
}
```

法一：**查找用例`c`** 搜索方法名`f`

**找到形如 `public class u implements c{}` 的代码。进入后寻找f的具体实现即可。**

通常实现方法的两种代码如下

```java
class Info implements ab {
    Info Obj = new Info;
}
// usage
obj.getColorStatParamStr();
```

or 类似于匿名

```java
Ab obj = new ab() {
    Map<String, String> getColorStatParamStr(boolean z, boolean z2, boolean z3) {
        ...
    }
}
// usage
obj.getColorStatParamStr();
```

法二： hook输出类型

```java
Map<String, String> colorStatParamStr = a.WV().getStatInfoConfigImpl().getColorStatParamStr(true, true, z);
```

```java
public interface ab {
    Map<String, String> getColorStatParamStr(boolean z, boolean z2, boolean z3);
}
```

hook `getStatInfoConfigImpl` 方法, 获取返回值 `Json.stringify(对象)`

eg.

```java
@Override // com.bilibili.api.a.b
public String getSessionId() {
    return com.bilibili.lib.foundation.e.b().getSessionId(); // 看e.b()是哪个类实现的
}
```

点入 `getSessionId()` 是接口

```java
public interface a {
    // 又是一个接口 不看它了
    String getSessionId();
}
```

点 `getSessionId`前面的`b()`

```java
public final class e {
    public static final Application a() {
        return d.g.b().c();
    }

    // a 表示返回值的类型
    public static final a b() {
        return d.g.b().d(); // hook返回值看看是哪个类的对象
    }

    public static final d.b c() {
        return d.g.b().e();
    }

    public static final c d() {
        return d.g.b().f();
    }

    // 读取xml文件
    public static final SharedPreferences e() {
        return d.g.b().g();
    }
}
```

```javascript
Java.perform(function() {
    var e = Java.use("com.xx.x.e");
    e.b.implementation = function() {
        var res = this.b();
        console.log("obj = ", JSON.stringify(res));
        return res;
    }
})
```

输出的`$className`就是它真正的类，就去这个类里面找`getSessionId()`方法



法三：hook输出调用栈





## 抓包问题

### 客户端校验

![image-20240420225823031](README.assets/image-20240420225823031.png)

很可能用了客户端校验之类的东西，如果`justtrustme` 和 frida 脚本依然抓不到包，就有可能是混淆的代码。

校验的触发位置是在：`okhttp3.internal.connection.RealConnection`类中的`connectTls`方法

有执行顺序，可能只hook掉第三个就能抓到包，也可能需要全部hook掉才行

![image-20221007160903732](README.assets/image-20221007160903732.png)

#### pinner校验混淆 

**调用栈分析**

1. 使用 frida/java/客户端校验的js脚本，根据输出的调用栈，对比寻找 `connectTls` 方法

![image-20221007161218948](README.assets/image-20221007161218948.png)

![image-20221007161358236](README.assets/image-20221007161358236.png)

![image-20221007161430339](README.assets/image-20221007161430339.png)

2. 反编译app看代码，和源码对比着看。源码可参考NetDemo3
3. 根据未混淆前的hook脚本，换成混淆的

![image-20231020114236347](README.assets/image-20231020114236347.png)

主要改的是 `Java.use` 和 方法名

#### 证书校验

如果继续寻找证书校验的相关代码，可以搜索：

- `checkServerTrusted`方法 或 `X509Certificate[] chain参数`
- `SSLContext.getInstance`  或 `new TrustManager[]` 或 hook `sslContext.init`



混淆的可用hook系统源码方法

https://github.com/google/conscrypt/blob/86ff4e3fd4b6b3bb76a7ec0e91290384401ccbf3/android/src/main/java/org/conscrypt/Platform.java#L396

![image-20221008165421397](README.assets/image-20221008165421397.png)



```javascript
Java.perform(function () {
    var Platform = Java.use('com.android.org.conscrypt.Platform');
    Platform.checkServerTrusted.overload('javax.net.ssl.X509TrustManager', '[Ljava.security.cert.X509Certificate;', 'java.lang.String', 'com.android.org.conscrypt.AbstractConscryptSocket').implementation = function (x509tm, chain, authType, socket) {
        console.log('\n[+] checkServer  ',x509tm,JSON.stringify(x509tm) );//输出tm可定位到真实的使用位置

        //return this.checkServerTrusted(x509tm, chain, authType, socket);
    };
});

// frida -U -f 包名 -l 6.hook_check.js --no-pause
// frida -UF -l 6.hook_check.js
```

此方法有风险，可能导致其它方法执行错误

更好的方法是：

先用这个脚本输出它执行的地方，然后再hook这个地方

#### 主机校验

`HostnameVerifier`属于是`javax.net.ssl` 中的接口不会被混淆。

所以，可以直接搜索 `HostnameVerifier`  或 `new HostnameVerifier` 或 `实现HostnameVerifier接口的类` 或 `verify` 关键字等。此方法不推荐



非通用脚本，混淆失效，需要改动

```javascript
Java.perform(function () {
    function getFieldValue(obj, fieldName) {
        var cls = obj.getClass();
        var field = cls.getDeclaredField(fieldName);
        field.setAccessible(true);
        var name = field.getName();
        var value = field.get(obj);
        return value;
    }

    function getMethodValue(obj, methodName) {
        var res;
        var cls = obj.getClass();
        var methods = cls.getDeclaredMethods();

        methods.forEach(function (method) {
            var method_name = method.getName();
            console.log(method_name, method);
            if (method_name === methodName) {
                method.setAccessible(true);
                res = method;
                return;
            }
        })
        return res;
    }

    var RealConnection = Java.use('okhttp3.internal.connection.RealConnection');// 混淆改这里
    // 这里传入的参数个数混淆后也有可能不同
    //.implementation 前面的需要改
    RealConnection.connectTls.implementation = function (connectionSpecSelector) {
        var route = getFieldValue(this, "route"); // this.route
        console.log('route=', route);
        var address = getFieldValue(route, 'address');
        console.log('address=', address);
        var hostnameVerifier = getFieldValue(address, 'hostnameVerifier');
        console.log('\n[+] hostnameVerifier', hostnameVerifier);
        /* 混淆的用这里
        try {
            var route = getFieldValue(this, "route");//混淆改这里
            console.log('route=', route);
            var address = getFieldValue(route, 'address');//混淆改这里
            console.log('address=', address);
            var func = getMethodValue(address, "hostnameVerifier");
            console.log('\n[+] addhostnameVerifierress', func.invoke(address, null));
        } catch (e) {
            console.log(e);
        }
        */
        return this.connectTls(connectionSpecSelector);
    };
});

// frida -U -f 包名 -l 7.hook_verify.js
// frida -UF -l 7.hook_verify.js
// frida -U -f cn.ticktick.task -l 7.hook_verify.js
```

法三 r0yse的抓包工具



### TCP hook抓包

tcp通用脚本

#### HTTP

**请求写入**

![image-20240416172831657](README.assets/image-20240416172831657.png)

抽象类，抽象方法，没有具体实现，是在某个具体的类继承这个方法，在方法的子类里才有具体实现

```java
// java.net.SocketOutputStream
OutputStream outputStream = socket.getOutputStream();
outputStream.write(sb.toString().getBytes());
```

```java
private void doRequest() {
    new Thread() {
        @Override
        public void run() {
            try {
                // http://wiki.mikecrm.com/index?ajax=1&page=2
                Socket socket = new Socket("wiki.mikecrm.com", 80);

                // 1.构造请求头
                StringBuilder sb = new StringBuilder();
                sb.append("GET /index?ajax=1&page=2 HTTP/1.1\r\n");
                sb.append("host: wiki.mikecrm.com\r\n");
                sb.append("user-Agent: test\r\n");
                sb.append("\r\n");

                // 2.写入数据（发送数据）
                // java.net.SocketOutputStream
                OutputStream outputStream = socket.getOutputStream();
                outputStream.write(sb.toString().getBytes());
                Log.e("outputStream的类 => ", outputStream.getClass().getName());

                // 3.读取数据（获取数据）
                // java.net.SocketInputStream
                InputStream inputStream = socket.getInputStream();
                Log.e("inputStream的类 => ", inputStream.getClass().getName());

                while (true) {
                    byte[] buffer = new byte[1024];
                    int len = inputStream.read(buffer, 0, buffer.length);
                    if (len == -1) {
                        break;
                    }
                    Log.e("读取相应内容 =>", new String(Arrays.copyOf(buffer, len)));
                }
                socket.close();
            } catch (Exception ex) {
                Log.e("Main", "网络请求异常" + ex);
            }
        }
    }.start();
}
```

使用`outputStream.getClass().getName()`获取到真正定义它的类

获取到所有http请求的数据

```javascript
Java.perform(function () {
    var SocketOutputStream = Java.use('java.net.SocketOutputStream');
    var HexDump = Java.use("com.android.internal.util.HexDump");
    var ByteString = Java.use("com.android.okhttp.okio.ByteString");

    SocketOutputStream.socketWrite0.overload('java.io.FileDescriptor', '[B', 'int', 'int').implementation = function (fd, b, off, len) {
        console.log("参数：", fd, b, off, len);
        console.log(HexDump.dumpHexString(b, off, len), "\n");
        console.log(ByteString.of(b).utf8(), "\n"); // 字节->字符串
        console.log(ByteString.of(b).utf8());

        return this.socketWrite0(fd, b, off, len);
    };
});

// frida -UF -l  1.hook.js
// console.log(Java.use("android.util.Log").getStackTraceString(Java.use("java.lang.Throwable").$new()));
```

```javascript
Java.perform(function () {
    var SocketOutputStream = Java.use('java.net.SocketOutputStream');
    var ByteString = Java.use("com.android.okhttp.okio.ByteString");

    SocketOutputStream.write.overload('[B').implementation = function (arr) {
        console.log(arr); // byte 104,101,108,,..
        console.log(ByteString.of(arr).utf8(), "\n"); // 字节->字符串
        console.log(ByteString.of(arr).hex()); // 68656c...
        // 只写这一行也可以 打印类似wireshark里的格式
        console.log(HexDump.dumpHexString(arr), "\n");
        return this.write(arr);
    };
});

// frida -UF -l  1.hook.js
// console.log(Java.use("android.util.Log").getStackTraceString(Java.use("java.lang.Throwable").$new()));
```

**获取相应**

java开发代码

```java
// java.net.SocketInputStream
InputStream inputStream = socket.getInputStream();
Log.e("inputStream的类 => ", inputStream.getClass().getName());
while (true) {
    byte[] buffer = new byte[1024];
    int len = inputStream.read(buffer, 0, buffer.length);
    if (len == -1) {
        break;
    }
    Log.e("读取相应内容 =>", new String(Arrays.copyOf(buffer, len)));
}
```

hook脚本

```javascript
Java.perform(function () {
    var SocketInputStream = Java.use('java.net.SocketInputStream');
    var ByteString = Java.use("com.android.okhttp.okio.ByteString");

    SocketInputStream.read.overload('[B','int','int').implementation = function (b,off,len) {
        var res = this.read(b,off,len);
        console.log(HexDump.dumpHexString(b, off, len));
        return res;
    };
});

// frida -UF -l  1.hook.js
// console.log(Java.use("android.util.Log").getStackTraceString(Java.use("java.lang.Throwable").$new()));
```

有时上面的方法会有重载overload，需要修改。我们可以深♂入分析，hook更深层

**Deeper♂**

其实可以用任意一个地方hook，不用这么深♂

寻找`socketRead0`和`socketWrite0`的hook的脚本：

```javascript
Java.perform(function() {
    const apiResolver = new ApiResolver('module');
    apiResolver.enumerateMatches('exports:*!*socket*0*').forEach(function (v) {
        console.log(v.name);
    });
});
```

```javascript
Java.perform(function () {
    const NET_Send = Module.getExportByName('libopenjdk.so', 'NET_Send');
    const NET_Read = Module.getExportByName('libopenjdk.so', 'NET_Read');
    Interceptor.attach(NET_Send, {
        onEnter(args) {
            console.log('write call'); // jni,jobj,data,off,len
            //console.log(hexdump(args[1], {length: args[2].toInt32()}));
            console.log(Memory.readByteArray(args[1], args[2].toInt32()));
        }
    });
    Interceptor.attach(NET_Read, {
        onEnter(args) {
            console.log('read call');
            this.buf = args[1];
        }, onLeave: function (retval) {
            retval |= 0; // Cast retval to 32-bit integer.
            if (retval <= 0) {
                return;
            }
            console.log(Memory.readByteArray(this.buf, retval));
        }
    });
});

// frida -UF -l hook.js
```

#### HTTPS

**请求写入**

```javascript
Java.perform(function () {
    var NativeCrypto = Java.use('com.android.org.conscrypt.NativeCrypto');
    var HexDump = Java.use("com.android.internal.util.HexDump");
    var ByteString = Java.use("com.android.okhttp.okio.ByteString");

    NativeCrypto.SSL_write.implementation = function (ssl, ssl_holder, fd, shc, b, off, len, timeout) {
        //console.log(HexDump.dumpHexString(b, off, len), "\n")
        console.log(ByteString.of(b).utf8(), "\n");

        return this.SSL_write(ssl, ssl_holder, fd, shc, b, off, len, timeout);
    };
});

// frida -UF -l  3.hook.js
// console.log(Java.use("android.util.Log").getStackTraceString(Java.use("java.lang.Throwable").$new()));
```

**请求获取**

```javascript
Java.perform(function () {
    var NativeCrypto = Java.use('com.android.org.conscrypt.NativeCrypto');
    var HexDump = Java.use("com.android.internal.util.HexDump");
    var ByteString = Java.use("com.android.okhttp.okio.ByteString");

    NativeCrypto.SSL_read.implementation = function (ssl, ssl_holder, fd, shc, b, off, len, timeout) {
        var res = this.SSL_read(ssl, ssl_holder, fd, shc, b, off, len, timeout);
        //console.log(HexDump.dumpHexString(b, off, len), "\n")
        console.log(ByteString.of(b).utf8(), "\n");
        return res;
    };
});

// frida -UF -l  4.hook.js
// console.log(Java.use("android.util.Log").getStackTraceString(Java.use("java.lang.Throwable").$new()));
```

**hook so**

寻找`SSL_write`和`SSL_read`的hook的脚本：

```javascript
Java.perform(function () {
    const apiResolver = new ApiResolver('module');
    // 包含：libttboringssl.so 或 libssl.so
    // 'exports:*lib*ssl*!SSL_*'
    apiResolver.enumerateMatches('exports:*lib*ssl*!SSL_*').forEach(function (v) {
        if (v.name.indexOf('SSL_write') > 0) {
            // SSL_write = v.address;
            console.log(v.name);
        } else if (v.name.indexOf('SSL_read') > 0) {
            // SSL_read = v.address;
            console.log(v.name);
        }
    });
});
```

**hook 请求和响应**

```javascript
Java.perform(function () {
    const SSL_write = Module.getExportByName('libssl.so', 'SSL_write');
    const SSL_read = Module.getExportByName('libssl.so', 'SSL_read');

    Interceptor.attach(SSL_write, {
        onEnter(args) {
            console.log('write call');
            //console.log(args[0]); // ssl
            //console.log(args[1]); // buffer
            //console.log(args[2]); // len
            //console.log(hexdump(args[1], {length: args[2].toInt32()}));
            console.log(Memory.readByteArray(args[1], parseInt(args[2])));
        }
    });

    Interceptor.attach(SSL_read, {
        onEnter(args) {
            console.log('read call');
            this.buf = args[1];
        }, onLeave: function (retval) {
            retval |= 0; // Cast retval to 32-bit integer.
            if (retval <= 0) {
                return;
            }
            console.log(Memory.readByteArray(this.buf, retval));
        }
    });
});

// frida -UF -l  7.hook.js
// frida -U -f com.nb.ssldemo2 -l 7.hook.js --no-pause
```

### 抓不到有用的包

在安卓开发时，OkHttp发送请求，设置 `Proxy.NO_PROXY`，基于系统代理都是抓不到包。

```java
OkHttpClient client = new OkHttpClient.Builder().proxy(Proxy.NO_PROXY).build();
FormBody form = new FormBody.Builder()
    .add("user", dataMap.get("username"))
    .add("pwd", dataMap.get("password"))
    .add("sign", dataMap.get("sign")).build();

Request req = new Request.Builder().url("http://192.168.0.6:9999/login").post(form).build();
```

`proxydroid`，`SocketDroid`, `drony`启动！

`drony`有时加载不出wifi

`SocksDroid` 切记：在使用前**删除**手机上设置的系统代理。

`ProxyDroid` 切记：在使用前**删除**手机上设置的系统代理。

### 抓包显示:negative_squared_cross_mark: :x:

使用SocketDroid转发





### :question: 其它协议

实时显示：弹幕

但是https会断开连接

猜想：基于TCP协议，不断开连接，实时显示

#### WebSocket

https不支持服务端主动向客户端推送消息

WebSocket可以实现相互的收发消息，服务端可以主动推送消息，支持长连接

使用场景：

1. web聊天室
1. 弹幕



### mitmproxy

用来抓包的，和Charles类似，可配置代理抓国外包。可写脚本。详见抓包文件夹



## 推荐阅读

[frida hook AES DES RSA 自吐算法]: https://www.codenong.com/jsecbee028022b/
[r0ysue大佬的课程目录]: http://www.dtasecurity.cn:20080/androidsenior/readme2.html

[FART教程]: https://www.anquanke.com/post/id/199898
[FART github]: https://github.com/hanbinglengyue/FART
[手机投到电脑上]: https://github.com/Genymobile/scrcpy


## ida使用

### 修改汇编指令

1. 把鼠标定位到要修改的指令(函数图上的就行)
2. 调出16进制的窗口 `View` -> `Open subviews` ->`Hex dump` （这步可以不做）
3. 修改 `Edit` -> `Patch program` -> `Change byte...`
4. 改完后，记得保存 `Edit` -> `Patch program` -> `Apply patches to input file`

### 快捷键

`Shift + F12` 调出字符串窗口

`Alt + T` 搜索字符串

## 花指令(junk code)

- 花指令根本不会影响x32dbg的动态调试
- jeb 高版本5.0自带控制流混淆还原



# python TOOLS

## 端口转发一键运行

```python
import subprocess
# 重新连接手机需要运行
subprocess.getoutput("adb forward tcp:27042 tcp:27042")
subprocess.getoutput("adb forward tcp:27043 tcp:27043")
```

## 切割?后的东西

形如 aa=11&bb=22&cc=33...xx=xx&xxx=xxx

```python
param_string = input(">>>")

import json
data_dict = {item.split('=')[0]: item.split('=')[1] for item in param_string.split('&')}
data_string = json.dumps(data_dict, indent=4)
print(data_string)

with open("output.txt", mode="w") as f:
    f.write(data_string)
```

## java字节数组(有符号) -> python字节数组(无符号)

```python
byte_list = [47,-38,-99,34,-13,44,-43,-119,3,76,8,32,47,-115,105,61,-91,-46 ...]

bs = []
for item in byte_list:
    if item < 0:
        item = item + 256
    bs.append(item)

print(bs)
```

## java字节数组(byte)转字符串

```python
byte_list = [97,110,99,104,111,114,82,101,112,108,121,73,100,48,99,111,110,116,101,110]
def byte2str(byte_list):
    data = bytearray()
    for i in byte_list:
        data.append(i)
    data_string = data.decode('utf-8')
    print(data_string)

byte2str(byte_list)
```

## 字节 -> 十六进制(Hex)字符串

```python
bytes_data = b'\x9f\x1bVbf\x12\xa73\x91\xe5\x90\xb3fN\xe6\xfb'
#       去除前面的0x 不满两位补0
result = "".join([hex(item)[2:].rjust(2, "0") for item in bytes_data])
print(result)
```

## 字节数组 -> 十六进制(Hex)字符串

```python
byte_list = [47, 218, 157, 34, 243, 44, 213, 137, 3, 76, 8, 32, 47, 141, 105, 61, 165, 210]  # 注意java的要先处理成python的
print([hex(ele)[2:] for ele in byte_list])
```

## 随机生成mac地址

```python
def create_random_mac(sep=":"):
    """ 随机生成mac地址 """
    data_list = []
    for i in range(1, 7):
        part = "".join(random.sample("0123456789ABCDEF", 2))
        data_list.append(part)
    mac = sep.join(data_list)
    return mac
```

## 字符串 -> 字节

```python
data_string = "啊吧啊吧"
data_string.encode('utf-8')
```

## 字典排序后拼接成x=x&xx=xx

```python
data_dict = {'x':'x', "xx":"xx"}
ordered_string = "&".join(["{}={}".format(key, data_dict[key]) for key in sorted(data_dict.keys())])
```

## 字典url转义

```python
puote_plus(
    json.dumps({"appid":1, "platform":3}, separators=(',', ":"))
)
```

### url转回来

```python
import json
from urllib.parse import quote_plus, unquote_plus

dinfo = "%7B%22ah1%22%3A%22%22%2C%22ah2%22%3A%22%22%2C%22ah3%22%3A%22%22%2C%22ah4%22%3A%22wifi%22%2C%22ah5%22%3A%221080_2236%22%2C%22ah6%22%3A1785600%2C%22ah7%22%3A8%2C%22ah8%22%3A5732413440%2C%22ah9%22%3A%22Pixel+4%22%2C%22ah10%22%3A%22%22%2C%22ah11%22%3A%22%22%2C%22ah12%22%3A%22%22%2C%22ah13%22%3A%22%22%2C%22as1%22%3A%2210%22%2C%22as2%22%3A%22%22%2C%22as3%22%3A%22%22%2C%22as4%22%3A%22b9ab99eb4438fddd%22%2C%22as5%22%3A%22%22%2C%22as6%22%3A%22%22%2C%22as7%22%3A%2229%22%2C%22ac1%22%3A%22ac791f31-66b8-301e-837d-539d4c0c39c8%22%7D"

result = unquote_plus(dinfo)
print(result)
data_dict = json.loads(result)
print(json.dumps(data_dict, indent=4))
```

## >>>

java的  `>>>` 在python里实现

```python
    def int_overflow(val):
        maxint = 2147483647
        if not -maxint - 1 <= val <= maxint:
            val = (val + (maxint + 1)) % (2 * (maxint + 1)) - maxint - 1
        return val
    
    def unsigned_right_shitf(n, i):
        # 数字小于0，则转为32位无符号uint
        if n < 0:
            n = ctypes.c_uint32(n).value
        # 正常位移位数是为正数，但是为了兼容js之类的，负数就右移变成左移好了
        if i < 0:
            return -int_overflow(n << abs(i))
        # print(n)
        return int_overflow(n >> i)
```

## 字典->符串

```python
cipher_dict = {
    'area': 'DV8nDNrpCzO4D18zDNSnCK==',
     'd_model': 'UwVubWu4GG==',
     'wifiBssid': 'dW5hbw93bq==',
     'osVersion': 'CJK=',
     'd_brand': 'WQvrb21f',
     'screen': 'CJC2EIe3CtK=',
     'uuid': 'DNS5DzCnYwU1EQVuDQVwYzq4YwDwCWCyCJYyCzGyYzS=',
     'aid': 'DNS5DzCnYwU1EQVuDQVwYzq4YwDwCWCyCJYyCzGyYzS=',
     'openudid': 'DNS5DzCnYwU1EQVuDQVwYzq4YwDwCWCyCJYyCzGyYzS='
}


data_dict = {
    "hdid": "JM9F1ywUPwflvMIpYPok0tt5k9kW4ArJEU3lfLhxBqw=",
    "ts": "1689560145797",
    'ridx': -1,
    'cipher': cipher_dict,
    'ciphertype': 5,
    "version": "1.2.0",
    'appname': "com.jingdong.app.mall",
}
```

```python
# 字符串中间没有空格
ep = json.dumps(data_dict, separators=(',', ':'))
```

输出的字符串为：

```
'{"hdid":"JM9F1ywUPwflvMIpYPok0tt5k9kW4ArJEU3lfLhxBqw=","ts":1689560145797,"ridx":-1,"cipher":{"area":"DV8nDNrpCzO4D18zDNSnCK==","d_model":"UwVubWu4GG==","wifiBssid":"dW5hbw93bq==","osVersion":"CJK=","d_brand":"WQvrb21f","screen":"CJC2EIe3CtK=","uuid":"DNS5DzCnYwU1EQVuDQVwYzq4YwDwCWCyCJYyCzGyYzS=","aid":"DNS5DzCnYwU1EQVuDQVwYzq4YwDwCWCyCJYyCzGyYzS=","openudid":"DNS5DzCnYwU1EQVuDQVwYzq4YwDwCWCyCJYyCzGyYzS="},"ciphertype":5,"version":"1.2.0","appname":"com.jingdong.app.mall"}'
```






# python连接frida-server的方式

## usb连接
```python
# 获取设备信息-----------------------
rdev = frida.get_remote_device()
session = rdev.attach("抖音短视频")
# ---------------------------------
```
## 端口连接
```
>>> ./frida -l 0.0.0.0:8888
```
```python
device = frida.get_device_manager().add_remote_device("192.168.x.x:8888") # 手机ip
session = device.attach("抖音短视频")  # 包名或名字  attach附加模式 不用重启app
```

```python
pid = device.spawn(["com.xx.xx"])  # 包名或名字  spawn模式 重启app
session = device.attach(pid)
```
## wifi连接

先连着usb线 连接完后可以断开

```
>>> adb tcpip 5555
>>> adb connect 192.168.100.20 手机ip
```

## :red_circle:模板

### spawn 重启应用

```python
import frida
import sys
# ---------------------------------
rdev = frida.get_remote_device()
pid = rdev.spawn(["com.xx.xx"])
session = rdev.attach(pid)
# ---------------------------------
scr = """
Java.perform(function () {
	var ClassName = Java.use('com.xxx.xx.ClassName');
	ClassName.Method.implementation = function(arg1, arg2, ...) {
		result = this.Method(arg1, arg2, ...);
		return result;
	}
})
"""
script = session.create_script(scr)

def on_message(message, data):
    print(message, data)
    
script.on("message", on_message)
script.load()
rdev.resume(pid)  # spawn
sys.stdin.read()  # 程序阻塞 不让停止
```

### attach 直接附加

```python
import frida
import sys

rdev = frida.get_remote_device()
session = rdev.attach("中文名")  # 14y

scr = """
Java.perform(function () {
   ...
});
"""
script = session.create_script(scr)

def on_message(message, data):
    print(message, data)

script.on("message", on_message)
script.load()
sys.stdin.read()
```



# frida rpc 主动调用

## 在"""里加载

### 格式一

```python
import frida

def get_frida_rpc_script():
    rdev = frida.get_remote_device()
    session = rdev.attach("猿人学2022")

    scr = """
    function invokeSign(data){
    var result;
        Java.perform(function () {
            Java.choose("com.yuanrenxue.match2022.security.Sign",{
                onMatch:function(ins){  // 实例化对象 可能需要刷新一下手机页面加载对象
                    console.log("ins=>",ins);
                    result = ins.sign(stringToByte(data));
                },onComplete(){}
            });
        })
        return result;
    }
    
    rpc.exports = {
        invokesignn:invokeSign,
    }
    """
    script = session.create_script(scr)
    script.load()
    return script


# 调用
script = get_frida_rpc_script()
sign = script.exports.invokesignn(sb)  # exports.后面的名字必须和上面exports{}键的一样 不支持下划线_
```

### 格式二

```python
import frida

def get_frida_rpc_script():
    rdev = frida.get_remote_device()
    session = rdev.attach("抖音短视频")
    scr = """
    rpc.exports = {   
        ttencrypt:function(bArr,len){
             var res;
             Java.perform(function () {
                 ......
             return res;
        },
        execandleviathan: function (i2,str){
            var result;
            Java.perform(function () {
				......
            });
            return result;
        }
    }
    """
    script = session.create_script(scr)
    script.load()
    return script


# 调用
script = get_frida_rpc_script()
gorgon_byte_list = script.exports.execandleviathan(khronos, un_sign_string)
```

## 读取文件加载

```python
import frida
# 不知道干什么用的
def my_message_handler(message, payload):
    print("message=>", message)
    print("payloa=>d", payload)

# connect wifiadb
device = frida.get_device_manager().add_remote_device("192.168.43.71:8888") # 手机ip
print('设备=>', device)
session = device.attach("com.yuanrenxue.match2022")
print('session=>', session)
# load script
with open("app.js") as f:  # app.js见格式一的s
    script = session.create_script(f.read())
script.on("message", my_message_handler)  # 调用错误处理
script.load()

print(script.exports.invokesign('page=' + data['page'] + data['t']))  # 调用
```

## 传参

- 字符串/整型/浮点型等直接传递。

```python
import frida

rdev = frida.get_remote_device()
session = rdev.attach("大姨妈")  # com.yoloho.dayima

scr = """
rpc.exports = {   
    encrypt:function(v1,v2,v3,v4,v5){
    
        console.log(v1,typeof v1);  //Number
        console.log(v2,typeof v2);  //String
        console.log(v3,typeof v3);  //Number
        console.log(v4,typeof v4);  //Number
        console.log(v5,typeof v5);  //String
        
        var v6 = parseInt(v5);
        console.log(v6,typeof v6);  //Number
    }
}
"""
script = session.create_script(scr)
script.load()

# 调用
script.exports.encrypt(100, "wupeiqi", 19.2, -10, "-1")
```

- 列表/字典

```python
import frida

rdev = frida.get_remote_device()
session = rdev.attach("大姨妈")  # com.yoloho.dayima

scr = """
rpc.exports = {   
    encrypt:function(v1,v2){
        console.log(v1,typeof v1, v1[0], v1[1]);
        console.log(v2,typeof v2, v2.name, v2.age);
        
        for(let key in v1){
            console.log(key, v1[key] )
        }
        
        for(let key in v2){
            console.log(key, v2[key] )
        }
    }
}
"""
script = session.create_script(scr)
script.load()

script.exports.encrypt([11, 22, 33], {"name": 123, "age": 456})
```

- 字节，无法直接传递，需转换为列表。

```python
import frida

rdev = frida.get_remote_device()
session = rdev.attach("大姨妈")  # com.yoloho.dayima

scr = """
rpc.exports = {   
    encrypt:function(v1,v2){
        console.log(v1,typeof v1);        
        // 转换为java的字节数组
        var bs = Java.array('byte',v1);
        console.log(JSON.stringify(bs))
        // 传入方法
        Java.perform(function () {
        	var Crypt = Java.use("com.xx.xxx");
        	res = Crypt.encrypt_data(bs);
        })
    }
}
"""
script = session.create_script(scr)
script.load()

arg_bytes = "武沛齐".encode('utf-8')  # b'\xb2\xe6..\x..'
byte_list = [i for i in arg_bytes]  # [230, 173, 166, ...]
script.exports.encrypt(byte_list)
```

- 某个类的对象，无法直接传递，可以将参数传入，然后再在JavaScript调用frida api构造相关对象。

```python
import frida

rdev = frida.get_remote_device()
session = rdev.attach("大姨妈")  # com.yoloho.dayima

scr = """
rpc.exports = {   
    encrypt:function(v1,v2){
        const StringBuilder = Java.use('java.lang.StringBuilder');;
        var obj = StringBuilder.$new();
        obj.append(v1);
        obj.append(v2);
        var result = obj.toString();
        console.log(result);       
        // 传入方法
        Java.perform(function () {
        	var Crypt = Java.use("com.xx.xxx");
        	res = Crypt.encrypt_data(obj, v1, v2);
        })
    }
}
"""
script = session.create_script(scr)
script.load()

script.exports.encrypt("武沛齐", "666")
```

```python
import frida

rdev = frida.get_remote_device()
session = rdev.attach("大姨妈")  # com.yoloho.dayima

scr = """
rpc.exports = {   
    encrypt:function(v1,v2,v3,v4){
        // 1.整型和字符串直接用
        console.log(v1,v2);
        // 2.字节数组
        var v3_obj = Java.array('byte',v3);
        console.log(v3_obj, JSON.stringify(v3_obj));        
        // 3.TreeMap对象   obj.get("xx")
        var TreeMap = Java.use("java.util.TreeMap");
        var v4_obj = TreeMap.$new();   
        for(let key in v4){
            //console.log(key,v4[key]);
            v4_obj.put(key,v4[key])
        }      
        console.log(v4_obj)
        console.log( v4_obj.get("name") )
        console.log( v4_obj.get("age") )
       
        var keyset = v4_obj.keySet();
        var it = keyset.iterator();
        while(it.hasNext()){
            var keystr = it.next().toString();
            var valuestr = v4_obj.get(keystr).toString();
            console.log(keystr, valuestr);
        }
    }
}
"""
script = session.create_script(scr)
script.load()
# z
v3 = [i for i in "wupeiqi".encode('utf-8')]
script.exports.encrypt(10, "wupeiqi", v3, {"name": "root", "age": "18"})
```

# 发送请求的格式

### 什么时候用json

```python
# 请求头content-type: "application/json"  json=json.
requests.post(url, headers=headers, data=data_dict)
```

### treemap有序 在python里怎么处理

```python
# 无序 -> hashmap
data_dict = {
    "_appid": "atc.android",
    "appversion": "2.8.2",
    "channelid": "csy",
    "pwd": md5(passwrod),
    "udid": udid,
    "username": username
}

result = "".join(["{}{}".format(key, data_dict[key]) for key in sorted(data_dict.keys())])
```

### 关于data

 可传字典或字符串，也可是二进制数据 `data = body_string.encode('utf-8')`

### 携带证书

```python
from requests_pkcs12 import get, post

res = post(
    url='https://8.218.11.182:21402/userservices/v2/user/login',
    json={
        "device_type": "app",
        "username": "008615131255555",
    },
    headers={
        "bundle_id": "com.paopaotalk.im",
        "version": "1.7.4",
    },
    pkcs12_filename='Client1.p12',
    pkcs12_password='111111',
    verify=False
)
print(res.text)
```

默认requests不支持直接使用p12格式的证书，所以需要将p12转换成pem才可以。

> openssl pkcs12 -in Client1.p12 -out demo.pem -nodes -passin 'pass:111111'

```python
from requests import post

res = post(
    url='https://8.218.11.182:21402/userservices/v2/user/login',
    json={
        "device_type": "app",
        "username": "008615131255555",
    },
    headers={
        "bundle_id": "com.paopaotalk.im",
        "version": "1.7.4",
    },
    cert='demo.pem',
    verify=False
)
print(res.text)
```



# 常见代码

## 3DES(对称)

### python

```python
def des3(data_string):
    BS = 8
    pad = lambda s: s + (BS - len(s) % BS) * chr(BS - len(s) % BS)

    # 3DES的MODE_CBC模式下只有前24位有意义
    key = b'appapiche168comappapiche168comap'[0:24]
    iv = b'appapich'

    plaintext = pad(data_string).encode("utf-8")

    # 使用MODE_CBC创建cipher
    cipher = DES3.new(key, DES3.MODE_CBC, iv)
    result = cipher.encrypt(plaintext)
    return base64.b64encode(result).decode('utf-8')
```

## AES(对称)

### java

```java
import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.io.UnsupportedEncodingException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.Base64;

public class Hello {
    public static void main(String[] args) throws Exception {
        String data = "武沛齐";
        String key = "fd6b639dbcff0c2a1b03b389ec763c4b";
        String iv = "77b07a672d57d64c";	
        Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");    
        // 加密
        byte[] raw = key.getBytes();
        SecretKeySpec skeySpec = new SecretKeySpec(raw, "AES");
        IvParameterSpec ivSpec = new IvParameterSpec(iv.getBytes());         
        cipher.init(Cipher.ENCRYPT_MODE, skeySpec, ivSpec);
        byte[] encrypted = cipher.doFinal(data.getBytes()); 
        //[90, -31, 86, -75, -100, -108, 2, -97, -56, -49, 55, 108, -2, 122, 41, -4]
        System.out.println(Arrays.toString(encrypted));
    }
}
```

### python

```python
# pip install pycryptodome
from Crypto.Cipher import AES
from Crypto.Util.Padding import pad

KEY = "fd6b639dbcff0c2a1b03b389ec763c4b"
IV = "77b07a672d57d64c"
def aes_encrypt(data_string):
    aes = AES.new(
        key=KEY.encode('utf-8'),
        mode=AES.MODE_CBC,
        iv=IV.encode('utf-8')
    )
    raw = pad(data_string.encode('utf-8'), 16)
    return aes.encrypt(raw)

data = aes_encrypt("武沛齐")
print(data)
print([i for i in data])
# [90, 225, 86, 181, 156, 148, 2, 159, 200, 207, 55, 108, 254, 122, 41, 252]
```

## base64编码(字节->字符串)

```java
import java.util.Base64;

public class Hello {
    public static void main(String[] args) {
        String name = "武沛齐";
        // 编码
        Base64.Encoder encoder  = Base64.getEncoder();
        String res = encoder.encodeToString(name.getBytes());
        System.out.println(res); // "5q2m5rK\n6b2Q"		
        // 解码
        Base64.Decoder decoder  = Base64.getDecoder();
        byte[] origin = decoder.decode(res);
        String data = new String(origin);
        System.out.println(data); // 武沛齐
    }
}
```

`Base64.encodeToString(byte[] byteArray, int flag)` flag有各种数字，可能是去掉换行符或=或=转义。打印输出结果和python结果进行比对再修改即可。

```python
import base64
name = "武沛齐"
res = base64.b64encode(name.encode('utf-8'))
print(res) # b'5q2m5rKb6b2Q'
data = base64.b64decode(res)
origin = data.decode('utf-8')
print(origin) # "武沛齐"
# 不同，换行符 + ==
```

## aes与base64

```python
from Crypto.Cipher import AES
from Crypto.Util.Padding import pad
import base64

def aes_encrypt(data_string, key):
    aes = AES.new(
        key=key.encode('utf-8'),
        mode=AES.MODE_ECB,
    )
    raw = pad(data_string.encode('utf-8'), 16)
    return aes.encrypt(raw)

data_string = "明文"
key = "key"
bytes_data = aes_encrypt(data_string, key)  # 字节类型
""" bytes_data
b'\x9f\x1bVbf\x12\xa73\x91\xe5\x90\xb3fN\xe6\xfb'
"""
#-处理成字节数组输出-----------------------------
result = [item for item in bytes_data]
# [90, 225, 86, 181, 156, 148, 2, 159, 200, 207, 55, 108, 254, 122, 41, 252]
print(result)
#------------------------------------------------
value = base64.encodebytes(bytes_data)
result = value.replace(b"\n", b'')  # python得到的结果会有\n, java的没有 要注意对比
print(result)
```











## sha256

### java

```java
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.Base64;

public class Hello {
    public static void main(String[] args) throws NoSuchAlgorithmException {
        String name = "武沛齐";
        MessageDigest instance = MessageDigest.getInstance("SHA-256");
        byte[] nameBytes = instance.digest(name.getBytes());
        // System.out.println(Arrays.toString(nameBytes));
        // String res = new String(nameBytes);
        // System.out.println(res);
        
        // 十六进制展示
        StringBuilder sb = new StringBuilder();
        for(int i=0;i<nameBytes.length;i++){
            int val = nameBytes[i] & 255;  // 负数转换为正数
            if (val<16){
                sb.append("0");
            }
            sb.append(Integer.toHexString(val));
        }
        String hexData = sb.toString();
        System.out.println(hexData); // 9841a685174241d957d28c49b868402d0170fdb7abf808af73cba60f70145fea
    }
}
```

### python

```python
import hashlib
data = "明文"
salt = "9cafa6466a028bfb"  # 盐
obj = hashlib.sha256()
# 按app的顺序update
obj.update(data.encode('utf-8'))
obj.update(salt.encode('utf-8'))
# --------------------------
res = obj.hexdigest()
print(res)
# e61583f49efa13187b053d2ab1cf2cc8cd99360367f42a6b7d013a49de72108e
```

## sha1

### java

```java
import java.security.MessageDigest;

MessageDigest messageDigest = MessageDigest.getInstance("sha-1");
messageDigest.update(str6.getBytes());
byte[] digest = messageDigest.digest();
// byte 2 hex -> arg7 = hash_object.hexdigest()
StringBuffer stringBuffer = new StringBuffer();
for (byte b : digest) {
    String lowerCase = Integer.toHexString(b & 255).toLowerCase(Locale.getDefault());
    if (lowerCase.length() < 2) {
        lowerCase = "0" + lowerCase;
    }
    stringBuffer.append(lowerCase);
}
deviceCode = stringBuffer.toString();
```

### python

```python
import hashlib
arg0 = "明文" 
hash_object = hashlib.sha1()
hash_object.update(arg0.encode('utf-8'))
arg7 = hash_object.hexdigest()
print(arg7)
```

### hook

```javascript
var MessageDigest = Java.use("java.security.MessageDigest");
var ByteString = Java.use("com.android.okhttp.okio.ByteString");
MessageDigest.update.overload("[B").implementation = function (data) {
    if (flag) {
        console.log(ByteString.of(data).utf8(), '\n' );
        console.log("---------------")
    }
    return this.update(data);
}
```





## md5

### java

```java
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.Base64;

public class Hello {
    public static void main(String[] args) throws NoSuchAlgorithmException {
        String name = "武沛齐";
        MessageDigest instance = MessageDigest.getInstance("MD5");
        byte[] nameBytes = instance.digest(name.getBytes());       
        // 十六进制展示
        StringBuilder sb = new StringBuilder();
        for(int i=0;i<nameBytes.length;i++){
            int val = nameBytes[i] & 255;  // 负数转换为正数
            if (val<16){
                sb.append("0");
            }
            sb.append(Integer.toHexString(val));
        }
        String hexData = sb.toString();
        System.out.println(hexData); // 17351012472429d52d0c0d23d468173d
    }
}
```

### python

```python
data_string = "武沛齐"
def md5_h(data_string):
    obj = hashlib.md5()
    obj.update(data_string.encode("utf-8"))
    # 原始的加密结果
    v1 = md5.digest()
	print(v1) # b'\x175\x10\x12G$)\xd5-\x0c\r#\xd4h\x17='
    # 得到十六进制字符串。java中没有这个功能
    hex_string = obj.hexdigest()
    # print(hex_string) # 17351012472429d52d0c0d23d468173d
    return hex_string
```

### 加盐

```java
    String name = "武沛齐";
    MessageDigest instance = MessageDigest.getInstance("MD5");
    instance.update("xxxxxx".getBytes());//只是多了这一行
```

```python
import hashlib
m = hashlib.md5("xxxxxx".encode('utf-8'))
m.update("武沛齐".encode("utf-8"))
v2 = m.hexdigest()
print(v2) # 17351012472429d52d0c0d23d468173d
```

## 隐藏字节

出现较少

`java`字节：有符号 `-128 ~ 127`
`python`：无符号  `0 ~ 255`

```java
String salt = "sign";
String v4 = new String(new byte[]{115, 105, 103, 110});
```

## java字节转python字符串

```python
byte_list = [-26, -83, -90, -26, -78, -101, -23, -67, -112]

bs = bytearray()  # python字节数组
for item in byte_list:
    # java字节数组先转成python的字节数组
    if item < 0:
        item = item + 256
    bs.append(item)

str_data = bs.decode('utf-8')  # data = bytes(bs)
print(str_data)
```

## 随机值

```java
import java.math.BigInteger;
import java.security.SecureRandom;

public class Hello {
    public static void main(String[] args) {
        // 随机生成80位，10个字节
        BigInteger v4 = new BigInteger(80, new SecureRandom());
        // 让字节以16进制展示
        String res = v4.toString(16);
        System.out.println(res); //20位
    }
}
```

**法一**

```python
import random
v4 = random.SystemRandom().getrandbits(80)
print(v4)
# 让整数以16进制展示
res = hex(v4)
print(res[2:])  # 0x646275f456bf0b942f08 
# 20位
# 646275f456bf0b942f08 
```

**法二**

```python
import secrets

data = secrets.token_bytes(10)
res = format(int.from_bytes(data, 'big'), '020x')
print(res)
```

*法三*

Python中采用的是字节作为随机数单位，而Java中采用的是位数（bit）作为随机数单位。因此，在Java中生成的随机数将始终是80个位（即20个十六进制字符），而在Python中生成的随机数可能会有不同的字节数，这将影响生成的十六进制字符串长度。

用下面这段代python码生成的结果可能不是20位(18/19/20)

```python
data = random.randbytes(10)  # python3.9
ele_list = []
for item in data:
    ele = hex(item)[2:]
    ele_list.append(ele)

res = "".join(ele_list)
print(res)
```

## 时间戳

```java
public class Hello {
    public static void main(String[] args) {
        // v1 = int(time.time())
        String t1 = String.valueOf(System.currentTimeMillis() / 1000);
        // v2 = int(time.time()*1000)
        String t2 = String.valueOf(System.currentTimeMillis());
        System.out.println(t1);
        System.out.println(t2);
    }
}
```

## 十六进制字符串

### java

```java
byte [] arg5 = {10, -26, -83, -90, -26, -78, -101, -23, -67, -112};
StringBuilder v0 = new StringBuilder();
int v1 = arg5.length;
int v2;
for(v2=0; v2 < v1; ++v2) {
    int v3 = arg5[v2] & 0xFF;
    if (v3 < 16) {
        v0.append('0');
    }
    v0.append(Integer.toHexString(v3));
}
System.out.println(v0.toString()); //0ae6ada6e6b29be9bd90
```

#### byte 2 hex

```java
StringBuffer stringBuffer = new StringBuffer();
for (byte b : digest) {
    String lowerCase = Integer.toHexString(b & 255).toLowerCase(Locale.getDefault());
    if (lowerCase.length() < 2) {
        lowerCase = "0" + lowerCase; // 不满两位补0
    }
    stringBuffer.append(lowerCase);
}
deviceCode = stringBuffer.toString();
```

### python

```python
# name_bytes = "武沛齐".encode('utf-8')
name_bytes = [10, -26, -83, -90, -26, -78, -101, -23, -67, -112]

data_list = []

for item in name_bytes:
    item = item & 0xff   # item<0时，让item+256
    ele = "%02x" % item
    data_list.append(ele)
    
print("".join(data_list))  # 0ae6ada6e6b29be9bd90
```

## 生成uuid

```java
import java.util.UUID;

public class Hello {
    public static void main(String[] args){
        String uid = UUID.randomUUID().toString();
        System.out.println(uid);
    }
}
```

```python
import uuid

uid = str(uuid.uuid4())
print(uid)
```

# frida常用脚本

```javascript
function hook() {
    Java.perform(function () {
        								//包名类名
        var LoginActivity = Java.use("com.xxx....Activity.LoginActivity");
        // 上面的变量.函数名.implementation    返回值的类型对应上
        LoginActivity.a.implementation = function(str, str2) { 
            var result = this.a(str, str2);
            console.log("LoginActivity.a->",str, str2, result);
            return result;
        }
    });
}
function main() {
    hook();
}
setImmediate(main);
```

注：用命令行运行的方式貌似不能同时两个脚本运行。。。想要两个脚本同时运行，其中一个用python，另一个用命令行即可；或者都用python执行

* 记得写`Java.perform(function(){...})`不要忘了 否则提示找不到类com.xx.xx

## so层

### 输出

输出地址中的字符串

```javascript
Memory.readCString(args[1]);
```

```javascript
args[1].readUtf8String();
```

上面如果输出乱码，可以十六进制形式输出

```javascript
hexdump(args[2]);
```

输出如下

```assembly
             0  1  2  3  4  5  6  7  8  9  A  B  C  D  E  F  0123456789ABCDEF
75de1c7b98  68 0b 05 13 30 01 29 15 d8 0b 05 13 00 00 00 00  h...0.).........
75de1c7ba8  90 84 1c de 75 00 00 00 ae 0a 00 00 ae 0a 00 00  ....u...........
75de1c7bb8  00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00  ................
75de1c7bc8  00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00  ................
75de1c7bd8  00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00  ................
75de1c7be8  00 00 00 00 00 00 00 00 00 54 5b 01 76 00 00 00  .........T[.v...
75de1c7bf8  00 00 00 00 00 00 00 00 00 54 5b 01 76 00 00 00  .........T[.v...
```

减少一部分输出

```javascript
hexdump(args[2], {length:16, header:false});
```



### 打印用到的so文件

使用场景：反调试 看看到了哪里被检测 找到so文件 删掉试试 没大碍就ok 如果不行另寻他路

```javascript
Java.perform(function () {
    var dlopen = Module.findExportByName(null, "dlopen"); // 系统文件
    var android_dlopen_ext = Module.findExportByName(null, "android_dlopen_ext");
    
    Interceptor.attach(dlopen, {
        onEnter: function (args) {
            var path_ptr = args[0];
            var path = ptr(path_ptr).readCString();
            console.log("[dlopen:]", path);
        },
        onLeave: function (retval) {
    
        }
    });
    
    Interceptor.attach(android_dlopen_ext, {
        onEnter: function (args) {
            var path_ptr = args[0];
            var path = ptr(path_ptr).readCString();
            console.log("[dlopen_ext:]", path);
        },
        onLeave: function (retval) {
    
        }
    });
});
// spawn mode
// frida -Uf com.xxx.xxxx -l hook.js  15.2.2版本不可使用
// frida -U -l hook.js -f com.xxx.xxxx --no-pause 用这个代替
```

### :small_red_triangle:定位RegisterNatives+使用内存地址hook

`indexOf`判断是否包含，不包含返回`-1`；包含返回`索引位置`

```javascript
function hook_RegisterNatives() {
    // 列举 libart.so 中的所有导出函数（成员列表）
    var symbols = Module.enumerateSymbolsSync("libart.so");
    // 获取 RegisterNatives 函数的内存地址，并赋值给addrRegisterNatives
    var addrRegisterNatives = null;
    for (var i = 0; i < symbols.length; i++) {
        var symbol = symbols[i]; // 成员对象

        // _ZN3art3JNI15RegisterNativesEP7_JNIEnvP7_jclassPK15JNINativeMethodi
        // 方式1：
        if (symbol.name.indexOf("art") >= 0 &&
            symbol.name.indexOf("JNI") >= 0 &&
            symbol.name.indexOf("RegisterNatives") >= 0 &&
            symbol.name.indexOf("CheckJNI") < 0) {
            addrRegisterNatives = symbol.address;
            console.log("RegisterNatives is at ", symbol.address, symbol.name);
        }

        // 方式2：
        var name = "_ZN3art3JNI15RegisterNativesEP7_JNIEnvP7_jclassPK15JNINativeMethodi";
        if(symbol.name.indexOf("art") >= 0){
            if(symbol.name.indexOf(name)>=0){
                addrRegisterNatives = symbol.address;
            }
        }
    }


    if (addrRegisterNatives != null) {
        //      这里写某函数的内存地址
        Interceptor.attach(addrRegisterNatives, {
            onEnter: function (args) {
                var env = args[0]; // jni对象 env
                var java_class = args[1]; // 类 jclass
                var class_name = Java.vm.tryGetEnv().getClassName(java_class);
                //console.log(class_name); //得到类名
                // 只有类名为com.bilibili.nativelibrary.LibBili，才打印输出
                var taget_class = "com.bilibili.nativelibrary.LibBili";
                if (class_name === taget_class) {
                    console.log("\n[RegisterNatives] method_count:", args[3]);
                    // args[2] 就是动态注册的对应关系。
                    // ptr是new NativePointer(s) 的缩写。(C语言中的指针)
                    var methods_ptr = ptr(args[2]);
                    var method_count = parseInt(args[3]);

                    for (var i = 0; i < method_count; i++) {
                        // Java中函数名字的
                        var name_ptr = Memory.readPointer(methods_ptr.add(i * Process.pointerSize * 3));
                        // 参数和返回值类型
                        var sig_ptr = Memory.readPointer(methods_ptr.add(i * Process.pointerSize * 3 + Process.pointerSize));
                        // C中的函数指针
                        var fnPtr_ptr = Memory.readPointer(methods_ptr.add(i * Process.pointerSize * 3 + Process.pointerSize * 2));
                        var name = Memory.readCString(name_ptr); // 读取java中函数名
                        var sig = Memory.readCString(sig_ptr); // 参数和返回值类型 e.g.(Ljava/lang/String;)Ljava/lan/String
                        var find_module = Process.findModuleByAddress(fnPtr_ptr); // 根据C中函数指针获取模块
                        // 偏移量(hex) = fnPtr_ptr - 模块基地址
                        var offset = ptr(fnPtr_ptr).sub(find_module.base)
                        // console.log("[RegisterNatives] java_class:", class_name);
                        console.log("name:", name, "sig:", sig, "module_name:", find_module.name, "offset:", offset);
                    }
                }
            }
        });
    }
}

setImmediate(hook_RegisterNatives);
// frida -U -f com.xingin.xhs  -l 8.dynamic_find_so.js
```

### :large_blue_diamond:hook c 函数

```c
size_t v27;
char *v32;
char v36[88];
sub_22B0(v36, v32, v27);
```

地址：ida32位 `22B0+1`	64位: `22B0`

```javascript
Java.perform(function () {
    // 找到so的基址
    var libbili = Module.findBaseAddress("libbili.so");
    // 32位的ida libbili.add(0x22b0).add(0x1);
	var s_func = libbili.add(0x22b0 + 1);
    console.log(s_func);
    Interceptor.attach(s_func, {
        onEnter: function (args) {
            // args[0] v36
            // args[1]，明文字符串 v32
            // args[2]，明文字符串长度 v27
            console.log("执行update，长度是：",args[2], args[2].toInt32());
            // console.log(hexdump(args[1], {length: args[2].toInt32()})); //当args[转不了字符串时 输出十六进制
            console.log(args[1].readUtf8String())
        },
        onLeave: function (args) {
            console.log("=======================over===================");
        }
    });
});
//frida -UF -l hook.js 用attach模式
```

### hook 导出函数

```javascript
Java.perform(function () {
  	// hook naive function
    //去内存中找，但native的这个函数是在java中通过反射(invoke)调用的
    //而这时内存中还没有加载这个函数 会返回null
    //所以hook的时候不能太早
    //手机 点了"同意",再 frida -UF -l hook_native_getByteHash.js
    var addr = Module.findExportByName("libkeyinfo.so", "getByteHash");
    console.log(addr);
    if (addr){
        Interceptor.attach(addr,{
            onEnter:function (args) {
                //args[2]) 的类型是指针
                this.x1 = args[2];
                // this.x2 = args[3];
                // console.log(Memory.readCString(args[2]));
                // console.log(args[3].toInt32());
            },onLeave:function (retval) {//返回
                console.log("------------getByteHash-ret----------")
                console.log(Memory.readCString(this.x1));
                console.log(Memory.readCString(retval));
            }
        })
    }
});
```

### hook NewStringUTF()

某参数为 `xxx: XYAAAAAQAAAAEAAAB......` 用jadx搜 `xxx`搜不到关键字。怀疑是在c层生成的。因为开头固定是`XYAAAAAQAAAAEAAAB` 所以查找以它开头的字符串

```javascript
var symbols = Module.enumerateSymbolsSync("libart.so"); // 找系统内部so
var addrNewStringUTF = null;
for (var i = 0; i < symbols.length; i++) {
    var symbol = symbols[i];

    if (symbol.name.indexOf("NewStringUTF") >= 0 && symbol.name.indexOf("CheckJNI") < 0) {
        addrNewStringUTF = symbol.address;
        console.log("NewStringUTF is at ", symbol.address, symbol.name);
    }
}

if (addrNewStringUTF != null) {
    Interceptor.attach(addrNewStringUTF, {
        onEnter: function (args) {
            var c_string = args[1];
            var dataString = c_string.readCString();

            if (dataString.indexOf("XYAAAAAQAAAAEAAAB") != -1) {
                console.log(dataString);
                console.log(Thread.backtrace(this.context, Backtracer.ACCURATE).map(DebugSymbol.fromAddress).join('\n') + '\n');
                console.log(Java.use("android.util.Log").getStackTraceString(Java.use("java.lang.Throwable").$new()));
            }
        }
    });
}

// frida -UF  -l  1.so_utfstring.js -o token.txt
```



### :large_blue_diamond:寻找so文件

#### 静态注册

```javascript
Java.perform() {
    var dlsymadd = Module.findExportByName("libdl.so", 'dlsym');
    Interceptor.attach(dlsymadd, {
        onEnter: function(args) {
            this.info = args[1]; // Java_包名_类名_方法名
        }, onLeave: function(retval) {
            // the so file: module.name
            var module = Process.findModuleByAddress(retval);
            if (module == null) {
                return retval;
            }
            // native method
            var funcName = this.info.readCString();
            // 修改这里的方法名即可
            if (funcName.indexOf("getHNASignature") !== -1) {
                console.log(module.name);
                console.log('\t', funcName);
            }
            return retval;
        }
    })
}
```

#### 动态注册

```javascript
var symbols = Module.enumerateSymbolsSync("libart.so");
var addrRegisterNatives = null;
for (var i = 0; i < symbols.length; i++) {
    var symbol = symbols[i];
    if (symbol.name.indexOf("art") >= 0 &&
        symbol.name.indexOf("JNI") >= 0 &&
        symbol.name.indexOf("RegisterNatives") >= 0 &&
        symbol.name.indexOf("CheckJNI") < 0) {
        addrRegisterNatives = symbol.address;
        console.log("RegisterNatives is at ", symbol.address, symbol.name);
    }
}
console.log("addrRegisterNatives=", addrRegisterNatives);

if (addrRegisterNatives != null) {
    Interceptor.attach(addrRegisterNatives, {
        onEnter: function (args) {
            var env = args[0];
            var java_class = args[1];
            var class_name = Java.vm.tryGetEnv().getClassName(java_class);
            
            // 只有类名为com.xunmeng.pinduoduo.secure.DeviceNative，才打印输出
            // native 在 jadx 里显示的类 只改这里就行
            var taget_class = "com.xunmeng.pinduoduo.secure.DeviceNative";
            
            if (class_name === taget_class) {
                console.log("\n[RegisterNatives] method_count:", args[3]);
                var methods_ptr = ptr(args[2]);
                var method_count = parseInt(args[3]);

                for (var i = 0; i < method_count; i++) {
                    // Java中函数名字的
                    var name_ptr = Memory.readPointer(methods_ptr.add(i * Process.pointerSize * 3));
                    // 参数和返回值类型
                    var sig_ptr = Memory.readPointer(methods_ptr.add(i * Process.pointerSize * 3 + Process.pointerSize));
                    // C中的函数指针
                    var fnPtr_ptr = Memory.readPointer(methods_ptr.add(i * Process.pointerSize * 3 + Process.pointerSize * 2));

                    var name = Memory.readCString(name_ptr); // 读取java中函数名
                    var sig = Memory.readCString(sig_ptr); // 参数和返回值类型
                    var find_module = Process.findModuleByAddress(fnPtr_ptr); // 根据C中函数指针获取模块

                    var offset = ptr(fnPtr_ptr).sub(find_module.base) // fnPtr_ptr - 模块基地址
                    // console.log("[RegisterNatives] java_class:", class_name);
                    console.log("name:", name, "sig:", sig, "module_name:", find_module.name, "offset:", offset);
                    //console.log("name:", name, "module_name:", find_module.name, "offset:", offset);
                }
            }
        }
    });
}

// frida -U -f  com.xunmeng.pinduoduo  -l dynamic_find_so.js
```

### 延迟hook

#### 利用系统底层检测是否加载并hook(和下面下面重复了)

当某些so文件还没加载时，hook会报错。用这个脚本可以避免报错，不用在hook的时候拼手速找时机

```javascript
function do_hook() {
    var addr = Module.findExportByName("libkeyinfo.so", "getByteHash");
    console.log(addr); //0xb696387d
    Interceptor.attach(addr, {
        onEnter: function (args) {
            this.x1 = args[2];
        },
        onLeave: function (retval) {
            console.log("--------------------")
            console.log(Memory.readCString(this.x1));
            console.log(Memory.readCString(retval));
        }
    })
}

function load_so_and_hook() {
    var dlopen = Module.findExportByName(null, "dlopen");
    var android_dlopen_ext = Module.findExportByName(null, "android_dlopen_ext");

    Interceptor.attach(dlopen, {
        onEnter: function (args) {
            var path_ptr = args[0];
            var path = ptr(path_ptr).readCString();
            console.log("[dlopen:]", path);
            this.path = path;
        }, onLeave: function (retval) {
            if (this.path.indexOf("libkeyinfo.so") !== -1) { // 如果包含我想要的so文件
                console.log("[dlopen:]", this.path);
                do_hook();

            }
        }
    });

    Interceptor.attach(android_dlopen_ext, {
        onEnter: function (args) {
            var path_ptr = args[0];
            var path = ptr(path_ptr).readCString();

            this.path = path;
        }, onLeave: function (retval) {
            if (this.path.indexOf("libkeyinfo.so") !== -1) {
                console.log("\nandroid_dlopen_ext加载：", this.path);
                do_hook();
            }
        }
    });
}
load_so_and_hook();
// frida -U -f com.achievo.vipshop -l delay_hook.js
```

如果上面还是报错找不到，就在do_hook()再延迟以下

```javascript
function do_hook() {
    setTimeout(function () { // 延时
        Java.perform(function () {
            var XhsHttpInterceptor = Java.use('com.xingin.shield.http.XhsHttpInterceptor');
            XhsHttpInterceptor.initialize.implementation = function (str) {
                console.log("str=", str);
                return this.initialize(str);
            };
        })
    }, 10); // delay 10 ms
}
```



#### 用时间延迟

如果上面的方法还是hook不到

```javascript
setTimeout(function() {
    // code
}, 1000) // delay one second
```

```javascript
setTimeout((x, y)=>{
    console.log(x + y);
}, 3000, 1, 2);
```

第一个参数是一个匿名函数，在第三个参数位置传递了两个参数值1, 2分别代表x和y的值，因此在3秒后会打印出3这个信息。

#### 检测文件是否加载

```javascript
function do_hook() {
	... // hook脚本
}

function delay_hook(so_name, hook_func) {
    var dlopen = Module.findExportByName(null, "dlopen");
    var android_dlopen_ext = Module.findExportByName(null, "android_dlopen_ext");

    Interceptor.attach(dlopen, {
        onEnter: function (args) {
            var path_ptr = args[0];
            var path = ptr(path_ptr).readCString();
            // console.log("[dlopen:]", path);
            this.path = path;
        }, onLeave: function (retval) {
            if (this.path.indexOf(so_name) !== -1) {
                console.log("[dlopen:]", this.path);
                hook_func();
            }
        }
    });

    Interceptor.attach(android_dlopen_ext, {
        onEnter: function (args) {
            var path_ptr = args[0];
            var path = ptr(path_ptr).readCString();
            this.path = path;
        },
        onLeave: function (retval) {
            if (this.path.indexOf(so_name) !== -1) {
                console.log("\nandroid_dlopen_ext加载：", this.path);
                hook_func();
            }
        }
    });
}

delay_hook("libkeyinfo.so", do_hook);
// frida -U -f  com.achievo.vipshop -l  hook.js
```



## java层

### 调用栈

```javascript
console.log(Java.use("android.util.Log").getStackTraceString(Java.use("java.lang.Throwable").$new()));
```

### hook拦截器

使用 JSON.stringify(inter) 把对象是什么打印出来 可以知道是哪些类实例化了拦截器。app需要重启：拦截器的注册在app初始的时候执行，而不是每次发送请求都注册一遍。

```javascript
Java.perform(function () {
    var Builder = Java.use('okhttp3.OkHttpClient$Builder');
    Builder.addInterceptor.implementation = function (inter) {
        //console.log("实例化：");
        console.log(JSON.stringify(inter));
        //console.log(Java.use("android.util.Log").getStackTraceString(Java.use("java.lang.Throwable").$new()));
        return this.addInterceptor(inter);
    };
})
// spawn 不要用 attach Interceptor可能在app启动的时候加载
// frida -U -l hook.js -f com.xxx.xxxx --no-pause
// frida -Uf com.hupu.shihuo -l hook.js -o all_interceptor.txt  15.2.2版本不可用
```
输出 eg. 从下往上找   记得手机上点击触发
```
            ...
            "<instance: okhttp3.Interceptor, $className: cn.shihuo.modulelib.startup.core.c.b>"
            "<instance: okhttp3.Interceptor, $className: cn.shihuo.modulelib.startup.core.c.a>"
```

```javascript
Java.perform(function () {
   // ... 根据上面的输出 找到为止
    var a9 = Java.use('cn.shihuo.modulelib.startup.core.c.b');
    a9.intercept.implementation = function (chain) {
        var request = chain.request();
        var urlString = request.url().toString();
        if(urlString.indexOf("https://sh-gateway.shihuo.cn/v4/services/sh-goodsapi/app_swoole_zone/getAttributes/v")!= -1){ // 过滤目标url
            console.log("拦截器9-->", urlString);
        }
        var response = chain.proceed(request); // 不执行当前拦截器 而走下一个拦截器
        return response;
    };

    var a10 = Java.use('cn.shihuo.modulelib.startup.core.c.a');
    a10.intercept.implementation = function (chain) {
        //console.log("拦截器10", chain);
        var request = chain.request();
        var urlString = request.url().toString();
        if(urlString.indexOf("https://sh-gateway.shihuo.cn/v4/services/sh-goodsapi/app_swoole_zone/getAttributes/v") != -1){
            console.log("拦截器10-->", urlString);
        }
        //console.log("拦截器",this.b.value);
        var res = this.intercept(chain);
        //console.log(Java.use("android.util.Log").getStackTraceString(Java.use("java.lang.Throwable").$new()));
        return res;
    }
})
// frida -UF -l hook.js
```

查看拦截器中Request的header/body等----okhttp3的通法

```javascript
Java.perform(function () {
    var XhsHttpInterceptor = Java.use('p.d0.v1.e0.n0.h');
    var Buffer = Java.use("okio.Buffer");
    var Charset = Java.use("java.nio.charset.Charset");

    XhsHttpInterceptor.intercept.overload('okhttp3.Interceptor$Chain').implementation = function (chain, j2) {
        console.log('\n--------------------请求来了--------------------');
        var request = chain.request();

        var urlString = request.url().toString();
        console.log("网址：")
        console.log(urlString)
        console.log("\n请求头：")
        console.log(request.headers().toString());

        var requestBody = request.body();
        if (requestBody) {
            var buffer = Buffer.$new();
            requestBody.writeTo(buffer);
            console.log("请求体：")
            // 有可能是乱码。用h
            console.log(buffer.readString(Charset.forName("utf8")));
        }

        var res = this.intercept(chain);
        return res;
    };
})
//  frida -UF  -l next_request.js
```



### hook Map

TreeMap

```javascript
Java.perform(function () {
    var TreeMap = Java.use('java.util.TreeMap');
    var Map = Java.use("java.util.Map");

    TreeMap.put.implementation = function (key,value) {
        if(key=="xxx"){ // 根据需要 看抓包
            console.log(key,value);
        }
        var res = this.put(key,value);
        return res;
    }
});

// frida -UF -l hook.js
// frida -U -l hook.js -f com.xx.xx --no-pause
```

通用脚本

```javascript
Java.perform(function () {
    function showMap(title,map){
        var result = "{";
        var keyset = map.keySet();
        var it = keyset.iterator();
        while(it.hasNext()){
            var keystr = it.next().toString();
            var valuestr = map.get(keystr).toString();
            result += '"' + keystr + '"';
            result += ":";
            result +=  '"' + valuestr + '"';
            result += ",";
        }
        result += "}";
        console.log(title, result);
    }
    
    // 这里是使用的例子，不要全抄过去了
    var EncryptTool = Java.use("com.jingdong.common.network.encrypt.EncryptTool");
    EncryptTool.encrypt.implementation = function(map){
        console.log("-----------------------");
        showMap("map字典->", map);
        var res = this.encrypt(map);
        console.log('返回值-->',res);
        // console.log(Java.use("android.util.Log").getStackTraceString(Java.use("java.lang.Throwable").$new()));
        return res;
    }
});

// frida -UF -l hook.js
// frida -U -l hook.js -f com.xx.xx --no-pause
```

使用cast转换

```javascript
Java.perform(function () {
    var EncryptTool = Java.use("com.jingdong.common.network.encrypt.EncryptTool");
    var HashMap = Java.use("java.uril.HashMap");
    EncryptTool.encrypt.implementation = function(map){
        console.log("-----------------------");
		console.log("map=", Json.stringfy(map));//map="<instance: java.util.Map, $className: java.util.HashMap>"
        console.log("map=", map.toString());//map=[object Object]
        var real_object = Java.cast(map, HashMap);
        console.log("map=", real_object.toString());//map={d_bodel=Pixel4,wifiBssidunknown...}
        var res = this.encrypt(map);
        console.log('返回值-->',res);
        // console.log(Java.use("android.util.Log").getStackTraceString(Java.use("java.lang.Throwable").$new()));
        return res;
    }
});
```





### hook StringBuilder

```javascript
Java.perform(function () {
    var StringBuilder = Java.use("java.lang.StringBuilder");
    StringBuilder.toString.implementation = function () {
        var res = this.toString();
        console.log(res); 
        return res;
    }
});
```

### hook Base64

AES常与base64搭配

```javascript
Java.perform(function () {
    var Base64 = Java.use("android.util.Base64");
    Base64.encodeToString.overload('[B', 'int').implementation = function (bArr,val) {
        var res = this.encodeToString(bArr,val);
        console.log("[*] base64 res ->",res);
        // console.log(Java.use("android.util.Log").getStackTraceString(Java.use("java.lang.Throwable").$new()));
        return res;
    }
});
```

### hook no_Proxy

如果代码被混淆 这个脚本不可用

```javascript
function f() {
    Java.perform(function(){
        var Builder = Java.use("okhttp3.OkHttpClient$Builder");
        Builder.proxy.implementation = function(proxy){
            var res = this.proxy(null);
            return res;
        }
    })
}
setImmediate(f);
// frida -U -f com.xx.xx -l hook-proxy.js
```

### hook构造方法

```java
import javax.crypto.spec.SecretKeySpec;
import javax.crypto.spec.IvParameterSpec
...
public final class b {
    public final byte[] a(String body) {
        x.q(body, "body");
        try { // private static final String b
            String str = b; // private static final String b
            Charset charset = com.bilibili.commons.c.b;
            x.h(charset, "Charsets.UTF_8");
            if (str != null) {
                byte[] bytes = str.getBytes(charset); // bytes from str
                // 将此String解码使用平台的默认字符集，并将结果存储到一个新的字节数组中的字节序列
                x.h(bytes, "(this as java.lang.String).getBytes(charset)");
                SecretKeySpec secretKeySpec = new SecretKeySpec(bytes, "AES"); // 系统包 from bytes
                String str2 = f22911c;
                Charset charset2 = com.bilibili.commons.c.b;
                x.h(charset2, "Charsets.UTF_8");
                if (str2 != null) {
                    byte[] bytes2 = str2.getBytes(charset2); // bytes2 from str2
                    x.h(bytes2, "(this as java.lang.String).getBytes(charset)");
                    IvParameterSpec ivParameterSpec = new IvParameterSpec(bytes2); // iv from bytes2
                    Charset charset3 = com.bilibili.commons.c.b;
                    x.h(charset3, "Charsets.UTF_8");
                    byte[] bytes3 = body.getBytes(charset3);
                    x.h(bytes3, "(this as java.lang.String).getBytes(charset)");
                    byte[] i = com.bilibili.droid.g0.a.i(secretKeySpec, ivParameterSpec, bytes3); // key, iv, byte
                    x.h(i, "AES.encryptToBytes(Secre…yteArray(Charsets.UTF_8))");
                    return i;
                }
                throw new TypeCastException("null cannot be cast to non-null type java.lang.String");
            }
            throw new TypeCastException("null cannot be cast to non-null type java.lang.String");
        } catch (Exception e2) {
            BLog.e(a, e2);
            Charset charset4 = com.bilibili.commons.c.b;
            x.h(charset4, "Charsets.UTF_8");
            byte[] bytes4 = body.getBytes(charset4);
            x.h(bytes4, "(this as java.lang.String).getBytes(charset)");
            return bytes4;
        }
    }
}
```
### 绕过客户端校验

代码见 `frida_multiple_unpinning.js`

第三方包可以被混淆，此脚本失效。

客户端证书校验：
	-\ 证书
	-\ Host
	-\ pinner

解决方法：找源码。输出log `Log.e("调用栈", Log.getStackTraceString(new Throwable()));`

在上述调用栈中发现在证书校验时，底层会走 `com.android.org.conscrypt.NativeSsl`的`doHandshake`方法。

所以，可以Hook他，根据调用栈向上找到证书校验的位置（其他验证也在旁边） 找到`RealConnection`类的`connectTls`方法

pinner校验

```javascript
Java.perform(function () {
    var NativeSsl = Java.use('com.android.org.conscrypt.NativeSsl');
    NativeSsl.doHandshake.overload('java.io.FileDescriptor', 'int').implementation = function (a, b) {
        console.log("参数：", a, b);
        console.log(Java.use("android.util.Log").getStackTraceString(Java.use("java.lang.Throwable").$new()));
        return this.doHandshake(a, b);
    };
});

// frida -UF -l 1.hook_check.js
```

证书校验

```javascript
Java.perform(function () {
    var Platform = Java.use('com.android.org.conscrypt.Platform');
    Platform.checkServerTrusted.overload('javax.net.ssl.X509TrustManager', '[Ljava.security.cert.X509Certificate;', 'java.lang.String', 'com.android.org.conscrypt.AbstractConscryptSocket').implementation = function (x509tm, chain, authType, socket) {
        console.log('\n[+] checkServer  ',x509tm,JSON.stringify(x509tm) );//输出tm可定位到真实的使用位置

        //return this.checkServerTrusted(x509tm, chain, authType, socket);
    };
});

// frida -U -f 包名 -l 6.hook_check.js --no-pause
// frida -UF -l 6.hook_check.js
```

主机校验

见tips/抓不到包

### 服务端校验

405

可能会获取到多个。因为手机内部服务或第三方服务也可能有证书，这些一般没有密码，可以根据这种情况来区分；也可以通过调用栈来看它是否是系统包。

```javascript
Java.perform(function () {
    var KeyStore = Java.use("java.security.KeyStore");
    var NativeSecret = Java.use("com.scorpio.common.NativeSecret");

    KeyStore.load.overload('java.io.InputStream', '[C').implementation = function (v1,v2) {
        var pwd = Java.use("java.lang.String").$new(v2)；
        console.log("类型:" + this.getType());
        console.log("密码:" + pwd);
        console.log(JSON.stringify(v1));
        var res = this.load(v1,v2);
        return res;
    };

    NativeSecret.getSslPassword.implementation = function (ctx) {
        var res = this.getSslPassword(ctx);
        console.log("密码=>",res);
        return res;
    }
});
// frida -U -f com.xh.xinghe -l hook_password.js --no-pause -o secret.txt
```

#### 导出bks证书

注意：在手机上一定要先给当前app开启可以操作硬盘的权限，否则无法导出证书文件。

```javascript
Java.perform(function () {
    var KeyStore = Java.use("java.security.KeyStore");
    var String = Java.use("java.lang.String");
    KeyStore.load.overload('java.io.InputStream', '[C').implementation = function (inputStream, v2) {
        var pwd = String.$new(v2);
        console.log('\n------------')
        console.log("密码：" + pwd, this.getType());

        if (this.getType() === "BKS") { // "PKCS12"
            var myArray = new Array(1024);
            for (var i = 0; i < myArray.length; i++) {
                myArray[i] = 0x0;
            }
            var buffer = Java.array('byte', myArray);

            var file = Java.use("java.io.File").$new("/sdcard/Download/paopao-" + new Date().getTime() + ".bks");  // ".p"
            var out = Java.use("java.io.FileOutputStream").$new(file);
            var r;
            while ((r = inputStream.read(buffer)) > 0) {
                out.write(buffer, 0, r);
            }
            console.log("save success!")
            out.close()
        }
        var res = this.load(inputStream, v2);
        return res;
    };
});

// frida -U -f com.paopaotalk.im -l hook_save.js
```

```javascript
Java.perform(function () {
    function uuid(len, radix) {
        var chars = '0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz'.split('');
        var uuid = [], i;
        radix = radix || chars.length;

        if (len) {
            // Compact form
            for (i = 0; i < len; i++) uuid[i] = chars[0 | Math.random() * radix];
        } else {
            // rfc4122, version 4 form
            var r;

            // rfc4122 requires these characters
            uuid[8] = uuid[13] = uuid[18] = uuid[23] = '-';
            uuid[14] = '4';

            // Fill in random data. At i==19 set the high bits of clock sequence as
            // per rfc4122, sec. 4.1.5
            for (i = 0; i < 36; i++) {
                if (!uuid[i]) {
                    r = 0 | Math.random() * 16;
                    uuid[i] = chars[(i == 19) ? (r & 0x3) | 0x8 : r];
                }
            }
        }

        return uuid.join('');
    }

    function storeP12(pri, p7, p12Path, p12Password) {
        var X509Certificate = Java.use("java.security.cert.X509Certificate")
        var p7X509 = Java.cast(p7, X509Certificate);
        var chain = Java.array("java.security.cert.X509Certificate", [p7X509])
        var ks = Java.use("java.security.KeyStore").getInstance("PKCS12", "BC");
        ks.load(null, null);
        ks.setKeyEntry("client", pri, Java.use('java.lang.String').$new(p12Password).toCharArray(), chain);
        try {
            var out = Java.use("java.io.FileOutputStream").$new(p12Path);
            ks.store(out, Java.use('java.lang.String').$new(p12Password).toCharArray())
        } catch (exp) {
            console.log(exp)
        }
    }

    //在服务器校验客户端的情形下，帮助dump客户端证书，并保存为p12的格式，证书密码为r0ysue
    Java.use("java.security.KeyStore$PrivateKeyEntry").getPrivateKey.implementation = function () {
        var result = this.getPrivateKey()
        var packageName = Java.use("android.app.ActivityThread").currentApplication().getApplicationContext().getPackageName();
        storeP12(this.getPrivateKey(), this.getCertificate(), '/sdcard/Download/' + packageName + uuid(10, 16) + '.p12', 'r0ysue');
        return result;
    }

    Java.use("java.security.KeyStore$PrivateKeyEntry").getCertificateChain.implementation = function () {
        var result = this.getCertificateChain()
        var packageName = Java.use("android.app.ActivityThread").currentApplication().getApplicationContext().getPackageName();
        storeP12(this.getPrivateKey(), this.getCertificate(), '/sdcard/Download/' + packageName + uuid(10, 16) + '.p12', 'r0ysue');
        return result;
    }
});


// frida -U -f com.xh.xinghe -l p12.js --no-pause
```

#### hook key

```javascript
Java.perform(function () {
    var ByteString = Java.use('com.android.okhttp.okio.ByteString');
    var SecretKeySpec = Java.use('javax.crypto.spec.SecretKeySpec');
    SecretKeySpec.$init.implementation = function (key, name) {
        console.lot("key->", ByteString.of(key).utf8());// 字节转成字符串
        var res = this.$init(key, name);
        return res;
    }
})
```

#### hook iv

```javascript
Java.perform(function () {
    var ByteString = Java.use('com.android.okhttp.okio.ByteString');
    var IvParameterSpec = Java.use('javax.crypto.spec.IvParameterSpec');
    IvParameterSpec.$init.implementation = function (bytes) {  // iv
        console.lot("iv->", ByteString.of(key).utf8());// 字节转成字符串
        var res = this.$init(bytes);
        return res;
    }
})
```

### aes相关

与上面重复了。只是格式化输出做得比较好。可用于下方提到的c构造类，在so中调用的java方法，在hook的时候直接按照hook java的方法来操作。$init.overload不一定有重载，这里是特例。看上面例子就没有重载。

```javascript
Java.perform(function () {
    var ByteString = Java.use("com.android.okhttp.okio.ByteString");
    var SecretKeySpec = Java.use("javax.crypto.spec.SecretKeySpec");
    SecretKeySpec.$init.overload('[B', 'java.lang.String').implementation = function (key, name) {
        if (name === 'AES') {
            console.log("-----------------------SecretKeySpec---------------------------");
            console.log("AES key bytes=", JSON.stringify(key));
            console.log("AES key hex=", ByteString.of(key).hex());
            console.log("AES key str=", ByteString.of(key).utf8());
        }
        var res = this.$init(key, name);
        return res;
    }
    
    var IvParameterSpec = Java.use("javax.crypto.spec.IvParameterSpec");
    IvParameterSpec.$init.overload('[B').implementation = function (iv) {
        console.log("-----------------------IvParameterSpec---------------------------");
        console.log("iv byte", JSON.stringify(iv));
        console.log("iv hex=", ByteString.of(iv).hex());
        console.log("iv str=", ByteString.of(iv).utf8());
        
        var res = this.$init(iv);
        return res;
    }
});
```

完整的hook key,iv,明文,密文。原理参见常见加密-java

```javascript
Java.perform(function () {
    var Cipher = Java.use("javax.crypto.Cipher");
    var ByteString = Java.use("com.android.okhttp.okio.ByteString");
    
    var Cipher = Java.use("javax.crypto.Cipher");
    var IvParameterSpec = Java.use("javax.crypto.spec.IvParameterSpec");

    Cipher.init.overload('int', 'java.security.Key', 'java.security.spec.AlgorithmParameterSpec').implementation = function (mode, key, iv) {
        console.log("-----------------------Cipher.init---------------------------");
        console.log("Cipher.init=", mode, key, iv);
        
        console.log("Cipher.key=", key.getEncoded());
        console.log("Cipher.key=", ByteString.of(key.getEncoded()).hex());
        
        console.log("Cipher.iv=", Java.cast(iv, IvParameterSpec).getIV());
        //										转换类型
        console.log("Cipher.iv=", ByteString.of(Java.cast(iv, IvParameterSpec).getIV()).hex());     
        this.init(mode, key, iv);
    }
    
    Cipher.doFinal.overload('[B').implementation = function (data) {
        console.log("-----------------------AES加密---------------------------");
        console.log("AES明文：",ByteString.of(data).utf8());      
        var res = this.doFinal(data);   
        console.log('AES密文：',ByteString.of(res).hex());
        return res;
    }

});
```





### 主动调用

```java
import kotlin.g0.q;
...
public final class a {
    public static final String b(String fpEntity) {
        k n1;
        i S0;
        int i;
        int a2;
        x.q(fpEntity, "fpEntity");
        n1 = q.n1(0, Math.min(fpEntity.length() - 1, 62));
        S0 = q.S0(n1, 2);
        int g = S0.g(); // 需要看g的值
        int h = S0.h(); // 需要看h的值
        int i2 = S0.i(); //需要看i2的值
        if (i2 < 0 ? g >= h : g <= h) {
            i = 0;
            while (true) {
                String substring = fpEntity.substring(g, g + 2);
                x.h(substring, "(this as java.lang.Strin…ing(startIndex, endIndex)");
                a2 = b.a(16); // 返回 int 16
                i += Integer.parseInt(substring, a2);
                if (g == h) {
                    break;
                }
                g += i2;
            }
        } else {
            i = 0;
        }
        e0 e0Var = e0.a;
        String format = String.format("%02x", Arrays.copyOf(new Object[]{Integer.valueOf(i % 256)}, 1));
        x.h(format, "java.lang.String.format(format, *args)");
        return format;
    }    
}
```

```javascript
Java.perform(function(){
    var q = Java.use("kotlin.g0.q");
    q.s0.implementation = function(iVar, i) {
        var res = this.S0(iVar, i);
        console.log('g=', res.g());
        console.log('h=', res.h());
        console.log('i2=', res.i2());
    }
})
```





## 类型转换输出

### 输出 是什么类

`console.log("obj = ", JSON.stringify(arg));`

输出

```
obj = "<instance:com.xx.x$x, $className:xx.xx.x$x>"
```

重点看`$className`

### map转字符串输出

```javascript
Java.perform(function () {
    let RequestUtils = Java.use("com.shizhuang.duapp.common.utils.RequestUtils");
    RequestUtils["c"].implementation = function (map, j2) {
        // 输出参数类型
        console.log("[*] type of map ->", JSON.stringify(map));
        /*
        * "<instance: java.util.Map, $className: java.util.HashMap>"
        *            Map 是它的父类泛指子类类型  而真实类型是 HashMap
        * */
        let Map = Java.use('java.util.HashMap'); 
        let obj = Java.cast(map, Map); // 类型转换
        console.log('c is called' + ', ' + 'map: ' + obj + ', ' + 'j2: ' + j2); // obj.soString()
        let ret = this.c(map, j2);
        console.log('c ret value is ' + ret);
        return ret;
    };
})
```

### 字节数组转十六进制字符串

```javascript
Java.perform(function () {
    let d = Java.use("tv.danmaku.biliplayerimpl.report.heartbeat.d");
    var ByteString = Java.use('com.android.okhttp.okio.ByteString'); // 加上这个
    d["H7"].implementation = function (arg1, ...) {
        let ret = this.H7(arg1, ...);
        console.log('H7 ret value is ' + ret); // ret: [79,-90,...]
        console.log('H7 ret HEX value is ' + ByteString.of(ret).hex()); // 加上这个
        return ret;
    };
})
```

### 字节转成字符串输出(可以转的前提下)

```javascript
var ByteString = Java.use('com.android.okhttp.okio.ByteString'); // 加上这个
... var res = this.xxx(...)
	console.log(ByteString.of(res).utf8());
```

### 输出某东西(bVar)是哪个类 

`Json.stringfy(bVar)`输出如下  `console.log(bVar)`默认输出`[object Object]`

```
  	        bVar泛指这个类型                       要找这个
"<instance: com.bilibili.api.a$b,    $className: tv.danmaku.bili.utils.p$a>"
```

### 控制长度输出

```javascript
console.log('getMD5=key 返回值：', hexdump(retval, {length: 16}), "\n");
```



## 脱壳

can can need https://bbs.kanxue.com/homepage-905443.htm 总结的很齐

### 法一 frida

```javascript
function enumerateClassLoader() {
    Java.perform(function () {
        var dexclassLoader = Java.use("dalvik.system.DexClassLoader");
        dexclassLoader.$init.implementation = function (dexPath, optimizedDirectory, librarySearchPath, parent) {
            console.log("-----------------------------------------");
            console.log(JSON.stringify(this));
            console.log("dexPath=" + dexPath);
            console.log("optimizedDirectory=" + optimizedDirectory);
            console.log("librarySearchPath=" + librarySearchPath);
            console.log("parent=" + parent);
            this.$init(dexPath, optimizedDirectory, librarySearchPath, parent);
        };
    });

}

setImmediate(enumerateClassLoader);
// frida -U -f com.nb.loaderdemo -l loader.js
// frida -U -f com.nb.loaderdemo2 -l loader.js
```

```javascript
Java.perform(function () {
    var dexclassLoader = Java.use("dalvik.system.DexClassLoader");
    dexclassLoader.$init.implementation = function (dexPath, optimizedDirectory, librarySearchPath, parent) {
        this.$init(dexPath, optimizedDirectory, librarySearchPath, parent);
        console.log("\n");
        //1.当前自定义 DexClassLoader
        console.log("1.当前自定义DexClassLoader:", this);

        //2.读取pathList字段
        // 2.1 获取父类的父类BaseDexClassLoader
        var cls = this.getClass().getSuperclass().getSuperclass();

        // 2.2 反射字段
        var field1 = cls.getDeclaredField("pathList");
        field1.setAccessible(true);

        // 2.3 获取pathList（Object泛指）
        var pathList = field1.get(this);

        // 2.4 转换类型DexPathList
        var realPathList = Java.cast(pathList, Java.use("dalvik.system.DexPathList"));
        console.log("2.读取pathList字段:", realPathList, JSON.stringify(realPathList));

        // 2.5 调用DexPathList的getDexPaths方法获取dex文件（可选）
        /*
        console.log("3.所有dex文件");
        var dexPathArrayList = realPathList.getDexPaths();
        var realDexPathArrayList = Java.cast(dexPathArrayList, Java.use("java.util.ArrayList"));
        for (var i = 0; i < realDexPathArrayList.size(); i++) {
            var item = realDexPathArrayList.get(i);
            console.log("\t dex路径->", item);
        }
        */

        // 3.获取dexElements字段（Element数组）
        var clsDexPathList = Java.use("dalvik.system.DexPathList");
        var field2 = clsDexPathList.class.getDeclaredField("dexElements");
        field2.setAccessible(true);
        var dexElements = field2.get(realPathList);
        var elementArray = Java.cast(dexElements, Java.use("[Ldalvik.system.DexPathList$Element;"));
        console.log("3.dexElements数组：",elementArray);

        // 4.循环每个元素Element对象
        console.log("4.读取数组的每个元素");
        var ArrayClz = Java.use("java.lang.reflect.Array");
        var len = ArrayClz.getLength(elementArray);
        for (var i = 0; i < len; i++) {
            var elementObject = ArrayClz.get(elementArray, i);
            var element = Java.cast(elementObject, Java.use("dalvik.system.DexPathList$Element"));
            var dexFile = element.dexFile.value;
            var mFileName = dexFile.mFileName.value;
            console.log("\t", mFileName);

        }
    };
});

//frida -U  -f com.nb.loaderdemo2 -l  2.dex.js
```



### 法二 fart + frida

自定义的`MyClassLoader`加载的相关方法不会被导出

因为Fart默认是用当前ActivityThread中获取得PathClassLoader，而这个PathClassLoader只会加载APP本身自己的定义的相关类和方法，而自定义的MyClassLoader在Fart中时不会被监测到的。所以，也不会导出相应的方法了。

基于Frida的Hook脚本去获取到相应的自定义ClassLoader，然后主动去调用 android.app.ActivityThread.fartwithClassloader方法，并将当前自定义的Classloader对象传入到参数中，就搞定了。

```javascript
Java.perform(function () {
    // 也有的APP或壳会继承BaseDexClassLoader，所以也可以尝试去Hook BaseDexClassLoader得构造方法
    var dexclassLoader = Java.use("dalvik.system.DexClassLoader");
    dexclassLoader.$init.implementation = function (dexPath, optimizedDirectory, librarySearchPath, parent) {
        this.$init(dexPath, optimizedDirectory, librarySearchPath, parent);

        // console.log(this);
        // console.log(this.getParent());

        //主动执行 android.app.ActivityThread.fartwithClassloader
        var ActivityThread = Java.use("android.app.ActivityThread");
        ActivityThread.fartwithClassloader(this);
    };
});

//frida -U  -f com.nb.loaderdemo2 -l 5.call_classloader.js
```

对于某些壳，dumpArtMethod的上半部分已经能对dex进行整体dump,但是对于部分抽取壳，dex即使被dump下来，函数体还是以nop填充，即空函数体，FART还把函数的CodeItem给dump下来是让用户手动来修复这些dump下来的空函数。

https://github.com/dqzg12300/dexfixer/releases/tag/v1.0.0 

```
java -jar ./dexfixer.jar dexpath binpath outpath
```





### 法三 frida + MT/NP

Terminal 运行命令

```
frida-dexdump -U -f [packageName]
```

默认输出dex到以包名命名的文件夹下

把文件传到手机上

```
adb push 文件夹名 /storage... 手机上的路径
```

删掉太大/小的Dex文件。

全部选中Dex文件，Dex修复功能。可能会报错，不用管，把报错的dex删掉。

把dex文件拉取到本地

```
adb pull
```

或者复制到apk内，再把apk拉取到本地



### 法四 MT + GG

隐约记得是MT管理器+GG修改器，，，忘了

### :question: 法五 ida

ida动态分析，还没看懂，略



# 正向开发

## 动态注册

```java
package com.nb.s3jni;
class DynamicUtils {
    static {
        System.loadLibrary("dynamic");
    }
    public static native int add(int v1, int v2);
}

```

```c++
#include <jni.h>

jint plus(JNIEnv *env, jobject obj, jint v1, jint v2) {
	...
    return v1 + v2;
}

static JNINativeMethod gMethods[] = {
        {"add", "(II)I", (void *) plus},
};

JNIEXPORT jint JNICALL JNI_OnLoad(JavaVM *vm, void *reserved) {
    JNIEnv *env = NULL;
    // 在java虚拟机中获取env
    if ((*vm)->GetEnv(vm, (void **) &env, JNI_VERSION_1_6) != JNI_OK) {
        return JNI_ERR;
    }
    // 找到Java中的类
    jclass clazz = (*env)->FindClass(env, "com/nb/s3jni/DynamicUtils");
    // 将类中的方法注册到JNI中 (RegisterNatives)
    int res = (*env)->RegisterNatives(env, clazz, gMethods, 1);
    if (res < 0) {
        return JNI_ERR;
    }
    return JNI_VERSION_1_6;
}
```

## c构造类 

### 不需要实例化对象

```java
String n8 = EncryptUtils.v8();
```

```java
package com.nb.s4luffy;
class EncryptUtils {
    static {
        System.loadLibrary("enc");
    }
    public static native String v8();
}
```

```c++
#include <jni.h>
#include <string.h>
#include <syslog.h>
#include<stdlib.h>

JNIEXPORT jstring
JNICALL
Java_com_nb_s4luffy_EncryptUtils_v8(JNIEnv *env, jclass clazz) {
    // 找到类
    jclass cls = (*env)->FindClass(env, "com/nb/s4luffy/SignQuery");
    // 找到方法
    jmethodID method1 = (*env)->GetStaticMethodID(env, cls, "getPart1", "()Ljava/lang/String;");
    // 执行方法
    jstring res1 = (*env)->CallStaticObjectMethod(env, cls, method1);
    char *p1 = (*env)->GetStringUTFChars(env, res1, 0);
    char *result = malloc(50);
    strcat(result,p1);
    return (*env)->NewStringUTF(env, result);
}
```

```java
package com.nb.s4luffy;
public class SignQuery {
    public static String getPart1() {
        return "wupeiqi";
    }
}
```

### 需要实例化对象

只是比上面多了实例化对象 `new SignQuery2(...)`

```c++
#include <jni.h>
#include <string.h>
#include <syslog.h>
#include<stdlib.h>

JNIEXPORT jstring
JNICALL
Java_com_nb_s4luffy_EncryptUtils_v9(JNIEnv *env, jclass clazz) {
    // 找到类
    jclass cls = (*env)->FindClass(env, "com/nb/s4luffy/SignQuery2");
    // 找到构造方法
    jmethodID init = (*env)->GetMethodID(env, cls, "<init>", "(Ljava/lang/String;I)V");

    // 实例化对象 new SignQuery2(...)    (env   类   构造方法    参数1, 参数2, ...)
    jobject cls_obj = (*env)->NewObject(env, cls, init, (*env)->NewStringUTF(env, "hahahahh"), 22);

    // 找到方法
    jmethodID method1 = (*env)->GetMethodID(env, cls, "getPart1", "()Ljava/lang/String;");
    jmethodID method2 = (*env)->GetMethodID(env, cls, "getPart2", "(I)Ljava/lang/String;");
    jmethodID method3 = (*env)->GetMethodID(env, cls, "getPart3","(Ljava/lang/String;)Ljava/lang/String;");
    jmethodID method4 = (*env)->GetMethodID(env, cls, "getPart4", "(Ljava/lang/String;I)I");
    // 执行方法
    jstring res1 = (*env)->CallObjectMethod(env, cls_obj, method1);
    jstring res2 = (*env)->CallObjectMethod(env, cls_obj, method2, 100);
    jstring res3 = (*env)->CallObjectMethod(env,cls_obj,method3,(*env)->NewStringUTF(env, "hahahahh"));
    jint res4 = (*env)->CallIntMethod(env,cls_obj,method4,(*env)->NewStringUTF(env, "hahahahh"),18);

    char *p1 = (*env)->GetStringUTFChars(env, res1, 0);
    return (*env)->NewStringUTF(env, p1);
}
```

```java
package com.nb.s4luffy;
public class SignQuery {
    String name;
    String city;
    int count;
    public SignQuery(String city, int count) {
        this.name = "wupeiqi";
        this.city = city;
        this.count = count;
    }
    public String getPart1() {
        return this.name;
    }
    public String getPart2(int len) {
        return "root".substring(2);
    }
    public String getPart3(String prev) {
        return "xxx-";
    }
    public int getPart4(String prev, int v1) {
        return 100;
    }
}
```



# Xposed模块

### 初始化

> https://www.bilibili.com/video/BV1VT411C7Sr/

1.Android Studio创建新项目
2.将下载的xposedBridgeApi.jar包拖进libs文件夹
3.右击jar包，选择add as library
4.修改xml文件配置

```xml
<!-- 是否是xposed模块，xposed根据这个来判断是否是模块 -->
<meta-data
    android:name="xposedmodule"
    android:value="true" />
<!-- 模块描述，显示在xposed模块列表那里第二行 -->
<meta-data
    android:name="xposeddescription"
    android:value="这是一个Xposed模块" />
<!-- 最低xposed版本号(lib文件名可知) -->
<meta-data
    android:name="xposedminversion"
    android:value="89" />
```

5.修改build.gradle,将此处修改为compileOnly 默认的是implementation

```
implementation 使用该方式依赖的库将会参与编译和打包
compileOnly 只在编译时有效，不会参与打包
```

6.新建-->Folder-->Assets Folder，创建xposed_init(不要后缀名):只有一行代码，就是说明入口类

把包名复制到第一行就行

7.新建Hook类，实现IXposedHookLoadPackage接口，然后在handleLoadPackage函数内编写Hook逻辑

```java
import de.robv.android.xposed.IXposedHookLoadPackage; 
import de.robv.android.xposed.callbacks.XC_LoadPackage;

public class Hook implements IXposedHookLoadPackage {
    @Override
    public void handleLoadPackage(XC_LoadPackage.LoadPackageParam loadPackageParam) throws Throwable {
        
    }
}
```

## Hook

打印log

```java
// Log.e("zj2595", param.args[0].toString());
XposedBridge.log(param.args[0].toString());
```

### Hook普通方法

修改返回值

```java
XposedHelpers.findAndHookMethod("com.zj.wuaipojie.Demo", classLoader, "getPublicInt", new XC_MethodHook() {
    @Override
    protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
        super.beforeHookedMethod(param);
    }
    @Override
    protected void afterHookedMethod(MethodHookParam param) throws Throwable {
        super.afterHookedMethod(param);
        param.setResult(999);
    }
});

```

修改参数

```java
XposedHelpers.findAndHookMethod("com.zj.wuaipojie.Demo", classLoader, "setPublicInt", int.class, new XC_MethodHook() {
    @Override
    protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
        super.beforeHookedMethod(param);
        param.args[0] = 999；
    }
    @Override
    protected void afterHookedMethod(MethodHookParam param) throws Throwable {
        super.afterHookedMethod(param);
    }
});

```

### Hook复杂&自定义参数

```java
Class a = loadPackageParam.classLoader.loadClass("com.zj.wuaipojie.Demo类名");
XposedBridge.hookAllMethods(a, "Inner", new XC_MethodHook() {
    @Override
    protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
        super.beforeHookedMethod(param);
        
        }
});

```


### Hook替换函数

```java
Class a = classLoader.loadClass("类名")
XposedBridge.hookAllMethods(a,"getId",new XC_MethodReplacement() {  
    @Override  
    protected Object replaceHookedMethod(MethodHookParam methodHookParam) throws Throwable {  
        return "";  // 用于去掉弹窗
    }  
});

```


### Hook加固通杀

```java
XposedHelpers.findAndHookMethod(Application.class, "attach", Context.class, new XC_MethodHook() {  
    @Override  
    protected void afterHookedMethod(MethodHookParam param) throws Throwable {  
        Context context = (Context) param.args[0];  
        ClassLoader classLoader = context.getClassLoader();  
        // 拿到classloader后再hook
        
    }  
});
```

