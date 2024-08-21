package com.example.rmaprojekt

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore


data class ListDetails(
    val listName: String,
    val friends: List<String>,
    var boughtItems: Int,
    var totalItems: Int
)


@Composable
fun ListScreen(navController: NavHostController) {
    val db = FirebaseFirestore.getInstance()
    val currentUser = FirebaseAuth.getInstance().currentUser
    val currentUserId = currentUser?.uid ?: ""
    var lists by remember { mutableStateOf(listOf<ListDetails>()) }

    LaunchedEffect(Unit) {
        db.collection("users").document(currentUserId)
            .get()
            .addOnSuccessListener { document ->
                val userLists = document.get("lists") as? List<String> ?: emptyList()
                userLists.forEach { listId ->
                    db.collection("lists").document(listId).get().addOnSuccessListener { document ->
                        val listName = document.getString("name") ?: "Unnamed List"
                        val friends = document.get("friends") as? List<String> ?: emptyList()
                        val itemsList = document.get("items") as? List<HashMap<String, Any>> ?: emptyList()
                        val boughtItems = mutableStateOf(0)
                        val totalItems = mutableStateOf(0)
                            itemsList.forEach { item ->
                                val isBought = mutableStateOf(item["isBought"] as? Boolean ?: false)
                                if (isBought.value) boughtItems.value++
                                    totalItems.value++
                            }
                            lists = lists + ListDetails(listName, friends, boughtItems.value, totalItems.value)
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
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(20.dp),
                verticalArrangement = Arrangement.Top,
                horizontalAlignment = Alignment.Start
            ) {
                item{
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Lists",
                            textAlign = TextAlign.Left,
                            fontSize = 60.sp,
                            color = FirstColor,
                            fontWeight = FontWeight.Bold
                        )
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.BottomEnd
                        ) {
                            IconButton(
                                onClick = {
                                    navController.navigate("create_list_screen")
                                }, modifier = Modifier.padding(top = 16.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(48.dp)
                                        .border(1.dp, color = FirstColor, shape = CircleShape),
                                    contentAlignment = Alignment.Center,
                                    content = {
                                    Box(
                                        modifier = Modifier.size(36.dp).background(
                                                color = SecondColor,
                                                shape = CircleShape
                                            ),
                                        contentAlignment = Alignment.Center,
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Add,
                                            contentDescription = "Create New List",
                                            tint = FirstColor
                                        )
                                    }
                                })
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(20.dp))
                }

                item{
                    lists.forEach { list ->

                        Button(
                            onClick = {
                                navController.navigate("list_details_screen/${list.listName}")
                            },

                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 8.dp),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = FourthColor, contentColor = FirstColor
                            )
                        ) {
                            Column(
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "${list.listName}",
                                        fontSize = 25.sp,
                                        fontWeight = FontWeight.Bold,
                                        textAlign = TextAlign.Center,
                                        modifier = Modifier.padding(bottom = 4.dp),
                                        fontFamily = FontFamily(Font(R.font.playfairdisplay))
                                    )
                                    Text(
                                        text = "${list.boughtItems}/${list.totalItems}",
                                        color = FirstColor,
                                        fontSize = 20.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                                Spacer(modifier = Modifier.height(10.dp))
                                Text(
                                    text = "Friends: ${list.friends.joinToString()}",
                                    fontSize = 20.sp,
                                    textAlign = TextAlign.Center,
                                    fontFamily = FontFamily(Font(R.font.playfairdisplay))
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}