package com.arun.xposed.eui_moder;

/**
 /* Created by arun on 7/9/16- 24/7/16.
 */

import android.app.Activity;
import android.content.Context;
import android.content.res.XResources;
import android.graphics.Color;
import android.util.TypedValue;
import android.view.WindowManager;

import de.robv.android.xposed.IXposedHookInitPackageResources;
import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.IXposedHookZygoteInit;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XC_MethodReplacement;
import de.robv.android.xposed.XSharedPreferences;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.callbacks.XC_InitPackageResources;
import de.robv.android.xposed.callbacks.XC_LoadPackage.LoadPackageParam;

import static de.robv.android.xposed.XposedHelpers.findAndHookMethod;
import static de.robv.android.xposed.XposedHelpers.findClass;
import static de.robv.android.xposed.XposedHelpers.getStaticObjectField;
import static de.robv.android.xposed.XposedHelpers.setStaticObjectField;

public class MainClass implements IXposedHookLoadPackage,IXposedHookInitPackageResources, IXposedHookZygoteInit {

    public static boolean DISABLE_EUI = true;
    public static boolean DISABLE_CC = true;
    public static boolean DARK_MATERIAL_COLOR = true;
    public static boolean SEMI_TRAN_STATUS = true;

    public void handleLoadPackage(final LoadPackageParam lpparam) throws Throwable {

        if (lpparam.packageName.equals("com.android.systemui")) {

            try {

                /*findAndHookMethod("com.android.systemui.statusbar.BaseStatusBar",lpparam.classLoader,"isNotificationHighPriorityLeui",String.class,new XC_MethodHook() {
                    @Override
                    protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                        param.setResult(true);
                    }
                });*/

/*
                //Enable notification access when app locked
                Class<?> BSBClass = findClass("com.android.systemui.statusbar.BaseStatusBar", lpparam.classLoader);
                Boolean notif_quick = (Boolean) getStaticObjectField(BSBClass, "LEUI_ACCESS_CONTROL");
                setStaticObjectField(BSBClass, "LEUI_ACCESS_CONTROL",!notif_quick);
*/

/*                findAndHookMethod("com.android.systemui.statusbar.phone.PhoneStatusBar", lpparam.classLoader, "makeStatusBarView", new XC_MethodHook() {
                    @Override
                    protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
//                        Class<?> BSBClass = findClass("com.android.systemui.statusbar.BaseStatusBar", lpparam.classLoader);
//                        setStaticObjectField(lpparam.getClass().getSuperclass(), "LEUI_ENABLE", false);
                        setObjectField(param.thisObject,"LEUI_ENABLE",false);
                    }

                    @Override
                    protected void afterHookedMethod(MethodHookParam param) throws Throwable {
//                        setStaticObjectField(lpparam.getClass().getSuperclass(), "LEUI_ENABLE", true);
                        setObjectField(param.thisObject,"LEUI_ENABLE",true);
                    }
                });*/

                //XposedBridge.log("Updating LEUI");
                loadPrefs();

                if (DISABLE_EUI) {
                    /* Disable LEUI Flag */
                    Class<?> KGClass = findClass("com.android.keyguard.KeyguardUpdateMonitor", lpparam.classLoader);
                    setStaticObjectField(KGClass, "LEUI_ENABLE", false);

                    findAndHookMethod("com.leui.keyguard.LeUiUtils",lpparam.classLoader,"addFingerprintUnlockFailedCount", Context.class,new XC_MethodReplacement() {
                        @Override
                        protected Object replaceHookedMethod(MethodHookParam methodHookParam) throws Throwable {
                            return null;
                        }
                    });
                }

                if (DISABLE_CC) {
                    /* Disable EUI recent switcher */
                    Class<?> RecentsClass = findClass("com.android.systemui.recents.Recents", lpparam.classLoader);
                    Class<?> RecentsEnum = findClass("com.android.systemui.recents.Recents$RECENTS_MODE", lpparam.classLoader);
                    Object loli = getStaticObjectField(RecentsEnum, "LOLIPOP");
                    setStaticObjectField(RecentsClass, "mSelectedUiMode", loli);
                }

            } catch (Exception e){
                XposedBridge.log("Something went wrong" + e.toString());
            }
/*
            if(SEMI_TRAN_STATUS) {
                findAndHookMethod("com.android.systemui.statusbar.phone.PhoneStatusBar", lpparam.classLoader, "makeStatusBarView", new XC_MethodHook() {
                    @Override
                    protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                        int mState = XposedHelpers.getIntField(param.thisObject, "mState");
                        View v = (View) XposedHelpers.getObjectField(param.thisObject, "mStatusBarView");
                        View kv = (View) XposedHelpers.getObjectField(param.thisObject, "mKeyguardStatusView");

                        if(mState == 0) {
                            // Normal mode
                            v.setBackgroundColor(Color.parseColor("#60000000"));
                            kv.setBackgroundColor(Color.parseColor("#00000000"));
                        } else if (mState == 1) {
                            // Keyguard Mode
                            v.setBackgroundColor(Color.parseColor("#00000000"));
                        }
                    }
                });
            }
*/

        }
        else if (SEMI_TRAN_STATUS && (lpparam.packageName.startsWith("com.letv") || lpparam.packageName.startsWith("com.android"))) {
            findAndHookMethod(Activity.class,"onStart",new XC_MethodHook() {
                @Override
                protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                    Activity a = (Activity) param.thisObject;
//                    Toast.makeText(a,"Activity created",Toast.LENGTH_SHORT).show();
                    try {
                        a.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
                        a.getWindow().addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);

                        a.getWindow().setStatusBarColor(Color.parseColor("#C0000000")); // Semi transparent
                    } catch (Throwable e) {
//                        Toast.makeText(a,"Flag setting failed",Toast.LENGTH_SHORT).show();
                    }
                }
            });

