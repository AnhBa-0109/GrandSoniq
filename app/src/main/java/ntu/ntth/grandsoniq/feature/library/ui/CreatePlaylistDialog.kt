package ntu.ntth.grandsoniq.feature.library.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ntu.ntth.grandsoniq.ui.theme.GrandSoniqAccent
import ntu.ntth.grandsoniq.ui.theme.GrandSoniqDivider
import ntu.ntth.grandsoniq.ui.theme.GrandSoniqElevated
import ntu.ntth.grandsoniq.ui.theme.GrandSoniqOnAccent
import ntu.ntth.grandsoniq.ui.theme.GrandSoniqPanel
import ntu.ntth.grandsoniq.ui.theme.GrandSoniqTextMuted
import ntu.ntth.grandsoniq.ui.theme.GrandSoniqTextPrimary
import ntu.ntth.grandsoniq.ui.theme.GrandSoniqTheme

@Composable
fun CreatePlaylistDialog(
    initialName: String = "",
    initialDescription: String = "",
    initialIsPublic: Boolean = true,
    initialIsCollaborative: Boolean = false,
    isEditing: Boolean = false,
    onDismiss: () -> Unit,
    onConfirm: (name: String, description: String, isPublic: Boolean, isCollaborative: Boolean) -> Unit
) {
    var name by remember(initialName) { mutableStateOf(initialName) }
    var description by remember(initialDescription) { mutableStateOf(initialDescription) }
    var isPublic by remember(initialIsPublic) { mutableStateOf(initialIsPublic) }
    var isCollaborative by remember(initialIsCollaborative) { mutableStateOf(initialIsCollaborative) }

    val fieldColors = OutlinedTextFieldDefaults.colors(
        focusedBorderColor = GrandSoniqAccent,
        unfocusedBorderColor = GrandSoniqDivider,
        focusedTextColor = GrandSoniqTextPrimary,
        unfocusedTextColor = GrandSoniqTextPrimary,
        cursorColor = GrandSoniqAccent,
        focusedLabelColor = GrandSoniqAccent,
        unfocusedLabelColor = GrandSoniqTextMuted
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = GrandSoniqPanel,
        titleContentColor = GrandSoniqTextPrimary,
        title = {
            Text(
                text = if (isEditing) "Chỉnh sửa playlist" else "Tạo playlist mới",
                fontWeight = FontWeight.Bold,
                fontSize = 19.sp
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Tên playlist") },
                    singleLine = true,
                    colors = fieldColors,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Mô tả (không bắt buộc)") },
                    maxLines = 3,
                    colors = fieldColors,
                    modifier = Modifier.fillMaxWidth()
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Hiển thị công khai", color = GrandSoniqTextPrimary, fontSize = 14.sp)
                    Switch(
                        checked = isPublic,
                        onCheckedChange = { isPublic = it },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = GrandSoniqOnAccent,
                            checkedTrackColor = GrandSoniqAccent,
                            uncheckedTrackColor = GrandSoniqElevated
                        )
                    )
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Playlist cộng tác (Mời bạn)", color = GrandSoniqTextPrimary, fontSize = 14.sp)
                    Switch(
                        checked = isCollaborative,
                        onCheckedChange = { isCollaborative = it },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = GrandSoniqOnAccent,
                            checkedTrackColor = GrandSoniqAccent,
                            uncheckedTrackColor = GrandSoniqElevated
                        )
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { onConfirm(name, description, isPublic, isCollaborative) },
                enabled = name.isNotBlank(),
                shape = RoundedCornerShape(50),
                colors = ButtonDefaults.buttonColors(
                    containerColor = GrandSoniqAccent,
                    contentColor = GrandSoniqOnAccent
                )
            ) {
                Text(if (isEditing) "Lưu" else "Tạo", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Hủy", color = GrandSoniqTextMuted)
            }
        }
    )
}

@Preview(showBackground = true, backgroundColor = 0xFF111514)
@Composable
private fun CreatePlaylistDialogPreview() {
    GrandSoniqTheme(darkTheme = true, dynamicColor = false) {
        CreatePlaylistDialog(
            initialName = "Đường về nhà",
            initialDescription = "Nhạc chill buổi tối",
            initialIsPublic = true,
            initialIsCollaborative = true,
            onDismiss = {},
            onConfirm = { _, _, _, _ -> }
        )
    }
}
