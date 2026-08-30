package com.hr.hrapp.service;

import com.warrenstrange.googleauth.GoogleAuthenticator;
import com.warrenstrange.googleauth.GoogleAuthenticatorKey;
import org.springframework.stereotype.Service;

@Service
public class MfaService {

    private final GoogleAuthenticator googleAuthenticator =
            new GoogleAuthenticator();

    public String generateSecret() {
        GoogleAuthenticatorKey key =
                googleAuthenticator.createCredentials();

        return key.getKey();
    }

    public boolean verifyCode(String secret, int code) {
        return googleAuthenticator.authorize(secret, code);
    }
}
