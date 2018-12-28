package com.cleven.clchat.fragment;

import android.content.Intent;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.TextView;

import com.cleven.clchat.API.OkGoUtil;
import com.cleven.clchat.R;
import com.cleven.clchat.base.CLBaseFragment;
import com.cleven.clchat.contack.activity.CLAddFriendActivity;
import com.cleven.clchat.contack.bean.CLFriendBean;
import com.cleven.clchat.contack.bean.CLNewFriendBean;
import com.cleven.clchat.fragment.contack.adpater.ContactsAdapter;
import com.cleven.clchat.fragment.contack.views.PinnedHeaderDecoration;
import com.cleven.clchat.manager.CLMessageManager;
import com.cleven.clchat.utils.CLAPPConst;
import com.cleven.clchat.utils.CLHUDUtil;
import com.cleven.clchat.utils.CLJsonUtil;
import com.nanchen.wavesidebar.SearchEditText;
import com.nanchen.wavesidebar.Trans2PinYinUtil;
import com.nanchen.wavesidebar.WaveSideBarView;
import com.wuhenzhizao.titlebar.widget.CommonTitleBar;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Created by cleven on 2018/12/11.
 */

public class CLContactFragment extends CLBaseFragment {

    private static int NEWFRIENDNOTIFAICATIONNUMBER = 0;

    private List<CLFriendBean> mContactModels;
    private List<CLFriendBean> mShowModels;
    private RecyclerView mRecyclerView;
    private WaveSideBarView mWaveSideBarView;
    private SearchEditText mSearchEditText;
    private ContactsAdapter mAdapter;
    private CommonTitleBar titleBar;

    @Override
    public View initView() {
        View view = View.inflate(mContext, R.layout.contact_layout,null);

        bindView(view);

        observerReceiveFriendHandler();
        return view;
    }

    private void observerReceiveFriendHandler() {
        /// 监听收到好友请求
        CLMessageManager.getInstance().setReceiveFriendOnListener(new CLMessageManager.CLReceiveFriendOnListener() {
            @Override
            public void onMessage(CLNewFriendBean friendBean) {
                NEWFRIENDNOTIFAICATIONNUMBER += 1;
                refreshFriendNotication();
            }
        });
    }

    /// 刷新好友通知cell
    private void refreshFriendNotication(){
        CLFriendBean userBean = mContactModels.get(1);
        userBean.setUnreadNum(NEWFRIENDNOTIFAICATIONNUMBER);
        mAdapter.notifyItemChanged(1);
    }

    ///设置titleBar
    public void setTitleBar(CommonTitleBar titleBar) {
        this.titleBar = titleBar;
        TextView rightTextView = titleBar.getRightTextView();
        rightTextView.setText("➕");
        titleBar.setListener(new CommonTitleBar.OnTitleBarListener() {
            @Override
            public void onClicked(View v, int action, String extra) {
                if (action == CommonTitleBar.ACTION_RIGHT_TEXT){
                    Intent intent = new Intent(mContext,CLAddFriendActivity.class);
                    intent.putExtra("title","查询好友");
                    mContext.startActivity(intent);
                }
            }
        });
    }

    private void bindView(View view) {

        // RecyclerView设置相关
        mRecyclerView = (RecyclerView) view.findViewById(R.id.main_recycler);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(mContext));
        final PinnedHeaderDecoration decoration = new PinnedHeaderDecoration();
        decoration.registerTypePinnedHeader(1, new PinnedHeaderDecoration.PinnedHeaderCreator() {
            @Override
            public boolean create(RecyclerView parent, int adapterPosition) {
                return true;
            }
        });
        mRecyclerView.addItemDecoration(decoration);

        // 侧边设置相关
        mWaveSideBarView = (WaveSideBarView) view.findViewById(R.id.main_side_bar);
        mWaveSideBarView.setTextColor(R.color.black);
        mWaveSideBarView.setOnSelectIndexItemListener(new WaveSideBarView.OnSelectIndexItemListener() {
            @Override
            public void onSelectIndexItem(String letter) {
                for (int i=0; i<mContactModels.size(); i++) {
                    if (mContactModels.get(i).getIndex().equals(letter)) {
                        ((LinearLayoutManager) mRecyclerView.getLayoutManager()).scrollToPositionWithOffset(i, 0);
                        return;
                    }
                }
            }
        });


