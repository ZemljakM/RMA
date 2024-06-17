package com.example.rmaprojekt

import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
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
fun CreateListScreen(navController: NavHostController, context: Context) {
    var listName by remember { mutableStateOf("") }
    var itemName by remember { mutableStateOf("") }
    var itemQuantity by remember { mutableStateOf("") }
    var itemList by remember { mutableStateOf(listOf<Pair<String, String>>()) }
    val auth = FirebaseAuth.getInstance()
    val currentUser = auth.currentUser
    var currentUserId = currentUser?.uid ?: ""
    val db = FirebaseFirestore.getInstance()
    var friends by remember { mutableStateOf(listOf<Pair<String, String>>()) }
    val participants = remember { mutableStateListOf<String>() }
    var errorMessage by remember { mutableStateOf("") }

    if (participants.isEmpty()) {
        db.collection("users").document(currentUserId)
            .get()
            .addOnSuccessListener { document ->
                val currentUser = document.getString("username")
                if (currentUser != null && !participants.contains(currentUser)) {
                    participants.add(currentUser)
                }
            }
    }

    LaunchedEffect(Unit){
        if(currentUserId.isNotEmpty()){
            db.collection("users").document(currentUserId)
                .get()
                .addOnSuccessListener { document ->
                    val friendsListId = document.get("friends") as? List<String> ?: emptyList()
                    friendsListId.forEach { friendId ->
                        db.collection("users").document(friendId)
                            .get()
                            .addOnSuccessListener { friendDocument ->
                                val friendUsername = friendDocument.getString("username")
                                if (friendUsername != null) {
                                    friends = friends + Pair(friendId, friendUsername)
                                }
                            }
                    }
                }
        }
    }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = SecondColor
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(20.dp),
            verticalArrangement = Arrangement.Top,
            horizontalAlignment = Alignment.Start
        ) {
            item {
                IconButton(
                    onClick = {
                        navController.popBackStack()
                    },
                ) {
                    Icon(
                        imageVector = Icons.Default.ArrowBack,
                        contentDescription = "Back",
                        modifier = Modifier.size(30.dp)
                    )
                }
                Text(
                    text = "New List",
                    textAlign = TextAlign.Left,
                    fontSize = 60.sp,
                    color = FirstColor,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily(Font(R.font.playfairdisplay))
                )
                Spacer(modifier = Modifier.height(16.dp))
                OutlinedTextField(
                    value = listName,
                    onValueChange = { listName = it },
                    label = { Text("List name", color = FirstColor, fontSize = 18.sp, fontFamily = FontFamily(Font(R.font.playfairdisplay))) },
                    textStyle = TextStyle(color = FirstColor, fontSize = 18.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily(Font(R.font.playfairdisplay))),
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
                Spacer(modifier = Modifier.height(16.dp))
            }

            item {
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
                            fontSize = 20.sp,
                            fontFamily = FontFamily(Font(R.font.playfairdisplay))
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        friends.forEach { (_, username) ->
                            var isSelected by remember { mutableStateOf(false) }

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
                                        fontSize = 20.sp,
                                        fontFamily = FontFamily(Font(R.font.playfairdisplay))
                                    )
                                    Text(
                                        text = username,
                                        color = FirstColor,
                                        fontSize = 20.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        fontFamily = FontFamily(Font(R.font.playfairdisplay))
                                    )
                                }
                                OutlinedButton(
                                    onClick = {
                                        isSelected = !isSelected
                                        if (isSelected) {
                                            participants.add(username)
                                        } else {
                                            participants.remove(username)
                                        }
                                        println("Participants: ${participants.joinToString(", ")}")
                                    },
                                    shape = RoundedCornerShape(12.dp),
                                    border = BorderStroke(1.dp, ThirdColor),
                                    modifier = Modifier
                                        .padding(end = 4.dp)
                                        .width(45.dp)
                                        .height(30.dp),
                                    contentPadding = PaddingValues(0.dp),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = if (isSelected) ThirdColor else Color.Transparent,
                                        contentColor = if (isSelected) SecondColor else ThirdColor,
                                        disabledContainerColor = if (isSelected) ThirdColor else ThirdColor,
                                        disabledContentColor = if (isSelected) SecondColor else ThirdColor
                                    )
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Check,
                                        contentDescription = "Add friend to list",
                                        tint = if (isSelected) SecondColor else ThirdColor,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                        }
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(35.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    OutlinedTextField(value = itemName,
                        onValueChange = { itemName = it },
                        label = { Text("Item name", color = FirstColor, fontSize = 18.sp, fontFamily = FontFamily(Font(R.font.playfairdisplay))) },
                        textStyle = TextStyle(
                            color = FirstColor,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily(Font(R.font.playfairdisplay))
                        ),
                        modifier = Modifier.fillMaxWidth(0.6f),
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            cursorColor = FirstColor,
                            focusedBorderColor = FirstColor,
                            unfocusedBorderColor = FirstColor,
                            focusedContainerColor = FourthColor,
                            unfocusedContainerColor = FourthColor
                        )
                    )

                    OutlinedTextField(value = itemQuantity,
                        onValueChange = { itemQuantity = it },
                        label = { Text("Item quantity", color = FirstColor, fontSize = 18.sp, fontFamily = FontFamily(Font(R.font.playfairdisplay))) },
                        textStyle = TextStyle(
                            color = FirstColor,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily(Font(R.font.playfairdisplay))
                        ),
                        modifier = Modifier.fillMaxWidth(0.94f),
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            cursorColor = FirstColor,
                            focusedBorderColor = FirstColor,
                            unfocusedBorderColor = FirstColor,
                            focusedContainerColor = FourthColor,
                            unfocusedContainerColor = FourthColor
                        )
                    )
                }

                Button(
                    onClick = {
                        if (itemName.isNotEmpty() && itemQuantity.isNotEmpty()){
                            itemList = itemList.plus(itemName to itemQuantity)
                            itemName = ""
                            itemQuantity = ""
                            println("Item list: $itemList")
                        }
                        else {
                            Toast.makeText(context, "Item name or quantity can't be empty", Toast.LENGTH_SHORT).show()
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = ThirdColor,
                        contentColor = Color.White
                    )
                ) {
                    Text(
                        text = "Add Item",
                        fontSize = 17.sp,
                        fontFamily = FontFamily(Font(R.font.playfairdisplay))
                    )
                }

                Spacer(modifier = Modifier.height(35.dp))
            }

            item {
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
                            text = "Items",
                            color = FirstColor,
                            fontSize = 20.sp
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        itemList.forEach { (name, quantity) ->
                            Row(
                                modifier = Modifier.fillMaxWidth(0.95f),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    modifier = Modifier
                                        .weight(0.7f)
                                        .padding(start = 16.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "•   ",
                                        color = FirstColor,
                                        fontSize = 20.sp
                                    )
                                    Text(
                                        text = "$name - ",
                                        color = FirstColor,
                                        fontSize = 20.sp,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                    Text(
                                        text = "$quantity",
                                        color = FirstColor,
                                        fontSize = 20.sp,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                }
                                IconButton(
                                    onClick = {
                                        itemList = itemList.filter { it.first != name}
                                    },
                                    modifier = Modifier
                                        .padding(end = 4.dp)
                                        .width(45.dp)
                                        .height(30.dp),
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Delete,
                                        contentDescription = "Delete Item",
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


            item {
                Button(
                    onClick = {
                        if (listName.isNotEmpty() && itemList.isNotEmpty()) {
                            val formattedFriends = participants.map { participantUsername ->
                                participantUsername
                            }.toList()

                            val formattedItems = itemList.map { (name, quantity) ->
                                hashMapOf(
                                    "itemName" to name,
                                    "itemQuantity" to quantity,
                                    "isBought" to false
                                )
                            }


                            db.collection("lists").add(
                                hashMapOf(
                                    "name" to listName,
                                    "friends" to formattedFriends,
                                    "items" to formattedItems,
                                    "totalCost" to 0.00
                                )
                            ).addOnSuccessListener { documentReference ->
                                val listId = documentReference.id

                                participants.forEach { participantUsername ->
                                    db.collection("users")
                                        .whereEqualTo("username", participantUsername)
                                        .get()
                                        .addOnSuccessListener { documents ->
                                            if (!documents.isEmpty) {
                                                val participantId = documents.documents.first().id
                                                db.collection("users").document(participantId)
                                                    .update("lists", FieldValue.arrayUnion(listId))
                                                    .addOnSuccessListener {
                                                        navController.navigate("list_screen")
                                                    }
                                            }
                                        }
                                        .addOnFailureListener { e ->
                                            errorMessage = e.message ?: "Error saving the list to the participants' documents"
                                            Toast.makeText(context, errorMessage, Toast.LENGTH_SHORT).show()
                                        }
                                }
                            }.addOnFailureListener { e ->
                                errorMessage = e.message ?: "Error saving list"
                                Toast.makeText(context, errorMessage, Toast.LENGTH_SHORT).show()
                            }
                        } else {
                            Toast.makeText(context, "Please enter a list name and at least one item", Toast.LENGTH_SHORT).show()
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = ThirdColor,
                        contentColor = Color.White
                    )
                ) {
                    Text(
                        text = "Save List",
                        fontSize = 17.sp,
                        fontFamily = FontFamily(Font(R.font.playfairdisplay))
                    )
                }
            }
        }
    }
}