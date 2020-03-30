package com.dean.mplayer.util;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.drawable.Drawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Build;
import android.provider.Settings;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.util.Size;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Toast;

import androidx.appcompat.widget.AppCompatEditText;
import androidx.core.content.ContextCompat;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.text.DecimalFormat;
import java.util.Objects;

public class Utils {

    private Utils() {

    }

    public static boolean isNotLocationInfoPermitted(Context context) {
        return ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED;
    }

    public static void openLocationSetting(Context context) {
        Intent intent = new Intent();
        intent.setAction(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
        context.startActivity(intent);
    }

    public static void openAuthoritySetting(Context context) {
        Intent intent = new Intent();
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.setAction("android.settings.APPLICATION_DETAILS_SETTINGS");
        intent.setData(Uri.fromParts("package", context.getPackageName(), null));
        context.startActivity(intent);
    }

    public static void openNotificationSetting(Context context) {
        Intent intent = new Intent();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            intent.setAction("android.settings.APP_NOTIFICATION_SETTINGS");
            intent.putExtra("android.provider.extra.APP_PACKAGE", context.getPackageName());
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            intent.setAction("android.settings.APP_NOTIFICATION_SETTINGS");
            intent.putExtra("app_package", context.getPackageName());
            intent.putExtra("app_uid", context.getApplicationInfo().uid);
        } else {
            intent.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
            intent.addCategory(Intent.CATEGORY_DEFAULT);
            intent.setData(Uri.parse("package:" + context.getPackageName()));
        }
        context.startActivity(intent);
    }

    public static boolean isNotNetworkAvailable(Context context) {
        ConnectivityManager connectivity = (ConnectivityManager) context
                .getSystemService(Context.CONNECTIVITY_SERVICE);
        if (connectivity != null) {
            NetworkInfo info = connectivity.getActiveNetworkInfo();
            if (info != null && info.isConnected()) {
                return info.getState() != NetworkInfo.State.CONNECTED;
            }
        }
        return true;
    }

    public static void checkNetworkError(Context context) {
        if (isNotNetworkAvailable(context)) {
            Toast.makeText(context, "网络错误", Toast.LENGTH_SHORT).show();
        }
    }

    public static boolean isUrlNeedOpenTel(String url) {
        return url.startsWith("tel:");
    }

    public static String addSpaceByCredit(String content) {
        if (TextUtils.isEmpty(content)) {
            return "";
        }
        content = content.replaceAll(" ", "");
        if (TextUtils.isEmpty(content)) {
            return "";
        }
        StringBuilder newString = new StringBuilder();
        for (int i = 1; i <= content.length(); i++) {
            if (i % 4 == 0 && i != content.length()) {
                newString.append(content.charAt(i - 1)).append(" ");
            } else {
                newString.append(content.charAt(i - 1));
            }
        }
        return newString.toString();
    }

    public static String getEditTextTrim(AppCompatEditText editText) {
        return getEditText(editText).replaceAll(" ", "");
    }

    public static String getEditText(AppCompatEditText editText) {
        return Objects.requireNonNull(editText.getText()).toString();
    }

    public static boolean isCreditNumber(String idCard) {
        return !TextUtils.isEmpty(idCard) && idCard.matches("^\\d{16}$");
    }

    public static String toSecretByCredit(String content) {
        content = content.replaceAll(" ", "");
        if (!isCreditNumber(content)) {
            return "";
        }

        StringBuilder newString = new StringBuilder();
        for (int i = 1; i <= content.length(); i++) {
            if (i <= 12) {
                newString.append("*");
            } else {
                newString.append(content.charAt(i - 1));
            }
        }

        content = addSpaceByCredit(newString.toString());
        if (TextUtils.isEmpty(content)) {
            return "";
        }
        return content;
    }

    public static int dp2px(Context context, float dpValue) {
        final float scale = context.getResources().getDisplayMetrics().density;
        return (int) (dpValue * scale + 0.5f);
    }

    public static int px2dp(Context context,float pxValue){
        final float scale = context.getResources ().getDisplayMetrics ().density;
        return (int) (pxValue / scale + 0.5f);
    }

    public static Size screenSize(Context context) {
        DisplayMetrics displayMetrics = new DisplayMetrics();
        ((Activity) context).getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        int width = displayMetrics.widthPixels;
        int height = displayMetrics.heightPixels;
        return new Size(width, height);
    }

    private static String MD5String(String dataStr) {
        try {
            MessageDigest m = MessageDigest.getInstance("MD5");
            m.update(dataStr.getBytes(StandardCharsets.UTF_8));
            byte[] bytes = m.digest();
            StringBuilder result = new StringBuilder();
            for (byte b : bytes) {
                result.append(Integer.toHexString((0x000000FF & b) | 0xFFFFFF00).substring(6));
            }
            return result.toString();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "";
    }

    public static String decimalPoint(String pointStr) {
        long point = Long.valueOf(pointStr);
        return new DecimalFormat("#,###").format(point);
    }

    public static void setWindowBrightness(Activity mActivity, float brightness) {
        Window window = mActivity.getWindow();
        WindowManager.LayoutParams lp = window.getAttributes();
        lp.screenBrightness = brightness;
        window.setAttributes(lp);
    }

    public static Bitmap vector2Bitmap(Context context, int vectorRes, int color) {
        Drawable vectorDrawable = context.getDrawable(vectorRes);
        if (vectorDrawable != null) {
            vectorDrawable.setTint(context.getResources().getColor(color));
            int h = vectorDrawable.getIntrinsicHeight() * 2;
            int w = vectorDrawable.getIntrinsicWidth() * 2;
            vectorDrawable.setBounds(0, 0, w, h);
            Bitmap bm = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
            Canvas canvas = new Canvas(bm);
            canvas.drawBitmap(bm , 0, 0, new Paint());
            vectorDrawable.draw(canvas);
            return bm;
        }
        return null;
    }

    public static Bitmap drawable2Bitmap(Drawable drawable) {
        int h = drawable.getIntrinsicHeight() * 2;
        int w = drawable.getIntrinsicWidth() * 2;
        drawable.setBounds(0, 0, w, h);
        Bitmap bm = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bm);
        canvas.drawBitmap(bm , 0, 0, new Paint());
        drawable.draw(canvas);
        return bm;
    }

}