        // 搜索按钮相关
        mSearchEditText = (SearchEditText) view.findViewById(R.id.main_search);
        mSearchEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                mShowModels.clear();
                for (CLFriendBean model : mContactModels) {
                    String str = Trans2PinYinUtil.trans2PinYin(model.getName());
                    if (str.contains(s.toString()) || model.getName().contains(s.toString())) {
                        if (model.getItemType() != 10000 || s.toString().length() == 0){
                            mShowModels.add(model);
                        }
                    }
                }
                mAdapter.notifyDataSetChanged();
            }
        });
    }

    @Override
    public void initData() {
        super.initData();
        /// 查询未查看的好友条数
        NEWFRIENDNOTIFAICATIONNUMBER = CLNewFriendBean.getAllNewFriendUnReadData();

        mContactModels = new ArrayList<>();
        mShowModels = new ArrayList<>();

        initDefaultData();

        mShowModels.addAll(mContactModels);
        mAdapter = new ContactsAdapter(mContext, mShowModels, new ContactsAdapter.onClickItemListener() {
            @Override
            public void onItem(int postion) {
                if (postion == 1){ // 好友通知
                    Intent intent = new Intent(mContext,CLAddFriendActivity.class);
                    intent.putExtra("title","好友通知");
                    startActivityForResult(intent,100);
                    NEWFRIENDNOTIFAICATIONNUMBER = 0;
                    refreshFriendNotication();
                }
            }
        });
        mRecyclerView.setAdapter(mAdapter);

        List<CLFriendBean> allFriendData = CLFriendBean.getAllFriendData();
        if (allFriendData.size() <= 0){
            getFriendList();
        }else {
            mContactModels.addAll(allFriendData);
            mShowModels.addAll(allFriendData);
            mAdapter.notifyDataSetChanged();
        }

    }

    private void initDefaultData(){
        CLFriendBean messageBean = new CLFriendBean();
        messageBean.setName("消息通知");
        messageBean.setItemType(10000);
        messageBean.setUnreadNum(0);
        mContactModels.add(messageBean);
        CLFriendBean friendBean = new CLFriendBean();
        friendBean.setName("好友通知");
        friendBean.setItemType(10000);
        friendBean.setUnreadNum(NEWFRIENDNOTIFAICATIONNUMBER);
        mContactModels.add(friendBean);
    }

    private void getFriendList(){
        CLHUDUtil.showLoading(mContext,"正在加载好友...");
        OkGoUtil.getRequets(CLAPPConst.HTTP_SERVER_BASE_URL + "friend/friendList", "friendList", null, new OkGoUtil.CLNetworkCallBack() {
            @Override
            public void onSuccess(Map response) {
                List list = (List) response.get("data");
                for (int i = 0; i< list.size(); i++){
                    CLFriendBean userBean = CLJsonUtil.parseJsonToObj(list.get(i).toString(), CLFriendBean.class);
                    mContactModels.add(userBean);
                    mShowModels.add(userBean);
                    /// 插入数据库
                    CLFriendBean.updateData(userBean);
                }
                mAdapter.notifyDataSetChanged();
                CLHUDUtil.hideHUD();
            }

            @Override
            public void onFailure(Map error) {
                CLHUDUtil.showErrorHUD(mContext,"获取好友失败");
            }
        });

    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (mContactModels != null){
            mContactModels.clear();
            mContactModels = null;
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 100){ // 好友通知
            /// 同意好友请求回调,刷新数据
            List<CLFriendBean> allFriendData = CLFriendBean.getAllFriendData();
            mContactModels.clear();
            mShowModels.clear();
            initDefaultData();
            mContactModels.addAll(allFriendData);
            mShowModels.addAll(mContactModels);
            mAdapter.notifyDataSetChanged();
        }

    }
}
