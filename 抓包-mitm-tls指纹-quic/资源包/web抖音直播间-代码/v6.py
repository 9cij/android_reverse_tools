import gzip
from mitmproxy import http
from douyin_pb2 import PushFrame, Response, ChatMessage, MemberMessage


def websocket_start(flow: http.HTTPFlow):
    pass


def websocket_message(flow: http.HTTPFlow):
    message = flow.websocket.messages[-1]

    if message.from_client:
        # print(f"客户端发送: {message.content}")
        pass
    else:
        # print(f"服务端发送: {message.content}")
        frame = PushFrame()
        frame.ParseFromString(message.content)
        # 对PushFrame的 payload 内容进行gzip解压
        origin_bytes = gzip.decompress(frame.payload)

        # 根据Response+gzip解压数据，生成数据对象
        response = Response()
        response.ParseFromString(origin_bytes)

        # 获取数据内容（需根据不同method，使用不同的结构对象对 数据 进行解析）
        #   注意：此处只处理 WebcastChatMessage ，其他处理方式都是类似的。
        for item in response.messagesList:

            # if item.method == "WebcastMemberMessage":
            #     message = MemberMessage()
            #     message.ParseFromString(item.payload)
            #     print(message)

            if item.method == "WebcastChatMessage":
                message = ChatMessage()
                message.ParseFromString(item.payload)
                # print(message)

                info = f"【{message.user.id}】{message.user.shortId} {message.user.nickName}】{message.content} "
                print(info)
                print("-" * 50)


def websocket_end(flow: http.HTTPFlow):
    pass
