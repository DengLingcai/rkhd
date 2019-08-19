package other.rkhd.timesheet;

import com.rkhd.platform.sdk.ScriptTrigger;
import com.rkhd.platform.sdk.exception.ScriptBusinessException;
import com.rkhd.platform.sdk.log.Logger;
import com.rkhd.platform.sdk.log.LoggerFactory;
import com.rkhd.platform.sdk.model.DataModel;
import com.rkhd.platform.sdk.param.ScriptTriggerParam;
import com.rkhd.platform.sdk.param.ScriptTriggerResult;

/**
 * @Author DengLingcai
 * @Date 2019/8/15
 * @Description  timesheet 主表 触发事件
 **/
public class TimeSheetMainTrigger implements ScriptTrigger {

    private Logger logger = LoggerFactory.getLogger();

    @Override
    public ScriptTriggerResult execute(ScriptTriggerParam scriptTriggerParam) throws ScriptBusinessException {
        DataModel extensionRequestList = scriptTriggerParam.getDataModelList().get(0);
        if (scriptTriggerParam.getDataModelList() != null && scriptTriggerParam.getDataModelList().size() > 0) {
            //
            Long wbsCode = Long.parseLong(extensionRequestList.getAttribute("customItem5__c").toString());
            logger.info("=======================================wbsCode：" + wbsCode);




        }










        return null;
    }














}
