package com.example.itrysohard.justactivity.PersonalPage


import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.itrysohard.R
import com.example.itrysohard.model.User
import com.example.itrysohard.model.answ.UserAnswDTO
import com.example.itrysohard.model.answ.UserAnswDTORolesNoRev

class UserAdapter(
    private val context: Context,
    private var users: List<UserAnswDTORolesNoRev>, // Используем DTO вместо User
    private val onBlock: (UserAnswDTORolesNoRev) -> Unit
) : RecyclerView.Adapter<UserAdapter.UserViewHolder>() {

    inner class UserViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val txtName: TextView = itemView.findViewById(R.id.userName)
        private val txtEmail: TextView = itemView.findViewById(R.id.userEmail)
        private val btBlock: TextView = itemView.findViewById(R.id.btnDelete)

        fun bind(user: UserAnswDTORolesNoRev) {
            txtName.text = user.name
            txtEmail.text = user.email

            val role = user.role
            btBlock.text = if (user.role == "BLOCKED") {
                "Разблокировать"
            } else if (user.role == "ADMIN") {
                "Заблокирвать Админа"
            } else{
                "Заблокировать"
            }



            btBlock.setOnClickListener { onBlock(user) }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
        UserViewHolder(
            LayoutInflater.from(context)
                .inflate(R.layout.item_user, parent, false)
        )

    override fun onBindViewHolder(holder: UserViewHolder, position: Int) {
        holder.bind(users[position])
    }

    override fun getItemCount() = users.size

    fun updateUsers(newUsers: List<UserAnswDTORolesNoRev>) {
        users = newUsers
        notifyDataSetChanged()
    }
}