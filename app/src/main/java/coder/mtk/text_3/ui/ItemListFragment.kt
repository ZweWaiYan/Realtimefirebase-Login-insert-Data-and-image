package coder.mtk.text_3.ui

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import androidx.activity.OnBackPressedCallback
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import coder.mtk.text_3.R
import coder.mtk.text_3.model.Item
import coder.mtk.text_3.ui.adapter.ItemsAdapter
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.*
import kotlinx.android.synthetic.main.fragment_item_list.*


class ItemListFragment : Fragment() , ItemsAdapter.ClickListener{

    private var mDatabase: FirebaseDatabase? = null
    private var mDatabaseReference: DatabaseReference? = null
    private var currentFirebaseUser : FirebaseUser? = null
    lateinit var itemsAdapter: ItemsAdapter

    var boo : Boolean = false

    companion object{
        var itemList : ArrayList<Item> = ArrayList()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_item_list, container, false)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        activity?.onBackPressedDispatcher?.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {

            }
        })
        hideSoftKeyboard(this)

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        view.clearFocus()
        boo=true
        itemList.clear()
        mDatabase = FirebaseDatabase.getInstance()
        mDatabaseReference = mDatabase!!.reference!!.child("Items")
        currentFirebaseUser = FirebaseAuth.getInstance().currentUser

        itemsAdapter = ItemsAdapter()

        recyclerView.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = itemsAdapter
        }

        mDatabaseReference!!.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (boo) {
                    Log.d("ItemListFb>>", "work")
                    val count =
                        snapshot.child(currentFirebaseUser!!.uid).child("itemCount").getValue()
                    var countItem = (count.toString().toInt()) - 1
                    for (dataCount in 0..countItem) {
                        val name =
                            snapshot.child(currentFirebaseUser!!.uid).child("ItemEntry").child(
                                dataCount.toString()
                            ).child("name").getValue()
                        val price =
                            snapshot.child(currentFirebaseUser!!.uid).child("ItemEntry").child(
                                dataCount.toString()
                            ).child("price").getValue()
                        val qty =
                            snapshot.child(currentFirebaseUser!!.uid).child("ItemEntry").child(
                                dataCount.toString()
                            ).child("qty").getValue()
                        val imgUrl =
                            snapshot.child(currentFirebaseUser!!.uid).child("ItemEntry").child(
                                dataCount.toString()
                            ).child("itemImageUrl").getValue()
                        Log.d(
                            "ItemList>>",
                            "" + name + " " + price.toString() + " " + qty.toString()
                        )
                        if (name != null && price != null) {
                            itemList.add(
                                Item(
                                    name = name.toString(),
                                    price = price.toString().toInt(),
                                    qty = qty.toString().toInt(),
                                    itemImageUrl = imgUrl.toString()
                                )
                            )
                        }
                    }
                    if (!itemList.isEmpty()) {
                        if (txtNoItemDataLabel != null) {
                            txtNoItemDataLabel.visibility = View.INVISIBLE
                        }
                    }
                    itemsAdapter.updateItemsList(itemList)
                    boo = false
                }
            }

            override fun onCancelled(error: DatabaseError) {

            }

        })

        btnAdd.setOnClickListener {
            var action = ItemListFragmentDirections.actionItemListFragmentToInsertFragment()
            findNavController().navigate(action)
        }

        itemsAdapter.setOnClickListener(this)
    }

    override fun onClick(item: Item) {
        val child =itemList.indexOf(Item(item.name, item.price, item.qty, item.itemImageUrl))

        var action = ItemListFragmentDirections.actionItemListFragmentToEditFragment(
            item.name,
            item.price,
            item.qty,
            item.itemImageUrl,
            child
        )
        findNavController().navigate(action)
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
                inputManager.hideSoftInputFromWindow(view.windowToken, 0)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

}