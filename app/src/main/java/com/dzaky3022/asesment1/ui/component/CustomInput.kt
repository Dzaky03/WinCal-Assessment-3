package com.dzaky3022.asesment1.ui.component

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.selection.TextSelectionColors
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.ArrowDropUp
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.dzaky3022.asesment1.ui.theme.BackgroundDark
import com.dzaky3022.asesment1.ui.theme.Danger
import com.dzaky3022.asesment1.ui.theme.Gray
import com.dzaky3022.asesment1.ui.theme.WhiteTitle

@Composable
fun <T> CustomInput(
    label: String = "",
    labelColor: Color? = null,
    textColor: Color = Color.Black,
    hint: String,
    helperText: String? = null,
    helperTextColor: Color? = null,
    initialValue: String = "",
    isEnabled: Boolean = true,
    isEmail: Boolean = false,
    isDarkMode: Boolean = false,
    isDigitOnly: Boolean = false,
    isRequired: Boolean = false,
    maxLength: Int? = null,
    maxLines: Int = 1,
    imeAction: ImeAction = ImeAction.Next,
    isSuffixDropdown: Boolean = false,
    options: List<T>? = listOf(),
    selectedOption: T? = null,
    optionLabel: (T?) -> String = {""},
    onOptionSelected: (T?) -> Unit= {},
    onChange: (String) -> Unit = {},
    suffixIcon: @Composable (() -> Unit)? = null,
    prefixIcon: @Composable (() -> Unit)? = null
) {
    var expanded by remember { mutableStateOf(false) }

    Column(modifier = Modifier.fillMaxWidth()) {
        if (label.isNotEmpty()) {
            Row(
                verticalAlignment = Alignment.Top
            ) {
                Text(
                    text = label,
                    fontSize = 14.sp,
                    color = labelColor ?: if (isDarkMode) WhiteTitle else Color.Black,
                    modifier = Modifier.padding(bottom = 4.dp),
                    fontWeight = FontWeight.SemiBold,
                )
                if (isRequired) {
                    Spacer(Modifier.width(4.dp))
                    Text(
                        "*", color = Danger, fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                    )
                }
            }
        }

        OutlinedTextField(
            shape = RoundedCornerShape(10.dp),
            colors = TextFieldDefaults.colors(
                selectionColors = TextSelectionColors(
                    handleColor = BackgroundDark,
                    backgroundColor = Color.Black.copy(alpha = .5f)
                ),
                focusedContainerColor = Color.White,
                unfocusedContainerColor = Color.White,
                disabledContainerColor = Color.White,
                focusedIndicatorColor = Gray,
                unfocusedIndicatorColor = Gray,
                disabledIndicatorColor = Gray,
            ),
            value = initialValue,
            onValueChange = {
                if (maxLength == null || it.length <= maxLength) {
                    onChange(it)
                }
            },
            enabled = isEnabled,
            singleLine = maxLines == 1,
            keyboardOptions = KeyboardOptions.Default.copy(
                keyboardType = when {
                    isDigitOnly -> KeyboardType.Number
                    isEmail -> KeyboardType.Email
                    else -> KeyboardType.Text
                },
                imeAction = imeAction
            ),
            textStyle = LocalTextStyle.current.copy(fontSize = 16.sp, color = textColor),
            placeholder = { Text(text = hint, color = Color.Gray) },
            modifier = Modifier.fillMaxWidth(),
            suffix = if (isSuffixDropdown) {
                {
                    Box {
                        Row(modifier = Modifier.clickable{ expanded = true }, verticalAlignment = Alignment.CenterVertically) {
                            Text(text = optionLabel(selectedOption), color = Color.Gray, fontSize = 16.sp)
                            Icon(if (!expanded) Icons.Default.ArrowDropDown else Icons.Default.ArrowDropUp, contentDescription = "Dropdown", tint = BackgroundDark)
                        }
                        DropdownMenu(
                            expanded = expanded,
                            onDismissRequest = { expanded = false },
                            modifier = Modifier.background(Color.White)
                        ) {
                            options?.forEach { option ->
                                DropdownMenuItem(text = { Text(optionLabel(option), fontSize = 16.sp) }, onClick = {
                                    onOptionSelected(option)
                                    expanded = false
                                })
                            }
                        }
                    }
                }
            } else suffixIcon,
            prefix = prefixIcon,
        )

        if (helperText != null) {
            Text(
                text = helperText,
                fontSize = 12.sp,
                color = helperTextColor ?: Color.Gray,
                modifier = Modifier.padding(top = 4.dp)
            )
        }
    }
}
