package com.image.bingimage

import android.app.Application
import android.graphics.Color
import com.billy.android.swipe.SmartSwipeBack




class ThisApp: Application() {
    override fun onCreate() {
        SmartSwipeBack.activitySlidingBack(this,
            { activity ->
                //根据传入的activity，返回true代表需要侧滑返回；false表示不需要侧滑返回
                activity !is MainActivity
            },70,
            Color.TRANSPARENT,
            Color.TRANSPARENT,0,0.25F,0x1)
        super.onCreate()
    }
}