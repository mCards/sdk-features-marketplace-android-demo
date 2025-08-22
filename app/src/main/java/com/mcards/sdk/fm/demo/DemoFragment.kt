package com.mcards.sdk.fm.demo

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.BaseTransientBottomBar
import com.google.android.material.snackbar.Snackbar
import com.mcards.sdk.auth.AuthSdk
import com.mcards.sdk.auth.AuthSdkProvider
import com.mcards.sdk.auth.model.auth.User
import com.mcards.sdk.cards.CardsSdk
import com.mcards.sdk.cards.CardsSdkProvider
import com.mcards.sdk.core.model.AuthTokens
import com.mcards.sdk.core.model.card.Card
import com.mcards.sdk.core.network.model.SdkResult
import com.mcards.sdk.fm.FmSdk
import com.mcards.sdk.fm.FmSdkProvider
import com.mcards.sdk.fm.demo.databinding.FragmentDemoBinding
import com.mcards.sdk.fm.model.features.Feature
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.core.SingleObserver
import io.reactivex.rxjava3.disposables.Disposable

private const val TEST_PHONE_NUMBER = "4052938132"

/**
 * A simple [Fragment] subclass as the default destination in the navigation.
 */
class DemoFragment : Fragment() {

    private var _binding: FragmentDemoBinding? = null
    private val binding get() = _binding!!
    private val fmSdk = FmSdkProvider.getInstance()
    private val cardsSdk = CardsSdkProvider.getInstance()

    private var userPhoneNumber = ""
    private var accessToken = ""
    private var idToken = ""

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentDemoBinding.inflate(inflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val authSdk = AuthSdkProvider.getInstance()

        val loginCallback = object : AuthSdk.Auth0Callback {
            override fun onSuccess(
                user: User,
                tokens: AuthTokens,
                regionChanged: Boolean,
                cardId: String?
            ) {
                accessToken = tokens.accessToken
                idToken = tokens.idToken
                userPhoneNumber = user.userClaim.phoneNumber
                initSdks(tokens)
            }

            override fun onFailure(message: String) {
                activity?.runOnUiThread {
                    Snackbar.make(view, message, Snackbar.LENGTH_LONG).show()
                }
            }
        }

        binding.loginBtn.setOnClickListener {
            if (userPhoneNumber.isBlank()) {
                authSdk.auth0Authenticate(requireContext(), TEST_PHONE_NUMBER, loginCallback)
            } else {
                authSdk.auth0Authenticate(requireContext(), userPhoneNumber, loginCallback)
            }
        }
    }

    @SuppressLint("CheckResult")
    private fun initSdks(tokens: AuthTokens) {
        cardsSdk.init(requireActivity(),
            tokens.accessToken,
            debug = true,
            object : CardsSdk.InvalidTokenCallback {
                override fun onTokenInvalid(): String {
                    return AuthSdkProvider.getInstance().refreshAuth0Tokens().accessToken
                }
            })

        fmSdk.init(requireContext(),
            tokens.accessToken,
            debug = true,
            object : FmSdk.InvalidTokenCallback {
                override fun onTokenInvalid(): String {
                    return AuthSdkProvider.getInstance().refreshAuth0Tokens().accessToken
                }
            }
        )


        getCards()
    }

    @SuppressLint("CheckResult")
    private fun getCards() {
        cardsSdk.getCardsList()
            .observeOn(AndroidSchedulers.mainThread())
            .subscribeWith(object : SingleObserver<SdkResult<List<Card>>> {
                override fun onSubscribe(d: Disposable) {
                    activity?.runOnUiThread {
                        binding.progressbar.visibility = View.VISIBLE
                    }
                }

                override fun onError(e: Throwable) {
                    activity?.runOnUiThread {
                        binding.progressbar.visibility = View.GONE
                        Snackbar.make(
                            requireView(),
                            e.localizedMessage!!,
                            BaseTransientBottomBar.LENGTH_LONG
                        ).show()
                    }
                }

                override fun onSuccess(t: SdkResult<List<Card>>) {
                    activity?.runOnUiThread {
                        binding.progressbar.visibility = View.GONE
                    }
                    t.result?.let {
                        if (it.isNotEmpty()) {
                            val card = it[0]
                            getFeatures(card)
                        }
                    } ?: t.errorMsg?.let {
                        activity?.runOnUiThread {
                            Snackbar.make(requireView(), it, BaseTransientBottomBar.LENGTH_LONG)
                                .show()
                        }
                    }
                }
            })
    }

    @SuppressLint("CheckResult")
    private fun getFeatures(card: Card) {
        fmSdk.features.getFeatures(card.uuid!!, Feature.Status.ALL, Feature.Sort.USER)
            .observeOn(AndroidSchedulers.mainThread())
            .subscribeWith(object : SingleObserver<SdkResult<Array<Feature>>> {
                override fun onSubscribe(d: Disposable) {
                    activity?.runOnUiThread {
                        binding.progressbar.visibility = View.VISIBLE
                    }
                }

                override fun onError(e: Throwable) {
                    activity?.runOnUiThread {
                        binding.progressbar.visibility = View.GONE
                        Snackbar.make(
                            requireView(),
                            e.localizedMessage!!,
                            BaseTransientBottomBar.LENGTH_LONG
                        ).show()
                    }
                }

                override fun onSuccess(t: SdkResult<Array<Feature>>) {
                    activity?.runOnUiThread {
                        binding.progressbar.visibility = View.GONE
                    }
                    t.result?.let {
                        if (it.isNotEmpty()) {
                            val feature = it[0]
                            activity?.runOnUiThread {
                                MaterialAlertDialogBuilder(requireContext())
                                    .setTitle("Success")
                                    .setMessage(getString(R.string.success, it.size))
                                    .setPositiveButton("Ok") { dialog, _ ->
                                        dialog.dismiss()
                                    }.create().show()
                            }
                        }
                    } ?: t.errorMsg?.let {
                        activity?.runOnUiThread {
                            Snackbar.make(requireView(), it, BaseTransientBottomBar.LENGTH_LONG)
                                .show()
                        }
                    }
                }
            })
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
