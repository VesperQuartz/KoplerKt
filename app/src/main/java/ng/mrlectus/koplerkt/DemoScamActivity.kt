package ng.mrlectus.koplerkt

import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class DemoScamActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_demo_scam)

        val showDemoOnly = {
            Toast.makeText(this, R.string.demo_submitted, Toast.LENGTH_SHORT).show()
        }

        findViewById<Button>(R.id.demo_verify).setOnClickListener { showDemoOnly() }
        findViewById<TextView>(R.id.demo_cancel).setOnClickListener { showDemoOnly() }
    }
}
