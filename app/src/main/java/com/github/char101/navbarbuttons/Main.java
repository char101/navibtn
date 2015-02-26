package com.github.char101.navbarbuttons;

import android.content.Context;
import android.content.res.XResources;
import android.content.res.XmlResourceParser;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.widget.ImageView;
import android.widget.LinearLayout;

import org.xmlpull.v1.XmlPullParser;

import de.robv.android.xposed.IXposedHookInitPackageResources;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_InitPackageResources.InitPackageResourcesParam;
import de.robv.android.xposed.callbacks.XC_LayoutInflated;

public class Main implements IXposedHookInitPackageResources {
    @Override
    public void handleInitPackageResources(InitPackageResourcesParam resparam) throws Throwable {
        if (!resparam.packageName.equals("com.android.systemui"))
            return;
        resparam.res.hookLayout("com.android.systemui", "layout", "mid_navigation_bar_port", new XC_LayoutInflated() {
            @Override
            public void handleLayoutInflated(LayoutInflatedParam liparam) throws Throwable {
                Context context = liparam.view.getContext();

                LinearLayout layout = (LinearLayout) liparam.view.findViewById(liparam.res.getIdentifier("mid_nav_buttons", "id", "com.android.systemui"));
                layout.addView(createButton(context, liparam.res, "ic_sysbar_killtask", 26), 0);
                layout.addView(createSeparator(context), 1);
                layout.addView(createSeparator(context));
                layout.addView(createButton(context, liparam.res,  "ic_sysbar_menu", 82));
            }

            private ImageView createButton(Context context, XResources xres, String image, int keyCode) throws Throwable {
                XmlResourceParser parser = xres.getXml(xres.getIdentifier("mid_navigation_bar_port", "layout", "com.android.systemui"));

                int type = 0;
                while (type != XmlPullParser.END_DOCUMENT) {
                    if (type == XmlPullParser.START_TAG && parser.getName() == "View") {
                        break;
                    }
                    type = parser.next();
                }

                ImageView btn = (ImageView) XposedHelpers.findClass("com.android.systemui.statusbar.policy.KeyButtonView", context.getClassLoader()).
                        getConstructor(Context.class, AttributeSet.class).newInstance(context, parser);

                btn.setLayoutParams(
                        new LinearLayout.LayoutParams(
                                xres.getDimensionPixelSize(xres.getIdentifier("navigation_key_width", "dimen", "com.android.systemui")),
                                LayoutParams.FILL_PARENT,
                                0.0f
                        )
                );
                btn.setImageResource(xres.getIdentifier(image, "drawable", "com.android.systemui"));
                if (keyCode == 26)
                    btn.setContentDescription("lock screen");
                XposedHelpers.setIntField(btn, "mCode", keyCode);
                return btn;
            }

            private View createSeparator(Context context) {
                View sep = new View(context);
                sep.setLayoutParams(new LinearLayout.LayoutParams(0, LayoutParams.FILL_PARENT, 1.0f));
                return sep;
            }
        });
    }
}