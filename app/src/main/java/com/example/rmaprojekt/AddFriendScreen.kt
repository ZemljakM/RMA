package com.example.rmaprojekt

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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
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
fun AddFriendScreen(navController: NavHostController) {
    var friendUsername by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf("") }
    var successMessage by remember { mutableStateOf("") }
    val db = FirebaseFirestore.getInstance()
    var currentUser = FirebaseAuth.getInstance().currentUser
    var currentUserId = currentUser?.uid ?: ""
    var receivedRequests by remember { mutableStateOf(listOf<Pair<String, String>>()) }
    var sentRequests by remember { mutableStateOf(listOf<Pair<String, String>>()) }

    val context = LocalContext.current


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
    ) { paddingValues ->
        Surface(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            color = SecondColor
        ){
            Column (
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.Start
            ) {
                Text(
                    text = "Friends",
                    textAlign = TextAlign.Left,
                    fontSize = 60.sp,
                    color = FirstColor,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(20.dp))
                OutlinedTextField(
                    value = friendUsername,
                    onValueChange = {friendUsername = it },
                    label = { Text("Enter friend's username", color = FirstColor, fontSize = 18.sp) },
                    textStyle = TextStyle(color = FirstColor, fontSize = 18.sp, fontWeight = FontWeight.Bold),
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        cursorColor = FirstColor,
                        focusedBorderColor = FirstColor,
                        unfocusedBorderColor = FirstColor,
                        focusedContainerColor = FourthColor,
                        unfocusedContainerColor = FourthColor
                    )
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
                                        Toast.makeText(context, errorMessage, Toast.LENGTH_SHORT).show()
                                    } else {
                                        val friendId = documents.documents.first().id
                                        db.collection("users").document(currentUserId)
                                            .get()
                                            .addOnSuccessListener { document ->
                                                val userFriends = document.get("friends") as? List<String> ?: emptyList()
                                                val userSentRequests = document.get("sent") as? List<String> ?: emptyList()
                                                val userReceivedRequests = document.get("received") as? List<String> ?: emptyList()
                                                if(userFriends.contains(friendId)){
                                                    errorMessage = "This user is already your friend"
                                                    Toast.makeText(context, errorMessage, Toast.LENGTH_SHORT).show()
                                                }
                                                else if(friendId == currentUserId){
                                                    errorMessage = "You can't add yourself as a friend"
                                                    Toast.makeText(context, errorMessage, Toast.LENGTH_SHORT).show()
                                                }
                                                else if(userSentRequests.contains(friendId)){
                                                    errorMessage = "You have already sent a request to that user"
                                                    Toast.makeText(context, errorMessage, Toast.LENGTH_SHORT).show()
                                                }
                                                else if(userReceivedRequests.contains(friendId)){
                                                    errorMessage = "The requested user has already sent you a request"
                                                    Toast.makeText(context, errorMessage, Toast.LENGTH_SHORT).show()
                                                }
                                                else{
                                                    db.collection("users").document(currentUserId)
                                                        .update("sent", FieldValue.arrayUnion(friendId))
                                                        .addOnSuccessListener {
                                                            db.collection("users").document(friendId)
                                                                .update("received", FieldValue.arrayUnion(currentUserId))
                                                                .addOnSuccessListener {
                                                                    successMessage = "Friend request sent"
                                                                    Toast.makeText(context, successMessage, Toast.LENGTH_SHORT).show()
                                                                    sentRequests = sentRequests + Pair(friendId, friendUsername)
                                                                }
                                                                .addOnFailureListener { e ->
                                                                    errorMessage = e.message ?: "Error updating received requests"
                                                                    Toast.makeText(context, errorMessage, Toast.LENGTH_SHORT).show()
                                                                }
                                                        }
                                                        .addOnFailureListener { e ->
                                                            errorMessage = e.message ?: "Error updating sent requests"
                                                            Toast.makeText(context, errorMessage, Toast.LENGTH_SHORT).show()
                                                        }
                                                }
                                            }
                                            .addOnFailureListener { e ->
                                                errorMessage = e.message ?: "Error fetching user data"
                                                Toast.makeText(context, errorMessage, Toast.LENGTH_SHORT).show()
                                            }
                                    }
                                }
                                .addOnFailureListener { e ->
                                    errorMessage = e.message ?: "Error fetching user"
                                    Toast.makeText(context, errorMessage, Toast.LENGTH_SHORT).show()
                                }
                        } else {
                            errorMessage = "Friend username must not be empty"
                            Toast.makeText(context, errorMessage, Toast.LENGTH_SHORT).show()
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = ThirdColor,
                        contentColor = Color.White
                    )
                ) {
                    Text(
                        text = "Send request",
                        fontSize = 17.sp,
                        fontFamily = FontFamily(Font(R.font.playfairdisplay))
                    )
                }

                Spacer(modifier = Modifier.height(25.dp))
                if (receivedRequests.isNotEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(0.25f)
                            .background(FourthColor, shape = RoundedCornerShape(12.dp))
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .verticalScroll(rememberScrollState())
                                .padding(start = 16.dp, top = 12.dp)
                        ) {
                            Text(
                                text = "Friend Requests",
                                color = FirstColor,
                                fontSize = 20.sp
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            receivedRequests.forEach { (userId, username) ->
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
                                            text = username,
                                            color = FirstColor,
                                            fontSize = 20.sp
                                        )
                                    }
                                    Row(
                                        modifier = Modifier.weight(0.5f),
                                        horizontalArrangement = Arrangement.End
                                    ) {
                                        Button(
                                            onClick = {
                                                db.collection("users").document(currentUserId)
                                                    .update("friends", FieldValue.arrayUnion(userId))
                                                    .addOnSuccessListener {
                                                        db.collection("users").document(userId)
                                                            .update("friends", FieldValue.arrayUnion(currentUserId))
                                                            .addOnSuccessListener {
                                                                db.collection("users")
                                                                    .document(userId)
                                                                    .update("sent", FieldValue.arrayRemove(currentUserId))
                                                                    .addOnSuccessListener {
                                                                        db.collection("users")
                                                                            .document(currentUserId)
                                                                            .update("received", FieldValue.arrayRemove(userId))
                                                                            .addOnSuccessListener {
                                                                                receivedRequests = receivedRequests.filter { it.first != userId }
                                                                                successMessage = "Friend request accepted"
                                                                                Toast.makeText(context, successMessage, Toast.LENGTH_SHORT).show()
                                                                            }
                                                                            .addOnFailureListener { e ->
                                                                                errorMessage = e.message ?: "Error updating received requests"
                                                                                Toast.makeText(context, errorMessage, Toast.LENGTH_SHORT).show()
                                                                            }
                                                                    }
                                                                    .addOnFailureListener { e ->
                                                                        errorMessage = e.message ?: "Error updating sent requests"
                                                                        Toast.makeText(context, errorMessage, Toast.LENGTH_SHORT).show()
                                                                    }
                                                            }
                                                            .addOnFailureListener { e ->
                                                                errorMessage = e.message ?: "Error updating friend's friends list"
                                                                Toast.makeText(context, errorMessage, Toast.LENGTH_SHORT).show()
                                                            }
                                                    }
                                                    .addOnFailureListener { e ->
                                                        errorMessage = e.message ?: "Error updating friends list"
                                                        Toast.makeText(context, errorMessage, Toast.LENGTH_SHORT).show()
                                                    }
                                            },
                                            shape = RoundedCornerShape(12.dp),
                                            modifier = Modifier
                                                .padding(end = 4.dp)
                                                .width(45.dp)
                                                .height(30.dp),
                                            contentPadding = PaddingValues(0.dp),
                                            colors = ButtonDefaults.buttonColors(
                                                containerColor = ThirdColor
                                            )
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Check,
                                                contentDescription = "Accept",
                                                tint = SecondColor,
                                                modifier = Modifier.size(15.dp)
                                            )
                                        }
                                        Button(
                                            onClick = {
                                                db.collection("users").document(currentUserId)
                                                    .update("received", FieldValue.arrayRemove(userId))
                                                    .addOnSuccessListener {
                                                        db.collection("users").document(userId)
                                                            .update("sent", FieldValue.arrayRemove(currentUserId))
                                                            .addOnSuccessListener {
                                                                receivedRequests = receivedRequests.filter { it.first != userId }
                                                                successMessage = "Request successfully removed"
                                                                Toast.makeText(context, successMessage, Toast.LENGTH_SHORT).show()
                                                            }
                                                            .addOnFailureListener { e ->
                                                                errorMessage = e.message ?: "Error removing request"
                                                                Toast.makeText(context, errorMessage, Toast.LENGTH_SHORT).show()
                                                            }
                                                    }
                                                    .addOnFailureListener { e ->
                                                        errorMessage = e.message ?: "Error removing request from sender list"
                                                        Toast.makeText(context, errorMessage, Toast.LENGTH_SHORT).show()
                                                    }
                                            },
                                            shape = RoundedCornerShape(12.dp),
                                            modifier = Modifier
                                                .padding(end = 4.dp)
                                                .width(45.dp)
                                                .height(30.dp),
                                            contentPadding = PaddingValues(0.dp),
                                            colors = ButtonDefaults.buttonColors(
                                                containerColor = ThirdColor
                                            )
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Delete,
                                                contentDescription = "Remove",
                                                tint = SecondColor,
                                                modifier = Modifier.size(15.dp)
                                            )
                                        }
                                    }
                                }
                                Spacer(modifier = Modifier.height(8.dp))
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                if (sentRequests.isNotEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(0.25f)
                            .background(FourthColor, shape = RoundedCornerShape(12.dp))
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .verticalScroll(rememberScrollState())
                                .padding(start = 16.dp, top = 12.dp)
                        ) {
                            Text(
                                text = "Sent Requests",
                                color = FirstColor,
                                fontSize = 20.sp
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            sentRequests.forEach { (userId, username) ->
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
                                            text = username,
                                            color = FirstColor,
                                            fontSize = 20.sp
                                        )
                                    }
                                    Button(
                                        onClick = {
                                            db.collection("users").document(userId)
                                                .update("received", FieldValue.arrayRemove(currentUserId))
                                                .addOnSuccessListener {
                                                    db.collection("users").document(currentUserId)
                                                        .update("sent", FieldValue.arrayRemove(userId))
                                                        .addOnSuccessListener {
                                                            sentRequests = sentRequests.filter { it.first != userId }
                                                            successMessage = "Request successfully removed"
                                                            Toast.makeText(context, successMessage, Toast.LENGTH_SHORT).show()
                                                        }
                                                        .addOnFailureListener { e ->
                                                            errorMessage = e.message ?: "Error removing request"
                                                            Toast.makeText(context, errorMessage, Toast.LENGTH_SHORT).show()
                                                        }
                                                }
                                                .addOnFailureListener { e ->
                                                    errorMessage = e.message ?: "Error removing request from receiver list"
                                                    Toast.makeText(context, errorMessage, Toast.LENGTH_SHORT).show()
                                                }
                                        },
                                        shape = RoundedCornerShape(12.dp),
                                        modifier = Modifier
                                            .padding(end = 4.dp)
                                            .width(45.dp)
                                            .height(30.dp),
                                        contentPadding = PaddingValues(0.dp),
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = ThirdColor,
                                        )
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Delete,
                                            contentDescription = "Remove",
                                            tint = SecondColor,
                                            modifier = Modifier.size(15.dp)
                                        )
                                    }
                                }
                                Spacer(modifier = Modifier.height(8.dp))
                            }
                        }
                    }
                }
            }
        }
    }
}