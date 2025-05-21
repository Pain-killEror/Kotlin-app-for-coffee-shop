package com.example.itrysohard.justactivity.PersonalPage

import android.app.DatePickerDialog // Импорт для DatePickerDialog
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.View
import android.widget.AdapterView // Импорт для AdapterView
import android.widget.ArrayAdapter // Импорт для ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.itrysohard.R // Убедитесь, что R импортирован для доступа к ресурсам строк

import com.example.itrysohard.databinding.ActivityPersAccBinding
import com.example.itrysohard.justactivity.MainPage.StartActivity
import com.example.itrysohard.justactivity.RegistrationAuthentication.RegAuthActivity
import com.example.itrysohard.justactivity.helpfull.CartCount
import com.example.itrysohard.justactivity.menu.MenuActivity
import com.example.itrysohard.justactivity.menu.cart.CartActivity
import com.example.itrysohard.jwt.JWTDecoder
import com.example.itrysohard.jwt.SharedPrefTokenManager
import com.example.itrysohard.jwt.TokenManager
import com.example.itrysohard.model.DishServ
import com.example.itrysohard.model.answ.AnalyticsResponseItem // Импорт модели ответа
import com.example.itrysohard.model.answ.UserAnswDTORolesNoRev
import com.example.itrysohard.model.answ.UserStatisticsAnswDTO
import com.example.itrysohard.model.info.UserInfoDTO
import com.example.itrysohard.retrofitforDU.DishApi
import com.example.itrysohard.retrofitforDU.OrderApi
import com.example.itrysohard.retrofitforDU.RetrofitService
import com.example.itrysohard.retrofitforDU.UserApi
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.Calendar // Импорт для Calendar
import java.util.Locale // Импорт для Locale

class PersAccActivity : CartCount() {

    private lateinit var binding: ActivityPersAccBinding
    private lateinit var userAdapter: UserAdapter
    private lateinit var tokenManager: TokenManager
    private lateinit var userApi: UserApi
    private lateinit var orderApi: OrderApi // Уже есть
    private lateinit var dishApi: DishApi
    private val handler = Handler(Looper.getMainLooper())
    private val searchRunnable = Runnable { loadUsers(currentSearchText) }
    private var currentSearchText: String = ""
    // private var currentUser: UserInfoDTO? = null // currentUser используется в loadUserData, но можно передавать как параметр

    // Для хранения выбранных значений аналитики
    private var selectedCategoriesList = mutableListOf<String>()
    private var selectedDishesList = mutableListOf<String>()


    // Карта для значений groupBy Spinner (Текст для пользователя -> значение для API)
    private lateinit var groupByOptionsMap: Map<String, String?>

    private var allAvailableDishes = listOf<DishServ>() // Список всех блюд
    private var allAvailableCategories = listOf<String>() // Список уникальных категорий
    private var selectedCategoryIndices: BooleanArray? = null // Состояние чекбоксов для категорий


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPersAccBinding.inflate(layoutInflater)
        setContentView(binding.root)



        tokenManager = SharedPrefTokenManager(this)
        val retrofitService = RetrofitService(this, tokenManager)
        userApi = retrofitService.getUserApi()
        orderApi = retrofitService.getOrderApi()
        dishApi = retrofitService.getDishApi() // Инициализация DishApi
        // 1. Настройка UI, не зависящего от данных пользователя
        setupAdminUI()           // Настройка видимости свитчей, кнопки настроек и т.д.
        setupAnalyticsControls() // Настройка спиннеров, кнопок аналитики
        setupClickListeners()    // Назначение слушателей кликов

        // 2. Проверка авторизации и загрузка данных (асинхронно)
        checkAuthorization() // Внутри вызовет loadUserData -> setupViewIfNeeded

        // 3. Обновление счетчика корзины (можно сразу)
        updateCartCountDisplay(binding.tvCartCount)

