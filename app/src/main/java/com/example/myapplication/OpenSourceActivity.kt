package com.example.myapplication

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.myapplication.databinding.ActivityOpenSourceBinding

class OpenSourceActivity : AppCompatActivity() {

    private lateinit var binding: ActivityOpenSourceBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityOpenSourceBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)
        supportActionBar?.apply {
            setDisplayHomeAsUpEnabled(true)
            title = "Open Source Libraries"
        }
        binding.toolbar.setNavigationOnClickListener { finish() }

        binding.licensesText.text = """
            This app uses the following open-source libraries:
            
            - Material Components for Android (Apache-2.0)
            - AndroidX Libraries (Apache-2.0)
            - CircleImageView by hdodenhof (Apache-2.0)
            
            Full licenses are included in the source repositories of each library.
        """.trimIndent()
    }
}
