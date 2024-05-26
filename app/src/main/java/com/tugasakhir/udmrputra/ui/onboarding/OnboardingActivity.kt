package com.tugasakhir.udmrputra.ui.onboarding

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.viewpager.widget.ViewPager
import com.tugasakhir.udmrputra.ui.main.MainActivity
import com.tugasakhir.udmrputra.R
import com.tugasakhir.udmrputra.ViewPagerAdapter
import com.tugasakhir.udmrputra.databinding.ActivityOnboardingBinding
import com.tugasakhir.udmrputra.ui.Home
import com.tugasakhir.udmrputra.ui.logreg.LoginActivity

class OnboardingActivity : AppCompatActivity() {

    private lateinit var dots: Array<TextView>
    private lateinit var viewPagerAdapter: ViewPagerAdapter
    private lateinit var binding: ActivityOnboardingBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityOnboardingBinding.inflate(layoutInflater)
        setContentView(binding.root)
        val isFirstInstall =
            getSharedPreferences("PREFS", MODE_PRIVATE).getBoolean("isFirstInstall", true)

        if (isFirstInstall) {
            setupFirstInstall()
        } else {
            setupFirstInstall()
        }
    }
    private fun setupFirstInstall() {
//        binding.btnBack.setOnClickListener {
//            if (getitem(0) > 0) {
//                binding.viewPager.setCurrentItem(getitem(-1), true)
//                binding.btnNext.text = getString(R.string.next_btn)
//            }
//        }

        binding.btnNext.setOnClickListener {

            if (getitem(0) == 1) {
                binding.btnNext.text = getString(R.string.menu)
            }
            if (getitem(0) < 1) {
                binding.btnSkip.visibility = View.INVISIBLE
            }
            if (getitem(0) < 2) {
                binding.viewPager.setCurrentItem(getitem(1), true)
            } else {
                val i = Intent(this@OnboardingActivity, LoginActivity::class.java)
                startActivity(i)
                finish()
                getSharedPreferences("PREFS", MODE_PRIVATE).edit()
                    .putBoolean("isFirstInstall", false).apply()
            }
        }



        binding.btnSkip.setOnClickListener {
            val i = Intent(this@OnboardingActivity, Home::class.java)
            startActivity(i)
            finish()
            getSharedPreferences("PREFS", MODE_PRIVATE).edit().putBoolean("isFirstInstall", false)
                .apply()
        }

        viewPagerAdapter = ViewPagerAdapter(this)
        binding.viewPager.adapter = viewPagerAdapter

        setUpIndicator(0)
        binding.viewPager.addOnPageChangeListener(viewListener)
    }

    private fun proceedToNextActivity() {
        val i = Intent(this@OnboardingActivity, Home::class.java)
        startActivity(i)
        finish()
    }

    private fun setUpIndicator(position: Int) {
        dots = Array(3) { TextView(this) }
        binding.indicator.removeAllViews()

        for (i in 0 until 3) {
            dots[i] = TextView(this)
            dots[i].text = "• "
            dots[i].textSize = 35f
            dots[i].setTextColor(
                ContextCompat.getColor(
                    this,
                    R.color.md_theme_dark_onPrimaryContainer
                )
            )
            binding.indicator.addView(dots[i])
        }

        if (dots.isNotEmpty()) {
            dots[position].setTextColor(ContextCompat.getColor(this, R.color.black))
        }
    }

    private val viewListener = object : ViewPager.OnPageChangeListener {
        override fun onPageScrolled(
            position: Int,
            positionOffset: Float,
            positionOffsetPixels: Int
        ) {

        }

        override fun onPageSelected(position: Int) {
            setUpIndicator(position)
            binding.btnSkip.visibility = if (position > 0) View.INVISIBLE else View.VISIBLE
            binding.btnNext.text =
                if (position == 2) getString(R.string.menu) else getString(R.string.next_btn)
        }

        override fun onPageScrollStateChanged(state: Int) {
        }

    }

    private fun getitem(i: Int): Int {
        return binding.viewPager.currentItem + i
    }
}