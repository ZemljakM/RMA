@file:OptIn(ExperimentalMaterial3Api::class)

package com.example.rmaprojekt


import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import android.content.Context
import android.os.Build
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.rmaprojekt.ui.theme.RmaProjektTheme
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.sp
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsControllerCompat
import com.example.rmaprojekt.ui.theme.FontColor


class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val sharedPreferences = getSharedPreferences("user_prefs", Context.MODE_PRIVATE)
        val isLoggedIn = sharedPreferences.getBoolean("is_logged_in", false)


        setContent {
            val navController = rememberNavController()


            RmaProjektTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = Color.White
                ) {
                    NavHost(navController = navController, startDestination = if (isLoggedIn) "list_screen" else "home_screen") {
                        composable("login_screen") {
                            LoginScreen(navController = navController, context = LocalContext.current)
                        }
                        composable("register_screen") {
                            RegisterScreen(navController = navController)
                        }
                        composable("home_screen") {
                            HomeScreen(navController = navController)
                        }
                        composable("add_friend_screen") {
                            AddFriendScreen(navController = navController)
                        }
                        composable("list_screen") {
                            ListScreen(navController = navController)
                        }
                        composable("map_screen") {
                            MapScreen(navController = navController)
                        }
                        composable("profile_screen") {
                            ProfileScreen(navController = navController, context = LocalContext.current)
                        }
                    }

                }
            }
        }
    }


}

@Composable
fun LoginScreen(navController: NavHostController, context: Context) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf("") }
    var passwordVisibility by remember { mutableStateOf(false) }



    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(50.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Login",
            textAlign = TextAlign.Center,
            fontSize = 70.sp,
            color = FontColor
        )
        Spacer(modifier = Modifier.height(50.dp))
        OutlinedTextField(
            value = email,
            onValueChange = {email = it},
            label = { Text("Email", color = FontColor) },
            modifier = Modifier.fillMaxWidth(),
            colors = OutlinedTextFieldDefaults.colors(
                cursorColor = FontColor,
                focusedBorderColor = FontColor,
                unfocusedBorderColor = FontColor,
            )
        )
        Spacer(modifier = Modifier.height(8.dp))
        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            label = { Text("Password", color = FontColor) },
            modifier = Modifier.fillMaxWidth(),
            colors = OutlinedTextFieldDefaults.colors(
                cursorColor = FontColor,
                focusedBorderColor = FontColor,
                unfocusedBorderColor = FontColor,
            ),
            visualTransformation = if (passwordVisibility) VisualTransformation.None else PasswordVisualTransformation(),
            trailingIcon = {
                IconButton(onClick = { passwordVisibility = !passwordVisibility }) {
                    Icon(
                        imageVector = if (passwordVisibility) Icons.Filled.Visibility else Icons.Filled.VisibilityOff,
                        contentDescription = "Toggle password visibility"
                    )
                }
            }
        )
        Spacer(modifier = Modifier.height(50.dp))
        Button(
            onClick = {
                if (email.isNotEmpty() && password.isNotEmpty()) {
                    FirebaseAuth.getInstance().signInWithEmailAndPassword(email, password)
                        .addOnCompleteListener { task ->
                            if (task.isSuccessful) {
                                val sharedPreferences = context.getSharedPreferences("user_prefs", Context.MODE_PRIVATE)
                                sharedPreferences.edit().putBoolean("is_logged_in", true).apply()
                                navController.navigate("list_screen")
                            } else {
                                errorMessage = task.exception?.message ?: "Unknown error"
                            }
                        }
                }
            },
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(
                containerColor = FontColor,
                contentColor = Color.White
            )
        ) {
            Text("Login")
        }
        Spacer(modifier = Modifier.height(8.dp))
        Button(
            onClick = { navController.navigate("register_screen") },
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(
                contentColor = FontColor,
                containerColor = Color.White
            )
        ) {
            Text("Register")
        }
        if (errorMessage.isNotEmpty()) {
            Text(text = errorMessage, color = MaterialTheme.colorScheme.error)
        }
    }
}


