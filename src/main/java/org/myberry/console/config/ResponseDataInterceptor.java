/*
* MIT License
*
* Copyright (c) 2020 gaoyang
*
* Permission is hereby granted, free of charge, to any person obtaining a copy
* of this software and associated documentation files (the "Software"), to deal
* in the Software without restriction, including without limitation the rights
* to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
* copies of the Software, and to permit persons to whom the Software is
* furnished to do so, subject to the following conditions:

* The above copyright notice and this permission notice shall be included in all
* copies or substantial portions of the Software.

* THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
* IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
* FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
* AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
* LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
* OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
* SOFTWARE.
*/
package org.myberry.console.config;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.myberry.console.exception.CalloutException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Aspect
@Component
public class ResponseDataInterceptor {

  private static final Logger log = LoggerFactory.getLogger(ResponseDataInterceptor.class);

  @Pointcut("@annotation(org.myberry.console.annotation.ResponseData)")
  public void annotationPoinCut() {}

  @Around("annotationPoinCut()")
  public Object around(ProceedingJoinPoint joinPoint) {
    Object[] args = joinPoint.getArgs();
    ResultData resultData = null;
    try {
      Object result = joinPoint.proceed(args);
      resultData = new ResultData(ResultData.SUCCESS, result);
    } catch (CalloutException e) {
      log.warn(e.getMsg());
      resultData = new ResultData(ResultData.FAIL, e.getMsg());
    } catch (Throwable e) {
      log.error(e.getMessage());
      resultData = new ResultData(ResultData.FAIL, "Error!");
    }

    return resultData;
  }

  private static class ResultData {
    public static final int FAIL = 0;
    public static final int SUCCESS = 1;
    private int code;
    private Object body;
    private String callOut;

    public ResultData(int code, Object body) {
      this.code = code;
      this.body = body;
    }

    public ResultData(int code, String callOut) {
      this.code = code;
      this.callOut = callOut;
    }

    public int getCode() {
      return code;
    }

    public void setCode(int code) {
      this.code = code;
    }

    public Object getBody() {
      return body;
    }

    public void setBody(Object body) {
      this.body = body;
    }

    public String getCallOut() {
      return callOut;
    }

    public void setCallOut(String callOut) {
      this.callOut = callOut;
    }
  }
}
