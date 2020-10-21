package com.github.liyue2008.rpc.client.stubs;

import com.github.liyue2008.rpc.client.RequestIdSupport;
import com.github.liyue2008.rpc.client.ServiceTypes;
import com.github.liyue2008.rpc.serialize.SerializeSupport;
import com.github.liyue2008.rpc.transport.Transport;
import com.github.liyue2008.rpc.transport.command.Code;
import com.github.liyue2008.rpc.transport.command.Command;
import com.github.liyue2008.rpc.transport.command.Header;
import com.github.liyue2008.rpc.transport.command.ResponseHeader;

import java.util.concurrent.ExecutionException;

/**
 * @PackageName:com.github.liyue2008.rpc.client.stubs
 * @ClassName: StubSupport
 * @Description:
 * @date: 2020/10/21 0021 17:35
 */
public class StubSupport {

    // 实现调用远程
    public static byte [] invokeRemote(RpcRequest request, Transport transport) {
        Header header = new Header(ServiceTypes.TYPE_RPC_REQUEST, 1, RequestIdSupport.next());
        byte [] payload = SerializeSupport.serialize(request);
        Command requestCommand = new Command(header, payload);
        try {
            Command responseCommand = transport.send(requestCommand).get();
            ResponseHeader responseHeader = (ResponseHeader) responseCommand.getHeader();
            if(responseHeader.getCode() == Code.SUCCESS.getCode()) {
                return responseCommand.getPayload();
            } else {
                throw new Exception(responseHeader.getError());
            }

        } catch (ExecutionException e) {
            throw new RuntimeException(e.getCause());
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
    }
}
