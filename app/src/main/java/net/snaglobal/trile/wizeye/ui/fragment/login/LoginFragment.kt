package net.snaglobal.trile.wizeye.ui.fragment.login

import android.arch.lifecycle.MutableLiveData
import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProviders
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v4.content.ContextCompat
import android.support.v4.content.res.ResourcesCompat
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.widget.EditText
import androidx.navigation.fragment.findNavController
import kotlinx.android.synthetic.main.fragment_login.*
import net.snaglobal.trile.wizeye.R
import net.snaglobal.trile.wizeye.data.remote.model.LoginResponse
import net.snaglobal.trile.wizeye.data.remote.retrofit.RetrofitClient
import net.snaglobal.trile.wizeye.data.remote.websocket.WebSocketClient
import net.snaglobal.trile.wizeye.ui.MainActivityViewModel
import net.snaglobal.trile.wizeye.utils.KeyboardHelper
import net.snaglobal.trile.wizeye.utils.observeOnce

/**
 * @author trile
 * @since Sep 27, 2018 at 4:45 PM
 *
 * A simple [Fragment] subclass.
 * Use the [LoginFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class LoginFragment : Fragment() {

    private val mainActivityViewModel by lazy {
        ViewModelProviders.of(activity!!).get(MainActivityViewModel::class.java)
    }

    private val loginViewModel by lazy {
        ViewModelProviders.of(this).get(LoginViewModel::class.java)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_login, container, false)

        mainActivityViewModel.isToolbarVisible.value = false

        mainActivityViewModel.logOut().observeOnce(activity!!, Observer {
            it?.also { isLoggedOut ->
                if (isLoggedOut) {
                    RetrofitClient.resetInstance()  // Important -> To use another Server URL
                    WebSocketClient.resetInstance() // Important -> To use another Server URL
                } else {
                    mainActivityViewModel.logOut()  // Try again
                }
            }
        })

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Remove auto-focus from all EditTexts
        main_container.requestFocus()

        initInputIconAndTextColor()

        // Check if all AutoCompleteTextViews are filled with credential info on Application Opening
        toggleLoginButtonEnabling(
                server_address_input.text.toString(),
                id_input.text.toString(),
                password_input.text.toString()
        )

        server_address_input.afterTextChanged {
            if (it.isEmpty()) {
                server_address_icon.setColorFilter(
                        ResourcesCompat.getColor(resources, R.color.white_60_percent, null)
                )
            } else {
                server_address_icon.setColorFilter(
                        ResourcesCompat.getColor(resources, android.R.color.white, null)
                )
            }
            toggleLoginButtonEnabling(it, id_input.text.toString(), password_input.text.toString())
        }

        id_input.afterTextChanged {
            if (it.isEmpty()) {
                id_icon.setColorFilter(
                        ResourcesCompat.getColor(resources, R.color.white_60_percent, null)
                )
            } else {
                id_icon.setColorFilter(
                        ResourcesCompat.getColor(resources, android.R.color.white, null)
                )
            }
            toggleLoginButtonEnabling(server_address_input.text.toString(), it, password_input.text.toString())
        }

        password_input.afterTextChanged {
            if (it.isEmpty()) {
                password_icon.setColorFilter(
                        ResourcesCompat.getColor(resources, R.color.white_60_percent, null)
                )
            } else {
                password_icon.setColorFilter(
                        ResourcesCompat.getColor(resources, android.R.color.white, null)
                )
            }
            toggleLoginButtonEnabling(server_address_input.text.toString(), id_input.text.toString(), it)
        }
        password_input.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                KeyboardHelper.hideSoftKeyboard(activity, true)
                login_button.callOnClick()
                return@setOnEditorActionListener true
            }
            return@setOnEditorActionListener false
        }

        login_button.setOnClickListener {
            if (login_button.isEnabled) {
                KeyboardHelper.hideSoftKeyboard(activity, true)
                attempLogin()
            }
        }

        loginViewModel.loginSuccessful.observe(this, Observer { loginResponse: LoginResponse? ->
            (mainActivityViewModel.loginSuccessfulNotifier as MutableLiveData).value = loginResponse

            loginResponse?.let {
                Log.d("API", "Token: ${it.token}")
            }
            enableLoginButton()
            findNavController().navigate(R.id.action_loginFragment_to_mainFragment)
        })

        loginViewModel.loginError.observe(this, Observer { throwable: Throwable? ->
            // [resetInstance] is Important -> To be able to Log In Again with another Server URL
            RetrofitClient.resetInstance()

            error_message.visibility = View.VISIBLE
            enableLoginButton()

            throwable?.printStackTrace()
        })
    }

    private fun initInputIconAndTextColor() {
        server_address_icon.setColorFilter(
                ResourcesCompat.getColor(resources, R.color.white_60_percent, null)
        )
        id_icon.setColorFilter(
                ResourcesCompat.getColor(resources, R.color.white_60_percent, null)
        )
        password_icon.setColorFilter(
                ResourcesCompat.getColor(resources, R.color.white_60_percent, null)
        )

        server_address_input.setOnFocusChangeListener { _, hasFocus ->
            if (hasFocus) {
                server_address_input_container.background = ContextCompat.getDrawable(
                        activity!!,
                        R.drawable.login_input_background_focused
                )
            } else {
                server_address_input_container.background = ContextCompat.getDrawable(
                        activity!!,
                        R.drawable.login_input_background_unfocused
                )
            }
        }
        id_input.setOnFocusChangeListener { _, hasFocus ->
            if (hasFocus) {
                id_input_container.background = ContextCompat.getDrawable(
                        activity!!,
                        R.drawable.login_input_background_focused
                )
            } else {
                id_input_container.background = ContextCompat.getDrawable(
                        activity!!,
                        R.drawable.login_input_background_unfocused
                )
            }
        }
        password_input.setOnFocusChangeListener { _, hasFocus ->
            if (hasFocus) {
                password_input_container.background = ContextCompat.getDrawable(
                        activity!!,
                        R.drawable.login_input_background_focused
                )
            } else {
                password_input_container.background = ContextCompat.getDrawable(
                        activity!!,
                        R.drawable.login_input_background_unfocused
                )
            }
        }
    }

    private fun toggleLoginButtonEnabling(serverAddress: String, id: String, password: String) {
        if (serverAddress.isNotEmpty() && id.isNotEmpty() && password.isNotEmpty()) {
            enableLoginButton()
        } else if (login_button.background.constantState != context?.run {
                    ContextCompat.getDrawable(this,
                            R.drawable.login_button_disabled_background)?.constantState
                }) {
            disableLoginButton()
        } else {
            login_button.isEnabled = false
        }

        if (error_message.visibility == View.VISIBLE) {
            error_message.visibility = View.INVISIBLE
        }
    }

    private fun enableLoginButton() {
        login_button.isEnabled = true
        login_button.background = context?.run {
            ContextCompat.getDrawable(this, R.drawable.login_button_enabled_background)
        }
    }

    private fun disableLoginButton() {
        login_button.isEnabled = false
        login_button.background = context?.run {
            ContextCompat.getDrawable(this, R.drawable.login_button_disabled_background)
        }
    }

    private fun EditText.afterTextChanged(afterTextChanged: (String) -> Unit) =
            this.addTextChangedListener(object : TextWatcher {
                override fun afterTextChanged(s: Editable?) {
                    afterTextChanged.invoke(s.toString())
                }

                override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {

                }

                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {

                }
            })

    private fun attempLogin() {
        disableLoginButton()

        val domain = server_address_input.text.toString()
        val username = id_input.text.toString()
        val password = password_input.text.toString()

        loginViewModel.login(domain, username, password)
    }


    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment.
         *
         * @return A new instance of fragment LoginFragment.
         */
        @JvmStatic
        fun newInstance() = LoginFragment()
    }
}
