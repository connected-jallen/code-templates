package com.connectedlab.templates.error;

import android.os.Bundle;

public class AppException extends RuntimeException{

    private ErrorInfo error;

    public AppException(ErrorInfo error){
        super(error.getDebugMessage(), error.getException());
        this.error = error;
    }

    public ErrorInfo getError() {
        return error;
    }


}
