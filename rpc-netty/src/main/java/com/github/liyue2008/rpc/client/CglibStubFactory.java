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

import com.github.liyue2008.rpc.client.stubs.RpcRequest;
import com.github.liyue2008.rpc.client.stubs.StubSupport;
import com.github.liyue2008.rpc.serialize.SerializeSupport;
import com.github.liyue2008.rpc.transport.Transport;
import com.github.liyue2008.rpc.transport.netty.NettyTransport;
import com.itranswarp.compiler.JavaStringCompiler;
import net.sf.cglib.proxy.*;

import java.lang.reflect.Method;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

/**
 * @author qdw
 * Date: 2019/9/27
 */
class A{
    public void show(){
        System.out.println("show");
    }

    public void set(){
        System.out.println("set");
    }
}

class MyInterceptor0 implements MethodInterceptor{

    private Transport transport;
    private Class aClass;
    private String test;

    @Override
    public Object intercept(Object o, Method method, Object[] objects, MethodProxy methodProxy) throws Throwable {
        System.out.println("增强0");
//        RpcRequest rpcRequest = new RpcRequest(methodProxy.getSuperName(), method.getName(), SerializeSupport.serialize("objects"));
        //System.out.println(aClass.getName()+" "+method.getName()+" "+(String)objects[0]+" "+SerializeSupport.serialize((String)objects[0]));
        RpcRequest rpcRequest = new RpcRequest(aClass.getName(), method.getName(), SerializeSupport.serialize((String)objects[0]));
        rpcRequest.toString();
        byte[] bytes = StubSupport.invokeRemote(rpcRequest, transport);
//        System.out.println(test);
        return SerializeSupport.parse(bytes);
    }

    public <T> T getProxyInstance(Transport transport, Class<T> serviceClass){
        this.transport = transport;
        this.aClass = serviceClass;
        Enhancer enhancer = new Enhancer();
        enhancer.setSuperclass(serviceClass);
        enhancer.setCallback(this);
        return (T)enhancer.create();
    }

    public <T> T getProxyInstance(String s, Class<T> serviceClass){
        this.test = s;
        Enhancer enhancer = new Enhancer();
        enhancer.setSuperclass(serviceClass);
        enhancer.setCallback(this);
        return (T)enhancer.create();
    }
}
class MyInterceptor1 implements MethodInterceptor{

    @Override
    public Object intercept(Object o, Method method, Object[] objects, MethodProxy methodProxy) throws Throwable {
        System.out.println("增强1");
        Object o1 = methodProxy.invokeSuper(o, objects);
        return o1;
    }
}
class MyCallackFilter implements CallbackFilter {

    // 返回值表示顺序
    @Override
    public int accept(Method method) {
        if ("show".equals(method.getName())){
            System.out.println("过滤到了"+method.getName());
            return 0;
        }
        return 1;
    }
}
class Test{
//    public Object getProxyInstance(){
//        Enhancer enhancer = new Enhancer();
//        enhancer.setSuperclass(A.class);
////        enhancer.setCallback(new MyInterceptor());
//        enhancer.setCallbacks(new Callback[]{new MyInterceptor0(),new MyInterceptor1()});
//        enhancer.setCallbackFilter(new MyCallackFilter());
//        return enhancer.create();
//    }

    public <T> T getProxyInstance(Transport transport, Class<T> serviceClass){

        Enhancer enhancer = new Enhancer();
        enhancer.setSuperclass(serviceClass);
//        enhancer.setCallback(new MyInterceptor());
        enhancer.setCallbacks(new Callback[]{new MyInterceptor0(),new MyInterceptor1()});
        enhancer.setCallbackFilter(new MyCallackFilter());
        return (T)enhancer.create();
    }

}
public class CglibStubFactory implements StubFactory{
    @Override
    @SuppressWarnings("unchecked")
    public <T> T createStub(Transport transport, Class<T> serviceClass) {

        try {
            MyInterceptor0 myInterceptor0 = new MyInterceptor0();
            T proxyInstance = myInterceptor0.getProxyInstance(transport, serviceClass);
            return proxyInstance;
        } catch (Throwable t) {
            throw new RuntimeException(t);
        }
    }
}
