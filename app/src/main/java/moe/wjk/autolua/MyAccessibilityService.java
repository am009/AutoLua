package moe.wjk.autolua;

import android.accessibilityservice.AccessibilityService;
import android.accessibilityservice.AccessibilityServiceInfo;
import android.accessibilityservice.GestureDescription;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Path;
import android.graphics.PixelFormat;
import android.os.Build;
import android.os.Handler;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.WindowManager;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.preference.PreferenceManager;

import java.io.File;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.List;

import moe.wjk.autolua.utils.Utils;

public class MyAccessibilityService extends AccessibilityService {

    static {
        // load libnative.so / native.dll
        System.loadLibrary("native");
    }

    public static final String TAG = "MyAccessibilityService";
    public static final String ACTION_REPORT_QUICK = "moe.wjk.action.REPORT";
    public static final String ACTION_REPORT_NOTIFICATION = "moe.wjk.action.REPORT_NOTIFICATION";
    public static final String ACTION_HIDE_ACTION_BAR = "moe.wjk.action.HIDE_ACTION_BAR";
    public static final String ACTION_SHOW_ACTION_BAR = "moe.wjk.action.SHOW_ACTION_BAR";
    public static final String ACTION_CLICK_NOTIFICATION = "moe.wjk.action.ACTION_CLICK_NOTIFICATION";
    private static final int NOTIFICATION_DELAY_MS = 5000;
    static final int ONGOING_NOTIFICATION_ID = 1;
    private static final String CHANNEL_ID = "prepare-warning";
    private boolean isRunnerStop = false;


    private boolean isForeground = false;
    private boolean isAsAccessService = false;
    private AccessibilityNodeInfo currentRoot = null;
    private Handler mainHandler = null;

