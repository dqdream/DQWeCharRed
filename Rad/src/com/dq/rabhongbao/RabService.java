package com.dq.rabhongbao;

import java.util.ArrayList;
import java.util.List;

import android.accessibilityservice.AccessibilityService;
import android.util.Log;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;

public class RabService extends AccessibilityService {

	private static final String WECHAT_DETAILS_EN = "Details";
	private static final String WECHAT_DETAILS_CH = "红包详情";
	private static final String WECHAT_BETTER_LUCK_EN = "Better luck next time!";
	private static final String WECHAT_BETTER_LUCK_CH = "手慢了";
	private static final String WECHAT_EXPIRES_CH = "红包已失效";
	private static final String WECHAT_VIEW_SELF_CH = "查看红包";
	private static final String WECHAT_VIEW_OTHERS_CH = "领取红包";
//	private final static String WECHAT_NOTIFICATION_TIP = "[微信红包]";
//	private String text="请点击我";
	AccessibilityNodeInfo rootNodeInfo;
	List<AccessibilityNodeInfo> nodes = new ArrayList<AccessibilityNodeInfo>();
	private int status = 0;// 1 点击领 2点拆开 -1 红包没了
	private String LastID=null;
	@Override
	public void onAccessibilityEvent(AccessibilityEvent event) {
		// event.getEventType()
		rootNodeInfo = event.getSource();
		if (rootNodeInfo == null)
			return;
//		nodes=rootNodeInfo.findAccessibilityNodeInfosByText(text)
		checkNodeInfo();
		switch (status) {
		case -1:
			performGlobalAction(GLOBAL_ACTION_BACK);
			status=0;
			break;
		case 1:
			if (nodes.size() > 0) {
				rootNodeInfo = nodes.get(nodes.size() - 1);
				if (rootNodeInfo!=null) {
					if (shouldReturn(getNodeID(rootNodeInfo))) {
						return;
					}
					rootNodeInfo.getParent().performAction(AccessibilityNodeInfo.ACTION_CLICK);
					Log.e("vv",nodes.size()+ "---111"+rootNodeInfo.toString());
				}
			}
			break;
		case 2:
			rootNodeInfo.performAction(AccessibilityNodeInfo.ACTION_CLICK);
			Log.e("vv",nodes.size()+ "---eee"+rootNodeInfo.toString());
			break;
		}
//		 if (nodes.size() > 0) {
//		 rootNodeInfo = nodes.get(nodes.size() - 1);
//		 rootNodeInfo.performAction(AccessibilityNodeInfo.ACTION_CLICK);
//		 performGlobalAction(GLOBAL_ACTION_BACK);
//		 }
	}

	private void checkNodeInfo() {
		if (status==0) {
			nodes = findNodeList(new String[] { WECHAT_VIEW_OTHERS_CH,
					WECHAT_VIEW_SELF_CH });
			if (!nodes.isEmpty()) {
				status = 1;
				return;
			}
		}
		
		if (status == 1) {
			rootNodeInfo = (this.rootNodeInfo.getChildCount() > 3) ? this.rootNodeInfo
					.getChild(3) : rootNodeInfo;
			if (rootNodeInfo != null
					&& rootNodeInfo.getClassName().equals(
							"android.widget.Button")) {
				status = 2;
				return;
			}
		}

		if (status == 1 || status == 2) {
			nodes = findNodeList(new String[] { WECHAT_BETTER_LUCK_CH,
					WECHAT_DETAILS_CH, WECHAT_BETTER_LUCK_EN,
					WECHAT_DETAILS_EN, WECHAT_EXPIRES_CH });
			if (!nodes.isEmpty()) {
				status = -1;
			}
		}
	}

	private String getNodeID(AccessibilityNodeInfo node) {
		String content;
		try {
			AccessibilityNodeInfo i = node.getParent().getChild(0);
			content = i.getText().toString();
		} catch (NullPointerException npe) {
			return null;
		}
		return content;
	}

	private List<AccessibilityNodeInfo> findNodeList(String[] texts) {
		for (String text : texts) {
			if (text == null)
				continue;
			List<AccessibilityNodeInfo> nodes = rootNodeInfo
					.findAccessibilityNodeInfosByText(text);
			if (!nodes.isEmpty()){
				Log.e("vv", text+"---"+ nodes.size()+"--"+status);
				return nodes;
			}
		}
		return new ArrayList<>();
	}

	@Override
	public void onInterrupt() {
		// TODO Auto-generated method stub

	}

    private boolean shouldReturn(String id) {
        // ID为空
        if (id == null) return true;
        // 名称和缓存不一致
        if (id.equals(LastID)) return true;
        LastID=id;
        return false;
    }
}