        // 4. Загрузка данных для фильтров (асинхронно)
        loadAllDishesAndCategories()// Загружаем данные для фильтров
    }
    private var isViewSetup = false

    private fun checkAuthorization() {
        val prefs = getSharedPreferences("secure_prefs", Context.MODE_PRIVATE)
        val accessToken = prefs.getString("ACCESS_TOKEN", null)
        val refreshToken = prefs.getString("REFRESH_TOKEN", null)

        if (accessToken == null || JWTDecoder.isExpired(refreshToken)) {
            redirectToLogin()
        } else {
            // Загружаем данные и ТОЛЬКО ПОСЛЕ ЭТОГО настраиваем View, зависящие от роли
            loadUserData {
                setupViewIfNeeded() // Вызываем настройку View здесь
            }
        }
    }

    private fun loadUserData(onComplete: () -> Unit = {}) {
        getUserByName { userData ->
            if (userData != null) {
                updateUI(userData)
                if (!isAdmin() && !isModerator()) {
                    loadUserStatistics(userData.id)
                }
            } else {
                showToast("Ошибка загрузки ваших данных")
            }
            onComplete() // Вызываем колбэк (который вызовет setupViewIfNeeded)
        }
    }


    private fun setupViewIfNeeded() {
        if (isViewSetup) return
        Log.d("SetupView", "Вызов setupViewIfNeeded")
        // setupAdminUI() // Убрали отсюда, вызывается в onCreate
        setupRecyclerView() // Инициализация RecyclerView и Adapter
        setupSearchListener() // Настройка слушателя поиска
        isViewSetup = true
    }

    // Инициализация RecyclerView и Адаптера
    private fun setupRecyclerView() {
        binding.recyclerView.layoutManager = LinearLayoutManager(this)
        userAdapter = UserAdapter(
            context = this,
            users = emptyList(),
            currentUserRole = getCurrentUserRole(), // Роль уже известна после checkAuthorization
            onBlockUnblockClick = { user -> showBlockConfirmationDialog(user) },
            onPromoteDemoteClick = { user -> handlePromoteDemoteClick(user) }
        )
        binding.recyclerView.adapter = userAdapter
        Log.d("SetupView", "RecyclerView и Adapter настроены")
    }

    // Настройка слушателя для поиска (можно оставить как есть или вынести из setupView)
    private fun setupSearchListener() {
        binding.tvSearchName.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                val newText = s?.toString()?.trim() ?: ""
                if (newText != currentSearchText) {
                    currentSearchText = newText
                    handler.removeCallbacks(searchRunnable)
                    handler.postDelayed(searchRunnable, 500)
                }
            }
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })
        Log.d("SetupView", "Search listener настроен")
    }



    // Метод для получения роли текущего пользователя
    private fun getCurrentUserRole(): String {
        val prefs = getSharedPreferences("secure_prefs", Context.MODE_PRIVATE)
        val accessToken = prefs.getString("ACCESS_TOKEN", null)
        return JWTDecoder.getRole(accessToken) ?: "ROLE_USER" // По умолчанию USER, если токен невалиден
    }

    // Переопределяем isAdmin на всякий случай
    private fun isAdmin(): Boolean {
        return getCurrentUserRole() == "ROLE_ADMIN"
    }

    // Новая функция для проверки роли модератора
    private fun isModerator(): Boolean {
        return getCurrentUserRole() == "ROLE_MODERATOR"
    }


    // Обновляем loadUsers для фильтрации
    fun loadUsers(prefix: String = "") {
        val currentRole = getCurrentUserRole()
        // Загружаем только если админ или модератор И включен свитч упр. пользователями
        if ((!isAdmin() && !isModerator()) || !binding.switchAdminUsers.isChecked) {
            // Убедимся что адаптер существует перед обновлением
            if (::userAdapter.isInitialized) {
                userAdapter.updateUsers(emptyList())
            }
            return
        }

        val call = if (prefix.isEmpty()) {
            userApi.getAllUsersWithRoles() // Модератор и Админ получают всех (кроме REMOVED)
        } else {
            userApi.getUserByPartOfName(prefix)
        }

        call.enqueue(object : Callback<List<UserAnswDTORolesNoRev>> {
            override fun onResponse(
                call: Call<List<UserAnswDTORolesNoRev>>,
                response: Response<List<UserAnswDTORolesNoRev>>
            ) {
                // Убедимся что адаптер существует
                if (!::userAdapter.isInitialized) return

                if (response.isSuccessful) {
                    val prefs = getSharedPreferences("secure_prefs", Context.MODE_PRIVATE)
                    val myName = JWTDecoder.getName(prefs.getString("ACCESS_TOKEN", null))

                    var usersToShow = response.body()?.filter {
                        it.role != "REMOVED" && it.name != myName // Фильтруем удаленных и себя
                    } ?: emptyList()

                    // Дополнительная фильтрация для Админа - он видит ТОЛЬКО USER и BLOCKED
                    if (isAdmin()) {
                        usersToShow = usersToShow.filter { it.role == "USER" || it.role == "BLOCKED" }
                    }
                    // Модератор видит всех отфильтрованных (USER, BLOCKED, ADMIN, MODERATOR)

                    userAdapter.updateUsers(usersToShow)
                } else {
                    showToast("Ошибка загрузки пользователей: ${response.code()}")
                    userAdapter.updateUsers(emptyList())
                }
            }
            override fun onFailure(call: Call<List<UserAnswDTORolesNoRev>>, t: Throwable) {
                // Убедимся что адаптер существует
                if (!::userAdapter.isInitialized) return

                showToast("Ошибка сети при загрузке пользователей: ${t.message}")
                userAdapter.updateUsers(emptyList())
            }
        })
    }


    // Новый обработчик для клика по кнопке повышения/понижения
    private fun handlePromoteDemoteClick(user: UserAnswDTORolesNoRev) {
        if (!isModerator()) return // Только модератор может это делать

        if (user.role == "USER") {
            // Повышаем до админа
            showConfirmationDialog("Повышение", "Повысить ${user.name} до Администратора?") {
                promoteUserToAdmin(user.id)
            }
        } else if (user.role == "ADMIN") {
            // Понижаем до юзера
            showConfirmationDialog("Понижение", "Понизить ${user.name} до Пользователя?") {
                demoteAdminToUser(user.id)
            }
        }
    }

    // Обобщенный диалог подтверждения
    private fun showConfirmationDialog(title: String, message: String, onConfirm: () -> Unit) {
        AlertDialog.Builder(this)
            .setTitle(title)
            .setMessage(message)
            .setPositiveButton("Да") { _, _ -> onConfirm() }
            .setNegativeButton("Нет", null)
            .show()
    }

    // Методы для вызова API повышения/понижения
    private fun promoteUserToAdmin(userId: Long) {
        userApi.promoteToAdmin(userId).enqueue(object: Callback<Void> { // Предполагаем, что promoteToAdmin есть в UserApi
            override fun onResponse(call: Call<Void>, response: Response<Void>) {
                if (response.isSuccessful) {
                    showToast("Пользователь повышен до Администратора")
                    loadUsers(currentSearchText) // Обновляем список
                } else {
                    showToast("Ошибка повышения: ${response.code()} - ${response.errorBody()?.string() ?: response.message()}")
                }
            }
            override fun onFailure(call: Call<Void>, t: Throwable) {
                showToast("Сетевая ошибка при повышении: ${t.message}")
            }
        })
    }

    private fun demoteAdminToUser(userId: Long) {
        userApi.demoteAdmin(userId).enqueue(object: Callback<Void> { // Предполагаем, что demoteAdmin есть в UserApi
            override fun onResponse(call: Call<Void>, response: Response<Void>) {
                if (response.isSuccessful) {
                    showToast("Администратор понижен до Пользователя")
                    loadUsers(currentSearchText) // Обновляем список
                } else {
                    showToast("Ошибка понижения: ${response.code()} - ${response.errorBody()?.string() ?: response.message()}")
                }
            }
            override fun onFailure(call: Call<Void>, t: Throwable) {
                showToast("Сетевая ошибка при понижении: ${t.message}")
            }
        })
    }


    // В checkAuthorization вызываем setupViewIfNeeded после loadUserData

    // Обновляем loadUserData для принятия колбэка



    private fun setupAdminUI() {
        val isAdmin = isAdmin()
        val isModerator = isModerator() // Добавляем проверку на модератора

        // Видимость по умолчанию
        binding.statistics.visibility = if (isAdmin || isModerator) View.GONE else View.VISIBLE // Скрываем статистику для админа и модера
        binding.btnAccountSettings.visibility = View.VISIBLE // Кнопка настроек видна ВСЕГДА
        binding.layoutAccountActions.visibility = View.GONE // Выйти/Удалить скрыты

        // Свитчи видны админу и модератору
        binding.switchAdminUsers.visibility = if (isAdmin || isModerator) View.VISIBLE else View.GONE
        binding.switchAnalytics.visibility = if (isAdmin || isModerator) View.VISIBLE else View.GONE // Аналитика только для админа

        // Панели по умолчанию скрыты
        binding.tvSearchName.visibility = View.GONE
        binding.recyclerView.visibility = View.GONE
        binding.analyticsFull.visibility = View.GONE

        // Слушатель для кнопки настроек аккаунта
        binding.btnAccountSettings.setOnClickListener {
            val isActionsVisible = binding.layoutAccountActions.visibility == View.VISIBLE
            binding.layoutAccountActions.visibility = if (isActionsVisible) View.GONE else View.VISIBLE
            binding.btnDelete.visibility = if(isAdmin || isModerator) View.GONE else View.VISIBLE
            // Выключаем свитчи, если они видны и активны, при открытии настроек
            if (!isActionsVisible && (isAdmin || isModerator)) {
                if (binding.switchAdminUsers.isChecked) binding.switchAdminUsers.isChecked = false
                if (binding.switchAnalytics.isChecked) binding.switchAnalytics.isChecked = false
            }
        }

        // Слушатели для свитчей (только если админ или модератор)
        if (isAdmin || isModerator) {
            binding.switchAdminUsers.setOnCheckedChangeListener { buttonView, isChecked ->
                if (buttonView.isPressed || !isChecked) {
                    if (isChecked) {
                        // Выключаем аналитику, если она есть и включена
                        if (isAdmin && binding.switchAnalytics.isChecked) binding.switchAnalytics.isChecked = false
                        binding.tvSearchName.visibility = View.VISIBLE
                        binding.recyclerView.visibility = View.VISIBLE
                        binding.analyticsFull.visibility = View.GONE // Скрыть аналитику
                        binding.statistics.visibility = View.GONE
                        // layoutAccountActions не трогаем
                        if (userAdapter.itemCount == 0) loadUsers()
                    } else {
                        // Скрываем панель, если только аналитика тоже выключена (для админа)
                        // или если просто выключили (для модера)
                        if (!binding.switchAnalytics.isChecked || !isAdmin) {
                            binding.tvSearchName.visibility = View.GONE
                            binding.recyclerView.visibility = View.GONE
                            // Возвращаем статистику юзеру, если он НЕ админ и НЕ модер
                            if(!isAdmin && !isModerator) binding.statistics.visibility = View.VISIBLE
                        }
                    }
                }
            }
        }
        // Слушатель для свитча аналитики (только если админ)
        if (isAdmin || isModerator) {
            binding.switchAnalytics.setOnCheckedChangeListener { buttonView, isChecked ->
                if (buttonView.isPressed || !isChecked) {
                    if (isChecked) {
                        binding.switchAdminUsers.isChecked = false // Выключаем упр. пользователями
                        binding.analyticsFull.visibility = View.VISIBLE // Показать аналитику
                        binding.tvSearchName.visibility = View.GONE
                        binding.recyclerView.visibility = View.GONE
                        binding.statistics.visibility = View.GONE
                        // layoutAccountActions не трогаем
                    } else {
                        // Скрываем панель, если только упр. пользователями тоже выключено
                        if (!binding.switchAdminUsers.isChecked) {
                            binding.analyticsFull.visibility = View.GONE
                            // Возвращаем статистику юзеру
                            if(!isAdmin && !isModerator) binding.statistics.visibility = View.VISIBLE
                        }
                    }
                }
            }
        } else {
            // Если не админ, убедимся что панель аналитики скрыта
            binding.analyticsFull.visibility = View.GONE
        }
    }

    private fun setupAnalyticsControls() {
        // --- Заголовок для сворачивания ---
        binding.tvAnalyticsSettingsHeader.setOnClickListener {
            Log.d("AnalyticsHeaderClick", "tvAnalyticsSettingsHeader clicked!")
            val isHeaderVisible = binding.analyticsHeader.visibility == View.VISIBLE
            binding.analyticsHeader.visibility = if (isHeaderVisible) View.GONE else View.VISIBLE
            binding.tvAnalyticsSettingsHeader.text = getString(
                if (isHeaderVisible) R.string.analytics_settings_header_collapsed
                else R.string.analytics_settings_header_expanded
            )
        }

        // --- Выбор типа периода ---
        val periodTypes = resources.getStringArray(R.array.analytics_period_types).toList()
        val periodTypeAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, periodTypes)
        periodTypeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.spinnerPeriodType.adapter = periodTypeAdapter
        binding.spinnerPeriodType.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                updatePeriodInputVisibility(periodTypes[position])
            }
            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }
        updatePeriodInputVisibility(periodTypes[0])

        // --- Спиннеры для деталей периода ---
        val monthsDisplay = resources.getStringArray(R.array.analytics_months_display)
        val monthDetailAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, monthsDisplay)
        monthDetailAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.spinnerPeriodMonthDetail.adapter = monthDetailAdapter

        val quartersDisplay = resources.getStringArray(R.array.analytics_quarters_display)
        val quarterDetailAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, quartersDisplay)
        quarterDetailAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.spinnerPeriodQuarterDetail.adapter = quarterDetailAdapter

        val seasonsDisplay = resources.getStringArray(R.array.analytics_seasons_display)
        val seasonDetailAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, seasonsDisplay)
        seasonDetailAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.spinnerPeriodSeasonDetail.adapter = seasonDetailAdapter

        // --- Date Pickers ---
        binding.tvPeriodStartDate.text = getString(R.string.analytics_hint_start_date)
        binding.tvPeriodEndDate.text = getString(R.string.analytics_hint_end_date)
        binding.tvPeriodStartDate.setOnClickListener { showDatePickerDialog(true) }
        binding.tvPeriodEndDate.setOnClickListener { showDatePickerDialog(false) }

        // --- Кнопки выбора категорий и блюд ---
        binding.btnSelectCategories.setOnClickListener { showCategorySelectionDialog() }
        binding.btnSelectDishes.setOnClickListener { showDishSelectionDialog() }

        // --- Группировка ---
        val groupByDisplayOptions = resources.getStringArray(R.array.analytics_group_by_display)
        val groupByApiValues = resources.getStringArray(R.array.analytics_group_by_api_values)
        groupByOptionsMap = groupByDisplayOptions.zip(groupByApiValues).associate { (display, api) ->
            display to if (api.isEmpty()) null else api
        }
        val groupByAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, groupByDisplayOptions.toList())
        groupByAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.spinnerAnalyticsGroupBy.adapter = groupByAdapter

        // --- Кнопка "Получить аналитику" ---
        binding.btnFetchAnalytics.setOnClickListener { fetchAnalyticsData() }
    }


    // Метод для загрузки данных для фильтров
    private fun loadAllDishesAndCategories() {
        // Убрали токен, так как API его не требует
        dishApi.getAllDishes().enqueue(object : Callback<List<DishServ>> {
            override fun onResponse(call: Call<List<DishServ>>, response: Response<List<DishServ>>) {
                if (response.isSuccessful) {
                    allAvailableDishes = response.body() ?: emptyList()
                    allAvailableCategories = allAvailableDishes
                        .map { it.category }
                        .filter { it.isNotBlank() } // Исключаем пустые категории, если они есть
                        .distinct()
                        .sorted()
                    Log.d("DataLoad", "Загружено ${allAvailableDishes.size} блюд и ${allAvailableCategories.size} категорий")
                    // Инициализируем массив для диалога категорий
                    selectedCategoryIndices = BooleanArray(allAvailableCategories.size) { false }
                } else {
                    Log.e("DataLoad", "Ошибка загрузки блюд: ${response.code()}")
                    showToast("Не удалось загрузить список блюд/категорий")
                }
            }
            override fun onFailure(call: Call<List<DishServ>>, t: Throwable) {
                Log.e("DataLoad", "Сетевая ошибка при загрузке блюд", t)
                showToast("Сетевая ошибка при загрузке блюд/категорий")
            }
        })
    }

    private fun showCategorySelectionDialog() {
        if (allAvailableCategories.isEmpty()) {
            showToast("Список категорий пуст или не загружен")
            // Попробовать загрузить еще раз?
            // loadAllDishesAndCategories()
            return
        }

        val categoriesArray = allAvailableCategories.toTypedArray()
        val checkedItems = selectedCategoryIndices?.clone() ?: BooleanArray(categoriesArray.size) { false }

        AlertDialog.Builder(this)
            .setTitle("Выберите категории")
            .setMultiChoiceItems(categoriesArray, checkedItems) { _, which, isChecked ->
                checkedItems[which] = isChecked
            }
            .setPositiveButton("ОК") { _, _ ->
                selectedCategoryIndices = checkedItems // Сохраняем состояние
                selectedCategoriesList.clear()
                for (i in categoriesArray.indices) {
                    if (checkedItems[i]) {
                        selectedCategoriesList.add(categoriesArray[i])
                    }
                }
                // Сбрасываем выбор блюд, так как категории могли измениться
                selectedDishesList.clear()
                // selectedDishIndices больше не нужен, т.к. массив генерируется динамически
                updateSelectedFiltersUI()
            }
            .setNegativeButton("Отмена", null)
            .setNeutralButton("Сбросить") { _, _ ->
                selectedCategoryIndices?.fill(false)
                selectedCategoriesList.clear()
                selectedDishesList.clear() // Сбрасываем и блюда
                updateSelectedFiltersUI()
            }
            .show()
    }

    private fun showDishSelectionDialog() {
        val relevantDishes = if (selectedCategoriesList.isEmpty()) {
            allAvailableDishes
        } else {
            allAvailableDishes.filter { selectedCategoriesList.contains(it.category) }
        }

        if (relevantDishes.isEmpty()) {
            showToast(if (selectedCategoriesList.isEmpty()) "Список блюд пуст" else "Нет блюд в выбранных категориях")
            return
        }

        val dishNamesArray = relevantDishes.map { it.name }.distinct().sorted().toTypedArray()
        // Динамически создаем массив состояния для текущего списка
        val currentCheckedItems = BooleanArray(dishNamesArray.size) { index ->
            selectedDishesList.contains(dishNamesArray[index]) // Отмечаем ранее выбранные
        }

        AlertDialog.Builder(this)
            .setTitle("Выберите блюда")
            .setMultiChoiceItems(dishNamesArray, currentCheckedItems) { _, which, isChecked ->
                currentCheckedItems[which] = isChecked
            }
            .setPositiveButton("ОК") { _, _ ->
                selectedDishesList.clear()
                for (i in dishNamesArray.indices) {
                    if (currentCheckedItems[i]) {
                        selectedDishesList.add(dishNamesArray[i])
                    }
                }
                updateSelectedFiltersUI()
            }
            .setNegativeButton("Отмена", null)
            .setNeutralButton("Сбросить") { _, _ ->
                selectedDishesList.clear()
                updateSelectedFiltersUI()
            }
            .show()
    }

    private fun updateSelectedFiltersUI() {
        binding.tvSelectedCategories.text = if (selectedCategoriesList.isEmpty()) {
            getString(R.string.analytics_filter_categories_all) // "Категории: все"
        } else {
            "${getString(R.string.analytics_filter_categories_prefix)} ${selectedCategoriesList.joinToString()}" // "Категории: ..."
        }

        binding.tvSelectedDishes.text = if (selectedDishesList.isEmpty()) {
            getString(R.string.analytics_filter_dishes_all) // "Блюда: все"
        } else {
            "${getString(R.string.analytics_filter_dishes_prefix)} ${selectedDishesList.joinToString()}" // "Блюда: ..."
        }
    }

    // --- Методы updatePeriodInputVisibility, showDatePickerDialog, validateDateRange, fetchAnalyticsData, displayAnalyticsResults ---
    // --- БЕЗ ИЗМЕНЕНИЙ по сравнению с предыдущим ответом ---
    // --- (кроме добавленных Log и Toast, и валидации в fetchAnalyticsData) ---
    private fun updatePeriodInputVisibility(selectedPeriodType: String) {
        binding.etPeriodYearShared.visibility = View.GONE
        binding.layoutPeriodDetails.visibility = View.GONE
        binding.spinnerPeriodMonthDetail.visibility = View.GONE
        binding.spinnerPeriodQuarterDetail.visibility = View.GONE
        binding.spinnerPeriodSeasonDetail.visibility = View.GONE
        binding.layoutDateRangeInput.visibility = View.GONE
        when (selectedPeriodType) {
            getString(R.string.analytics_period_year) -> binding.etPeriodYearShared.visibility = View.VISIBLE
            getString(R.string.analytics_period_month) -> {
                binding.etPeriodYearShared.visibility = View.VISIBLE
                binding.layoutPeriodDetails.visibility = View.VISIBLE
                binding.spinnerPeriodMonthDetail.visibility = View.VISIBLE
            }
            getString(R.string.analytics_period_quarter) -> {
                binding.etPeriodYearShared.visibility = View.VISIBLE
                binding.layoutPeriodDetails.visibility = View.VISIBLE
                binding.spinnerPeriodQuarterDetail.visibility = View.VISIBLE
            }
            getString(R.string.analytics_period_season) -> {
                binding.etPeriodYearShared.visibility = View.VISIBLE
                binding.layoutPeriodDetails.visibility = View.VISIBLE
                binding.spinnerPeriodSeasonDetail.visibility = View.VISIBLE
            }
            getString(R.string.analytics_period_range) -> binding.layoutDateRangeInput.visibility = View.VISIBLE
            getString(R.string.analytics_period_all_time) -> { /* Ничего */ }
        }
    }
    private fun showDatePickerDialog(isStartDate: Boolean) {
        val calendar = Calendar.getInstance()
        val targetTextView = if (isStartDate) binding.tvPeriodStartDate else binding.tvPeriodEndDate
        val hintText = if (isStartDate) getString(R.string.analytics_hint_start_date) else getString(R.string.analytics_hint_end_date)
        val currentText = targetTextView.text.toString()
        if (currentText.isNotEmpty() && currentText != hintText) {
            try {
                val parts = currentText.split("-")
                if (parts.size == 3) calendar.set(parts[0].toInt(), parts[1].toInt() - 1, parts[2].toInt())
            } catch (e: Exception) { Log.e("DatePicker", "Error parsing date: $currentText", e) }
        }
        val datePickerDialog = DatePickerDialog( this, { _, year, month, dayOfMonth ->
            val selectedDate = String.format(Locale.getDefault(), "%d-%02d-%02d", year, month + 1, dayOfMonth)
            targetTextView.text = selectedDate
            validateDateRange()
        },
            calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH)
        )
        datePickerDialog.show()
    }
    private fun validateDateRange() {
        val startDateStr = binding.tvPeriodStartDate.text.toString().takeIf { it != getString(R.string.analytics_hint_start_date) }
        val endDateStr = binding.tvPeriodEndDate.text.toString().takeIf { it != getString(R.string.analytics_hint_end_date) }
        if (startDateStr != null && endDateStr != null && startDateStr > endDateStr) {
            showToast("Начальная дата не может быть позже конечной!")
            binding.tvPeriodEndDate.text = getString(R.string.analytics_hint_end_date)
        }
    }
    private fun fetchAnalyticsData() {
        val token = "Bearer ${tokenManager.getAccessToken() ?: run { showToast("Ошибка авторизации для аналитики"); return }}"
        var year: Int? = null; var monthApiValue: String? = null; var quarter: Int? = null
        var seasonApiValue: String? = null; var startDate: String? = null; var endDate: String? = null
        val selectedPeriodType = binding.spinnerPeriodType.selectedItem.toString()
        val yearInputEditText = binding.etPeriodYearShared
        var hasValidationError = false
        try {
            when (selectedPeriodType) {
                getString(R.string.analytics_period_year) -> {
                    year = yearInputEditText.text.toString().toIntOrNull()
                    if (year == null && yearInputEditText.text.isNotBlank()) { showToast("Введите корректный год"); hasValidationError = true }
                    else if(year != null && (year < 1900 || year > 2100)) { showToast("Введите корректный год (1900-2100)"); hasValidationError = true }
                    else if (year == null && yearInputEditText.text.isBlank()){ showToast("Год обязателен для типа периода 'Год'"); hasValidationError = true }
                }
                getString(R.string.analytics_period_month) -> {
                    year = yearInputEditText.text.toString().toIntOrNull()
                    val selectedMonthPosition = binding.spinnerPeriodMonthDetail.selectedItemPosition
                    if (year == null && yearInputEditText.text.isNotBlank()) { showToast("Введите корректный год для месяца"); hasValidationError = true }
                    else if (year == null && selectedMonthPosition > 0) { showToast("Сначала укажите год для выбранного месяца"); hasValidationError = true }
                    else if(year != null && (year < 1900 || year > 2100)) { showToast("Введите корректный год (1900-2100)"); hasValidationError = true }
                    if (!hasValidationError && selectedMonthPosition > 0) { monthApiValue = resources.getStringArray(R.array.analytics_months_api_values)[selectedMonthPosition - 1] }
                    else if (!hasValidationError && year == null && selectedMonthPosition == 0) { /* Возможно, запрос за все время? Или ошибка? */ showToast("Укажите год или месяц"); hasValidationError = true }
                }
                getString(R.string.analytics_period_quarter) -> {
                    year = yearInputEditText.text.toString().toIntOrNull()
                    val selectedQuarterPosition = binding.spinnerPeriodQuarterDetail.selectedItemPosition
                    if (year == null && yearInputEditText.text.isNotBlank()) { showToast("Введите корректный год для квартала"); hasValidationError = true }
                    else if (year == null && selectedQuarterPosition > 0) { showToast("Сначала укажите год для выбранного квартала"); hasValidationError = true }
                    else if(year != null && (year < 1900 || year > 2100)) { showToast("Введите корректный год (1900-2100)"); hasValidationError = true }
                    if (!hasValidationError && selectedQuarterPosition > 0) { quarter = selectedQuarterPosition }
                    else if (!hasValidationError && year == null && selectedQuarterPosition == 0) { showToast("Укажите год или квартал"); hasValidationError = true }
                }
                getString(R.string.analytics_period_season) -> {
                    year = yearInputEditText.text.toString().toIntOrNull()
                    val selectedSeasonPosition = binding.spinnerPeriodSeasonDetail.selectedItemPosition
                    if (year == null && yearInputEditText.text.isNotBlank()) { showToast("Введите корректный год для сезона"); hasValidationError = true }
                    else if (year == null && selectedSeasonPosition > 0) { showToast("Сначала укажите год для выбранного сезона"); hasValidationError = true }
                    else if(year != null && (year < 1900 || year > 2100)) { showToast("Введите корректный год (1900-2100)"); hasValidationError = true }
                    if (!hasValidationError && selectedSeasonPosition > 0) { seasonApiValue = resources.getStringArray(R.array.analytics_seasons_api_values)[selectedSeasonPosition - 1] }
                    else if (!hasValidationError && year == null && selectedSeasonPosition == 0) { showToast("Укажите год или сезон"); hasValidationError = true }
                }
                getString(R.string.analytics_period_range) -> {
                    startDate = binding.tvPeriodStartDate.text.toString().takeIf { it != getString(R.string.analytics_hint_start_date) }
                    endDate = binding.tvPeriodEndDate.text.toString().takeIf { it != getString(R.string.analytics_hint_end_date) }
                    if (startDate == null || endDate == null) { showToast("Выберите начальную и конечную даты"); hasValidationError = true }
                    else if (startDate > endDate) { showToast("Начальная дата не может быть позже конечной"); hasValidationError = true }
                }
                getString(R.string.analytics_period_all_time) -> { /* OK */ }
            }
        } catch (e: Exception) { showToast("Ошибка при обработке параметров периода."); Log.e("AnalyticsFetch", "Error parsing period parameters", e); hasValidationError = true }
        if (hasValidationError) return
        val metricsList = mutableListOf<String>().apply {
            if (binding.cbAnalyticsCount.isChecked) add("count")
            if (binding.cbAnalyticsRevenue.isChecked) add("revenue")
            if (binding.cbAnalyticsPercentage.isChecked) add("percentage")
        }
        if (metricsList.isEmpty()) { showToast("Выберите хотя бы одну метрику"); return }
        val selectedGroupByDisplay = binding.spinnerAnalyticsGroupBy.selectedItem.toString()
        val groupByApiValue: String? = groupByOptionsMap[selectedGroupByDisplay]
        Log.d("AnalyticsFetch", "Params: year=$year, month=$monthApiValue, quarter=$quarter, season=$seasonApiValue, startDate=$startDate, endDate=$endDate, categories=$selectedCategoriesList, dishes=$selectedDishesList, metrics=$metricsList, groupBy=$groupByApiValue")
        binding.analyticsBody.removeAllViews(); binding.analyticsBody.visibility = View.GONE
        orderApi.getOrderAnalytics(token, year, monthApiValue, selectedCategoriesList.ifEmpty { null }, selectedDishesList.ifEmpty { null }, metricsList, groupByApiValue, quarter, startDate, endDate, seasonApiValue)
            .enqueue(object : Callback<List<AnalyticsResponseItem>> {
                override fun onResponse(call: Call<List<AnalyticsResponseItem>>, response: Response<List<AnalyticsResponseItem>>) {
                    if (response.isSuccessful) {
                        displayAnalyticsResults(response.body() ?: emptyList())
                    } else {
                        showToast("Ошибка загрузки аналитики: ${response.code()}")
                        Log.e("AnalyticsError", "Code: ${response.code()}, Message: ${response.message()}, Body: ${response.errorBody()?.string()}")
                        displayAnalyticsResults(emptyList(), "Ошибка: ${response.code()}")
                    }
                }
                override fun onFailure(call: Call<List<AnalyticsResponseItem>>, t: Throwable) {
                    showToast("Сетевая ошибка: ${t.message}")
                    Log.e("AnalyticsFailure", "Error: ", t)
                    displayAnalyticsResults(emptyList(), "Сетевая ошибка")
                }
            })
    }
    private fun displayAnalyticsResults(data: List<AnalyticsResponseItem>, errorMessage: String? = null) {
        binding.analyticsBody.removeAllViews(); binding.analyticsBody.visibility = View.VISIBLE
        val context = this
        if (errorMessage != null) {
            val textView = android.widget.TextView(context).apply { text = errorMessage; setTextColor(ContextCompat.getColor(context, android.R.color.holo_red_dark)); gravity = android.view.Gravity.CENTER; setPadding(16.dpToPx(), 16.dpToPx(), 16.dpToPx(), 16.dpToPx()) }
            binding.analyticsBody.addView(textView); return
        }
        if (data.isEmpty()) {
            val textView = android.widget.TextView(context).apply { text = "Нет данных для отображения по выбранным фильтрам."; gravity = android.view.Gravity.CENTER; setPadding(16.dpToPx(), 16.dpToPx(), 16.dpToPx(), 16.dpToPx()) }
            binding.analyticsBody.addView(textView); return
        }
        data.forEach { item ->
            val textView = android.widget.TextView(context).apply {
                val countText = item.count?.toString() ?: "-"
                val revenueText = item.revenue?.let { "%.2f".format(it) } ?: "-"
                val percentageText = item.percentage?.let { "%.2f%%".format(it) } ?: "-"
                text = "Группа: ${item.group ?: "N/A"}\n  Кол-во: $countText, Выручка: $revenueText, Процент: $percentageText"
                setPadding(16.dpToPx(), 8.dpToPx(), 16.dpToPx(), 8.dpToPx())
                setTextColor(ContextCompat.getColor(context, android.R.color.black))
            }
            binding.analyticsBody.addView(textView)
            val separator = View(context).apply {
                layoutParams = android.widget.LinearLayout.LayoutParams(android.widget.LinearLayout.LayoutParams.MATCH_PARENT, 1.dpToPx())
                setBackgroundColor(ContextCompat.getColor(context, android.R.color.darker_gray))
                (layoutParams as android.widget.LinearLayout.LayoutParams).setMargins(16.dpToPx(), 0, 16.dpToPx(), 0)
            }
            binding.analyticsBody.addView(separator)
        }
    }

    // Функция расширения для конвертации dp в px
    fun Int.dpToPx(): Int = (this * resources.displayMetrics.density).toInt()


    // --- Остальные методы вашего класса (loadUserStatistics, updateUI, isAdmin, и т.д.) ---
    // Убедитесь, что вы переименовали binding.switchAdmin на binding.switchAdminUsers
    // в существующих частях кода, где это необходимо.
    // Например, в isAdmin() и в слушателе switchAdminUsers.
    // ... (ваш существующий код)

    private fun loadUserStatistics(userId: Long) {
        val token = "Bearer ${tokenManager.getAccessToken() ?: run {
            showToast("Ошибка авторизации")
            return
        }}"

        Log.d("Statistics", "Requesting stats for userId: $userId")

        orderApi.getUserStatistics(token, userId).enqueue(object : Callback<UserStatisticsAnswDTO> {
            override fun onResponse(call: Call<UserStatisticsAnswDTO>, response: Response<UserStatisticsAnswDTO>) {
                if (response.isSuccessful) {
                    response.body()?.let { statistics ->
                        updateStatisticsUI(statistics)
                    }
                } else {
                    showToast("Ошибка загрузки статистики: ${response.code()}")
                    Log.e("Statistics", "Error response: ${response.errorBody()?.string()}")
                }
            }

            override fun onFailure(call: Call<UserStatisticsAnswDTO>, t: Throwable) {
                showToast("Ошибка сети: ${t.message}")
                Log.e("Statistics", "Network error", t)
            }
        })
    }

    private fun updateStatisticsUI(statistics: UserStatisticsAnswDTO) {
        // Основная статистика
        binding.tvCountOrdersThisMonth.text = statistics.ordersThisMonth.toString()
        binding.tvCountOrdersThisYear.text = statistics.ordersThisYear.toString()
        binding.tvCountOrdersFullPeriod.text = statistics.totalOrders.toString()
        binding.tvCountOrdersAverageOrderAmount.text = "%.2f".format(statistics.averageOrderAmount)

        // Управление видимостью раздела с топом блюд
        if (statistics.topDishes.isEmpty()) {
            // Скрываем весь блок если нет блюд
            binding.tvOrdersTopDishes.visibility = View.GONE
            binding.tvTopDish1.visibility = View.GONE
            binding.tvTopDish2.visibility = View.GONE
            binding.tvTopDish3.visibility = View.GONE
        } else {
            // Показываем заголовок
            binding.tvOrdersTopDishes.visibility = View.VISIBLE

            // Сбрасываем видимость всех полей
            binding.tvTopDish1.visibility = View.VISIBLE
            binding.tvTopDish2.visibility = View.VISIBLE
            binding.tvTopDish3.visibility = View.VISIBLE

            // Обрабатываем каждое блюдо
            statistics.topDishes.forEachIndexed { index, dish ->
                when (index) {
                    0 -> binding.tvTopDish1.text = dish
                    1 -> binding.tvTopDish2.text = dish
                    2 -> binding.tvTopDish3.text = dish
                }
            }

            // Скрываем неиспользуемые поля
            when (statistics.topDishes.size) {
                1 -> {
                    binding.tvTopDish2.visibility = View.GONE
                    binding.tvTopDish3.visibility = View.GONE
                }
                2 -> {
                    binding.tvTopDish3.visibility = View.GONE
                }
            }
        }

        // Общая статистика по деньгам
        binding.tvCountOrdersTotalSpent.text = "%.2f".format(statistics.totalSpent)
        binding.tvCountOrdersTotalSaved.text = "%.2f".format(statistics.totalSaved)
    }




    private fun getUserByName(onResult: (UserInfoDTO?) -> Unit) {
        val prefs = getSharedPreferences("secure_prefs", Context.MODE_PRIVATE)
        val accessToken = prefs.getString("ACCESS_TOKEN", null)
        val name = JWTDecoder.getName(accessToken) // Извлекаем имя из токена

        userApi.getUserByName(name).enqueue(object : Callback<UserInfoDTO> {
            override fun onResponse(call: Call<UserInfoDTO>, response: Response<UserInfoDTO>) {
                if (response.isSuccessful) {
                    val userData = response.body()
                    if (userData != null) {
                        onResult(userData)
                    } else {
                        showToast("Не удалось получить данные пользователя (пустой ответ)") // Более конкретно
                        Log.e("GetUserData", "getUserByName returned successful but empty body")
                        onResult(null)
                    }
                } else {
                    // Более детальное сообщение об ошибке
                    val errorMsg = "Ошибка загрузки данных пользователя: ${response.code()}"
                    showToast(errorMsg)
                    Log.e("GetUserData", "$errorMsg - ${response.errorBody()?.string()}")
                    onResult(null)
                }
            }

            override fun onFailure(call: Call<UserInfoDTO>, t: Throwable) {
                // Более детальное сообщение об ошибке
                val errorMsg = "Сетевая ошибка при загрузке данных пользователя"
                showToast("$errorMsg: ${t.message}")
                Log.e("GetUserData", errorMsg, t)
                onResult(null)
            }
        })

    }




    private fun updateUI(user: UserInfoDTO) {
        val email = user.email
        val name = user.name
        binding.tvPersName.text = name
        binding.tvPersEmail.setText(email) // У вас было setText, для TextView это просто .text
        binding.tvPersEmail.text = email   // Исправлено

        // Видимость блока статистики пользователя и кнопки удаления аккаунта
        // перенесена в setupAdminUI и loadUserData для корректного отображения
        // в зависимости от того, админ ли пользователь и какой свитч активен.
    }



    // При загрузке списка пользователей:


    private fun setupClickListeners() {
        binding.btLogout.setOnClickListener { showLogoutConfirmationDialog() }
        binding.btnHome.setOnClickListener { navigateTo(StartActivity::class.java) }
        binding.btnMenu.setOnClickListener { navigateTo(MenuActivity::class.java) }
        binding.btnCart.setOnClickListener { navigateTo(CartActivity::class.java) }
        // Слушатель для кнопки "Удалить аккаунт" - теперь доступен всем
        binding.btnDelete.setOnClickListener {
            showDeleteConfirmationDialog() // Просто вызываем диалог подтверждения
        }
        // binding.btnPersAcc.setOnClickListener { /* ... */ }
    }



    private fun showBlockConfirmationDialog(user: UserAnswDTORolesNoRev) {
        if (user.role == "BLOCKED") {
            AlertDialog.Builder(this)
                .setTitle("Разблокировка")
                .setMessage("Вы уверены, что хотите разблокировать ${user.name}?")
                .setPositiveButton("Да") { _, _ -> performUnblockUser(user.id) }
                .setNegativeButton("Нет", null)
                .show()
        } else if (user.role == "USER") {
            AlertDialog.Builder(this)
                .setTitle("Блокировка")
                .setMessage("Заблокировать ${user.name}?")
                .setPositiveButton("Да") { _, _ -> blockUser(user.id) }
                .setNegativeButton("Нет", null)
                .show()
        }
    }

    private fun performUnblockUser(userId: Long) {
        userApi.unblockUser(userId).enqueue(object : Callback<Void> {
            override fun onResponse(call: Call<Void>, response: Response<Void>) {
                if (response.isSuccessful) {
                    showToast("Пользователь разблокирован")
                    loadUsers(currentSearchText) // Обновляем список пользователей с учетом текущего поиска
                } else {
                    showToast("Ошибка разблокировки: ${response.code()}")
                }
            }
            override fun onFailure(call: Call<Void>, t: Throwable) {
                showToast("Ошибка сети при разблокировке: ${t.message}")
            }
        })
    }

    private fun blockUser(userId: Long) {
        userApi.blockUser(userId).enqueue(object : Callback<Void> {
            override fun onResponse(call: Call<Void>, response: Response<Void>) {
                if (response.isSuccessful) {
                    showToast("Пользователь заблокирован")
                    loadUsers(currentSearchText) // Обновляем список после блокировки с учетом текущего поиска
                } else {
                    showToast("Ошибка блокировки: ${response.code()}")
                }
            }
            override fun onFailure(call: Call<Void>, t: Throwable) {
                showToast("Ошибка блокировки: ${t.message}")
            }
        })
    }

    private fun showDeleteConfirmationDialog() {
        AlertDialog.Builder(this)
            .setTitle("Удаление аккаунта")
            .setMessage("Вы уверены? Это действие нельзя отменить!")
            .setPositiveButton("Да") { _, _ -> deleteAccount() }
            .setNegativeButton("Нет", null)
            .show()
    }

    private fun deleteAccount() {
        getUserByName { userData -> // Получаем ID текущего пользователя
            if (userData != null) {
                val userId = userData.id
                userApi.removeUser(userId).enqueue(object : Callback<Void> {
                    override fun onResponse(call: Call<Void>, response: Response<Void>) {
                        if (response.isSuccessful) {
                            showToast("Аккаунт успешно удален")
                            logout() // Выходим и перенаправляем
                        } else {
                            showToast("Ошибка удаления: ${response.code()}")
                        }
                    }
                    override fun onFailure(call: Call<Void>, t: Throwable) {
                        showToast("Ошибка удаления: ${t.message}")
                    }
                })
            } else {
                showToast("Не удалось получить ID пользователя для удаления")
            }
        }
    }

    private fun showLogoutConfirmationDialog() {
        AlertDialog.Builder(this)
            .setTitle("Выход")
            .setMessage("Вы уверены что хотите выйти?")
            .setPositiveButton("Да") { _, _ -> logout() }
            .setNegativeButton("Нет", null)
            .show()
    }

    private fun logout() {
        clearTokens()
        navigateTo(StartActivity::class.java) // Или на экран логина
    }
    fun clearTokens() { // Сделаем публичным, если нужно вызывать извне
        val prefs = getSharedPreferences("secure_prefs", Context.MODE_PRIVATE)
        prefs.edit()
            .remove("ACCESS_TOKEN")
            .remove("REFRESH_TOKEN")
            .apply()
        Log.d("TokenDebug", "Токены успешно удалены.")
    }

    private fun navigateTo(cls: Class<*>) {
        val intent = Intent(this, cls)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK // Очищаем стек
        startActivity(intent)
        finish()
    }

    private fun redirectToLogin() {
        navigateTo(RegAuthActivity::class.java)
    }

    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }
}