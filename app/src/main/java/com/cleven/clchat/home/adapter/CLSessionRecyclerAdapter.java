package com.cleven.clchat.home.adapter;

import android.content.Context;
import android.graphics.drawable.AnimationDrawable;
import android.net.Uri;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.cleven.clchat.R;
import com.cleven.clchat.home.Bean.CLMessageBean;
import com.cleven.clchat.home.Bean.CLMessageBodyType;
import com.cleven.clchat.home.Bean.CLReceivedStatus;
import com.cleven.clchat.home.Bean.CLSendStatus;
import com.cleven.clchat.home.CLEmojiCommon.utils.CLEmojiCommonUtils;
import com.cleven.clchat.manager.CLUserManager;
import com.cleven.clchat.utils.CLImageLoadUtil;
import com.cleven.clchat.utils.CLPhotoBrowser;
import com.cleven.clchat.utils.CLUtils;
import com.lqr.audio.AudioPlayManager;
import com.lqr.audio.IAudioPlayListener;

import java.util.List;

import dev.utils.app.SizeUtils;
import dev.utils.common.ScaleUtils;

import static com.cleven.clchat.home.Bean.CLMessageBodyType.MessageBodyType_Text;

/**
 * Created by cleven on 2018/12/14.
 */

public class CLSessionRecyclerAdapter extends RecyclerView.Adapter {

    /// 点击重试按钮回调
    private CLMessageSendFailListener messageSendFailListener;
    public void setMessageSendFailListener(CLMessageSendFailListener messageSendFailListener) {
        this.messageSendFailListener = messageSendFailListener;
    }
    public interface CLMessageSendFailListener{
        void onRetry(CLMessageBean messageBean);
    }

    /// 上下文
    private final Context mContext;
    private final List<CLMessageBean> mMessages;
    private final LayoutInflater layoutInflater;

    public CLSessionRecyclerAdapter(Context context, List<CLMessageBean> messages) {
        this.mContext = context;
        this.mMessages = messages;
        /// 初始化加载布局
        layoutInflater = LayoutInflater.from(this.mContext);
    }

    class  CLTimeViewHolder extends RecyclerView.ViewHolder {

        private final TextView mTime;

        public CLTimeViewHolder(Context mContext, View itemView) {
            super(itemView);
            mTime = itemView.findViewById(R.id.message_time);
        }

        public void setTimeData(String sentTime) {
            mTime.setText(CLUtils.formatTime(sentTime));
        }
    }

    class  CLAudioViewHolder extends RecyclerView.ViewHolder {

        private ImageView ivAvatar;
        private TextView name;
        private ImageView sendfail;
        private ProgressBar pbBar;
        private final RelativeLayout mContent;
        private final ImageView mVoiceImage;
        private final TextView mAudio_duration;
        private View mAudio_unread;
        private AnimationDrawable animationDrawable;
        private int postion = 0;
        private int[] leftImages = {R.mipmap.audio_animation_list_left_1,R.mipmap.audio_animation_list_left_2,R.mipmap.audio_animation_list_left_3};
        private int[] rightImages = {R.mipmap.audio_animation_list_right_1,R.mipmap.audio_animation_list_right_2,R.mipmap.audio_animation_list_right_3};
        private Handler handler;
        private  boolean isPlaying = false;

        public CLAudioViewHolder(final Context mContext, View itemView, boolean isGroup) {
            super(itemView);
            LinearLayout contentLayout = (LinearLayout) itemView.findViewById(R.id.contentLayoout);
            ivAvatar = (ImageView)itemView.findViewById( R.id.iv_avatar );
            name = (TextView)itemView.findViewById( R.id.name );
            sendfail = (ImageView)itemView.findViewById( R.id.sendfail );
            pbBar = (ProgressBar)itemView.findViewById( R.id.pb_bar );
            mContent = (RelativeLayout) itemView.findViewById(R.id.content);
            mAudio_unread = (View) itemView.findViewById(R.id.audio_unread);
            mVoiceImage = (ImageView) itemView.findViewById(R.id.voice_image);
            mAudio_duration = (TextView) itemView.findViewById(R.id.audio_duration);
            animationDrawable = (AnimationDrawable) mVoiceImage.getBackground();

            //单聊改变布局
            if (isGroup == false){
                RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) contentLayout.getLayoutParams();
                params.addRule(RelativeLayout.ALIGN_PARENT_TOP);
                params.topMargin = SizeUtils.dipConvertPx(8);
                contentLayout.setLayoutParams(params);
                name.setVisibility(View.GONE);
            }else {
                name.setVisibility(View.VISIBLE);
            }
        }

