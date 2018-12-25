package com.cleven.clchat.manager;

import android.util.Log;

import com.alibaba.fastjson.JSON;
import com.cleven.clchat.home.Bean.CLMessageBean;
import com.cleven.clchat.home.Bean.CLMessageBodyType;
import com.cleven.clchat.home.CLEmojiCommon.utils.CLEmojiFileUtils;
import com.cleven.clchat.utils.CLUtils;

import dev.utils.LogPrintUtils;
import dev.utils.app.image.BitmapUtils;

import static com.cleven.clchat.home.Bean.CLSendStatus.SendStatus_FAILED;
import static com.cleven.clchat.home.Bean.CLSendStatus.SendStatus_SEND;
import static com.cleven.clchat.home.Bean.CLSendStatus.SendStatus_SENDING;

/**
 * Created by cleven on 2018/12/16.
 */

public class CLMessageManager {

    private static CLMessageManager instance = new CLMessageManager();
    private CLMessageManager(){}
    public static CLMessageManager getInstance(){
        return instance;
    }

    /**
     * 定义发送消息回调接口
     */
    public interface CLSendMessageStatusOnListener {
        void onSuccess(CLMessageBean message);
        void onFailure(CLMessageBean message);
    }
    private CLSendMessageStatusOnListener messageStatusOnListener;
    public void setMessageStatusOnListener(CLSendMessageStatusOnListener messageStatusOnListener) {
        this.messageStatusOnListener = messageStatusOnListener;
    }

    /**
     * 定义上传回调接口
     */
    public interface CLUploadStatusOnListener {
        void onSuccess(String fileName);
        void onFailure(String fileName);
    }
    private CLUploadStatusOnListener uploadStatusOnListener;
    public void setUploadStatusOnListener(CLUploadStatusOnListener uploadStatusOnListener) {
        this.uploadStatusOnListener = uploadStatusOnListener;
    }

    /**
     * 定义收到消息的接口
     */
    public interface CLReceiveMessageOnListener{
        void onMessage(CLMessageBean message);
    }
    private CLReceiveMessageOnListener receiveMessageOnListener;
    public void setReceiveMessageOnListener(CLReceiveMessageOnListener receiveMessageOnListener) {
        this.receiveMessageOnListener = receiveMessageOnListener;
    }


    /**
     * 发送emoji
     * @param filePath
     */
    public CLMessageBean sendEmojiMessage(String filePath,String userId,boolean isGroup){
        CLMessageBean message = new CLMessageBean();
        /// 是否是群聊会话
        message.setGroupSession(isGroup);
        /// 消息内容
        message.setContent("");
        /// 消息类型
        message.setMessageType(CLMessageBodyType.MessageBodyType_Emoji.getTypeName());
        message.setLocalUrl(filePath);
        int[] size = BitmapUtils.getImageWidthHeight(CLEmojiFileUtils.getFolderPath("/") + filePath);
        /// 设置图片的size
        if (size != null && size.length > 0){
            message.setWitdh(size[0]);
            message.setHeight(size[1]);
        }

        String imageName = CLUtils.timeStamp + ".png";
        message.setMediaUrl(imageName);
        /// 发送者信息
        message.setUserInfo(CLUserManager.getInstence().getUserInfo());
        /// 目标id
        message.setTargetId(userId);
        long timeStamp = CLUtils.timeStamp;
        /// 消息id
        message.setMessageId("" + timeStamp);
        return message;
    }

    /**
     * 发送图片
     * @param filePath
     */
    public CLMessageBean sendImageMessage(String filePath,String fileName,int w,int h,String userId,boolean isGroup){
        CLMessageBean message = new CLMessageBean();
        /// 是否是群聊会话
        message.setGroupSession(isGroup);
        /// 消息内容
        message.setContent("");
        /// 消息类型
        message.setMessageType(CLMessageBodyType.MessageBodyType_Image.getTypeName());
        message.setLocalUrl(filePath);
        int[] size = BitmapUtils.getImageWidthHeight(filePath);
        /// 设置图片的size
        message.setWitdh(w);
        message.setHeight(h);
        message.setMediaUrl(fileName);
        /// 发送者信息
        message.setUserInfo(CLUserManager.getInstence().getUserInfo());
        /// 目标id
        message.setTargetId(userId);
        long timeStamp = CLUtils.timeStamp;
        /// 消息id
        message.setMessageId("" + timeStamp);
        CLUploadManager.getInstance().uploadImage(filePath, fileName, new CLUploadManager.CLUploadOnLitenser() {
            @Override
            public void uploadSuccess(String fileName) {
                LogPrintUtils.eTag("图片上传",fileName);
                if (uploadStatusOnListener != null){
                    uploadStatusOnListener.onSuccess(fileName);
                }
            }
            @Override
            public void uploadError(String fileName) {
                if (uploadStatusOnListener != null){
                    uploadStatusOnListener.onFailure(fileName);
                }
            }
            @Override
            public void uploadProgress(int progress) {

            }
            @Override
            public void uploadCancel(String fileName) {

            }
        });
        return message;
    }

