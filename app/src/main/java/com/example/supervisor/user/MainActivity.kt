package com.example.supervisor.user

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import com.example.supervisor.R
import com.example.supervisor.admin.AddSampleDataActivity

class MainActivity : AppCompatActivity() {
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        
        val btnViewSupervisors: Button = findViewById(R.id.btnViewSupervisors)
        val btnAddSampleData: Button = findViewById(R.id.btnAddSampleData)
        
        btnViewSupervisors.setOnClickListener {
            val intent = Intent(this, SupervisorListActivity::class.java)
            startActivity(intent)
        }
        
        btnAddSampleData.setOnClickListener {
            val intent = Intent(this, AddSampleDataActivity::class.java)
            startActivity(intent)
        }
    }
}