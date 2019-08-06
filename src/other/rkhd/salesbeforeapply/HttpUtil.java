package other.rkhd.salesbeforeapply;

import com.rkhd.platform.sdk.http.RkhdHttpClient;
import com.rkhd.platform.sdk.http.RkhdHttpData;

import java.io.IOException;

/**
 * @Author:dengl
 * @Date:2019-08-05
 * @Description:
 **/
public class HttpUtil {


    /**
     * 发送https请求到后台 获取数据
     * @param methodType  方法类型
     * @param url  请求路径
     * @param querySql  查询sql
     * @return 查询结果json字符串
     * @throws IOException 异常处理
     */
    public static String sentRequestGetResult(String methodType,String url,String querySql) throws IOException {
        RkhdHttpClient rkhdHttpClient = new RkhdHttpClient();
        RkhdHttpData rkhdHttpData = new RkhdHttpData();
        rkhdHttpData.setCall_type(methodType);
        rkhdHttpData.setCallString(url);
        rkhdHttpData.putFormData("q",querySql);
        return  rkhdHttpClient.performRequest(rkhdHttpData);
    }











}
