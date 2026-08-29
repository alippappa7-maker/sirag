package com.siraj.app.core.accessibility

import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.semantics.*
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Provides semantic enhancements and TalkBack/VoiceOver accessibility helpers
 * tailored for Arabic RTL layout hierarchy and WCAG 2.1 AA/AAA compliance.
 */
object AccessibilitySemantics {

    /**
     * Enforces the standard minimum touch target size (48x48 dp) as mandated by Material 3 and WCAG 2.5.5.
     */
    fun Modifier.sirajTouchTarget(minSize: Dp = 48.dp): Modifier = this
        .minimumInteractiveComponentSize()
        .defaultMinSize(minWidth = minSize, minHeight = minSize)

    /**
     * Sets semantic traversal index and groupings to enforce natural Right-To-Left reading order for TalkBack.
     */
    fun Modifier.sirajRtlTraversal(
        traversalIndex: Float,
        isGroup: Boolean = false
    ): Modifier = this.semantics {
        if (isGroup) {
            isTraversalGroup = true
        }
        this.traversalIndex = traversalIndex
    }

    /**
     * Marks a text element as a Section Heading for TalkBack navigation ("العناوين").
     */
    fun Modifier.sirajHeading(): Modifier = this.semantics {
        heading()
    }

    /**
     * Adds an accessible clickable behavior with explicit action labels, roles, and minimum touch target.
     */
    fun Modifier.sirajClickable(
        onClickLabel: String? = null,
        role: Role? = Role.Button,
        enabled: Boolean = true,
        onLongClickLabel: String? = null,
        onClick: () -> Unit
    ): Modifier = composed {
        this
            .sirajTouchTarget()
            .clickable(
                enabled = enabled,
                onClickLabel = onClickLabel,
                role = role,
                onClick = onClick
            )
            .semantics {
                if (onLongClickLabel != null) {
                    customActions = listOf(
                        CustomAccessibilityAction(onLongClickLabel) {
                            onClick()
                            true
                        }
                    )
                }
            }
    }

    /**
     * Describes the full state of a verification claim or content status clearly without relying solely on color.
     */
    fun Modifier.sirajStatusDescription(
        statusText: String,
        role: Role = Role.Button,
        isError: Boolean = false,
        errorMessage: String? = null
    ): Modifier = this.semantics {
        this.role = role
        stateDescription = statusText
        if (isError && errorMessage != null) {
            error(errorMessage)
        }
    }

    /**
     * Marks dynamic UI updates to be politely or assertively announced by TalkBack.
     */
    fun Modifier.sirajLiveRegion(isAssertive: Boolean = false): Modifier = this.semantics {
        liveRegion = if (isAssertive) LiveRegionMode.Assertive else LiveRegionMode.Polite
    }
}
