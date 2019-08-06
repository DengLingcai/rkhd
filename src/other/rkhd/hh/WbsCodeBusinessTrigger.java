package other.rkhd.hh;

import com.rkhd.platform.sdk.exception.ScriptBusinessException;
import com.rkhd.platform.sdk.log.Logger;
import com.rkhd.platform.sdk.log.LoggerFactory;
import com.rkhd.platform.sdk.model.XObject;
import com.rkhd.platform.sdk.trigger.DataResult;
import com.rkhd.platform.sdk.trigger.Trigger;
import com.rkhd.platform.sdk.trigger.TriggerRequest;
import com.rkhd.platform.sdk.trigger.TriggerResponse;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.List;

/**
 *
 * @Author:邓令才
 * @Date:2019-07-30
 * @Description:   业务逻辑代码
 *
 **/
public class WbsCodeBusinessTrigger implements Trigger {

    private Logger logger = LoggerFactory.getLogger();

    @Override
    public TriggerResponse execute(TriggerRequest triggerRequest) throws ScriptBusinessException {
        List < DataResult > dataResults = new ArrayList< DataResult >();
        // 获取业务对象集合
        List<XObject> accountList = triggerRequest.getDataList();
        logger.info("----------------获取业务对象集合:"+accountList.size());
        if (accountList != null && accountList.size() > 0) {

            for (XObject xObject : accountList) {
                String phone = xObject.getAttribute("phone");
                if (phone != null && phone.length() == 11){
                    logger.info("-----------------phone 赋值标准");
                    dataResults.add(new DataResult(true, "", xObject));
                }else {
                    return new TriggerResponse(false, "手机号格式错误，应该是11位数字！",Collections.<DataResult>emptyList());
                }
            }

        }else {
            return new TriggerResponse(false, "Error: empty dataList", Collections.<DataResult>emptyList());
        }

        return  new TriggerResponse(true, "", dataResults);
    }


    public static void main(String[] args) {

        System.out.println(new BigDecimal("1.3").setScale(0, BigDecimal.ROUND_UP));

        Calendar calendar1 = Calendar.getInstance();
        calendar1.add(Calendar.DATE, 5);
        int year = calendar1.get(Calendar.YEAR);
        int month = calendar1.get(Calendar.MONTH) + 1;
        int day = calendar1.get(Calendar.DATE);

        System.out.println(year+"-"+month+"-"+day);

    }








}
