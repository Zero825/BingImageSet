package com.image.bingimage

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.content.ContextCompat


class SettingsActivity:AppCompatActivity(){



    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)
        setSupportActionBar(findViewById(R.id.activity_settings_toolbar))
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        val intent= intent
        val window = this.window
        val nscolor=intent.getIntExtra("nscolor",ContextCompat.getColor(this,R.color.colorPrimary))
        window.statusBarColor=nscolor
        window.navigationBarColor=nscolor
        val tb1=findViewById<Toolbar>(R.id.activity_settings_toolbar)
        tb1.setBackgroundColor(nscolor)
        supportFragmentManager
            .beginTransaction()
            .replace(R.id.settings_container, MySettingsFragment())
            .commit()
    }




    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return super.onSupportNavigateUp()
    }

}