@Composable
fun RegisterScreen(navController: NavHostController) {
    var email by remember { mutableStateOf("") }
    var username by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf("") }
    var passwordVisibility by remember { mutableStateOf(false) }
    val db = FirebaseFirestore.getInstance()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(50.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Register",
            textAlign = TextAlign.Center,
            fontSize = 70.sp,
            color = FontColor
        )
        Spacer(modifier = Modifier.height(50.dp))
        OutlinedTextField(
            value = email,
            onValueChange = {email = it},
            label = { Text("Email", color = FontColor) },
            modifier = Modifier.fillMaxWidth(),
            colors = OutlinedTextFieldDefaults.colors(
                cursorColor = FontColor,
                focusedBorderColor = FontColor,
                unfocusedBorderColor = FontColor,
            )
        )
        Spacer(modifier = Modifier.height(8.dp))
        OutlinedTextField(
            value = username,
            onValueChange = {username = it},
            label = { Text("Username", color = FontColor) },
            modifier = Modifier.fillMaxWidth(),
            colors = OutlinedTextFieldDefaults.colors(
                cursorColor = FontColor,
                focusedBorderColor = FontColor,
                unfocusedBorderColor = FontColor,
            )
        )
        Spacer(modifier = Modifier.height(8.dp))
        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            label = { Text("Password", color = FontColor) },
            modifier = Modifier.fillMaxWidth(),
            colors = OutlinedTextFieldDefaults.colors(
                cursorColor = FontColor,
                focusedBorderColor = FontColor,
                unfocusedBorderColor = FontColor,
            ),
            visualTransformation = if (passwordVisibility) VisualTransformation.None else PasswordVisualTransformation(),
            trailingIcon = {
                IconButton(onClick = { passwordVisibility = !passwordVisibility }) {
                    Icon(
                        imageVector = if (passwordVisibility) Icons.Filled.Visibility else Icons.Filled.VisibilityOff,
                        contentDescription = "Toggle password visibility"
                    )
                }
            }
        )
        Spacer(modifier = Modifier.height(50.dp))
        Button(
            onClick = {
                if (email.isNotEmpty() && password.isNotEmpty() && username.isNotEmpty()) {
                    FirebaseAuth.getInstance().createUserWithEmailAndPassword(email, password)
                        .addOnCompleteListener { task ->
                            if (task.isSuccessful) {
                                val user = FirebaseAuth.getInstance().currentUser
                                user?.let {
                                    val userId = it.uid
                                    val userMap = hashMapOf(
                                        "username" to username,
                                        "sent" to emptyList<String>(),
                                        "received" to emptyList<String>(),
                                        "friends" to emptyList<String>()
                                    )
                                    db.collection("users").document(userId)
                                        .set(userMap)
                                        .addOnSuccessListener {
                                            navController.navigate("list_screen")
                                        }
                                        .addOnFailureListener{e ->
                                            errorMessage = e.message ?: "Error saving user data"
                                        }
                                }
                            } else {
                                errorMessage = task.exception?.message ?: "Unknown error"
                            }
                        }
                } else {
                    errorMessage = "Email and password must not be empty"
                }

            },
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(
                contentColor = Color.White,
                containerColor = FontColor
            )
        ) {
            Text("Register")
        }
        Spacer(modifier = Modifier.height(8.dp))
        TextButton(
            onClick = { navController.navigate("login_screen") },
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(
                contentColor = FontColor,
                containerColor = Color.White
            )
        ) {
            Text("Back to Login")
        }
        if (errorMessage.isNotEmpty()) {
            Text(text = errorMessage, color = MaterialTheme.colorScheme.error)
        }
    }
}


