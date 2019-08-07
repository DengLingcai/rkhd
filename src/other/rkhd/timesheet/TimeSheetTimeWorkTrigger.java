package other.rkhd.timesheet;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.rkhd.platform.sdk.ScriptTrigger;
import com.rkhd.platform.sdk.exception.ScriptBusinessException;
import com.rkhd.platform.sdk.http.RkhdHttpClient;
import com.rkhd.platform.sdk.http.RkhdHttpData;
import com.rkhd.platform.sdk.log.Logger;
import com.rkhd.platform.sdk.log.LoggerFactory;
import com.rkhd.platform.sdk.model.DataModel;
import com.rkhd.platform.sdk.model.XObject;
import com.rkhd.platform.sdk.param.ScriptTriggerParam;
import com.rkhd.platform.sdk.param.ScriptTriggerResult;
import com.rkhd.platform.sdk.trigger.DataResult;
import com.rkhd.platform.sdk.trigger.TriggerRequest;
import com.rkhd.platform.sdk.trigger.TriggerResponse;
import org.apache.commons.lang.StringUtils;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

/**
 *
 * @Author: 邓令才
 * @Date:2019-08-01
 * @Description:  timesheet子表保存时
 *
 **/
public class TimeSheetTimeWorkTrigger implements ScriptTrigger {

    private Logger logger = LoggerFactory.getLogger();

    @Override
    public ScriptTriggerResult execute(ScriptTriggerParam scriptTriggerParam) throws ScriptBusinessException {
        DataModel dataModel = scriptTriggerParam.getDataModelList().get(0);
        //timesheet 子表的 wbscode
        String wbsCode = dataModel.getAttribute("customItem6__c").toString();
        int allTimeWork = 0;
        String querySql =" select customItem3__c from customEntity5__c where customItem6__c = " +wbsCode + " limit 0,300  ";
        RkhdHttpClient rkhdHttpClient = null;
        try {
            rkhdHttpClient = new RkhdHttpClient();
            RkhdHttpData rkhdHttpData = new RkhdHttpData();
            rkhdHttpData.setCall_type("POST");
            rkhdHttpData.setCallString("/data/v1/query");
            rkhdHttpData.putFormData("q",querySql);
            String pageResult =  rkhdHttpClient.performRequest(rkhdHttpData);
            if (StringUtils.isNotBlank(pageResult)){
                JSONObject pageResultJson = JSONObject.parseObject(pageResult);
                Object object =  pageResultJson.get("records") ;
                logger.info("------------seconed query object:"+object+"---");
                JSONArray jsonArray = JSON.parseArray(object== null ? "" : object.toString());
                logger.info("------------seconed query jsonArray:"+jsonArray+"---");
                int size = jsonArray.size();
                for (int j = 0; j < size; j++) {
                    JSONObject jsonObject = (JSONObject) jsonArray.get(j);
                    // 工时
                    String overtimeWork  = "".equals(jsonObject.get("customItem3__c").toString()) ? "0" : jsonObject.get("customItem3__c").toString();
                    allTimeWork = new BigDecimal(allTimeWork).add(new BigDecimal(overtimeWork)).intValue();
                }
                /**
                 *  修改wbscode 中的项目已使用工时字段
                 */
                JSONObject jsonObject = new JSONObject();
                JSONObject jsonObject1 = new JSONObject();
                jsonObject1.put("customItem26__c",allTimeWork);
                jsonObject.put("data",jsonObject1);
                RkhdHttpClient rkhdHttpClient1 = new RkhdHttpClient();
                RkhdHttpData rkhdHttpData1 = new RkhdHttpData();
                rkhdHttpData1.setCall_type("PATCH");
                rkhdHttpData1.setCallString("/rest/data/v2/objects/customEntity7__c/"+wbsCode);
                rkhdHttpData1.setBody(jsonObject.toString());
                String pageResult1 = rkhdHttpClient1.performRequest(rkhdHttpData1);
                String httpCode = JSONObject.parseObject(pageResult1).get("code").toString();
                if ("200".equals(httpCode)) {
                    logger.info("=======================================update success："+jsonObject1.toString());
                }else {
                    logger.info("=======================================update fail："+JSONObject.parseObject(pageResult1).get("msg").toString());
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return new ScriptTriggerResult(scriptTriggerParam.getDataModelList());
    }



}
