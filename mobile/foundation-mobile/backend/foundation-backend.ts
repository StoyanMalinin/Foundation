import { getToken } from "@/app/auth/utils";

const BACKEND_API_BASE_URL = "https://ffoundationn.fun:6969"

class _FoundationBackend {
    login(username: string, password: string) {
        return fetch(`${BACKEND_API_BASE_URL}/login-mobile`, {
            method: "POST",
            headers: {
                "Content-Type": "application/json"
            },
            body: JSON.stringify({ username, password })
        });
    }

    searchesMetadata() {
        return fetch(`${BACKEND_API_BASE_URL}/searches-metadata`, {
            method: "GET",
            headers: {
                "Content-Type": "application/json"
            }
        });
    }

    refreshJWT(refreshToken: string) {
        return fetch(`${BACKEND_API_BASE_URL}/refresh-jwt-mobile`, {
            method: "POST",
            headers: {
                "Authorization": `Bearer ${refreshToken}`
            }
        })
    }

    async injectPresences(
        searchIds: number[], 
        presences: {lat: number, lon: number, recorded_at: number}[]
    ) {
        return fetch(`${BACKEND_API_BASE_URL}/inject-presences`, {
            method: "PUT",
            headers: {
                "Content-Type": "application/json"
            },
            body: JSON.stringify({ 
                "search_ids": searchIds,
                "presences": presences,
                "jwt": (await getToken("jwt")),
            })
        });
    }
}

export const FoundationBackend = new _FoundationBackend();