@Composable
fun HomeScreen(navController: NavHostController) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(50.dp),
        verticalArrangement = Arrangement.Top,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Groceries",
            textAlign = TextAlign.Center,
            fontSize = 80.sp,
            color = FontColor
        )
        Text(
            text = "Tracker",
            textAlign = TextAlign.Center,
            fontSize = 80.sp,
            color = FontColor
        )
        Spacer(modifier = Modifier.height(80.dp))
        Image(
            painter = painterResource(id = R.drawable.groceries),
            contentDescription = "Logo",
            modifier = Modifier.height(200.dp)
        )
        Spacer(modifier = Modifier.height(80.dp))
        Button(
            onClick = { navController.navigate("login_screen") },
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 32.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = FontColor,
                contentColor = Color.White
            )
        ) {
            Text(
                text = "Login",
                fontSize = 20.sp
            )
        }
        Spacer(modifier = Modifier.height(16.dp))
        Button(
            onClick = { navController.navigate("register_screen") },
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 32.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = FontColor,
                contentColor = Color.White
            )
        ) {
            Text(
                text = "Register",
                fontSize = 20.sp
            )
        }
    }
}

@Composable
fun BottomNavigationBar(navController: NavHostController) {
    val items = listOf(
        BottomNavItem("Add Friend", Icons.Default.PersonAdd, "add_friend_screen"),
        BottomNavItem("Lists", Icons.Default.List, "list_screen"),
        BottomNavItem("Map", Icons.Default.Map, "map_screen"),
        BottomNavItem("Profile", Icons.Default.Person, "profile_screen")
    )

    NavigationBar {
        items.forEach { item ->
            NavigationBarItem(
                icon = { Icon(item.icon, contentDescription = item.title) },
                label = { Text(item.title) },
                selected = false,
                onClick = { navController.navigate(item.route) }
            )
        }
    }
}

data class BottomNavItem(val title: String, val icon: ImageVector, val route: String)

