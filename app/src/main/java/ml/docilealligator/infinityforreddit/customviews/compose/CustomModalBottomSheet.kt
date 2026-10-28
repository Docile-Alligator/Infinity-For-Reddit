package ml.docilealligator.infinityforreddit.customviews.compose

import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SheetState
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CustomModalBottomSheet(
    sheetState: SheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
    onDismissRequest: () -> Unit,
    content: @Composable ColumnScope.() -> Unit
) {
    ModalBottomSheet(
        containerColor = Color(LocalAppTheme.current.backgroundColor),
        onDismissRequest = onDismissRequest,
        sheetState = sheetState,
        content = content
    )
}