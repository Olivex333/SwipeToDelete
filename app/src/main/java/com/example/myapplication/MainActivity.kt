package com.example.myapplication

import android.annotation.SuppressLint
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.draggable
import androidx.compose.foundation.gestures.rememberDraggableState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import kotlin.math.roundToInt

class UserViewModelFactory(private val userService: UserService) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(UserViewModel::class.java)) {
            return UserViewModel(userService) as T
        }
        throw IllegalArgumentException("Nieznany model klasy")
    }
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                Surface(color = MaterialTheme.colors.background) {
                    val userViewModel: UserViewModel by viewModels {
                        UserViewModelFactory(ServiceFactory.createUserService())
                    }
                    Navigation(userViewModel)
                }
            }
        }
    }
}

@SuppressLint("SuspiciousIndentation")
@Composable
fun Navigation(userViewModel: UserViewModel) {
    val navController = rememberNavController()

    NavHost(navController = navController, startDestination = "mainScreen") {
        composable("mainScreen") {
            MainScreen(navController, userViewModel)
        }
        composable("userDetails/{userId}") { backStackEntry ->
            val userId = backStackEntry.arguments?.getString("userId")
            UserDetailsScreenWithCards(userId = userId, navController, userViewModel)
        }
    }
}

@SuppressLint("UnusedMaterialScaffoldPaddingParameter")
@Composable
fun MainScreen(navController: NavHostController, userViewModel: UserViewModel) {
    var currentScreen by remember { mutableStateOf("documents") }
    val backgroundColor = if (currentScreen == "documents") Color(0xFF003568) else Color.White

    Scaffold(
        bottomBar = {
            BottomNavigation(
                backgroundColor = Color.Transparent,
                contentColor = Color(0xFF252525),
                elevation = 0.dp
            ) {
                BottomNavigationItem(
                    icon = { Icon(Icons.Filled.Description, contentDescription = "Dokumenty") },
                    label = { Text("Dokumenty") },
                    selected = currentScreen == "documents",
                    onClick = { currentScreen = "documents" },
                    selectedContentColor = Color(0xFF252525),
                    unselectedContentColor = Color.Gray
                )
                BottomNavigationItem(
                    icon = { Icon(Icons.Filled.QrCodeScanner, contentDescription = "Skanowanie RFID") },
                    label = { Text("Skanowanie RFID") },
                    selected = currentScreen == "rfid",
                    onClick = { currentScreen = "rfid" },
                    selectedContentColor = Color(0xFF252525),
                    unselectedContentColor = Color.Gray
                )
            }
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(backgroundColor)
        ) {
            when (currentScreen) {
                "documents" -> LogisticDocumentScreen(navController, userViewModel)
                "rfid" -> RfidScreen()
            }
        }
    }
}

@Composable
fun RfidScreen() {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text("rfid", style = MaterialTheme.typography.h4)
    }
}

@Composable
fun LogisticDocumentScreen(navController: NavHostController, userViewModel: UserViewModel) {
    val titles = listOf("Przyjęcia", "Wydania")
    var currentTitleIndex by remember { mutableStateOf(0) }
    var isExternalSelected by remember { mutableStateOf(false) }

    fun toggleTitle() {
        currentTitleIndex = (currentTitleIndex + 1) % titles.size
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .wrapContentHeight(align = Alignment.Top)
            .background(Color(0xFF252525))
    ) {
        Column {
            TopAppBar(
                title = {
                    Text(
                        text = titles[currentTitleIndex],
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { toggleTitle() }) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Toggle title")
                    }
                },
                actions = {
                    IconButton(onClick = { toggleTitle() }) {
                        Icon(Icons.Filled.ArrowForward, contentDescription = "Toggle title")
                    }
                },
                backgroundColor = Color.Transparent,
                contentColor = Color.White,
                elevation = 0.dp
            )
            ButtonToggleGroup(isExternalSelected = isExternalSelected, onSelectionChange = { selected ->
                isExternalSelected = selected
            })
        }
    }

    Surface(
        modifier = Modifier
            .fillMaxSize()
            .padding(top = 150.dp)
            .clip(RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp)),
        color = Color.White
    ) {
        if (!isExternalSelected) {
            MyLazyColumnList(navController, userViewModel)
        }
    }
}

