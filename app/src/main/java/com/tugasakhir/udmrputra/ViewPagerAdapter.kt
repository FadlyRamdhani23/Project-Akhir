package com.tugasakhir.udmrputra

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.viewpager.widget.PagerAdapter
import com.tugasakhir.udmrputra.databinding.SlideLayoutBinding


class ViewPagerAdapter(private val context: Context) : PagerAdapter() {
    private val images = intArrayOf(
        R.drawable.onboarding_persediaan,
        R.drawable.onboarding_tracking_pengiriman,
        R.drawable.ic_launcher_background
    )

    private val descriptions = intArrayOf(
        R.string.slide_persediaan,
        R.string.slide_tracking,
        R.string.slide_persediaan
    )

    override fun getCount(): Int {
        return descriptions.size
    }

    override fun isViewFromObject(view: View, `object`: Any): Boolean {
        return view == `object` as LinearLayout
    }

    override fun instantiateItem(container: ViewGroup, position: Int): Any {
        val layoutInflater = LayoutInflater.from(context)
        val binding = SlideLayoutBinding.inflate(layoutInflater, container, false)
        val view = binding.root

        val slideTitleImage = binding.imageView
        val slideDescription = binding.description

        slideTitleImage.setImageResource(images[position])
        slideDescription.setText(descriptions[position])
        container.addView(view)

        return view
    }

    override fun destroyItem(container: ViewGroup, position: Int, `object`: Any) {
        container.removeView(`object` as LinearLayout)
    }
}
