package other.rkhd.salesbeforeapply;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.rkhd.platform.sdk.exception.ScriptBusinessException;
import com.rkhd.platform.sdk.http.RkhdHttpClient;
import com.rkhd.platform.sdk.http.RkhdHttpData;
import com.rkhd.platform.sdk.log.Logger;
import com.rkhd.platform.sdk.log.LoggerFactory;
import com.rkhd.platform.sdk.model.XObject;
import com.rkhd.platform.sdk.trigger.DataResult;
import com.rkhd.platform.sdk.trigger.Trigger;
import com.rkhd.platform.sdk.trigger.TriggerRequest;
import com.rkhd.platform.sdk.trigger.TriggerResponse;
import org.apache.commons.lang.StringUtils;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 *
 * @Author: 邓令才
 * @Date:2019-08-01
 * @Description:  延期申请工时的汇总（可能会有多个延期） 延期申请审批通过
 *
 **/
public class SalesBeforeApplyTrigger implements Trigger {


    private Logger logger = LoggerFactory.getLogger();


    @Override
    public TriggerResponse execute(TriggerRequest triggerRequest) throws ScriptBusinessException {
        List<DataResult> dataResults = new ArrayList< DataResult >();
        List<XObject> extensionRequestList = triggerRequest.getDataList();
        String querySql = "";
        int allTimeWork = 0;
        int allOverDays = 0;
        if (extensionRequestList != null && extensionRequestList.size() > 0) {
            for (XObject xObject : extensionRequestList) {
                //获取延期延时申请中的wbscode
                String wbsCode = xObject.getAttribute("customItem1__c");
                logger.info("---------------get 获取wbscode:"+wbsCode);
                /**
                 * 根据wbscode获取到所有的超时x工时 求和
                 */
                querySql = " select customItem3__c,customItem5__c  from customEntity15__c where customItem1__c =  '" + wbsCode + "' order by id asc   ";
                try {
                    String pageResult = HttpUtil.sentRequestGetResult("POST","/data/v1/query","q",querySql + " limit 0,300 ");
                    if (StringUtils.isNotBlank(pageResult)){
                        JSONObject pageResultJson = JSONObject.parseObject(pageResult);
                        String totalSize = pageResultJson.get("totalSize").toString();
                        String count = pageResultJson.get("count").toString();
                        int pageSize = new BigDecimal("".equals(totalSize) ? "0" : totalSize).divide(new BigDecimal("".equals(count) ? "0" : count)).setScale(0, BigDecimal.ROUND_UP).intValue();
                        for (int i = 0; i < pageSize; i ++){
                            String pageResult1 = HttpUtil.sentRequestGetResult("POST","/data/v1/query","q",querySql + " limit 300*"+i+",300 ");
                            JSONObject pageResultJson1 = JSONObject.parseObject(pageResult1);
                            JSONArray jsonArray = JSONArray.parseArray(pageResultJson1.get("records").toString());
                            int size = jsonArray.size();
                            for (int j = 0; j < size; j++) {
                                JSONObject jsonObject = (JSONObject) jsonArray.get(i);
                                //超时工时
                                String overtimeWork  = jsonObject.get("customItem3__c").toString();
                                allTimeWork = new BigDecimal(allTimeWork).add(new BigDecimal(overtimeWork)).intValue();
                                //延期天数
                                String overDay  = jsonObject.get("customItem5__c").toString();
                                allOverDays = new BigDecimal(allOverDays).add(new BigDecimal(overDay)).intValue();
                            }
                        }
                    }
                    /**
                     * update wbscode 中的  已批准超支工时（小时） 已批准延期（天数） 当前结束日期
                     */
                    querySql = "select customItem17__c from  customEntity7__c where id = " + wbsCode ;
                    String pageResult2 = HttpUtil.sentRequestGetResult("POST","q","/data/v1/query",querySql);
                    JSONObject pageResultJson1 = JSONObject.parseObject(pageResult2);
                    JSONArray jsonArray = JSONArray.parseArray(pageResultJson1.get("records").toString());
                    int size = jsonArray.size();
                    String nowEndDate = "";
                    for (int j = 0; j < size; j++) {
                        JSONObject jsonObject = (JSONObject) jsonArray.get(j);
                         //wbscode的当前结束日期
                         nowEndDate  = jsonObject.get("customItem17__c").toString();
                    }
                    nowEndDate = "".equals(nowEndDate) || nowEndDate == null ? DateUtil.getAfterDay(DateUtil.getSysDate(),allOverDays) : DateUtil.getAfterDay(nowEndDate,allOverDays) ;
                    JSONObject jsonObject1 = new JSONObject();
                    jsonObject1.put("customItem25__c",allTimeWork);
                    jsonObject1.put("customItem16__c",allOverDays);
                    jsonObject1.put("customItem17__c",nowEndDate);
                    String pageResult1 = HttpUtil.sentRequestGetResult("PATCH","/rest/data/v2/objects/customEntity7__c/"+wbsCode,"data",jsonObject1.toString());
                    String httpCode = JSONObject.parseObject(pageResult1).get("code").toString();
                    if ("200".equals(httpCode)) {
                        logger.info("=======================================update success："+jsonObject1.toString());
                    }else {
                        logger.info("=======================================update fail："+jsonObject1.toString());
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                    logger.error("--------------------IOexception:"+e.getMessage());
                }
                dataResults.add(new DataResult(true, "", xObject));
            }
        }else {
            return new TriggerResponse(false, "数据为空！", Collections.<DataResult>emptyList());
        }
        return new TriggerResponse(false, "", dataResults);
    }



}
