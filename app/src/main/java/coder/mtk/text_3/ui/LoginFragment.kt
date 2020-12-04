package coder.mtk.text_3.ui

import android.app.ProgressDialog
import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.core.content.ContextCompat.getSystemService
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import coder.mtk.text_3.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import kotlinx.android.synthetic.main.fragment_login.*


class LoginFragment : Fragment() {

    private var email: String? = null
    private var password: String? = null

    private var mDatabase: FirebaseDatabase? = null
    private var mDatabaseReference: DatabaseReference? = null
    private var mAuth: FirebaseAuth? = null
    private var mProgress: ProgressDialog? = null


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_login, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initLogin()

        btnCreateAccount_loginPage.setOnClickListener {
            var action = LoginFragmentDirections.actionLoginFragmentToCreateAccountFragment()
            findNavController().navigate(action)
        }

    }

    private fun initLogin() {
        mProgress = ProgressDialog(this.requireContext())

        mAuth = FirebaseAuth.getInstance()

        btnLogin_loginPage.setOnClickListener {
            loginUser()
        }
    }

    private fun loginUser() {
        mDatabase = FirebaseDatabase.getInstance()
        mDatabaseReference = mDatabase!!.reference!!.child("users")
        if (edtEmail_loginPage.text.isNotEmpty() && edtPassword_loginPage.text.isNotEmpty()){
            email = edtEmail_loginPage.text.toString()
            password = edtPassword_loginPage.text.toString()

            mProgress!!.setMessage("Login user....")
            mProgress!!.show()

            mAuth!!.signInWithEmailAndPassword(email!!, password!!)
                .addOnCompleteListener(this.requireActivity()) { task ->

                    if (task.isSuccessful){
                        val currentFirebaseUser = FirebaseAuth.getInstance().currentUser
                        mDatabaseReference!!.addValueEventListener(object : ValueEventListener {
                            override fun onDataChange(snapshot: DataSnapshot) {

                            }

                            override fun onCancelled(error: DatabaseError) {
                                Log.d("Login", "Failed to read value.", error.toException())
                            }

                        })
                        mProgress!!.hide()
                        Toast.makeText(this.requireContext(), "Success Login", Toast.LENGTH_LONG).show()
                        edtPassword_loginPage.clearFocus()
                        edtEmail_loginPage.clearFocus()
                        hideSoftKeyboard(this)
                        var action = LoginFragmentDirections.actionLoginFragmentToItemListFragment()
                        findNavController().navigate(action)

                    } else{
                        Toast.makeText(this.requireContext(), "Login Failed", Toast.LENGTH_LONG).show()
                    }
                }
        }else{          Toast.makeText(
            this.requireContext(),
            "Please fill the field",
            Toast.LENGTH_LONG
        ).show()
        }

    }

    fun hideSoftKeyboard(mFragment: Fragment?) {
        try {
            if (mFragment == null || mFragment.activity == null) {
                return
            }
            val view = mFragment.requireActivity().currentFocus
            if (view != null) {
                val inputManager = mFragment.requireActivity()
                    .getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                inputManager.hideSoftInputFromWindow(edtPassword_loginPage.windowToken, 0)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

}
