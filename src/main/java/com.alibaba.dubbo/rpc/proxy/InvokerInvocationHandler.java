/*
 * Copyright 1999-2011 Alibaba Group.
 *  
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *  
 *      http://www.apache.org/licenses/LICENSE-2.0
 *  
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.alibaba.dubbo.rpc.proxy;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;

import com.alibaba.dubbo.remoting.exchange.ExchangeClient;
import com.alibaba.dubbo.remoting.exchange.support.header.HeaderExchangeClient;
import com.alibaba.dubbo.remoting.exchange.support.header.HeaderExchangeHandler;
import com.alibaba.dubbo.remoting.transport.DecodeHandler;
import com.alibaba.dubbo.rpc.Invoker;
import com.alibaba.dubbo.rpc.Result;
import com.alibaba.dubbo.rpc.RpcInvocation;
import com.alibaba.dubbo.rpc.RpcResult;
import com.alibaba.dubbo.rpc.protocol.AbstractInvoker;
import com.alibaba.dubbo.rpc.protocol.dubbo.DubboCodec;
import com.alibaba.dubbo.rpc.protocol.dubbo.DubboInvoker;
import com.alibaba.dubbo.rpc.protocol.dubbo.telnet.InvokeTelnetHandler;
import com.alibaba.fastjson.JSONObject;

/**
 * InvokerHandler
 * 
 * @author william.liangf
 */
public class InvokerInvocationHandler implements InvocationHandler {

    private final Invoker<?> invoker;
    
    public InvokerInvocationHandler(Invoker<?> handler){
        this.invoker = handler;
    }

    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        String methodName = method.getName();
        Class<?>[] parameterTypes = method.getParameterTypes();
        if (method.getDeclaringClass() == Object.class) {
            return method.invoke(invoker, args);
        }
        if ("toString".equals(methodName) && parameterTypes.length == 0) {
            return invoker.toString();
        }
        if ("hashCode".equals(methodName) && parameterTypes.length == 0) {
            return invoker.hashCode();
        }
        if ("equals".equals(methodName) && parameterTypes.length == 1) {
            return invoker.equals(args[0]);
        }
        RpcInvocation invocation=new RpcInvocation(method, args);
        //consumer method proxy 拿到producter返回参数 2016-11-22 张科伟
        InvokerExchangeFilter.beforeInvoke(invoker, invocation);
        Result result =null;
        try {
            result = invoker.invoke(invocation);
            return result.recreate();
        }finally {
            InvokerExchangeFilter.afterInvoke(invoker, result,invocation);
        }
    }
}