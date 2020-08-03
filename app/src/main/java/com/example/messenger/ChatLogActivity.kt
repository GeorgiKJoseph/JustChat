package com.example.messenger

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import androidx.recyclerview.widget.RecyclerView
import com.example.messenger.models.ChatMessage
import com.example.messenger.models.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.ChildEventListener
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.squareup.picasso.Picasso
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.GroupieViewHolder
import com.xwray.groupie.Item
import kotlinx.android.synthetic.main.activity_chat_log.*
import kotlinx.android.synthetic.main.activity_chat_log.view.*
import kotlinx.android.synthetic.main.activity_new_message.*
import kotlinx.android.synthetic.main.chat_from_row.view.*
import kotlinx.android.synthetic.main.chat_to_row.view.*
import kotlinx.android.synthetic.main.user_row_new_message.view.*

class ChatLogActivity : AppCompatActivity() {

    companion object{
        val TAG = "ChatLog"
    }

    val adapter = GroupAdapter<GroupieViewHolder>()
    var toUser: User? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chat_log)

        recyclerview_chatLog.adapter = adapter

        toUser = intent.getParcelableExtra<User>(NewMessageActivity.USER_KEY)
        supportActionBar?.title = toUser?.username

        listenForMessages()

        btn_send_chatLog.setOnClickListener {
            performSendMessage()
        }
    }

    private fun listenForMessages(){
        val fromId = FirebaseAuth.getInstance().uid
        val toId = toUser!!.uid
        val ref = FirebaseDatabase.getInstance().getReference("/user-messages/$fromId/$toId")
        ref.addChildEventListener(object: ChildEventListener {
            override fun onChildAdded(snapshot: DataSnapshot, previousChildName: String?) {
                val chatMessage = snapshot.getValue(ChatMessage::class.java)
                val curId = FirebaseAuth.getInstance().uid
                val toUser = intent.getParcelableExtra<User>(NewMessageActivity.USER_KEY)
                val toId = toUser!!.uid

                if (chatMessage != null){
                    Log.d(TAG,"Dynamically loaded msgs: ${chatMessage!!.text}")

                    if (chatMessage.fromId == curId && chatMessage.toId == toId){
                        val currentUser = LatestMessagesActivity.currentUser

                        adapter.add(ChatToItem(chatMessage.text,currentUser!!))
                    } else if(chatMessage.fromId == toId && chatMessage.toId == curId ){
                        adapter.add(ChatFromItem(chatMessage.text,toUser))
                    }
                }
                recyclerview_chatLog.scrollToPosition(adapter.itemCount - 1)
            }

            override fun onCancelled(error: DatabaseError) { }

            override fun onChildChanged(snapshot: DataSnapshot, previousChildName: String?) { }

            override fun onChildMoved(snapshot: DataSnapshot, previousChildName: String?) { }

            override fun onChildRemoved(snapshot: DataSnapshot) { }
        })

    }


    private fun performSendMessage(){
        val text = et_chatLog.text.toString()
        if (text.isNotEmpty()){

            // SignedIn users uid
            val fromId = FirebaseAuth.getInstance().uid
            val toId = toUser!!.uid

            //val reference = FirebaseDatabase.getInstance().getReference("/messages").push()
            val reference = FirebaseDatabase.getInstance().getReference("/user-messages/$fromId/$toId").push()
            val toReference = FirebaseDatabase.getInstance().getReference("/user-messages/$toId/$fromId").push()

            if (fromId == null) return
            val chatMessage = ChatMessage(reference.key!!,text, fromId, toId, System.currentTimeMillis() / 1000)
            reference.setValue(chatMessage)
                .addOnSuccessListener {
                    Log.d(TAG,"Saved our chat message: ${reference.key}")
                    et_chatLog.text.clear()
                    recyclerview_chatLog.scrollToPosition(adapter.itemCount - 1)
                }
            toReference.setValue(chatMessage)

            val latestMessageRef = FirebaseDatabase.getInstance().getReference("/latest-messages/$fromId/$toId")
            // Overwrite the previous msg in the reference
            latestMessageRef.setValue(chatMessage)

            val latestMessageToRef = FirebaseDatabase.getInstance().getReference("/latest-messages/$toId/$fromId")
            // Overwrite the previous msg in the reference
            latestMessageToRef.setValue(chatMessage)
        }
    }

}

class ChatFromItem(val text: String, val user: User): Item<GroupieViewHolder>() {
    override fun bind(viewHolder: GroupieViewHolder, position: Int) {
        viewHolder.itemView.textView_from_row.text = text
        Picasso.get().load(user.profileImageUrl).into(viewHolder.itemView.imageView_from_row)
    }
    override fun getLayout(): Int {
        return R.layout.chat_from_row
    }
}

class ChatToItem(val text: String, val user: User): Item<GroupieViewHolder>() {
    override fun bind(viewHolder: GroupieViewHolder, position: Int) {
        viewHolder.itemView.textView_to_row.text = text
        Picasso.get().load(user.profileImageUrl).into(viewHolder.itemView.imageView_to_row)
    }
    override fun getLayout(): Int {
        return R.layout.chat_to_row
    }
}