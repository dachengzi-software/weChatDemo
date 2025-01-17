package com.emmanuel.wechatdemo.adapter;

import android.content.Context;
import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.emmanuel.wechatdemo.R;
import com.emmanuel.wechatdemo.activity.BrowsePictureActivity;
import com.emmanuel.wechatdemo.bean.ShuoShuo;
import com.emmanuel.wechatdemo.databinding.ItemFriendsBinding;
import com.emmanuel.wechatdemo.databinding.ViewStubPictureBinding;
import com.emmanuel.wechatdemo.databinding.ViewStubVideoBinding;
import com.emmanuel.wechatdemo.util.Constants;
import com.emmanuel.wechatdemo.util.ImageLoadUtil;
import com.emmanuel.wechatdemo.util.UserUtil;
import com.emmanuel.wechatdemo.view.CommentLinearLayout;
import com.emmanuel.wechatdemo.view.CommentPopupWindows;
import com.emmanuel.wechatdemo.view.MultiImageView;
import com.emmanuel.wechatdemo.view.VideoTextureView;
import com.nostra13.universalimageloader.core.ImageLoader;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by user on 2016/8/16.
 */
public class FriendsAdapter extends BaseRecycleViewAdapter {
    private static final String TAG = "FriendsAdapter";

    private final int TYPE_HEAD = 101;
    private final int TYPE_FOOTER = 102;
    private final int TYPE_CONTENT_NORMAL = 1002; //普通说说
    private final int TYPE_CONTENT_PICTURE = 1003; //带图片说说
    private final int TYPE_CONTENT_VIDEO = 1004; //带视频说说

    private Context context;
    private CommentPopupWindows popupWindow;
    private SSViewHolder ssViewHolder;
    private int currentPosition = -1;
    private Handler myHandler;

    private CommentPopupWindows.PopupwindowClickListener onClickListener = new CommentPopupWindows.PopupwindowClickListener() {
        @Override
        public void onClick(int id) {
            switch (id) {
                case R.id.pop_win_layout_zan:
                    if (currentPosition >= 0 && currentPosition < datas.size()) {
                        ShuoShuo ss = (ShuoShuo) datas.get(currentPosition);
                        if (!ss.hasZan) {
                            String name = UserUtil.getInstance(context).getString(UserUtil.KEY_NAME, "");
                            ss.zanList.add(name);
                            notifyDataSetChanged();
                            ss.hasZan = true;
                        } else {
                            String name = UserUtil.getInstance(context).getString(UserUtil.KEY_NAME, "");
                            ss.zanList.remove(name);
                            notifyDataSetChanged();
                            ss.hasZan = false;
                        }
                    }
                    break;
                case R.id.pop_win_layout_comment:
                    if (myHandler != null) {
                        Message message = new Message();
                        Bundle bundle = new Bundle();
                        bundle.putInt(Constants.BUNDLE_KEY_SHUOSHUO_POS, currentPosition);
                        message.what = Constants.HANDLER_FLAG_SHOW_EDITTEXT;
                        message.setData(bundle);
                        myHandler.sendMessage(message);
                    }
                    break;
            }
        }
    };

    public FriendsAdapter(Context context, Handler handler) {
        this.context = context;
        this.myHandler = handler;
    }

