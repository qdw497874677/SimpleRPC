/**
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.github.liyue2008.rpc.client;

import com.github.liyue2008.rpc.NameService;
import com.github.liyue2008.rpc.RpcAccessPoint;
import com.github.liyue2008.rpc.hello.HelloService;
import com.github.liyue2008.rpc.spi.ServiceSupport;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author LiYue
 * Date: 2019/9/20
 */
public class Client {
    private static final Logger logger = LoggerFactory.getLogger(Client.class);
    public static void main(String [] args) throws IOException {
        String serviceName = HelloService.class.getCanonicalName();
        File tmpDirFile = new File(System.getProperty("java.io.tmpdir"));
        File file = new File(tmpDirFile, "simple_rpc_name_service.data");
//        String name = "Master MQ";
        String name = "qdw";

        try(RpcAccessPoint rpcAccessPoint = ServiceSupport.load(RpcAccessPoint.class)) {
            NameService nameService = rpcAccessPoint.getNameService(file.toURI());
            assert nameService != null;
            URI uri = nameService.lookupService(serviceName);
            assert uri != null;
            logger.info("找到服务{}，提供者: {}.", serviceName, uri);
            long start = System.currentTimeMillis();
            HelloService helloService = rpcAccessPoint.getRemoteService(uri, HelloService.class);
            long end = System.currentTimeMillis();
            logger.info("创建实例时间为：{}",(end-start));
            start = System.currentTimeMillis();
            logger.info("请求服务, name: {}...", name);

            String response;
            AtomicInteger count = new AtomicInteger();
            ExecutorService executorService = Executors.newFixedThreadPool(4);
            long left = System.currentTimeMillis();
//            do {
//                executorService.submit(()->{
//                    String hello = helloService.hello(name);
//                    System.out.println(hello);
//                    count.getAndIncrement();
//                });
////                response = helloService.hello(name);
//                count.getAndIncrement();
//            }while (System.currentTimeMillis()-left<5000);
//            }while (false);

            response = helloService.hello(name);
            System.out.println(response);
            System.out.println("QPS:"+count.get()/5);
//            executorService.shutdownNow();

            System.out.println("耗时："+(System.currentTimeMillis()-start));
//            logger.info("收到响应: {}.", response);
        }


    }
}
