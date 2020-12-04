package coder.mtk.text_3.ui

import android.app.ProgressDialog
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.fragment.findNavController
import coder.mtk.text_3.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import kotlinx.android.synthetic.main.fragment_create_account.*
import kotlinx.android.synthetic.main.fragment_login.*

class CreateAccountFragment : Fragment() {

    private var mAuth: FirebaseAuth? = null

    private var mDatabase: FirebaseDatabase? = null
    private var mDatabaseReference: DatabaseReference? = null

    private var name: String? = null
    private var email: String? = null
    private var password: String? = null
    private var mProgress: ProgressDialog? = null

    private var userId: String = ""

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_create_account, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initFirebase()



        btnLoginAccount_createPage.setOnClickListener {
            var action = CreateAccountFragmentDirections.actionCreateAccountFragmentToLoginFragment()
            findNavController().navigate(action)
        }

    }

    private fun initFirebase() {
        mProgress = ProgressDialog(this.requireContext())

        mDatabase = FirebaseDatabase.getInstance()
        mDatabaseReference = mDatabase!!.reference!!.child("Items")

        userId = mDatabaseReference!!.push().key.toString()

        mAuth = FirebaseAuth.getInstance()

        btnCreateAccount_createPage.setOnClickListener {
            if (edtName_createPage.text.isNotEmpty() && edtEmail_createPage.text.isNotEmpty() && edtPassword_createPage.text.isNotEmpty()){
                name = edtName_createPage.text.toString()
                email = edtEmail_createPage.text.toString()
                password = edtPassword_createPage.text.toString()
                createAccount()
            }else{
                Toast.makeText(this.requireContext(), "Please fill the field", Toast.LENGTH_LONG).show()
            }

        }
    }

    private fun createAccount() {
        mProgress!!.setMessage("Register user.....")
        mProgress!!.show()

        mAuth!!.createUserWithEmailAndPassword(email!!, password!!)
            .addOnCompleteListener(this.requireActivity()) { task ->
                if (task.isSuccessful) {
                    Toast.makeText(this.requireContext(), "Success signup", Toast.LENGTH_LONG).show()

                    val userId = mAuth!!.currentUser!!.uid

                    verifyEmail()

                    val currentUserDb = mDatabaseReference!!.child(userId)
                    currentUserDb.child("Name").setValue(name)
                    currentUserDb.child("userId").setValue(userId)
                    currentUserDb.child("itemCount").setValue(0)

                    updateUI()


                } else {
                    Toast.makeText(this.requireContext(), "Fail signup", Toast.LENGTH_LONG).show()
                }
            }
    }

    private fun updateUI(){
        mProgress!!.dismiss()
        var action = CreateAccountFragmentDirections.actionCreateAccountFragmentToLoginFragment()
        findNavController().navigate(action)
    }

    private fun verifyEmail(){
        val mUser = mAuth!!.currentUser
        mUser!!.sendEmailVerification()
            .addOnCompleteListener(this.requireActivity()) { task ->
                if (task.isSuccessful){
                    Toast.makeText(this.requireContext(), "Verification successful",Toast.LENGTH_LONG).show()
                }
            }
    }
}