    FrameLayout mLayout;

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        super.onStartCommand(intent, flags, startId);
        if (intent.getAction() == null) {
            Log.w(TAG, "onStartCommand: intent with null action!");
        }
        if (intent.getAction().equals(MyAccessibilityService.ACTION_REPORT_QUICK)) {
            Log.d(TAG, "onStartCommand: Received ACTION_REPORT intent.");
            startReport(true);
        }
        if (intent.getAction().equals(MyAccessibilityService.ACTION_REPORT_NOTIFICATION)) {
            Log.d(TAG, "onStartCommand: Received ACTION_REPORT_NOTIFICATION intent.");
            startReport(false);
        }
        if (intent.getAction().equals(MyAccessibilityService.ACTION_HIDE_ACTION_BAR)) {
            if (!isAsAccessService) {
                Log.e(TAG, "onStartCommand: ACTION_SHOW_ACTION_BAR: accessibility service not enabled.");
                return START_NOT_STICKY;
            }
            disableActionBar();
        }
        if (intent.getAction().equals(MyAccessibilityService.ACTION_SHOW_ACTION_BAR)) {
            if (!isAsAccessService) {
                Log.e(TAG, "onStartCommand: ACTION_SHOW_ACTION_BAR: accessibility service not enabled.");
                return START_NOT_STICKY;
            }
            enableActionBar();
        }
        if (intent.getAction().equals(MyAccessibilityService.ACTION_CLICK_NOTIFICATION)) {
            isRunnerStop = true;
        }
        return START_STICKY;
    }

    @Override
    protected void onServiceConnected() {
        super.onServiceConnected();
        isAsAccessService = true;
        mainHandler = new Handler(this.getMainLooper());

        createNotificationChannel();

        // enable_action_bar
        SharedPreferences sharedPreferences =
                PreferenceManager.getDefaultSharedPreferences(this);
        boolean enable_action_bar = sharedPreferences.getBoolean("enable_action_bar", false);
        if (enable_action_bar) {
            enableActionBar();
        }

        Toast.makeText(this, getText(R.string.toast_accsrv_on_connected), Toast.LENGTH_SHORT).show();
    }

    @Override
    public boolean onUnbind(Intent intent) {
        isAsAccessService = false;
        mainHandler = null;
        return super.onUnbind(intent);
    }

    // lua vm wrapper
    public native boolean runFile(String path);

    public class LuaRunner implements Runnable {

        @Override
        public void run() {
            isRunnerStop = false;
            File code = new File(getFilesDir(), Utils.CODE_FILENAME);
            goForeground();
            try {
                // 预留时间让用户取消
                lua_sleep_ms(NOTIFICATION_DELAY_MS);
                runFile(code.getPath());
            } catch (InterruptedException e) {
                onScriptError(e);
            } finally {
                cancelForeground();
            }
        }
    }

    public class LuaRunnerQuick implements Runnable {

        @Override
        public void run() {
            isRunnerStop = false;
            File code = new File(getFilesDir(), Utils.CODE_FILENAME);
            runFile(code.getPath());
        }
    }

    void startReport(boolean isQuick) {
        if (isAsAccessService) {
            if (isQuick) {
                new Thread(new LuaRunnerQuick()).start();
            } else {
                new Thread(new LuaRunner()).start();
            }
        } else {
            Toast.makeText(this, getText(R.string.toast_accsrv_disable), Toast.LENGTH_LONG).show();
            Log.d(TAG, "startReport: AccessibilityService not enable in settings.");
        }
    }

    private void enableActionBar() {
        if (mLayout == null) {
            WindowManager wm = (WindowManager) getSystemService(WINDOW_SERVICE);
            mLayout = new FrameLayout(this);
            WindowManager.LayoutParams lp = new WindowManager.LayoutParams();
            lp.type = WindowManager.LayoutParams.TYPE_ACCESSIBILITY_OVERLAY;
            lp.format = PixelFormat.TRANSLUCENT;
            lp.flags |= WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE;
            lp.width = WindowManager.LayoutParams.WRAP_CONTENT;
            lp.height = WindowManager.LayoutParams.WRAP_CONTENT;
            lp.gravity = Gravity.TOP;
            LayoutInflater inflater = LayoutInflater.from(this);
            inflater.inflate(R.layout.action_bar, mLayout);
            wm.addView(mLayout, lp);
            configureButtons();
        }
    }

    private void disableActionBar() {
        if (mLayout != null) {
            WindowManager wm = (WindowManager) getSystemService(WINDOW_SERVICE);
            wm.removeView(mLayout);
            mLayout = null;
        }
    }


    private void configureButtons() {
        Button current = (Button) mLayout.findViewById(R.id.action_bar_exec);
        current.setOnClickListener(view -> startReport(true));
        current = mLayout.findViewById(R.id.action_bar_exec_notification);
        current.setOnClickListener(view -> startReport(false));
        current = mLayout.findViewById(R.id.action_bar_interrupt);
        current.setOnClickListener(view -> {
            isRunnerStop = true;
        });
    }

    @Override
    public void onAccessibilityEvent(AccessibilityEvent event) {
        int type = event.getEventType();
        if (type == AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED || type == AccessibilityEvent.TYPE_VIEW_FOCUSED) {
            AccessibilityNodeInfo root = getRootInActiveWindow();
            if (root != null) {
                currentRoot = root;
//                Log.d(TAG, "onAccessibilityEvent: currentRoot updated.");
            }
        }
    }

    @Override
    public void onInterrupt() {
        Log.d(TAG, "onInterrupt");
        Toast.makeText(this, getText(R.string.toast_accsrv_on_interrupt), Toast.LENGTH_SHORT).show();
    }

    private void startApp(String packageName) {
        try {
            Intent intent = getPackageManager().getLaunchIntentForPackage(packageName);
            if (intent != null) {
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
            } else {
                Log.w(TAG, "startWechat: getLaunchIntentForPackage failed.");
            }
        } catch (ActivityNotFoundException e) {
            Toast.makeText(getBaseContext(), getText(R.string.toast_accsrv_startApp_failed),
                    Toast.LENGTH_LONG).show();
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private void goForeground() {
        isForeground = true;
        Intent notificationIntent = new Intent(this, MyAccessibilityService.class);
        notificationIntent.setAction(ACTION_CLICK_NOTIFICATION);
        PendingIntent pendingIntent =
                PendingIntent.getService(this, 0, notificationIntent, 0);

        Notification notification =
                new Notification.Builder(this, CHANNEL_ID)
                        .setContentTitle(getText(R.string.notification_warn_title))
                        .setContentText(getText(R.string.notification_warn_message))
                        .setSmallIcon(R.drawable.ic_stat_executing)
                        .setContentIntent(pendingIntent)
                        .setOngoing(true)
                        .build();

// Notification ID cannot be 0.
        startForeground(ONGOING_NOTIFICATION_ID, notification);
    }

    private void cancelForeground() {
        isForeground = false;
        stopForeground(true);
    }

    private boolean openWechatHomeContacts() {
        AccessibilityNodeInfo root = getRoot();
        AccessibilityNodeInfo current = getClickableByText(root, "微信");
        if (current == null) {
            return false;
        }
        current = getClickableByText(root, "发现");
        if (current == null) {
            return false;
        }
        current = getClickableByText(root, "我");
        if (current == null) {
            return false;
        }
        return openNext(root, "通讯录");
    }

    private boolean openNext(String text) {
        Log.d(TAG, "openNext: 开始查找: "+ text);
        AccessibilityNodeInfo root = getRoot();
        AccessibilityNodeInfo current = getClickableByText(root, text);
        if (current == null) {
            return false;
        }
        current.performAction(AccessibilityNodeInfo.ACTION_CLICK);
        return true;
    }

    private boolean openNext(@NonNull AccessibilityNodeInfo root, String text) {
        Log.d(TAG, "openNext: 开始查找: "+ text);
        AccessibilityNodeInfo current = getClickableByText(root, text);
        if (current == null) {
            return false;
        }
        current.performAction(AccessibilityNodeInfo.ACTION_CLICK);
        return true;
    }

    private AccessibilityNodeInfo getRoot() {
        AccessibilityNodeInfo root = getRootInActiveWindow();
        if (root == null) {
            Log.w(TAG, "getRoot: Root window 为空, 使用缓存的root节点");
            root = currentRoot;
        }
        return root;
    }

    private AccessibilityNodeInfo getClickableByText(String text) {
        return getClickableByText(getRoot(), text);
    }

    private boolean clickNode(AccessibilityNodeInfo node) {
        if (!node.isClickable()) {
            return false;
        }
        return node.performAction(AccessibilityNodeInfo.ACTION_CLICK);
    }

    private boolean performAction(AccessibilityNodeInfo node, int action) {
        return node.performAction(action);
    }

    // 如果有多个相同的，则返回最后一个
    @Nullable
    private static AccessibilityNodeInfo getClickableByText(@NonNull AccessibilityNodeInfo root, String text) {
        List<AccessibilityNodeInfo> list = root.findAccessibilityNodeInfosByText(text);
        if (list != null && list.size() > 0) {
//            Log.d(TAG, "openNext: 找到" + list.size() + "个节点");
            AccessibilityNodeInfo current = list.get(list.size() - 1);
//            Log.d(TAG, "openNext: 当前节点名称: " + current.getText());

            while (!current.isClickable()) {
//                Log.d(TAG, "openNext: 查找clickable：" + current.getClassName());
                current = current.getParent();
            }
//            Log.d(TAG, "openNext: 找到clickable：" + current.getClassName());
            return current;
        } else {
            Log.w(TAG, "getClickableByText: 找不到有效的节点");
            return null;
        }
    }

    private void click(int x, int y) {
//        Log.d(TAG, "click: " + String.format("x: %d, y: %d.", x, y));
        Path path = pointsToPath(new int[][]{{x,y}});
        GestureDescription.StrokeDescription stroke = new GestureDescription.StrokeDescription(path, 0, ViewConfiguration.getTapTimeout() + 50);
        GestureDescription.Builder builder = new GestureDescription.Builder();
        builder.addStroke(stroke);
        dispatchGesture(builder.build(),null,null);
    }

    private Path pointsToPath(int[][] points) {
        Path path = new Path();
        path.moveTo(points[0][0], points[0][1]);
        for (int i = 1; i < points.length; i++) {
            int[] point = points[i];
            path.lineTo(point[0], point[1]);
        }
        return path;
    }

    private AccessibilityNodeInfo getScrollableNode() {
        return getScrollableNode(getRoot());
    }

    private AccessibilityNodeInfo getScrollableNode(AccessibilityNodeInfo root) {
        Deque<AccessibilityNodeInfo> deque = new ArrayDeque<>();
        if (root == null) {
            Log.e(TAG, "findScrollableNode: called with null root!!");
            return null;
        }
        deque.add(root);
        while (!deque.isEmpty()) {
            AccessibilityNodeInfo node = deque.removeFirst();
            if (node.getActionList().contains(AccessibilityNodeInfo.AccessibilityAction.ACTION_SCROLL_FORWARD)) {
                return node;
            }
            for (int i = 0; i < node.getChildCount(); i++) {
                deque.addLast(node.getChild(i));
            }
        }
        return null;
    }

    private boolean scrollDown() {
        AccessibilityNodeInfo scrollable = getScrollableNode(getRoot());
        if (scrollable != null) {
            scrollable.performAction(AccessibilityNodeInfo.AccessibilityAction.ACTION_SCROLL_FORWARD.getId());
            return true;
        } else {
            Log.d(TAG, "scrollDown: cannot find scrollable node.");
            return false;
        }
    }

    private void global_action(int action) {
        performGlobalAction(action);
    }

    private void createNotificationChannel() {
        // Create the NotificationChannel, but only on API 26+ because
        // the NotificationChannel class is new and not in the support library
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = getString(R.string.notification_channel_name);
            String description = getString(R.string.notification_channel_description);
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, name, importance);
            channel.setDescription(description);
            // Register the channel with the system; you can't change the importance
            // or other notification behaviors after this
            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }

    // for lua vm
    private void showToast(String s) {
//        Log.d(TAG, "showToast: msg: "+s);
        mainHandler.post(new Runnable() {
            private final String msg;
            {
                this.msg = s;
            }
            @Override
            public void run() {
                Toast.makeText(MyAccessibilityService.this, msg, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void onScriptError(String err) {
        showToast(String.valueOf(getText(R.string.toast_script_error)));
        String log = String.format("--------%s-------\n%s\n", Utils.getTimeStamp(), err);
//        Log.d(TAG, "onScriptError: " + log);
        Utils.saveFile(this, Utils.ERR_LOG_FILENAME, log.getBytes(), true);
    }

    private void onScriptError(Throwable err) {
        showToast(String.valueOf(getText(R.string.toast_script_error)));
        String log = String.format("--------%s-------\n%s\n", Utils.getTimeStamp(), Log.getStackTraceString(err));
//        Log.d(TAG, "onScriptError: " + log);
        Utils.saveFile(this, Utils.ERR_LOG_FILENAME, log.getBytes(), true);
    }

    private void onScriptError(String serr, Throwable err) {
        showToast(String.valueOf(getText(R.string.toast_script_error)));
        String log = String.format("--------%s-------\n%s\n%s\n", Utils.getTimeStamp(), serr, Log.getStackTraceString(err));
//        Log.d(TAG, "onScriptError: " + log);
        Utils.saveFile(this, Utils.ERR_LOG_FILENAME, log.getBytes(), true);
    }

    private void enableTouchExploration() {
        AccessibilityServiceInfo info = getServiceInfo();
        info.flags |= AccessibilityServiceInfo.FLAG_REQUEST_TOUCH_EXPLORATION_MODE;
        setServiceInfo(info);
    }

    private void disableTouchExploration() {
        AccessibilityServiceInfo info = getServiceInfo();
        info.flags &= ~AccessibilityServiceInfo.FLAG_REQUEST_TOUCH_EXPLORATION_MODE;
        setServiceInfo(info);
    }

    public void lua_sleep_ms(int ms) throws InterruptedException {
        if (isRunnerStop) {
            throw new InterruptedException(getString(R.string.exception_interrupt));
        }
        Thread.sleep(ms);
        if (isRunnerStop) {
            throw new InterruptedException(getString(R.string.exception_interrupt));
        }
    }

}


/*

    @Override
    public boolean onGesture (AccessibilityGestureEvent gestureEvent) {
        Log.d(TAG, "onGesture: get event.");
        gestureEvent.getGestureEvents();
        disableTouchExploration();
        return super.onGesture(gestureEvent);
    }


    class ReportRunner implements Runnable {

        void sleep(int ms) {
            try {
                Thread.sleep(ms);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        @Override
        public void run() {
            startApp("com.tencent.mm");
            sleep(3000);
            int tried = 0;
            while (!openWechatHomeContacts()) {
                if (tried >= 4) {
                    Log.d(TAG, "ReportRunner.run: 回到主页似乎失败了");
                    return;
                }
                showToast("不是主页");
                performGlobalAction(GLOBAL_ACTION_BACK);
                sleep(2000);
                tried++;
            }
            sleep(3000);
            openNext("北京邮电大学");
            sleep(3000);
            openNext("疫情防控通");
            sleep(3000);
            openNext("您的今日健康情况还未上报，请及时上报");
            Log.d(TAG, "ReportRunner.run: 等待页面加载");
            showToast("等待页面加载");
            sleep(7000);
            showToast("冲");
            Log.d(TAG, "ReportRunner.run: 冲!");
            AccessibilityNodeInfo scrollable = getScrollableNode(getRootInActiveWindow());
            if (scrollable == null) {
                Log.d(TAG, "ReportRunner.run: cannot find web view to scroll.");
                return;
            }
//            Log.d(TAG, "Scroll: 组件类：" + scrollable.getClassName() + ", id: "+ scrollable.getViewIdResourceName());
//            Log.d(TAG, "Scroll: 组件描述：" + scrollable.getContentDescription());
            for (int i=0;i<1;i++) {
                scrollable.performAction(AccessibilityNodeInfo.AccessibilityAction.ACTION_SCROLL_FORWARD.getId());
                sleep(500);
            }
            click(452,2077);
            sleep(5000);
            click(760, 1297);
            sleep(1000);
            for (int i=0;i<5;i++) {
                scrollable.performAction(AccessibilityNodeInfo.AccessibilityAction.ACTION_SCROLL_FORWARD.getId());
                sleep(500);
            }
            click(530, 1991);
            sleep(1000);
            click(649, 1230);
        }
    }
    private void global_action_back() {
        performGlobalAction(GLOBAL_ACTION_BACK);
    }

    private void global_action_home() {
        performGlobalAction(GLOBAL_ACTION_HOME);
    }

    // 需要安卓9
    @RequiresApi(api = Build.VERSION_CODES.P)
    private void global_action_lock() {
        performGlobalAction(GLOBAL_ACTION_LOCK_SCREEN);
    }

    @RequiresApi(api = Build.VERSION_CODES.P)
    private void global_action_screenshot() {
        performGlobalAction(GLOBAL_ACTION_TAKE_SCREENSHOT);
    }
 */