## 1 AES加解密 java代码实现


### 1.1 AES加密

```java
//bytesContent 要加密的数据
//key 密钥
public static byte[] aes_enc(byte[] bytesContent, String key) throws Exception
{
    //key相关
    byte[] raw = key.getBytes("utf-8");
    SecretKeySpec skeySpec = new SecretKeySpec(raw, "AES");

    //"算法/模式/补码方式" 初始化cipher
    Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
    cipher.init(Cipher.ENCRYPT_MODE, skeySpec);

    //执行加密
    byte[] enc = cipher.doFinal(bytesContent);
    return enc;
}
```

### 1.2 AES解密

```java
//bytesContent 要解密的数据
//key 密钥
public byte[] aes_dec(byte[] bytesContent, String key) throws Exception
{
    //key相关
    byte[] raw = key.getBytes("utf-8");
    SecretKeySpec skeySpec = new SecretKeySpec(raw, "AES");

    //"算法/模式/补码方式" 初始化cipher
    Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
    cipher.init(Cipher.DECRYPT_MODE, skeySpec);

    //执行解密
    byte[] dec = cipher.doFinal(bytesContent);
    return dec;
}
```

这里AES加解密的区别只有一点

```java
//Cipher.DECRYPT_MODE为解密  
//Cipher.ENCRYPT_MODE 加密
cipher.init(Cipher.DECRYPT_MODE, skeySpec)
```

## 2. DES加解密 java实现代码

### 2.1 DES加密

```java
private static byte[] des_enc(byte[] data, byte[] key) throws Exception {
    // 生成一个可信任的随机数源
    SecureRandom sr = new SecureRandom();

    // 从原始密钥数据创建DESKeySpec对象
    DESKeySpec dks = new DESKeySpec(key);

    // 创建一个密钥工厂，然后用它把DESKeySpec转换成SecretKey对象
    SecretKeyFactory keyFactory = SecretKeyFactory.getInstance("DES");
    SecretKey securekey = keyFactory.generateSecret(dks);

    // Cipher对象实际完成加密操作
    Cipher cipher = Cipher.getInstance("DES");

    // 用密钥初始化Cipher对象
    cipher.init(Cipher.ENCRYPT_MODE, securekey, sr);

    return cipher.doFinal(data);
}
```

### 2.2 DES解密

```java
private static byte[] des_dec(byte[] data, byte[] key) throws Exception {
    // 生成一个可信任的随机数源
    SecureRandom sr = new SecureRandom();

    // 从原始密钥数据创建DESKeySpec对象
    DESKeySpec dks = new DESKeySpec(key);

    // 创建一个密钥工厂，然后用它把DESKeySpec转换成SecretKey对象
    SecretKeyFactory keyFactory = SecretKeyFactory.getInstance("DES");
    SecretKey securekey = keyFactory.generateSecret(dks);

    // Cipher对象实际完成解密操作
    Cipher cipher = Cipher.getInstance("DES");

    // 用密钥初始化Cipher对象
    cipher.init(Cipher.DECRYPT_MODE, securekey, sr);

    return cipher.doFinal(data);
}
```

这里DES加解密的区别只有一点

```java
//Cipher.DECRYPT_MODE为解密  
//Cipher.ENCRYPT_MODE 加密
cipher.init(Cipher.DECRYPT_MODE, securekey, sr);
```

## 3. RSA加解密 java代码实现

### RSA加解密代码实现

```java
public static void RSA(byte[] bytesData) throws Exception
{
    //秘钥长度为1024 生成秘钥对
    KeyPairGenerator keyPairGenerator=KeyPairGenerator.getInstance("RSA");
    keyPairGenerator.initialize(1024);
    KeyPair keyPair= keyPairGenerator.generateKeyPair();

    //获取公钥 私钥
    PublicKey publicKey=keyPair.getPublic();
    PrivateKey privateKey=keyPair.getPrivate();

    //公钥加密 java默认"RSA"="RSA/ECB/PKCS1Padding"
    Cipher cipher=Cipher.getInstance("RSA");
    cipher.init(Cipher.ENCRYPT_MODE, publicKey);
    byte[] encBytes = cipher.doFinal(bytesData);

    //私钥解密 java默认"RSA"="RSA/ECB/PKCS1Padding"
    Cipher cipher1=Cipher.getInstance("RSA");
    cipher1.init(Cipher.DECRYPT_MODE, privateKey);
    byte[] decBytes = cipher1.doFinal(encBytes);
    Log.d("xxx",new String(decBytes));
}
```

