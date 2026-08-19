package com.hermex.domain.chat

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class AgentRunStateTest {
    @Test
    fun textDeltasAccumulateWithoutReplacingState() {
        var state: AgentRunState = AgentRunState.Idle
        state = state.reduce(AgentRunEvent.Started("run-1"))
        state = state.reduce(AgentRunEvent.TextDelta("Hello "))
        state = state.reduce(AgentRunEvent.TextDelta("world"))

        assertEquals("Hello world", (state as AgentRunState.Running).assistantText)
    }

    @Test
    fun toolLifecycleBecomesCompletedAndLeavesHistory() {
        var state: AgentRunState = AgentRunState.Running("run-1")
        state = state.reduce(
            AgentRunEvent.ToolStarted(
                ToolRun(id = "tool-1", name = "shell")
            )
        )
        state = state.reduce(AgentRunEvent.ToolOutput("tool-1", "hello"))
        state = state.reduce(AgentRunEvent.ToolCompleted("tool-1", "hello"))

        val running = state as AgentRunState.Running
        assertTrue(running.activeTool == null)
        assertEquals(ToolStatus.Completed, running.toolHistory.single().status)
        assertEquals("hello", running.toolHistory.single().outputPreview)
    }

    @Test
    fun approvalPromotesRunningStateWithoutLosingTranscript() {
        var state: AgentRunState = AgentRunState.Running("run-1", assistantText = "Before")
        state = state.reduce(
            AgentRunEvent.ApprovalRequired(
                ApprovalRequest("approval-1", "Run command", "rm -rf ./build", danger = true)
            )
        )

        val approval = state as AgentRunState.AwaitingApproval
        assertEquals("Before", approval.assistantText)
        assertTrue(approval.approval.danger)
    }

    @Test
    fun completedEventProducesTerminalState() {
        val state = AgentRunState.Running("run-1", assistantText = "done")
            .reduce(AgentRunEvent.Completed)

        assertEquals("done", (state as AgentRunState.Completed).assistantText)
    }
}
