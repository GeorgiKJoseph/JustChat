package com.example.messenger

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.ViewGroup
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.squareup.picasso.Picasso
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.GroupieViewHolder
import com.xwray.groupie.Item
import com.example.messenger.models.User
import com.google.firebase.auth.FirebaseAuth
import kotlinx.android.synthetic.main.activity_latest_messages.*

import kotlinx.android.synthetic.main.activity_new_message.*
import kotlinx.android.synthetic.main.user_row_new_message.view.*

class NewMessageActivity : AppCompatActivity() {

    val adapter = GroupAdapter<GroupieViewHolder>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_new_message)

        // Changing title of Nav/Action bar
        supportActionBar?.title = "Select User"
        recyclerview_newmessage.adapter = adapter
        recyclerview_newmessage.addItemDecoration(DividerItemDecoration(this, DividerItemDecoration.VERTICAL))

        // Setting content for recyclerview with Groupie
        fetchUsers()
    }

    companion object {
        val USER_KEY = "USER_KEY"
    }

    // Fetching user info from firebase
    private fun fetchUsers(){
        val ref =  FirebaseDatabase.getInstance().getReference("/users")
        ref.addListenerForSingleValueEvent(object: ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                val currentUserId = FirebaseAuth.getInstance().uid
                snapshot.children.forEach {
                    Log.d("NewMessage",it.toString())
                    val user = it.getValue(User::class.java)

                    if (user != null){
                        if (user!!.uid  != currentUserId ){
                            adapter.add(UserItem(user))
                        }
                    }
                    adapter.setOnItemClickListener{ item, view ->

                        val userItem = item as UserItem

                        val intent = Intent(view.context,ChatLogActivity::class.java)
                        intent.putExtra(USER_KEY,userItem.user)
                        startActivity(intent)
                        finish()
                    }
                }
            }
            override fun onCancelled(error: DatabaseError) {

            }
        })
    }
}

class UserItem(val user: User): Item<GroupieViewHolder>(){
    override fun bind(viewHolder: GroupieViewHolder, position: Int) {
        // will be called in list for each user object
        viewHolder.itemView.tw_username_newMessage.text = user.username
        // Load image
        Picasso.get().load(user.profileImageUrl).into(viewHolder.itemView.imgv_newMessage)
    }

    // Setting row for recycler view
    override fun getLayout(): Int {
        return R.layout.user_row_new_message
    }
}