这里忽略前面RSA加解密都需要的生成公钥私钥的部分
核心功能代码如下

### 3.1 RSA加密代码

```java
//公钥加密 java默认"RSA"="RSA/ECB/PKCS1Padding"
Cipher cipher=Cipher.getInstance("RSA");
cipher.init(Cipher.ENCRYPT_MODE, publicKey);
byte[] encBytes = cipher.doFinal(bytesData);
```

### 3.2 RSA解密代码

```java
//私钥解密 java默认"RSA"="RSA/ECB/PKCS1Padding"
Cipher cipher1=Cipher.getInstance("RSA");
cipher1.init(Cipher.DECRYPT_MODE, privateKey);
byte[] decBytes = cipher1.doFinal(encBytes);
```

这里RSA加解密的区别也只有一点

```java
//Cipher.DECRYPT_MODE为解密   publicKey  公钥加密
//Cipher.ENCRYPT_MODE为加密   privateKey 私钥解密
cipher.init(Cipher.ENCRYPT_MODE, publicKey);
```

看了上面的一些代码 这里可以找到一些共性
虽然实现处有些区别 但大体架构和使用的java接口是可以找到一些规律的

这里出镜率比较高的有

1. secretKeySpec
2. Cipher.getInstance
3. cipher.init
4. cipher.doFinal
5. DESKeySpec
   ...(后续还有 这里不一一列举)

查阅java帮助文档可以发现, 这些API都是一些加密算法常用的接口, 那么实现自吐 就是hook加密算法常用的API,打印相关参数,以便于快速的定位算法和相关参数 加密模式等

