package com.image.bingimage


import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter

class ImagePagerAdapter(fa:FragmentActivity,num:Int): FragmentStateAdapter(fa) {


    private var num = 0
    init {
        this.num=num
    }
    override fun getItemCount(): Int {

        return num
    }

    override fun createFragment(position: Int): Fragment {
        return ImagePageFragment().newInstance(position)
    }
}