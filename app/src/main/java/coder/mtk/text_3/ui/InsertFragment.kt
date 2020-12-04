package coder.mtk.text_3.ui

import android.app.Activity
import android.app.ProgressDialog
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.text.TextUtils
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import coder.mtk.text_3.R
import coder.mtk.text_3.model.Item
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.*
import com.google.firebase.storage.FirebaseStorage
import kotlinx.android.synthetic.main.fragment_insert.*
import kotlinx.android.synthetic.main.fragment_item_list.*
import java.util.*
import kotlin.math.roundToInt


class InsertFragment : Fragment() {

    private lateinit var dbReference: DatabaseReference
    private lateinit var firebaseDatabase: FirebaseDatabase
    private var mAuth: FirebaseAuth? = null
    private var mProgress: ProgressDialog? = null
    var currentFirebaseUser : FirebaseUser? = null

    private var userId: String = ""

    var selectedPhotoUri : Uri? = null
    var boo : Boolean = false

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_insert, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        boo=true
        mProgress = ProgressDialog(this.requireContext())
        mAuth = FirebaseAuth.getInstance()
        firebaseDatabase = FirebaseDatabase.getInstance()
        dbReference = firebaseDatabase.getReference("Items")
        Log.d("test",userId)

        currentFirebaseUser = FirebaseAuth.getInstance().currentUser

        btnSave.setOnClickListener {
            if (edtItemName_insert.editText!!.text.isNotEmpty() && edtItemPrice1_insert.text!!.isNotEmpty() && edtItemQty1_insert.text!!.isNotEmpty()){
                uploadImageToFirebaseStorage()
            }
        }

        btnChoosePhoto_insert.setOnClickListener {
            val intent = Intent(Intent.ACTION_PICK)
            intent.type="image/*"
            startActivityForResult(intent,0)
        }

    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == 0 && resultCode == Activity.RESULT_OK && data != null){
            Log.d("InsertFragment>>","Photo is selected")

            selectedPhotoUri = data.data

            val bitmap = MediaStore.Images.Media.getBitmap(activity?.contentResolver,selectedPhotoUri)

            val bitmapDrawable = BitmapDrawable( scaleBitMap(bitmap))

            img_insert.setImageDrawable(bitmapDrawable)
        }
    }

    private fun scaleBitMap (bm : Bitmap) : Bitmap {
        var width : Int = bm.width
        var height : Int = bm.height

        var bitmap = bm

        Log.d("Pictures", "Width and height are " + width + "--" + height);

        height=600
        width=600

        Log.d("Pictures", "after scaling Width and height are " + width + "--" + height)

        bitmap = Bitmap.createScaledBitmap(bm, width, height, true);

        return bitmap
    }

    private fun uploadImageToFirebaseStorage(){
        mProgress = ProgressDialog(this.requireContext())
        mProgress!!.setMessage("Uploading to Server....")
        mProgress!!.show()
        if (selectedPhotoUri == null) return
            // random filename
            val filename = UUID.randomUUID().toString()
            val ref = FirebaseStorage.getInstance().getReference("/images/$filename")

            ref.putFile(selectedPhotoUri!!)
                .addOnSuccessListener {
                    Log.d("InsertFragment>>", "Successfully upload image : ${it.metadata?.path}")

                    ref.downloadUrl.addOnSuccessListener {
                        it.toString()
                        Log.d("InsertFragment>>", "File location : $it")
                        saveUserToFirebaseDatabase(it.toString())
                    }
                }
    }

    private fun saveUserToFirebaseDatabase(iteImageUrl: String){
        var itemName:String = edtItemName_insert.editText!!.text.toString()
        var itemPrice: Int = edtItemPrice1_insert.text.toString().toInt()
        var itemQty : Int = edtItemQty1_insert.text.toString().toInt()
        Log.d("test>>",""+itemName +" "+ itemPrice.toString() + " "+itemQty.toString())

        var item = Item(itemName,itemPrice,itemQty,iteImageUrl)

        dbReference.addValueEventListener(object : ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                if (boo){
                    val count = snapshot.child(currentFirebaseUser!!.uid).child("itemCount").getValue()
                    var countItem = count.toString().toInt()
                    dbReference.child(currentFirebaseUser!!.uid).child("ItemEntry").child(countItem.toString()).setValue(item)
                    ++countItem
                    dbReference.child(currentFirebaseUser!!.uid).child("itemCount").setValue(countItem)
                    boo =false
                    mProgress!!.dismiss()
                    var action = InsertFragmentDirections.actionInsertFragmentToItemListFragment()
                    findNavController().navigate(action)
                }
            }

            override fun onCancelled(error: DatabaseError) {

            }

        })
    }



}