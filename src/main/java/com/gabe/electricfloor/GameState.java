package com.gabe.electricfloor;

public enum GameState {
    WAITING(true),STARTING(false),INGAME(false),ENDING(false);

    private boolean canJoin;

    private GameState currentState;

    GameState(boolean canJoin){
        this.canJoin = canJoin;
    }

    public boolean canJoin(){
        return  canJoin;
    }


}