@Composable
fun ButtonToggleGroup(isExternalSelected: Boolean, onSelectionChange: (Boolean) -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
            .background(Color.LightGray, RoundedCornerShape(50))
            .padding(8.dp),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        TextButton(
            onClick = { onSelectionChange(false) },
            modifier = Modifier
                .weight(1f)
                .height(48.dp),
            shape = RoundedCornerShape(50),
            colors = ButtonDefaults.textButtonColors(
                backgroundColor = if (!isExternalSelected) Color(0xFF252525) else Color.Transparent,
                contentColor = if (!isExternalSelected) Color.White else Color.DarkGray
            )
        ) {
            Text("wewnętrzne")
        }

        Spacer(Modifier.width(8.dp))

        TextButton(
            onClick = { onSelectionChange(true) },
            modifier = Modifier
                .weight(1f)
                .height(48.dp),
            shape = RoundedCornerShape(50),
            colors = ButtonDefaults.textButtonColors(
                backgroundColor = if (isExternalSelected) Color(0xFF252525) else Color.Transparent,
                contentColor = if (isExternalSelected) Color.White else Color.DarkGray
            )
        ) {
            Text("zewnętrzne")
        }
    }
}

@Composable
fun MyLazyColumnList(navController: NavHostController, userViewModel: UserViewModel) {
    val context = LocalContext.current
    val users by userViewModel.users.observeAsState(emptyList())

    var currentUserToDelete by remember { mutableStateOf<User?>(null) }
    var currentUserToApprove by remember { mutableStateOf<User?>(null) }

    LazyColumn(
        modifier = Modifier
            .padding(horizontal = 16.dp)
            .fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        contentPadding = PaddingValues(vertical = 16.dp)
    ) {
        items(users, key = { it.id }) { user ->
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable {
                        navController.navigate("userDetails/${user.id}")
                    }
            ) {
                DraggableListItem(
                    item = user.name,
                    secondaryText = "Kliknij, aby zobaczyć informacje\nPrzesuń, aby wywołać akcje",
                    maxOffsetDp = 75.dp,
                    onDismiss = {
                        currentUserToDelete = user
                    },
                    onApprove = {
                        currentUserToApprove = user
                    }
                )
            }
        }
    }

    currentUserToDelete?.let { userToDelete ->
        AlertDialog(
            onDismissRequest = { currentUserToDelete = null },
            title = { Text("Usuwanie") },
            text = { Text("Czy na pewno chcesz usunąć tego użytkownika?") },
            confirmButton = {
                Button(
                    onClick = {
                        userViewModel.deleteUser(userToDelete)
                        currentUserToDelete = null
                    }
                ) {
                    Text("Usuń")
                }
            },
            dismissButton = {
                Button(
                    onClick = { currentUserToDelete = null }
                ) {
                    Text("Anuluj")
                }
            }
        )
    }

    currentUserToApprove?.let { userToApprove ->
        AlertDialog(
            onDismissRequest = { currentUserToApprove = null },
            title = { Text("Potwierdzenie") },
            text = { Text("Czy na pewno chcesz zaakceptować tego użytkownika?") },
            confirmButton = {
                Button(
                    onClick = {
                        Toast.makeText(context, "Użytkownik zaakceptowany", Toast.LENGTH_SHORT).show()
                        userViewModel.deleteUser(userToApprove)
                        currentUserToApprove = null
                    }
                ) {
                    Text("Potwierdź")
                }
            },
            dismissButton = {
                Button(
                    onClick = { currentUserToApprove = null }
                ) {
                    Text("Anuluj")
                }
            }
        )
    }
}

