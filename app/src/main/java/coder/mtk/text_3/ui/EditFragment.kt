package coder.mtk.text_3.ui

import android.app.Activity
import android.app.ProgressDialog
import android.content.Intent
import android.graphics.drawable.BitmapDrawable
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.navigation.fragment.findNavController
import coder.mtk.text_3.R
import coder.mtk.text_3.model.Item
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.*
import com.google.firebase.storage.FirebaseStorage
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.fragment_edit.*
import kotlinx.android.synthetic.main.fragment_insert.*
import java.util.*


class EditFragment : Fragment() {

    private var mDatabase: FirebaseDatabase? = null
    private var mDatabaseReference: DatabaseReference? = null
    private var currentFirebaseUser : FirebaseUser? = null
    private var mProgress: ProgressDialog? = null
    var selectedPhotoUri : Uri? = null

    var boo : Boolean = false

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_edit, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        boo= true
        mDatabase = FirebaseDatabase.getInstance()
        mDatabaseReference = mDatabase!!.reference!!.child("Items")
        currentFirebaseUser = FirebaseAuth.getInstance().currentUser

        var messagearg = arguments?.let {
            EditFragmentArgs.fromBundle(it)
        }

        var name:String? = messagearg?.name
        var price : Int? = messagearg?.price
        var qty : Int? = messagearg?.qty
        var imageUrl : String? = messagearg?.imageUrl
        var index : Int? = messagearg?.index
        Log.d("EditFragment>","index is $index")
        edtItemName1_edit.setText(name)
        edtItemPrice1_edit.setText(price.toString())
        edtItemQty1_edit.setText(qty.toString())
        Picasso.get()
            .load(imageUrl)
            .fit()
            .into(img_edit)

        var itemList = ItemListFragment.itemList
        Log.d("EditFragment>","itemList size is ${itemList.size}")

        btnDelete_edit.setOnClickListener{

                mDatabaseReference!!.addValueEventListener(object :ValueEventListener{
                    override fun onDataChange(snapshot: DataSnapshot) {
                        if (boo){
                            Log.d("editfragment>","work")
                            snapshot.child(currentFirebaseUser!!.uid).child("ItemEntry").child(index.toString()).ref.removeValue()
                            itemList.removeAt(index!!)
                            mDatabaseReference!!.child(currentFirebaseUser!!.uid).child("itemCount").setValue(itemList.size)
                            snapshot.child(currentFirebaseUser!!.uid).child("ItemEntry").ref.removeValue()
                            mDatabaseReference!!.child(currentFirebaseUser!!.uid).child("ItemEntry").setValue(itemList)
                            boo=false
                            var action = EditFragmentDirections.actionEditFragmentToItemListFragment()
                            findNavController().navigate(action)
                        }
                    }

                    override fun onCancelled(error: DatabaseError) {

                    }

                })

        }

        btnUpdate_edit.setOnClickListener {
            mProgress = ProgressDialog(this.requireContext())
            mProgress!!.setMessage("Uploading to Server....")
            mProgress!!.show()
            if (selectedPhotoUri==null){
                saveUserToFirebaseDatabase(imageUrl!!,index)
            }else{
                uploadImageToFirebaseStorage(index)
            }
        }

        btnChoosePhoto_edit.setOnClickListener {
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

            val bitmapDrawable = BitmapDrawable(bitmap)

            img_edit.setImageDrawable(bitmapDrawable)
        }
    }

    private fun uploadImageToFirebaseStorage(index: Int?) {
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
                    saveUserToFirebaseDatabase(it.toString(),index)
                }
            }
    }

    private fun saveUserToFirebaseDatabase(iteImageUrl: String, index: Int?){
            var itemName:String = edtItemName_edit.editText!!.text.toString()
            var itemPrice: Int = edtItemPrice1_edit.text.toString().toInt()
            var itemQty : Int = edtItemQty1_edit.text.toString().toInt()
            Log.d("test>>",""+itemName +" "+ itemPrice.toString() + " "+itemQty.toString())

            var item = Item(itemName,itemPrice,itemQty,iteImageUrl)

            mDatabaseReference!!.addValueEventListener(object : ValueEventListener{
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (boo){
                        mDatabaseReference!!.child(currentFirebaseUser!!.uid).child("ItemEntry").child(index.toString()).setValue(item)
                        boo =false
                        mProgress!!.dismiss()
                        var action = EditFragmentDirections.actionEditFragmentToItemListFragment()
                        findNavController().navigate(action)
                    }
                }

                override fun onCancelled(error: DatabaseError) {

                }

            })
        }

}