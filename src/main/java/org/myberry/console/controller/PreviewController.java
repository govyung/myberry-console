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
package org.myberry.console.controller;

import javax.annotation.Resource;
import org.myberry.client.admin.DefaultAdminClient;
import org.myberry.client.admin.SendResult;
import org.myberry.client.admin.SendStatus;
import org.myberry.common.ProduceMode;
import org.myberry.common.protocol.body.admin.CRComponentData;
import org.myberry.common.protocol.body.admin.NSComponentData;
import org.myberry.console.annotation.ResponseData;
import org.myberry.console.config.MyBerryProperties;
import org.myberry.console.exception.CalloutException;
import org.myberry.console.vo.Component;
import org.myberry.console.vo.ComponentSize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.ModelAndView;

@RestController
public class PreviewController {

  @Resource private DefaultAdminClient defaultAdminClient;
  @Resource private MyBerryProperties myBerryProperties;

  @GetMapping("/")
  public ModelAndView console(ModelAndView mv) throws Exception {
    SendResult sendResult = defaultAdminClient.queryClusterList();
    mv.addObject("clusterList", sendResult.getClusterList().getClusters());
    mv.setViewName("console");
    return mv;
  }

  @GetMapping("/component")
  public ModelAndView component(ModelAndView mv) throws Exception {
    SendResult sendResult = defaultAdminClient.queryComponentSize();
    mv.addObject("componentSize", sendResult.getSize());
    mv.addObject("produceMode", myBerryProperties.getProduceMode());
    mv.setViewName("component");
    return mv;
  }

  @PostMapping("/component/search")
  @ResponseBody
  @ResponseData
  public Object componentSearch(@RequestBody Component component) throws Exception {
    SendResult sendResult = defaultAdminClient.queryComponentByKey(component.getKey());
    if (SendStatus.SEND_OK == sendResult.getSendStatus()) {
      return sendResult.getComponent();
    } else if (SendStatus.KEY_NOT_EXISTED == sendResult.getSendStatus()) {
      throw new CalloutException(String.format("Key: [%s] not existed", component.getKey()));
    } else {
      throw new CalloutException(String.format("Unknown status [%s]", sendResult.getSendStatus()));
    }
  }

  @PostMapping("/component/create")
  @ResponseBody
  @ResponseData
  public Object componentCreate(@RequestBody Component component) throws Exception {
    SendResult sendResult = null;
    if (ProduceMode.CR.getProduceName().equals(myBerryProperties.getProduceMode())) {
      CRComponentData crcd = new CRComponentData();
      crcd.setKey(component.getKey());
      crcd.setExpression(component.getExpression());
      sendResult = defaultAdminClient.createComponent(crcd);
    } else if (ProduceMode.NS.getProduceName().equals(myBerryProperties.getProduceMode())) {
      NSComponentData nscd = new NSComponentData();
      nscd.setKey(component.getKey());
      nscd.setValue(component.getValue());
      nscd.setStepSize(component.getStepSize());
      nscd.setResetType(component.getResetType());
      sendResult = defaultAdminClient.createComponent(nscd);
    }

    if (SendStatus.SEND_OK == sendResult.getSendStatus()) {
      return new ComponentSize(defaultAdminClient.queryComponentSize().getSize());
    } else if (SendStatus.KEY_EXISTED == sendResult.getSendStatus()) {
      throw new CalloutException(String.format("Key: [%s] existed", component.getKey()));
    } else {
      throw new CalloutException(String.format("Unknown status [%s]", sendResult.getSendStatus()));
    }
  }
}