    @Override
    public int getItemViewType(int position) {
        if (position == 0) {
            return TYPE_HEAD;
        } else if (position + 1 == getItemCount()) {
            return TYPE_FOOTER;
        } else {
            if (position >= datas.size())
                return TYPE_CONTENT_NORMAL;
            ShuoShuo shuoShuo = (ShuoShuo) datas.get(position - 1);
            if (shuoShuo.picList != null && shuoShuo.picList.size() > 0) {
                return TYPE_CONTENT_PICTURE;
            }
            if (!TextUtils.isEmpty(shuoShuo.videoPath)) {
                return TYPE_CONTENT_VIDEO;
            }
            return TYPE_CONTENT_NORMAL;
        }
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view;
        if (viewType == TYPE_HEAD) {
            view = LayoutInflater.from(context).inflate(R.layout.item_friends_header, parent, false);
        } else if (viewType == TYPE_FOOTER) {
            view = LayoutInflater.from(context).inflate(R.layout.footer_view_load_more, parent, false);
        } else {
            view = LayoutInflater.from(context).inflate(R.layout.item_friends, parent, false);
        }
        SSViewHolder viewHolder = new SSViewHolder(view, viewType);
        popupWindow = new CommentPopupWindows(context);
        popupWindow.setPopupwindowClickListener(onClickListener);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        if (getItemViewType(position) == TYPE_HEAD) {

        } else if (getItemViewType(position) == TYPE_FOOTER) {

        } else {
            int dataIndex = position - 1;
            SSViewHolder viewHolder = (SSViewHolder) holder;
            final ShuoShuo shuoShuo = (ShuoShuo) datas.get(dataIndex);
            viewHolder.tvName.setText(shuoShuo.user.name);
            viewHolder.tvContent.setText(shuoShuo.content);
            if (shuoShuo.user.photoUrl == null) {
                viewHolder.ivPhoto.setImageResource(R.mipmap.myphoto);
            } else {
                ImageLoader.getInstance().displayImage(shuoShuo.user.photoUrl, viewHolder.ivPhoto, ImageLoadUtil.getOptions2());
            }
            viewHolder.ivComment.setOnClickListener(new OnViewClickListener(null, dataIndex));
            if (shuoShuo.zanList == null || shuoShuo.zanList.size() <= 0) {
                viewHolder.layoutZans.setVisibility(View.GONE);
                if (shuoShuo.commentList == null || shuoShuo.commentList.size() <= 0) {
                    viewHolder.layoutSns.setVisibility(View.GONE);
                }
            } else {
                viewHolder.layoutZans.setVisibility(View.VISIBLE);
                viewHolder.tvZans.setText(getZansText(shuoShuo.zanList));
            }
            if (!(shuoShuo.commentList == null || shuoShuo.commentList.size() <= 0)) {
                viewHolder.layoutSns.setVisibility(View.VISIBLE);
                viewHolder.commentLinearLayout.setVisibility(View.VISIBLE);
                viewHolder.commentLinearLayout.setCommentList(shuoShuo.commentList, dataIndex);
            } else {
                viewHolder.commentLinearLayout.setVisibility(View.GONE);
            }
            if ((shuoShuo.commentList == null || shuoShuo.commentList.size() <= 0) || (shuoShuo.zanList == null || shuoShuo.zanList.size() <= 0)) {
                viewHolder.divider1.setVisibility(View.GONE);
            } else {
                viewHolder.divider1.setVisibility(View.VISIBLE);
            }

            if (getItemViewType(position) == TYPE_CONTENT_PICTURE) {
                initPictureLayout(viewHolder, dataIndex, shuoShuo, position);
            } else if (getItemViewType(position) == TYPE_CONTENT_VIDEO) {
                initVideoLayout(viewHolder, dataIndex, shuoShuo, position);
            }
        }
    }

