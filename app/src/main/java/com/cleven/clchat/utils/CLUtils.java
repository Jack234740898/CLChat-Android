package com.cleven.clchat.utils;

import com.cleven.clchat.home.Bean.CLMessageBean;
import com.cleven.clchat.home.Bean.CLMessageBodyType;
import com.cleven.clchat.home.CLEmojiCommon.utils.CLEmojiFileUtils;
import com.cleven.clchat.manager.CLUserManager;

import java.util.Date;
import java.util.UUID;

import dev.utils.LogPrintUtils;
import dev.utils.common.DateUtils;
import dev.utils.common.FileUtils;

/**
 * Created by cleven on 2018/12/16.
 */

public class CLUtils {
    /// 时间戳,毫秒级
    public static long getTimeStamp(){
        return (long) (System.currentTimeMillis());
    };

    /// 获取UUID
    public static String getUUID() {
        UUID uuid = UUID.randomUUID();
        String uniqueId = uuid.toString();
        return uniqueId;
    }

    public static String formatTime(String time){
        long parseLong = Long.parseLong(time);
        if (parseLong == 0){
            return "";
        }
        parseLong = parseLong  / 1000;
        long currentTime = getTimeStamp() /1000;
        long marginTime = currentTime - parseLong;

        String formatTime = DateUtils.formatTime(Long.parseLong(time), "yyyy-MM-dd HH:mm:ss");
        Date dateTime = DateUtils.parseDate(formatTime);
        String format = "00:00";
        int month = DateUtils.getMonth(dateTime);
        int day1 = DateUtils.getDay(dateTime);
        int hour = DateUtils.get24Hour(dateTime);
        int minute = DateUtils.getMinute(dateTime);
        LogPrintUtils.eTag("时间戳",marginTime + "");
        LogPrintUtils.eTag("时间戳 parseLong",parseLong + "");
        LogPrintUtils.eTag("时间戳 currentTime",currentTime + "");
        if (marginTime > 3600 * 24 * 365) {
            format = formatTime;
        }else if (marginTime > 3600 * 24) {
            format = month + "-" + day1 + " " + hour + ":" + minute;
        }else {
            format = hour + ":" + minute;
        }
        return format;
    }

    /// 检查本地文件是否存在
    public static String checkLocalPath(CLMessageBean message){
        String currentUserId = CLUserManager.getInstence().getUserInfo().getUserId();
        if (currentUserId.equals(message.getUserInfo().getUserId())){//判断自己发送的
            if (CLMessageBodyType.fromTypeName(message.getMessageType())==CLMessageBodyType.MessageBodyType_Emoji){
                message.setLocalUrl(CLEmojiFileUtils.getFolderPath("assets") + message.getLocalUrl());
            }
            if (FileUtils.isFileExists(message.getLocalUrl())){
                return message.getLocalUrl();
            }else {
                CLMessageBodyType type = CLMessageBodyType.fromTypeName(message.getMessageType());
                if (type == CLMessageBodyType.MessageBodyType_Image){
                    return CLAPPConst.QINGCLOUD_IMAGE_URL + message.getMediaUrl();
                }else if (type == CLMessageBodyType.MessageBodyType_Video){
                    return CLAPPConst.QINGCLOUD_VIDEO_URL + message.getMediaUrl();
                }else if (type == CLMessageBodyType.MessageBodyType_Voice){
                    return CLAPPConst.QINGCLOUD_AUDIO_URL + message.getMediaUrl();
                }
                return message.getMediaUrl();
            }
        }else{
            CLMessageBodyType type = CLMessageBodyType.fromTypeName(message.getMessageType());
            if (type == CLMessageBodyType.MessageBodyType_Image){
                return CLAPPConst.QINGCLOUD_IMAGE_URL + message.getMediaUrl();
            }else if (type == CLMessageBodyType.MessageBodyType_Video){
                return CLAPPConst.QINGCLOUD_VIDEO_URL + message.getMediaUrl();
            }else if (type == CLMessageBodyType.MessageBodyType_Voice){
                return CLAPPConst.QINGCLOUD_AUDIO_URL + message.getMediaUrl();
            }
            return message.getMediaUrl();
        }

    }

    /// 格式化消息未读数
    public static String formatUnreadNumber(int num){
        if (num > 99){
            return "99+";
        }else {
            return "" + num;
        }
    }

}