@Composable
fun AddFriendScreen(navController: NavHostController) {
    var friendUsername by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf("") }
    var successMessage by remember { mutableStateOf("") }
    val db = FirebaseFirestore.getInstance()
    val currentUser = FirebaseAuth.getInstance().currentUser
    val currentUserId = currentUser?.uid ?: ""
    var receivedRequests by remember { mutableStateOf(listOf<Pair<String, String>>()) }
    var sentRequests by remember { mutableStateOf(listOf<Pair<String, String>>()) }


    LaunchedEffect(Unit){
        if(currentUserId.isNotEmpty()){
            db.collection("users").document(currentUserId)
                .get()
                .addOnSuccessListener { document ->
                    val received = document.get("received") as? List<String> ?: emptyList()
                    received.forEach { senderId ->
                        db.collection("users").document(senderId)
                            .get()
                            .addOnSuccessListener { senderDocument ->
                                val senderUsername = senderDocument.getString("username")
                                if (senderUsername != null) {
                                    receivedRequests = receivedRequests + Pair(senderId, senderUsername)
                                }
                            }
                    }
                    val sent = document.get("sent") as? List<String> ?: emptyList()
                    sent.forEach { receiverId ->
                        db.collection("users").document(receiverId)
                            .get()
                            .addOnSuccessListener { receiverDocument ->
                                val receiverUsername = receiverDocument.getString("username")
                                if (receiverUsername != null) {
                                    sentRequests = sentRequests + Pair(receiverId, receiverUsername)
                                }
                            }
                    }
                }
        }
    }

    Scaffold(
        bottomBar = { BottomNavigationBar(navController) }
    ) {
        Column (
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.Center
        ) {
            TextField(
                value = friendUsername,
                onValueChange = { friendUsername = it },
                label = { Text("Enter friend's username") },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(8.dp))
            Button(
                onClick = {
                    if (friendUsername.isNotEmpty()) {
                        db.collection("users")
                            .whereEqualTo("username", friendUsername)
                            .get()
                            .addOnSuccessListener { documents ->
                                if (documents.isEmpty) {
                                    errorMessage = "User not found"
                                } else {
                                    val friendId = documents.documents.first().id
                                    val sentUpdate = hashMapOf<String, Any>(
                                        "sent.$friendId" to false
                                    )
                                    db.collection("users").document(currentUserId)
                                        .update("sent", FieldValue.arrayUnion(friendId))
                                        .addOnSuccessListener {
                                            db.collection("users").document(friendId)
                                                .update(
                                                    "received",
                                                    FieldValue.arrayUnion(currentUserId)
                                                )
                                                .addOnSuccessListener {
                                                    successMessage = "Friend request sent"
                                                    sentRequests = sentRequests + Pair(
                                                        friendId,
                                                        friendUsername
                                                    )
                                                }
                                                .addOnFailureListener { e ->
                                                    errorMessage = e.message
                                                        ?: "Error updating received requests"
                                                }
                                        }
                                        .addOnFailureListener { e ->
                                            errorMessage =
                                                e.message ?: "Error updating sent requests"
                                        }
                                }
                            }
                            .addOnFailureListener { e ->
                                errorMessage = e.message ?: "Error fetching user"
                            }
                    } else {
                        errorMessage = "Friend username must not be empty"
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Send request")
            }

            if (receivedRequests.isNotEmpty()) {
                Text("Friend Requests")
                receivedRequests.forEach { (userId, username) ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(text = username)
                        Button(
                            onClick = {
                                db.collection("users").document(currentUserId)
                                    .update("friends", FieldValue.arrayUnion(userId))
                                    .addOnSuccessListener {
                                        db.collection("users").document(userId)
                                            .update("friends", FieldValue.arrayUnion(currentUserId))
                                            .addOnSuccessListener {
                                                db.collection("users").document(userId)
                                                    .update(
                                                        "sent",
                                                        FieldValue.arrayRemove(currentUserId)
                                                    )
                                                    .addOnSuccessListener {
                                                        db.collection("users")
                                                            .document(currentUserId)
                                                            .update(
                                                                "received",
                                                                FieldValue.arrayRemove(userId)
                                                            )
                                                            .addOnSuccessListener {
                                                                receivedRequests =
                                                                    receivedRequests.filter { it.first != userId }
                                                                successMessage =
                                                                    "Friend request accepted"
                                                            }
                                                            .addOnFailureListener { e ->
                                                                errorMessage = e.message
                                                                    ?: "Error updating received requests"
                                                            }
                                                    }
                                                    .addOnFailureListener { e ->
                                                        errorMessage =
                                                            e.message
                                                                ?: "Error updating sent requests"
                                                    }
                                            }
                                            .addOnFailureListener { e ->
                                                errorMessage =
                                                    e.message
                                                        ?: "Error updating friend's friends list"
                                            }
                                    }
                                    .addOnFailureListener { e ->
                                        errorMessage = e.message ?: "Error updating friends list"
                                    }
                            }
                        ) {
                            Text("Accept")
                        }
                        Button(
                            onClick = {
                                db.collection("users").document(currentUserId)
                                    .update("received", FieldValue.arrayRemove(userId))
                                    .addOnSuccessListener {
                                        db.collection("users").document(userId)
                                            .update("sent", FieldValue.arrayRemove(currentUserId))
                                            .addOnSuccessListener {
                                                receivedRequests =
                                                    receivedRequests.filter { it.first != userId }
                                                successMessage = "Request successfully removed"
                                            }
                                            .addOnFailureListener { e ->
                                                errorMessage = e.message ?: "Error removing request"
                                            }
                                    }
                                    .addOnFailureListener { e ->
                                        errorMessage =
                                            e.message ?: "Error removing request from sender list"
                                    }
                            }
                        ) {
                            Text("Remove")
                        }
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                }
            }

            if (sentRequests.isNotEmpty()) {
                Spacer(modifier = Modifier.height(16.dp))
                Text("Sent Requests")
                sentRequests.forEach { (userId, username) ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(text = username)
                        Button(
                            onClick = {
                                db.collection("users").document(userId)
                                    .update("received", FieldValue.arrayRemove(currentUserId))
                                    .addOnSuccessListener {
                                        db.collection("users").document(currentUserId)
                                            .update("sent", FieldValue.arrayRemove(userId))
                                            .addOnSuccessListener {
                                                sentRequests =
                                                    sentRequests.filter { it.first != userId }
                                                successMessage = "Request successfully removed"
                                            }
                                            .addOnFailureListener { e ->
                                                errorMessage = e.message ?: "Error removing request"
                                            }
                                    }
                                    .addOnFailureListener { e ->
                                        errorMessage =
                                            e.message ?: "Error removing request from receiver list"
                                    }
                            }
                        ) {
                            Text("Remove")
                        }
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                }
            }

            if (successMessage.isNotEmpty()) {
                Text(text = successMessage, color = MaterialTheme.colorScheme.primary)
            }
            if (errorMessage.isNotEmpty()) {
                Text(text = errorMessage, color = MaterialTheme.colorScheme.error)
            }
        }
    }
}

@Composable
fun ListScreen(navController: NavHostController) {
    Scaffold(
        bottomBar = { BottomNavigationBar(navController) }
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(it),
            contentAlignment = Alignment.Center
        ) {
            Text(text = "List Screen")
        }
    }
}


