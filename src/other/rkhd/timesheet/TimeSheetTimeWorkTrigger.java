package other.rkhd.timesheet;

import com.alibaba.fastjson.JSONObject;
import com.rkhd.platform.sdk.ScriptTrigger;
import com.rkhd.platform.sdk.exception.ScriptBusinessException;
import com.rkhd.platform.sdk.http.RkhdHttpClient;
import com.rkhd.platform.sdk.http.RkhdHttpData;
import com.rkhd.platform.sdk.log.Logger;
import com.rkhd.platform.sdk.log.LoggerFactory;
import com.rkhd.platform.sdk.model.DataModel;
import com.rkhd.platform.sdk.param.ScriptTriggerParam;
import com.rkhd.platform.sdk.param.ScriptTriggerResult;


import java.io.IOException;


/**
 * @Author: 邓令才
 * @Date:2019-08-01
 * @Description: timesheet子表保存时
 **/
public class TimeSheetTimeWorkTrigger implements ScriptTrigger {

    private Logger logger = LoggerFactory.getLogger();

    @Override
    public ScriptTriggerResult execute(ScriptTriggerParam scriptTriggerParam) throws ScriptBusinessException {
        DataModel extensionRequestList = scriptTriggerParam.getDataModelList().get(0);
        if (scriptTriggerParam.getDataModelList() != null && scriptTriggerParam.getDataModelList().size() > 0) {
            //
            Long wbsCode = Long.parseLong(extensionRequestList.getAttribute("customItem5__c").toString());
            logger.info("=======================================wbsCode：" + wbsCode);
            /**
             * 修改wbscode 中的  是否在 timesheet明细中 发生过insert update delete
             */
            JSONObject jsonObject = new JSONObject();
            JSONObject jsonObject1 = new JSONObject();
            jsonObject1.put("wbs_code_falg__c", 0L);
            jsonObject.put("data", jsonObject1);
            RkhdHttpClient rkhdHttpClient1 = null;
            try {
                rkhdHttpClient1 = new RkhdHttpClient();
                RkhdHttpData rkhdHttpData1 = new RkhdHttpData();
                rkhdHttpData1.setCall_type("PATCH");
                rkhdHttpData1.setCallString("/rest/data/v2/objects/customEntity7__c/" + wbsCode);
                rkhdHttpData1.setBody(jsonObject.toString());
                String pageResult1 = rkhdHttpClient1.performRequest(rkhdHttpData1);
                String httpCode = JSONObject.parseObject(pageResult1).get("code").toString();
                if ("200".equals(httpCode)) {
                    logger.info("=======================================update success：" + jsonObject1.toString());
                } else {
                    logger.info("=======================================update fail：" + JSONObject.parseObject(pageResult1).get("msg").toString());
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return new ScriptTriggerResult(scriptTriggerParam.getDataModelList());
    }


}
