package com.xxxx.yygh.config.service.impl;

import com.cloopen.rest.sdk.CCPRestSmsSDK;
import com.xxxx.common.result.ResultCodeEnum;
import com.xxxx.common.result.exception.YyghException;
import com.xxxx.yygh.config.service.MsmService;
import com.xxxx.yygh.vo.msm.MsmVo;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import javax.servlet.http.HttpSession;
import java.util.HashMap;
import java.util.Set;

@Service
public class MsmServiceImpl implements MsmService {


    @Override
    public boolean send(HttpSession session,String phone,int mobile_code) {
        //判断参数是否规范
        if(StringUtils.isEmpty(phone) || phone.length()!=11){
            throw new YyghException(ResultCodeEnum.PARAM_ERROR);
        }

        HashMap<String, Object> result = null;
        //初始化SDK
        CCPRestSmsSDK restAPI = new CCPRestSmsSDK();
        restAPI.init("app.cloopen.com", "8883");
        restAPI.setAccount("8a216da87e7baef8017eaba79a6606c4", "1a4a952da3f44df1bbbc6eff0d274432");	//参数顺序：第一个参数是ACOUNT SID，第二个参数是AUTH TOKEN。
        restAPI.setAppId("8a216da87e7baef8017eaba79b8106cb");		//应用ID的获取：登陆官网，在“应用-应用列表”，点击应用名称，看应用详情获取APP ID
        		//用随机数当验证码
        String yzm = String.valueOf(mobile_code);
        result = restAPI.sendTemplateSMS(phone,"1" ,new String[]{yzm,"2"});	//	第一个参数是手机号，第二个参数是你是用的第几个模板，第三个参数是你的验证码，第四个是在几分钟之内输入

        session.setAttribute("yzm", yzm);

        System.out.println("SDKTestGetSubAccounts result=" + result);
        if("000000".equals(result.get("statusCode"))){
            //正常返回输出data包体信息（map）
            HashMap<String,Object> data = (HashMap<String, Object>) result.get("data");
            Set<String> keySet = data.keySet();

            for(String key:keySet){
                Object object = data.get(key);
                System.out.println(key +" = "+object);
            }
        }else{
            //异常返回输出错误码和错误信息
            System.out.println("错误码=" + result.get("statusCode") +" 错误信息= "+result.get("statusMsg"));
            return false;
        }
        return true;
    }

    @Override
    public boolean send(HttpSession session,MsmVo msmVo) {
        if(!StringUtils.isEmpty(msmVo.getPhone())){
            Integer code = (Integer) msmVo.getParam().get("code");
            return this.send(session,msmVo.getPhone(),code);
        }
        return false;
    }
}
