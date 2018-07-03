package com.hydroh.yamibo.ui

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.annotation.TargetApi
import android.content.ContentValues.TAG
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.text.TextUtils
import android.util.Log
import android.view.MenuItem
import android.view.View
import android.view.inputmethod.EditorInfo
import android.widget.*
import com.hydroh.yamibo.R
import com.hydroh.yamibo.network.WebRequest
import com.hydroh.yamibo.network.callback.CookieCallbackListener
import com.hydroh.yamibo.util.PrefUtils

class LoginActivity : AppCompatActivity() {

    private val mUsernameView by lazy { findViewById<EditText>(R.id.username) }
    private val mPasswordView by lazy { findViewById<EditText>(R.id.password) }
    private val mProgressView by lazy { findViewById<ProgressBar>(R.id.login_progress) }
    private val mLoginFormView by lazy { findViewById<ScrollView>(R.id.login_form) }
    private val mErrorHintView by lazy { findViewById<TextView>(R.id.error_hint_text) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)
        actionBar?.setDisplayHomeAsUpEnabled(true)

        mPasswordView.setOnEditorActionListener(TextView.OnEditorActionListener { textView, id, keyEvent ->
            if (id == EditorInfo.IME_ACTION_DONE || id == EditorInfo.IME_NULL) {
                attemptLogin()
                return@OnEditorActionListener true
            }
            false
        })

        val mUsernameSignInButton = findViewById<View>(R.id.sign_in_button) as Button
        mUsernameSignInButton.setOnClickListener { attemptLogin() }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> {
                onBackPressed()
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }

    /**
     * Attempts to sign in or register the account specified by the login form.
     * If there are form errors (invalid email, missing fields, etc.), the
     * errors are presented and no actual login attempt is made.
     */
    private fun attemptLogin() {
        // Reset errors.
        mUsernameView.error = null
        mPasswordView.error = null

        // Store values at the time of the login attempt.
        val username = mUsernameView.text.toString()
        val password = mPasswordView.text.toString()

        var cancel = false
        var focusView: View? = null

        // Check for a valid password, if the user entered one.
        if (TextUtils.isEmpty(password)) {
            mPasswordView.error = getString(R.string.error_password_required)
            focusView = mPasswordView
            cancel = true
        }

        // Check for a valid username.
        if (TextUtils.isEmpty(username)) {
            mUsernameView.error = getString(R.string.error_username_required)
            focusView = mUsernameView
            cancel = true
        }

        if (cancel) {
            // There was an error; don't attempt login and focus the first
            // form field with an error.
            focusView!!.requestFocus()
        } else {
            // Show a progress spinner, and kick off a background task to
            // perform the user login attempt.
            showProgress(true)
            mErrorHintView.visibility = View.GONE
            WebRequest.getLogonCookies(username, password, this, object : CookieCallbackListener {
                override fun onFinish(cookies: MutableMap<String, String>) {
                    runOnUiThread {
                        showProgress(false)
                        Toast.makeText(mUsernameView!!.context, R.string.login_success, Toast.LENGTH_SHORT).show()
                    }
                    PrefUtils.setCookiePreference(this@LoginActivity, cookies)
                    Log.d(TAG, "onFinish: Login Success!")
                    val intent = Intent("com.hydroh.yamibo.REFRESH")
                    sendBroadcast(intent)
                    finish()
                }

                override fun onError(e: Exception) {
                    runOnUiThread {
                        showProgress(false)
                        mErrorHintView.text = e.message ?: "未知错误"
                        mErrorHintView.visibility = View.VISIBLE
                    }
                    Log.d(TAG, "onError: Login Failed!")
                }
            })
        }
    }

    /**
     * Shows the progress UI and hides the login form.
     */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
    private fun showProgress(show: Boolean) {
        // On Honeycomb MR2 we have the ViewPropertyAnimator APIs, which allow
        // for very easy animations. If available, use these APIs to fade-in
        // the progress spinner.
        val shortAnimTime = resources.getInteger(android.R.integer.config_shortAnimTime)

        mLoginFormView.visibility = if (show) View.GONE else View.VISIBLE
        mLoginFormView.animate().setDuration(shortAnimTime.toLong()).alpha(
                (if (show) 0 else 1).toFloat()).setListener(object : AnimatorListenerAdapter() {
            override fun onAnimationEnd(animation: Animator) {
                mLoginFormView.visibility = if (show) View.GONE else View.VISIBLE
            }
        })

        mProgressView.visibility = if (show) View.VISIBLE else View.GONE
        mProgressView.animate().setDuration(shortAnimTime.toLong()).alpha(
                (if (show) 1 else 0).toFloat()).setListener(object : AnimatorListenerAdapter() {
            override fun onAnimationEnd(animation: Animator) {
                mProgressView.visibility = if (show) View.VISIBLE else View.GONE
            }
        })
    }

}