在网上查找 相关资料 我找到了一份frida自吐算法的源码 链接如下
[https://blog.csdn.net/weixin_34365417/article/details/93088342]

看了上面的源码 作者写的还是不错的 而且不仅hook了 我上面提到的加密算法 还hook了一些消息摘要算法 MAC家族和md家族等 也就是 md5 sha 等通信协议中常用的hash算法 另外也有对 IV这种加密中用到的向量成员的hook

这里 我修改了下源码
修改的部分主要分为下面几点

1. 针对上面的打印堆栈的代码做了修改 修复在高版本 打印堆栈不换行的问题
2. 增加了一些hook api
3. 把原来的 python脚本换成了js
4. 修复一个bug
5. ui调整 增加显示 加密模式 解密模式 把原脚本的dec结果 改成str结果 增加dofinal str显示

## 4 修改后的源码

```javascript
var N_ENCRYPT_MODE = 1
var N_DECRYPT_MODE = 2

function showStacks() {
    var Exception = Java.use("java.lang.Exception");
    var ins = Exception.$new("Exception");
    var straces = ins.getStackTrace();

    if (undefined == straces || null == straces) {
        return;
    }

    console.log("============================= Stack strat=======================");
    console.log("");

    for (var i = 0; i < straces.length; i++) {
        var str = "   " + straces[i].toString();
        console.log(str);
    }

    console.log("");
    console.log("============================= Stack end=======================\r\n");
    Exception.$dispose();
}

//工具相关函数
var base64EncodeChars = 'ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789+/',
    base64DecodeChars = new Array((-1), (-1), (-1), (-1), (-1), (-1), (-1), (-1), (-1), (-1), (-1), (-1), (-1), (-1), (-1), (-1), (-1), (-1), (-1), (-1), (-1), (-1), (-1), (-1), (-1), (-1), (-1), (-1), (-1), (-1), (-1), (-1), (-1), (-1), (-1), (-1), (-1), (-1), (-1), (-1), (-1), (-1), (-1), 62, (-1), (-1), (-1), 63, 52, 53, 54, 55, 56, 57, 58, 59, 60, 61, (-1), (-1), (-1), (-1), (-1), (-1), (-1), 0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20, 21, 22, 23, 24, 25, (-1), (-1), (-1), (-1), (-1), (-1), 26, 27, 28, 29, 30, 31, 32, 33, 34, 35, 36, 37, 38, 39, 40, 41, 42, 43, 44, 45, 46, 47, 48, 49, 50, 51, (-1), (-1), (-1), (-1), (-1));

function stringToBase64(e) {
    var r, a, c, h, o, t;
    for (c = e.length, a = 0, r = ''; a < c;) {
        if (h = 255 & e.charCodeAt(a++), a == c) {
            r += base64EncodeChars.charAt(h >> 2),
                r += base64EncodeChars.charAt((3 & h) << 4),
                r += '==';
            break
        }
        if (o = e.charCodeAt(a++), a == c) {
            r += base64EncodeChars.charAt(h >> 2),
                r += base64EncodeChars.charAt((3 & h) << 4 | (240 & o) >> 4),
                r += base64EncodeChars.charAt((15 & o) << 2),
                r += '=';
            break
        }
        t = e.charCodeAt(a++),
            r += base64EncodeChars.charAt(h >> 2),
            r += base64EncodeChars.charAt((3 & h) << 4 | (240 & o) >> 4),
            r += base64EncodeChars.charAt((15 & o) << 2 | (192 & t) >> 6),
            r += base64EncodeChars.charAt(63 & t)
    }
    return r
}
function base64ToString(e) {
    var r, a, c, h, o, t, d;
    for (t = e.length, o = 0, d = ''; o < t;) {
        do
            r = base64DecodeChars[255 & e.charCodeAt(o++)];
        while (o < t && r == -1);
        if (r == -1)
            break;
        do
            a = base64DecodeChars[255 & e.charCodeAt(o++)];
        while (o < t && a == -1);
        if (a == -1)
            break;
        d += String.fromCharCode(r << 2 | (48 & a) >> 4);
        do {
            if (c = 255 & e.charCodeAt(o++), 61 == c)
                return d;
            c = base64DecodeChars[c]
        } while (o < t && c == -1);
        if (c == -1)
            break;
        d += String.fromCharCode((15 & a) << 4 | (60 & c) >> 2);
        do {
            if (h = 255 & e.charCodeAt(o++), 61 == h)
                return d;
            h = base64DecodeChars[h]
        } while (o < t && h == -1);
        if (h == -1)
            break;
        d += String.fromCharCode((3 & c) << 6 | h)
    }
    return d
}
function hexToBase64(str) {
    return base64Encode(String.fromCharCode.apply(null, str.replace(/\r|\n/g, "").replace(/([\da-fA-F]{2}) ?/g, "0x$1 ").replace(/ +$/, "").split(" ")));
}
function base64ToHex(str) {
    for (var i = 0, bin = base64Decode(str.replace(/[ \r\n]+$/, "")), hex = []; i < bin.length; ++i) {
        var tmp = bin.charCodeAt(i).toString(16);
        if (tmp.length === 1)
            tmp = "0" + tmp;
        hex[hex.length] = tmp;
    }
    return hex.join("");
}
function hexToBytes(str) {
    var pos = 0;
    var len = str.length;
    if (len % 2 != 0) {
        return null;
    }
    len /= 2;
    var hexA = new Array();
    for (var i = 0; i < len; i++) {
        var s = str.substr(pos, 2);
        var v = parseInt(s, 16);
        hexA.push(v);
        pos += 2;
    }
    return hexA;
}
function bytesToHex(arr) {
    var str = '';
    var k, j;
    for (var i = 0; i < arr.length; i++) {
        k = arr[i];
        j = k;
        if (k < 0) {
            j = k + 256;
        }
        if (j < 16) {
            str += "0";
        }
        str += j.toString(16);
    }
    return str;
}
function stringToHex(str) {
    var val = "";
    for (var i = 0; i < str.length; i++) {
        if (val == "")
            val = str.charCodeAt(i).toString(16);
        else
            val += str.charCodeAt(i).toString(16);
    }
    return val
}
function stringToBytes(str) {
    var ch, st, re = [];
    for (var i = 0; i < str.length; i++) {
        ch = str.charCodeAt(i);
        st = [];
        do {
            st.push(ch & 0xFF);
            ch = ch >> 8;
        }
        while (ch);
        re = re.concat(st.reverse());
    }
    return re;
}
//将byte[]转成String的方法
function bytesToString(arr) {
    var str = '';
    arr = new Uint8Array(arr);
    for (var i in arr) {
        str += String.fromCharCode(arr[i]);
    }
    return str;
}
function bytesToBase64(e) {
    var r, a, c, h, o, t;
    for (c = e.length, a = 0, r = ''; a < c;) {
        if (h = 255 & e[a++], a == c) {
            r += base64EncodeChars.charAt(h >> 2),
                r += base64EncodeChars.charAt((3 & h) << 4),
                r += '==';
            break
        }
        if (o = e[a++], a == c) {
            r += base64EncodeChars.charAt(h >> 2),
                r += base64EncodeChars.charAt((3 & h) << 4 | (240 & o) >> 4),
                r += base64EncodeChars.charAt((15 & o) << 2),
                r += '=';
            break
        }
        t = e[a++],
            r += base64EncodeChars.charAt(h >> 2),
            r += base64EncodeChars.charAt((3 & h) << 4 | (240 & o) >> 4),
            r += base64EncodeChars.charAt((15 & o) << 2 | (192 & t) >> 6),
            r += base64EncodeChars.charAt(63 & t)
    }
    return r
}
function base64ToBytes(e) {
    var r, a, c, h, o, t, d;
    for (t = e.length, o = 0, d = []; o < t;) {
        do
            r = base64DecodeChars[255 & e.charCodeAt(o++)];
        while (o < t && r == -1);
        if (r == -1)
            break;
        do
            a = base64DecodeChars[255 & e.charCodeAt(o++)];
        while (o < t && a == -1);
        if (a == -1)
            break;
        d.push(r << 2 | (48 & a) >> 4);
        do {
            if (c = 255 & e.charCodeAt(o++), 61 == c)
                return d;
            c = base64DecodeChars[c]
        } while (o < t && c == -1);
        if (c == -1)
            break;
        d.push((15 & a) << 4 | (60 & c) >> 2);
        do {
            if (h = 255 & e.charCodeAt(o++), 61 == h)
                return d;
            h = base64DecodeChars[h]
        } while (o < t && h == -1);
        if (h == -1)
            break;
        d.push((3 & c) << 6 | h)
    }
    return d
}
//stringToBase64 stringToHex stringToBytes
//base64ToString base64ToHex base64ToBytes
//               hexToBase64  hexToBytes    
// bytesToBase64 bytesToHex bytesToString

Java.perform(function () {
    var secretKeySpec = Java.use('javax.crypto.spec.SecretKeySpec');
    secretKeySpec.$init.overload('[B', 'java.lang.String').implementation = function (a, b) {
        showStacks();
        var result = this.$init(a, b);
        console.log("======================================");
        console.log("算法名：" + b + "|str密钥:" + bytesToString(a));
        console.log("算法名：" + b + "|Hex密钥:" + bytesToHex(a));
        return result;
    }

    var DESKeySpec = Java.use('javax.crypto.spec.DESKeySpec');
    DESKeySpec.$init.overload('[B').implementation = function (a) {
        showStacks();
        var result = this.$init(a);
        console.log("======================================");
        var bytes_key_des = this.getKey();
        console.log("des密钥  |str " + bytesToString(bytes_key_des));
        console.log("des密钥  |hex " + bytesToHex(bytes_key_des));
        return result;
    }

    DESKeySpec.$init.overload('[B', 'int').implementation = function (a, b) {
        showStacks();
        var result = this.$init(a, b);
        console.log("======================================");
        var bytes_key_des = this.getKey();
        console.log("des密钥  |str " + bytesToString(bytes_key_des));
        console.log("des密钥  |hex " + bytesToHex(bytes_key_des));
        return result;
    }

    var mac = Java.use('javax.crypto.Mac');
    mac.getInstance.overload('java.lang.String').implementation = function (a) {
        showStacks();
        var result = this.getInstance(a);
        console.log("======================================");
        console.log("算法名：" + a);
        return result;
    }
    mac.update.overload('[B').implementation = function (a) {
        //showStacks();
        this.update(a);
        console.log("======================================");
        console.log("update:" + bytesToString(a))
    }
    mac.update.overload('[B', 'int', 'int').implementation = function (a, b, c) {
        //showStacks();
        this.update(a, b, c)
        console.log("======================================");
        console.log("update:" + bytesToString(a) + "|" + b + "|" + c);
    }
    mac.doFinal.overload().implementation = function () {
        //showStacks();
        var result = this.doFinal();
        console.log("======================================");
        console.log("doFinal结果: |str  :"     + bytesToString(result));
        console.log("doFinal结果: |hex  :"     + bytesToHex(result));
        console.log("doFinal结果: |base64  :"  + bytesToBase64(result));
        return result;
    }
    mac.doFinal.overload('[B').implementation = function (a) {
        //showStacks();
        var result = this.doFinal(a);
        console.log("======================================");
        console.log("doFinal参数: |str  :"     + bytesToString(a));
        console.log("doFinal结果: |str  :"     + bytesToString(result));
        console.log("doFinal结果: |hex  :"     + bytesToHex(result));
        console.log("doFinal结果: |base64  :"  + bytesToBase64(result));
        return result;
    }

    var md = Java.use('java.security.MessageDigest');
    md.getInstance.overload('java.lang.String', 'java.lang.String').implementation = function (a, b) {
        //showStacks();
        console.log("======================================");
        console.log("算法名：" + a);
        return this.getInstance(a, b);
    }
    md.getInstance.overload('java.lang.String').implementation = function (a) {
        //showStacks();
        console.log("======================================");
        console.log("算法名：" + a);
        return this.getInstance(a);
    }
    md.update.overload('[B').implementation = function (a) {
        //showStacks();
        console.log("======================================");
        console.log("update:" + bytesToString(a))
        return this.update(a);
    }
    md.update.overload('[B', 'int', 'int').implementation = function (a, b, c) {
        //showStacks();
        console.log("======================================");
        console.log("update:" + bytesToString(a) + "|" + b + "|" + c);
        return this.update(a, b, c);
    }
    md.digest.overload().implementation = function () {
        //showStacks();
        console.log("======================================");
        var result = this.digest();
        console.log("digest结果:" + bytesToHex(result));
        console.log("digest结果:" + bytesToBase64(result));
        return result;
    }
    md.digest.overload('[B').implementation = function (a) {
        //showStacks();
        console.log("======================================");
        console.log("digest参数:" + bytesToString(a));
        var result = this.digest(a);
        console.log("digest结果:" + bytesToHex(result));
        console.log("digest结果:" + bytesToBase64(result));
        return result;
    }

    var ivParameterSpec = Java.use('javax.crypto.spec.IvParameterSpec');
    ivParameterSpec.$init.overload('[B').implementation = function (a) {
        //showStacks();
        var result = this.$init(a);
        console.log("======================================");
        console.log("iv向量: |str:" + bytesToString(a));
        console.log("iv向量: |hex:" + bytesToHex(a));
        return result;
    }

    var cipher = Java.use('javax.crypto.Cipher');
    cipher.getInstance.overload('java.lang.String').implementation = function (a) {
        //showStacks();
        var result = this.getInstance(a);
        console.log("======================================");
        console.log("模式填充:" + a);
        return result;
    }
    cipher.init.overload('int', 'java.security.Key').implementation = function (a, b) {
        //showStacks();
        var result = this.init(a, b);
        console.log("======================================");
        if (N_ENCRYPT_MODE == a)
        {
            console.log("init  | 加密模式");    
        }
        else if(N_DECRYPT_MODE == a)
        {
            console.log("init  | 解密模式");    
        }

        var bytes_key = b.getEncoded();
        console.log("init key:" + "|str密钥:" + bytesToString(bytes_key));
        console.log("init key:" + "|Hex密钥:" + bytesToHex(bytes_key));
        return result;
    }
    cipher.init.overload('int', 'java.security.cert.Certificate').implementation = function (a, b) {
        //showStacks();
        var result = this.init(a, b);
        console.log("======================================");

        if (N_ENCRYPT_MODE == a)
        {
            console.log("init  | 加密模式");    
        }
        else if(N_DECRYPT_MODE == a)
        {
            console.log("init  | 解密模式");    
        }

        return result;
    }
    cipher.init.overload('int', 'java.security.Key', 'java.security.spec.AlgorithmParameterSpec').implementation = function (a, b, c) {
        //showStacks();
        var result = this.init(a, b, c);
        console.log("======================================");

        if (N_ENCRYPT_MODE == a)
        {
            console.log("init  | 加密模式");    
        }
        else if(N_DECRYPT_MODE == a)
        {
            console.log("init  | 解密模式");    
        }

        var bytes_key = b.getEncoded();
        console.log("init key:" + "|str密钥:" + bytesToString(bytes_key));
        console.log("init key:" + "|Hex密钥:" + bytesToHex(bytes_key));

        return result;
    }
    cipher.init.overload('int', 'java.security.cert.Certificate', 'java.security.SecureRandom').implementation = function (a, b, c) {
        //showStacks();
        var result = this.init(a, b, c);
        if (N_ENCRYPT_MODE == a)
        {
            console.log("init  | 加密模式");    
        }
        else if(N_DECRYPT_MODE == a)
        {
            console.log("init  | 解密模式");    
        }
        return result;
    }
    cipher.init.overload('int', 'java.security.Key', 'java.security.SecureRandom').implementation = function (a, b, c) {
        //showStacks();
        var result = this.init(a, b, c);
        if (N_ENCRYPT_MODE == a)
        {
            console.log("init  | 加密模式");    
        }
        else if(N_DECRYPT_MODE == a)
        {
            console.log("init  | 解密模式");    
        }

         var bytes_key = b.getEncoded();
        console.log("init key:" + "|str密钥:" + bytesToString(bytes_key));
        console.log("init key:" + "|Hex密钥:" + bytesToHex(bytes_key));
        return result;
    }
    cipher.init.overload('int', 'java.security.Key', 'java.security.AlgorithmParameters').implementation = function (a, b, c) {
        //showStacks();
        var result = this.init(a, b, c);
        if (N_ENCRYPT_MODE == a)
        {
            console.log("init  | 加密模式");    
        }
        else if(N_DECRYPT_MODE == a)
        {
            console.log("init  | 解密模式");    
        }

        var bytes_key = b.getEncoded();
        console.log("init key:" + "|str密钥:" + bytesToString(bytes_key));
        console.log("init key:" + "|Hex密钥:" + bytesToHex(bytes_key));
        return result;
    }
    cipher.init.overload('int', 'java.security.Key', 'java.security.AlgorithmParameters', 'java.security.SecureRandom').implementation = function (a, b, c, d) {
        //showStacks();
        var result = this.init(a, b, c, d);
        if (N_ENCRYPT_MODE == a)
        {
            console.log("init  | 加密模式");    
        }
        else if(N_DECRYPT_MODE == a)
        {
            console.log("init  | 解密模式");    
        }

        var bytes_key = b.getEncoded();
        console.log("init key:" + "|str密钥:" + bytesToString(bytes_key));
        console.log("init key:" + "|Hex密钥:" + bytesToHex(bytes_key));
        return result;
    }
    cipher.init.overload('int', 'java.security.Key', 'java.security.spec.AlgorithmParameterSpec', 'java.security.SecureRandom').implementation = function (a, b, c, d) {
        //showStacks();
        var result = this.update(a, b, c, d);
        if (N_ENCRYPT_MODE == a)
        {
            console.log("init  | 加密模式");    
        }
        else if(N_DECRYPT_MODE == a)
        {
            console.log("init  | 解密模式");    
        }

         var bytes_key = b.getEncoded();
        console.log("init key:" + "|str密钥:" + bytesToString(bytes_key));
        console.log("init key:" + "|Hex密钥:" + bytesToHex(bytes_key));
        return result;
    }

    cipher.update.overload('[B').implementation = function (a) {
        //showStacks();
        var result = this.update(a);
        console.log("======================================");
        console.log("update:" + bytesToString(a));
        return result;
    }
    cipher.update.overload('[B', 'int', 'int').implementation = function (a, b, c) {
        //showStacks();
        var result = this.update(a, b, c);
        console.log("======================================");
        console.log("update:" + bytesToString(a) + "|" + b + "|" + c);
        return result;
    }
    cipher.doFinal.overload().implementation = function () {
        //showStacks();
        var result = this.doFinal();
        console.log("======================================");
        console.log("doFinal结果: |str  :"     + bytesToString(result));
        console.log("doFinal结果: |hex  :"     + bytesToHex(result));
        console.log("doFinal结果: |base64  :"  + bytesToBase64(result));
        return result;
    }
    cipher.doFinal.overload('[B').implementation = function (a) {
        //showStacks();
        var result = this.doFinal(a);
        console.log("======================================");
        console.log("doFinal参数: |str  :"     + bytesToString(a));
        console.log("doFinal结果: |str  :"     + bytesToString(result));
        console.log("doFinal结果: |hex  :"     + bytesToHex(result));
        console.log("doFinal结果: |base64  :"  + bytesToBase64(result));
        return result;
    }

    var x509EncodedKeySpec = Java.use('java.security.spec.X509EncodedKeySpec');
    x509EncodedKeySpec.$init.overload('[B').implementation = function (a) {
        //showStacks();
        var result = this.$init(a);
        console.log("======================================");
        console.log("RSA密钥:" + bytesToBase64(a));
        return result;
    }

    var rSAPublicKeySpec = Java.use('java.security.spec.RSAPublicKeySpec');
    rSAPublicKeySpec.$init.overload('java.math.BigInteger', 'java.math.BigInteger').implementation = function (a, b) {
        //showStacks();
        var result = this.$init(a, b);
        console.log("======================================");
        //console.log("RSA密钥:" + bytesToBase64(a));
        console.log("RSA密钥N:" + a.toString(16));
        console.log("RSA密钥E:" + b.toString(16));
        return result;
    }

    var KeyPairGenerator = Java.use('java.security.KeyPairGenerator');
    KeyPairGenerator.generateKeyPair.implementation = function ()
    {
        //showStacks();
        var result = this.generateKeyPair();
        console.log("======================================");

        var str_private = result.getPrivate().getEncoded();
        var str_public = result.getPublic().getEncoded();
        console.log("公钥  |hex" + bytesToHex(str_public));
        console.log("私钥  |hex" + bytesToHex(str_private));

        return result;
    }

    KeyPairGenerator.genKeyPair.implementation = function ()
    {
        //showStacks();
        var result = this.genKeyPair();
        console.log("======================================");

        var str_private = result.getPrivate().getEncoded();
        var str_public = result.getPublic().getEncoded();
        console.log("公钥  |hex" + bytesToHex(str_public));
        console.log("私钥  |hex" + bytesToHex(str_private));

        return result;
    }
});
```



## 5 另一个版本 base64/MD/MAC/DES/AES/PBE/RSA

```javascript
Java.perform(function() {
    //Base64
    var base64=Java.use('android.util.Base64');
    var string=Java.use('java.lang.String');
    /*base64.encode.overload('[B', 'int', 'int', 'int').implementation = function(){
        send("=================base64 encode====================");
        send(Java.use("android.util.Log").getStackTraceString(Java.use("java.lang.Throwable").$new()));
        send(arguments[0]);
        send(arguments[1]);
        send(arguments[2]);
        send(arguments[3]);
        var data=this.encode(arguments[0],arguments[1],arguments[2],arguments[3])
        send("base64:"+string.$new(data));
        return data;
    }*/
    
    /*base64.decode.overload('[B', 'int', 'int', 'int').implementation = function(){
        send("=================base64 decode====================");
        send(Java.use("android.util.Log").getStackTraceString(Java.use("java.lang.Throwable").$new()));
        send(arguments[0]);
        send(arguments[1]);
        send(arguments[2]);
        send(arguments[3]);
        var data=this.decode(arguments[0],arguments[1],arguments[2],arguments[3])
        send("base64:"+string.$new(data));
        return data;
    }*/
   

    // MD SHA 
    var messageDigest=Java.use('java.security.MessageDigest');
    // update
    for(var i = 0; i < messageDigest.update.overloads.length; i++){
        messageDigest.update.overloads[i].implementation = function(){
            var name=this.getAlgorithm()
            send("================="+name+"====================");
            send(Java.use("android.util.Log").getStackTraceString(Java.use("java.lang.Throwable").$new()));
            if(arguments.length == 1){
                send(arguments[0]);
                this.update(arguments[0]);
            }else if(arguments.length == 3){
                send(arguments[0]);
                send(arguments[1]);
                send(arguments[2]);
                this.update(arguments[0],arguments[1],arguments[2]);
            }
        }
    }
    // digest
    for(var i = 0; i < messageDigest.digest.overloads.length; i++){
        messageDigest.digest.overloads[i].implementation = function(){
            var name=this.getAlgorithm()
            send("================="+name+"====================");
            send(Java.use("android.util.Log").getStackTraceString(Java.use("java.lang.Throwable").$new()));
            if(arguments.length == 0){
                var data=this.digest();
                send(data);
                return data;
            }else if(arguments.length == 1){
                send(arguments[0]);
                var data=this.digest(arguments[0]);
                send(data);
                return data;
            }else if(arguments.length == 3){
                send(arguments[0]);
                send(arguments[1]);
                send(arguments[2]);
                var data=this.digest(arguments[0],arguments[1],arguments[2]);
                send(data);
                return data;
            }
        }
    }

    //MAC
    var mac=Java.use('javax.crypto.Mac');
    for(var i = 0; i < mac.doFinal.overloads.length; i++){
        mac.doFinal.overloads[i].implementation = function(){
            var name=this.getAlgorithm()
            send("================="+name+"====================");
            send(Java.use("android.util.Log").getStackTraceString(Java.use("java.lang.Throwable").$new()));
            if(arguments.length == 0){
                var data=this.doFinal();
                send(data);
                return data;
            }else if(arguments.length == 1){
                send(arguments[0]);
                var data=this.doFinal(arguments[0]);
                send(data);
                return data;
            }else if(arguments.length == 2){
                send(arguments[0]);
                send(arguments[1]);
                var data=this.doFinal(arguments[0],arguments[1]);
                send(data);
                return data;
            }
        }
    }
    
    // DES DESede AES PBE RSA
    var cipher=Java.use('javax.crypto.Cipher');
     for(var i = 0; i < cipher.doFinal.overloads.length; i++){
        cipher.doFinal.overloads[i].implementation = function(){
            var name=this.getAlgorithm()
            send("================="+name+"====================");
            send(Java.use("android.util.Log").getStackTraceString(Java.use("java.lang.Throwable").$new()));
            if(arguments.length == 0){
                var data=this.doFinal();
                send(data);
                return data;
            }else if(arguments.length == 1){
                send(arguments[0]);
                var data=this.doFinal(arguments[0]);
                send(data);
                return data;
            }else if(arguments.length == 2){
                send(arguments[0]);
                send(arguments[1]);
                var data=this.doFinal(arguments[0],arguments[1]);
                send(data);
                return data;
            }else if(arguments.length == 3){
                send(arguments[0]);
                send(arguments[1]);
                send(arguments[2]);
                var data=this.doFinal(arguments[0],arguments[1],arguments[2]);
                send(data);
                return data;
            }else if(arguments.length == 5){
                send(arguments[0]);
                send(arguments[1]);
                send(arguments[2]);
                send(arguments[3]);
                send(arguments[4]);
                var data=this.doFinal(arguments[0],arguments[1],arguments[2],arguments[3],arguments[4]);
                send(data);
                return data;
            }else{
                send(arguments[0]);
                send(arguments[1]);
                send(arguments[2]);
                send(arguments[3]);
                var data=this.doFinal(arguments[0],arguments[1],arguments[2],arguments[3]);
                send(data);
                return data;
            }
        }
    }
    
    //KEY
    var secretKey=Java.use('javax.crypto.spec.SecretKeySpec');
    for(var i = 0; i < secretKey.$init.overloads.length; i++){
        secretKey.$init.overloads[i].implementation = function(){
            var name=this.getAlgorithm()
            send("=================KEY====================");
            //send(Java.use("android.util.Log").getStackTraceString(Java.use("java.lang.Throwable").$new()));
            if(arguments.length == 2){
                send(arguments[0]);
                send(arguments[1]);
                this.$init(arguments[0],arguments[1]);
            }else if(arguments.length == 4){
                send(arguments[0]);
                send(arguments[1]);
                send(arguments[2]);
                send(arguments[3]);
                this.$init(arguments[0],arguments[1],arguments[2],arguments[3]);
            }
        }
    }
    //IV
    //DES KEY  
    //DESede KEY
    //PBE KEY salt
});
```