@Composable
fun DraggableListItem(
    item: String,
    maxOffsetDp: Dp,
    onDismiss: () -> Unit,
    onApprove: () -> Unit,
    secondaryText: String
) {
    val maxOffsetPx = with(LocalDensity.current) { maxOffsetDp.roundToPx().toFloat() }
    var offsetPx by remember { mutableStateOf(0f) }
    val offsetX = offsetPx.coerceIn(-maxOffsetPx, maxOffsetPx)

    LaunchedEffect(key1 = item) { offsetPx = 0f }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(IntrinsicSize.Min)
    ) {
        Box(
            modifier = Modifier
                .matchParentSize()
                .clip(RoundedCornerShape(topEnd = 16.dp, bottomEnd = 16.dp))
                .background(if (offsetX < 0) Color.Red else Color.Transparent),
            contentAlignment = Alignment.CenterEnd
        ) {
            if (offsetX < 0) {
                Icon(
                    Icons.Filled.Delete,
                    contentDescription = "Delete",
                    tint = Color.White,
                    modifier = Modifier
                        .padding(horizontal = 20.dp)
                        .clickable(onClick = onDismiss)
                )
            }
        }

        Box(
            modifier = Modifier
                .matchParentSize()
                .clip(RoundedCornerShape(topStart = 16.dp, bottomStart = 16.dp))
                .background(if (offsetX > 0) Color.Green else Color.Transparent),
            contentAlignment = Alignment.CenterStart
        ) {
            if (offsetX > 0) {
                Icon(
                    Icons.Filled.Check,
                    contentDescription = "Approve",
                    tint = Color.White,
                    modifier = Modifier
                        .padding(horizontal = 20.dp)
                        .clickable(onClick = onApprove)
                )
            }
        }

        Card(
            elevation = 4.dp,
            backgroundColor = Color(0xFFF0F0F0),
            shape = RoundedCornerShape(8.dp),
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.Center)
                .offset { IntOffset(offsetX.roundToInt(), 0) }
                .draggable(
                    orientation = Orientation.Horizontal,
                    state = rememberDraggableState { delta ->
                        offsetPx += delta
                        offsetPx = offsetPx.coerceIn(-maxOffsetPx, maxOffsetPx)
                    },
                    onDragStopped = {
                        if (offsetPx != -maxOffsetPx && offsetPx != maxOffsetPx) {
                            offsetPx = 0f
                        }
                    }
                )
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    Icons.Filled.Person,
                    contentDescription = "User Icon",
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(16.dp))
                Column {
                    Text(
                        text = item,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = secondaryText,
                        fontSize = 14.sp,
                        style = MaterialTheme.typography.body2
                    )
                }
            }
        }
    }
}

@Composable
fun UserDetailsScreenWithCards(userId: String?, navController: NavHostController, userViewModel: UserViewModel) {
    val whiteColor = Color(0xFFF0F0F0)
    val darkGrayColor = Color(0xFF252525)
    val userIdInt = userId?.toIntOrNull()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Szczegóły użytkownika", color = whiteColor) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Powrót", tint = whiteColor)
                    }
                },
                backgroundColor = darkGrayColor
            )
        },
        content = { paddingValues ->
            val user = userViewModel.users.value?.find { it.id == userIdInt }
            if (user != null) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                        .padding(16.dp)
                ) {
                    UserDetailCard(
                        icon = Icons.Filled.Person,
                        title = "Imię i nazwisko",
                        content = user.name,
                        backgroundColor = whiteColor,
                        contentColor = darkGrayColor
                    )
                    UserDetailCard(
                        icon = Icons.Filled.AccountCircle,
                        title = "Username",
                        content = user.username,
                        backgroundColor = whiteColor,
                        contentColor = darkGrayColor
                    )
                    UserDetailCard(
                        icon = Icons.Filled.Email,
                        title = "Email",
                        content = user.email,
                        backgroundColor = whiteColor,
                        contentColor = darkGrayColor
                    )
                    UserDetailCard(
                        icon = Icons.Filled.LocationOn,
                        title = "Adres",
                        content = "Ulica: ${user.address.street}\nApartament: ${user.address.suite}\nMiasto: ${user.address.city}\nKod pocztowy: ${user.address.zipcode}",
                        backgroundColor = whiteColor,
                        contentColor = darkGrayColor
                    )
                    UserDetailCard(
                        icon = Icons.Filled.Phone,
                        title = "Telefon",
                        content = user.phone,
                        backgroundColor = whiteColor,
                        contentColor = darkGrayColor
                    )
                    UserDetailCard(
                        icon = Icons.Filled.Web,
                        title = "Strona internetowa",
                        content = user.website,
                        backgroundColor = whiteColor,
                        contentColor = darkGrayColor
                    )
                    UserDetailCard(
                        icon = Icons.Filled.Business,
                        title = "Firma",
                        content = "Nazwa: ${user.company.name}\nSlogan: ${user.company.catchPhrase}\nBS: ${user.company.bs}",
                        backgroundColor = whiteColor,
                        contentColor = darkGrayColor
                    )
                }
            } else {
                Text("Nie znaleziono użytkownika", fontSize = 20.sp, fontWeight = FontWeight.Bold)
            }
        }
    )
}

@Composable
fun UserDetailCard(icon: ImageVector, title: String, content: String, backgroundColor: Color, contentColor: Color) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp),
        backgroundColor = backgroundColor,
        elevation = 0.dp,
        shape = RoundedCornerShape(16.dp),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(icon, contentDescription = title, tint = contentColor)
            Spacer(modifier = Modifier.width(16.dp))
            Column {
                Text(title, fontWeight = FontWeight.Bold, color = contentColor)
                Text(content, color = contentColor)
            }
        }
    }
}
