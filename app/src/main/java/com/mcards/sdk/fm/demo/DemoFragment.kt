package com.mcards.sdk.fm.demo

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.google.android.material.snackbar.BaseTransientBottomBar
import com.google.android.material.snackbar.Snackbar
import com.mcards.sdk.auth.AuthSdk
import com.mcards.sdk.auth.AuthSdkProvider
import com.mcards.sdk.auth.model.auth.User
import com.mcards.sdk.cards.CardsSdk
import com.mcards.sdk.cards.CardsSdkProvider
import com.mcards.sdk.cards.CardsViewModel
import com.mcards.sdk.core.model.AuthTokens
import com.mcards.sdk.fm.FeaturesMarketplace
import com.mcards.sdk.fm.FmSdkFactory
import com.mcards.sdk.fm.demo.databinding.FragmentDemoBinding
import com.mcards.sdk.fm.model.FmArgs
import com.mcards.sdk.fm.ui.features.FeaturesViewModel

/**
 * A simple [Fragment] subclass as the default destination in the navigation.
 */
class DemoFragment : Fragment() {

    private var _binding: FragmentDemoBinding? = null
    private val binding get() = _binding!!
    private val featuresVM: FeaturesViewModel by activityViewModels()
    private val fmSdk = FmSdkFactory.get()
    private val cardsVM: CardsViewModel by activityViewModels()
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

        val loginCallback = object : AuthSdk.LoginCallback {
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
                Snackbar.make(view, message, Snackbar.LENGTH_LONG).show()
            }
        }

        binding.loginBtn.setOnClickListener {
            if (userPhoneNumber.isBlank()) {
                authSdk.login(requireContext(), loginCallback)
            } else {
                authSdk.login(requireContext(), userPhoneNumber, loginCallback)
            }
        }

        requireActivity().runOnUiThread {
            cardsVM.cardsList.observe(viewLifecycleOwner) { list ->
                list?.let {
                    //TODO do something with the cards
                    if (it.isNotEmpty()) {
                        val card = it[0]
                        fmSdk.setCard(card)
                        featuresVM.requestAllFeatures(card.uuid!!)
                    }
                }
            }
        }

        requireActivity().runOnUiThread {
            featuresVM.allFeatures.observe(viewLifecycleOwner) {
                it?.let {
                    if (it.isNotEmpty()) {
                        val feature = it[0]
                        //TODO do something with the features
                    }
                }
            }
        }
    }

    private fun initSdks(tokens: AuthTokens) {
        cardsSdk.init(requireActivity(),
            tokens.accessToken,
            debug = true,
            useFirebase =  false,
            object : CardsSdk.InvalidTokenCallback {
                override fun onTokenInvalid(): String {
                    return AuthSdkProvider.getInstance().refreshTokens().accessToken
                }
            })

        val tokenCallback = object : FeaturesMarketplace.InvalidTokenCallback {
            override fun onTokenInvalid(): AuthTokens {
                return AuthSdkProvider.getInstance().refreshTokens()
            }
        }

        val syncCallback = object : FeaturesMarketplace.SyncCallback {
            override fun onFailure(msg: String) {
                Snackbar.make(requireView(), msg, BaseTransientBottomBar.LENGTH_LONG).show()
                binding.progressbar.visibility = View.GONE
            }

            override fun onSubscribe() {
                binding.progressbar.visibility = View.VISIBLE
            }

            override fun onSuccess() {
                binding.progressbar.visibility = View.GONE
                //TODO take any needed action now that the FMSDK is successfully synced,
                // depending on your app and business logic
            }
        }

        val args = FmArgs("programId", requireActivity(), tokenCallback, syncCallback)
        fmSdk.init(args)
        fmSdk.setTokens(tokens)

        cardsVM.requestCardsList()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
