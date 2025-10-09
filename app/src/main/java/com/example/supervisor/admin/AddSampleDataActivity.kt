package com.example.supervisor.admin

import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.supervisor.R
import com.example.supervisor.utils.SampleDataGenerator

class AddSampleDataActivity : AppCompatActivity() {
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_sample_data)
        
        val addSupervisorsButton: Button = findViewById(R.id.btnAddSupervisors)
        
        addSupervisorsButton.setOnClickListener {
            SampleDataGenerator.addSampleSupervisors()
            Toast.makeText(this, "Sample supervisors are being added to the database", Toast.LENGTH_LONG).show()
        }
    }
}