            findAndHookMethod(Activity.class,"onResume",new XC_MethodHook() {
                @Override
                protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                    Activity a = (Activity) param.thisObject;
//                    Toast.makeText(a,"Activity resumed",Toast.LENGTH_SHORT).show();
                    a.getWindow().setStatusBarColor(Color.parseColor("#C0000000")); // Semi transparent
                }
            });
        }

    }

    @Override
    public void handleInitPackageResources(XC_InitPackageResources.InitPackageResourcesParam resparam) throws Throwable {
        // replacements only for SystemUI
        if (resparam.packageName.equals("com.android.systemui")) {
            loadPrefs();
            if (DARK_MATERIAL_COLOR) {
                // Set dark material background for notification (color taken from ASOP)
                resparam.res.setReplacement("com.android.systemui", "color", "notification_material_background_color", Color.parseColor("#ff303030"));
            }

            if (DISABLE_EUI) {
                resparam.res.setReplacement("com.android.systemui", "dimen", "status_bar_icon_padding", new XResources.DimensionReplacement(0, TypedValue.COMPLEX_UNIT_DIP));
                resparam.res.setReplacement("com.android.systemui", "dimen", "status_bar_icon_drawing_size", new XResources.DimensionReplacement(12, TypedValue.COMPLEX_UNIT_DIP));
                resparam.res.setReplacement("com.android.systemui", "dimen", "status_bar_icon_width", new XResources.DimensionReplacement(0, TypedValue.COMPLEX_UNIT_DIP));
            }


    /*
            // White Notification bar from ASOP (Theme should be material light to work)
            resparam.res.setReplacement("com.android.systemui", "color", "notification_material_background_color", Color.parseColor("#fffafafa"));
            resparam.res.setReplacement("com.android.systemui", "color", "notification_material_background_dimmed_color", Color.parseColor("#d4ffffff"));
            resparam.res.setReplacement("com.android.systemui", "color", "notification_material_background_low_priority_color", Color.parseColor("#ffdcdcdc"));
            resparam.res.setReplacement("com.android.systemui", "color", "notification_ripple_untinted_color", Color.parseColor("#20000000"));
            resparam.res.setReplacement("com.android.systemui", "color", "notification_ripple_tinted_color", Color.parseColor("#30ffffff"));
    */

    /*
            // Trial and error, didn't work as expected
            resparam.res.setReplacement("com.android.systemui", "color", "notification_guts_bg_color", Color.parseColor("#1a000000"));
            resparam.res.setReplacement("com.android.systemui", "color", "notification_guts_title_color", Color.parseColor("#ff000000"));
            resparam.res.setReplacement("com.android.systemui", "color", "notification_guts_text_color", Color.parseColor("#b2000000"));
            resparam.res.setReplacement("com.android.systemui", "color", "notification_guts_btn_color", Color.parseColor("#ff000000"));
    */
        }
       /* else if(resparam.packageName.equals("com.android.settings")){
            XposedBridge.log("Inside settings");
//            resparam.res.setReplacement("com.android.settings", "color", "leui_status_bar_background", Color.parseColor("#ff000000"));
//            resparam.res.setReplacement("com.android.settings", "color", "status_bar", Color.parseColor("#ffff0000"));
//            resparam.res.setReplacement("com.android.settings", "color", "leui_activity_background", Color.parseColor("#ff00ff00"));
        }*/
    }

    @Override
    public void initZygote(IXposedHookZygoteInit.StartupParam startupParam) throws Throwable {

        loadPrefs();
/*
        // Tried changing dark theme as white, didn't work
        XResources.setSystemWideReplacement("android", "color", "primary_text_default_material_dark", Color.parseColor("#de000000"));
        XResources.setSystemWideReplacement("android", "color", "secondary_text_default_material_dark", Color.parseColor("#8a000000"));
*/
        if(DISABLE_EUI) {
            XResources.setSystemWideReplacement("android", "dimen", "status_bar_icon_size", new XResources.DimensionReplacement(20, TypedValue.COMPLEX_UNIT_DIP));
        }

    }

    private void loadPrefs(){

        try {
            XSharedPreferences prefs = new XSharedPreferences("com.arun.xposed.eui_moder");//,"com.arun.xposed.eui_moder_preferences");
//            XposedBridge.log("PrefFile : " + prefs.getFile().getPath());

            if(prefs.getFile() != null) {
                prefs.reload();

/*
                Map<String, ?> m = prefs.getAll();
                for (Map.Entry<String, ?> entry : m.entrySet()) {
                    XposedBridge.log("EUIMOD :" + entry.getKey());
                }
*/

                DISABLE_EUI = prefs.getBoolean("disable_eui", true);
                DISABLE_CC = prefs.getBoolean("disable_cc", false);
                DARK_MATERIAL_COLOR = !prefs.getBoolean("enable_transparent_notif", false);
                SEMI_TRAN_STATUS = prefs.getBoolean("enable_statusbar_tint", true);

//                XposedBridge.log("EUI Moder read " + DISABLE_EUI + " " + DISABLE_CC + " " + DARK_MATERIAL_COLOR);
            } else {
                XposedBridge.log("EUIMOD: Null prefs file!");
            }


        }
        catch(Throwable e) {
            XposedBridge.log("XSharedPrefs : "+e);
        }
    }
}
