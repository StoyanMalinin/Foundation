import { FoundationBackend } from '@/backend/foundation-backend';
import * as SecureStore from 'expo-secure-store';
import { jwtDecode } from "jwt-decode";

function isBrowser() {
    return typeof window !== 'undefined' && typeof window.document !== 'undefined';
}

export async function saveToken(tokenName: string, token: string) {
    console.log("Saving token:", tokenName);
    if (isBrowser()) {
        window.localStorage.setItem(tokenName, token);
        console.log("Token saved to localStorage.");
    } else {
        try {
            await SecureStore.setItemAsync(tokenName, token);
            console.log("Token saved to SecureStore successfully.");
        } catch (error) {
            console.error("Error saving token to SecureStore:", tokenName, error);
        }
    }
}

export async function getToken(tokenName: string): Promise<string | null> {
    console.log("Getting token:", tokenName);
    if (isBrowser()) {
        const item = window.localStorage.getItem(tokenName);
        console.log("Retrieved from localStorage:", item);
        return item;
    } else {
        try {
            const token = await SecureStore.getItemAsync(tokenName);
            console.log("Token retrieved from SecureStore:", token);
            // SecureStore.getItemAsync returns null if the key does not exist
            return token;
        } catch (error) {
            console.error("Error getting token from SecureStore:", tokenName, error);
            return null;
        }
    }
}

export async function isLoggedIn() {
    const jwt = await getToken("jwt");
    if (jwt == null) return false;

    const decoded = jwtDecode(jwt);
    console.log("decoded", decoded);
    console.log("now", Date.now());
    if (decoded?.exp && decoded?.exp * 1000 > Date.now()) {
        return true;
    }

    const refreshToken = await getToken("refreshToken");
    if (refreshToken == null) return false;

    const result = await FoundationBackend.refreshJWT(refreshToken);
    if (!result.ok) return false;

    const newJWT = (await result.json()).jwt;
    if (!newJWT) return false;

    await saveToken("jwt", newJWT);
    return true;
}

export async function login(username: string, password: string): Promise<string | null> {
    const result = await FoundationBackend.login(username, password);
    if (!result.ok) return result.text();

    const json = await result.json();
    const jwt = json["jwt"];
    const refreshToken = json["refresh_token"];
    if (!jwt || !refreshToken) return "Server result is malformed";

    await saveToken("jwt", jwt);
    await saveToken("refreshToken", refreshToken);
    return null;
}
