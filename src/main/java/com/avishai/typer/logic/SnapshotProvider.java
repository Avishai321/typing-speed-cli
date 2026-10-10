package com.avishai.typer.logic;

public interface SnapshotProvider {
    TypingState.TextSnapshot getTextSnapshot();
    TypingState.StatesSnapshot getStatesSnapshot();
}
