package com.vi5hnu.notesapp.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.vi5hnu.notesapp.screen.NoteScreen
import com.vi5hnu.notesapp.screen.Screens

@Composable
fun NoteNavigation() {
    val navController= rememberNavController()
    NavHost(navController = navController, startDestination = Screens.HomeScreen.name ){
        composable(Screens.HomeScreen.name){
            NoteScreen(navController=navController);
        }
    }
}