    private void initVideoLayout(final SSViewHolder viewHolder, int dataIndex, ShuoShuo shuoShuo, int position) {
        viewHolder.videoTextureView.setVideoPath(shuoShuo.videoPath);
        viewHolder.videoTextureView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!viewHolder.videoTextureView.getPlayStatus())
                    viewHolder.videoTextureView.startMediaPlayer();
                else {
                    viewHolder.videoTextureView.pauseMediaPlayer();
                }
            }
        });
    }

    private void initPictureLayout(SSViewHolder viewHolder, final int dataIndex, final ShuoShuo shuoShuo, int position) {
        if (shuoShuo.picList != null) {
            if (shuoShuo.picList.size() > 0) {
                if (shuoShuo.picList.size() == 1) {
                    viewHolder.viewStubIvPic.setVisibility(View.VISIBLE);
                    viewHolder.viewStubMiv.setVisibility(View.GONE);
                    viewHolder.viewStubIvPic.setOnClickListener(new OnViewClickListener(viewHolder, position));
                    ImageLoader.getInstance().displayImage(shuoShuo.picList.get(0).uri, viewHolder.viewStubIvPic, ImageLoadUtil.getOptions1());
                } else {
                    viewHolder.viewStubIvPic.setVisibility(View.GONE);
                    viewHolder.viewStubMiv.setVisibility(View.VISIBLE);
                    viewHolder.viewStubMiv.setShuoShuoPosition(dataIndex);
                    viewHolder.viewStubMiv.setOnItemClickListener(new MultiImageView.OnItemClickListener() {
                        @Override
                        public void onItemClick(View view, int position, int shuoPos) {
                            Intent intent = new Intent(context, BrowsePictureActivity.class);
                            intent.putExtra("type", Constants.TYPE_BROWSE);
                            intent.putExtra("selectedPictures", shuoShuo.picList);
                            context.startActivity(intent);
                        }
                    });
                    List<String> list = new ArrayList<String>();
                    for (int i = 0; i < shuoShuo.picList.size(); i++) {
                        list.add(shuoShuo.picList.get(i).uri);
                    }
                    viewHolder.viewStubMiv.setList(list);
                }
            }
        }
    }

    private String getZansText(List<String> zans) {
        StringBuilder str = new StringBuilder("");
        for (int i = 0; i < zans.size(); i++) {
            str.append(zans.get(i));
            if (i + 1 < zans.size()) {
                str.append('，');
            }
        }
        return str.toString();
    }

    @Override
    public int getItemCount() {
        return datas.size() + 2;
    }

    public class SSViewHolder extends RecyclerView.ViewHolder {

        public TextView tvName;
        public TextView tvTime;
        public TextView tvContent;
        public TextView tvZans;
        public LinearLayout layoutZans, layoutSns;
        public CommentLinearLayout commentLinearLayout;
        public ImageView ivPhoto, ivComment;

        public ImageView ivHeader;

        public ImageView viewStubIvPic;
        public MultiImageView viewStubMiv;
        public View divider1;

        //视频部分
        public ImageView ivPlay;
        public VideoTextureView videoTextureView;

        public SSViewHolder(View itemView, int itemType) {
            super(itemView);

            if (itemType == TYPE_HEAD) {

            } else if (itemType == TYPE_FOOTER) {

            } else {

                ItemFriendsBinding binding = DataBindingUtil.bind(itemView);

                tvName = (TextView) itemView.findViewById(R.id.tv_name);
                tvTime = (TextView) itemView.findViewById(R.id.tv_msg_time);
                tvContent = (TextView) itemView.findViewById(R.id.tv_content);
                ivPhoto = (ImageView) itemView.findViewById(R.id.iv_photo);

                layoutZans = (LinearLayout) itemView.findViewById(R.id.layout_zans);
                commentLinearLayout = (CommentLinearLayout) itemView.findViewById(R.id.layout_comment);
                commentLinearLayout.setHandler(myHandler);
                tvZans = (TextView) itemView.findViewById(R.id.tv_zans);
                layoutSns = (LinearLayout) itemView.findViewById(R.id.layout_sns);
                ivComment = (ImageView) itemView.findViewById(R.id.btn_comment);
                if (itemType == TYPE_CONTENT_PICTURE) {
                    //显示图片

                    binding.viewStubProxy.getViewStub().setLayoutResource(R.layout.view_stub_picture);
                    binding.viewStubProxy.getViewStub().inflate();
                    ViewStubPictureBinding viewStubPictureBinding = (ViewStubPictureBinding) binding.viewStubProxy.getBinding();
                    viewStubMiv = viewStubPictureBinding.mivPicture;
                    viewStubIvPic = viewStubPictureBinding.ivPicture;
                } else if (itemType == TYPE_CONTENT_VIDEO) {
                    //显示视频
                    binding.viewStubProxy.getViewStub().setLayoutResource(R.layout.view_stub_video);
                    binding.viewStubProxy.getViewStub().inflate();
                    ViewStubVideoBinding viewStubVideoBinding = (ViewStubVideoBinding) binding.viewStubProxy.getBinding();
                    videoTextureView = (VideoTextureView) viewStubVideoBinding.videoTextureView;
                    ivPlay = (ImageView) viewStubVideoBinding.ivPlay;
                    videoTextureView.setIvTip(ivPlay);
                }
                divider1 = binding.divider1;
            }

        }
    }

    public class OnViewClickListener implements View.OnClickListener {

        private SSViewHolder viewHolder;
        private int pos;

        public OnViewClickListener(SSViewHolder viewHolder, int position) {
            this.viewHolder = viewHolder;
            this.pos = position;
        }

        @Override
        public void onClick(View view) {
            switch (view.getId()) {
                case R.id.btn_comment:
                    ssViewHolder = viewHolder;
                    currentPosition = pos;
                    ShuoShuo shuoShuo = (ShuoShuo) datas.get(currentPosition);
                    popupWindow.setZanFlag(shuoShuo.hasZan);
                    popupWindow.showLeft(view);
                    break;
                case R.id.iv_picture:
                    break;
            }
        }
    }


}
