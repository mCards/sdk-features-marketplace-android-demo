package com.mcards.sdk.fm.demo

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil.setContentView
import com.mcards.sdk.auth.AuthSdkProvider

//TODO add your auth0 aud gotten from the mCards team
private const val AUTH0_AUD = ""

//TODO add your auth0 client ID gotten from the mCards team
private const val AUTH0_CLIENT_ID = ""

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)

        // if login appears to succeed, but the 2step screen spins indefinitely without
        // redirecting to your app, it means the auth0 scheme is incorrect.
        AuthSdkProvider.getInstance().init(getString(R.string.auth0_domain),
            AUTH0_CLIENT_ID,
            AUTH0_AUD,
            getString(R.string.auth0_scheme))
    }
}
