package coddiers.hackyeah.dziki.ui

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.viewpager.widget.ViewPager
import coddiers.hackyeah.dziki.MainActivity
import coddiers.hackyeah.dziki.R
import kotlinx.android.synthetic.main.view_four.*
import kotlinx.android.synthetic.main.view_one.*
import kotlinx.android.synthetic.main.view_tree.*
import kotlinx.android.synthetic.main.view_two.*


class InfoSliderActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.decorView.systemUiVisibility = (View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                or View.SYSTEM_UI_FLAG_FULLSCREEN
                or View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY)

        setContentView(R.layout.activity_info_slider)

        val viewPager = findViewById<View>(R.id.viewpager) as ViewPager
        viewPager.adapter = CustomPagerAdapter(this)


    }

    fun fancyMethod(view: View){
        finish()
    }



    override fun onBackPressed() {
        super.onBackPressed()
        var main = Intent(this, MainActivity::class.java)
        startActivity(main);
    }


}