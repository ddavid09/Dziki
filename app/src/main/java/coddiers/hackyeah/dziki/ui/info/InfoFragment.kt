package coddiers.hackyeah.dziki.ui.info

import android.content.Intent
import androidx.lifecycle.ViewModelProvider
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import coddiers.hackyeah.dziki.MainActivity
import coddiers.hackyeah.dziki.R
import coddiers.hackyeah.dziki.ui.InfoSliderActivity
import kotlinx.android.synthetic.main.activity_main.*

class InfoFragment : Fragment() {

    companion object {
        fun newInstance() = InfoFragment()
    }

    private lateinit var viewModel: InfoViewModel

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        MainActivity.btn.visibility = View.GONE;
        return inflater.inflate(R.layout.fragment_info, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel = ViewModelProvider(this).get(InfoViewModel::class.java)

    }

}