@Composable
fun MapScreen(navController: NavHostController) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Text(text = "Map Screen")
    }
}

@Composable
fun ProfileScreen(navController: NavHostController, context: Context) {
    val auth = FirebaseAuth.getInstance()
    val currentUser = auth.currentUser
    val db = FirebaseFirestore.getInstance()

    var username by remember { mutableStateOf("") }
    var email by remember { mutableStateOf(currentUser?.email ?: "") }
    var friends by remember { mutableStateOf(listOf<String>()) }
    var errorMessage by remember { mutableStateOf("") }
    var friendUsernames = remember { mutableStateListOf<String>() }

    LaunchedEffect(Unit) {
        currentUser?.uid?.let { userId ->
            db.collection("users").document(userId)
                .get()
                .addOnSuccessListener { document ->
                    username = document.getString("username") ?: ""
                    friends = document.get("friends") as? List<String> ?: emptyList()
                    friends.forEach { friendId ->
                        db.collection("users").document(friendId)
                            .get()
                            .addOnSuccessListener { friendDocument ->
                                val friendUsername = friendDocument.getString("username")
                                if(friendUsername != null){
                                    friendUsernames.add(friendUsername)
                                }
                            }
                            .addOnFailureListener { e ->
                                errorMessage = e.message ?: "Error fetching friend data"
                            }
                    }
                }
                .addOnFailureListener { e ->
                    errorMessage = e.message ?: "Error fetching user data"
                }
        }
    }

    Scaffold(
        bottomBar = { BottomNavigationBar(navController) }
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.Top,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(text = "Profile")
            Spacer(modifier = Modifier.height(16.dp))
            Text(text = "Username: $username")
            Text(text = "Email: $email")
            Spacer(modifier = Modifier.height(16.dp))
            Text(text = "Friends")
            friendUsernames.forEach { friend ->
                Text(text = friend)
            }
            Spacer(modifier = Modifier.height(16.dp))
            Button(onClick = {
                auth.signOut()
                val sharedPreferences = context.getSharedPreferences("user_prefs", Context.MODE_PRIVATE)
                sharedPreferences.edit().remove("is_logged_in").apply()
                navController.navigate("home_screen") {
                    popUpTo(navController.graph.startDestinationId) { inclusive = true }
                }
            }) {
                Text("Logout")
            }
            if (errorMessage.isNotEmpty()) {
                Spacer(modifier = Modifier.height(16.dp))
                Text(text = errorMessage, color = MaterialTheme.colorScheme.error)
            }
        }
    }
}


