package com.cleven.clchat.home.adapter;

import android.content.Context;
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
import android.widget.Toast;

import com.cleven.clchat.R;
import com.cleven.clchat.home.Bean.CLMessageBean;
import com.cleven.clchat.home.Bean.CLMessageBodyType;
import com.cleven.clchat.home.Bean.CLSendStatus;
import com.cleven.clchat.manager.CLUserManager;
import com.cleven.clchat.utils.CLUtils;

import java.util.List;

import dev.utils.app.SizeUtils;

import static com.cleven.clchat.home.Bean.CLMessageBodyType.MessageBodyType_Text;

/**
 * Created by cleven on 2018/12/14.
 */

public class CLSessionRecyclerAdapter extends RecyclerView.Adapter {

    /// 上下文
    private final Context mContext;
    private final List<CLMessageBean> mMessages;
    private final LayoutInflater layoutInflater;
    private int currentItemType;

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
            mTime.setText(CLUtils.formatTiem(sentTime));
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

        public CLAudioViewHolder(Context mContext, View itemView, boolean isGroup) {
            super(itemView);
            LinearLayout contentLayout = (LinearLayout) itemView.findViewById(R.id.contentLayoout);
            ivAvatar = (ImageView)itemView.findViewById( R.id.iv_avatar );
            name = (TextView)itemView.findViewById( R.id.name );
            sendfail = (ImageView)itemView.findViewById( R.id.sendfail );
            pbBar = (ProgressBar)itemView.findViewById( R.id.pb_bar );
            mContent = (RelativeLayout) itemView.findViewById(R.id.content);
            mVoiceImage = (ImageView) itemView.findViewById(R.id.voice_image);
            mAudio_duration = (TextView) itemView.findViewById(R.id.audio_duration);

            //单聊改变布局
            if (isGroup == false){
                RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) contentLayout.getLayoutParams();
                params.addRule(RelativeLayout.ALIGN_PARENT_TOP);
                params.topMargin = SizeUtils.dipConvertPx(15);
                contentLayout.setLayoutParams(params);
                name.setVisibility(View.GONE);
            }else {
                name.setVisibility(View.VISIBLE);
            }

        }

        public void setAudioData(CLMessageBean messageBean) {
            mAudio_duration.setText(messageBean.getDuration() + "″");
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
                params.topMargin = SizeUtils.dipConvertPx(15);
                contentLayout.setLayoutParams(params);
                name.setVisibility(View.GONE);
            }else {
                name.setVisibility(View.VISIBLE);
            }

        }

        public void setData(final CLMessageBean data) {

            name.setText(data.getUserInfo().getName());
            mContent.setText(data.getContent());

//            Glide.with(mContext).load(data.getUserInfo().getAvatarUrl()).into(ivAvatar);
            // 发送失败
            if (CLSendStatus.fromTypeName(data.getSendStatus()) == CLSendStatus.SendStatus_FAILED){
                sendfail.setVisibility(View.VISIBLE);
                pbBar.setVisibility(View.GONE);
                sendfail.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Toast.makeText(mContext,"重发",Toast.LENGTH_SHORT).show();
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
        String currentUserId = CLUserManager.getInstence().getUserInfo().getUserId();

        if (messageBodyType == MessageBodyType_Text){
            View baseView;
            /// 发送
            if (messageBean.getUserInfo().getUserId() == currentUserId){
                baseView = layoutInflater.inflate(R.layout.message_right_text_item,null);
            }else { // 接受
                baseView = layoutInflater.inflate(R.layout.message_left_text_item,null);
            }
            return new CLMessageTextViewHolder(mContext, baseView,messageBean.isGroupSession());
        }else if (messageBodyType == CLMessageBodyType.MessageBodyType_Time){
            return new CLTimeViewHolder(mContext,layoutInflater.inflate(R.layout.message_time_layout,null));
        }else if (messageBodyType == CLMessageBodyType.MessageBodyType_Voice){
            View baseView;
            /// 发送
            if (messageBean.getUserInfo().getUserId() == currentUserId){
                baseView = layoutInflater.inflate(R.layout.message_right_audio_item,null);
            }else { // 接受
                baseView = layoutInflater.inflate(R.layout.message_left_audio_item,null);
            }
            return new CLAudioViewHolder(mContext,baseView,messageBean.isGroupSession());
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
            timeViewHolder.setTimeData(messageBean.getSentTime());
        }else if (messageBodyType == CLMessageBodyType.MessageBodyType_Voice){
            CLAudioViewHolder audioViewHolder = (CLAudioViewHolder) holder;
            audioViewHolder.setAudioData(messageBean);
        }

    }

    @Override
    public int getItemCount() {
        return mMessages.size();
    }
}
