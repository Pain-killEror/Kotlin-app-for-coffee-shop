package com.example.itrysohard.justactivity.PersonalPage

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.itrysohard.R
import com.example.itrysohard.model.answ.UserAnswDTORolesNoRev

class UserAdapter(
    private val context: Context,
    private var users: List<UserAnswDTORolesNoRev>,
    private val currentUserRole: String, // Добавляем роль текущего пользователя (e.g., "ROLE_ADMIN", "ROLE_MODERATOR", "ROLE_USER")
    private val onBlockUnblockClick: (UserAnswDTORolesNoRev) -> Unit, // Переименовали для ясности
    private val onPromoteDemoteClick: (UserAnswDTORolesNoRev) -> Unit // Новая лямбда для роли
) : RecyclerView.Adapter<UserAdapter.UserViewHolder>() {

    inner class UserViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val txtName: TextView = itemView.findViewById(R.id.userName)
        private val txtEmail: TextView = itemView.findViewById(R.id.userEmail)
        private val btnBlockUnblock: Button = itemView.findViewById(R.id.btnBlockUnblock) // Обновленный ID
        private val btnPromoteDemote: Button = itemView.findViewById(R.id.btnPromoteDemote) // Новый ID

        fun bind(userInRow: UserAnswDTORolesNoRev) {
            txtName.text = userInRow.name
            txtEmail.text = userInRow.email

            // --- Логика для кнопки Блокировки/Разблокировки ---
            when (userInRow.role) {
                "BLOCKED" -> {
                    btnBlockUnblock.text = "Разблок."
                    btnBlockUnblock.isEnabled = true // Админ и Модератор могут разблокировать
                    btnBlockUnblock.visibility = View.VISIBLE
                }
                "USER" -> {
                    btnBlockUnblock.text = "Заблок."
                    btnBlockUnblock.isEnabled = true // Админ и Модератор могут заблокировать
                    btnBlockUnblock.visibility = View.VISIBLE
                }
                "ADMIN", "MODERATOR" -> { // Админ/Модератор может заблокировать админа/модератора? Зависит от прав сервера.
                    // Если да:
                    // btnBlockUnblock.text = "Заблок."
                    // btnBlockUnblock.isEnabled = currentUserRole == "ROLE_ADMIN" // Только админ блокирует других админов/модераторов?
                    // btnBlockUnblock.visibility = if(currentUserRole == "ROLE_ADMIN") View.VISIBLE else View.GONE

                    // Если нет (самый безопасный вариант без уточнения прав сервера):
                    btnBlockUnblock.visibility = View.GONE
                    btnBlockUnblock.isEnabled = false
                }
                else -> { // На случай других ролей или ошибок
                    btnBlockUnblock.visibility = View.GONE
                    btnBlockUnblock.isEnabled = false
                }
            }
            btnBlockUnblock.setOnClickListener { onBlockUnblockClick(userInRow) }


            // --- Логика для кнопки Повысить/Понизить (видна только Модератору) ---
            if (currentUserRole == "ROLE_MODERATOR") {
                when (userInRow.role) {
                    "USER" -> {
                        btnPromoteDemote.text = "Сделать админом"
                        btnPromoteDemote.visibility = View.VISIBLE
                        btnPromoteDemote.isEnabled = true
                    }
                    "ADMIN" -> {
                        btnPromoteDemote.text = "Сделать юзером"
                        btnPromoteDemote.visibility = View.VISIBLE
                        btnPromoteDemote.isEnabled = true
                    }
                    else -> { // Нельзя менять роль модераторов, заблокированных, удаленных
                        btnPromoteDemote.visibility = View.GONE
                        btnPromoteDemote.isEnabled = false
                    }
                }
                btnPromoteDemote.setOnClickListener { onPromoteDemoteClick(userInRow) }
            } else { // Админы и юзеры не видят эту кнопку
                btnPromoteDemote.visibility = View.GONE
                btnPromoteDemote.isEnabled = false
            }
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