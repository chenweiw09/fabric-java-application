
# fabric
======= 本项目的web交互借鉴来自于https://gitee.com/aberic/fabric-net-server。
# springboot-fabric


# 本项目分为两大部分，fabric的web页面交互和对fabirc网络的操作；
## 1、页面交互后端采用了最新的springboot框架，用spring-jpa做orm，数据库采用轻量级的sqlite3;页面的渲染再用thymeleaf。具体的交互页面内容有：
### 1）管理区块链联盟
### 2）管理Orderer排序组织
### 3）管理peer节点
### 4）管理CA认证用户和证书
### 5）管理通道channel
### 6）管理chaincode，动态安装、实例化链码；页面查询、调用链码；动态获取区块信息等

## 2、操作Fabric网络
### 本项目的前提是fabric网络已经搭建完成，在此基础上，更集中在链码的开发和维护。 链码的维护包括：
### 1）动态管理链码，包括安装、实例化和升级；
### 2）查询链码信息，channel上的交易信息、区块内容
### 3）动态管理对peer的监听、对block的监听以及对chaincode的监听事件


# 后期工作内容
### 1）基于已经搭建好的底层网络基础设施，能够动态管理peer节点信息；
### 2）能够动态的增加通道管理
### 3）能够动态的管理CA，比如证书的添加、证书的召回以及证书的授权等

## 效果展示图：
![Image index](https://github.com/chenweiw09/fabric-java-application/raw/v3/pic/index.jpg)
![Image league](https://github.com/chenweiw09/fabric-java-application/raw/v3/pic/league.jpg)
![Image league](https://github.com/chenweiw09/fabric-java-application/raw/v3/pic/ca-add.jpg)
![Image league](https://github.com/chenweiw09/fabric-java-application/raw/v3/pic/chaincode-install.jpg)
![Image league](https://github.com/chenweiw09/fabric-java-application/raw/v3/pic/chaincode-verify.jpg)