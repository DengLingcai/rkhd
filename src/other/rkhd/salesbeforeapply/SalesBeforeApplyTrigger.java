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
        String alltimeWork = "0";
        String allOverDays = "0";
        if (extensionRequestList != null && extensionRequestList.size() > 0) {
            for (XObject xObject : extensionRequestList) {
                //获取wbscode
                String wbscode = xObject.getAttribute("id");
                logger.info("---------------get 获取wbscode:"+wbscode);
                /**
                 * 根据wbscode获取到所有的超时工时 求和
                 */
                querySql = " select customItem3__c,customItem5__c  from customEntity15__c where customItem1__c =  '" + wbscode + "' order by id asc   ";
                try {
                    String pageResult = HttpUtil.sentRequestGetResult("POST","/data/v1/query",querySql + " limit 0,300 ");
                    if (StringUtils.isNotBlank(pageResult)){
                        JSONObject pageResultJson = JSONObject.parseObject(pageResult);
                        String totalSize = pageResultJson.get("totalSize").toString();
                        String count = pageResultJson.get("count").toString();
                        int pageSize = new BigDecimal("".equals(totalSize) ? "0" : totalSize).divide(new BigDecimal("".equals(count) ? "0" : count)).setScale(0, BigDecimal.ROUND_UP).intValue();
                        for (int i = 0; i < pageSize; i ++){
                            String pageResult1 = HttpUtil.sentRequestGetResult("POST","/data/v1/query",querySql + " limit 300*"+i+",300 ");
                            JSONObject pageResultJson1 = JSONObject.parseObject(pageResult1);
                            JSONArray jsonArray = JSONArray.parseArray(pageResultJson1.get("records").toString());
                            int size = jsonArray.size();
                            for (int j = 0; j < size; j++) {
                                JSONObject jsonObject = (JSONObject) jsonArray.get(i);
                                //超时工时
                                String overtimeWork  = jsonObject.get("customItem3__c").toString();
                                alltimeWork = new BigDecimal(alltimeWork).add(new BigDecimal(overtimeWork)).toPlainString();
                                //延期天数
                                String overDay  = jsonObject.get("customItem5__c").toString();
                                allOverDays = new BigDecimal(allOverDays).add(new BigDecimal(overDay)).toPlainString();
                            }
                        }
                    }
                    /**
                     * update wbscode 中的  已批准超支工时（小时） 已批准延期（天数） 当前结束日期
                     */
                    querySql = "select id,customItem17__c from  customEntity7__c where id = " + wbscode ;
                    String pageResult2 = HttpUtil.sentRequestGetResult("POST","/data/v1/query",querySql);
                    JSONObject pageResultJson1 = JSONObject.parseObject(pageResult2);
                    JSONArray jsonArray = JSONArray.parseArray(pageResultJson1.get("records").toString());
                    int size = jsonArray.size();
                    for (int j = 0; j < size; j++) {
                        JSONObject jsonObject = (JSONObject) jsonArray.get(j);
                        String id  = jsonObject.get("id").toString();
                        String nowEndDate  = jsonObject.get("customItem17__c").toString();
                    }

                    JSONObject jsonObject = new JSONObject();
                    JSONObject jsonObject1 = new JSONObject();
                    jsonObject1.put("customItem25__c",alltimeWork);
                    jsonObject1.put("customItem16__c",allOverDays);
                    jsonObject1.put("customItem17__c","");
                    jsonObject.put("data", jsonObject1);




                } catch (IOException e) {
                    e.printStackTrace();
                }


                dataResults.add(new DataResult(true, "", xObject));
            }
        }else {
            return new TriggerResponse(false, "数据为空！", Collections.<DataResult>emptyList());
        }






        return null;
    }






}
