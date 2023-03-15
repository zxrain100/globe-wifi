package com.gbw.wifi

import android.animation.ValueAnimator
import android.content.Intent
import android.os.Bundle
import android.view.KeyEvent
import com.gbw.wifi.databinding.ActivityAminBinding
import kotlinx.coroutines.launch

class AminActivity : BaseActivity() {

    private var anim: ValueAnimator? = null
    private var type = 0

    private lateinit var binding: ActivityAminBinding
    private val netTestHelper = NetTestHelper()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAminBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.statusBar.setStatusBar()

        type = intent.getIntExtra("type", 0)

        if (type == 0) {
            binding.midImg.setImageResource(R.mipmap.ic_anim_security)
            binding.tvDesc.text = "Testing your phone security"
            binding.tvTitle.text = "Safety Test…"
        } else if (type == 1) {
            binding.midImg.setImageResource(R.mipmap.ic_anim_net)
            binding.tvDesc.text = "Testing your phone Network"
            binding.tvTitle.text = "Network Test…"
        }

        startCleanAmin()
        netTestHelper.setCallback {
            goResult()
        }
        launch {
            netTestHelper.start()
        }

    }

    private fun startCleanAmin() {
        anim = ValueAnimator.ofFloat(0f, 360f)
        anim?.duration = 1500L
        anim?.repeatCount = -1
        anim?.addUpdateListener {
            binding.aminImg.rotation = it.animatedValue as Float
        }
        anim?.start()
    }

    private fun goResult() {
        if (type == 0) {
            val intent = Intent(this, SecurityActivity::class.java)
            startActivity(intent)
        } else {
            val intent = Intent(this, NetworkActivity::class.java)
            startActivity(intent)
        }

        finish()
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            return true
        }
        return super.onKeyDown(keyCode, event)
    }

    override fun onDestroy() {
        super.onDestroy()
        anim?.cancel()
    }
}