        private void setAnimation(final int[] images){
            handler = new Handler(){
               @Override
               public void handleMessage(Message msg) {
                   super.handleMessage(msg);
                   if (isPlaying == false){return;}
                   mVoiceImage.setImageResource(images[postion]);
                   postion += 1;
                   if (postion >= images.length){
                       postion = 0;
                   }
                   handler.removeCallbacksAndMessages(0);
                   if (isPlaying){
                       sendEmptyMessageDelayed(0,500);
                   }
               }
           };
            handler.sendEmptyMessageDelayed(0,0);
        }

        private void stopAnimation(CLMessageBean messageBean){
            isPlaying = false;
            handler.removeCallbacksAndMessages(0);
            if (messageBean.getUserInfo().getUserId().equals(CLUserManager.getInstence().getUserInfo().getUserId())){
                mVoiceImage.setImageResource(R.mipmap.audio_animation_right);
            }else {
                mVoiceImage.setImageResource(R.mipmap.audio_animation_left);
            }
        }

        public void setAudioData(final CLMessageBean messageBean) {
            mAudio_duration.setText(messageBean.getDuration() + "″");
            CLImageLoadUtil.loadRoundImg(ivAvatar,messageBean.getUserInfo().getAvatarUrl(),R.drawable.avatar,20);
            // 发送失败
            if (CLSendStatus.fromTypeName(messageBean.getSendStatus()) == CLSendStatus.SendStatus_FAILED){
                sendfail.setVisibility(View.VISIBLE);
                pbBar.setVisibility(View.GONE);
                mAudio_duration.setVisibility(View.GONE);
                sendfail.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        if (messageSendFailListener != null){
                            sendfail.setVisibility(View.GONE);
                            pbBar.setVisibility(View.VISIBLE);
                            messageSendFailListener.onRetry(messageBean);
                        }
                    }
                });
            }else if (CLSendStatus.fromTypeName(messageBean.getSendStatus()) == CLSendStatus.SendStatus_SEND){
                // 发送成功
                if (pbBar != null){
                    pbBar.setVisibility(View.GONE);
                }
                if (sendfail != null){
                    sendfail.setVisibility(View.GONE);
                }
                mAudio_duration.setVisibility(View.VISIBLE);
            }

            boolean isMe = false;
            if (messageBean.getUserInfo().getUserId().equals(CLUserManager.getInstence().getUserInfo().getUserId())){
                isMe = true;
            }

            if (isMe == false){
                if (CLReceivedStatus.fromTypeName(messageBean.getReceivedStatus()) != CLReceivedStatus.ReceivedStatus_LISTENED){
                    mAudio_unread.setVisibility(View.VISIBLE);
                }else {
                    mAudio_unread.setVisibility(View.GONE);
                }
            }

            /// 播放点击事件
            final boolean finalIsMe = isMe;
            mContent.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (isPlaying){
                        return;
                    }
                    isPlaying = true;
                    if (finalIsMe){
                        setAnimation(rightImages);
                    }else {
                        setAnimation(leftImages);
                        mAudio_unread.setVisibility(View.GONE);
                    }


                    String url = CLUtils.checkLocalPath(messageBean);
                    AudioPlayManager.getInstance().startPlay(mContext,Uri.parse(url), new IAudioPlayListener() {
                        @Override
                        public void onStart(Uri var1) {
                            //开播（一般是开始语音消息动画）
                        }

                        @Override
                        public void onStop(Uri var1) {
                            //停播（一般是停止语音消息动画）
                            stopAnimation(messageBean);
                        }

                        @Override
                        public void onComplete(Uri var1) {
                            //播完（一般是停止语音消息动画）
                            stopAnimation(messageBean);
                        }
                    });
                    /// 状态改为已听
                    messageBean.setReceivedStatus(CLReceivedStatus.ReceivedStatus_LISTENED.getTypeName());
                    /// 更新数据库
                    CLMessageBean.updateAudioPlayStatus(messageBean);
                }
            });

        }
    }

    class CLImageViewHolder extends RecyclerView.ViewHolder {

        private ImageView ivAvatar;
        private TextView name;
        private ImageView sendfail;
        private ProgressBar pbBar;
        private final ImageView mContent;
        private final LinearLayout contentLayout;

        public CLImageViewHolder(Context mContext, View itemView, boolean isGroup) {
            super(itemView);
            contentLayout = (LinearLayout) itemView.findViewById(R.id.contentLayoout);
            ivAvatar = (ImageView)itemView.findViewById( R.id.iv_avatar );
            name = (TextView)itemView.findViewById( R.id.name );
            sendfail = (ImageView)itemView.findViewById( R.id.sendfail );
            pbBar = (ProgressBar)itemView.findViewById( R.id.pb_bar );
            mContent = (ImageView) itemView.findViewById(R.id.content);

            //单聊改变布局
            if (isGroup == false){
                RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) contentLayout.getLayoutParams();
                params.addRule(RelativeLayout.ALIGN_PARENT_TOP);
                params.topMargin = SizeUtils.dipConvertPx(8);
                contentLayout.setLayoutParams(params);
                name.setVisibility(View.GONE);
            }else {
                name.setVisibility(View.VISIBLE);
            }

        }

        public void setImageData(final CLMessageBean data) {

            name.setText(data.getUserInfo().getName());

            /// 根据图片的size更新布局
            String url = CLUtils.checkLocalPath(data);
            CLImageLoadUtil.loadRoundImg(mContent,url,R.mipmap.default_image,0);
            RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) contentLayout.getLayoutParams();
            int[] ints = ScaleUtils.calcWidthHeightToScale(data.getWitdh(), data.getHeight(), (float) 1, (float) 0.5);
            params.width = ints[0];
            params.height = ints[1];
            contentLayout.setLayoutParams(params);
            ViewGroup.LayoutParams imageParams = mContent.getLayoutParams();
            imageParams.width = ints[0];
            imageParams.height = ints[1];
            mContent.setLayoutParams(imageParams);

            CLImageLoadUtil.loadRoundImg(ivAvatar,data.getUserInfo().getAvatarUrl(),R.drawable.avatar,20);
            // 发送失败
            if (CLSendStatus.fromTypeName(data.getSendStatus()) == CLSendStatus.SendStatus_FAILED){
                pbBar.setAlpha(1);
                sendfail.setAlpha(1);
                sendfail.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        if (messageSendFailListener != null){
                            sendfail.setVisibility(View.GONE);
                            pbBar.setVisibility(View.VISIBLE);
                            messageSendFailListener.onRetry(data);
                        }
                    }
                });
            }else if (CLSendStatus.fromTypeName(data.getSendStatus()) == CLSendStatus.SendStatus_SEND){
                // 发送成功,图片也上传成功
                pbBar.setAlpha(0);
                sendfail.setAlpha(0);
            }
            /// 浏览图片
            mContent.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    List<String> imageUrlList = CLMessageBean.getMediaUrlData(data.getTargetId());
                    int i = imageUrlList.indexOf(CLUtils.checkLocalPath(data));
                    CLPhotoBrowser.Browser(mContext,imageUrlList,i);
                }
            });
        }
    }

    class CLVideoViewHolder extends RecyclerView.ViewHolder {

        private ImageView ivAvatar;
        private TextView name;
        private ImageView sendfail;
        private ProgressBar pbBar;
        private final RelativeLayout mContent;
        private final LinearLayout contentLayout;
        private ImageView mVideo_thumbnail;
        private ImageView mVideo_play;

        public CLVideoViewHolder(Context mContext, View itemView, boolean isGroup) {
            super(itemView);
            contentLayout = (LinearLayout) itemView.findViewById(R.id.contentLayoout);
            ivAvatar = (ImageView)itemView.findViewById( R.id.iv_avatar );
            name = (TextView)itemView.findViewById( R.id.name );
            sendfail = (ImageView)itemView.findViewById( R.id.sendfail );
            pbBar = (ProgressBar)itemView.findViewById( R.id.pb_bar );
            mContent = (RelativeLayout) itemView.findViewById(R.id.content);
            mVideo_thumbnail = (ImageView) itemView.findViewById(R.id.video_thumbnail);
            mVideo_play = (ImageView) itemView.findViewById(R.id.video_play);

            //单聊改变布局
            if (isGroup == false){
                RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) contentLayout.getLayoutParams();
                params.addRule(RelativeLayout.ALIGN_PARENT_TOP);
                params.topMargin = SizeUtils.dipConvertPx(8);
                contentLayout.setLayoutParams(params);
                name.setVisibility(View.GONE);
            }else {
                name.setVisibility(View.VISIBLE);
            }

        }

        public void setVideoData(final CLMessageBean data) {

            name.setText(data.getUserInfo().getName());
            CLImageLoadUtil.loadRoundImg(ivAvatar,data.getUserInfo().getAvatarUrl(),R.drawable.avatar,20);
            CLImageLoadUtil.loadRoundImg(mVideo_thumbnail,data.getVideoThumbnail(),R.drawable.avatar,0);

            /// 根据图片的size更新布局
            RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) contentLayout.getLayoutParams();
            params.width = SizeUtils.dipConvertPx(data.getWitdh());
            params.height = SizeUtils.dipConvertPx(data.getHeight());
            contentLayout.setLayoutParams(params);

            // 发送失败
            if (CLSendStatus.fromTypeName(data.getSendStatus()) == CLSendStatus.SendStatus_FAILED){
                sendfail.setVisibility(View.VISIBLE);
                pbBar.setVisibility(View.GONE);
                sendfail.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        if (messageSendFailListener != null){
                            sendfail.setVisibility(View.GONE);
                            pbBar.setVisibility(View.VISIBLE);
                            messageSendFailListener.onRetry(data);
                        }
                    }
                });
            }else if (CLSendStatus.fromTypeName(data.getSendStatus()) == CLSendStatus.SendStatus_SEND){
                // 发送成功
                pbBar.setVisibility(View.GONE);
                sendfail.setVisibility(View.GONE);
            }

            String url = CLUtils.checkLocalPath(data);
        }
    }

    class CLMessageTextViewHolder extends RecyclerView.ViewHolder {

        private ImageView ivAvatar;
        private TextView name;
        private ImageView sendfail;
        private ProgressBar pbBar;
        private final TextView mContent;

        public CLMessageTextViewHolder(Context mContext, View itemView, boolean isGroup) {
            super(itemView);
            LinearLayout contentLayout = (LinearLayout) itemView.findViewById(R.id.contentLayoout);
            ivAvatar = (ImageView)itemView.findViewById( R.id.iv_avatar );
            name = (TextView)itemView.findViewById( R.id.name );
            sendfail = (ImageView)itemView.findViewById( R.id.sendfail );
            pbBar = (ProgressBar)itemView.findViewById( R.id.pb_bar );
            mContent = (TextView) itemView.findViewById(R.id.content);

            //单聊改变布局
            if (isGroup == false){
                RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) contentLayout.getLayoutParams();
                params.addRule(RelativeLayout.ALIGN_PARENT_TOP);
                params.topMargin = SizeUtils.dipConvertPx(8);
                contentLayout.setLayoutParams(params);
                name.setVisibility(View.GONE);
            }else {
                name.setVisibility(View.VISIBLE);
            }

        }

        public void setData(final CLMessageBean data) {

            name.setText(data.getUserInfo().getName());
            /// 过滤出表情,直接展示
            CLEmojiCommonUtils.spannableEmoticonFilter(mContent,data.getContent());
            CLImageLoadUtil.loadRoundImg(ivAvatar,data.getUserInfo().getAvatarUrl(),R.drawable.avatar,20);

            // 发送失败
            if (CLSendStatus.fromTypeName(data.getSendStatus()) == CLSendStatus.SendStatus_FAILED){
                sendfail.setVisibility(View.VISIBLE);
                pbBar.setVisibility(View.GONE);
                sendfail.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        if (messageSendFailListener != null){
                            sendfail.setVisibility(View.GONE);
                            pbBar.setVisibility(View.VISIBLE);
                            messageSendFailListener.onRetry(data);
                        }
                    }
                });
            }else if (CLSendStatus.fromTypeName(data.getSendStatus()) == CLSendStatus.SendStatus_SEND){
                // 发送成功
                pbBar.setVisibility(View.GONE);
                sendfail.setVisibility(View.GONE);
            }
        }
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int i) {
        CLMessageBean messageBean = mMessages.get(i);
        CLMessageBodyType messageBodyType = CLMessageBodyType.fromTypeName(messageBean.getMessageType());
        String currentUserId = "";
        if (CLUserManager.getInstence().getUserInfo() != null){
            currentUserId = CLUserManager.getInstence().getUserInfo().getUserId();
        }

        if (messageBodyType == MessageBodyType_Text){
            View baseView;
            /// 发送
            if (messageBean.getUserInfo().getUserId().equals(currentUserId)){
                baseView = layoutInflater.inflate(R.layout.message_right_text_item,null);
            }else { // 接受
                baseView = layoutInflater.inflate(R.layout.message_left_text_item,null);
            }
            return new CLMessageTextViewHolder(mContext, baseView,messageBean.isGroupSession());
        }else if (messageBodyType == CLMessageBodyType.MessageBodyType_Time){
            return new CLTimeViewHolder(mContext,layoutInflater.inflate(R.layout.message_time_layout,null));
        }else if (messageBodyType == CLMessageBodyType.MessageBodyType_Voice){
            View baseView;
            Boolean isMe = false;
            /// 发送
            if (messageBean.getUserInfo().getUserId().equals(currentUserId)){
                isMe = true;
                baseView = layoutInflater.inflate(R.layout.message_right_audio_item,null);
            }else { // 接受
                isMe = false;
                baseView = layoutInflater.inflate(R.layout.message_left_audio_item,null);
            }
            return new CLAudioViewHolder(mContext,baseView,messageBean.isGroupSession());
        }else if (messageBodyType == CLMessageBodyType.MessageBodyType_Image || messageBodyType == CLMessageBodyType.MessageBodyType_Emoji){
            View baseView;
            /// 发送
            if (messageBean.getUserInfo().getUserId().equals(currentUserId)){
                baseView = layoutInflater.inflate(R.layout.message_right_image_item,null);
            }else { // 接受
                baseView = layoutInflater.inflate(R.layout.message_left_image_item,null);
            }
            return new CLImageViewHolder(mContext,baseView,messageBean.isGroupSession());
        }else if (messageBodyType == CLMessageBodyType.MessageBodyType_Video){
            View baseView;
            /// 发送
            if (messageBean.getUserInfo().getUserId().equals(currentUserId)){
                baseView = layoutInflater.inflate(R.layout.message_right_video_item,null);
            }else { // 接受
                baseView = layoutInflater.inflate(R.layout.message_left_video_item,null);
            }
            return new CLVideoViewHolder(mContext,baseView,messageBean.isGroupSession());
        }
        return null;
    }

    @Override
    public int getItemViewType(int position) {
        return position;
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        CLMessageBean messageBean = mMessages.get(position);
        CLMessageBodyType messageBodyType = CLMessageBodyType.fromTypeName(messageBean.getMessageType());
        if (messageBodyType == CLMessageBodyType.MessageBodyType_Text){
            CLMessageTextViewHolder textViewHolder = (CLMessageTextViewHolder) holder;
            textViewHolder.setData(messageBean);
        }else if (messageBodyType == CLMessageBodyType.MessageBodyType_Time){
            CLTimeViewHolder timeViewHolder = (CLTimeViewHolder) holder;
            timeViewHolder.setTimeData(messageBean.getSendTime());
        }else if (messageBodyType == CLMessageBodyType.MessageBodyType_Voice){
            CLAudioViewHolder audioViewHolder = (CLAudioViewHolder) holder;
            audioViewHolder.setAudioData(messageBean);
        }else if (messageBodyType == CLMessageBodyType.MessageBodyType_Image){
            CLImageViewHolder imageViewHolder = (CLImageViewHolder) holder;
            imageViewHolder.setImageData(messageBean);
        }else if (messageBodyType == CLMessageBodyType.MessageBodyType_Video){
            CLVideoViewHolder videoViewHolder = (CLVideoViewHolder) holder;
            videoViewHolder.setVideoData(messageBean);
        }

    }

    @Override
    public int getItemCount() {
        return mMessages.size();
    }
}
