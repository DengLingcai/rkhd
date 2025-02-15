package other.rkhd.salesbeforeapply;

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
import com.rkhd.platform.sdk.trigger.Trigger;
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
 * @Author: 邓令才
 * @Date:2019-08-01
 * @Description: 延期申请工时的汇总（可能会有多个延期） 延期申请审批通过
 **/
public class SalesBeforeApplyTrigger implements ScriptTrigger {

    private Logger logger = LoggerFactory.getLogger();

    @Override
    public ScriptTriggerResult execute(ScriptTriggerParam triggerRequest) throws ScriptBusinessException {
        DataModel extensionRequestList = triggerRequest.getDataModelList().get(0);
        String querySql = "";
        //超时工时
        int allTimeWork = 0;
        //延期天数
        int allOverDays = 0;
        if (triggerRequest.getDataModelList() != null && triggerRequest.getDataModelList().size() > 0) {
            //获取延期延时申请中的wbscode
            Long wbsCode = Long.parseLong(extensionRequestList.getAttribute("customItem1__c").toString());
            logger.info("---------------get wbscode:" + wbsCode);
            /**
             * 根据wbscode获取到所有的超时工时 求和
             */
            querySql = " select customItem3__c,customItem5__c  from customEntity15__c where customItem1__c =  '" + wbsCode + "'  and approvalStatus = 3  order by id asc   ";
            try {
                int pageSize = getPageSize(querySql);
                logger.info("----------------pageSize:" + pageSize);
                for (int i = 0; i < pageSize; i++) {
                    int pageNo = 300 * i;
                    String pageResult1 = HttpUtil.sentRequestGetResult("POST", "/data/v1/query", "q", querySql + " limit " + pageNo + ",300 ");
                    JSONObject pageResultJson1 = JSONObject.parseObject(pageResult1);
                    if (pageResultJson1 != null) {
                        Object object = pageResultJson1.get("records");
                        JSONArray jsonArray = JSON.parseArray(object == null ? "" : object.toString());
                        int size = jsonArray.size();
                        for (int j = 0; j < size; j++) {
                            JSONObject jsonObject = (JSONObject) jsonArray.get(j);
                            //超时工时
                            String overtimeWork = jsonObject.get("customItem3__c").toString();
                            allTimeWork = new BigDecimal(allTimeWork).add(new BigDecimal(overtimeWork)).intValue();
                            //延期天数
                            String overDay = jsonObject.get("customItem5__c").toString();
                            allOverDays = new BigDecimal(allOverDays).add(new BigDecimal(overDay)).intValue();
                        }
                    }
                }
                logger.info("------------超时工时:" + allTimeWork + "----延期天数:" + allOverDays);
                /**
                 * update wbscode 中的  已批准超支工时（小时） 已批准延期（天数） 当前结束日期
                 */
                querySql = "select customItem15__c from  customEntity7__c where id = " + wbsCode;
                String pageResult2 = HttpUtil.sentRequestGetResult("POST", "/data/v1/query", "q", querySql);
                JSONObject pageResultJson1 = JSONObject.parseObject(pageResult2);
                JSONArray jsonArray = JSONArray.parseArray(pageResultJson1.get("records").toString());
                int size = jsonArray.size();
                Long nowEndDate = System.currentTimeMillis();
                for (int j = 0; j < size; j++) {
                    JSONObject jsonObject = (JSONObject) jsonArray.get(j);
                    //wbscode的原始结束日期
                    nowEndDate = DateUtil.strDateToLong(jsonObject.get("customItem15__c").toString());
                }
                logger.info("------------//nowEndDate:" + nowEndDate);
                nowEndDate = nowEndDate == null ? DateUtil.getAfterDay(DateUtil.getSysDate(), allOverDays) : DateUtil.getAfterDay(nowEndDate, allOverDays);
                logger.info("------------//nowEndDate22:" + nowEndDate);
                JSONObject jsonObject = new JSONObject();
                JSONObject jsonObject1 = new JSONObject();
                jsonObject1.put("customItem25__c", allTimeWork);
                jsonObject1.put("customItem16__c", allOverDays);
                jsonObject1.put("customItem17__c", nowEndDate);
                jsonObject.put("data", jsonObject1);
                RkhdHttpClient rkhdHttpClient = new RkhdHttpClient();
                RkhdHttpData rkhdHttpData = new RkhdHttpData();
                rkhdHttpData.setCall_type("PATCH");
                rkhdHttpData.setCallString("/rest/data/v2/objects/customEntity7__c/" + wbsCode);
                rkhdHttpData.setBody(jsonObject.toString());
                String pageResult1 = rkhdHttpClient.performRequest(rkhdHttpData);
                String httpCode = JSONObject.parseObject(pageResult1).get("code").toString();
                if ("200".equals(httpCode)) {
                    logger.info("=======================================update success：" + jsonObject1.toString());
                } else {
                    logger.info("=======================================update fail：" + JSONObject.parseObject(pageResult1).get("msg").toString());
                }
            } catch (IOException e) {
                e.printStackTrace();
                logger.error("--------------------IOexception:" + e.getMessage());
            }
        } else {
            return new ScriptTriggerResult(triggerRequest.getDataModelList());
        }
        return new ScriptTriggerResult(triggerRequest.getDataModelList());
    }


    /**
     * 根据wbscode 获取 该wbscode
     *
     * @param wbsCode
     * @return
     */
    public int getAllTimeWorkByWbsCode(Long wbsCode) {


        return 0;
    }

    /**
     * 获取页数
     * @param querySql
     * @return
     */
    public int getPageSize(String querySql) {
        int pageSize = 0;
        String pageResult = null;
        try {
            pageResult = HttpUtil.sentRequestGetResult("POST", "/data/v1/query", "q", querySql + " limit 0,300 ");
        } catch (IOException e) {
            e.printStackTrace();
        }
        logger.info("------------first query pageResult:" + pageResult);
        if (StringUtils.isNotBlank(pageResult)) {
            JSONObject pageResultJson = JSONObject.parseObject(pageResult);
            String totalSize = pageResultJson.get("totalSize") == null ? "0" : pageResultJson.get("totalSize").toString();
            String count = pageResultJson.get("count") == null ? "1" : pageResultJson.get("count").toString();
            logger.info("----------------totalSize:" + totalSize + ":--------count:" + count);
            pageSize = new BigDecimal("".equals(totalSize) ? "0" : totalSize).divide(new BigDecimal("".equals(count) ? "1" : count)).setScale(0, BigDecimal.ROUND_UP).intValue();
            logger.info("----------------pageSize:" + pageSize);
        }
        return pageSize;
    }


}
