package com.example.trainboard
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.collection.emptyLongSet
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.input.rememberTextFieldState
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.trainboard.ui.theme.TrainBoardTheme
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import java.util.SimpleTimeZone
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat.startActivity
import androidx.core.net.toUri


class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            TrainBoardTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    Page(modifier = Modifier.padding(innerPadding))
                }
            }
        }
    }
}
@Composable
fun Page(modifier: Modifier)
{

    val selectedOptionTextFrom = remember { mutableStateOf("")}
    val selectedOptionTextTo = remember { mutableStateOf("")}
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(color = Color.Red),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ){
        Text("LNER", color = Color.White, fontSize = 25.sp, modifier = Modifier.padding(top = 0.dp))
        Row (){
            SimpleExposedDropdown("From", selectedOptionTextFrom)
            SimpleExposedDropdown("To", selectedOptionTextTo)

        }
        ButtonFromTo(selectedOptionTextFrom, selectedOptionTextTo)
    }

}
@Composable
fun ButtonFromTo (selectedFrom: MutableState<String>, selectedTo: MutableState<String>)
{   val context = LocalContext.current
    val buttonText = remember { mutableStateOf("Find route") }
    val codeDictionary = mutableMapOf("London" to "KGX", "Edinburgh" to "EDB", "Oxford" to "OXF", "Bristol" to "BRI", "Liverpool" to "LVC")
    val url = "https://www.lner.co.uk/travel-information/travelling-now/live-train-times/depart/${codeDictionary[selectedFrom.value]}/${codeDictionary[selectedTo.value]}/#tab_livedepartures"
    Button(

        onClick = {

            if (selectedFrom.value != "" && selectedTo.value != "")
            {
                val intent = Intent(Intent.ACTION_VIEW, url.toUri())

                context.startActivity(intent)
            }
            else {

                buttonText.value = "Stations not chosen"
            }

        },
        colors = ButtonDefaults.buttonColors(
            containerColor = Color.White,
            contentColor = Color.Black
        )
    )
    {
        Text(buttonText.value)
    }


}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SimpleExposedDropdown(name: String, selectedOptionText: MutableState<String>) {
    val options = listOf("London", "Edinburgh", "Oxford", "Liverpool", "Bristol")
    var expanded by remember { mutableStateOf(false) }
    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = !expanded }
    ) {
        TextField(
            value = selectedOptionText.value,
            onValueChange = {},
            readOnly = true,
            label = { Text(name) },
            trailingIcon = {
                ExposedDropdownMenuDefaults.TrailingIcon(expanded)
            },
            modifier = Modifier.menuAnchor().clickable { expanded = !expanded }.padding(5.dp).width(150.dp)
        )

        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            options.forEach { selectionOption ->
                DropdownMenuItem(
                    text = { Text(selectionOption) },
                    onClick = {
                        selectedOptionText.value = selectionOption
                        expanded = false
                    }
                )
            }
        }
    }
}

@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
    Text(
        text = "Hello $name!",
        modifier = modifier
    )
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    TrainBoardTheme {
        Greeting("Arav")
    }
}