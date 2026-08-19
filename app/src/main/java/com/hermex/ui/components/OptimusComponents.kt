package com.hermex.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ArrowUpward
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material.icons.outlined.ContentCopy
import androidx.compose.material.icons.outlined.MoreHoriz
import androidx.compose.material.icons.outlined.Pause
import androidx.compose.material.icons.outlined.PlayArrow
import androidx.compose.material.icons.outlined.Stop
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.hermex.ui.theme.OptimusBackground
import com.hermex.ui.theme.OptimusDivider
import com.hermex.ui.theme.OptimusError
import com.hermex.ui.theme.OptimusInfo
import com.hermex.ui.theme.OptimusPrimary
import com.hermex.ui.theme.OptimusSuccess
import com.hermex.ui.theme.OptimusSurface
import com.hermex.ui.theme.OptimusSurfaceElevated
import com.hermex.ui.theme.OptimusTextPrimary
import com.hermex.ui.theme.OptimusTextSecondary
import com.hermex.ui.theme.OptimusTextTertiary
import com.hermex.ui.theme.OptimusWarning

@Composable
fun OptimusStatusPill(
    text: String,
    tone: OptimusTone = OptimusTone.Neutral,
    modifier: Modifier = Modifier
) {
    val accent = when (tone) {
        OptimusTone.Neutral -> OptimusTextSecondary
        OptimusTone.Info -> OptimusInfo
        OptimusTone.Success -> OptimusSuccess
        OptimusTone.Warning -> OptimusWarning
        OptimusTone.Error -> OptimusError
    }
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(999.dp),
        color = accent.copy(alpha = 0.10f),
        contentColor = accent
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Box(Modifier.size(6.dp).clip(CircleShape).background(accent))
            Text(text, style = MaterialTheme.typography.labelMedium, maxLines = 1)
        }
    }
}

enum class OptimusTone { Neutral, Info, Success, Warning, Error }

@Composable
fun OptimusGlassCard(
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    content: @Composable () -> Unit
) {
    val shape = RoundedCornerShape(20.dp)
    Card(
        modifier = modifier
            .clip(shape)
            .then(
                if (onClick != null) Modifier.clickable(role = Role.Button, onClick = onClick)
                else Modifier
            )
            .border(1.dp, OptimusDivider.copy(alpha = 0.55f), shape)
            .animateContentSize(animationSpec = spring(stiffness = Spring.StiffnessMediumLow)),
        colors = CardDefaults.cardColors(containerColor = OptimusSurfaceElevated),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        content()
    }
}

@Composable
fun OptimusToolCard(
    title: String,
    subtitle: String? = null,
    outputPreview: String? = null,
    running: Boolean = false,
    status: OptimusTone = if (running) OptimusTone.Info else OptimusTone.Success,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }
    OptimusGlassCard(modifier = modifier, onClick = { expanded = !expanded }) {
        Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                OptimusStatusPill(if (running) "Running" else "Completed", status)
                Spacer(Modifier.width(10.dp))
                Text(title, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Medium, maxLines = 1, overflow = TextOverflow.Ellipsis)
                Spacer(Modifier.weight(1f))
                Icon(Icons.Outlined.MoreHoriz, contentDescription = "More tool actions", tint = OptimusTextTertiary)
            }
            subtitle?.let { Text(it, style = MaterialTheme.typography.bodySmall, color = OptimusTextSecondary, maxLines = 2, overflow = TextOverflow.Ellipsis) }
            AnimatedVisibility(
                visible = expanded && !outputPreview.isNullOrBlank(),
                enter = fadeIn() + expandVertically(animationSpec = spring(stiffness = Spring.StiffnessMediumLow)),
                exit = fadeOut() + shrinkVertically()
            ) {
                Surface(shape = RoundedCornerShape(12.dp), color = OptimusBackground) {
                    Row(Modifier.horizontalScroll(rememberScrollState()).padding(12.dp)) {
                        Text(outputPreview.orEmpty(), style = MaterialTheme.typography.bodySmall, color = OptimusTextSecondary)
                    }
                }
            }
        }
    }
}

@Composable
fun OptimusApprovalCard(
    title: String,
    detail: String,
    onApprove: () -> Unit,
    onReject: () -> Unit,
    modifier: Modifier = Modifier
) {
    OptimusGlassCard(modifier = modifier) {
        Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            OptimusStatusPill("Approval required", OptimusTone.Warning)
            Text(title, style = MaterialTheme.typography.titleMedium, color = OptimusTextPrimary)
            Text(detail, style = MaterialTheme.typography.bodyMedium, color = OptimusTextSecondary)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                TextButton(onClick = onReject) { Text("Reject", color = OptimusError) }
                Spacer(Modifier.weight(1f))
                AssistChip(onClick = onApprove, label = { Text("Approve") }, leadingIcon = { Icon(Icons.Outlined.Check, contentDescription = null) })
            }
        }
    }
}

@Composable
fun OptimusRunControls(
    running: Boolean,
    onStop: () -> Unit,
    onPause: () -> Unit,
    onResume: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(modifier, horizontalArrangement = Arrangement.spacedBy(4.dp), verticalAlignment = Alignment.CenterVertically) {
        IconButton(onClick = onStop, modifier = Modifier.semantics { contentDescription = "Stop run" }) {
            Icon(Icons.Outlined.Stop, contentDescription = null, tint = OptimusError)
        }
        IconButton(
            onClick = if (running) onPause else onResume,
            modifier = Modifier.semantics { contentDescription = if (running) "Pause run" else "Resume run" }
        ) {
            Icon(if (running) Icons.Outlined.Pause else Icons.Outlined.PlayArrow, contentDescription = null)
        }
    }
}

@Composable
fun OptimusComposerHint(
    model: String,
    profile: String,
    modifier: Modifier = Modifier
) {
    Row(modifier, horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
        OptimusStatusPill(model, OptimusTone.Info)
        OptimusStatusPill(profile, OptimusTone.Neutral)
    }
}