    /**
     * 发送音频
     * @param filePath
     */
    public CLMessageBean sendAudioMessage(String filePath,int duration,String userId,boolean isGroup){
        CLMessageBean message = new CLMessageBean();
        /// 是否是群聊会话
        message.setGroupSession(isGroup);
        /// 消息内容
        message.setContent("");
        /// 消息类型
        message.setMessageType(CLMessageBodyType.MessageBodyType_Voice.getTypeName());
        message.setLocalUrl(filePath);
        String imageName = CLUtils.timeStamp + ".mp3";
        message.setMediaUrl(imageName);
        message.setDuration(duration);
        /// 发送者信息
        message.setUserInfo(CLUserManager.getInstence().getUserInfo());
        /// 目标id
        message.setTargetId(userId);
        long timeStamp = CLUtils.timeStamp;
        /// 消息id
        message.setMessageId("" + timeStamp);
        return message;
    }

    /**
     * 发送视频
     * @param filePath
     */
    public CLMessageBean sendVideoMessage(String filePath,String thumnailPath,int duration,String userId,boolean isGroup){
        CLMessageBean message = new CLMessageBean();
        /// 是否是群聊会话
        message.setGroupSession(isGroup);
        /// 消息内容
        message.setContent("");
        /// 消息类型
        message.setMessageType(CLMessageBodyType.MessageBodyType_Video.getTypeName());
        message.setLocalUrl(filePath);
        String videoName = CLUtils.timeStamp + ".mp4";
        message.setMediaUrl(videoName);
        int[] size = BitmapUtils.getImageWidthHeight(CLEmojiFileUtils.getFolderPath("/") + thumnailPath);
        /// 设置图片的size
        if (size != null && size.length > 0){
            message.setWitdh(size[0]);
            message.setHeight(size[1]);
        }
        message.setVideoThumbnail(thumnailPath);
        message.setDuration(duration);
        /// 发送者信息
        message.setUserInfo(CLUserManager.getInstence().getUserInfo());
        /// 目标id
        message.setTargetId(userId);

        long timeStamp = CLUtils.timeStamp;
        /// 消息id
        message.setMessageId("" + timeStamp);

        return message;
    }

    /**
     * 发送文本消息
     * @param text 内容
     */
    public CLMessageBean sendTextMessage(String text,String userId,boolean isGroup){
        CLMessageBean message = new CLMessageBean();
        /// 是否是群聊会话
        message.setGroupSession(isGroup);
        /// 消息内容
        message.setContent(text);
        /// 消息类型
        message.setMessageType(CLMessageBodyType.MessageBodyType_Text.getTypeName());
        /// 发送者信息
        message.setUserInfo(CLUserManager.getInstence().getUserInfo());
        /// 目标id
        message.setTargetId(userId);
        long timeStamp = CLUtils.timeStamp;
        /// 消息id
        message.setMessageId("" + timeStamp);
        sendMessage(message,userId);

        return message;
    }

    public CLMessageBean sendMessage(CLMessageBean message, String userId){
        /// 是否是群聊会话
        message.setGroupSession(false);

        /// 发送状态
        if (CLMQTTManager.getInstance().getCurrentStatus() != CLMQTTManager.CLMQTTStatus.connect_succss){
            message.setSendStatus(SendStatus_FAILED.getTypeName());
        }else {
            message.setSendStatus(SendStatus_SENDING.getTypeName());
        }
        /// 发送时间
        message.setSentTime("" + CLUtils.timeStamp);

        String jsonString = JSON.toJSONString(message);

        /// 发送
        if (CLMQTTManager.getInstance().getCurrentStatus() == CLMQTTManager.CLMQTTStatus.connect_succss){
            CLMQTTManager.getInstance().sendSingleMessage(jsonString,userId);
        }

        LogPrintUtils.d(jsonString);

        CLMQTTManager.getInstance().setMessageStatusOnListener(new CLMQTTManager.CLSendMessageStatusOnListener() {
            @Override
            public void onSuccess(String message) {
                if (messageStatusOnListener != null){
                    Log.e("tag","message = " + message);
                    CLMessageBean messageBean = JSON.parseObject(message, CLMessageBean.class);
                    // 发送成功  CLSendStatus
                    messageBean.setSendStatus(SendStatus_SEND.getTypeName());
                    messageStatusOnListener.onSuccess(messageBean);
                }
            }

            @Override
            public void onFailure(String message) {
                if (messageStatusOnListener != null){
                    CLMessageBean messageBean = JSON.parseObject(message, CLMessageBean.class);
                    /// 发送失败 CLSendStatus
                    messageBean.setSendStatus(SendStatus_FAILED.getTypeName());
                    messageStatusOnListener.onFailure(messageBean);
                }
            }
        });
        return message;
    }


    public void receiveMessageHandler(String msg){
        CLMessageBean messageBean = JSON.parseObject(msg, CLMessageBean.class);
        if (receiveMessageOnListener != null){
            receiveMessageOnListener.onMessage(messageBean);
        }
    }


}
