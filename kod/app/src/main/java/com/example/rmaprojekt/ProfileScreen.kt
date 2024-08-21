package com.example.rmaprojekt

import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.example.rmaprojekt.ui.theme.FirstColor
import com.example.rmaprojekt.ui.theme.FourthColor
import com.example.rmaprojekt.ui.theme.SecondColor
import com.example.rmaprojekt.ui.theme.ThirdColor
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore


@Composable
fun ProfileScreen(navController: NavHostController, context: Context) {
    val auth = FirebaseAuth.getInstance()
    val currentUser = auth.currentUser
    val db = FirebaseFirestore.getInstance()

    var username by remember { mutableStateOf("") }
    var email by remember { mutableStateOf(currentUser?.email ?: "") }
    var friends by remember { mutableStateOf(listOf<String>()) }
    var errorMessage by remember { mutableStateOf("") }
    var successMessage by remember { mutableStateOf("") }
    var friendUsernames = remember { mutableStateListOf<String>() }
    var currentUserId = currentUser?.uid ?: ""

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
    ) { paddingValues ->
        Surface(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            color = SecondColor
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(20.dp),
                verticalArrangement = Arrangement.Top,
                horizontalAlignment = Alignment.Start
            ) {
                Text(
                    text = "Profile",
                    textAlign = TextAlign.Left,
                    fontSize = 60.sp,
                    color = FirstColor,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(40.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = "$username",
                        textAlign = TextAlign.Center,
                        fontSize = 30.sp,
                        color = FirstColor,
                        fontWeight = FontWeight.SemiBold
                    )
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = "$email",
                        textAlign = TextAlign.Center,
                        fontSize = 20.sp,
                        color = FirstColor,
                        fontWeight = FontWeight.SemiBold
                    )
                }
                Spacer(modifier = Modifier.height(30.dp))
                if(friendUsernames.isNotEmpty()){
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(FourthColor, shape = RoundedCornerShape(12.dp))
                            .padding(start = 16.dp, top = 12.dp, bottom = 12.dp)
                    ) {
                        Column(
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = "Friends",
                                color = FirstColor,
                                fontSize = 20.sp
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            friendUsernames.forEach { friend ->
                                Row(
                                    modifier = Modifier.fillMaxWidth(0.95f),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .weight(0.5f)
                                            .padding(start = 16.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = "•   ",
                                            color = FirstColor,
                                            fontSize = 20.sp
                                        )
                                        Text(
                                            text = friend,
                                            color = FirstColor,
                                            fontSize = 20.sp
                                        )
                                    }
                                    Button(
                                        onClick = {
                                            if (friend.isNotEmpty()) {
                                                db.collection("users")
                                                    .whereEqualTo("username", friend)
                                                    .get()
                                                    .addOnSuccessListener { documents ->
                                                        if (documents.isEmpty) {
                                                            errorMessage = "Error finding user"
                                                            Toast.makeText(context, errorMessage, Toast.LENGTH_SHORT).show()
                                                        } else {
                                                            val friendId = documents.documents.first().id
                                                            db.collection("users")
                                                                .document(friendId)
                                                                .update("friends", FieldValue.arrayRemove(currentUserId))
                                                                .addOnSuccessListener {
                                                                    db.collection("users")
                                                                        .document(currentUserId)
                                                                        .update("friends", FieldValue.arrayRemove(friendId))
                                                                        .addOnSuccessListener {
                                                                            friendUsernames.remove(friend)
                                                                            successMessage = "Friend successfully removed"
                                                                            Toast.makeText(context, successMessage, Toast.LENGTH_SHORT).show()
                                                                        }
                                                                        .addOnFailureListener { e ->
                                                                            errorMessage = e.message ?: "Error removing friend"
                                                                            Toast.makeText(context, errorMessage, Toast.LENGTH_SHORT).show()
                                                                        }
                                                                }
                                                                .addOnFailureListener { e ->
                                                                    errorMessage = e.message ?: "Error removing you as a friend"
                                                                    Toast.makeText(context, errorMessage, Toast.LENGTH_SHORT).show()
                                                                }
                                                        }
                                                    }
                                            }
                                        },
                                        shape = RoundedCornerShape(12.dp),
                                        modifier = Modifier
                                            .padding(end = 4.dp)
                                            .width(45.dp)
                                            .height(30.dp),
                                        contentPadding = PaddingValues(0.dp),
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = Color.Transparent,
                                        )
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Delete,
                                            contentDescription = "Remove friend",
                                            tint = ThirdColor,
                                            modifier = Modifier.size(22.dp)
                                        )
                                    }
                                }
                                Spacer(modifier = Modifier.height(8.dp))
                            }
                        }
                    }


                }

                Spacer(modifier = Modifier.height(16.dp))
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.BottomEnd
                ) {
                    Button(
                        onClick = {
                            auth.signOut()
                            val sharedPreferences = context.getSharedPreferences("user_prefs", Context.MODE_PRIVATE)
                            sharedPreferences.edit().remove("is_logged_in").apply()
                            navController.navigate("home_screen") {
                                popUpTo(navController.graph.startDestinationId) { inclusive = true }
                            }
                        },
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth(0.3f),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = ThirdColor,
                            contentColor = Color.White
                        )
                    ) {
                        Text(
                            text = "Logout",
                            fontSize = 17.sp,
                            fontFamily = FontFamily(Font(R.font.playfairdisplay))
                        )
                    }
                }
            }
        }
    }
}


